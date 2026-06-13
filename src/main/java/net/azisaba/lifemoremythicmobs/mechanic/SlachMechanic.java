package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

public class SlachMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {
   private final PlaceholderDouble width;
   private final PlaceholderDouble height;
   private final PlaceholderDouble radius;
   private final PlaceholderDouble angle;
   private final PlaceholderDouble points;
   private final PlaceholderString rotationString;
   private final PlaceholderDouble pitch;
   private final PlaceholderDouble yaw;
   private final PlaceholderDouble roll;
   private final PlaceholderInt duration;
   private final PlaceholderDouble xOffset;
   private final PlaceholderDouble yOffset;
   private final PlaceholderDouble zOffset;
   private final PlaceholderDouble forwardOffset;
   private final PlaceholderDouble targetxOffset;
   private final PlaceholderDouble targetyOffset;
   private final PlaceholderDouble targetzOffset;
   private final boolean directionTowardsTarget;
   private final boolean matchCasterDirection;
   private final boolean fromOrigin;
   private final PlaceholderInt hitLimit;
   private final String hitConditionString;
   private List<SkillCondition> hitConditions;
   private final PlaceholderString onStartSkillName;
   private final PlaceholderString onEndSkillName;
   private final PlaceholderString onPointSkillName;
   private final PlaceholderString onHitEntitySkillName;
   private Optional<Skill> onStartSkill = Optional.empty();
   private Optional<Skill> onEndSkill = Optional.empty();

   public SlachMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.width = PlaceholderDouble.of(config.getString(new String[]{"width", "w"}, "1", new String[0]));
      this.height = PlaceholderDouble.of(config.getString(new String[]{"height", "h"}, "1", new String[0]));
      this.radius = PlaceholderDouble.of(config.getString(new String[]{"radius", "r"}, "1", new String[0]));
      this.angle = PlaceholderDouble.of(config.getString(new String[]{"angle", "arc", "a"}, "180", new String[0]));
      this.points = PlaceholderDouble.of(config.getString(new String[]{"points", "p"}, String.valueOf(10), new String[0]));
      this.rotationString = PlaceholderString.of(config.getString(new String[]{"rotation", "rot"}, "0,0,0", new String[0]));
      this.pitch = PlaceholderDouble.of(config.getString("pitch", "0"));
      this.yaw = PlaceholderDouble.of(config.getString("yaw", "0"));
      this.roll = PlaceholderDouble.of(config.getString("roll", "0"));
      this.duration = PlaceholderInt.of(config.getString("duration", "0"));
      this.xOffset = PlaceholderDouble.of(config.getString(new String[]{"xoffset", "xo", "x"}, "0", new String[0]));
      this.yOffset = PlaceholderDouble.of(config.getString(new String[]{"yoffset", "yo", "y"}, "0", new String[0]));
      this.zOffset = PlaceholderDouble.of(config.getString(new String[]{"zoffset", "zo", "z"}, "0", new String[0]));
      this.forwardOffset = PlaceholderDouble.of(config.getString(new String[]{"forwardoffset", "foffset", "fo"}, "0", new String[0]));
      this.targetxOffset = PlaceholderDouble.of(config.getString(new String[]{"targetxoffset", "txo", "tx"}, "0", new String[0]));
      this.targetyOffset = PlaceholderDouble.of(config.getString(new String[]{"targetyoffset", "tyo", "ty"}, "0", new String[0]));
      this.targetzOffset = PlaceholderDouble.of(config.getString(new String[]{"targetzoffset", "tzo", "tz"}, "0", new String[0]));
      this.directionTowardsTarget = config.getBoolean(new String[]{"directiontowardstarget", "dtt"}, false);
      this.matchCasterDirection = config.getBoolean(new String[]{"matchcasterdirection", "matchdirection", "mcd", "mpd", "md", "direction"}, false);
      this.fromOrigin = config.getBoolean(new String[]{"formOrigin"}, false);
      this.hitLimit = PlaceholderInt.of(config.getString(new String[]{"hitLimit", "hitcount", "hl"}, "-1", new String[0]));
      this.hitConditionString = config.getString(new String[]{"hitconditions", "conditions", "cond", "c", "hc", "oc"}, null, new String[0]);
      if (this.hitConditionString != null) {
         this.hitConditions = MythicBukkit.inst().getSkillManager().getConditions(this.hitConditionString);
      }

