package net.azisaba.lifemoremythicmobs.targeters;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.api.skills.targeters.IEntitySelector;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayersInRadiusLimitVariableTargeter extends IEntitySelector {
   private final PlaceholderString radiusStr;
   private final PlaceholderString limitStr;

   public PlayersInRadiusLimitVariableTargeter(MythicLineConfig config) {
      super(config);
      this.radiusStr = PlaceholderString.of(config.getString(new String[]{"radius", "r"}, "5", new String[0]));
      this.limitStr = PlaceholderString.of(config.getString(new String[]{"limit", "l"}, null, new String[0]));
   }

   public HashSet<AbstractEntity> getEntities(SkillMetadata data) {
      AbstractLocation origin = data.getCaster().getLocation();
      Location center = BukkitAdapter.adapt(origin);
      double radius = this.parseDouble(this.radiusStr.get(data));
      int limit = this.parseLimit(this.limitStr != null ? this.limitStr.get(data) : null);
      List<Player> nearbyPlayers = center.getWorld()
         .getNearbyEntities(center, radius, radius, radius)
         .stream()
         .filter(e -> e instanceof Player)
         .map(e -> (Player)e)
         .filter(px -> px.getGameMode() != GameMode.CREATIVE && px.getGameMode() != GameMode.SPECTATOR)
         .collect(Collectors.toList());
      if (limit > 0 && nearbyPlayers.size() > limit) {
         Collections.shuffle(nearbyPlayers);
         nearbyPlayers = nearbyPlayers.subList(0, limit);
      }

      HashSet<AbstractEntity> result = new HashSet<>();

      for (Player p : nearbyPlayers) {
         result.add(BukkitAdapter.adapt(p));
      }

      return result;
   }

   private double parseDouble(String str) {
      try {
         return Double.parseDouble(str);
      } catch (Exception e) {
         return 5.0;
      }
   }

   private int parseLimit(String str) {
      if (str == null) {
         return -1;
      }

      try {
         return Integer.parseInt(str);
      } catch (Exception e) {
         return -1;
      }
   }
}
