package nl.tinyaii.tinytitles.listener;

import nl.tinyaii.tinytitles.TinyTitlesPlugin;
import nl.tinyaii.tinytitles.data.TitleManager;
import nl.tinyaii.tinytitles.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

/**
 * 称号显示：
 *  - 进服：设置 TAB 显示名（称号+名字）+ 进服通报（拥有通报称号的玩家）
 *  - 聊天：消息前加称号前缀
 */
public class TitleListener implements Listener {

    private final TinyTitlesPlugin plugin;

    public TitleListener(TinyTitlesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        TitleManager tm = plugin.getTitleManager();
        // 若玩家有称号但从没勾选显示 → 自动显示第一个拥有的（"给了就显示"，兼容老数据）
        List<String> owned = tm.getPlayerTitles(p.getUniqueId());
        List<String> disp = tm.getDisplayed(p.getUniqueId());
        if (!owned.isEmpty() && disp.isEmpty()) {
            int max = plugin.getConfig().getInt("settings.max-display", 1);
            List<String> auto = new java.util.ArrayList<>();
            for (String name : owned) {
                if (auto.size() >= max) break;
                auto.add(name);
            }
            tm.setDisplayed(p.getUniqueId(), auto);
            disp = auto;
        }
        // TAB 显示称号
        if (plugin.getConfig().getBoolean("settings.show-in-tab", true)) {
            updateTab(p);
        }
        // 进服通报：拥有 broadcast-on-join 称号的玩家
        boolean announced = false;
        for (String name : disp) {
            TitleManager.TitleDef def = tm.getTitle(name);
            if (def != null && def.broadcastOnJoin) {
                String fmt = plugin.getConfig().getString("broadcast-format",
                        "&6[{title}] &e{player} &a驾临服务器！")
                        .replace("{player}", p.getName())
                        .replace("{title}", name);
                Bukkit.broadcastMessage(Messages.color(fmt));
                announced = true;
                break;
            }
        }
        if (!announced) {
            // 没显示称号但有通报称号的，也通报一次（按拥有列表）
            for (String name : tm.getPlayerTitles(p.getUniqueId())) {
                TitleManager.TitleDef def = tm.getTitle(name);
                if (def != null && def.broadcastOnJoin) {
                    String fmt = plugin.getConfig().getString("broadcast-format",
                            "&6[{title}] &e{player} &a驾临服务器！")
                            .replace("{player}", p.getName())
                            .replace("{title}", name);
                    Bukkit.broadcastMessage(Messages.color(fmt));
                    break;
                }
            }
        }
    }

    /** 更新 TAB 显示名 + 头顶名字：称号前缀 + 名字（名字不上色）。
     *  头顶名字用 scoreboard Team 前缀（setCustomName 对玩家无效，Team 前缀才会显示在头顶名字前）。 */
    public void updateTab(Player p) {
        String prefix = plugin.getTitleManager().buildPrefix(p.getUniqueId());
        String display = Messages.color(prefix + p.getName());
        // TAB 面板
        if (plugin.getConfig().getBoolean("settings.show-in-tab", true)) {
            p.setPlayerListName(display);
        } else {
            p.setPlayerListName(p.getName());
        }
        // 头顶名字（name tag）：用 scoreboard Team 前缀
        if (plugin.getConfig().getBoolean("settings.show-in-nametag", true)) {
            String prefixRaw = plugin.getTitleManager().buildPrefix(p.getUniqueId());
            plugin.getNametag().setPlayerPrefix(p, prefixRaw);
        } else {
            plugin.getNametag().clearPlayerPrefix(p);
        }
    }

    /** 聊天显示：消息前加称号前缀 */
    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!plugin.getConfig().getBoolean("settings.show-in-chat", true)) return;
        Player p = e.getPlayer();
        String prefix = plugin.getTitleManager().buildPrefix(p.getUniqueId());
        if (prefix.isEmpty()) return;
        // 格式：前缀 + 玩家名 + : + 消息（玩家名用 §r 保持默认色）
        String newFormat = Messages.color(prefix) + "%s" + Messages.color("&7: &f") + "%s";
        e.setFormat(newFormat);
    }
}
