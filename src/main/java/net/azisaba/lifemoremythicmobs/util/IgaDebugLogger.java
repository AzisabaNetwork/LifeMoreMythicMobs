package net.azisaba.lifemoremythicmobs.util;

import org.bukkit.Bukkit;

public class IgaDebugLogger {
   private static final String PREFIX = "[LifeMoreMythicMobs]";

   public static void log(String tag, String msg) {
      Bukkit.getLogger().info("[LifeMoreMythicMobs][" + tag + "]" + msg);
   }

   public static void log(Class<?> clazz, String msg) {
      log(clazz.getSimpleName(), msg);
   }

   public static void log(String msg) {
      log("General", msg);
   }
}
