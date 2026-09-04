package nl.tinyaii.tinytitles.listener;

import nl.tinyaii.tinytitles.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 头顶名字前缀：用 scoreboard Team 的 prefix 实现。
 * （Bukkit 的 setCustomName 对玩家实体无效，Team prefix 才是给玩家头顶名字加前缀的正解。）
 */
public class NametagManager {

    private final Map<UUID, String> teams = new HashMap<>();

    /** 给玩家设头顶前缀（Team prefix，队友自己可见） */
    public void setPlayerPrefix(Player p, String prefix) {
        if (prefix == null) prefix = "";
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("tt_" + p.getName().toLowerCase());
        if (team == null) {
            team = board.registerNewTeam("tt_" + p.getName().toLowerCase());
        }
        team.setPrefix(Messages.color(prefix));
        team.addEntry(p.getName());
        teams.put(p.getUniqueId(), prefix);
    }

    /** 清除玩家头顶前缀 */
    public void clearPlayerPrefix(Player p) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("tt_" + p.getName().toLowerCase());
        if (team != null) team.unregister();
        teams.remove(p.getUniqueId());
    }
}
