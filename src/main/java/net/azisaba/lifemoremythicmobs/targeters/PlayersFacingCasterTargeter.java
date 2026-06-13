package net.azisaba.lifemoremythicmobs.targeters;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.targeters.IEntitySelector;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayersFacingCasterTargeter extends IEntitySelector {
   private final double radius;
   private final double fov;
   private static final double MAX_DISTANCE_TO_CASTER = 100.0;

   public PlayersFacingCasterTargeter(MythicLineConfig config) {
      super(config);
      this.radius = config.getDouble("radius", 10.0);
      this.fov = config.getDouble("fov", 90.0);
   }

   public HashSet<AbstractEntity> getEntities(SkillMetadata data) {
      HashSet<AbstractEntity> result = new HashSet<>();
      AbstractEntity caster = data.getCaster().getEntity();
      Location casterLoc = BukkitAdapter.adapt(caster.getLocation());

      for (Player player : Bukkit.getOnlinePlayers()) {
         if (player.isOnline()
            && !player.isDead()
            && player.getWorld().equals(casterLoc.getWorld())
            && !(player.getLocation().distanceSquared(casterLoc) > this.radius * this.radius)
            && !(player.getLocation().distanceSquared(casterLoc) > 10000.0)) {
            Location playerLoc = player.getLocation();
            float playerYaw = playerLoc.getYaw();
            Location toCaster = casterLoc.clone().subtract(playerLoc);
            double dx = toCaster.getX();
            double dz = toCaster.getZ();
            double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
            double deltaYaw = this.wrapAngleTo180(targetYaw - playerYaw);
            if (Math.abs(deltaYaw) <= this.fov / 2.0) {
               result.add(BukkitAdapter.adapt(player));
            }
         }
      }

      return result;
   }

   private double wrapAngleTo180(double angle) {
      angle %= 360.0;
      if (angle >= 180.0) {
         angle -= 360.0;
      }

      if (angle < -180.0) {
         angle += 360.0;
      }

      return angle;
   }
}
