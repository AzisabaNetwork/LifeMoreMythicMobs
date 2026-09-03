package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.core.skills.SkillExecutor;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.core.skills.mechanics.ParticleEffect;
import io.lumine.mythic.api.skills.placeholders.PlaceholderFloat;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.core.utils.MythicUtil;
import io.lumine.mythic.bukkit.utils.Schedulers;
import io.lumine.mythic.bukkit.utils.serialize.Chroma;
import java.awt.Color;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Particle.DustOptions;
import org.bukkit.plugin.Plugin;

public class GradientParticleEffectMechanic extends ParticleEffect implements ITargetedEntitySkill, ITargetedLocationSkill {
   private final PlaceholderInt durationTicks;
   private final PlaceholderInt intervalTicks;
   private final boolean gradientEnabled;
   private final String gradientTo;
   private final PlaceholderFloat dustSize;

   public GradientParticleEffectMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY;
      this.durationTicks = PlaceholderInt.of(config.getString(new String[]{"duration", "d"}, "20", new String[0]));
      this.intervalTicks = PlaceholderInt.of(config.getString(new String[]{"interval", "i"}, "1", new String[0]));
      this.gradientEnabled = config.getBoolean(new String[]{"dradient", "grad"}, false);
      this.gradientTo = config.getString(new String[]{"grandientto", "gradto", "to"}, "black", new String[0]).toLowerCase();
      this.dustSize = PlaceholderFloat.of(config.getString(new String[]{"size"}, "1.0f", new String[0]));
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      Collection<AbstractPlayer> audienceEntities = this.audience.get(data, null);
      this.playEffectTimed(data, target, audienceEntities);
      return SkillResult.SUCCESS;
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      AbstractLocation startLocation = this.useEyeLocation ? target.getEyeLocation() : target.getLocation();
      Collection<AbstractPlayer> audienceEntities = this.audience.get(data, target);
      this.playEffectTimed(data, startLocation, audienceEntities);
      return SkillResult.SUCCESS;
   }

   private void playEffectTimed(final SkillMetadata data, AbstractLocation target, final Collection<AbstractPlayer> audience) {
      AbstractLocation base = target.clone();
      if (this.setYaw) {
         base.setYaw(this.yaw);
      }

      if (this.setPitch) {
         base.setPitch(this.pitch);
      }

      float forward = this.startForwardOffset;
      if (forward != 0.0F) {
         base = MythicUtil.move(base, forward, 0.0, 0.0);
      }

      float side = this.startSideOffset.get(data);
      if (side != 0.0F) {
         base = MythicUtil.move(base, 0.0, 0.0, side);
      }

      int dur = Math.max(0, this.durationTicks.get(data));
      int interval = Math.max(1, this.intervalTicks.get(data));
      if (dur <= 0) {
         this.playOnceWithColor(data, base, audience, resolveColor(this.color.get(data)), this.dustSize.get(data));
      } else {
         final int steps = Math.max(1, (dur + interval - 1) / interval);
         final Color start = resolveColor(this.color.get(data));
         final Color end = resolveEndColor(this.gradientTo);
         final float size = this.dustSize.get(data);
         Plugin plugin = MythicBukkit.inst();
         final AbstractLocation finalBase = base;
         final AtomicInteger tid = new AtomicInteger(-1);
         int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int step = 0;

            @Override
            public void run() {
               if (this.step >= steps) {
                  Bukkit.getScheduler().cancelTask(tid.get());
               } else {
                  Color c = start;
                  if (GradientParticleEffectMechanic.this.gradientEnabled && start != null) {
                     double t = steps <= 1 ? 1.0 : (double)this.step / (steps - 1);
                     c = GradientParticleEffectMechanic.lerp(start, end, t);
                  }

                  GradientParticleEffectMechanic.this.playOnceWithColor(data, finalBase, audience, c, size);
                  this.step++;
               }
            }
         }, 0L, interval);
         tid.set(id);
      }
   }

   private void playOnceWithColor(SkillMetadata data, AbstractLocation target, Collection<AbstractPlayer> audience, Color colorOrNull, float dustSize) {
      AbstractLocation ln = target.clone().add(0.0, this.yOffset.get(data), 0.0);
      if (this.directional) {
         super.playDirectionalParticleEffect(data, data.getOrigin(), target, audience);
      } else {
         if (!this.isMob) {
            if (this.particleData != null) {
               if (this.particleData instanceof DustOptions && colorOrNull != null) {
                  DustOptions dust = new DustOptions(org.bukkit.Color.fromRGB(colorOrNull.getRed(), colorOrNull.getGreen(), colorOrNull.getBlue()), dustSize);
                  this.particle.send(audience, ln, this.pSpeed.get(data), this.amount.get(data), this.xSpread.get(data), this.vSpread.get(data), this.zSpread.get(data), dust, this.exactOffsets);
               } else {
                  this.particle
                     .send(audience, ln, this.pSpeed.get(data), this.amount.get(data), this.xSpread.get(data), this.vSpread.get(data), this.zSpread.get(data), this.particleData, this.exactOffsets);
               }
            } else if (colorOrNull != null) {
               this.particle.sendLegacyColored(audience, ln, this.pSpeed.get(data), this.amount.get(data), this.xSpread.get(data), this.vSpread.get(data), this.zSpread.get(data), Chroma.of(colorOrNull.getRed(), colorOrNull.getGreen(), colorOrNull.getBlue()));
            } else {
               this.particle.send(audience, ln, this.pSpeed.get(data), this.amount.get(data), this.xSpread.get(data), this.vSpread.get(data), this.zSpread.get(data));
            }
         } else {
            ln.add(
               Math.random() * this.xSpread.get(data) * 2.0 - this.xSpread.get(data),
               Math.random() * this.vSpread.get(data) * 2.0 - this.vSpread.get(data),
               Math.random() * this.zSpread.get(data) * 2.0 - this.zSpread.get(data)
            );
            Schedulers.sync().run(() -> MythicBukkit.inst().getMobManager().spawnMob(this.strMob, ln));
         }
      }
   }

   private static Color resolveEndColor(String to) {
      return "white".equalsIgnoreCase(to) ? Color.WHITE : Color.BLACK;
   }

   private static Color resolveColor(String value) {
      if (value == null || value.isBlank()) return null;
      try {
         String v = value.trim();
         if (v.startsWith("#")) return Color.decode(v);
         java.lang.reflect.Field field = Color.class.getField(v.toUpperCase());
         return (Color) field.get(null);
      } catch (Exception ignored) {
         return null;
      }
   }

   private static Color lerp(Color a, Color b, double t) {
      t = Math.max(0.0, Math.min(1.0, t));
      int r = (int)Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
      int g = (int)Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
      int bl = (int)Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
      return new Color(clamp255(r), clamp255(g), clamp255(bl));
   }

   private static int clamp255(int v) {
      return Math.max(0, Math.min(255, v));
   }
}



