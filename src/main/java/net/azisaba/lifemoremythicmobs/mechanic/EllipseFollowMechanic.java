package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EllipseFollowMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderDouble a;
   private final PlaceholderDouble b;
   private final PlaceholderDouble stepsPH;
   private final PlaceholderDouble rotXDeg;
   private final PlaceholderDouble rotYDeg;
   private final PlaceholderDouble rotZDeg;
   private final PlaceholderDouble ox;
   private final PlaceholderDouble oy;
   private final PlaceholderDouble oz;
   private final PlaceholderDouble durationPH;
   private final PlaceholderDouble drawIntervalPH;
   private final PlaceholderDouble pointIntervalPH;
   private final boolean reverse;
   private final Particle ellipseParticle;
   private final int ellipseAmount;
   private final double ellipseSpeed;
   private final PlaceholderString ellipseColorPH;
   private final PlaceholderDouble ellipseSizePH;
   private final PlaceholderString ellipseBlockPH;
   private final PlaceholderString ellipseItemPH;
   private final Particle pointParticle;
   private final int pointAmount;
   private final double pointSpeed;
   private final PlaceholderString pointColorPH;
   private final PlaceholderDouble pointSizePH;
   private final PlaceholderString pointBlockPH;
   private final PlaceholderString pointItemPH;
   private final String onPointSkillNameRaw;
   private Optional<Skill> onPointSkill = Optional.empty();
   private final boolean log;
   private final int logEvery;
   private final int debugPoints;

   public EllipseFollowMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.a = PlaceholderDouble.of(config.getString(new String[]{"a", "radiusa"}, "4", new String[0]));
      this.b = PlaceholderDouble.of(config.getString(new String[]{"b", "radiusb"}, "2", new String[0]));
      this.stepsPH = PlaceholderDouble.of(config.getString(new String[]{"steps"}, "120", new String[0]));
      this.rotXDeg = PlaceholderDouble.of(config.getString(new String[]{"rotx", "rotationx", "rx"}, "0", new String[0]));
      this.rotYDeg = PlaceholderDouble.of(config.getString(new String[]{"roty", "rotationy", "ry"}, "0", new String[0]));
      this.rotZDeg = PlaceholderDouble.of(config.getString(new String[]{"rotz", "rotationz", "rz"}, "0", new String[0]));
      this.ox = PlaceholderDouble.of(config.getString(new String[]{"ox"}, "0", new String[0]));
      this.oy = PlaceholderDouble.of(config.getString(new String[]{"oy"}, "0", new String[0]));
      this.oz = PlaceholderDouble.of(config.getString(new String[]{"oz"}, "0", new String[0]));
      this.durationPH = PlaceholderDouble.of(config.getString(new String[]{"duration", "d"}, "200", new String[0]));
      this.drawIntervalPH = PlaceholderDouble.of(config.getString(new String[]{"drawInterval"}, "1", new String[0]));
      this.pointIntervalPH = PlaceholderDouble.of(config.getString(new String[]{"pointInterval"}, "2", new String[0]));
      this.reverse = config.getBoolean(new String[]{"reverse", "clockwise"}, false);
      this.ellipseParticle = parseParticle(config.getString(new String[]{"particle", "ellipseparticle"}, "REDSTONE", new String[0]));
      this.ellipseAmount = config.getInteger(new String[]{"amount", "ellipseamount"}, 1);
      this.ellipseSpeed = config.getDouble(new String[]{"speed", "ellipsespeed"}, 0.0);
      this.ellipseColorPH = PlaceholderString.of(config.getString(new String[]{"color"}, null, new String[0]));
      this.ellipseSizePH = PlaceholderDouble.of(config.getString(new String[]{"size"}, "1.0", new String[0]));
      this.ellipseBlockPH = PlaceholderString.of(config.getString(new String[]{"block"}, null, new String[0]));
      this.ellipseItemPH = PlaceholderString.of(config.getString(new String[]{"item"}, null, new String[0]));
      this.pointParticle = parseParticle(config.getString(new String[]{"pointparticle"}, this.ellipseParticle.name(), new String[0]));
      this.pointAmount = config.getInteger(new String[]{"pointamount"}, Math.max(1, this.ellipseAmount));
      this.pointSpeed = config.getDouble(new String[]{"pointspeed"}, this.ellipseSpeed);
      this.pointColorPH = PlaceholderString.of(config.getString(new String[]{"pointColor"}, null, new String[0]));
      this.pointSizePH = PlaceholderDouble.of(config.getString(new String[]{"pointSize"}, null, new String[0]));
      this.pointBlockPH = PlaceholderString.of(config.getString(new String[]{"pointBlock"}, null, new String[0]));
      this.pointItemPH = PlaceholderString.of(config.getString(new String[]{"pointItem"}, null, new String[0]));
      this.onPointSkillNameRaw = config.getString(new String[]{"onpointskill", "onPoint", "oP", "onpoint", "op", "ops"}, null, new String[0]);
      this.log = config.getBoolean("log", false);
      this.logEvery = Math.max(1, config.getInteger("logEvery", 20));
      this.debugPoints = Math.max(0, config.getInteger("debugPoints", 3));
      this.setAsyncSafe(false);
      MythicBukkit.inst()
         .getSkillManager()
         .queueSecondPass(
            () -> {
               if (this.onPointSkillNameRaw != null) {
                  this.onPointSkill = MythicBukkit.inst().getSkillManager().getSkill(this.onPointSkillNameRaw);
                  if (this.log) {
                     IgaDebugLogger.log(
                        this.getClass(),
                        "init: particle="
                           + this.ellipseParticle
                           + " amount="
                           + this.ellipseAmount
                           + " speed="
                           + this.ellipseSpeed
                           + " pointParticle="
                           + this.pointParticle
                           + " pointAmount="
                           + this.pointAmount
                           + " pointSpeed="
                           + this.pointSpeed
                           + " reverse="
                           + this.reverse
                           + " logEvery="
                           + this.logEvery
                           + "debugPoints="
                           + this.debugPoints
                     );
                  }
               }
            }
         );
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!Bukkit.isPrimaryThread()) {
         if (this.log) {
            IgaDebugLogger.log(this.getClass(), "castAtEntity: off-thread -> reschedule to main");
         }

         Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> this.doCastAtEntity(data, target));
         return SkillResult.SUCCESS;
      } else {
         return this.doCastAtEntity(data, target);
      }
   }

   private boolean doCastAtEntity(final SkillMetadata data, AbstractEntity target) {
      final Entity bukkitTarget = target.getBukkitEntity();
      if (bukkitTarget != null && bukkitTarget.isValid()) {
         final int steps = Math.max(8, (int)Math.round(this.stepsPH.get(data)));
         double aVal = Math.max(0.001, this.a.get(data));
         double bVal = Math.max(0.001, this.b.get(data));
         double rotX = Math.toRadians(this.rotXDeg.get(data));
         double rotY = Math.toRadians(this.rotYDeg.get(data));
         double rotZ = Math.toRadians(this.rotZDeg.get(data));
         final long duration = Math.max(1L, Math.round(this.durationPH.get(data)));
         final long drawInterval = Math.max(1L, Math.round(this.drawIntervalPH.get(data)));
         final long pointInterval = Math.max(1L, Math.round(this.pointIntervalPH.get(data)));
         final double offX = this.ox.get(data);
         final double offY = this.oy.get(data);
         final double offZ = this.oz.get(data);
         if (this.log) {
            IgaDebugLogger.log(
               this.getClass(),
               String.format(
                  Locale.ROOT,
                  "cast: steps=%d a=%.3f b=%.3f rot=(%.1f,%.1f,%.1f) off=(%.2f,%.2f,%.2f) dur=%d drawInt=%d pointInt=%d reverse=%s",
                  steps,
                  aVal,
                  bVal,
                  Math.toDegrees(rotX),
                  Math.toDegrees(rotY),
                  Math.toDegrees(rotZ),
                  offX,
                  offY,
                  offZ,
                  duration,
                  drawInterval,
                  pointInterval,
                  this.reverse
               )
            );
         }

         final List<Vector> ring = new ArrayList<>(steps);
         double minX = Double.POSITIVE_INFINITY;
         double minY = Double.POSITIVE_INFINITY;
         double minZ = Double.POSITIVE_INFINITY;
         double maxX = Double.NEGATIVE_INFINITY;
         double maxY = Double.NEGATIVE_INFINITY;
         double maxZ = Double.NEGATIVE_INFINITY;

         for (int i = 0; i < steps; i++) {
            double t = (Math.PI * 2) * i / steps;
            Vector p = new Vector(aVal * Math.cos(t), 0.0, bVal * Math.sin(t));
            p = rotateX(p, rotX);
            p = rotateY(p, rotY);
            p = rotateZ(p, rotZ);
            ring.add(p);
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxZ = Math.max(maxZ, p.getZ());
         }

         if (this.log) {
            IgaDebugLogger.log(
               this.getClass(),
               String.format(Locale.ROOT, "ring: built=%d AABB x[%.2f..%.2f] y[%.2f..%.2f] z[%.2f..%.2f]", ring.size(), minX, maxX, minY, maxY, minZ, maxZ)
            );

            for (int i = 0; i < Math.min(this.debugPoints, ring.size()); i++) {
               Vector v = ring.get(i);
               IgaDebugLogger.log(this.getClass(), String.format(Locale.ROOT, "ring[%d]=(%.3f, %.3f, %.3f)", i, v.getX(), v.getY(), v.getZ()));
            }
         }

         final int stepDir = this.reverse ? -1 : 1;
         final int[] pointIndex = new int[]{this.reverse ? steps - 1 : 0};
         final long[] tickCounter = new long[1];
         final long[] pointTickCounter = new long[1];
         String ellipseColor = this.ellipseColorPH != null ? this.ellipseColorPH.get(data) : null;
         double ellipseSize = this.ellipseSizePH != null ? this.ellipseSizePH.get(data) : 1.0;
         String ellipseBlock = this.ellipseBlockPH != null ? this.ellipseBlockPH.get(data) : null;
         String ellipseItem = this.ellipseItemPH != null ? this.ellipseItemPH.get(data) : null;
         String pointColor = this.pointColorPH != null ? this.pointColorPH.get(data) : ellipseColor;
         double pointSize = this.pointSizePH != null ? this.pointSizePH.get(data) : ellipseSize;
         String pointBlock = this.pointBlockPH != null ? this.pointBlockPH.get(data) : null;
         String pointItem = this.pointItemPH != null ? this.pointItemPH.get(data) : null;
         final EllipseFollowMechanic.ParticleSpec ellipseSpec = buildSpecForEllipse(
            this.ellipseParticle, this.ellipseAmount, this.ellipseSpeed, ellipseColor, ellipseSize, ellipseBlock, ellipseItem
         );
         final EllipseFollowMechanic.ParticleSpec pointSpec = buildSpecForEllipse(
            this.pointParticle, this.pointAmount, this.pointSpeed, pointColor, pointSize, pointBlock, pointItem
         );
         if (this.log) {
            IgaDebugLogger.log(
               this.getClass(),
               "spec ellipse=" + this.ellipseParticle + " color=" + ellipseColor + " size=" + ellipseSize + " block=" + ellipseBlock + " item=" + ellipseItem
            );
            IgaDebugLogger.log(
               this.getClass(),
               "spec point=" + this.pointParticle + " color=" + pointColor + " size=" + pointSize + "block=" + pointBlock + " item=" + pointItem
            );
         }

         (new BukkitRunnable() {
               public void run() {
                  if (bukkitTarget.isValid() && !bukkitTarget.isDead()) {
                     if (tickCounter[0] >= duration) {
                        if (EllipseFollowMechanic.this.log) {
                           IgaDebugLogger.log(this.getClass(), "runner: cancel (duration reached) t=" + tickCounter[0]);
                        }

                        this.cancel();
                     } else {
                        Location center = bukkitTarget.getLocation().clone().add(offX, offY, offZ);
                        World world = center.getWorld();
                        if (world == null) {
                           if (EllipseFollowMechanic.this.log) {
                              IgaDebugLogger.log(this.getClass(), "runner: world is null -> cancel");
                           }

                           this.cancel();
                        } else {
                           for (Vector v : ring) {
                              Location loc = center.clone().add(v);

                              try {
                                 ellipseSpec.spawn(world, loc, true);
                              } catch (Throwable e) {
                                 if (EllipseFollowMechanic.this.log) {
                                    IgaDebugLogger.log(
                                       this.getClass(), "spawn ellipse particle failed: " + e.getClass().getSimpleName() + ": " + e.getMessage()
                                    );
                                 }
                              }
                           }

                           if (pointTickCounter[0] >= pointInterval) {
                              pointTickCounter[0] = 0L;
                              Vector pv = ring.get(pointIndex[0]);
                              Location pointLoc = center.clone().add(pv);

                              try {
                                 pointSpec.spawn(world, pointLoc, true);
                              } catch (Throwable e) {
                                 if (EllipseFollowMechanic.this.log) {
                                    IgaDebugLogger.log(this.getClass(), "spawn point particle failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                                 }
                              }

                              EllipseFollowMechanic.this.executeOnPointSkill(data, pointLoc);
                              pointIndex[0] = (pointIndex[0] + stepDir) % steps;
                              if (pointIndex[0] < 0) {
                                 pointIndex[0] = pointIndex[0] + steps;
                              }
                           }

                           tickCounter[0] = tickCounter[0] + drawInterval;
                           pointTickCounter[0] = pointTickCounter[0] + drawInterval;
                           if (EllipseFollowMechanic.this.log && tickCounter[0] % EllipseFollowMechanic.this.logEvery == 0L) {
                              IgaDebugLogger.log(
                                 this.getClass(),
                                 String.format(
                                    Locale.ROOT,
                                    "tick=%d center=(%.2f,%.2f,%.2f) world=%s pointIndex=%d/%d nextIn=%d",
                                    tickCounter[0],
                                    center.getX(),
                                    center.getY(),
                                    center.getZ(),
                                    world.getName(),
                                    pointIndex[0],
                                    steps,
                                    Math.max(0, (int)(pointInterval - pointTickCounter[0]))
                                 )
                              );
                           }
                        }
                     }
                  } else {
                     if (EllipseFollowMechanic.this.log) {
                        IgaDebugLogger.log(this.getClass(), "runner: cancel (target invalid)");
                     }

                     this.cancel();
                  }
               }
            })
            .runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), 0L, Math.max(1L, drawInterval));
         if (this.log) {
            IgaDebugLogger.log(this.getClass(), "runner: scheduled interval=" + drawInterval + " plugin=" + JavaPlugin.getPlugin(LifeMoreMythicMobs.class).getName());
         }

         return SkillResult.SUCCESS;
      } else {
         if (this.log) {
            IgaDebugLogger.log(this.getClass(), "cast: target invalid");
         }

         return SkillResult.FAILURE;
      }
   }

   private void executeOnPointSkill(SkillMetadata baseMeta, Location pointLoc) {
      if (this.onPointSkill.isPresent()) {
         Skill skill = this.onPointSkill.get();

         try {
            SkillMetadata child = baseMeta.deepClone();
            child.setOrigin(BukkitAdapter.adapt(pointLoc));
            skill.execute(child);
            if (this.log) {
               IgaDebugLogger.log(
                  this.getClass(),
                  "onPointSkill: executed via deepClone origin=("
                     + pointLoc.getX()
                     + ","
                     + pointLoc.getY()
                     + ","
                     + pointLoc.getZ()
                     + ") name="
                     + this.onPointSkillNameRaw
               );
            }
         } catch (Throwable var6) {
            try {
               skill.execute(baseMeta);
            } catch (Throwable var5) {
            }
         }
      }
   }

   private static Particle parseParticle(String name) {
      try {
         return Particle.valueOf(name.toUpperCase(Locale.ROOT));
      } catch (Throwable t) {
         IgaDebugLogger.log("[LifeMoreMythicMobs]", "unknown particle '" + name + "', fallback CRIT");
         return Particle.CRIT;
      }
   }

   private static Vector rotateX(Vector v, double rx) {
      double cos = Math.cos(rx);
      double sin = Math.sin(rx);
      double y = v.getY() * cos - v.getZ() * sin;
      double z = v.getY() * sin + v.getZ() * cos;
      return new Vector(v.getX(), y, z);
   }

   private static Vector rotateY(Vector v, double ry) {
      double cos = Math.cos(ry);
      double sin = Math.sin(ry);
      double x = v.getX() * cos + v.getZ() * sin;
      double z = -v.getX() * sin + v.getZ() * cos;
      return new Vector(x, v.getY(), z);
   }

   private static Vector rotateZ(Vector v, double rz) {
      double cos = Math.cos(rz);
      double sin = Math.sin(rz);
      double x = v.getX() * cos - v.getY() * sin;
      double y = v.getX() * sin + v.getY() * cos;
      return new Vector(x, y, v.getZ());
   }

   private static Color parseColor(String s) {
      if (s == null) {
         return Color.WHITE;
      }

      s = s.trim();

      try {
         if (s.startsWith("#") && s.length() == 7) {
            int rgb = Integer.parseInt(s.substring(1), 16);
            return Color.fromRGB(rgb);
         } else {
            String[] sp = s.split(",");
            int r = Integer.parseInt(sp[0].trim());
            int g = Integer.parseInt(sp[1].trim());
            int b = Integer.parseInt(sp[2].trim());
            return Color.fromRGB(Math.max(0, Math.min(255, r)), Math.max(0, Math.min(255, g)), Math.max(0, Math.min(255, b)));
         }
      } catch (Throwable t) {
         IgaDebugLogger.log("[LifeMoreMythicMobs]", "invalid color '" + s + "' , fallback WHITE");
         return Color.WHITE;
      }
   }

   private static EllipseFollowMechanic.ParticleSpec buildSpecForEllipse(
      Particle type, int amount, double speed, String colorStr, double size, String blockStr, String itemStr
   ) {
      switch (type) {
         case SPELL_MOB:
         case SPELL_MOB_AMBIENT: {
            Color c = parseColor(colorStr);
            double r = c.getRed() / 255.0;
            double g = c.getGreen() / 255.0;
            double b = c.getBlue() / 255.0;
            return new EllipseFollowMechanic.ParticleSpec(type, 0, r, g, b, 1.0, null);
         }
         case REDSTONE: {
            Color c = parseColor(colorStr);
            DustOptions dust = new DustOptions(c, (float)Math.max(0.001, size));
            int count = Math.max(1, amount);
            return new EllipseFollowMechanic.ParticleSpec(type, count, 0.0, 0.0, 0.0, 0.0, dust);
         }
         case ITEM_CRACK:
            if (itemStr == null) {
               IgaDebugLogger.log("[LifeMoreMythicMobs]", type + "requires item=<MATERIAL>");
               return new EllipseFollowMechanic.ParticleSpec(type, Math.max(1, amount), 0.0, 0.0, 0.0, speed, new ItemStack(Material.STONE));
            }

            Material m = Material.matchMaterial(itemStr.toUpperCase(Locale.ROOT));
            if (m == null || !m.isItem()) {
               IgaDebugLogger.log("[LifeMoreMythicMobs]", "unknown item '" + itemStr + "', fallback STONE");
               m = Material.STONE;
            }

            return new EllipseFollowMechanic.ParticleSpec(type, Math.max(1, amount), 0.0, 0.0, 0.0, speed, new ItemStack(m));
         case BLOCK_CRACK:
         case BLOCK_DUST:
         case FALLING_DUST:
            if (blockStr == null) {
               IgaDebugLogger.log("[LifeMoreMythicMobs]", type + "requires block=<MATERIAL>");
               BlockData bd = Material.STONE.createBlockData();
               return new EllipseFollowMechanic.ParticleSpec(type, Math.max(1, amount), 0.0, 0.0, 0.0, speed, bd);
            }

            Material m = Material.matchMaterial(blockStr.toUpperCase(Locale.ROOT));
            if (m == null || !m.isBlock()) {
               IgaDebugLogger.log("[LifeMoreMythicMobs]", "unknown block '" + blockStr + "', fallback STONE");
               m = Material.STONE;
            }

            return new EllipseFollowMechanic.ParticleSpec(type, Math.max(1, amount), 0.0, 0.0, 0.0, speed, m.createBlockData());
         default:
            return new EllipseFollowMechanic.ParticleSpec(type, Math.max(1, amount), 0.0, 0.0, 0.0, speed, null);
      }
   }

   private static class ParticleSpec {
      final Particle type;
      final int count;
      final double offX;
      final double offY;
      final double offZ;
      final double extra;
      final Object data;

      ParticleSpec(Particle type, int count, double offX, double offY, double offZ, double extra, Object data) {
         this.type = type;
         this.count = count;
         this.offX = offX;
         this.offY = offY;
         this.offZ = offZ;
         this.extra = extra;
         this.data = data;
      }

      void spawn(World world, Location loc, boolean force) {
         world.spawnParticle(this.type, loc, this.count, this.offX, this.offY, this.offZ, this.extra, this.data, force);
      }
   }
}
