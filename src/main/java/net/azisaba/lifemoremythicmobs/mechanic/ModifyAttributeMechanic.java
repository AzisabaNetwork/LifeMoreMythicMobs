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
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModifyAttributeMechanic extends SkillMechanic implements ITargetedEntitySkill, Listener {

    private static final Map<String, AttributeModTask> activeMods = new ConcurrentHashMap<>();
    private static boolean listenerRegistered = false;
    private static Listener registeredListener;

    protected final PlaceholderString amountStr;
    protected final int duration;
    protected final String auraName;
    protected final String attributeName;
    protected final boolean fixed;
    protected final boolean capAtZero;

    protected final String onTickSkill;
    protected final String onEndSkill;
    protected final int tickInterval;

    public ModifyAttributeMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.amountStr = PlaceholderString.of(config.getString(new String[]{"amount", "a"}, "0.0"));
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.auraName = config.getString(new String[]{"auraName", "aura", "n"}, "attr_mod");
        this.fixed = config.getBoolean(new String[]{"fixed", "fix", "f"}, false);
        this.capAtZero = config.getBoolean(new String[]{"capAtZero", "cap", "c"}, true);
        this.attributeName = config.getString(new String[]{"type", "t", "attribute"}, "GENERIC_ARMOR").toUpperCase();
        this.onTickSkill = config.getString(new String[]{"onTick", "ot"}, null);
        this.onEndSkill = config.getString(new String[]{"onEnd", "oe"}, null);
        this.tickInterval = Math.max(1, config.getInteger(new String[]{"tickInterval", "ti"}, 1));

        registerListener();
    }

    private void registerListener() {
        if (!listenerRegistered) {
            Plugin plugin = getOwningPlugin();
            if (plugin != null) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                listenerRegistered = true;
                registeredListener = this;
            }
        }
    }

    private static Plugin getOwningPlugin() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LifeMoreMythicMobs");
        if (plugin == null) plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        return plugin;
    }

    private static void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }

        Plugin plugin = getOwningPlugin();
        if (plugin != null && plugin.isEnabled()) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void shutdown() {
        runSync(() -> {
            List<AttributeModTask> tasks = new ArrayList<>(activeMods.values());
            for (AttributeModTask task : tasks) {
                task.stop(false);
            }
            activeMods.clear();

            if (registeredListener != null) {
                HandlerList.unregisterAll(registeredListener);
                registeredListener = null;
                listenerRegistered = false;
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String uuidStr = event.getPlayer().getUniqueId().toString();
        new ArrayList<>(activeMods.entrySet()).forEach(entry -> {
            String id = entry.getKey();
            AttributeModTask task = entry.getValue();
            if (id.startsWith(uuidStr)) {
                task.stop(false);
            }
        });
    }

    public static void remove(AbstractEntity target, String auraName) {
        String uuidStr = target.getUniqueId().toString();
        runSync(() -> {
            new ArrayList<>(activeMods.entrySet()).forEach(entry -> {
                String id = entry.getKey();
                AttributeModTask task = entry.getValue();
                if (id.startsWith(uuidStr) && id.endsWith(":" + auraName)) {
                    task.stop(false);
                }
            });
        });
    }

    private static void recalculateEntityAttributes(LivingEntity entity, Attribute attribute) {
        String prefix = entity.getUniqueId().toString() + ":" + attribute.getKey().getKey().toUpperCase() + ":";
        new ArrayList<>(activeMods.entrySet()).forEach(entry -> {
            String id = entry.getKey();
            AttributeModTask task = entry.getValue();
            if (id.startsWith(prefix)) {
                try {
                    task.updateModifierValue();
                } catch (Exception e) {
                    task.stop(false);
                }
            }
        });
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        String resolvedAmount = this.amountStr.get(data, target);

        Schedulers.sync().run(() -> {
            try {
                if (!(BukkitAdapter.adapt(target) instanceof LivingEntity)) return;
                LivingEntity entity = (LivingEntity) BukkitAdapter.adapt(target);

                Attribute targetAttr = parseAttribute(attributeName);
                if (targetAttr == null) return;

                AttributeInstance attrInstance = entity.getAttribute(targetAttr);
                if (attrInstance == null) return;

                String id = entity.getUniqueId() + ":" + targetAttr.getKey().getKey().toUpperCase() + ":" + auraName;

                if (activeMods.containsKey(id)) {
                    activeMods.get(id).refresh(resolvedAmount, duration, data);
                    return;
                }

                new AttributeModTask(entity, targetAttr, id, resolvedAmount, duration, data);
            } catch (Exception e) {
                MythicBukkit.inst().getLogger().severe("ModifyAttributeMechanic でエラーが発生しました: " + e.getMessage());
                e.printStackTrace();
            }
        });
        return SkillResult.SUCCESS;
    }

    private Attribute parseAttribute(String name) {
        String formatted = name.toLowerCase();
        if (!formatted.startsWith("generic_") && !formatted.contains(":")) {
            formatted = "generic_" + formatted;
        }
        formatted = formatted.replace("generic_generic_", "generic_");
        return Registry.ATTRIBUTE.get(NamespacedKey.minecraft(formatted));
    }

    private class AttributeModTask implements Runnable {
        private final LivingEntity entity;
        private final Attribute attribute;
        private final String id;
        private String amountInput;
        private SkillMetadata data;
        private NamespacedKey modKey;
        private AttributeModifier currentModifier;
        private int ticksRemaining;
        private int taskId = -1;
        private boolean stopped = false;

        public AttributeModTask(LivingEntity entity, Attribute attribute, String id, String amountInput, int duration, SkillMetadata data) {
            this.entity = entity;
            this.attribute = attribute;
            this.id = id;
            this.amountInput = normalizeAmount(amountInput);
            this.ticksRemaining = duration;
            this.data = data.deepClone();

            Plugin plugin = getOwningPlugin();

            if (plugin != null) {
                activeMods.put(id, this);
                try {
                    updateModifierValue();
                    this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1L, 1L);
                } catch (Exception e) {
                    activeMods.remove(id);
                    throw new RuntimeException(e);
                }
            }
        }

        public void refresh(String amountInput, int duration, SkillMetadata data) {
            this.amountInput = normalizeAmount(amountInput);
            this.ticksRemaining = duration;
            this.data = data.deepClone();
            updateModifierValue();
        }

        private String normalizeAmount(String amountInput) {
            if (amountInput == null) return "0";
            return amountInput.trim();
        }

        private boolean isCurrentModifier(AttributeModifier modifier) {
            return currentModifier != null && currentModifier.getKey().equals(modifier.getKey());
        }

        private boolean isPluginModifier(AttributeModifier modifier) {
            for (AttributeModTask task : activeMods.values()) {
                if (task.currentModifier != null && task.currentModifier.getKey().equals(modifier.getKey())) {
                    return true;
                }
            }
            return false;
        }

        public void updateModifierValue() {
            AttributeInstance attrInstance = entity.getAttribute(attribute);
            if (attrInstance == null) return;

            // 一時的に古い自分のModifierの値を記録して、あとでパース計算に使用する
            double prevModAmount = (currentModifier != null) ? currentModifier.getAmount() : 0.0;

            AttributeModifier oldModifier = currentModifier;
            if (oldModifier != null) {
                attrInstance.removeModifier(currentModifier);
                currentModifier = null;
            }

            double baseValue = attrInstance.getBaseValue();
            double currentValueWithoutThisModifier = attrInstance.getValue();

            double finalModAmount = calculateFinalModAmount(attrInstance, baseValue, currentValueWithoutThisModifier, prevModAmount, oldModifier != null);

            this.modKey = new NamespacedKey("lifemore", auraName.toLowerCase().replaceAll("[^a-z0-9/._-]", "_") + "_" + UUID.randomUUID().toString().substring(0, 8));
            this.currentModifier = new AttributeModifier(modKey, finalModAmount, AttributeModifier.Operation.ADD_NUMBER);
            attrInstance.addModifier(currentModifier);
        }

        private double calculateFinalModAmount(AttributeInstance attrInstance, double baseValue, double currentValueWithoutThisModifier, double prevModAmount, boolean hadModifier) {
            double parsedVal;
            try {
                parsedVal = Double.parseDouble(amountInput.replaceAll("[^0-9.\\-]", ""));
            } catch (NumberFormatException e) { parsedVal = 0; }

            double targetTotal;
            boolean isPercent = amountInput.endsWith("%");

            if (amountInput.startsWith("+")) {
                double add = isPercent ? (currentValueWithoutThisModifier * (parsedVal / 100.0)) : parsedVal;
                targetTotal = currentValueWithoutThisModifier + add;
            } else if (amountInput.startsWith("-")) {
                double sub = isPercent ? (currentValueWithoutThisModifier * (Math.abs(parsedVal) / 100.0)) : Math.abs(parsedVal);
                targetTotal = currentValueWithoutThisModifier - sub;
            } else if (isPercent) {
                targetTotal = baseValue * (parsedVal / 100.0);
            } else if (fixed) {
                targetTotal = parsedVal;
            } else {
                targetTotal = baseValue + parsedVal;
            }

            if (capAtZero && targetTotal < 0) targetTotal = 0;

            if (amountInput.startsWith("+") || amountInput.startsWith("-")) {
                return targetTotal - currentValueWithoutThisModifier;
            }

            if (fixed) {
                // 現在の実際の最終合計値（自分のModifierが付いたままの状態の値）
                // ただし、updateModifierValue 内で直前に取り除かれているため、
                // 取り除いた後の値（attrInstance.getValue()）に、直前までの自分の値を擬似的に足して「現在のズレ前の値」とする
                double currentActualTotal = currentValueWithoutThisModifier + prevModAmount;

                // もし現在の最終値が目標値とズレている場合、その差分を今のModifier値にフィードバックする
                double deviation = targetTotal - currentActualTotal;

                // 新しいModifier値 = 前回のModifier値 + ズレの修正値
                double result = prevModAmount + deviation;

                // 外部要因でModifierが完全に消し飛んだ時などのセーフティ
                if (!hadModifier) {
                    double otherPluginModsSum = 0;
                    for (AttributeModifier m : attrInstance.getModifiers()) {
                        if (isCurrentModifier(m) || isPluginModifier(m)) continue;
                        if (m.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                            otherPluginModsSum += m.getAmount();
                        }
                    }
                    result = targetTotal - (baseValue + otherPluginModsSum);
                }

                if (capAtZero && (currentValueWithoutThisModifier + result) < 0) {
                    return -currentValueWithoutThisModifier;
                }
                return result;
            } else {
                return targetTotal - baseValue;
            }
        }

        @Override
        public void run() {
            if (!entity.isValid() || entity.isDead() || ticksRemaining <= 0) {
                stop(ticksRemaining <= 0);
                return;
            }

            if (fixed) {
                try {
                    updateModifierValue();
                } catch (Exception e) {
                    stop(false);
                    return;
                }
            }

            if (onTickSkill != null && ticksRemaining % tickInterval == 0) {
                executeSkill(onTickSkill);
            }
            ticksRemaining--;
        }

        public void stop(boolean executeEndSkill) {
            if (stopped) return;
            stopped = true;

            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            }
            activeMods.remove(id);

            AttributeInstance attr = entity.getAttribute(attribute);
            if (attr != null && currentModifier != null) {
                attr.removeModifier(currentModifier);
            }

            recalculateEntityAttributes(entity, attribute);

            if (executeEndSkill) {
                executeSkill(onEndSkill);
            }
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
