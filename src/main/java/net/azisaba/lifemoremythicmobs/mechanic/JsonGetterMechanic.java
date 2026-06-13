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

public class JsonGetterMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString json;
   private final PlaceholderString key;
   private final String value;

   public JsonGetterMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.json = PlaceholderString.of(config.getString(new String[]{"json"}));
      this.key = PlaceholderString.of(config.getString(new String[]{"key", "k"}));
      this.value = config.getString(new String[]{"value", "val", "v"});
   }

   public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
      String key = this.key.get(skillMetadata);
      String json = this.json.get(skillMetadata);
      String scopedValueKey = this.value;
      if (scopedValueKey.startsWith("<") && scopedValueKey.endsWith(">")) {
         scopedValueKey = scopedValueKey.substring(1, scopedValueKey.length() - 1);
      }

      if (!scopedValueKey.matches("^(caster|target|skill|global)\\.var\\..+$")) {
         return SkillResult.FAILURE;
      }

      Object result = null;

      try {
         if (json.trim().startsWith("{")) {
            JSONObject jsonObject = new JSONObject(json);
            result = this.resolvePath(jsonObject, key);
         } else {
            JSONArray jsonArray = new JSONArray(json);
            result = this.resolvePath(jsonArray, key);
         }
      } catch (JSONException e) {
         return SkillResult.FAILURE;
      }

      if (result != null) {
         VariableUtil.setScopedVariable(scopedValueKey, result.toString(), skillMetadata, abstractEntity);
         return SkillResult.SUCCESS;
      } else {
         VariableUtil.setScopedVariable(scopedValueKey, "", skillMetadata, abstractEntity);
         return SkillResult.FAILURE;
      }
   }

   private Object resolvePath(Object json, String path) throws JSONException {
      String[] tokens = path.split("\\.");
      Object current = json;
      String[] var8 = tokens;
      int var7 = tokens.length;

      for (int var6 = 0; var6 < var7; var6++) {
         String token = var8[var6];
         String field = token.replaceAll("\\[.*?\\]", "");
         String[] indices = token.split("\\[");
         if (current instanceof JSONObject && !field.isEmpty()) {
            current = ((JSONObject)current).opt(field);
            if (current == null) {
               return null;
            }
         }

         for (int i = field.isEmpty() ? 0 : 1; i < indices.length; i++) {
            if (!(current instanceof JSONArray)) {
               return null;
            }

            int index = Integer.parseInt(indices[i].replace("]", ""));
            current = ((JSONArray)current).opt(index);
            if (current == null) {
               return null;
            }
         }

         if (current instanceof JSONArray && field.isEmpty()) {
            String[] var14 = indices;
            int var13 = indices.length;

            for (int var17 = 0; var17 < var13; var17++) {
               String part = var14[var17];
               if (!part.isEmpty()) {
                  int index = Integer.parseInt(part.replace("]", ""));
                  current = ((JSONArray)current).opt(index);
                  if (current == null) {
                     return null;
                  }
               }
            }
         }
      }

      return current;
   }
}
