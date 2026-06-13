package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class RectParticleWallMechanic extends SkillMechanic implements ITargetedLocationSkill, INoTargetSkill {
   private final PlaceholderString cornerRaw;
   private final PlaceholderString worldRaw;
   private final PlaceholderString uVecRaw;
   private final boolean uYawRandomize;
   private final PlaceholderDouble uYawMinRaw;
   private final PlaceholderDouble uYawMaxRaw;
   private final PlaceholderString uYawBaseRaw;
   private final PlaceholderString vVecRaw;
   private final PlaceholderDouble uLenRaw;
   private final PlaceholderDouble vLenRaw;
   private final PlaceholderDouble stepURaw;
   private final PlaceholderDouble stepVRaw;
   private final boolean borderOnly;
   private final PlaceholderString particleRaw;
   private final PlaceholderDouble countRaw;
   private final PlaceholderDouble speedRaw;
   private final PlaceholderDouble rRaw;
   private final PlaceholderDouble gRaw;
   private final PlaceholderDouble bRaw;
   private final PlaceholderDouble sizeRaw;
   private final PlaceholderDouble durationRaw;
   private final PlaceholderDouble intervalRaw;
   private final PlaceholderDouble jitterRaw;
   private final boolean toCasterOnly;
   private final Random random = new Random();
   private final PlaceholderString onHitSkillRaw;
   private final PlaceholderDouble hitIntervalRaw;
   private final PlaceholderDouble hitThicknessRaw;
   private final PlaceholderDouble hitRadiusRaw;
   private final PlaceholderString hitFilterRaw;
   private final boolean includeCaster;
   private final String hitConditionString;
   private List<SkillCondition> hitConditions;
   private final Map<UUID, Long> lastHitAtMs = new ConcurrentHashMap<>();

   public RectParticleWallMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.cornerRaw = PlaceholderString.of(config.getString(new String[]{"corner"}, null, new String[0]));
      this.worldRaw = PlaceholderString.of(config.getString(new String[]{"world"}, null, new String[0]));
      this.uVecRaw = PlaceholderString.of(config.getString(new String[]{"uVec"}, "1,0,0", new String[0]));
      this.uYawRandomize = config.getBoolean(new String[]{"uYawRandom", "uRandYaw"}, false);
      this.uYawMinRaw = PlaceholderDouble.of(config.getString(new String[]{"uYawMin"}, "0", new String[0]));
      this.uYawMaxRaw = PlaceholderDouble.of(config.getString(new String[]{"uYawMax"}, "180", new String[0]));
      this.uYawBaseRaw = PlaceholderString.of(config.getString(new String[]{"uYawBase"}, "world", new String[0]));
      this.vVecRaw = PlaceholderString.of(config.getString(new String[]{"vVec"}, "0,1,0", new String[0]));
      this.uLenRaw = PlaceholderDouble.of(config.getString(new String[]{"uLen"}, "5", new String[0]));
      this.vLenRaw = PlaceholderDouble.of(config.getString(new String[]{"vLen"}, "3", new String[0]));
      this.stepURaw = PlaceholderDouble.of(config.getString(new String[]{"stepU"}, "1.0", new String[0]));
      this.stepVRaw = PlaceholderDouble.of(config.getString(new String[]{"stepV"}, "1.0", new String[0]));
      this.borderOnly = config.getBoolean("borderOnly", false);
      this.particleRaw = PlaceholderString.of(config.getString(new String[]{"particle"}, "REDSTONE", new String[0]));
      this.countRaw = PlaceholderDouble.of(config.getString(new String[]{"count"}, "1", new String[0]));
      this.speedRaw = PlaceholderDouble.of(config.getString(new String[]{"speed"}, "0", new String[0]));
      this.rRaw = PlaceholderDouble.of(config.getString(new String[]{"r"}, "255", new String[0]));
      this.gRaw = PlaceholderDouble.of(config.getString(new String[]{"g"}, "255", new String[0]));
      this.bRaw = PlaceholderDouble.of(config.getString(new String[]{"b"}, "255", new String[0]));
      this.sizeRaw = PlaceholderDouble.of(config.getString(new String[]{"size"}, "1.0", new String[0]));
      this.durationRaw = PlaceholderDouble.of(config.getString(new String[]{"duration"}, "0", new String[0]));
      this.intervalRaw = PlaceholderDouble.of(config.getString(new String[]{"interval"}, "1", new String[0]));
      this.jitterRaw = PlaceholderDouble.of(config.getString(new String[]{"jitter"}, "0", new String[0]));
      this.toCasterOnly = config.getBoolean("toCasterOnly", false);
      this.onHitSkillRaw = PlaceholderString.of(config.getString(new String[]{"onHitSkill"}, null, new String[0]));
      this.hitIntervalRaw = PlaceholderDouble.of(config.getString(new String[]{"hitInterval"}, "20", new String[0]));
      this.hitThicknessRaw = PlaceholderDouble.of(config.getString(new String[]{"hitThickness"}, "0.6", new String[0]));
      this.hitRadiusRaw = PlaceholderDouble.of(config.getString(new String[]{"hitRadius"}, "0.3", new String[0]));
      this.hitFilterRaw = PlaceholderString.of(config.getString(new String[]{"hitFilter"}, "PLAYERS", new String[0]));
      this.includeCaster = config.getBoolean("includeCaster", false);
      this.hitConditionString = config.getString(new String[]{"hitconditions", "conditions", "cond", "c", "hc", "oc"}, null, new String[0]);
      if (this.hitConditionString != null) {
         this.hitConditions = MythicBukkit.inst().getSkillManager().getConditions(this.hitConditionString);
      }
   }

   public boolean castAtLocation(SkillMetadata data, AbstractLocation loc) {
      Location corner = BukkitAdapter.adapt(loc);
      return this.doCast(data, corner);
   }

   public SkillResult cast(SkillMetadata data) {
      AbstractEntity caster = data.getCaster().getEntity();
      Location corner = BukkitAdapter.adapt(caster.getLocation());
      return this.doCast(data, corner);
   }

   private boolean doCast(final SkillMetadata data, Location defaultCorner) {
      try {
         final Location corner = this.resolveCorner(data, defaultCorner);
         if (corner != null && corner.getWorld() != null) {
            Vector u = this.parseVector(data, this.uVecRaw, new Vector(1, 0, 0));
            Vector v = this.parseVector(data, this.vVecRaw, new Vector(0, 1, 0));
            if (this.uYawRandomize) {
               String base = (this.uYawBaseRaw != null ? this.uYawBaseRaw.get(data) : "world").toLowerCase(Locale.ROOT);
               double baseYawRad = 0.0;
               AbstractEntity casterAE = data.getCaster().getEntity();
               if ("look".equals(base) && casterAE != null) {
                  Vector fwd = BukkitAdapter.adapt(casterAE.getLocation().getDirection());
                  fwd.setY(0);
                  if (fwd.lengthSquared() > 0.0) {
                     fwd.normalize();
                  }

                  baseYawRad = Math.atan2(fwd.getZ(), fwd.getX());
               } else if ("caster_to_target".equals(base) && casterAE != null && defaultCorner != null) {
                  Vector dir = defaultCorner.toVector().subtract(BukkitAdapter.adapt(casterAE.getLocation()).toVector());
                  dir.setY(0);
                  if (dir.lengthSquared() > 0.0) {
                     dir.normalize();
                  }

                  baseYawRad = Math.atan2(dir.getZ(), dir.getX());
               }

               double minDeg = this.uYawMinRaw.get(data);
               double maxDeg = this.uYawMaxRaw.get(data);
               if (maxDeg < minDeg) {
                  double t = minDeg;
                  minDeg = maxDeg;
                  maxDeg = t;
               }

               double deltaRad = Math.toRadians(minDeg + this.random.nextDouble() * (maxDeg - minDeg));
               double yaw = baseYawRad + deltaRad;
               u = new Vector(Math.cos(yaw), 0.0, Math.sin(yaw));
               v = new Vector(0, 1, 0);
            }

            u.normalize();
            v.normalize();
            if (u.clone().crossProduct(v).lengthSquared() < 1.0E-6) {
               IgaDebugLogger.log(this.getClass(), "uVecとvVecが並行/ゼロです。面を生成できません。");
               return SkillResult.FAILURE;
            }

            final double uLen = Math.max(0.0, this.uLenRaw.get(data));
            final double vLen = Math.max(0.0, this.vLenRaw.get(data));
            final double stepU = Math.max(0.05, this.stepURaw.get(data));
            final double stepV = Math.max(0.05, this.stepVRaw.get(data));
            final double jitter = Math.max(0.0, this.jitterRaw.get(data));
            final String onHitSkillName = this.onHitSkillRaw != null ? this.onHitSkillRaw.get(data) : null;
            final Skill onHitSkill = this.getSkill(onHitSkillName).orElse(null);
            final boolean hitEnabled = onHitSkill != null;
            final long hitIntervalMs = (long)Math.max(0.0, Math.floor(this.hitIntervalRaw.get(data) * 50.0));
            final double hitThickness = Math.max(0.0, this.hitThicknessRaw.get(data));
            final double hitRadius = Math.max(0.0, this.hitRadiusRaw.get(data));
            final String hitFilter = (this.hitFilterRaw != null ? this.hitFilterRaw.get(data) : "PLAYERS").toUpperCase(Locale.ROOT);
            AbstractEntity ae = data.getCaster().getEntity();
            final UUID casterUUID = ae != null && ae.getBukkitEntity() != null ? ae.getBukkitEntity().getUniqueId() : null;
            final Vector uNorm = u.clone().normalize();
            final Vector vNorm = v.clone().normalize();
            final Vector nNorm = uNorm.clone().crossProduct(vNorm).normalize();
            String particleName = this.particleRaw.get(data);
            final Particle particle = safeParticle(particleName);
            final int count = Math.max(1, (int)Math.round(this.countRaw.get(data)));
            final double speed = this.speedRaw.get(data);
            final DustOptions dust = particle == Particle.REDSTONE
               ? new DustOptions(
                  Color.fromRGB(this.clampColor(this.rRaw.get(data)), this.clampColor(this.gRaw.get(data)), this.clampColor(this.bRaw.get(data))),
                  (float)Math.max(0.01, this.sizeRaw.get(data))
               )
               : null;
            long duration = (long)Math.max(0.0, Math.floor(this.durationRaw.get(data)));
            long interval = Math.max(1L, (long)Math.floor(this.intervalRaw.get(data)));
            if (duration <= 0L) {
               this.drawOnce(data, corner, u, v, uLen, vLen, stepU, stepV, jitter, particle, count, speed, dust);
               if (hitEnabled) {
                  this.detectHits(
                     data, corner, uNorm, vNorm, nNorm, uLen, vLen, hitThickness, hitRadius, hitFilter, onHitSkill, onHitSkillName, hitIntervalMs, casterUUID
                  );
               }
            } else {
               final long repeats = Math.max(1L, duration / interval);
               Plugin plugin = JavaPlugin.getPlugin(LifeMoreMythicMobs.class);
               final Vector uF = u.clone();
               final Vector vF = v.clone();
               (new BukkitRunnable() {
                     long n = 0L;

                     public void run() {
                        if (this.n++ >= repeats) {
                           this.cancel();
                        } else {
                           RectParticleWallMechanic.this.drawOnce(data, corner, uF, vF, uLen, vLen, stepU, stepV, jitter, particle, count, speed, dust);
                           if (hitEnabled) {
                              RectParticleWallMechanic.this.detectHits(
                                 data,
                                 corner,
                                 uNorm,
                                 vNorm,
                                 nNorm,
                                 uLen,
                                 vLen,
                                 hitThickness,
                                 hitRadius,
                                 hitFilter,
                                 onHitSkill,
                                 onHitSkillName,
                                 hitIntervalMs,
                                 casterUUID
                              );
                           }
                        }
                     }
                  })
                  .runTaskTimer(plugin, 0L, interval);
            }

            return SkillResult.SUCCESS;
         } else {
            IgaDebugLogger.log(this.getClass(), "corner/worldが解決できませんでした");
            return SkillResult.FAILURE;
         }
      } catch (Throwable t) {
         IgaDebugLogger.log(this.getClass(), "例外: " + t.getClass().getSimpleName() + " " + t.getMessage());
         return SkillResult.FAILURE;
      }
   }

   private void drawOnce(
      SkillMetadata data,
      Location corner,
      Vector u,
      Vector v,
      double uLen,
      double vLen,
      double stepU,
      double stepV,
      double jitter,
      Particle particle,
      int count,
      double speed,
      DustOptions dust
   ) {
      World w = corner.getWorld();
      if (w != null) {
         int uSteps = Math.max(1, (int)Math.floor(uLen / stepU)) + 1;
         int vSteps = Math.max(1, (int)Math.floor(vLen / stepV)) + 1;
         AbstractEntity ae = data.getCaster().getEntity();
         Player only = this.toCasterOnly && ae != null && ae.isPlayer() ? (Player)ae.getBukkitEntity() : null;

         for (int i = 0; i < uSteps; i++) {
            for (int j = 0; j < vSteps; j++) {
               boolean onBorder = i == 0 || j == 0 || i == uSteps - 1 || j == vSteps - 1;
               if (!this.borderOnly || onBorder) {
                  double du = Math.min(i * stepU, uLen);
                  double dv = Math.min(j * stepV, vLen);
                  Vector offset = u.clone().multiply(du).add(v.clone().multiply(dv));
                  if (jitter > 0.0) {
                     double ju = (this.random.nextDouble() * 2.0 - 1.0) * jitter;
                     double jv = (this.random.nextDouble() * 2.0 - 1.0) * jitter;
                     offset.add(u.clone().multiply(ju)).add(v.clone().multiply(jv));
                  }

                  Location p = corner.clone().add(offset);
                  if (only != null) {
                     if (dust != null) {
                        only.spawnParticle(Particle.REDSTONE, p, count, 0.0, 0.0, 0.0, speed, dust);
                     } else {
                        only.spawnParticle(particle, p, count, 0.0, 0.0, 0.0, speed);
                     }
                  } else if (dust != null) {
                     w.spawnParticle(Particle.REDSTONE, p, count, 0.0, 0.0, 0.0, speed, dust);
                  } else {
                     w.spawnParticle(particle, p, count, 0.0, 0.0, 0.0, speed);
                  }
               }
            }
         }
      }
   }

   private Location resolveCorner(SkillMetadata data, Location fallback) {
      World world = fallback != null ? fallback.getWorld() : null;
      if (this.worldRaw != null) {
         String worldName = this.worldRaw.get(data);
         if (worldName != null && !worldName.isEmpty()) {
            World w = Bukkit.getWorld(worldName);
            if (w != null) {
               world = w;
            }
         }
      }

      if (world == null) {
         AbstractEntity ae = data.getCaster().getEntity();
         if (ae != null) {
            world = BukkitAdapter.adapt(ae.getLocation()).getWorld();
         }
      }

      if (world == null) {
         return null;
      }

      if (this.cornerRaw != null) {
         String s = this.cornerRaw.get(data);
         Vector vec = this.parseVectorStr(s, null);
         if (vec != null) {
            return new Location(world, vec.getX(), vec.getY(), vec.getZ());
         }
      }

      return fallback != null ? fallback : new Location(world, 0.0, 0.0, 0.0);
   }

   private Vector parseVector(SkillMetadata data, PlaceholderString raw, Vector def) {
      String s = raw != null ? raw.get(data) : null;
      Vector v = this.parseVectorStr(s, def);
      return v != null ? v : def;
   }

   private Vector parseVectorStr(String s, Vector def) {
      if (s == null) {
         return def;
      }

      String[] sp = s.split("\\s*,\\s*");
      if (sp.length < 3) {
         return def;
      }

      try {
         double x = Double.parseDouble(sp[0]);
         double y = Double.parseDouble(sp[1]);
         double z = Double.parseDouble(sp[2]);
         return new Vector(x, y, z);
      } catch (NumberFormatException e) {
         return def;
      }
   }

   private int clampColor(double v) {
      int x = (int)Math.round(v);
      return Math.max(0, Math.min(255, x));
   }

   private static Particle safeParticle(String name) {
      try {
         return Particle.valueOf(name.toUpperCase(Locale.ROOT).trim());
      } catch (IllegalArgumentException ex) {
         IgaDebugLogger.log(RectParticleWallMechanic.class, "未知のパーティクル: " + name + " -> REDSTONEにフォールバック");
         return Particle.REDSTONE;
      }
   }

   private void detectHits(
      SkillMetadata data,
      Location corner,
      Vector uHat,
      Vector vHat,
      Vector nHat,
      double uLen,
      double vLen,
      double hitThickness,
      double hitRadius,
      String hitFilter,
      Skill onHitSkill,
      String onHitSkillName,
      long hitIntervalMs,
      UUID casterUUID
   ) {
      World w = corner.getWorld();
      if (w != null) {
         Vector[] pts = new Vector[]{
            corner.toVector(),
            corner.clone().add(uHat.clone().multiply(uLen)).toVector(),
            corner.clone().add(vHat.clone().multiply(vLen)).toVector(),
            corner.clone().add(uHat.clone().multiply(uLen)).add(vHat.clone().multiply(vLen)).toVector()
         };
         double minX = Double.POSITIVE_INFINITY;
         double minY = Double.POSITIVE_INFINITY;
         double minZ = Double.POSITIVE_INFINITY;
         double maxX = Double.NEGATIVE_INFINITY;
         double maxY = Double.NEGATIVE_INFINITY;
         double maxZ = Double.NEGATIVE_INFINITY;
         Vector[] var37 = pts;
         int cx = pts.length;

         for (int var35 = 0; var35 < cx; var35++) {
            Vector v = var37[var35];
            minX = Math.min(minX, v.getX());
            minY = Math.min(minY, v.getY());
            minZ = Math.min(minZ, v.getZ());
            maxX = Math.max(maxX, v.getX());
            maxY = Math.max(maxY, v.getY());
            maxZ = Math.max(maxZ, v.getZ());
         }

         double expand = hitThickness * 0.5 + hitRadius + 0.5;
         minX -= expand;
         minY -= expand;
         minZ -= expand;
         maxX += expand;
         maxY += expand;
         maxZ += expand;
         double cxx = (minX + maxX) * 0.5;
         double cy = (minY + maxY) * 0.5;
         double cz = (minZ + maxZ) * 0.5;
         double rx = (maxX - minX) * 0.5;
         double ry = (maxY - minY) * 0.5;
         double rz = (maxZ - minZ) * 0.5;
         Location center = new Location(w, cxx, cy, cz);

         for (Entity e : w.getNearbyEntities(center, rx, ry, rz)) {
            if (this.includeCaster || casterUUID == null || !e.getUniqueId().equals(casterUUID)) {
               String rp = hitFilter;
               switch (hitFilter.hashCode()) {
                  case -2049100119:
                     if (rp.equals("LIVING") && !(e instanceof LivingEntity)) {
                        continue;
                     }
                     break;
                  case 64897:
                     if (!rp.equals("ALL")) {
                     }
                     break;
                  case 224415122:
                     if (rp.equals("PLAYERS") && !(e instanceof Player)) {
                        continue;
                     }
               }

               Vector rp = e.getLocation().toVector().subtract(corner.toVector());
               double du = rp.dot(uHat);
               double dv = rp.dot(vHat);
               double dn = rp.dot(nHat);
               double halfT = hitThickness * 0.5;
               boolean inside = Math.abs(dn) <= halfT + hitRadius && du >= -hitRadius && du <= uLen + hitRadius && dv >= -hitRadius && dv <= vLen + hitRadius;
               if (inside) {
                  AbstractEntity casterAE = data.getCaster().getEntity();
                  AbstractEntity targetAE = BukkitAdapter.adapt(e);
                  if (this.entityPassesConditions(casterAE, targetAE)) {
                     long now = System.currentTimeMillis();
                     Long last = this.lastHitAtMs.get(e.getUniqueId());
                     if (last == null || now - last >= hitIntervalMs) {
                        if (onHitSkill != null) {
                           SkillMetadata clone = data.deepClone();
                           clone.setEntityTargets(Collections.singleton(targetAE));
                           clone.setOrigin(targetAE.getLocation());

                           try {
                              onHitSkill.execute(clone);
                           } catch (Throwable ex) {
                              IgaDebugLogger.log(this.getClass(), "onHitSkill exception: " + ex.getClass().getSimpleName() + " " + ex.getMessage());
                           }
                        }

                        this.lastHitAtMs.put(e.getUniqueId(), now);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean entityPassesConditions(AbstractEntity caster, AbstractEntity entity) {
      if (this.hitConditions != null) {
         for (SkillCondition cond : this.hitConditions) {
            if (!cond.evaluateToEntity(caster, entity)) {
               return SkillResult.FAILURE;
            }
         }
      }

      return SkillResult.SUCCESS;
   }

   private Optional<Skill> getSkill(String name) {
      return name != null && !name.isEmpty() ? MythicBukkit.inst().getSkillManager().getSkill(name) : Optional.empty();
   }
}
