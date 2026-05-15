package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PersistentZoneMechanic extends SkillMechanic implements ITargetedLocationSkill, ITargetedEntitySkill {
    private final String onEnterSkill;
    private final String onStaySkill;
    private final String onLeaveSkill;
    private final double radius;
    private final int duration;
    private final int interval;

    public PersistentZoneMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.onEnterSkill = config.getString(new String[]{"onEnter", "oe"});
        this.onStaySkill = config.getString(new String[]{"onStay", "os"});
        this.onLeaveSkill = config.getString(new String[]{"onLeave", "ol"});
        this.radius = config.getDouble(new String[]{"radius", "r"}, 5.0);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.interval = config.getInteger(new String[]{"interval", "i"}, 20);
    }

    @Override
    public boolean castAtLocation(SkillMetadata data, AbstractLocation target) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LifeMoreMythicMobs");
        if (plugin instanceof LifeMoreMythicMobs) {
            new ZoneTask((LifeMoreMythicMobs) plugin, data, target);
            return true;
        }
        return false;
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        return castAtLocation(data, target.getLocation());
    }

    private class ZoneTask extends BukkitRunnable {
        private final SkillMetadata data;
        private final AbstractLocation location;
        private final Set<UUID> currentEntities = new HashSet<>();
        private int elapsed = 0;

        public ZoneTask(LifeMoreMythicMobs plugin, SkillMetadata data, AbstractLocation location) {
            this.data = data;
            this.location = location;
            this.runTaskTimer(plugin, 0L, (long) interval);
        }

        @Override
        public void run() {
            if (elapsed >= duration) {
                for (UUID uuid : currentEntities) {
                    executeSkill(onLeaveSkill, uuid);
                }
                this.cancel();
                return;
            }

            Location bukkitLoc = BukkitAdapter.adapt(location);
            Set<UUID> nextEntities = new HashSet<>();

            for (Entity e : bukkitLoc.getWorld().getNearbyEntities(bukkitLoc, radius, radius, radius)) {
                nextEntities.add(e.getUniqueId());
            }

            for (UUID uuid : currentEntities) {
                if (!nextEntities.contains(uuid)) {
                    executeSkill(onLeaveSkill, uuid);
                }
            }

            for (UUID uuid : nextEntities) {
                if (!currentEntities.contains(uuid)) {
                    executeSkill(onEnterSkill, uuid);
                }
                executeSkill(onStaySkill, uuid);
            }

            currentEntities.clear();
            currentEntities.addAll(nextEntities);
            elapsed += interval;
        }

        private void executeSkill(String skillName, UUID targetUUID) {
            if (skillName == null) return;
            Entity bukkitEntity = Bukkit.getEntity(targetUUID);
            if (bukkitEntity == null) return;

            Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(skillName);
            maybeSkill.ifPresent(skill -> {
                SkillMetadata newData = data.deepClone();
                newData.setTrigger(BukkitAdapter.adapt(bukkitEntity));
                skill.execute(newData);
            });
        }
    }
}