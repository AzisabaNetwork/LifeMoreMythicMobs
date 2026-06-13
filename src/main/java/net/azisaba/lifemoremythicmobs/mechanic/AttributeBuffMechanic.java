package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.LivingEntity;

public class AttributeBuffMechanic extends SkillMechanic implements ITargetedEntitySkill, INoTargetSkill {
   private final PlaceholderString attributeName;
   private final PlaceholderDouble amount;
   private final PlaceholderString operation;
   private final PlaceholderDouble duration;
   private final PlaceholderString key;
   private final boolean refresh;
   private final boolean healToMax;
   private final boolean log;
   private static final Map<String, Integer> GEN_MAP = new ConcurrentHashMap<>();

   public AttributeBuffMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.attributeName = PlaceholderString.of(config.getString(new String[]{"attribute"}, "GENERIC_ARMOR", new String[0]));
      this.amount = PlaceholderDouble.of(config.getString(new String[]{"amount", "a"}, "0", new String[0]));
      this.operation = PlaceholderString.of(config.getString(new String[]{"operation", "o"}, "add", new String[0]));
      this.duration = PlaceholderDouble.of(config.getString(new String[]{"duration", "d"}, "5", new String[0]));
      this.key = PlaceholderString.of(config.getString(new String[]{"key", "k"}, "<attr>", new String[0]));
      this.refresh = config.getBoolean(new String[]{"refresh", "r"}, true);
      this.healToMax = config.getBoolean(new String[]{"healToMax", "h"}, false);
      this.log = config.getBoolean(new String[]{"log"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (target == null || target.getBukkitEntity() == null) {
         return SkillResult.FAILURE;
      }

      if (!(target.getBukkitEntity() instanceof LivingEntity)) {
         return SkillResult.FAILURE;
      }

      LivingEntity le = (LivingEntity)target.getBukkitEntity();
      String attrRaw = normalizeAttr(this.attributeName.get(data, target));
      Attribute attr = parseAttribute(attrRaw);
      if (attr == null) {
         if (this.log) {
            IgaDebugLogger.log(this.getClass(), "Unknown attribute: " + attrRaw);
         }

         return SkillResult.FAILURE;
      } else {
         double amount = this.amount.get(data, target);
         if (isFinite(amount) && !(Math.abs(amount) < 1.0E-12)) {
            Operation op = parseOperation(safeLower(this.operation.get(data, target)));
            long ticks = Math.max(1L, Math.round(this.duration.get(data, target) * 20.0));
            String keyArg = this.key.get(data, target);
            String key = !"<attr>".equals(keyArg) && keyArg != null && !keyArg.isEmpty() ? keyArg : attr.name();
            AttributeInstance inst = le.getAttribute(attr);
            if (inst == null) {
               if (this.log) {
                  IgaDebugLogger.log(this.getClass(), String.format("entity %s has no attribute %s, skip.", le.getType(), attr.name()));
               }

               return SkillResult.FAILURE;
            } else {
               UUID uuid = UUID.nameUUIDFromBytes(("IgaAttrBuff:" + attr.name() + ":" + key).getBytes());
               inst.getModifiers().stream().filter(m -> m.getUniqueId().equals(uuid)).forEach(inst::removeModifier);
               amount = clampFinite(amount, -1024.0, 1024.0);
               AttributeModifier mod = new AttributeModifier(uuid, "IgaAttrBuff:" + key, amount, op);
               inst.addModifier(mod);
               if (this.log) {
                  IgaDebugLogger.log(this.getClass(), String.format("add %s amount=%.5f op=%s key=%s to=%s", attr.name(), amount, op.name(), key, le.getName()));
               }

               if (this.healToMax && attr == Attribute.GENERIC_MAX_HEALTH) {
                  try {
                     double newMax = inst.getValue();
                     le.setHealth(Math.min(newMax, newMax));
                  } catch (Throwable var19) {
                  }
               }

               this.scheduleTimeRemoval(le, attr, uuid, ticks);
               return SkillResult.SUCCESS;
            }
         } else {
            IgaDebugLogger.log(this.getClass(), "amount is zero/non-finite. skip.");
            return SkillResult.FAILURE;
         }
      }
   }

   public SkillResult cast(SkillMetadata data) {
      return this.castAtEntity(data, data.getCaster().getEntity());
   }

   private static String safeLower(String s) {
      return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
   }

   private static boolean isFinite(double v) {
      return !Double.isNaN(v) && !Double.isInfinite(v);
   }

   private static double clampFinite(double v, double min, double max) {
      return !isFinite(v) ? 0.0 : Math.max(min, Math.min(max, v));
   }

   private static String normalizeAttr(String raw) {
      if (raw == null) {
         return "";
      }

      String s = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(" ", "_");
      if ("ARMOR_TOUGHNESS".equals(s)) {
         s = "GENERIC_ARMOR_TOUGHNESS";
      }

      if ("ARMOR".equals(s)) {
         s = "GENERIC_ARMOR";
      }

      if ("MAX_HEALTH".equals(s)) {
         s = "GENERIC_MAX_HEALTH";
      }

      if ("FOLLOW_RANGE".equals(s)) {
         s = "GENERIC_FOLLOW_RANGE";
      }

      if ("KNOCKBACK_RESISTANCE".equals(s)) {
         s = "GENERIC_KNOCKBACK_RESISTANCE";
      }

      if ("MOVEMENT_SPEED".equals(s)) {
         s = "GENERIC_MOVEMENT_SPEED";
      }

      if ("FLYING_SPEED".equals(s)) {
         s = "GENERIC_FLYING_SPEED";
      }

      if ("ATTACK_DAMAGE".equals(s)) {
         s = "GENERIC_ATTACK_DAMAGE";
      }

      if ("ATTACK_SPEED".equals(s)) {
         s = "GENERIC_ATTACK_SPEED";
      }

      if ("LUCK".equals(s)) {
         s = "GENERIC_LUCK";
      }

      return s;
   }

   private static Attribute parseAttribute(String name) {
      try {
         return Attribute.valueOf(name);
      } catch (IllegalArgumentException e) {
         return null;
      }
   }

   private static Operation parseOperation(String op) {
      String var1 = op;
      switch (op.hashCode()) {
         case -908189716:
            if (!var1.equals("scalar")) {
               return Operation.ADD_NUMBER;
            }
            break;
         case 96417:
            if (var1.equals("add")) {
               return Operation.ADD_NUMBER;
            }

            return Operation.ADD_NUMBER;
         case 108484:
            if (var1.equals("mul")) {
               return Operation.MULTIPLY_SCALAR_1;
            }

            return Operation.ADD_NUMBER;
         case 104256825:
            if (var1.equals("multi")) {
               return Operation.MULTIPLY_SCALAR_1;
            }

            return Operation.ADD_NUMBER;
         case 109250890:
            if (!var1.equals("scale")) {
               return Operation.ADD_NUMBER;
            }
            break;
         case 653829668:
            if (var1.equals("multiply")) {
               return Operation.MULTIPLY_SCALAR_1;
            }

            return Operation.ADD_NUMBER;
         default:
            return Operation.ADD_NUMBER;
      }

      try {
         return Operation.valueOf("ADD_SCALAR");
      } catch (IllegalArgumentException e) {
         return Operation.MULTIPLY_SCALAR_1;
      }
   }

   private void scheduleTimeRemoval(LivingEntity le, Attribute attr, UUID uuid, long delayTicks) {
      String genKey = genKey(le, attr, uuid);
      int myGen = GEN_MAP.merge(genKey, 1, (oldV, n) -> this.refresh ? oldV + 1 : oldV + 1);
      Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> {
         Integer g = GEN_MAP.get(genKey);
         if (g != null && g == myGen) {
            AttributeInstance inst = le.getAttribute(attr);
            if (inst != null) {
               inst.getModifiers().stream().filter(m -> m.getUniqueId().equals(uuid)).forEach(inst::removeModifier);
               if (this.log) {
                  IgaDebugLogger.log(this.getClass(), String.format("removed %s uuid=%s from=%s", attr.name(), uuid, le.getName()));
               }

               if (attr == Attribute.GENERIC_MAX_HEALTH) {
                  try {
                     double newMax = inst.getValue();
                     if (le.getHealth() > newMax) {
                        le.setHealth(newMax);
                     }
                  } catch (Throwable var11) {
                  }
               }
            }

            GEN_MAP.remove(genKey, myGen);
         }
      }, delayTicks);
   }

   private static String genKey(LivingEntity le, Attribute attr, UUID uuid) {
      return le.getUniqueId() + "|" + attr.name() + "|" + uuid.toString();
   }
}
