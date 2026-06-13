package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.IParentSkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.mechanics.MissileMechanic.MissileTracker;
import io.lumine.mythic.api.skills.mechanics.ProjectileMechanic.ProjectileMechanicTracker;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.projectiles.Projectile.ProjectileTracker;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectileVelocityMechanic extends SkillMechanic implements INoTargetSkill {
   private static final Logger log = LoggerFactory.getLogger(ProjectileVelocityMechanic.class);
   private final PlaceholderDouble vx;
   private final PlaceholderDouble vy;
   private final PlaceholderDouble vz;
   private final boolean relative;
   private final ProjectileVelocityMechanic.VelocityMode mode;

   public ProjectileVelocityMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.vx = PlaceholderDouble.of(config.getString(new String[]{"velocityx", "vx", "x"}, "1.0", new String[0]));
      this.vy = PlaceholderDouble.of(config.getString(new String[]{"velocityy", "vy", "y"}, "1.0", new String[0]));
      this.vz = PlaceholderDouble.of(config.getString(new String[]{"velocityz", "vz", "z"}, "1.0", new String[0]));
      this.relative = config.getBoolean(new String[]{"relative", "r"}, true);
      String m = config.getString(new String[]{"mode", "m"}, "SET", new String[0]);

      ProjectileVelocityMechanic.VelocityMode parsed;
      try {
         parsed = ProjectileVelocityMechanic.VelocityMode.valueOf(m.toUpperCase());
      } catch (IllegalArgumentException ex) {
         parsed = ProjectileVelocityMechanic.VelocityMode.SET;
      }

      this.mode = parsed;
   }

   public SkillResult cast(SkillMetadata data) {
      IParentSkill calling = data.getCallingEvent();
      if (calling instanceof ProjectileTracker) {
         this.applyVelocityChange(data, (ProjectileTracker)calling);
      } else if (calling instanceof MissileTracker) {
         this.applyVelocityChange(data, (ProjectileTracker)calling);
      } else if (calling instanceof ProjectileMechanicTracker) {
         this.applyVelocityChange(data, (ProjectileTracker)calling);
      }

      return SkillResult.SUCCESS;
   }

   private void applyVelocityChange(SkillMetadata data, ProjectileTracker tracker) {
      AbstractVector cur = cloneVec(this.getCurrentVelocity(tracker));
      if (cur != null) {
         double cx = cur.getX();
         double cy = cur.getY();
         double cz = cur.getZ();
         double dvx = safe(this.vx.get(data));
         double dvy = safe(this.vy.get(data));
         double dvz = safe(this.vz.get(data));
         if (this.relative) {
            ProjectileVelocityMechanic.Basis b = this.makeBasis(cur);
            double lx = dot(cur, b.right);
            double ly = dot(cur, b.up);
            double lz = dot(cur, b.forward);
            switch (this.mode) {
               case SET:
                  lx = dvx;
                  ly = dvy;
                  lz = dvz;
                  break;
               case ADD:
                  lx += dvx;
                  ly += dvy;
                  lz += dvz;
                  break;
               case MULTIPLY:
                  lx *= dvx;
                  ly *= dvy;
                  lz *= dvz;
                  break;
               case REMOVE:
                  lx -= dvx;
                  ly -= dvy;
                  lz -= dvz;
                  break;
               case DIVIDE:
                  lx = dvx != 0.0 ? lx / dvx : lx;
                  ly = dvy != 0.0 ? ly / dvy : ly;
                  lz = dvz != 0.0 ? lz / dvz : lz;
            }

            AbstractVector nw = add(add(scale(b.right, lx), scale(b.up, ly)), scale(b.forward, lz));
            sanitize(nw);
            this.setCurrentVelocity(tracker, nw);
         } else {
            double nx = cx;
            double ny = cy;
            double nz = cz;
            switch (this.mode) {
               case SET:
                  nx = dvx;
                  ny = dvy;
                  nz = dvz;
                  break;
               case ADD:
                  nx += dvx;
                  ny += dvy;
                  nz += dvz;
                  break;
               case MULTIPLY:
                  nx *= dvx;
                  ny *= dvy;
                  nz *= dvz;
                  break;
               case REMOVE:
                  nx -= dvx;
                  ny -= dvy;
                  nz -= dvz;
                  break;
               case DIVIDE:
                  if (dvx != 0.0) {
                     double var10000 = ny / dvx;
                  }

                  if (dvy != 0.0) {
                     double var25 = ny / dvy;
                  }

                  nx = dvz != 0.0 ? ny / dvz : nz;
            }

            AbstractVector nw = vec(nx, ny, nz);
            sanitize(nw);
            this.setCurrentVelocity(tracker, nw);
         }
      }
   }

   private ProjectileVelocityMechanic.Basis makeBasis(AbstractVector forwardRaw) {
      AbstractVector f = normalize(forwardRaw);
      if (length(f) < 1.0E-6) {
         f = vec(0.0, 0.0, 1.0);
      }

      AbstractVector worldUp = vec(0.0, 1.0, 0.0);
      AbstractVector r = cross(f, worldUp);
      if (length(r) < 1.0E-6) {
         worldUp = vec(0.0, 0.0, 1.0);
         r = cross(f, worldUp);
      }

      r = normalize(r);
      AbstractVector u = normalize(cross(r, f));
      return new ProjectileVelocityMechanic.Basis(r, u, f);
   }

   private static AbstractVector vec(double x, double y, double z) {
      return new AbstractVector(x, y, z);
   }

   private static AbstractVector cloneVec(AbstractVector v) {
      return v == null ? null : vec(v.getX(), v.getY(), v.getZ());
   }

   private static double length(AbstractVector v) {
      return Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY() + v.getZ() * v.getZ());
   }

   private static AbstractVector normalize(AbstractVector v) {
      double len = length(v);
      return len > 1.0E-9 ? vec(v.getX() / len, v.getY() / len, v.getZ() / len) : vec(0.0, 0.0, 0.0);
   }

   private static AbstractVector add(AbstractVector a, AbstractVector b) {
      return vec(a.getX() + b.getX(), a.getY() + b.getY(), a.getZ() + b.getZ());
   }

   private static AbstractVector scale(AbstractVector a, double s) {
      return vec(a.getX() * s, a.getY() * s, a.getZ() * s);
   }

   private static AbstractVector cross(AbstractVector a, AbstractVector b) {
      return vec(a.getY() * b.getZ() - a.getZ() * b.getY(), a.getZ() * b.getX() - a.getX() * b.getZ(), a.getX() * b.getY() - a.getY() * b.getX());
   }

   private static double dot(AbstractVector a, AbstractVector b) {
      return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
   }

   private static double safe(double d) {
      return Double.isFinite(d) ? d : 0.0;
   }

   private static void sanitize(AbstractVector v) {
      if (!Double.isFinite(v.getX())) {
         v.setX(0);
      }

      if (!Double.isFinite(v.getY())) {
         v.setY(0);
      }

      if (!Double.isFinite(v.getZ())) {
         v.setZ(0);
      }
   }

   private AbstractVector getCurrentVelocity(ProjectileTracker tracker) {
      try {
         Method m = tracker.getClass().getMethod("getCurrentVelocity");
         Object ret = m.invoke(tracker);
         if (ret instanceof AbstractVector) {
            return (AbstractVector)ret;
         }
      } catch (Exception var17) {
      }

      try {
         Field f = ProjectileTracker.class.getDeclaredField("currentVelocity");
         f.setAccessible(true);
         Object ret = f.get(tracker);
         if (ret instanceof AbstractVector) {
            return (AbstractVector)ret;
         }
      } catch (Exception var16) {
      }

      try {
         Method mc = tracker.getClass().getMethod("getCurrentLocation");
         Method mp = tracker.getClass().getMethod("getPreviousLocation");
         Object c = mc.invoke(tracker);
         Object p = mp.invoke(tracker);
         if (c != null && p != null) {
            Method getX = c.getClass().getMethod("getX");
            Method getY = c.getClass().getMethod("getY");
            Method getZ = c.getClass().getMethod("getZ");
            double dx = ((Number)getX.invoke(c)).doubleValue() - ((Number)getX.invoke(p)).doubleValue();
            double dy = ((Number)getY.invoke(c)).doubleValue() - ((Number)getY.invoke(p)).doubleValue();
            double dz = ((Number)getZ.invoke(c)).doubleValue() - ((Number)getZ.invoke(p)).doubleValue();
            return vec(dx, dy, dz);
         }
      } catch (Exception var15) {
      }

      return null;
   }

   private void setCurrentVelocity(ProjectileTracker tracker, AbstractVector v) {
      try {
         Method m = tracker.getClass().getMethod("setCurrentVelocity", AbstractVector.class);
         m.invoke(tracker, v);
      } catch (Exception var8) {
         try {
            Field f = ProjectileTracker.class.getDeclaredField("currentVelocity");
            f.setAccessible(true);
            f.set(tracker, v);
         } catch (Exception var7) {
            try {
               Method setVel = tracker.getClass().getMethod("setVelocity", double.class);
               double mag = length(v);
               setVel.invoke(tracker, mag);
            } catch (Exception var6) {
            }
         }
      }
   }

   private static class Basis {
      AbstractVector right;
      AbstractVector up;
      AbstractVector forward;

      Basis(AbstractVector r, AbstractVector u, AbstractVector f) {
         this.right = r;
         this.up = u;
         this.forward = f;
      }
   }

   public enum VelocityMode {
      SET,
      ADD,
      MULTIPLY,
      REMOVE,
      DIVIDE;
   }
}
