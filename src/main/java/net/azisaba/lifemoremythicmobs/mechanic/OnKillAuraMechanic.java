package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OnKillAuraMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private static final Map<String, KillAura> activeAuras = new ConcurrentHashMap<>();

    protected final String auraName;
    protected final String onKillSkill;
    protected final String onTickSkill;
    protected final String onEndSkill;
    protected final int duration;
    protected final int tickInterval;

    public OnKillAuraMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.auraName = config.getString(new String[]{"auraName", "aura", "n"}, "kill_aura");
        this.onKillSkill = config.getString(new String[]{"onKill", "ok"}, null);
        this.onTickSkill = config.getString(new String[]{"onTick", "ot"}, null);
        this.onEndSkill = config.getString(new String[]{"onEnd", "oe"}, null);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 200);
        this.tickInterval = config.getInteger(new String[]{"tickInterval", "ti"}, 1);
    }

    public static void remove(AbstractEntity target, String auraName) {
        String id = target.getUniqueId().toString() + ":" + auraName;
        if (activeAuras.containsKey(id)) {
            activeAuras.get(id).stop(false);
        }
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        String id = target.getUniqueId().toString() + ":" + this.auraName;

        if (activeAuras.containsKey(id)) {
            activeAuras.get(id).refresh(this.duration);
            return true;
        }

        new KillAura(target, data, id);
        return true;
    }

    private class KillAura implements Listener, Runnable {
        private final AbstractEntity target;
        private final SkillMetadata data;
        private final String id;
        private int ticksRemaining;
        private final int taskId;

        public KillAura(AbstractEntity target, SkillMetadata data, String id) {
            this.target = target;
            this.data = data;
            this.id = id;
            this.ticksRemaining = duration;

            Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
            activeAuras.put(id, this);
            Bukkit.getPluginManager().registerEvents(this, plugin);
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
        }

        public void refresh(int newDuration) {
            this.ticksRemaining = newDuration;
        }

        @Override
        public void run() {
            if (target.isDead()) {
                stop(false);
                return;
            }
            if (ticksRemaining <= 0) {
                stop(true);
                return;
            }

            if (onTickSkill != null && ticksRemaining % tickInterval == 0) {
                executeSkill(onTickSkill, target);
            }

            ticksRemaining--;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onKill(EntityDeathEvent event) {
            LivingEntity victim = event.getEntity();
            if (victim.getKiller() != null && victim.getKiller().getUniqueId().equals(target.getUniqueId())) {
                executeSkill(onKillSkill, BukkitAdapter.adapt(victim));
            }
        }

        private void stop(boolean timeOut) {
            Bukkit.getScheduler().cancelTask(taskId);
            HandlerList.unregisterAll(this);
            activeAuras.remove(id);
            if (timeOut) executeSkill(onEndSkill, target);
        }

        private void executeSkill(String skillName, AbstractEntity trigger) {
            if (skillName == null || skillName.isEmpty()) return;
            Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(skillName);
            maybeSkill.ifPresent(skill -> {
                SkillMetadata clone = data.deepClone();
                if (trigger != null) {
                    clone.setTrigger(trigger);
                } else {
                    clone.setTrigger(target);
                }
                skill.execute(clone);
            });
        }
    }
}