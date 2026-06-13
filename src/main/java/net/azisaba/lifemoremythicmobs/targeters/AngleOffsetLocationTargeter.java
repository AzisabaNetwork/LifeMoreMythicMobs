package net.azisaba.lifemoremythicmobs.targeters;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.targeters.ILocationSelector;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.World;

public class AngleOffsetLocationTargeter extends ILocationSelector {
   private final PlaceholderDouble hOffset;
   private final PlaceholderDouble vOffset;
   private final PlaceholderDouble length;
   private final PlaceholderDouble baseX;
   private final PlaceholderDouble baseY;
   private final PlaceholderDouble baseZ;
   private final PlaceholderDouble baseYaw;
   private final PlaceholderDouble basePitch;
   private final String bufName;
   private final String saveBufName;
   private final String saveMode;
   private final String useA;
   private final String useOrigin;
   private final int cap;
   private final boolean clear;
   private final boolean preferAngles;
   private final String saveAs;
   private final String saveBufMode;
   private final String saveBufPrefix;
   private final String saveBufSuffix;
   private final String tag;
   private static final double EPS = 1.0E-9;
   private static final Map<String, Deque<Location>> BUFFERS = new ConcurrentHashMap<>();

   public AngleOffsetLocationTargeter(MythicLineConfig config) {
      super(config);
      String sx = config.getString("x", null);
      String sy = config.getString("y", null);
      String sz = config.getString("z", null);
      String syaw = config.getString("baseyaw", null);
      String spit = config.getString("basepitch", null);
      this.baseX = sx != null ? new PlaceholderDouble(sx) : null;
      this.baseY = sy != null ? new PlaceholderDouble(sy) : null;
      this.baseZ = sz != null ? new PlaceholderDouble(sz) : null;
      this.baseYaw = syaw != null ? new PlaceholderDouble(syaw) : null;
      this.basePitch = spit != null ? new PlaceholderDouble(spit) : null;
      this.hOffset = new PlaceholderDouble(config.getString("h", "0"));
      this.vOffset = new PlaceholderDouble(config.getString("v", "0"));
      this.length = new PlaceholderDouble(config.getString("len", "1"));
      this.bufName = config.getString("buf", null);
      this.saveBufName = config.getString("savebuf", null);
      this.saveMode = config.getString("save", "none");
      this.useA = config.getString("useA", "xyz");
      this.useOrigin = config.getString("useOrigin", "current");
      this.cap = parseInt(config.getString("cap", "32"), 32);
      this.clear = Boolean.parseBoolean(config.getString("clear", "false"));
      this.preferAngles = Boolean.parseBoolean(config.getString("preferangles", "false"));
      this.saveAs = config.getString("saveas", "target");
      this.saveBufMode = config.getString("savebufmode", "none");
      this.saveBufPrefix = config.getString("savebufprefix", "");
      this.saveBufSuffix = config.getString("savebufsuffix", "");
      this.tag = config.getString("tag", null);
   }

