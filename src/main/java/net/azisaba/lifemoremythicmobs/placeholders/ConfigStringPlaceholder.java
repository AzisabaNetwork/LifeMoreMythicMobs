package net.azisaba.lifemoremythicmobs.placeholders;

import io.lumine.mythic.api.skills.placeholders.Placeholder;
import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import java.util.ArrayList;
import java.util.List;

public class ConfigStringPlaceholder {
   private static final String NAME = "cfg_str";

   private ConfigStringPlaceholder() {
   }

   public static void register(PlaceholderManager manager) {
      manager.register("cfg_str", Placeholder.meta((meta, arg) -> {
         if (arg == null) {
            return "";
         } else {
            String raw = arg.trim();
            if (raw.isEmpty()) {
               return "";
            } else {
               return raw.indexOf(46) >= 0 ? raw : toYamlPathFromUnderscore(raw);
            }
         }
      }));
   }

   private static String toYamlPathFromUnderscore(String raw) {
      List<String> parts = new ArrayList<>();
      StringBuilder cur = new StringBuilder();

      for (int i = 0; i < raw.length(); i++) {
         char c = raw.charAt(i);
         if (c == '_') {
            if (i + 1 < raw.length() && raw.charAt(i + 1) == '_') {
               cur.append('_');
               i++;
            } else {
               parts.add(cur.toString());
               cur.setLength(0);
            }
         } else {
            cur.append(c);
         }
      }

      parts.add(cur.toString());
      StringBuilder path = new StringBuilder();

      for (int i = 0; i < parts.size(); i++) {
         if (i > 0) {
            path.append('_');
         }

         path.append(parts.get(i));
      }

      return path.toString();
   }
}
