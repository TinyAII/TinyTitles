package nl.tinyaii.tinytitles.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import nl.tinyaii.tinytitles.TinyTitlesPlugin;
import nl.tinyaii.tinytitles.util.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Paper 1.19.3+ 新版聊天事件监听（AsyncChatEvent，Adventure 体系）。
 * 用 ChatRenderer 在消息前加称号前缀。旧版 Spigot 的 AsyncPlayerChatEvent 在 TitleListener 中。
 */
public class PaperChatListener implements Listener {

    private final TinyTitlesPlugin plugin;
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public PaperChatListener(TinyTitlesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        if (!plugin.getConfig().getBoolean("settings.show-in-chat", true)) return;
        Player p = e.getPlayer();
        String prefix = plugin.getTitleManager().buildPrefix(p.getUniqueId());
        if (prefix.isEmpty()) return;

        final Component prefixComp = LEGACY.deserialize(Messages.color(prefix));
        final Player source = p;
        e.renderer(ChatRenderer.viewerUnaware((src, sourceDisplayName, message) ->
                prefixComp.append(sourceDisplayName).append(Component.text(Messages.color("&7: &f"))).append(message)));
    }
}
