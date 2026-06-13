package net.azisaba.lifemoremythicmobs.util;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CustomAura implements Listener, Runnable {
    protected final AbstractEntity target;
    protected final SkillMetadata data;
    protected final String auraName;
    protected final String id;
    protected int ticksRemaining;
    protected final int tickInterval;
    protected int taskId = -1;
    protected boolean ended = false;

    protected static final Map<String, CustomAura> ACTIVE_AURAS = new ConcurrentHashMap<>();

    public CustomAura(AbstractEntity target, SkillMetadata data, String auraName, int duration, int tickInterval) {
        this.target = target;
        this.data = data;
        this.auraName = auraName;
        this.id = target.getUniqueId().toString() + ":" + auraName;
        this.ticksRemaining = duration;
        this.tickInterval = tickInterval;

        Plugin plugin = LifeMoreMythicMobs.inst();
        ACTIVE_AURAS.put(id, this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
    }

    public void refresh(int newDuration) {
        this.ticksRemaining = newDuration;
    }

    @Override
    public void run() {
        if (ended) return;
        if (target.isDead()) {
            stop(false);
            return;
        }
        if (ticksRemaining <= 0) {
            stop(true);
            return;
        }

        onTick();
        ticksRemaining--;
    }

    protected abstract void onTick();

    public void stop(boolean timeOut) {
        if (ended) return;
        ended = true;
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        HandlerList.unregisterAll(this);
        ACTIVE_AURAS.remove(id);
        onEnd(timeOut);
    }

    protected abstract void onEnd(boolean timeOut);

    public static void remove(AbstractEntity target, String auraName) {
        String id = target.getUniqueId().toString() + ":" + auraName;
        CustomAura aura = ACTIVE_AURAS.get(id);
        if (aura != null) {
            aura.stop(false);
        }
    }

    public static void removeAll(UUID uuid) {
        String uuidStr = uuid.toString();
        ACTIVE_AURAS.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(uuidStr)) {
                entry.getValue().stop(false);
                return true;
            }
            return false;
        });
    }

    public static CustomAura getActive(String id) {
        return ACTIVE_AURAS.get(id);
    }
}