   public HashSet<AbstractLocation> getLocations(SkillMetadata data) {
      HashSet<AbstractLocation> out = new HashSet<>(1);
      AbstractLocation originAbs = data.getOrigin();
      Location originCurrent = BukkitAdapter.adapt(originAbs);
      World world = originCurrent.getWorld();
      if (world == null) {
         return out;
      }

      String bufferKey = makeBufferKey(data, this.bufName);
      Deque<Location> buf = bufferKey != null ? BUFFERS.computeIfAbsent(bufferKey, k -> new ArrayDeque<>()) : null;
      if (buf != null && this.clear) {
         buf.clear();
      }

      Double baseYawDeg = null;
      Double basePitchDeg = null;
      if (this.preferAngles) {
         if (this.baseYaw != null) {
            baseYawDeg = this.safeGet(this.baseYaw, data, "baseYaw(prefer)");
         }

         if (this.basePitch != null) {
            basePitchDeg = this.safeGet(this.basePitch, data, "basePitch(prefer)");
         }
      }

      AngleOffsetLocationTargeter.Resolved rA = this.resolveA(data, buf, originCurrent, world);
      AngleOffsetLocationTargeter.Resolved rB = this.resolveOrigin(data, buf, originCurrent, world);
      Location A = rA != null ? rA.loc : null;
      Location B = rB != null ? rB.loc : null;
      String aSrc = rA != null ? rA.source : null;
      String bSrc = rB != null ? rB.source : null;
      if (A != null && B != null) {
         double dx = B.getX() - A.getX();
         double dz = B.getZ() - A.getZ();
         double r = Math.sqrt(dx * dx + dz * dz);
         if (r < 1.0E-9) {
            IgaDebugLogger.log(this.getClass(), this.prefix() + "WARN: |A→B| (horizontal) ≈ 0 (r=" + r + ") → yaw は 0°（南）寄り");
         }

         if (baseYawDeg == null) {
            baseYawDeg = Math.toDegrees(Math.atan2(-dx, dz));
         }

         if (basePitchDeg == null) {
            double dy = B.getY() - A.getY();
            basePitchDeg = Math.toDegrees(Math.atan2(-dy, r));
         }
      }

      if (baseYawDeg == null && this.baseYaw != null) {
         baseYawDeg = this.safeGet(this.baseYaw, data, "baseYaw(fallback)");
      }

      if (basePitchDeg == null && this.basePitch != null) {
         basePitchDeg = this.safeGet(this.basePitch, data, "basePitch(fallback)");
      }

      if (baseYawDeg == null) {
         baseYawDeg = 0.0;
      }

      if (basePitchDeg == null) {
         basePitchDeg = 0.0;
      }

      double hVal = this.safeGet(this.hOffset, data, "h");
      double vVal = this.safeGet(this.vOffset, data, "v");
      double len = Math.max(0.0, this.safeGet(this.length, data, "len"));
      double hDeg = baseYawDeg + hVal;
      double vDeg = basePitchDeg + vVal;
      double yawRad = Math.toRadians(hDeg);
      double pitchRad = Math.toRadians(vDeg);
      double cosPitch = Math.cos(pitchRad);
      double dirX = -Math.sin(yawRad) * cosPitch;
      double dirZ = Math.cos(yawRad) * cosPitch;
      double dirY = -Math.sin(pitchRad);
      double tx = originCurrent.getX() + dirX * len;
      double ty = originCurrent.getY() + dirY * len;
      double tz = originCurrent.getZ() + dirZ * len;
      IgaDebugLogger.log(this.getClass(), this.prefix() + "useA=(" + this.fmtLoc(A) + ")");
      IgaDebugLogger.log(this.getClass(), this.prefix() + "useOrigin=(" + this.fmtLoc(B) + ")");
      IgaDebugLogger.log(this.getClass(), this.prefix() + "dir=(x=" + dirX + ", y=" + dirY + ", z=" + dirZ + ")");
      IgaDebugLogger.log(this.getClass(), this.prefix() + "h=" + hVal);
      IgaDebugLogger.log(this.getClass(), this.prefix() + "finalYawDeg=" + hDeg);
      Location target = new Location(world, tx, ty, tz);
      if ("push".equalsIgnoreCase(this.saveMode)) {
         Location saveLoc;
         label126: {
            String saveName;
            switch ((saveName = normalize(this.saveAs)).hashCode()) {
               case -381429869:
                  if (saveName.equals("origincurrent")) {
                     saveLoc = originCurrent;
                     break label126;
                  }
                  break;
               case 97:
                  if (saveName.equals("a")) {
                     saveLoc = A;
                     break label126;
                  }
                  break;
               case 270857069:
                  if (saveName.equals("useorigin")) {
                     saveLoc = B;
                     break label126;
                  }
            }

            saveLoc = target;
         }

         String saveName;
         if ("bya".equalsIgnoreCase(normalize(this.saveBufMode)) && aSrc != null) {
            saveName = this.saveBufPrefix + aSrc + this.saveBufSuffix;
         } else if ("byorigin".equalsIgnoreCase(normalize(this.saveBufMode)) && bSrc != null) {
            saveName = this.saveBufPrefix + bSrc + this.saveBufSuffix;
         } else {
            saveName = this.saveBufName != null ? this.saveBufName : this.bufName;
         }

         String saveKey = makeBufferKey(data, saveName);
         if (saveKey != null && saveLoc != null) {
            Deque<Location> saveBuf = BUFFERS.computeIfAbsent(saveKey, k -> new ArrayDeque<>());
            saveBuf.addLast(new Location(saveLoc.getWorld(), saveLoc.getX(), saveLoc.getY(), saveLoc.getZ()));

            while (saveBuf.size() > this.cap) {
               saveBuf.pollFirst();
            }

            IgaDebugLogger.log(this.getClass(), this.prefix() + "[save] name='" + saveName + "', as=" + this.saveAs + ", loc=(" + this.fmtLoc(saveLoc) + ")");
         }
      }

      out.add(BukkitAdapter.adapt(target));
      return out;
   }

