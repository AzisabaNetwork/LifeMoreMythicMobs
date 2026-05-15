package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.UUID;

public class BouncingRaytraceMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {
    private final String onHitSkill;
    private final String onLineSkill;
    private final double damage;
    private final double maxRange;
    private final int maxBounces;
    private final double step;

    public BouncingRaytraceMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.onHitSkill = config.getString(new String[]{"onHit", "oh", "oH"}, null);
        this.onLineSkill = config.getString(new String[]{"onLine", "ol", "oL"}, null);
        this.damage = config.getDouble(new String[]{"damage", "a"}, 5.0);
        this.maxRange = config.getDouble(new String[]{"maxRange", "mr"}, 30.0);
        this.maxBounces = config.getInteger(new String[]{"maxBounces", "mb"}, 3);
        this.step = config.getDouble(new String[]{"step", "s"}, 0.2);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        return castAtLocation(data, target.getLocation());
    }

    @Override
    public boolean castAtLocation(SkillMetadata data, AbstractLocation target) {
        Location currentPos = BukkitAdapter.adapt(data.getCaster().getLocation()).add(0, 1.5, 0);
        Vector direction = currentPos.getDirection().normalize();
        double remainingDist = maxRange;
        UUID casterUUID = data.getCaster().getEntity().getUniqueId();

        for (int b = 0; b <= maxBounces; b++) {
            final int bounceCount = b;
            RayTraceResult result = currentPos.getWorld().rayTrace(
                    currentPos.clone().add(direction.clone().multiply(0.1)),
                    direction,
                    remainingDist,
                    org.bukkit.FluidCollisionMode.NEVER,
                    true,
                    0.5,
                    (entity) -> {
                        // 最初の発射時(bounceCount == 0)のみ、自分自身を無視する
                        // これにより自爆を防ぎつつ、反射して戻ってきたら自分に当たるようになる
                        if (bounceCount == 0 && entity.getUniqueId().equals(casterUUID)) {
                            return false;
                        }
                        return entity instanceof LivingEntity;
                    }
            );

            if (result == null) {
                executeLine(data, currentPos, direction, remainingDist);
                break;
            }

            Location hitLoc = result.getHitPosition().toLocation(currentPos.getWorld());
            double distToHit = currentPos.distance(hitLoc);

            executeLine(data, currentPos, direction, distToHit);

            remainingDist -= distToHit;
            currentPos = hitLoc;

            if (result.getHitEntity() != null) {
                applyHit(data, result.getHitEntity());
                break;
            }

            if (result.getHitBlock() != null && result.getHitBlockFace() != null) {
                direction = calculateReflection(direction, result.getHitBlockFace());
                currentPos.add(direction.clone().multiply(0.05));
            }

            if (remainingDist <= 0) break;
        }
        return true;
    }

    private void executeLine(SkillMetadata data, Location start, Vector dir, double dist) {
        if (onLineSkill == null || onLineSkill.isEmpty()) return;

        Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(onLineSkill);
        maybeSkill.ifPresent(skill -> {
            // クローンはループの外で1回だけ行う（負荷軽減）
            SkillMetadata newData = data.deepClone();
            for (double d = 0; d < dist; d += step) {
                Location loc = start.clone().add(dir.clone().multiply(d));
                newData.setLocationTarget(BukkitAdapter.adapt(loc));
                skill.execute(newData);
            }
        });
    }

    private Vector calculateReflection(Vector v, BlockFace face) {
        Vector n = new Vector(face.getModX(), face.getModY(), face.getModZ());
        return v.clone().subtract(n.multiply(2 * v.dot(n))).normalize();
    }

    private void applyHit(SkillMetadata data, Entity entity) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity target = (LivingEntity) entity;

        target.damage(damage, BukkitAdapter.adapt(data.getCaster().getEntity()));

        if (onHitSkill != null && !onHitSkill.isEmpty()) {
            Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(onHitSkill);
            maybeSkill.ifPresent(skill -> {
                SkillMetadata newData = data.deepClone();
                newData.setTrigger(BukkitAdapter.adapt(target));
                skill.execute(newData);
            });
        }
    }
}