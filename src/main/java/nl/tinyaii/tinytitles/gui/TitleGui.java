package nl.tinyaii.tinytitles.gui;

import nl.tinyaii.tinytitles.TinyTitlesPlugin;
import nl.tinyaii.tinytitles.data.TitleManager;
import nl.tinyaii.tinytitles.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 称号选择 GUI：显示所有已定义称号。
 * 已解锁排前，未解锁排后（灰显）。鼠标悬停显示 已解锁/未解锁 + 描述。
 */
public class TitleGui {

    private final TinyTitlesPlugin plugin;
    private final UUID owner;

    public TitleGui(TinyTitlesPlugin plugin, UUID owner) {
        this.plugin = plugin;
        this.owner = owner;
    }

    public void open(Player viewer) {
        TitleManager tm = plugin.getTitleManager();
        int max = plugin.getConfig().getInt("settings.max-display", 1);
        List<String> owned = tm.getPlayerTitles(owner);
        List<String> displayed = tm.getDisplayed(owner);

        // 排序：已解锁在前，未解锁在后
        List<Map.Entry<String, TitleManager.TitleDef>> all = new ArrayList<>(tm.getTitles().entrySet());
        all.sort(Comparator.comparing(e -> !owned.contains(e.getKey())));   // 已解锁=true 排前

        int size = Math.max(9, ((Math.max(all.size(), 1) + 8) / 9) * 9);
        size = Math.min(54, Math.max(9, size));
        Inventory inv = Bukkit.createInventory(new TitleHolder(owner), size, ChatColor.DARK_GRAY + "称号选择");

        for (int i = 0; i < size; i++) inv.setItem(i, glass());

        inv.setItem(4, titleItem(max));

        int slot = 9;
        for (Map.Entry<String, TitleManager.TitleDef> en : all) {
            if (slot >= size) break;
            String name = en.getKey();
            TitleManager.TitleDef def = en.getValue();
            boolean unlocked = owned.contains(name);
            boolean selected = displayed.contains(name);
            inv.setItem(slot++, titleItem(name, def, unlocked, selected));
        }
        if (all.isEmpty()) {
            inv.setItem(22, emptyItem());
        }

        viewer.openInventory(inv);
    }

    private ItemStack titleItem(int max) {
        ItemStack it = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Messages.color("&e我的称号"));
            meta.setLore(List.of(Messages.color("&7已解锁排前、未解锁灰显，最多显示 &e" + max + " &7个")));
            it.setItemMeta(meta);
        }
        return it;
    }

    private ItemStack titleItem(String name, TitleManager.TitleDef def, boolean unlocked, boolean selected) {
        String color = def != null ? def.color : "&f";
        Material mat;
        String status;
        if (!unlocked) {
            mat = Material.BARRIER;
            status = "&c✘ 未解锁";
        } else if (selected) {
            mat = Material.LIME_DYE;
            status = "&a✔ 已解锁 · 显示中";
        } else {
            mat = Material.GRAY_DYE;
            status = "&a✔ 已解锁";
        }
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Messages.color(color + name));
            List<String> lore = new ArrayList<>();
            if (def != null && def.desc != null && !def.desc.isEmpty()) lore.add(Messages.color("&7" + def.desc));
            lore.add(Messages.color(status));
            if (unlocked && def != null && def.broadcastOnJoin) lore.add(Messages.color("&6⚡ 进服通报称号"));
            lore.add(Messages.color(unlocked ? "&7点击" + (selected ? "取消显示" : "显示") : "&7等待管理员发放"));
            meta.setLore(lore);
            it.setItemMeta(meta);
        }
        return it;
    }

    private ItemStack emptyItem() {
        ItemStack it = new ItemStack(Material.BARRIER);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Messages.color("&c暂无称号"));
            meta.setLore(List.of(Messages.color("&7管理员可用 /称号 创建 定义")));
            it.setItemMeta(meta);
        }
        return it;
    }

    private ItemStack glass() {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); it.setItemMeta(meta); }
        return it;
    }
}
