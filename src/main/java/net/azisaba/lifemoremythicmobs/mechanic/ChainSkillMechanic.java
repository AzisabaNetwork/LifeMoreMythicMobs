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
import net.azisaba.lifemoremythicmobs.util.ManagedEntityRegistry;
import net.azisaba.lifemoremythicmobs.util.SkillUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * ターゲット間を次々と連鎖（チェイン）してスキルとパーティクル線を実行するメカニクス。
 * <pre>
 * lmChainSkill{bounces=5;r=7;delay=3;decay=0.85;target=MOBS;p=ELECTRIC_SPARK;onHit=ChainZap} @target
 * </pre>
 */
public class ChainSkillMechanic extends SkillMechanic implements ITargetedEntitySkill {
    public enum TargetFilter {
        ALL, PLAYERS, MOBS
    }

    private final int bounces;
    private final double radius;
    private final int delayTicks;
    private final double decay;
    private final boolean allowRepeat;
    private final TargetFilter targetFilter;
    private final Particle particle;
    private final double particleStep;
    private final String indexVar;
    private final String scaleVar;
    private final String onHitSkill;

    public ChainSkillMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.bounces = Math.max(1, Math.min(32, config.getInteger(new String[]{"bounces", "b", "max"}, 4)));
        this.radius = Math.max(0.5, config.getDouble(new String[]{"radius", "r"}, 6.0));
        this.delayTicks = Math.max(0, config.getInteger(new String[]{"delay", "d"}, 2));
        this.decay = config.getDouble(new String[]{"decay", "dc"}, 1.0);
        this.allowRepeat = config.getBoolean(new String[]{"allowrepeat", "rep"}, false);
        TargetFilter filter = TargetFilter.ALL;
        try {
            filter = TargetFilter.valueOf(config.getString(new String[]{"target", "tf"}, "ALL").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        this.targetFilter = filter;
        String pName = config.getString(new String[]{"particle", "p"}, "ELECTRIC_SPARK");
        Particle parsed = null;
        if (!pName.equalsIgnoreCase("none")) {
            try {
                parsed = Particle.valueOf(pName.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        this.particle = parsed;
        this.particleStep = Math.max(0.15, config.getDouble(new String[]{"particlestep", "ps"}, 0.35));
        this.indexVar = config.getString(new String[]{"indexvar", "iv"}, "lmmm_chain_index");
        this.scaleVar = config.getString(new String[]{"scalevar", "sv"}, "lmmm_chain_scale");
        this.onHitSkill = config.getString(new String[]{"onhit", "oh"}, null);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        Entity first = BukkitAdapter.adapt(target);
        if (!(first instanceof LivingEntity living)) return SkillResult.INVALID_TARGET;

        Runnable start = () -> {
            Set<UUID> visited = new HashSet<>();
            Location startLoc = data.getCaster() != null && data.getCaster().getEntity() != null
                    ? BukkitAdapter.adapt(data.getCaster().getEntity()).getLocation().add(0, 1.0, 0)
                    : living.getLocation().add(0, 1.0, 0);
            stepChain(data.deepClone(), startLoc, living, 1, visited);
        };
        if (Bukkit.isPrimaryThread()) start.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), start);
        return SkillResult.SUCCESS;
    }

    private void stepChain(SkillMetadata data, Location prevLoc, LivingEntity current, int step, Set<UUID> visited) {
        if (!current.isValid() || current.isDead()) return;
        visited.add(current.getUniqueId());

        Location curLoc = current.getLocation().add(0, current.getHeight() * 0.5, 0);
        drawLine(prevLoc, curLoc);

        double scale = Math.pow(decay, step - 1);
        SkillMetadata stepData = data.deepClone();
        if (stepData.getVariables() != null) {
            stepData.getVariables().putInt(indexVar, step);
            stepData.getVariables().putDouble(scaleVar, scale);
        }
        stepData.setOrigin(BukkitAdapter.adapt(curLoc));
        SkillUtil.executeSkill(onHitSkill, stepData, BukkitAdapter.adapt(current));

        if (step >= bounces) return;

        LivingEntity next = findNext(current, visited, data.getCaster() != null && data.getCaster().getEntity() != null
                ? data.getCaster().getEntity().getUniqueId() : null);
        if (next == null) return;

        if (delayTicks <= 0) {
            stepChain(data, curLoc, next, step + 1, visited);
        } else {
            Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(),
                    () -> stepChain(data, curLoc, next, step + 1, visited), delayTicks);
        }
    }

    private LivingEntity findNext(LivingEntity current, Set<UUID> visited, UUID casterId) {
        LivingEntity best = null;
        double bestDistSq = Double.MAX_VALUE;
        Location origin = current.getLocation();

        for (Entity e : current.getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity candidate) || e.isDead()) continue;
            UUID id = e.getUniqueId();
            if (id.equals(current.getUniqueId()) || id.equals(casterId)) continue;
            if (ManagedEntityRegistry.isManaged(e)) continue;
            if (!allowRepeat && visited.contains(id)) continue;
            if (targetFilter == TargetFilter.PLAYERS && !(e instanceof Player)) continue;
            if (targetFilter == TargetFilter.MOBS && (e instanceof Player)) continue;

            double dSq = candidate.getLocation().distanceSquared(origin);
            if (dSq < bestDistSq && dSq <= radius * radius) {
                bestDistSq = dSq;
                best = candidate;
            }
        }
        return best;
    }

    private void drawLine(Location a, Location b) {
        if (particle == null || a.getWorld() == null || !a.getWorld().equals(b.getWorld())) return;
        World w = a.getWorld();
        Vector va = a.toVector();
        Vector dir = b.toVector().subtract(va);
        double len = dir.length();
        if (len < 1.0e-4) return;
        dir.normalize();
        for (double d = 0; d <= len; d += particleStep) {
            Vector p = va.clone().add(dir.clone().multiply(d));
            w.spawnParticle(particle, p.getX(), p.getY(), p.getZ(), 1, 0, 0, 0, 0);
        }
    }
}
