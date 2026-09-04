package nl.tinyaii.tinytitles.gui;

import nl.tinyaii.tinytitles.TinyTitlesPlugin;
import nl.tinyaii.tinytitles.data.TitleManager;
import nl.tinyaii.tinytitles.util.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 称号选择 GUI 点击：勾选/取消显示称号（最多 max-display 个）。
 */
public class TitleGuiListener implements Listener {

    private final TinyTitlesPlugin plugin;

    public TitleGuiListener(TinyTitlesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof TitleHolder)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        TitleHolder holder = (TitleHolder) e.getInventory().getHolder();
        if (!holder.owner.equals(p.getUniqueId())) {
            p.sendMessage(Messages.color("&c只能操作自己的称号。"));
            return;
        }
        ItemStack it = e.getCurrentItem();
        if (it == null || !it.hasItemMeta() || !it.getItemMeta().hasDisplayName()) return;
        // 去除颜色和勾选标记，取称号名
        String raw = net.md_5.bungee.api.ChatColor.stripColor(it.getItemMeta().getDisplayName());
        String name = raw.replace("✔ ", "").trim();

        TitleManager tm = plugin.getTitleManager();
        if (!tm.hasTitleDef(name) || !tm.ownsTitle(p.getUniqueId(), name)) return;
        int max = plugin.getConfig().getInt("settings.max-display", 1);

        List<String> displayed = new ArrayList<>(tm.getDisplayed(p.getUniqueId()));
        if (displayed.contains(name)) {
            displayed.remove(name);
        } else {
            if (displayed.size() >= max) {
                p.sendMessage(Messages.color(plugin.getConfig().getString("messages.select-max", "&c最多只能显示 {max} 个称号。")
                        .replace("{max}", String.valueOf(max))));
                return;
            }
            displayed.add(name);
        }
        tm.setDisplayed(p.getUniqueId(), displayed);
        plugin.getTitleListener().updateTab(p);
        new TitleGui(plugin, holder.owner).open(p);
    }
}
