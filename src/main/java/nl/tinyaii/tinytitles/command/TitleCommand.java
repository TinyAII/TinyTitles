package nl.tinyaii.tinytitles.command;

import nl.tinyaii.tinytitles.TinyTitlesPlugin;
import nl.tinyaii.tinytitles.data.TitleManager;
import nl.tinyaii.tinytitles.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 称号命令（双语）：
 *  /称号                     → 查看自己称号/打开 GUI
 *  /称号 使用 <称号1> [称号2] [称号3]  → 设置显示（最多 max-display 个）
 *  /称号 列表                 → 列出拥有的称号
 *  /称号 清除                 → 清除显示称号
 *  /称号 给予 <玩家> <称号>     → (管理) 发放
 *  /称号 收回 <玩家> <称号>     → (管理) 收回
 *  /称号 创建 <名字> <颜色> [描述] → (管理) 动态创建称号
 *  /称号 重载                 → (管理) 重载配置
 */
public class TitleCommand implements CommandExecutor {

    private final TinyTitlesPlugin plugin;

    public TitleCommand(TinyTitlesPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isAdmin(CommandSender s) {
        if (s.hasPermission("titles.admin") || s.isOp()) return true;
        s.sendMessage(Messages.color(plugin.getConfig().getString("messages.no-permission", "&c你没有权限这么做。")));
        return false;
    }

    private boolean isPlayer(CommandSender s) {
        if (s instanceof Player) return true;
        s.sendMessage("仅玩家可用。");
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!isPlayer(sender)) return true;
            return gui((Player) sender);
        }
        switch (args[0]) {
            case "使用": case "use":
                if (!isPlayer(sender)) return true;
                return use((Player) sender, args);
            case "列表": case "list":
                if (!isPlayer(sender)) return true;
                return list((Player) sender);
            case "清除": case "clear":
                if (!isPlayer(sender)) return true;
                plugin.getTitleManager().clearDisplayed(((Player) sender).getUniqueId());
                plugin.getTitleListener().updateTab((Player) sender);
                sender.sendMessage(Messages.color(plugin.getConfig().getString("messages.cleared", "&a已清除显示称号。")));
                return true;
            case "给予": case "give":
                if (!isAdmin(sender)) return true;
                if (args.length < 3) { sender.sendMessage(Messages.color("&c用法: /称号 给予 <玩家> <称号>")); return true; }
                return give(sender, args[1], args[2]);
            case "收回": case "remove":
                if (!isAdmin(sender)) return true;
                if (args.length < 3) { sender.sendMessage(Messages.color("&c用法: /称号 收回 <玩家> <称号>")); return true; }
                return remove(sender, args[1], args[2]);
            case "创建": case "create":
                if (!isAdmin(sender)) return true;
                if (args.length < 3) { sender.sendMessage(Messages.color("&c用法: /称号 创建 <名字> <颜色(如&e)> [描述]")); return true; }
                return create(sender, args);
            case "重载": case "reload":
                if (!isAdmin(sender)) return true;
                plugin.reloadConfig();
                plugin.getTitleManager().load();
                sender.sendMessage(Messages.color(plugin.getConfig().getString("messages.reloaded", "&a配置已重载。")));
                return true;
            default:
                help(sender);
                return true;
        }
    }

    private void help(CommandSender s) {
        s.sendMessage(Messages.color("&e===== 称号帮助 ====="));
        s.sendMessage(Messages.color("&7/称号 &e- &7查看/选择称号（GUI）"));
        s.sendMessage(Messages.color("&7/称号 使用 <称号1> [称号2] [称号3] &e- &7设置显示"));
        s.sendMessage(Messages.color("&7/称号 列表 &e- &7拥有的称号"));
        s.sendMessage(Messages.color("&7/称号 给予|收回|创建 <玩家/名字> ... &e- &7(管理)"));
        s.sendMessage(Messages.color("&7/称号 重载 &e- &7(管理)重载配置"));
    }

    private boolean gui(Player p) {
        new nl.tinyaii.tinytitles.gui.TitleGui(plugin, p.getUniqueId()).open(p);
        return true;
    }

    private boolean use(Player p, String[] args) {
        if (args.length < 2) { p.sendMessage(Messages.color("&c用法: /称号 使用 <称号1> [称号2] [称号3]")); return true; }
        TitleManager tm = plugin.getTitleManager();
        int max = plugin.getConfig().getInt("settings.max-display", 1);
        List<String> toDisplay = new ArrayList<>();
        for (int i = 1; i < args.length && i <= max; i++) {
            String name = args[i];
            if (!tm.hasTitleDef(name)) { p.sendMessage(Messages.color("&c称号 &e" + name + " &c不存在。")); return true; }
            if (!tm.ownsTitle(p.getUniqueId(), name)) { p.sendMessage(Messages.color("&c你没有称号 &e" + name + "&c。")); return true; }
            toDisplay.add(name);
        }
        tm.setDisplayed(p.getUniqueId(), toDisplay);
        plugin.getTitleListener().updateTab(p);
        p.sendMessage(Messages.color(plugin.getConfig().getString("messages.select-ok", "&a已设置显示称号。")));
        return true;
    }

    private boolean list(Player p) {
        List<String> titles = plugin.getTitleManager().getPlayerTitles(p.getUniqueId());
        if (titles.isEmpty()) { p.sendMessage(Messages.color(plugin.getConfig().getString("messages.no-titles", "&c你还没有任何称号。"))); return true; }
        p.sendMessage(Messages.color("&e===== 我的称号 ====="));
        for (String t : titles) {
            TitleManager.TitleDef def = plugin.getTitleManager().getTitle(t);
            String color = def != null ? def.color : "&f";
            p.sendMessage(Messages.color(color + t + " &7" + (def != null && def.desc != null ? def.desc : "")));
        }
        return true;
    }

    private boolean give(CommandSender sender, String targetName, String titleName) {
        if (!plugin.getTitleManager().hasTitleDef(titleName)) {
            sender.sendMessage(Messages.color("&c称号 &e" + titleName + " &c不存在。用 /称号 创建 定义。"));
            return true;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
        plugin.getTitleManager().giveTitle(op.getUniqueId(), titleName);
        sender.sendMessage(Messages.color(plugin.getConfig().getString("messages.give-ok", "&a已给予 &e{player} &a称号 &e{title}")
                .replace("{player}", targetName).replace("{title}", titleName)));
        Player online = op.getPlayer();
        if (online != null) {
            online.sendMessage(Messages.color("&a你获得了称号 &e" + titleName + "&a！用 /称号 使用 " + titleName + " 显示"));
        }
        return true;
    }

    private boolean remove(CommandSender sender, String targetName, String titleName) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
        if (!plugin.getTitleManager().removeTitle(op.getUniqueId(), titleName)) {
            sender.sendMessage(Messages.color("&c该玩家没有此称号。"));
            return true;
        }
        sender.sendMessage(Messages.color(plugin.getConfig().getString("messages.remove-ok", "&a已收回 &e{player} &a的称号 &e{title}")
                .replace("{player}", targetName).replace("{title}", titleName)));
        Player online = op.getPlayer();
        if (online != null) {
            plugin.getTitleListener().updateTab(online);
            online.sendMessage(Messages.color("&c你的称号 &e" + titleName + " &c已被收回。"));
        }
        return true;
    }

    private boolean create(CommandSender sender, String[] args) {
        String name = args[1];
        String color = args[2];
        StringBuilder desc = new StringBuilder();
        for (int i = 3; i < args.length; i++) { if (i > 3) desc.append(" "); desc.append(args[i]); }
        plugin.getTitleManager().createTitle(name, color, desc.toString(), false);
        sender.sendMessage(Messages.color(plugin.getConfig().getString("messages.create-ok", "&a已创建称号 &e{title} &a（颜色 &e{color}&a）")
                .replace("{title}", name).replace("{color}", color)));
        return true;
    }
}
