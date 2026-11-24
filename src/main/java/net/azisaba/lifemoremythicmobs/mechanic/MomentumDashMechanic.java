package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * {@code momentumDash{ticks=2;multiplier=3;max=2}}: pushes the target the way it is already moving. It measures how
 * far the target moves in {@code ticks}, multiplies that by {@code multiplier}, scales the horizontal part down
 * (by 0.95 steps) until neither axis is over {@code max}, and keeps the vertical speed it had at the start.
 */
public class MomentumDashMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final int ticks;
    private final double multiplier;
    private final double max;

    public MomentumDashMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, null, config.getLine(), config);
        this.ticks = Math.max(1, config.getInteger(new String[]{"ticks", "t"}, 2));
        this.multiplier = config.getDouble(new String[]{"multiplier", "m"}, 3.0D);
        this.max = Math.max(0.01D, config.getDouble(new String[]{"max"}, 2.0D));
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        Entity entity = target.getBukkitEntity();
        Location start = entity.getLocation();
        double y = entity.getVelocity().getY();
        Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(), () -> {
            if (!entity.isValid() || !entity.getWorld().equals(start.getWorld())) return;
            Vector delta = entity.getLocation().toVector().subtract(start.toVector()).multiply(multiplier);
            entity.setVelocity(limit(delta, max).setY(y));
        }, ticks);
        return SkillResult.SUCCESS;
    }

    /** Shrinks x and z together by 0.95 until the larger one is within max (same steps as FFCore). */
    static Vector limit(Vector v, double max) {
        double big = Math.max(Math.abs(v.getX()), Math.abs(v.getZ()));
        double scale = 1.0D;
        while (big * scale > max) scale *= 0.95D;
        return new Vector(v.getX() * scale, v.getY(), v.getZ() * scale);
    }
}
