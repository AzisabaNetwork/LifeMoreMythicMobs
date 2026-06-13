package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.IParentSkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Locale;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ProjectileOrientationStoreMechanic extends SkillMechanic implements INoTargetSkill {
   private final String yawVarPS;
   private final String pitchVarPS;
   private final boolean normalizeYaw;
   private final int precision;
   private final boolean once;
   private final PlaceholderString onceKeyPS;
   private final double minSpeed;
   private static final String ONCE_META_KEY = "iga.projectileorientationStore.once";
   private final boolean log;

   public ProjectileOrientationStoreMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.yawVarPS = config.getString(new String[]{"yawVar", "yaw"}, "", new String[0]);
      this.pitchVarPS = config.getString(new String[]{"pitchVar", "pitch"}, "", new String[0]);
      this.normalizeYaw = config.getBoolean(new String[]{"normalizeYaw"}, config.getBoolean("normalize", false));
      this.precision = Math.max(0, config.getInteger(new String[]{"precision"}, 2));
      this.once = config.getBoolean(new String[]{"once"}, false);
      this.onceKeyPS = PlaceholderString.of(config.getString(new String[]{"onceKey"}, "", new String[0]));
      this.minSpeed = Math.max(0.0, config.getDouble(new String[]{"minSpeed"}, 0.01));
      this.log = config.getBoolean(new String[]{"log"}, false);
   }

   public SkillResult cast(SkillMetadata data) {
      if (this.once) {
         String guardKey = this.buildGuardKey(data);
         Entity casterBukkit = this.getCasterBukkit(data);
         if (casterBukkit != null && casterBukkit.hasMetadata(guardKey)) {
            this.logf("once: already marked key='%s' -> skip (true)", guardKey);
            return SkillResult.SUCCESS;
         }
      }

      AbstractLocation loc = data.getOrigin();
      if (loc == null) {
         this.logf("cast: origin is null -> abort");
         return SkillResult.FAILURE;
      }

      Double[] dir = this.yawPitchFromVelocity(data);
      if (dir == null) {
         this.logf("velocity required but unavailable -> return false");
         return SkillResult.FAILURE;
      }

      double yaw = dir[0];
      double pitch = dir[1];
      this.logf("resolved origin: rawYaw=%.3f, rawPitch=%.3f", yaw, pitch);
      if (this.normalizeYaw) {
         double before = yaw;
         yaw %= 360.0;
         if (yaw < 0.0) {
            yaw += 360.0;
         }

         this.logf("normalizeYaw: in=%.3f -> out=%.3f", before, yaw);
      }

      String fmt = "%." + this.precision + "f";
      String yawStr = String.format(Locale.ROOT, fmt, yaw);
      String pitchStr = String.format(Locale.ROOT, fmt, pitch);
      this.logf("format: precision=%d, yawStr=%s, pitchStr=%s", this.precision, yawStr, pitchStr);
      boolean ok = false;
      this.logf("var names: yawVar='%s', pitchVar='%s'", this.yawVarPS, this.pitchVarPS);
      if (this.yawVarPS != null && !this.yawVarPS.isEmpty()) {
         boolean r = VariableUtil.setScopedVariable(this.yawVarPS, yawStr, data, data.getTrigger());
         ok |= r;
         this.logf("save yaw: name='%s', value='%s', result=%s", this.yawVarPS, yawStr, String.valueOf(r));
      } else {
         this.logf("skip yaw: yawVar empty");
      }

      if (this.pitchVarPS != null && !this.pitchVarPS.isEmpty()) {
         boolean r = VariableUtil.setScopedVariable(this.pitchVarPS, pitchStr, data, data.getTrigger());
         ok |= r;
         this.logf("save pitch: name='%s', value='%s', result=%s", this.pitchVarPS, pitchStr, String.valueOf(r));
      } else {
         this.logf("skip pitch: pitchVar empty");
      }

      if (this.once && ok) {
         String guardKey = this.buildGuardKey(data);
         Entity casterBukkit = this.getCasterBukkit(data);
         if (casterBukkit != null) {
            casterBukkit.setMetadata(guardKey, new FixedMetadataValue(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), Boolean.TRUE));
            this.logf("once: mark key='%s' on caster=%s", guardKey, casterBukkit.getUniqueId());
         }
      }

      this.logf("cast done: ok=%s", String.valueOf(ok));
      return ok;
   }

   private void logf(String fmt, Object... args) {
      if (this.log) {
         IgaDebugLogger.log(this.getClass(), String.format(Locale.ROOT, fmt, args));
      }
   }

   private Entity getCasterBukkit(SkillMetadata data) {
      try {
         if (data.getCaster() != null && data.getCaster().getEntity() != null) {
            return data.getCaster().getEntity().getBukkitEntity();
         }
      } catch (Throwable var3) {
      }

      return null;
   }

   private Double[] yawPitchFromVelocity(SkillMetadata data) {
      AbstractEntity trig = data.getTrigger();
      if (trig != null && trig.getBukkitEntity() != null) {
         Vector vel = trig.getBukkitEntity().getVelocity();
         if (vel == null) {
            this.logf("velocity: null");
            return null;
         } else {
            double speed = vel.length();
            this.logf("velocity: v=%s, |v|=%.5f (minSpeed=%.5f)", vel.toString(), speed, this.minSpeed);
            if (speed < this.minSpeed) {
               this.logf("velocity: below minSpeed -> fail");
               return null;
            } else {
               double yaw = Math.toDegrees(Math.atan2(-vel.getX(), vel.getZ()));
               double pitch = Math.toDegrees(-Math.atan2(vel.getY(), Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ())));
               this.logf("velocity->yaw/pitch: %.3f / %.3f", yaw, pitch);
               return new Double[]{yaw, pitch};
            }
         }
      } else {
         this.logf("velocity: trigger entity not available");
         return null;
      }
   }

   private String buildGuardKey(SkillMetadata data) {
      String key = "iga.projectileorientationStore.once";

      try {
         String userKey = this.onceKeyPS != null ? this.onceKeyPS.get(data, data.getCaster() != null ? data.getCaster().getEntity() : null) : "";
         if (userKey != null && !userKey.isEmpty()) {
            key = key + ":user" + userKey;
            this.logf("buildGuardKey (userKey): %s", key);
            return key;
         }
      } catch (Throwable var5) {
      }

      try {
         IParentSkill caller = data.getCallingEvent();
         if (caller != null) {
            key = key + ":call=" + System.identityHashCode(caller);
            this.logf("buildGuardKey (caller): %s", key);
            return key;
         }
      } catch (Throwable var4) {
      }

      try {
         if (data.getTrigger() != null && data.getTrigger().getBukkitEntity() != null) {
            key = key + ":trg=" + data.getTrigger().getBukkitEntity().getUniqueId().toString();
         } else {
            key = key + ":trg=null";
         }
      } catch (Throwable ignored) {
         key = key + ":trg=err";
      }

      this.logf("buildGuardKey (fallback): %s", key);
      return key;
   }
}
