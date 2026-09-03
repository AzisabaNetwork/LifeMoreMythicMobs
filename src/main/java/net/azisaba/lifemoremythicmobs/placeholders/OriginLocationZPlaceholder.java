package net.azisaba.lifemoremythicmobs.placeholders;

import net.azisaba.lifemoremythicmobs.util.PlaceholderFactory;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;

public class OriginLocationZPlaceholder {
   public static void register(PlaceholderManager manager) {
      manager.register("origin_l_z", PlaceholderFactory.meta((placeholderMeta, s) -> {
         if (!(placeholderMeta instanceof SkillMetadata)) {
            return "0";
         }

         SkillMetadata data = (SkillMetadata)placeholderMeta;
         AbstractLocation origin = data.getOrigin();
         if (origin == null) {
            return "null";
         }

         double z = origin.getZ();
         return String.valueOf(z);
      }));
   }
}

