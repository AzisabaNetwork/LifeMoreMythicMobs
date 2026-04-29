package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderDouble;
import io.lumine.xikage.mythicmobs.utils.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModifyAttributeMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private static final Map<String, AttributeModTask> activeMods = new ConcurrentHashMap<>();

    protected final PlaceholderDouble amount;
    protected final int duration;
    protected final String auraName;
    protected final String attributeName;
    protected final boolean fixed;

    public ModifyAttributeMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.amount = PlaceholderDouble.of(config.getString(new String[]{"amount", "a"}, "0.0"));
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.auraName = config.getString(new String[]{"auraName", "n"}, "attr_mod");
        this.fixed = config.getBoolean(new String[]{"fixed", "f"}, false);
        this.attributeName = config.getString(new String[]{"type", "t", "attribute"}, "GENERIC_ARMOR").toUpperCase();
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        double val = this.amount.get(data, target);

        Schedulers.sync().run(() -> {
            if (!(BukkitAdapter.adapt(target) instanceof LivingEntity)) return;
            LivingEntity entity = (LivingEntity) BukkitAdapter.adapt(target);

            Attribute targetAttr = parseAttribute(attributeName);
            if (targetAttr == null) return;

            AttributeInstance attrInstance = entity.getAttribute(targetAttr);
            if (attrInstance == null) return;

            String id = entity.getUniqueId() + ":" + targetAttr.name() + ":" + auraName;

            if (activeMods.containsKey(id)) {
                activeMods.get(id).stop();
            }

            new AttributeModTask(entity, targetAttr, id, val, duration);
        });
        return true;
    }

    private Attribute parseAttribute(String name) {
        try {
            if (!name.startsWith("GENERIC_")) {
                try { return Attribute.valueOf("GENERIC_" + name); } catch (Exception ignored) {}
            }
            return Attribute.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

    private class AttributeModTask implements Runnable {
        private final LivingEntity entity;
        private final Attribute attribute;
        private final String id;
        private final AttributeModifier modifier;
        private int ticksRemaining;
        private int taskId = -1;

        public AttributeModTask(LivingEntity entity, Attribute attribute, String id, double amount, int duration) {
            this.entity = entity;
            this.attribute = attribute;
            this.id = id;
            this.ticksRemaining = duration;

            AttributeInstance attrInstance = entity.getAttribute(attribute);
            if (attrInstance == null) {
                this.modifier = null;
                return;
            }

            double finalAmount = fixed ? (amount - attrInstance.getBaseValue()) : amount;

            this.modifier = new AttributeModifier(UUID.randomUUID(), auraName, finalAmount, AttributeModifier.Operation.ADD_NUMBER);

            for (AttributeModifier m : attrInstance.getModifiers()) {
                if (m.getName().equals(auraName)) {
                    attrInstance.removeModifier(m);
                }
            }

            attrInstance.addModifier(modifier);

            Plugin plugin = Bukkit.getPluginManager().getPlugin("LifeMoreMythicMobs");
            if (plugin == null) plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");

            if (plugin != null) {
                activeMods.put(id, this);
                this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1L, 1L);
            }
        }

        @Override
        public void run() {
            if (!entity.isValid() || entity.isDead() || ticksRemaining <= 0) {
                stop();
                return;
            }
            ticksRemaining--;
        }

        public void stop() {
            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            }
            AttributeInstance attr = entity.getAttribute(attribute);
            if (attr != null && modifier != null) {
                attr.removeModifier(modifier);
            }
            activeMods.remove(id);
        }
    }
}