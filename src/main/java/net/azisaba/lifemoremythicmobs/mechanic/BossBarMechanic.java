package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class BossBarMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final boolean always;
    protected final int duration;
    protected final double progress;
    protected final String title;
    protected final String style;
    protected final String color;
    protected final String id;

    public static HashMap<String, BossBar> bars = new HashMap<>();

    public BossBarMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.always = config.getBoolean(new String[]{"always", "a", "al"}, false);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.progress = config.getDouble(new String[]{"progress", "pro", "p"}, 1);
        this.title = config.getString(new String[]{"title", "t"}, "BossBar");
        this.style = config.getString(new String[]{"style", "s"}, "SOLID").toUpperCase();
        this.color = config.getString(new String[]{"color", "c"}, "RED").toUpperCase();
        this.id = config.getString(new String[]{"id", "i"}, "def");

    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        Player bukkitTarget = (Player) BukkitAdapter.adapt(target);

        bossBar(title, style, color, bukkitTarget, progress, duration, always, id);

        return true;
    }

    public void bossBar(String title, String style, String color, Player p, double progress, long duration, boolean always, String id) {

        LifeMoreMythicMobs MoreMM = LifeMoreMythicMobs.getPlugin(LifeMoreMythicMobs.class);

        if ( bars.containsKey(id) ) {
            bars.get(id).removeAll();
        }

        BossBar bar = Bukkit.createBossBar(title, BarColor.valueOf(color), BarStyle.valueOf(style));

        bars.put(id, bar);

        bar.setVisible(true);
        bar.addPlayer(p);
        bar.setProgress(progress);

        if ( !always ) {
            Bukkit.getScheduler().runTaskLater(MoreMM, () -> bar.removePlayer(p), duration);
        }

    }

}