      this.onStartSkillName = PlaceholderString.of(config.getString(new String[]{"onstartskill", "onstart", "os"}, "", new String[0]));
      this.onEndSkillName = PlaceholderString.of(config.getString(new String[]{"onendskill", "onend", "oe"}, "", new String[0]));
      this.onPointSkillName = PlaceholderString.of(config.getString(new String[]{"onpointskill", "onpoint", "op"}, "", new String[0]));
      this.onHitEntitySkillName = PlaceholderString.of(config.getString(new String[]{"onHitEntitySkill", "onhitentity", "ohe", "oh"}, "", new String[0]));
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      return this.castAtLocation(data, target.getLocation());
   }

   public boolean castAtLocation(final SkillMetadata data, AbstractLocation target) {
      int pointCount = (int)this.points.get(data);
      int totalDuration = this.duration.get(data);
      double arcAngle = this.angle.get(data);
      double w = this.width.get(data);
      double h = this.height.get(data);
      final double r = this.radius.get(data);
      final int maxHits = this.hitLimit.get(data);
      final Set<UUID> alreadyHit = new HashSet<>();
      final AtomicInteger currentHits = new AtomicInteger(0);
      List<AbstractLocation> pointsList = new ArrayList<>();
      AbstractLocation origin = this.fromOrigin ? data.getOrigin().clone() : target.clone();
      AbstractVector direction = origin.getDirection().normalize();
      origin.add(direction.multiply(this.forwardOffset.get(data)));
      origin.add(this.xOffset.get(data), this.yOffset.get(data), this.zOffset.get(data));
      AbstractVector rotation = this.getRotation(data, target, origin);

      for (int i = 0; i < pointCount; i++) {
         double theta = Math.toRadians(-arcAngle / 2.0 + arcAngle * i / (pointCount - 1));
         double x = w * Math.sin(theta);
         double z = h * Math.cos(theta);
         AbstractVector rawVector = new AbstractVector(x, 0.0, z);
         rawVector = this.rotateAroundAxisZ(rawVector, rotation.getZ());
         rawVector = this.rotateAroundAxisX(rawVector, rotation.getX());
         rawVector = this.rotateAroundAxisY(rawVector, -rotation.getY());
         AbstractLocation point = origin.clone().add(rawVector);
         pointsList.add(point);
      }

      final Optional<Skill> onStartSkill = this.getSkill(this.onStartSkillName.get(data));
      final Optional<Skill> onEndSkill = this.getSkill(this.onEndSkillName.get(data));
      final Skill onPointSkill = this.getSkill(this.onPointSkillName.get(data)).orElse(null);
      final Skill onHitEntitySkill = this.getSkill(this.onHitEntitySkillName.get(data)).orElse(null);
      if (totalDuration > 0) {
         final List<List<AbstractLocation>> steps = this.splitList(pointsList, totalDuration);
         final AtomicInteger stepIndex = new AtomicInteger(0);
         (new BukkitRunnable() {
               public void run() {
                  int index = stepIndex.getAndIncrement();
                  if (index >= steps.size()) {
                     this.cancel();
                  } else {
                     List<AbstractLocation> current = steps.get(index);
                     if (index == 0 && onStartSkill.isPresent() && !current.isEmpty()) {
                        AbstractLocation start = current.get(0);
                        onStartSkill.get().execute(data.deepClone().setLocationTarget(start).setOrigin(start));
                     }

                     for (AbstractLocation point : current) {
                        if (onPointSkill != null) {
                           onPointSkill.execute(data.deepClone().setLocationTarget(point).setOrigin(point));
                        }

                        if (onHitEntitySkill != null && (maxHits < 0 || currentHits.get() < maxHits)) {
                           for (AbstractEntity entity : SlachMechanic.this.getNearByEntities(point, r, data)
                              .stream()
                              .filter(e -> !alreadyHit.contains(e.getUniqueId()))
                              .filter(e -> SlachMechanic.this.entityPassesConditions(data.getCaster().getEntity(), e))
                              .collect(Collectors.toList())) {
                              if (maxHits >= 0 && currentHits.get() >= maxHits) {
                                 break;
                              }

                              alreadyHit.add(entity.getUniqueId());
                              SkillMetadata clone = data.deepClone();
                              clone.setEntityTargets(Collections.singleton(entity));
                              clone.setOrigin(point);
                              onHitEntitySkill.execute(clone);
                              currentHits.incrementAndGet();
                           }
                        }
                     }

                     if (index == steps.size() - 1 && onEndSkill.isPresent() && !current.isEmpty()) {
                        AbstractLocation end = current.get(current.size() - 1);
                        onEndSkill.get().execute(data.deepClone().setLocationTarget(end).setOrigin(end));
                     }
                  }
               }
            })
            .runTaskTimer(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), 0L, 1L);
      } else {
         if (onStartSkill.isPresent() && !pointsList.isEmpty()) {
            AbstractLocation first = pointsList.get(0);
            onStartSkill.get().execute(data.deepClone().setLocationTarget(first).setOrigin(first));
         }

         for (AbstractLocation point : pointsList) {
            if (onPointSkill != null) {
               onPointSkill.execute(data.deepClone().setLocationTarget(point).setOrigin(point));
            }

            if (onHitEntitySkill != null && (maxHits < 0 || currentHits.get() < maxHits)) {
               for (AbstractEntity entity : this.getNearByEntities(point, r, data)
                  .stream()
                  .filter(e -> !alreadyHit.contains(e.getUniqueId()))
                  .filter(e -> this.entityPassesConditions(data.getCaster().getEntity(), e))
                  .collect(Collectors.toSet())) {
                  if (maxHits >= 0 && currentHits.get() >= maxHits) {
                     break;
                  }

                  alreadyHit.add(entity.getUniqueId());
                  SkillMetadata clone = data.deepClone();
                  clone.setEntityTargets(Collections.singleton(entity));
                  clone.setOrigin(point);
                  onHitEntitySkill.execute(clone);
                  currentHits.incrementAndGet();
               }
            }
         }

         if (onEndSkill.isPresent() && !pointsList.isEmpty()) {
            AbstractLocation last = pointsList.get(pointsList.size() - 1);
            onEndSkill.get().execute(data.deepClone().setLocationTarget(last).setOrigin(last));
         }
      }

      return SkillResult.SUCCESS;
   }

   private Optional<Skill> getSkill(String name) {
      return name != null && !name.isEmpty() ? MythicBukkit.inst().getSkillManager().getSkill(name) : Optional.empty();
   }

   private Collection<AbstractEntity> getNearByEntities(AbstractLocation location, double radius, SkillMetadata data) {
      Set<AbstractEntity> result = new HashSet<>();
      BoundingBox box = BoundingBox.of(BukkitAdapter.adapt(location), radius, radius, radius);

      for (AbstractEntity entity : MythicBukkit.inst().getVolatileCodeHandler().getWorldHandler().getEntitiesNearLocation(location, radius)) {
         if (!entity.getUniqueId().equals(data.getCaster().getEntity().getUniqueId()) && box.overlaps(entity.getBukkitEntity().getBoundingBox())) {
            result.add(entity);
         }
      }

      return result;
   }

   private AbstractVector getRotation(SkillMetadata data, AbstractLocation target, AbstractLocation origin) {
      if (this.directionTowardsTarget && target != null) {
         AbstractLocation offsetTarget = target.clone().add(this.targetxOffset.get(data), this.targetyOffset.get(data), this.targetzOffset.get(data));
         AbstractVector delta = offsetTarget.toVector().subtract(origin.toVector());
         double yaw = Math.toDegrees(Math.atan2(delta.getZ(), delta.getX())) - 90.0;
         double horizontal = Math.sqrt(delta.getX() * delta.getX() + delta.getZ() * delta.getZ());
         double pitch = Math.toDegrees(Math.atan2(-delta.getY(), horizontal));
         double rollValue = this.roll.get(data);
         return new AbstractVector(pitch, yaw, rollValue);
      }

      if (this.matchCasterDirection) {
         float yaw = origin.getYaw();
         float pitch = origin.getPitch();
         float adjustedPitch = pitch;
         double rollValue = this.roll.get(data);
         return new AbstractVector(adjustedPitch, -yaw, rollValue);
      }

      double x = 0.0;
      double y = 0.0;
      double z = 0.0;
      String raw = this.rotationString.get(data);
      if (raw != null && raw.contains(",")) {
         try {
            String[] parts = raw.split(",");
            if (parts.length == 3) {
               x = Double.parseDouble(parts[0]);
               y = Double.parseDouble(parts[1]);
               z = Double.parseDouble(parts[2]);
            }
         } catch (Exception var17) {
         }
      }

      double px = this.pitch.get(data);
      double py = this.yaw.get(data);
      double pz = this.roll.get(data);
      if (px != 0.0) {
         x = px;
      }

      if (py != 0.0) {
         y = py;
      }

      if (pz != 0.0) {
         z = pz;
      }

      return new AbstractVector(x, y, z);
   }

   private AbstractVector rotateAroundAxisX(AbstractVector v, double angle) {
      angle = Math.toRadians(angle);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double y = v.getY() * cos - v.getZ() * sin;
      double z = v.getY() * sin + v.getZ() * cos;
      return new AbstractVector(v.getX(), y, z);
   }

   private AbstractVector rotateAroundAxisY(AbstractVector v, double angle) {
      angle = Math.toRadians(angle);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double x = v.getX() * cos + v.getZ() * sin;
      double z = -v.getX() * sin + v.getZ() * cos;
      return new AbstractVector(x, v.getY(), z);
   }

   private AbstractVector rotateAroundAxisZ(AbstractVector v, double angle) {
      angle = Math.toRadians(angle);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double x = v.getX() * cos - v.getY() * sin;
      double y = v.getX() * sin + v.getY() * cos;
      return new AbstractVector(x, y, v.getZ());
   }

   private <T> List<List<T>> splitList(List<T> list, int parts) {
      List<List<T>> result = new ArrayList<>();
      int size = list.size();
      int chunkSize = size / parts;
      int remainder = size % parts;
      int start = 0;

      for (int i = 0; i < parts; i++) {
         int end = start + chunkSize + (i < remainder ? 1 : 0);
         result.add(new ArrayList<>(list.subList(start, end)));
         start = end;
      }

      return result;
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
}
