package net.azisaba.lifemoremythicmobs.placeholders;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.Placeholder;
import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;

public class OriginLocationXPlaceholder {
   public static void register(PlaceholderManager manager) {
      manager.register("origin_l_x", Placeholder.meta((placeholderMeta, s) -> {
         if (!(placeholderMeta instanceof SkillMetadata)) {
            return "0";
         }

         SkillMetadata data = (SkillMetadata)placeholderMeta;
         AbstractLocation origin = data.getOrigin();
         if (origin == null) {
            return "null";
         }

         double x = origin.getX();
         return String.valueOf(x);
      }));
   }
}
