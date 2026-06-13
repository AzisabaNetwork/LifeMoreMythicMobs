package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ConditionAction;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class MMReflect {
   private MMReflect() {
   }

   public static void copyActionAndVars(SkillCondition from, SkillCondition to) {
      try {
         Field fAction = SkillCondition.class.getDeclaredField("ACTION");
         fAction.setAccessible(true);
         ConditionAction action = (ConditionAction)fAction.get(from);
         to.setAction(action);
         Field fVar = SkillCondition.class.getDeclaredField("actionVar");
         fVar.setAccessible(true);
         Object ps = fVar.get(from);
         fVar.set(to, ps);
         Field fCondVar = SkillCondition.class.getDeclaredField("conditionVar");
         fCondVar.setAccessible(true);
         fCondVar.set(to, fCondVar.get(from));
      } catch (ReflectiveOperationException ex) {
         IgaDebugLogger.log("[LifeMoreMythicMobs]", "Failed to copy ACTION/actionVar via reflection: " + ex);
         to.setAction(ConditionAction.REQUIRED);
      }
   }

   public static void dumpAction(String tag, SkillCondition sc, SkillMetadata metaOrNull) {
      try {
         Field fAction = SkillCondition.class.getDeclaredField("ACTION");
         fAction.setAccessible(true);
         ConditionAction action = (ConditionAction)fAction.get(sc);
         Field fVar = SkillCondition.class.getDeclaredField("actionVar");
         fVar.setAccessible(true);
         Object ps = fVar.get(sc);
         String varShown = "null";
         if (ps != null) {
            try {
               if (metaOrNull != null) {
                  Method m = ps.getClass().getMethod("get", SkillMetadata.class);
                  varShown = String.valueOf(m.invoke(ps, metaOrNull));
               } else {
                  Method m = ps.getClass().getMethod("get");
                  varShown = String.valueOf(m.invoke(ps));
               }
            } catch (NoSuchMethodException ignore) {
               varShown = "<set>";
            }
         }
      } catch (ReflectiveOperationException ex) {
         IgaDebugLogger.log("[LifeMoreMythicMobs]", "Dump failed: " + ex);
      }
   }
}
