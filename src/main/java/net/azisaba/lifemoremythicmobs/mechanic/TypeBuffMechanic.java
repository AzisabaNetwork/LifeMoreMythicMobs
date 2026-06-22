package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.MythicMobs;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.Skill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import net.azisaba.lifemoremythicmobs.util.AuraSkillHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TypeBuffMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private static final ConcurrentHashMap<String, List<TypeBuffAura>> REGISTRY = new ConcurrentHashMap<>();

    protected final String auraName;
    protected final Map<String, Double> mods;
    protected final int duration;
    protected final int maxStacks;
    protected final boolean stackTimer;
    protected final boolean refreshDuration;
    protected final int interval;
    protected final String onStartSkillName;
    protected final String onTickSkillName;
    protected final String onEndSkillName;

    public TypeBuffMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.auraName = config.getString(new String[]{"auraName", "aura", "name", "n"}, "typebuff");

        String modsRaw = config.getString(new String[]{"mods", "mod"}, "");
        this.mods = parseMods(modsRaw);

        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.maxStacks = config.getInteger(new String[]{"maxstacks", "ms"}, 1);
        this.stackTimer = config.getBoolean(new String[]{"stacktimer", "st"}, false);
        this.refreshDuration = config.getBoolean(new String[]{"refreshDuration", "rd"}, false);
        this.interval = config.getInteger(new String[]{"interval", "i"}, 4);
        this.onStartSkillName = config.getString(new String[]{"onStart", "oS"}, "");
        this.onTickSkillName = config.getString(new String[]{"onTick", "oT"}, "");
        this.onEndSkillName = config.getString(new String[]{"onEnd", "oE"}, "");
    }

    private static Map<String, Double> parseMods(String raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyMap();
        String cleanRaw = raw.replace("\"", "").replace("<&sp>", " ").replace("<&cm>", ",");
        Map<String, Double> result = new LinkedHashMap<>();
        for (String entry : cleanRaw.split(",")) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split("\\s+");
            if (parts.length < 2) continue;
            try {
                String valStr = parts[1].replace("+", "");
                double modValue;
                if (valStr.contains("%")) {
                    modValue = Double.parseDouble(valStr.replace("%", "")) / 100.0;
                } else {
                    modValue = Double.parseDouble(valStr);
                }
                result.put(parts[0], modValue);
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    private static Plugin resolvePlugin() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LifeMoreMythicMobs");
        if (plugin == null) plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        return plugin;
    }

    private static Optional<Skill> resolveSkill(String name) {
        if (name == null || name.isEmpty()) return Optional.empty();
        return MythicMobs.inst().getSkillManager().getSkill(name);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        String registryKey = target.getUniqueId().toString() + ":" + auraName;
        List<TypeBuffAura> stacks = REGISTRY.computeIfAbsent(registryKey, k -> new ArrayList<>());

        synchronized (stacks) {
            int active = countActive(stacks);

            if (refreshDuration) {
                for (TypeBuffAura aura : stacks) {
                    if (!aura.ended) aura.ticksRemaining = duration;
                }
            }

            if (active >= maxStacks) return true;

            new TypeBuffAura(
                    target.getUniqueId(), registryKey, auraName, mods, duration,
                    maxStacks, stackTimer, interval,
                    resolveSkill(onStartSkillName),
                    resolveSkill(onTickSkillName),
                    resolveSkill(onEndSkillName),
                    data, stacks
            );
        }
        return true;
    }

    private static int countActive(List<TypeBuffAura> stacks) {
        int count = 0;
        for (TypeBuffAura a : stacks) {
            if (!a.ended) count++;
        }
        return count;
    }

    public static void remove(AbstractEntity target, String auraName) {
        remove(target, auraName, -1);
    }

    public static void remove(AbstractEntity target, String auraName, int count) {
        String registryKey = target.getUniqueId().toString() + ":" + auraName;
        List<TypeBuffAura> stacks = REGISTRY.get(registryKey);
        if (stacks == null) return;
        synchronized (stacks) {
            if (count < 0) {
                new ArrayList<>(stacks).forEach(aura -> aura.terminate(false));
            } else {
                int removed = 0;
                for (TypeBuffAura aura : new ArrayList<>(stacks)) {
                    if (removed >= count) break;
                    if (!aura.ended) {
                        aura.terminate(false);
                        removed++;
                    }
                }
            }
        }
    }

    public static Map<String, Double> getCombinedMods(UUID uuid) {
        Map<String, Double> deltaAccum = new HashMap<>();
        REGISTRY.forEach((key, stacks) -> {
            if (!key.startsWith(uuid.toString())) return;
            synchronized (stacks) {
                for (TypeBuffAura aura : stacks) {
                    if (!aura.ended) {
                        aura.mods.forEach((element, val) ->
                                deltaAccum.merge(element, val, Double::sum)
                        );
                    }
                }
            }
        });
        Map<String, Double> combined = new HashMap<>();
        deltaAccum.forEach((element, totalDelta) ->
                combined.put(element, 1.0 + totalDelta)
        );
        return combined;
    }

    public static boolean hasAura(UUID uuid, String auraName) {
        String registryKey = uuid.toString() + ":" + auraName;
        List<TypeBuffAura> stacks = REGISTRY.get(registryKey);
        if (stacks == null) return false;
        synchronized (stacks) {
            return countActive(stacks) > 0;
        }
    }

    public static int getStacks(UUID uuid, String auraName) {
        String registryKey = uuid.toString() + ":" + auraName;
        List<TypeBuffAura> stacks = REGISTRY.get(registryKey);
        if (stacks == null) return 0;
        synchronized (stacks) {
            return countActive(stacks);
        }
    }

    public static void removeAll(UUID uuid) {
        String uuidStr = uuid.toString();
        REGISTRY.forEach((key, stacks) -> {
            if (key.startsWith(uuidStr)) {
                synchronized (stacks) {
                    new ArrayList<>(stacks).forEach(aura -> aura.terminate(false));
                }
            }
        });
    }

    static class TypeBuffAura implements Runnable {
        private final UUID targetUUID;
        private final String registryKey;
        final String auraName;
        final Map<String, Double> mods;
        final int maxStacks;
        final boolean stackTimer;
        final int interval;
        final Optional<Skill> onStartSkill;
        final Optional<Skill> onTickSkill;
        final Optional<Skill> onEndSkill;
        final SkillMetadata originMeta;
        final List<TypeBuffAura> stackList;
        volatile boolean ended = false;
        volatile int ticksRemaining;
        private int ticksSinceLastTick = 0;
        private int taskId = -1;

        TypeBuffAura(UUID targetUUID, String registryKey, String auraName,
                     Map<String, Double> mods, int duration, int maxStacks, boolean stackTimer,
                     int interval,
                     Optional<Skill> onStartSkill, Optional<Skill> onTickSkill, Optional<Skill> onEndSkill,
                     SkillMetadata originMeta, List<TypeBuffAura> stackList) {
            this.targetUUID = targetUUID;
            this.registryKey = registryKey;
            this.auraName = auraName;
            this.mods = mods;
            this.ticksRemaining = duration;
            this.maxStacks = maxStacks;
            this.stackTimer = stackTimer;
            this.interval = interval;
            this.onStartSkill = onStartSkill;
            this.onTickSkill = onTickSkill;
            this.onEndSkill = onEndSkill;
            this.originMeta = originMeta;
            this.stackList = stackList;

            Plugin plugin = resolvePlugin();
            if (plugin != null) {
                stackList.add(this);
                this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1L, 1L);
                executeAuraSkill(onStartSkill);
            }
        }

        @Override
        public void run() {
            if (ended) {
                cancelTask();
                return;
            }

            synchronized (stackList) {
                boolean isFirst = false;
                for (TypeBuffAura aura : stackList) {
                    if (!aura.ended) {
                        if (aura == this) isFirst = true;
                        break;
                    }
                }

                if (isFirst) {
                    ticksRemaining--;
                    ticksSinceLastTick++;

                    if (ticksSinceLastTick >= interval) {
                        ticksSinceLastTick = 0;
                        executeAuraSkill(onTickSkill);
                    }

                    if (ticksRemaining <= 0) {
                        if (stackTimer) {
                            terminate(false);
                        } else {
                            terminateAll();
                        }
                    }
                }
            }
        }

        private void executeAuraSkill(Optional<Skill> skillOpt) {
            if (!skillOpt.isPresent()) return;
            Skill skill = skillOpt.get();
            SkillMetadata meta = originMeta.deepClone();
            AuraSkillHelper.setMeta(meta, getTargetEntity());
            meta.getVariables().putString("aura-name", auraName);
            meta.getVariables().putInt("aura-stacks", getStacks(targetUUID, auraName));
            meta.getVariables().putInt("aura-duration", ticksRemaining);
            skill.execute(meta);
        }

        private AbstractEntity getTargetEntity() {
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                org.bukkit.entity.Entity e = world.getEntity(targetUUID);
                if (e != null) return io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter.adapt(e);
            }
            return originMeta.getTrigger();
        }

        void terminate(boolean fromAll) {
            if (ended) return;
            ended = true;
            cancelTask();
            executeAuraSkill(onEndSkill);
            if (!fromAll) {
                synchronized (stackList) {
                    stackList.remove(this);
                    if (stackList.isEmpty()) {
                        REGISTRY.remove(registryKey);
                    }
                }
            }
        }

        private void terminateAll() {
            synchronized (stackList) {
                List<TypeBuffAura> copy = new ArrayList<>(stackList);
                for (TypeBuffAura a : copy) {
                    a.terminate(true);
                }
                stackList.clear();
                REGISTRY.remove(registryKey);
            }
        }

        private void cancelTask() {
            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            }
        }

        public UUID getTargetUUID() {
            return targetUUID;
        }
    }
}