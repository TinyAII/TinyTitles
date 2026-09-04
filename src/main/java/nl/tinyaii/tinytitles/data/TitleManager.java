package nl.tinyaii.tinytitles.data;

import nl.tinyaii.tinytitles.TinyTitlesPlugin;
import nl.tinyaii.tinytitles.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 称号管理：称号定义（config）+ 玩家称号存储（data.yml）+ 显示称号。
 */
public class TitleManager {

    /** 称号定义 */
    public static class TitleDef {
        public String name;
        public String color;           // 如 &e
        public String desc;
        public boolean broadcastOnJoin;
    }

    private final TinyTitlesPlugin plugin;
    /** 玩家 → 拥有的称号列表 */
    private final Map<UUID, List<String>> playerTitles = new HashMap<>();
    /** 玩家 → 当前显示的称号列表（最多 max-display 个） */
    private final Map<UUID, List<String>> displayed = new HashMap<>();
    /** 称号名 → 定义 */
    private final Map<String, TitleDef> titles = new HashMap<>();
    private File file;

    public TitleManager(TinyTitlesPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        titles.clear();
        // 从 config 读称号定义
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("titles");
        if (sec != null) {
            for (String name : sec.getKeys(false)) {
                ConfigurationSection t = sec.getConfigurationSection(name);
                TitleDef def = new TitleDef();
                def.name = name;
                def.color = t.getString("color", "&f");
                def.desc = t.getString("desc", "");
                def.broadcastOnJoin = t.getBoolean("broadcast-on-join", false);
                titles.put(name, def);
            }
        }
        // 读玩家称号
        playerTitles.clear();
        displayed.clear();
        file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) return;
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection pt = yml.getConfigurationSection("player-titles");
        if (pt != null) {
            for (String key : pt.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    playerTitles.put(uuid, new ArrayList<>(pt.getStringList(key)));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        ConfigurationSection dp = yml.getConfigurationSection("displayed");
        if (dp != null) {
            for (String key : dp.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    displayed.put(uuid, new ArrayList<>(dp.getStringList(key)));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void save() {
        YamlConfiguration yml = new YamlConfiguration();
        for (Map.Entry<UUID, List<String>> e : playerTitles.entrySet()) {
            yml.set("player-titles." + e.getKey(), e.getValue());
        }
        for (Map.Entry<UUID, List<String>> e : displayed.entrySet()) {
            yml.set("displayed." + e.getKey(), e.getValue());
        }
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            yml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("保存称号数据失败: " + ex.getMessage());
        }
    }

    // ===== 称号定义 =====
    public Map<String, TitleDef> getTitles() { return titles; }
    public TitleDef getTitle(String name) { return titles.get(name); }
    public boolean hasTitleDef(String name) { return titles.containsKey(name); }

    public void createTitle(String name, String color, String desc, boolean broadcast) {
        TitleDef def = new TitleDef();
        def.name = name;
        def.color = color;
        def.desc = desc;
        def.broadcastOnJoin = broadcast;
        titles.put(name, def);
        plugin.getConfig().set("titles." + name + ".color", color);
        plugin.getConfig().set("titles." + name + ".desc", desc);
        plugin.getConfig().set("titles." + name + ".broadcast-on-join", broadcast);
        plugin.saveConfig();
    }

    // ===== 玩家称号 =====
    public List<String> getPlayerTitles(UUID uuid) {
        return playerTitles.getOrDefault(uuid, new ArrayList<>());
    }

    public boolean ownsTitle(UUID uuid, String name) {
        return getPlayerTitles(uuid).contains(name);
    }

    public void giveTitle(UUID uuid, String name) {
        List<String> list = playerTitles.computeIfAbsent(uuid, k -> new ArrayList<>());
        if (!list.contains(name)) {
            list.add(name);
            // 若玩家当前没有已显示的称号 → 自动把刚给的设为显示（"给了就显示"，无需再手动勾选）
            List<String> disp = displayed.get(uuid);
            if (disp == null || disp.isEmpty()) {
                int max = plugin.getConfig().getInt("settings.max-display", 1);
                List<String> newDisp = new ArrayList<>();
                newDisp.add(name);
                // 只保留上限内
                while (newDisp.size() > max) newDisp.remove(newDisp.size() - 1);
                displayed.put(uuid, newDisp);
            }
            save();
        }
    }

    public boolean removeTitle(UUID uuid, String name) {
        List<String> list = playerTitles.get(uuid);
        boolean removed = list != null && list.remove(name);
        if (removed) {
            // 若正在显示则移除
            List<String> disp = displayed.get(uuid);
            if (disp != null) {
                disp.remove(name);
                if (disp.isEmpty()) displayed.remove(uuid);
            }
            save();
        }
        return removed;
    }

    // ===== 显示管理 =====
    public List<String> getDisplayed(UUID uuid) {
        return displayed.getOrDefault(uuid, new ArrayList<>());
    }

    public void setDisplayed(UUID uuid, List<String> names) {
        if (names.isEmpty()) displayed.remove(uuid);
        else displayed.put(uuid, names);
        save();
    }

    public void clearDisplayed(UUID uuid) {
        displayed.remove(uuid);
        save();
    }

    /** 构建 TAB/聊天前缀（称号 + 空格 + §r + 名字不上色） */
    public String buildPrefix(UUID uuid) {
        List<String> disp = getDisplayed(uuid);
        if (disp.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String name : disp) {
            TitleDef def = titles.get(name);
            if (def == null) continue;
            sb.append(def.color).append('[').append(name).append("] ");
        }
        sb.append("§r");   // 重置颜色，保证玩家名不上色
        return sb.toString();
    }
}