   private Deque<Location> getBufferByName(SkillMetadata data, String name) {
      if (name == null) {
         return null;
      }

      String key = makeBufferKey(data, name);
      return key != null ? BUFFERS.get(key) : null;
   }

   private AngleOffsetLocationTargeter.Resolved resolveA(SkillMetadata data, Deque<Location> buf, Location originCurrent, World world) {
      String ua = normalize(this.useA);
      if (ua.startsWith("buf:last:")) {
         if (ua.startsWith("buf:last:nearest:")) {
            String namesCsv = this.useA.substring("buf:last:nearest:".length());
            String[] names = namesCsv.split(",");
            Location best = null;
            String bestName = null;
            double bestD2 = Double.POSITIVE_INFINITY;
            IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest] originCurrent=(" + this.fmtLoc(originCurrent) + ")");
            IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest] candidates=" + namesCsv);
            String[] var15 = names;
            int var14 = names.length;

            for (int var13 = 0; var13 < var14; var13++) {
               String raw = var15[var13];
               String name = raw.trim();
               if (name.isEmpty()) {
                  IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest] skip empty name");
               } else {
                  Deque<Location> other = this.getBufferByName(data, name);
                  if (other != null && !other.isEmpty()) {
                     Location cand = peekLast(other);
                     if (cand == null) {
                        IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest] buffer '" + name + "' last=null");
                     } else {
                        double dx = cand.getX() - originCurrent.getX();
                        double dy = cand.getY() - originCurrent.getY();
                        double dz = cand.getZ() - originCurrent.getZ();
                        double d2 = dx * dx + dy * dy + dz * dz;
                        IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest] '" + name + "' cand=(" + this.fmtLoc(cand) + ") d2=" + d2);
                        if (d2 < bestD2) {
                           bestD2 = d2;
                           best = cand;
                           bestName = name;
                           IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest] -> update best: '" + bestName + "' d2=" + bestD2);
                        }
                     }
                  } else {
                     IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest] buffer '" + name + "' is null/empty");
                  }
               }
            }

            if (best != null) {
               IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest] selected '" + bestName + "' A=(" + this.fmtLoc(best) + ")");
               return new AngleOffsetLocationTargeter.Resolved(best, bestName);
            }

            IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest] no available candidate, fallback to normal resolution");
         }

         String name = this.useA.substring("buf:last:".length());
         Deque<Location> other = this.getBufferByName(data, name);
         Location last = other != null ? peekLast(other) : null;
         if (last != null) {
            return new AngleOffsetLocationTargeter.Resolved(last, name);
         }
      }

      String var28 = ua;
      switch (ua.hashCode()) {
         case -1367559124:
            if (var28.equals("caster")) {
               return new AngleOffsetLocationTargeter.Resolved(this.safeCasterLoc(data), "caster");
            }
            break;
         case -1059891784:
            if (var28.equals("trigger")) {
               return new AngleOffsetLocationTargeter.Resolved(this.safeTriggerLoc(data), "trigger");
            }
            break;
         case -1008619738:
            if (var28.equals("origin")) {
               return new AngleOffsetLocationTargeter.Resolved(originCurrent, "origin");
            }
            break;
         case 3314326:
            if (var28.equals("last")) {
               return buf != null ? new AngleOffsetLocationTargeter.Resolved(peekLast(buf), "last") : null;
            }
      }

      if (this.useA.startsWith("idx:")) {
         if (buf == null) {
            return null;
         }

         Integer idx = parseIdx(this.useA.substring(4));
         Location l = getByIndex(buf, idx);
         return l != null ? new AngleOffsetLocationTargeter.Resolved(l, "idx:" + idx) : null;
      } else if (this.baseX != null && this.baseZ != null) {
         Double ax = this.safeGet(this.baseX, data, "ax");
         Double ay = this.baseY != null ? this.safeGet(this.baseY, data, "ay") : originCurrent.getY();
         Double az = this.safeGet(this.baseZ, data, "az");
         return new AngleOffsetLocationTargeter.Resolved(new Location(world, ax, ay, az), "xyz");
      } else {
         return null;
      }
   }

   private AngleOffsetLocationTargeter.Resolved resolveOrigin(SkillMetadata data, Deque<Location> buf, Location originCurrent, World world) {
      String uo = normalize(this.useOrigin);
      if (uo.startsWith("buf:last:nearest:")) {
         String namesCsv = this.useOrigin.substring("buf:last:nearest:".length());
         String[] names = namesCsv.split(",");
         Location best = null;
         String bestName = null;
         double bestD2 = Double.POSITIVE_INFINITY;
         IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest-origin] originCurrent=(" + this.fmtLoc(originCurrent) + ")");
         IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest-origin] candidates=" + namesCsv);
         String[] var15 = names;
         int var14 = names.length;

         for (int var13 = 0; var13 < var14; var13++) {
            String raw = var15[var13];
            String name = raw.trim();
            if (name.isEmpty()) {
               IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest-origin] skip empty name");
            } else {
               Deque<Location> other = this.getBufferByName(data, name);
               Location cand = other != null ? peekLast(other) : null;
               if (cand == null) {
                  IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest-origin] buffer '" + name + "' null/empty");
               } else {
                  double dx = cand.getX() - originCurrent.getX();
                  double dy = cand.getY() - originCurrent.getY();
                  double dz = cand.getZ() - originCurrent.getZ();
                  double d2 = dx * dx + dy * dy + dz * dz;
                  IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest-origin] '" + name + "' cand=(" + this.fmtLoc(cand) + ") d2=" + d2);
                  if (d2 < bestD2) {
                     bestD2 = d2;
                     best = cand;
                     bestName = name;
                  }
               }
            }
         }

         if (best != null) {
            IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest-origin] selected '" + bestName + "' origin=(" + this.fmtLoc(best) + ")");
            return new AngleOffsetLocationTargeter.Resolved(best, bestName);
         }

         IgaDebugLogger.log(this.getClass(), this.prefix() + "[nearest-origin] no available candidate, fallback to normal resolution");
      }

      if (uo.startsWith("buf:last:")) {
         String name = this.useOrigin.substring("buf:last:".length());
         Deque<Location> other = this.getBufferByName(data, name);
         Location last = other != null ? peekLast(other) : null;
         return last != null ? new AngleOffsetLocationTargeter.Resolved(last, name) : null;
      }

      String var27 = uo;
      switch (uo.hashCode()) {
         case -1367559124:
            if (var27.equals("caster")) {
               return new AngleOffsetLocationTargeter.Resolved(this.safeCasterLoc(data), "caster");
            }
            break;
         case -1059891784:
            if (var27.equals("trigger")) {
               return new AngleOffsetLocationTargeter.Resolved(this.safeTriggerLoc(data), "trigger");
            }
            break;
         case 104125:
            if (var27.equals("idx")) {
               if (buf == null) {
                  return null;
               }

               Integer idx = parseIdx(this.useOrigin.length() > 3 ? this.useOrigin.substring(4) : null);
               Location li = getByIndex(buf, idx);
               return li != null ? new AngleOffsetLocationTargeter.Resolved(li, "idx:" + idx) : null;
            }
            break;
         case 119193:
            if (var27.equals("xyz")) {
               if (this.baseX != null && this.baseZ != null) {
                  Double bx = this.safeGet(this.baseX, data, "bx");
                  Double by = this.baseY != null ? this.safeGet(this.baseY, data, "by") : originCurrent.getY();
                  Double bz = this.safeGet(this.baseZ, data, "bz");
                  return new AngleOffsetLocationTargeter.Resolved(new Location(world, bx, by, bz), "xyz");
               }

               return null;
            }
            break;
         case 3314326:
            if (var27.equals("last")) {
               return buf != null ? new AngleOffsetLocationTargeter.Resolved(peekLast(buf), "last") : null;
            }
      }

      if (this.useOrigin.startsWith("idx:")) {
         if (buf == null) {
            return null;
         }

         Integer i = parseIdx(this.useOrigin.substring(4));
         Location lk = getByIndex(buf, i);
         return lk != null ? new AngleOffsetLocationTargeter.Resolved(lk, "idx:" + i) : null;
      } else {
         return new AngleOffsetLocationTargeter.Resolved(originCurrent, "current");
      }
   }

   private static String makeBufferKey(SkillMetadata data, String bufName) {
      if (bufName == null) {
         return null;
      }

      String exec = "GLOBAL";

      try {
         if (data.getCaster() != null && data.getCaster().getEntity() != null) {
            UUID id = data.getCaster().getEntity().getUniqueId();
            if (id != null) {
               exec = id.toString();
            }
         }
      } catch (Throwable var4) {
      }

      return exec + "|" + bufName;
   }

   private static String normalize(String s) {
      return s == null ? "" : s.toLowerCase();
   }

   private static Integer parseIdx(String s) {
      try {
         return Integer.parseInt(s.trim());
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static int parseInt(String s, int def) {
      try {
         return Integer.parseInt(s.trim());
      } catch (Throwable ignored) {
         return def;
      }
   }

   private static Location peekLast(Deque<Location> dq) {
      return dq.isEmpty() ? null : dq.peekLast();
   }

   private static Location getByIndex(Deque<Location> dq, Integer idx) {
      if (dq != null && !dq.isEmpty() && idx != null) {
         int n = dq.size();
         int i = idx;
         if (i < 0) {
            i += n;
         }

         if (i >= 0 && i < n) {
            int k = 0;

            for (Location loc : dq) {
               if (k == i) {
                  return loc;
               }

               k++;
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private Location safeCasterLoc(SkillMetadata data) {
      try {
         if (data.getCaster() != null && data.getCaster().getEntity() != null) {
            return BukkitAdapter.adapt(data.getCaster().getEntity().getLocation());
         }
      } catch (Throwable var3) {
      }

      return null;
   }

   private Location safeTriggerLoc(SkillMetadata data) {
      try {
         if (data.getTrigger() != null) {
            return BukkitAdapter.adapt(data.getTrigger().getLocation());
         }
      } catch (Throwable var3) {
      }

      return null;
   }

   private double safeGet(PlaceholderDouble ph, SkillMetadata data, String name) {
      try {
         return ph.get(data);
      } catch (Throwable t) {
         return 0.0;
      }
   }

   private static double normDeg(double deg) {
      double x = deg % 360.0;
      if (x <= -180.0) {
         x += 360.0;
      }

      if (x > 180.0) {
         x -= 360.0;
      }

      return x;
   }

   private static double deltaDeg(double a, double b) {
      return normDeg(a - b);
   }

   private String prefix() {
      return "[AngleOffset" + (this.tag != null ? ":" + this.tag : "") + "] ";
   }

   private String fmtLoc(Location l) {
      return l == null ? "null" : String.format("%.3f, %.3f, %.3f", l.getX(), l.getY(), l.getZ());
   }

   private static class Resolved {
      final Location loc;
      final String source;

      Resolved(Location loc, String source) {
         this.loc = loc;
         this.source = source;
      }
   }
}
