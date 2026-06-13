package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonArrayPushMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final String json;
   private final PlaceholderString key;
   private final PlaceholderString value;
   private final PlaceholderString jsonVariable;

   public JsonArrayPushMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.json = config.getString(new String[]{"json"});
      this.jsonVariable = PlaceholderString.of(this.json);
      this.key = PlaceholderString.of(config.getString(new String[]{"key", "k"}));
      this.value = PlaceholderString.of(config.getString(new String[]{"value", "val", "v"}));
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      String keyPath = this.key.get(data);
      String value = this.value.get(data);
      String jsonStr = this.jsonVariable.get(data);

      try {
         Object root = jsonStr.trim().startsWith("[") ? new JSONArray(jsonStr) : new JSONObject(jsonStr);
         if (!this.pushToArray(root, keyPath, value)) {
            return SkillResult.FAILURE;
         }

         String updateJson = root instanceof JSONArray ? ((JSONArray)root).toString() : ((JSONObject)root).toString();
         VariableUtil.setScopedVariable(this.json, updateJson, data, target);
         return SkillResult.SUCCESS;
      } catch (JSONException e) {
         return SkillResult.FAILURE;
      }
   }

   private boolean pushToArray(Object json, String path, String valueToAdd) throws JSONException {
      String[] tokens = path.split("\\.");
      Object current = json;

      for (int i = 0; i < tokens.length; i++) {
         String token = tokens[i];
         String field = token.replaceAll("\\[.*?\\]", "");
         String[] indices = token.split("\\[");
         if (current instanceof JSONObject && !field.isEmpty()) {
            current = ((JSONObject)current).opt(field);
            if (current == null) {
               return SkillResult.FAILURE;
            }
         }

         for (int j = field.isEmpty() ? 0 : 1; j < indices.length; j++) {
            if (!(current instanceof JSONArray)) {
               return SkillResult.FAILURE;
            }

            int index = Integer.parseInt(indices[j].replace("]", ""));
            current = ((JSONArray)current).opt(index);
            if (current == null) {
               return SkillResult.FAILURE;
            }
         }

         if (current instanceof JSONArray && field.isEmpty()) {
            String[] var13 = indices;
            int var12 = indices.length;

            for (int var16 = 0; var16 < var12; var16++) {
               String part = var13[var16];
               if (!part.isEmpty()) {
                  int index = Integer.parseInt(part.replace("]", ""));
                  current = ((JSONArray)current).opt(index);
                  if (current == null) {
                     return SkillResult.FAILURE;
                  }
               }
            }
         }
      }

      if (current instanceof JSONArray) {
         ((JSONArray)current).put(valueToAdd);
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }
}
