package net.azisaba.lifemoremythicmobs.util;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks temporary entities spawned by LifeMoreMythicMobs (e.g. Display/Interaction)
 * so they are reliably removed on plugin disable or if orphaned after a crash.
 */
public final class ManagedEntityRegistry implements Listener {
    private static final Set<UUID> ACTIVE_ENTITIES = ConcurrentHashMap.newKeySet();
    private static volatile NamespacedKey managedKey;

    public static void init(LifeMoreMythicMobs plugin) {
        managedKey = new NamespacedKey(plugin, "lmmm_managed");
    }

    public static void track(Entity entity) {
        if (entity == null) return;
        ACTIVE_ENTITIES.add(entity.getUniqueId());
        if (managedKey != null) {
            entity.getPersistentDataContainer().set(managedKey, PersistentDataType.BYTE, (byte) 1);
        }
    }

    public static void untrack(Entity entity) {
        if (entity == null) return;
        ACTIVE_ENTITIES.remove(entity.getUniqueId());
    }

    public static void removeAndUntrack(Entity entity) {
        if (entity == null) return;
        ACTIVE_ENTITIES.remove(entity.getUniqueId());
        if (entity.isValid() || !entity.isDead()) {
            entity.remove();
        }
    }

    public static boolean isManaged(Entity entity) {
        if (entity == null) return false;
        if (ACTIVE_ENTITIES.contains(entity.getUniqueId())) return true;
        return managedKey != null && entity.getPersistentDataContainer().has(managedKey, PersistentDataType.BYTE);
    }

    public static void shutdownAll() {
        for (UUID uuid : ACTIVE_ENTITIES) {
            for (World world : Bukkit.getWorlds()) {
                Entity e = world.getEntity(uuid);
                if (e != null) {
                    e.remove();
                    break;
                }
            }
        }
        ACTIVE_ENTITIES.clear();
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        if (managedKey == null) return;
        for (Entity entity : event.getEntities()) {
            if (!ACTIVE_ENTITIES.contains(entity.getUniqueId())
                    && entity.getPersistentDataContainer().has(managedKey, PersistentDataType.BYTE)) {
                entity.remove();
            }
        }
    }
}
