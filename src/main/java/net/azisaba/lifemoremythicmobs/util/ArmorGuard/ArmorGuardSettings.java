package net.azisaba.lifemoremythicmobs.util.ArmorGuard;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ArmorGuardSettings {
   public final boolean enabled;
   public final ArmorGuardSettings.PerAttr armor;
   public final ArmorGuardSettings.PerAttr toughness;
   public final int periodicCheckTicks;
   public final boolean debug;
   public final boolean playerOnly;
   public final ArmorGuardSettings.Safety safety;

   public ArmorGuardSettings(
      ArmorGuardSettings.PerAttr armor,
      ArmorGuardSettings.PerAttr toughness,
      int periodicCheckTicks,
      boolean debug,
      boolean playerOnly,
      boolean enabled,
      ArmorGuardSettings.Safety safety
   ) {
      this.armor = armor;
      this.toughness = toughness;
      this.periodicCheckTicks = periodicCheckTicks;
      this.debug = debug;
      this.playerOnly = playerOnly;
      this.enabled = enabled;
      this.safety = safety;
   }

   public static ArmorGuardSettings fromConfig(FileConfiguration config) {
      ConfigurationSection root = config.getConfigurationSection("armorGuard");
      if (root == null) {
         return new ArmorGuardSettings(
            new ArmorGuardSettings.PerAttr(0.0, 3.0, true, 0.0, 100.0),
            new ArmorGuardSettings.PerAttr(0.0, 3.0, true, 0.0, 100.0),
            10,
            false,
            true,
            true,
            ArmorGuardSettings.Safety.fromConfig(null)
         );
      }

      ArmorGuardSettings.PerAttr armor = readPer(root.getConfigurationSection("armor"), 0.0, 3.0, true, 0.0, 100.0);
      ArmorGuardSettings.PerAttr tough = readPer(root.getConfigurationSection("armor_toughness"), 0.0, 3.0, true, 0.0, 100.0);
      int ticks = root.getInt("periodic_check_ticks", 10);
      boolean debug = root.getBoolean("debug", false);
      boolean playersOnly = root.getBoolean("players_only", true);
      boolean enabled = root.getBoolean("enabled", true);
      ArmorGuardSettings.Safety safety = ArmorGuardSettings.Safety.fromConfig(root.getConfigurationSection("safety"));
      return new ArmorGuardSettings(armor, tough, ticks, debug, playersOnly, enabled, safety);
   }

   private static ArmorGuardSettings.PerAttr readPer(ConfigurationSection sec, double do2Min, double do2Max, boolean dNeg, double dMin, double dMax) {
      if (sec == null) {
         return new ArmorGuardSettings.PerAttr(do2Min, do2Max, dNeg, dMin, dMax);
      }

      double o2Min = sec.getDouble("o2_min_factor", do2Min);
      double o2Max = sec.getDouble("o2_max_factor", do2Max);
      boolean neg = sec.getBoolean("negative_prod_to_zero", dNeg);
      double fMin = sec.getDouble("final_min", dMin);
      double fMax = sec.getDouble("final_max", dMax);
      return new ArmorGuardSettings.PerAttr(o2Min, o2Max, neg, fMin, fMax);
   }

   public static class PerAttr {
      public final double o2Min;
      public final double o2Max;
      public final boolean negativeToZero;
      public final double finalMin;
      public final double finalMax;

      public PerAttr(double o2Min, double o2Max, boolean negativeToZero, double finalMin, double finalMax) {
         this.o2Min = o2Min;
         this.o2Max = o2Max;
         this.negativeToZero = negativeToZero;
         this.finalMin = finalMin;
         this.finalMax = finalMax;
      }
   }

   public static class Safety {
      public final double tinyProdEps;
      public final double hugeProdCap;
      public final double o2PatchAbsCap;
      public final double addPatchAbsCap;

      public Safety(double tinyProdEps, double hugeProdCap, double o2PatchAbsCap, double addPatchAbsCap) {
         this.tinyProdEps = tinyProdEps;
         this.hugeProdCap = hugeProdCap;
         this.o2PatchAbsCap = o2PatchAbsCap;
         this.addPatchAbsCap = addPatchAbsCap;
      }

      public static ArmorGuardSettings.Safety fromConfig(ConfigurationSection s) {
         double eps = 1.0E-6;
         double prodCap = 1.0E-6;
         double o2Cap = 4.0;
         double addCap = 1000.0;
         if (s != null) {
            eps = s.getDouble("tiny_prod_epsilon", eps);
            prodCap = s.getDouble("huge_prod_cap", prodCap);
            o2Cap = s.getDouble("o2_patch_abs_cap", o2Cap);
            addCap = s.getDouble("add_patch_abs_cap", addCap);
         }

         return new ArmorGuardSettings.Safety(eps, prodCap, o2Cap, addCap);
      }
   }
}
