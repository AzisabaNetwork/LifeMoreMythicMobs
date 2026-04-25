package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NullRecoveryMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private static final Map<String, NullRecoveryAura> activeAuras = new ConcurrentHashMap<>();

    protected final String auraName;
    protected final String onStart;
    protected final String onHeal;
    protected final String onEnd;
    protected final int duration;
    protected final String amount;

    public NullRecoveryMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.auraName = config.getString(new String[]{"auraName", "n", "名前"}, "default");
        this.onStart = config.getString(new String[]{"onStart", "os"}, null);
        this.onHeal = config.getString(new String[]{"onHeal", "oh"}, null);
        this.onEnd = config.getString(new String[]{"onEnd", "oe"}, null);
        this.duration = config.getInteger(new String[]{"duration", "d", "持続時間"}, 100);
        this.amount = config.getString(new String[]{"amount", "a", "量"}, "100%");
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        String identifier = abstractEntity.getUniqueId().toString() + ":" + this.auraName;

        if (activeAuras.containsKey(identifier)) {
            activeAuras.get(identifier).refresh(this.duration);
            return true;
        }

        new NullRecoveryAura(abstractEntity, skillMetadata, identifier);
        return true;
    }

    private class NullRecoveryAura implements Listener, Runnable {
        private final AbstractEntity target;
        private final SkillMetadata data;
        private final String identifier;
        private int ticksRemaining;
        private int taskId = -1;
        private double lastHealth;

        public NullRecoveryAura(AbstractEntity target, SkillMetadata data, String identifier) {
            this.target = target;
            this.data = data;
            this.identifier = identifier;
            this.ticksRemaining = duration;
            this.lastHealth = target.getHealth();

            Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
            if (plugin != null) {
                activeAuras.put(identifier, this);
                Bukkit.getPluginManager().registerEvents(this, plugin);
                this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
                executeSkill(onStart);
            }
        }

        public void refresh(int newDuration) {
            this.ticksRemaining = newDuration;
        }

        @Override
        public void run() {
            if (target.isDead() || ticksRemaining <= 0) {
                stop();
                return;
            }

            double currentHealth = target.getHealth();
            if (currentHealth > lastHealth) {
                double diff = currentHealth - lastHealth;
                double reduce;
                try {
                    if (amount.endsWith("%")) {
                        reduce = diff * (Double.parseDouble(amount.replace("%", "")) / 100.0);
                    } else {
                        reduce = Double.parseDouble(amount);
                    }
                } catch (Exception e) { reduce = 0; }

                double finalHealth = Math.max(lastHealth, currentHealth - reduce);
                target.setHealth(finalHealth);
                if (reduce > 0) executeSkill(onHeal);
                this.lastHealth = finalHealth;
            } else {
                this.lastHealth = currentHealth;
            }
            ticksRemaining--;
        }

        public void stop() {
            if (this.taskId != -1) Bukkit.getScheduler().cancelTask(this.taskId);
            HandlerList.unregisterAll(this);
            activeAuras.remove(identifier);
            executeSkill(onEnd);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onRegain(EntityRegainHealthEvent event) {
            if (event.getEntity().getUniqueId().equals(target.getUniqueId())) {
                this.lastHealth = target.getHealth();
            }
        }

        private void executeSkill(String skillName) {
            if (skillName == null || skillName.isEmpty()) return;
            Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(skillName);
            maybeSkill.ifPresent(skill -> {
                SkillMetadata clone = data.deepClone();
                clone.setTrigger(target);
                skill.execute(clone);
            });
        }
    }
}