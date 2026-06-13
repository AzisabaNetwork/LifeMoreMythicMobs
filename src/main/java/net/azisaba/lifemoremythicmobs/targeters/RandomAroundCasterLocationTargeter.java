package net.azisaba.lifemoremythicmobs.targeters;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.api.skills.targeters.ILocationSelector;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;

public class RandomAroundCasterLocationTargeter extends ILocationSelector {
   private final PlaceholderString radiusArg;
   private final PlaceholderString countArg;
   private final PlaceholderString yOffsetArg;

   public RandomAroundCasterLocationTargeter(MythicLineConfig config) {
      super(config);
      this.radiusArg = PlaceholderString.of(config.getString(new String[]{"radius", "r"}, "5", new String[0]));
      this.countArg = PlaceholderString.of(config.getString(new String[]{"count", "c", "n", "amount", "a"}, "1", new String[0]));
      this.yOffsetArg = PlaceholderString.of(config.getString(new String[]{"yoffset", "yo", "y"}, "1", new String[0]));
   }

   public HashSet<AbstractLocation> getLocations(SkillMetadata data) {
      HashSet<AbstractLocation> out = new HashSet<>();
      Location base = BukkitAdapter.adapt(data.getCaster().getLocation());
      World world = base.getWorld();
      if (world == null) {
         return out;
      }

      double radius = parseDoubleSafe(this.radiusArg.get(data), 5.0);
      if (radius < 0.0) {
         radius = 0.0;
      }

      int count = (int)Math.max(0L, Math.round(parseDoubleSafe(this.countArg.get(data), 1.0)));
      if (count > 256) {
         count = 256;
      }

      double yOffset = parseDoubleSafe(this.yOffsetArg.get(data), 1.0);
      double fixedY = base.getY() + yOffset;
      ThreadLocalRandom rnd = ThreadLocalRandom.current();

      for (int i = 0; i < count; i++) {
         double theta = rnd.nextDouble(0.0, Math.PI * 2);
         double u = rnd.nextDouble();
         double r = Math.sqrt(u) * radius;
         double x = base.getX() + r * Math.cos(theta);
         double z = base.getZ() + r * Math.sin(theta);
         Location loc = new Location(world, x, fixedY, z);
         out.add(BukkitAdapter.adapt(loc));
      }

      return out;
   }

   private static double parseDoubleSafe(String s, double def) {
      if (s == null) {
         return def;
      }

      try {
         return Double.parseDouble(s.trim());
      } catch (Exception ignored) {
         return def;
      }
   }
}
