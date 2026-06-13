package net.azisaba.lifemoremythicmobs.conditions;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import java.util.Arrays;
import java.util.UUID;
import org.bukkit.Bukkit;

public class ChinChiro456Condition extends SkillCondition implements ISkillMetaCondition {
   private final PlaceholderInt v1;
   private final PlaceholderInt v2;
   private final PlaceholderInt v3;
   private final String v1Raw;
   private final String v2Raw;
   private final String v3Raw;
   private final boolean strictDice;
   private final boolean passOnError;
   private final boolean debug;

   public ChinChiro456Condition(String line, MythicLineConfig config) {
      super(line);
      Bukkit.getLogger().info("[ChinChiro456Condition] lineの中身:" + line);
      this.v1Raw = config.getString(new String[]{"v1", "a", "x", "d1"}, "0", new String[0]);
      this.v2Raw = config.getString(new String[]{"v2", "b", "y", "d2"}, "0", new String[0]);
      this.v3Raw = config.getString(new String[]{"v3", "c", "z", "d3"}, "0", new String[0]);
      this.v1 = PlaceholderInt.of(this.v1Raw);
      this.v2 = PlaceholderInt.of(this.v2Raw);
      this.v3 = PlaceholderInt.of(this.v3Raw);
      this.strictDice = config.getBoolean(new String[]{"strict", "strictdice"}, true);
      this.passOnError = config.getBoolean(new String[]{"passonerror", "poe"}, false);
      this.debug = config.getBoolean(new String[]{"debug"}, false);
      this.log("=== Constructor ===");
      this.log("line: " + line);
      this.log("args raw -> v1: '" + this.v1Raw + "', v2: '" + this.v2Raw + "', v3: '" + this.v3Raw + "'");
      this.log("flags -> strictDice=" + this.strictDice + ", passOnError=" + this.passOnError + ", debug=" + this.debug);
   }

   public boolean check(SkillMetadata data) {
      this.log("--- check() start ---");
      if (data == null) {
         this.log("SkillMetadata is null (unexpected).");
      } else {
         try {
            UUID casterUuid = data.getCaster() != null && data.getCaster().getEntity() != null ? data.getCaster().getEntity().getUniqueId() : null;
            UUID triggerUuid = data.getTrigger() != null ? data.getTrigger().getUniqueId() : null;
            this.log("meta: casterUUID=" + casterUuid + ", triggerUUID=" + triggerUuid);
         } catch (Throwable t) {
            this.log("Failed to read meta basic info: " + t.getClass().getSimpleName() + ": " + t.getMessage());
         }
      }

      try {
         int a = this.v1.get(data);
         int b = this.v2.get(data);
         int c = this.v3.get(data);
         this.log("resolved -> a=" + a + " (from '" + this.v1Raw + "'), b=" + b + " (from '" + this.v2Raw + "'), c=" + c + " (from '" + this.v3Raw + "')");
         this.log("isDice(a/b/c) -> " + isDice(a) + "/" + isDice(b) + "/" + isDice(c) + " (strictDice=" + this.strictDice + ")");
         if (!this.strictDice || isDice(a) && isDice(b) && isDice(c)) {
            int[] arr = new int[]{a, b, c};
            Arrays.sort(arr);
            boolean result = arr[0] == 4 && arr[1] == 5 && arr[2] == 6;
            this.log("sorted -> [" + arr[0] + ", " + arr[1] + ", " + arr[2] + "]");
            this.log("final result=" + result);
            this.log("--- check() end ---");
            return result;
         } else {
            this.log("strictDice check failed: one or more values are out of 1..6");
            this.log("--- check() end: result=false (strict failure) ---");
            return false;
         }
      } catch (Exception e) {
         this.log("Exception in check(): " + e.getClass().getSimpleName() + ": " + e.getMessage());
         if (this.debug) {
            e.printStackTrace();
         }

         this.log("--- check() end: result=" + this.passOnError + " (passOnError) ---");
         return this.passOnError;
      }
   }

   private static boolean isDice(int n) {
      return n >= 1 && n <= 6;
   }

   private static int parseInt(String s) {
      return Integer.parseInt(s.trim());
   }

   private void log(String msg) {
      if (this.debug) {
         IgaDebugLogger.log(this.getClass(), "[ChinChiro456Condition] " + msg);
      }
   }
}
