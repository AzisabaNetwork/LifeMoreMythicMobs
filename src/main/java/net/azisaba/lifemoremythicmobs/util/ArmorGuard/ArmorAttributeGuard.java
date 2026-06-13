package net.azisaba.lifemoremythicmobs.util.ArmorGuard;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ArmorAttributeGuard {
   private final Plugin plugin;
   private final ArmorGuardSettings settings;
   private int taskId = -1;

   public ArmorAttributeGuard(Plugin plugin, ArmorGuardSettings settings) {
      this.plugin = plugin;
      this.settings = settings;
   }

   public void register() {
      if (!this.settings.enabled) {
         this.log("Disabled via config. Not registering listeners or tasks.");
      } else {
         Bukkit.getPluginManager().registerEvents(new ArmorGuardListener(this), this.plugin);
         int period = Math.max(0, this.settings.periodicCheckTicks);
         if (period > 0) {
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
               for (Player p : Bukkit.getOnlinePlayers()) {
                  this.applyAll(p);
               }
            }, period, period);
         }

         this.log("ArmorAttributeGuard registered. period=" + period);
      }
   }

   public void unregister() {
      if (this.taskId != -1) {
         Bukkit.getScheduler().cancelTask(this.taskId);
         this.taskId = -1;
      }
   }

   public void scheduleApply(LivingEntity e) {
      if (e != null && !e.isDead()) {
         if (this.settings.enabled) {
            if (!this.settings.playerOnly || e instanceof Player) {
               Bukkit.getScheduler().runTask(this.plugin, () -> this.applyAll(e));
            }
         }
      }
   }

   public void applyAll(LivingEntity e) {
      if (this.settings.enabled) {
         if (!this.settings.playerOnly || e instanceof Player) {
            this.guardAttribute(
               e,
               Attribute.GENERIC_ARMOR,
               this.settings.armor.o2Min,
               this.settings.armor.o2Max,
               this.settings.armor.negativeToZero,
               this.settings.armor.finalMin,
               this.settings.armor.finalMax
            );
            this.guardAttribute(
               e,
               Attribute.GENERIC_ARMOR_TOUGHNESS,
               this.settings.armor.o2Min,
               this.settings.armor.o2Max,
               this.settings.armor.negativeToZero,
               this.settings.armor.finalMin,
               this.settings.armor.finalMax
            );
         }
      }
   }

   private static UUID o2UUID(Attribute attr) {
      return UUID.nameUUIDFromBytes(("IgaO2Guard:" + attr.name()).getBytes());
   }

   private static UUID addUUID(Attribute attr) {
      return UUID.nameUUIDFromBytes(("IgaClamp:" + attr.name()).getBytes());
   }

   private void guardAttribute(LivingEntity e, Attribute attr, double o2Min, double o2Max, boolean negToZero, double finalMin, double finalMax) {
      AttributeInstance inst = e.getAttribute(attr);
      if (inst != null) {
         UUID myO2 = o2UUID(attr);
         UUID myAdd = addUUID(attr);
         removeByUUID(inst, myO2);
         removeByUUID(inst, myAdd);
         List<AttributeModifier> mods = new ArrayList<>(inst.getModifiers());
         List<AttributeModifier> addMods = filter(mods, Operation.ADD_NUMBER, myO2, myAdd);
         List<AttributeModifier> o1Mods = filter(mods, Operation.ADD_SCALAR, myO2, myAdd);
         List<AttributeModifier> o2Mods = filter(mods, Operation.MULTIPLY_SCALAR_1, myO2, myAdd);
         ArmorGuardSettings.Safety safety = this.settings.safety;
         double EPS = safety.tinyProdEps;
         double PROD_CAP = safety.hugeProdCap;
         double O2_CAP = safety.o2PatchAbsCap;
         double ADD_CAP = safety.addPatchAbsCap;
         double prod = 1.0;

         for (AttributeModifier m : o2Mods) {
            double f = 1.0 + m.getAmount();
            prod *= f;
            if (!Double.isFinite(prod) || Math.abs(prod) > PROD_CAP) {
               break;
            }
         }

         double target = prod;
         if (negToZero && target < 0.0) {
            target = 0.0;
         }

         target = clamp(target, o2Min, o2Max);
         removeByUUID(inst, myO2);
         boolean degenerateProd = !Double.isFinite(prod) || Math.abs(prod) < EPS;
         double patch;
         if (target != 0.0 && !degenerateProd) {
            double raw = target / prod - 1.0;
            if (!Double.isFinite(raw)) {
               patch = Math.copySign(O2_CAP, raw);
            } else if (raw < -0.99) {
               patch = -0.99;
            } else if (Math.abs(raw) > O2_CAP) {
               patch = Math.copySign(O2_CAP, raw);
            } else {
               patch = raw;
            }
         } else {
            patch = -1.0;
         }

         if (patch != 0.0) {
            AttributeModifier m = new AttributeModifier(myO2, "IgaO2Guard", patch, Operation.MULTIPLY_SCALAR_1);
            inst.addModifier(m);
            if (this.settings.debug) {
               this.logDebug(e, attr, "O2 patch (safe) applied: patch=" + patch + " prod=" + prod + " target=" + target);
            }
         }

         mods = new ArrayList<>(inst.getModifiers());
         addMods = filter(mods, Operation.ADD_NUMBER, null, null);
         o1Mods = filter(mods, Operation.ADD_SCALAR, null, null);
         o2Mods = filter(mods, Operation.MULTIPLY_SCALAR_1, null, null);
         double base = inst.getBaseValue();
         double v0 = base + sumAmount(addMods);
         double v1 = v0 + base * sumAmount(o1Mods);
         double v2 = v1 * productFactor(o2Mods);
         if (!Double.isFinite(v2)) {
            removeByUUID(inst, myO2);
            removeByUUID(inst, myAdd);
            AttributeModifier kill = new AttributeModifier(myO2, "IgaO2Guard", -1.0, Operation.MULTIPLY_SCALAR_1);
            inst.addModifier(kill);
            if (this.settings.debug) {
               this.logDebug(e, attr, "v2 non-finite -> forcing O2=0 with patch=-1.0");
            }
         } else {
            double clamped = clamp(v2, finalMin, finalMax);
            double needAdd = clamped - v2;
            removeByUUID(inst, myAdd);
            if (Double.isFinite(needAdd)) {
               if (Math.abs(needAdd) > ADD_CAP) {
                  needAdd = Math.copySign(ADD_CAP, needAdd);
               }

               if (Math.abs(needAdd) > 1.0E-9) {
                  AttributeModifier patchAdd = new AttributeModifier(myAdd, "IgaClamp", needAdd, Operation.ADD_NUMBER);
                  inst.addModifier(patchAdd);
                  if (this.settings.debug) {
                     this.logDebug(e, attr, String.format("Final clamp: v2=%.6f -> %.6f (add %.6f)", v2, clamped, needAdd));
                  }
               } else if (this.settings.debug) {
                  this.logDebug(e, attr, String.format("Final ok: v2=%.6f (no clamp)", v2));
               }
            } else if (this.settings.debug) {
               this.logDebug(e, attr, "needAdd non-finite; skipped ADD_NUMBER patch");
            }
         }
      }
   }

   private static List<AttributeModifier> filter(Collection<AttributeModifier> src, Operation op, UUID exclude1, UUID exclude2) {
      return src.stream()
         .filter(m -> m.getOperation() == op)
         .filter(m -> exclude1 == null || !m.getUniqueId().equals(exclude1))
         .filter(m -> exclude2 == null || !m.getUniqueId().equals(exclude2))
         .collect(Collectors.toList());
   }

   private static void removeByUUID(AttributeInstance inst, UUID uuid) {
      for (AttributeModifier m : inst.getModifiers().stream().filter(mx -> mx.getUniqueId().equals(uuid)).collect(Collectors.toList())) {
         inst.removeModifier(m);
      }
   }

   private static double sumAmount(Collection<AttributeModifier> mods) {
      double s = 1.0;

      for (AttributeModifier m : mods) {
         s += m.getAmount();
      }

      return s;
   }

   private static double productFactor(Collection<AttributeModifier> mods) {
      double p = 1.0;

      for (AttributeModifier m : mods) {
         double f = 1.0 + m.getAmount();
         p *= f;
         if (!Double.isFinite(p)) {
            return p;
         }
      }

      return p;
   }

   private static double clamp(double v, double min, double max) {
      return Math.max(min, Math.min(max, v));
   }

   private void log(String s) {
      IgaDebugLogger.log(this.getClass(), s);
   }

   private void logDebug(LivingEntity e, Attribute attr, String msg) {
      IgaDebugLogger.log(this.getClass(), "[Debug][" + e.getType().name() + "@" + e.getUniqueId() + "][" + attr.name() + "] " + msg);
   }
}
