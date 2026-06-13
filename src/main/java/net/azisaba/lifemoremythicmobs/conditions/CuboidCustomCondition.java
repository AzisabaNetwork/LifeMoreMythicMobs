package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.IEntityLocationComparisonCondition;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.skills.SkillCondition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class CuboidCustomCondition extends SkillCondition implements IEntityLocationComparisonCondition {
   private final PlaceholderString p1Raw;
   private final PlaceholderString p2Raw;
   private final String worldRaw;

   public CuboidCustomCondition(String line, MythicLineConfig config) {
      super(line);
      this.p1Raw = PlaceholderString.of(config.getString(new String[]{"point1", "p1"}, "0,0,0"));
      this.p2Raw = PlaceholderString.of(config.getString(new String[]{"point2", "p2"}, "0,0,0"));
      this.worldRaw = config.getString("world", null);
   }

   @Override
   public boolean check(AbstractEntity target, AbstractLocation location, SkillMetadata data) {
      String parsedP1 = this.p1Raw.get(data);
      String parsedP2 = this.p2Raw.get(data);
      World world = this.getTargetWorld(target, data);
      Location loc1 = this.parseLocation(parsedP1, world);
      Location loc2 = this.parseLocation(parsedP2, world);
      Location targetLoc = target.getBukkitEntity().getLocation();
      return loc1 != null && loc2 != null && this.isWithinCuboid(targetLoc, loc1, loc2);
   }

   private Location parseLocation(String input, World world) {
      if (world == null) {
         return null;
      }

      String[] parts = input.split(",");
      if (parts.length != 3) {
         return null;
      }

      try {
         double x = Double.parseDouble(parts[0]);
         double y = Double.parseDouble(parts[1]);
         double z = Double.parseDouble(parts[2]);
         return new Location(world, x, y, z);
      } catch (NumberFormatException e) {
         return null;
      }
   }

   private World getTargetWorld(AbstractEntity target, SkillMetadata data) {
      if (this.worldRaw != null) {
         return Bukkit.getWorld(this.worldRaw);
      } else {
         return target != null ? target.getBukkitEntity().getWorld() : data.getCaster().getEntity().getBukkitEntity().getWorld();
      }
   }

   private boolean isWithinCuboid(Location loc, Location p1, Location p2) {
      double minX = Math.min(p1.getX(), p2.getX());
      double minY = Math.min(p1.getY(), p2.getY());
      double minZ = Math.min(p1.getZ(), p2.getZ());
      double maxX = Math.max(p1.getX(), p2.getX());
      double maxY = Math.max(p1.getY(), p2.getY());
      double maxZ = Math.max(p1.getZ(), p2.getZ());
      return loc.getX() >= minX && loc.getX() <= maxX && loc.getY() >= minY && loc.getY() <= maxY && loc.getZ() >= minZ && loc.getZ() <= maxZ;
   }
}
