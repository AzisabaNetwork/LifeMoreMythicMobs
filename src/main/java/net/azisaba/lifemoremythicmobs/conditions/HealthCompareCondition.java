package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class HealthCompareCondition extends SkillCondition implements IEntityCondition {
   private final PlaceholderDouble value;
   private final HealthCompareCondition.Op op;
   private final double epsilon;

   public HealthCompareCondition(String line, MythicLineConfig config) {
      super(line);
      this.value = PlaceholderDouble.of(config.getString(new String[]{"value", "val", "v", "health", "h", "amount", "a"}, "0", new String[0]));
      String rawOp = config.getString(new String[]{"op", "o", "compare", "cmp"}, ">=", new String[0]).trim().toLowerCase();
      this.op = this.parseOp(rawOp);
      this.epsilon = config.getDouble(new String[]{"epsilon", "eps"}, 1.0E-9);
   }

   private HealthCompareCondition.Op parseOp(String s) {
      String var2 = s;
      switch (s.hashCode()) {
         case 60:
            if (var2.equals("<")) {
               return HealthCompareCondition.Op.LT;
            }
            break;
         case 61:
            if (var2.equals("=")) {
               return HealthCompareCondition.Op.EQ;
            }
            break;
         case 62:
            if (var2.equals(">")) {
               return HealthCompareCondition.Op.GT;
            }
            break;
         case 1921:
            if (var2.equals("<=")) {
               return HealthCompareCondition.Op.LE;
            }
            break;
         case 1952:
            if (var2.equals("==")) {
               return HealthCompareCondition.Op.EQ;
            }
            break;
         case 1983:
            if (var2.equals(">=")) {
               return HealthCompareCondition.Op.GE;
            }
            break;
         case 3244:
            if (var2.equals("eq")) {
               return HealthCompareCondition.Op.EQ;
            }
            break;
         case 3294:
            if (var2.equals("ge")) {
               return HealthCompareCondition.Op.GE;
            }
            break;
         case 3309:
            if (var2.equals("gt")) {
               return HealthCompareCondition.Op.GT;
            }
            break;
         case 3449:
            if (var2.equals("le")) {
               return HealthCompareCondition.Op.LE;
            }
            break;
         case 3464:
            if (var2.equals("lt")) {
               return HealthCompareCondition.Op.LT;
            }
            break;
         case 102680:
            if (var2.equals("gte")) {
               return HealthCompareCondition.Op.GE;
            }
            break;
         case 107485:
            if (var2.equals("lte")) {
               return HealthCompareCondition.Op.LE;
            }
      }

      return HealthCompareCondition.Op.GE;
   }

   public boolean check(AbstractEntity target) {
      if (target == null) {
         return false;
      }

      Entity bukkit = target.getBukkitEntity();
      if (!(bukkit instanceof LivingEntity)) {
         return false;
      }

      LivingEntity le = (LivingEntity)bukkit;
      double currentHp = le.getHealth();
      double rhs = this.value.get(target);
      switch (this.op) {
         case LT:
            if (currentHp < rhs) {
               return true;
            }

            return false;
         case LE:
            if (!(currentHp <= rhs) && !this.nearlyEquals(currentHp, rhs)) {
               return false;
            }

            return true;
         case EQ:
            return this.nearlyEquals(currentHp, rhs);
         case GE:
            if (!(currentHp >= rhs) && !this.nearlyEquals(currentHp, rhs)) {
               return false;
            }

            return true;
         case GT:
            if (currentHp > rhs) {
               return true;
            }

            return false;
         default:
            return false;
      }
   }

   private boolean nearlyEquals(double a, double b) {
      return Math.abs(a - b) <= this.epsilon;
   }

   private enum Op {
      LT,
      LE,
      EQ,
      GE,
      GT;
   }
}
