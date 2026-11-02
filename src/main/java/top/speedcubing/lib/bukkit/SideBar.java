package top.speedcubing.lib.bukkit;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class SideBar implements Listener {

    public static SideBar getSidebar(Player player) {
        return CubingLibPlayer.get(player).getSideBar();
    }

    public static Collection<SideBar> getSidebars() {
        Set<SideBar> s = new HashSet<>();
        for (CubingLibPlayer p : CubingLibPlayer.user.values()) {
            if (p.getSideBar() != null) {
                s.add(p.getSideBar());
            }
        }
        return s;
    }

    public final Scoreboard scoreboard;
    public final Objective objective;
    public final Player player;
    int changer = -1;
    int line = 1;
    Map<Integer, String> lines = new HashMap<>();
    Map<Integer, Set<String>> scores = new HashMap<>();

    public SideBar(Player player, String displayName) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("sidebar", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(displayName);
        this.player = player;
        CubingLibPlayer.get(player).setSideBar(this);
    }

    public void create() {
        player.setScoreboard(scoreboard);
    }

    private SideBar setPrefixSuffix(String str, String team) {
        Team t = scoreboard.getTeam(team);
        String[] result = split(str, team.charAt(3));
        if (!result[0].equals(t.getPrefix()))
            t.setPrefix(result[0]);
        if (!result[1].equals(t.getSuffix()))
            t.setSuffix(result[1]);
        return this;
    }

    public SideBar staticLine(String str, int score) {
        Preconditions.checkArgument(!str.matches("^\\w{1,16}$"));
        return add(str, score, true);
    }

    SideBar add(String str, int score, boolean isStaticLine) {
        objective.getScore(str).setScore(score);
        if (!isStaticLine) {
            lines.put(line, str);
            Set<String> s = scores.get(score);
            if (s != null)
                s.add(str);
            else {
                Set<String> set = new HashSet<>(Collections.singletonList(str));
                scores.put(score, set);
            }
        }
        line += 1;
        return this;
    }

    public SideBar changeableLine(String str, int score) {
        changer += 1;
        String team = "§" + "!0123456789abcdef".charAt(changer) + "§f";
        String[] result = split(str, team.charAt(3));
        Team t = scoreboard.registerNewTeam(team);
        t.addEntry(team);
        t.setPrefix(result[0]);
        t.setSuffix(result[1]);
        return add(team, score, false);
    }

    public SideBar setLine(String str, int line) {
        return setPrefixSuffix(str, lines.get(line));
    }

    public SideBar setLineByScore(String str, int score) {
        for (String s : scores.get(score)) {
            setPrefixSuffix(str, s);
        }
        return this;
    }

    private static String[] split(String str, char c) {
        String prefix;
        String suffix = "";
        if (str.length() <= 16)
            prefix = str;
        else {
            boolean split = false;
            if (str.charAt(15) == '§') {
                prefix = str.substring(0, 15);
                if (str.charAt(16) < 'k')
                    suffix = str.substring(15);
            } else {
                split = true;
                prefix = str.substring(0, 16);
            }
            for (int j = split ? 14 : 13; j >= 0; j--) {
                char next = str.charAt(j + 1);
                if (str.charAt(j) == '§' && next < 'k') {
                    suffix = "§" + next + str.substring(split ? 16 : 15);
                    break;
                }
            }
            suffix = suffix.isEmpty() ? str.substring(split ? 16 : 15) : suffix;
        }
        if (suffix.length() > 1) {
            if (suffix.charAt(0) == '§' && suffix.charAt(1) == c)
                suffix = suffix.substring(2);
        }
        return new String[]{prefix, suffix};
    }
}
