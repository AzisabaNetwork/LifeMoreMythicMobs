package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import net.azisaba.lifemoremythicmobs.util.AuraSkillHelper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnDeathAuraMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private static final Map<String, DeathAura> activeAuras = new ConcurrentHashMap<>();

    protected final String auraName;
    protected final String onStartSkill;
    protected final String onDeathSkill;
    protected final String onTickSkill;
    protected final String onEndSkill;
    protected final int duration;
    protected final int tickInterval;

    public OnDeathAuraMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.auraName = config.getString(new String[]{"auraName", "aura", "n"}, "death_aura");
        this.onStartSkill = config.getString(new String[]{"onStart", "oS"}, null);
        this.onDeathSkill = config.getString(new String[]{"onDeath", "od"}, null);
        this.onTickSkill = config.getString(new String[]{"onTick", "oT"}, null);
        this.onEndSkill = config.getString(new String[]{"onEnd", "oE"}, null);
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

        new DeathAura(target, data, id);
        return true;
    }

    private class DeathAura implements Listener, Runnable {
        private final AbstractEntity target;
        private final SkillMetadata data;
        private final String id;
        private int ticksRemaining;
        private final int taskId;

        public DeathAura(AbstractEntity target, SkillMetadata data, String id) {
            this.target = target;
            this.data = data;
            this.id = id;
            this.ticksRemaining = duration;

            Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
            activeAuras.put(id, this);
            Bukkit.getPluginManager().registerEvents(this, plugin);
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
            AuraSkillHelper.executeSkill(onStartSkill, data, target);
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
                executeSkill(onTickSkill);
            }

            ticksRemaining--;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent event) {
            if (event.getEntity().getUniqueId().equals(target.getUniqueId())) {
                executeSkill(onDeathSkill);
                stop(false);
            }
        }

        private void stop(boolean timeOut) {
            Bukkit.getScheduler().cancelTask(taskId);
            HandlerList.unregisterAll(this);
            activeAuras.remove(id);
            if (timeOut) AuraSkillHelper.executeSkill(onEndSkill, data, target);
        }

        private void executeSkill(String skillName) {
            AuraSkillHelper.executeSkill(skillName, data, target);
        }
    }
}