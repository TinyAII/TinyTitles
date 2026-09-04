package nl.tinyaii.tinytitles;

import nl.tinyaii.tinytitles.data.TitleManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TinyTitlesPlugin extends JavaPlugin {

    private TitleManager titleManager;
    private nl.tinyaii.tinytitles.listener.TitleListener titleListener;
    private nl.tinyaii.tinytitles.listener.NametagManager nametagManager;

    @Override
    public void onEnable() {
        // TinyAII 品牌横幅 —— 必须在所有初始化逻辑之前输出（与 AutoBackup 完全一致）
        getLogger().info(" _____ _                _    ___ ___");
        getLogger().info("|_   _(_)_ __  _   _   / \\  |_ _|_ _|");
        getLogger().info("  | | | | '_ \\| | | | / _ \\  | | | |");
        getLogger().info("  | | | | | | | |_| |/ ___ \\ | | | |");
        getLogger().info("  |_| |_|_| |_|\\__, /_/   \\_\\___|___|");
        getLogger().info("               |___/");
        getLogger().info("TinyTitles 称号系统 v" + getDescription().getVersion() + " - TinyAII 出品");

        saveDefaultConfig();
        titleManager = new TitleManager(this);
        titleManager.load();
        nametagManager = new nl.tinyaii.tinytitles.listener.NametagManager();
        titleListener = new nl.tinyaii.tinytitles.listener.TitleListener(this);

        getServer().getPluginManager().registerEvents(titleListener, this);
        getServer().getPluginManager().registerEvents(new nl.tinyaii.tinytitles.gui.TitleGuiListener(this), this);
        getCommand("称号").setExecutor(new nl.tinyaii.tinytitles.command.TitleCommand(this));

        // 聊天显示跨版本：Paper 1.19.3+ 走 AsyncChatEvent（反射注册），Spigot 旧版走 AsyncPlayerChatEvent（已在上方）
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            Class<?> hook = Class.forName("nl.tinyaii.tinytitles.listener.PaperChatListener");
            java.lang.reflect.Constructor<?> ctor = hook.getConstructor(TinyTitlesPlugin.class);
            org.bukkit.event.Listener listener = (org.bukkit.event.Listener) ctor.newInstance(this);
            getServer().getPluginManager().registerEvents(listener, this);
            getLogger().info("[聊天] 使用 Paper 新版聊天事件 (1.19.3+)");
        } catch (Throwable t) {
            getLogger().info("[聊天] 使用旧版聊天事件 (Spigot AsyncPlayerChatEvent)");
        }

        getLogger().info("称号系统已启用。定义称号 " + titleManager.getTitles().size()
                + " 个，最多显示 " + getConfig().getInt("settings.max-display", 1) + " 个");
    }

    @Override
    public void onDisable() {
        if (titleManager != null) titleManager.save();
    }

    public TitleManager getTitleManager() { return titleManager; }
    public nl.tinyaii.tinytitles.listener.TitleListener getTitleListener() { return titleListener; }
    public nl.tinyaii.tinytitles.listener.NametagManager getNametag() { return nametagManager; }
}
