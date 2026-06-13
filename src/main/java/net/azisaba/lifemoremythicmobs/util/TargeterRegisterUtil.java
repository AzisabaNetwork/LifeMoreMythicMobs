package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.targeters.IEntitySelector;
import io.lumine.mythic.util.annotations.MythicTargeter;
import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TargeterRegisterUtil {
   private static final Logger LOGGER = Logger.getLogger("LifeMoreMythicMobs");

   private TargeterRegisterUtil() {
   }

   public static boolean registerIfMatches(MythicTargeterLoadEvent event, Class<? extends IEntitySelector> clazz) {
      MythicTargeter ann = clazz.getAnnotation(MythicTargeter.class);
      if (ann == null) {
         return false;
      }

      String requested = event.getTargeterName();
      if (requested == null) {
         return false;
      }

      String reqLower = requested.toLowerCase(Locale.ROOT);
      Logger logger = MythicBukkit.inst().getLogger();
      logger.info("[TargeterRegister] requested=" + requested + ", checking class=" + clazz.getSimpleName() + " (name=" + ann.name() + ")");
      if (ann.name() != null && !ann.name().isEmpty() && reqLower.equals(ann.name().toLowerCase(Locale.ROOT))) {
         logger.info("[TargeterRegister] matched by name: " + ann.name() + " -> " + clazz.getSimpleName());
         return instantiateAndRegister(event, clazz);
      }

      String[] var9;
      int var8 = (var9 = ann.aliases()).length;

      for (int var7 = 0; var7 < var8; var7++) {
         String alias = var9[var7];
         if (alias != null && !alias.isEmpty() && reqLower.equals(alias.toLowerCase(Locale.ROOT))) {
            logger.info("[TargeterRegister] matched by alias: " + alias + " -> " + clazz.getSimpleName());
            return instantiateAndRegister(event, clazz);
         }
      }

      return false;
   }

   private static boolean instantiateAndRegister(MythicTargeterLoadEvent event, Class<? extends IEntitySelector> clazz) {
      try {
         MythicLineConfig config = event.getConfig();
         Constructor<? extends IEntitySelector> ctor = clazz.getConstructor(MythicLineConfig.class);
         IEntitySelector instance = ctor.newInstance(config);
         event.register(instance);
         return true;
      } catch (NoSuchMethodException ex) {
         LOGGER.log(Level.SEVERE, "[LifeMoreMythicMobs] Targeter class " + clazz.getName() + " must have a constructor(MythicLineConfig config)", ex);
      } catch (Exception ex) {
         LOGGER.log(Level.SEVERE, "[LifeMoreMythicMobs] Failed to instantiate targeter class " + clazz.getName(), ex);
      }

      return false;
   }
}
