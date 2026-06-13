package net.azisaba.lifemoremythicmobs.util;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;

public class TickCounter {
   private static volatile long currentTick = 0L;

   public static void start() {
      Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> currentTick++, 0L, 1L);
   }

   public static long getCurrentTick() {
      return currentTick;
   }
}
