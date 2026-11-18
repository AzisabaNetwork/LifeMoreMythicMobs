package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.GeometryUtil;
import net.azisaba.lifemoremythicmobs.util.ManagedEntityRegistry;
import net.azisaba.lifemoremythicmobs.util.SkillUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * キャスター(またはOrigin)とターゲットを結ぶ追従ビーム/鎖ギミック。
 * <pre>
 * lmTether{b=red_stained_glass;w=0.18;d=100;maxDist=14;onBreak=ChainBreak;onCross=LaserHit;glow=#FF3333} @target
 * </pre>
 */
public class TetherBeamMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {
    private final BlockData blockData;
    private final float width;
    private final int duration;
    private final int interval;
    private final double yOffset;
    private final boolean fromOrigin;
    private final double maxDist;
    private final double minDist;
    private final boolean breakOnNear;
    private final double crossRadius;
    private final boolean crossPlayersOnly;
    private final int crossCooldown;
    private final Color glowColor;
    private final Particle particle;
    private final double particleStep;

    private final String onTickSkill;
    private final String onBreakSkill;
    private final String onNearSkill;
    private final String onCrossSkill;
    private final String onEndSkill;

    public TetherBeamMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        String blockName = config.getString(new String[]{"block", "b"}, "sea_lantern");
        if (blockName.equalsIgnoreCase("none")) {
            this.blockData = null;
        } else {
            Material m = Material.matchMaterial(blockName);
            this.blockData = (m == null || !m.isBlock() ? Material.SEA_LANTERN : m).createBlockData();
        }
        this.width = (float) Math.max(0.02, config.getDouble(new String[]{"width", "w"}, 0.16));
        this.duration = Math.max(1, config.getInteger(new String[]{"duration", "d"}, 100));
        this.interval = Math.max(1, config.getInteger(new String[]{"interval", "i"}, 2));
        this.yOffset = config.getDouble(new String[]{"yoffset", "yo"}, 1.0);
        this.fromOrigin = config.getBoolean(new String[]{"fromorigin", "fo"}, false);
        this.maxDist = config.getDouble(new String[]{"maxdist", "maxd"}, -1);
        this.minDist = config.getDouble(new String[]{"mindist", "mind"}, -1);
        this.breakOnNear = config.getBoolean(new String[]{"breakonnear", "bon"}, true);
        this.crossRadius = config.getDouble(new String[]{"crossradius", "cr"}, 0.8);
        this.crossPlayersOnly = config.getBoolean(new String[]{"crossplayersonly", "cpo"}, true);
        this.crossCooldown = Math.max(1, config.getInteger(new String[]{"crosscooldown", "cc"}, 10));
        this.glowColor = parseColor(config.getString(new String[]{"glow", "gl"}, ""));
        String pName = config.getString(new String[]{"particle", "p"}, "none");
        Particle parsedParticle = null;
        if (!pName.equalsIgnoreCase("none")) {
            try {
                parsedParticle = Particle.valueOf(pName.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        this.particle = parsedParticle;
        this.particleStep = Math.max(0.15, config.getDouble(new String[]{"particlestep", "ps"}, 0.4));

        this.onTickSkill = config.getString(new String[]{"ontick", "ot"}, null);
        this.onBreakSkill = config.getString(new String[]{"onbreak", "ob"}, null);
        this.onNearSkill = config.getString(new String[]{"onnear", "on"}, null);
        this.onCrossSkill = config.getString(new String[]{"oncross", "oc"}, null);
        this.onEndSkill = config.getString(new String[]{"onend", "oe"}, null);
    }

    private static Color parseColor(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            String hex = raw.replace("#", "").replace("\"", "").trim();
            if (hex.length() == 6) {
                return Color.fromRGB(Integer.parseInt(hex, 16));
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        Runnable start = () -> new TetherTask(data, target, null).start();
        if (Bukkit.isPrimaryThread()) start.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), start);
        return SkillResult.SUCCESS;
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        Location fixedEnd = BukkitAdapter.adapt(target).clone().add(0, yOffset, 0);
        Runnable start = () -> new TetherTask(data, null, fixedEnd).start();
        if (Bukkit.isPrimaryThread()) start.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), start);
        return SkillResult.SUCCESS;
    }

    /**
     * Computes shortest distance between point P and line segment AB.
     */
    public static double pointToSegmentDistance(Vector p, Vector a, Vector b) {
        return GeometryUtil.pointToSegmentDistance(
                p.getX(), p.getY(), p.getZ(),
                a.getX(), a.getY(), a.getZ(),
                b.getX(), b.getY(), b.getZ()
        );
    }

    private final class TetherTask extends BukkitRunnable {
        private final SkillMetadata data;
        private final AbstractEntity targetEntity;
        private final Location fixedStart;
        private final Location fixedEnd;
        private final Map<UUID, Integer> crossCooldowns = new HashMap<>();
        private BlockDisplay display;
        private int elapsed = 0;

        TetherTask(SkillMetadata data, AbstractEntity targetEntity, Location fixedEnd) {
            this.data = data.deepClone();
            this.targetEntity = targetEntity;
            this.fixedStart = fromOrigin && data.getOrigin() != null
                    ? BukkitAdapter.adapt(data.getOrigin()).clone().add(0, yOffset, 0)
                    : null;
            this.fixedEnd = fixedEnd;
        }

        void start() {
            Location start = resolveStart();
            Location end = resolveEnd();
            if (start == null || end == null || !start.getWorld().equals(end.getWorld())) {
                return;
            }
            if (blockData != null) {
                display = start.getWorld().spawn(start, BlockDisplay.class, d -> {
                    d.setBlock(blockData);
                    d.setBrightness(new Display.Brightness(15, 15));
                    d.setInterpolationDuration(interval);
                    d.setViewRange(64f);
                    if (glowColor != null) {
                        d.setGlowing(true);
                        d.setGlowColorOverride(glowColor);
                    }
                    ManagedEntityRegistry.track(d);
                });
                updateDisplay(start, end);
            }
            this.runTaskTimer(LifeMoreMythicMobs.inst(), 0L, interval);
        }

        private Location resolveStart() {
            if (fixedStart != null) return fixedStart;
            if (data.getCaster() == null || data.getCaster().getEntity() == null) return null;
            Entity e = BukkitAdapter.adapt(data.getCaster().getEntity());
            if (e == null || !e.isValid() || e.isDead()) return null;
            return e.getLocation().clone().add(0, yOffset, 0);
        }

        private Location resolveEnd() {
            if (fixedEnd != null) return fixedEnd;
            if (targetEntity == null) return null;
            Entity e = BukkitAdapter.adapt(targetEntity);
            if (e == null || !e.isValid() || e.isDead()) return null;
            return e.getLocation().clone().add(0, yOffset, 0);
        }

        private void updateDisplay(Location start, Location end) {
            if (display == null || !display.isValid()) return;
            display.teleport(start);
            Vector delta = end.toVector().subtract(start.toVector());
            float dist = (float) delta.length();
            if (dist < 0.01f) dist = 0.01f;
            Vector3f dir = new Vector3f((float) delta.getX(), (float) delta.getY(), (float) delta.getZ()).normalize();
            Quaternionf rot = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), dir);
            Vector3f translation = new Vector3f(-width / 2f, -width / 2f, 0f);
            rot.transform(translation);
            display.setInterpolationDelay(0);
            display.setTransformation(new Transformation(
                    translation,
                    rot,
                    new Vector3f(width, width, dist),
                    new Quaternionf()
            ));
        }

        private void spawnParticles(Location start, Location end) {
            if (particle == null) return;
            World w = start.getWorld();
            Vector a = start.toVector();
            Vector b = end.toVector();
            Vector dir = b.clone().subtract(a);
            double len = dir.length();
            if (len < 1.0e-5) return;
            dir.normalize();
            for (double d = 0; d <= len; d += particleStep) {
                Vector p = a.clone().add(dir.clone().multiply(d));
                w.spawnParticle(particle, p.getX(), p.getY(), p.getZ(), 1, 0, 0, 0, 0);
            }
        }

        @Override
        public void run() {
            Location start = resolveStart();
            Location end = resolveEnd();
            if (start == null || end == null || !start.getWorld().equals(end.getWorld())) {
                cleanup();
                return;
            }
            if (elapsed >= duration) {
                SkillUtil.executeSkill(onEndSkill, data, targetEntity);
                cleanup();
                return;
            }

            double dist = start.distance(end);
            if (maxDist > 0 && dist > maxDist) {
                SkillUtil.executeSkill(onBreakSkill, data, targetEntity);
                cleanup();
                return;
            }
            if (minDist > 0 && dist < minDist) {
                SkillUtil.executeSkill(onNearSkill, data, targetEntity);
                if (breakOnNear) {
                    cleanup();
                    return;
                }
            }

            updateDisplay(start, end);
            spawnParticles(start, end);
            SkillUtil.executeSkill(onTickSkill, data, targetEntity);

            if (onCrossSkill != null && !onCrossSkill.isEmpty()) {
                checkCrossings(start, end);
            }

            elapsed += interval;
        }

        private void checkCrossings(Location start, Location end) {
            crossCooldowns.entrySet().removeIf(entry -> {
                int next = entry.getValue() - interval;
                if (next <= 0) return true;
                entry.setValue(next);
                return false;
            });

            Location mid = start.clone().add(end).multiply(0.5);
            double searchRadius = start.distance(end) * 0.5 + crossRadius + 1.5;
            Vector a = start.toVector();
            Vector b = end.toVector();
            UUID casterId = data.getCaster() != null && data.getCaster().getEntity() != null
                    ? data.getCaster().getEntity().getUniqueId() : null;
            UUID targetId = targetEntity != null ? targetEntity.getUniqueId() : null;

            for (Entity e : start.getWorld().getNearbyEntities(mid, searchRadius, searchRadius, searchRadius)) {
                if (!(e instanceof LivingEntity living) || e.isDead()) continue;
                if (crossPlayersOnly && !(e instanceof Player)) continue;
                UUID id = e.getUniqueId();
                if (id.equals(casterId) || id.equals(targetId)) continue;
                if (ManagedEntityRegistry.isManaged(e)) continue;
                if (crossCooldowns.containsKey(id)) continue;

                Vector center = living.getLocation().toVector().add(new Vector(0, living.getHeight() * 0.5, 0));
                if (pointToSegmentDistance(center, a, b) <= crossRadius) {
                    crossCooldowns.put(id, crossCooldown);
                    SkillUtil.executeSkill(onCrossSkill, data, BukkitAdapter.adapt(e));
                }
            }
        }

        private void cleanup() {
            ManagedEntityRegistry.removeAndUntrack(display);
            this.cancel();
        }
    }
}
