package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * プレイヤーごとのボスバー（2026-11-15: 腐敗ゲージなど、その人だけの値をボスバーで見せる）。
 * <pre>
 * pBossBar{id=rot;t="&amp;4腐敗 &amp;c&lt;target.var.cl8_rot|0&gt;&amp;8/100";v=&lt;target.var.cl8_rot|0&gt;;max=100;c=RED;s=SEGMENTED_10;d=30} @PIR{r=48}
 * pBossBar{id=rot;remove=true} @PIR{r=48}
 * </pre>
 * id ごとに 1 本。d tick 呼ばれなければ消える。値は v / max（0〜1 に収める）。
 */
public class PlayerBossBarMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private static final Map<String, BossBar> BARS = new HashMap<>();
    private static final Map<String, BukkitTask> EXPIRE = new HashMap<>();
    private final String id;
    private final PlaceholderString title;
    private final PlaceholderDouble value;
    private final PlaceholderDouble max;
    private final BarColor color;
    private final BarStyle style;
    private final int duration;
    private final boolean remove;

    public PlayerBossBarMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.id = config.getString(new String[] {"id"}, "bar");
        this.title = PlaceholderString.of(config.getString(new String[] {"title", "t"}, ""));
        this.value = PlaceholderDouble.of(config.getString(new String[] {"value", "v"}, "0"));
        this.max = PlaceholderDouble.of(config.getString(new String[] {"max", "m"}, "100"));
        this.color = BarColor.valueOf(config.getString(new String[] {"color", "c"}, "RED").toUpperCase());
        this.style = BarStyle.valueOf(config.getString(new String[] {"style", "s"}, "SOLID").toUpperCase());
        this.duration = config.getInteger(new String[] {"duration", "d"}, 30);
        this.remove = config.getBoolean(new String[] {"remove", "r"}, false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null || !(target.getBukkitEntity() instanceof Player player)) return SkillResult.INVALID_TARGET;
        String t = ChatColor.translateAlternateColorCodes('&', title.get(data, target));
        double mx = max.get(data, target);
        double p = mx <= 0 ? 0 : Math.max(0, Math.min(1, value.get(data, target) / mx));
        Runnable r = () -> apply(player, t, p);
        if (Bukkit.isPrimaryThread()) r.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), r);
        return SkillResult.SUCCESS;
    }

    private void apply(Player player, String t, double p) {
        String k = id + ":" + player.getUniqueId();
        if (remove) {
            drop(k, player.getUniqueId());
            return;
        }
        BossBar bar = BARS.computeIfAbsent(k, x -> Bukkit.createBossBar(t, color, style));
        bar.setTitle(t);
        bar.setProgress(p);
        if (!bar.getPlayers().contains(player)) bar.addPlayer(player);
        BukkitTask old = EXPIRE.remove(k);
        if (old != null) old.cancel();
        UUID u = player.getUniqueId();
        EXPIRE.put(k, Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(), () -> drop(k, u), duration));
    }

    private static void drop(String k, UUID u) {
        BukkitTask t = EXPIRE.remove(k);
        if (t != null) t.cancel();
        BossBar bar = BARS.remove(k);
        if (bar != null) bar.removeAll();
    }
}
