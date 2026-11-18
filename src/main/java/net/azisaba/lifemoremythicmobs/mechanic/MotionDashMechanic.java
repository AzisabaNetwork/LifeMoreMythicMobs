package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * 精密なダッシュ・引き寄せ・吹き飛ばし移動を行うメカニクス。
 * <pre>
 * lmDash{mode=FORWARD;power=1.4;y=0.25;ignoreY=true;noFall=40} @self
 * lmDash{mode=TOWARD;power=1.1;y=0.2} @PIR{r=12}
 * </pre>
 */
public class MotionDashMechanic extends SkillMechanic implements ITargetedEntitySkill {
    public enum Mode {
        FORWARD, TOWARD, AWAY, UP
    }

    private final Mode mode;
    private final double power;
    private final double yBoost;
    private final boolean ignoreY;
    private final boolean addVelocity;
    private final int noFallTicks;

    public MotionDashMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        Mode parsed = Mode.FORWARD;
        try {
            parsed = Mode.valueOf(config.getString(new String[]{"mode", "m"}, "FORWARD").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        this.mode = parsed;
        this.power = config.getDouble(new String[]{"power", "p"}, 1.2);
        this.yBoost = config.getDouble(new String[]{"yboost", "y"}, 0.25);
        this.ignoreY = config.getBoolean(new String[]{"ignorey", "iy"}, true);
        this.addVelocity = config.getBoolean(new String[]{"add", "a"}, false);
        this.noFallTicks = Math.max(0, config.getInteger(new String[]{"nofall", "nf"}, 0));
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        Entity entity = BukkitAdapter.adapt(target);
        if (entity == null || !entity.isValid()) return SkillResult.INVALID_TARGET;

        Runnable r = () -> applyMotion(data, entity);
        if (Bukkit.isPrimaryThread()) r.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), r);
        return SkillResult.SUCCESS;
    }

    private void applyMotion(SkillMetadata data, Entity target) {
        Vector dir;
        Location origin = data.getOrigin() != null ? BukkitAdapter.adapt(data.getOrigin())
                : (data.getCaster() != null && data.getCaster().getEntity() != null
                ? BukkitAdapter.adapt(data.getCaster().getEntity()).getLocation()
                : target.getLocation());

        switch (mode) {
            case FORWARD -> dir = target.getLocation().getDirection();
            case TOWARD -> dir = origin.toVector().subtract(target.getLocation().toVector());
            case AWAY -> dir = target.getLocation().toVector().subtract(origin.toVector());
            case UP -> dir = new Vector(0, 1, 0);
            default -> dir = new Vector(0, 0, 0);
        }

        if (mode != Mode.UP && ignoreY) {
            dir.setY(0);
        }
        if (dir.lengthSquared() < 1.0e-6) {
            dir = new Vector(0, 0, 0);
        } else {
            dir.normalize().multiply(power);
        }
        if (mode != Mode.UP) {
            dir.setY(dir.getY() + yBoost);
        }

        if (addVelocity) {
            target.setVelocity(target.getVelocity().add(dir));
        } else {
            target.setVelocity(dir);
        }

        if (noFallTicks > 0) {
            new BukkitRunnable() {
                int rem = noFallTicks;

                @Override
                public void run() {
                    if (rem <= 0 || !target.isValid() || target.isDead()) {
                        this.cancel();
                        return;
                    }
                    target.setFallDistance(0f);
                    rem -= 2;
                }
            }.runTaskTimer(LifeMoreMythicMobs.inst(), 0L, 2L);
        }
    }
}
