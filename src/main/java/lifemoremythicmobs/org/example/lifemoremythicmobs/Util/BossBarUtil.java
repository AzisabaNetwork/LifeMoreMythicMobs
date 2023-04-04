package lifemoremythicmobs.org.example.lifemoremythicmobs.Util;

import lifemoremythicmobs.org.example.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarUtil {

    static BossBar bar;

    public static void bossBar(String title, String style, String color, Player p, double progress, long duration, boolean always) {

        bar = Bukkit.createBossBar(title, BarColor.valueOf(color), BarStyle.valueOf(style));
        LifeMoreMythicMobs MoreMM = LifeMoreMythicMobs.getPlugin(LifeMoreMythicMobs.class);

        bar.setVisible(true);
        bar.addPlayer(p);
        bar.setProgress(progress);

        if ( !always ) {
            Bukkit.getScheduler().runTaskLater(MoreMM, () -> bar.removePlayer(p), duration);
        }

    }

    public static void removeBossBar(Player p) {

        bar.removePlayer(p);

    }

}
