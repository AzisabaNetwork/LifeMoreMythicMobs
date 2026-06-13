package net.azisaba.lifemoremythicmobs.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class EquipLockManager {
   private static final EquipLockManager INSTANCE = new EquipLockManager();
   private final Map<UUID, Integer> lockMap = new HashMap<>();

   public static EquipLockManager getInstance() {
      return INSTANCE;
   }

   public synchronized void addLock(UUID uuid) {
      int count = this.lockMap.getOrDefault(uuid, 0);
      this.lockMap.put(uuid, count + 1);
   }

   public synchronized void removeLock(UUID uuid) {
      int count = this.lockMap.getOrDefault(uuid, 0);
      if (count <= 1) {
         this.lockMap.remove(uuid);
      } else {
         this.lockMap.put(uuid, count - 1);
      }
   }

   public synchronized boolean isLocked(Player player) {
      return this.lockMap.getOrDefault(player.getUniqueId(), 0) > 0;
   }
}
