package net.azisaba.lifemoremythicmobs.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EquipLockManager {
   private static final EquipLockManager INSTANCE = new EquipLockManager();
   private final Map<UUID, Integer> lockMap = new HashMap<>();

   public static EquipLockManager getInstance() { return INSTANCE; }

   public synchronized void addLock(UUID uuid) {
      lockMap.merge(uuid, 1, Integer::sum);
   }

   public synchronized void removeLock(UUID uuid) {
      lockMap.computeIfPresent(uuid, (k, v) -> v <= 1 ? null : v - 1);
   }
}
