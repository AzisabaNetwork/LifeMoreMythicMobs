package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.Schedulers;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModifyPlayerAttributeMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private static final Map<String, AttributeModTask> activeMods = new ConcurrentHashMap<>();

    protected final PlaceholderString amountStr;
    protected final int duration;
    protected final String auraName;
    protected final String attributeName;
    protected final boolean fixed;
    protected final boolean capAtZero;

    protected final String onTickSkill;
    protected final String onEndSkill;
    protected final int tickInterval;

    public ModifyPlayerAttributeMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.amountStr = PlaceholderString.of(config.getString(new String[]{"amount", "a"}, "0.0"));
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.auraName = config.getString(new String[]{"auraName", "aura", "n"}, "attr_mod");
        this.fixed = config.getBoolean(new String[]{"fixed", "fix", "f"}, false);
        this.capAtZero = config.getBoolean(new String[]{"capAtZero", "cap", "c"}, true);
        this.attributeName = config.getString(new String[]{"type", "t", "attribute"}, "GENERIC_ARMOR").toUpperCase();
        this.onTickSkill = config.getString(new String[]{"onTick", "ot"}, null);
        this.onEndSkill = config.getString(new String[]{"onEnd", "oe"}, null);
        this.tickInterval = config.getInteger(new String[]{"tickInterval", "ti"}, 1);
    }

    public static void remove(AbstractEntity target, String auraName) {
        String uuidStr = target.getUniqueId().toString();
        activeMods.forEach((id, task) -> {
            if (id.startsWith(uuidStr) && id.endsWith(":" + auraName)) {
                task.stop();
            }
        });
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        String resolvedAmount = this.amountStr.get(data, target);

        Schedulers.sync().run(() -> {
            if (!(BukkitAdapter.adapt(target) instanceof LivingEntity)) return;
            LivingEntity entity = (LivingEntity) BukkitAdapter.adapt(target);

            Attribute targetAttr = parseAttribute(attributeName);
            if (targetAttr == null) return;

            AttributeInstance attrInstance = entity.getAttribute(targetAttr);
            if (attrInstance == null) return;

            String id = entity.getUniqueId() + ":" + targetAttr.getKey().getKey().toUpperCase() + ":" + auraName;

            if (activeMods.containsKey(id)) {
                activeMods.get(id).stop();
            }

            new AttributeModTask(entity, targetAttr, id, resolvedAmount, duration, data);
        });
        return SkillResult.SUCCESS;
    }

    private Attribute parseAttribute(String name) {
        String formatted = name.toLowerCase();
        if (!formatted.startsWith("generic_") && !formatted.contains(":")) {
            formatted = "generic_" + formatted;
        }
        return Registry.ATTRIBUTE.get(NamespacedKey.minecraft(formatted.replace("generic_generic_", "generic_")));
    }

    private class AttributeModTask implements Runnable {
        private final LivingEntity entity;
        private final Attribute attribute;
        private final String id;
        private final AttributeModifier modifier;
        private final SkillMetadata data;
        private int ticksRemaining;
        private int taskId = -1;

        public AttributeModTask(LivingEntity entity, Attribute attribute, String id, String amountInput, int duration, SkillMetadata data) {
            this.entity = entity;
            this.attribute = attribute;
            this.id = id;
            this.ticksRemaining = duration;
            this.data = data;

            AttributeInstance attrInstance = entity.getAttribute(attribute);
            if (attrInstance == null) {
                this.modifier = null;
                return;
            }

            double baseValue = attrInstance.getBaseValue();
            double currentValue = attrInstance.getValue();

            double parsedVal;
            try {
                parsedVal = Double.parseDouble(amountInput.replaceAll("[^0-9.\\-]", ""));
            } catch (Exception e) { parsedVal = 0; }

            double targetTotal;
            boolean isPercent = amountInput.endsWith("%");

            if (amountInput.startsWith("+")) {
                double add = isPercent ? (currentValue * (parsedVal / 100.0)) : parsedVal;
                targetTotal = currentValue + add;
            } else if (amountInput.startsWith("-")) {
                double sub = isPercent ? (currentValue * (Math.abs(parsedVal) / 100.0)) : Math.abs(parsedVal);
                targetTotal = currentValue - sub;
            } else if (isPercent) {
                targetTotal = baseValue * (parsedVal / 100.0);
            } else if (fixed) {
                targetTotal = parsedVal;
            } else {
                targetTotal = baseValue + parsedVal;
            }

            if (capAtZero && targetTotal < 0) targetTotal = 0;

            double finalModAmount = targetTotal - baseValue;

            NamespacedKey mKey = new NamespacedKey("lifemore", auraName.toLowerCase().replaceAll("[^a-z0-9/._-]", ""));
            this.modifier = new AttributeModifier(mKey, finalModAmount, AttributeModifier.Operation.ADD_NUMBER);

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
            if (onTickSkill != null && ticksRemaining % tickInterval == 0) {
                executeSkill(onTickSkill);
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
            if (ticksRemaining <= 0) {
                executeSkill(onEndSkill);
            }
            activeMods.remove(id);
        }

        private void executeSkill(String skillName) {
            if (skillName == null || skillName.isEmpty()) return;
            Optional<Skill> maybeSkill = MythicBukkit.inst().getSkillManager().getSkill(skillName);
            maybeSkill.ifPresent(skill -> {
                SkillMetadata clone = data.deepClone();
                clone.setTrigger(BukkitAdapter.adapt(entity));
                skill.execute(clone);
            });
        }
    }
}