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

public class JsonSetterMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final String json;
   private final PlaceholderString key;
   private final PlaceholderString value;
   private final PlaceholderString jsonVariable;

   public JsonSetterMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.json = config.getString(new String[]{"json"});
      this.jsonVariable = PlaceholderString.of(this.json);
      this.key = PlaceholderString.of(config.getString(new String[]{"key", "k"}));
      this.value = PlaceholderString.of(config.getString(new String[]{"value", "val", "v"}));
   }

   public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
      String key = this.key.get(skillMetadata);
      String value = this.value.get(skillMetadata);
      String jsonStr = this.jsonVariable.get(skillMetadata);
      String scopedJsonVar = this.json;
      if (scopedJsonVar.startsWith("<") && scopedJsonVar.endsWith(">")) {
         scopedJsonVar = scopedJsonVar.substring(1, scopedJsonVar.length() - 1);
      }

      if (!scopedJsonVar.matches("^(caster|target|skill|global)\\.var\\..+$")) {
         return SkillResult.FAILURE;
      }

      try {
         Object root = jsonStr.trim().startsWith("[") ? new JSONArray(jsonStr) : new JSONObject(jsonStr);
         if (!this.updatePath(root, key, value)) {
            return SkillResult.FAILURE;
         }

         String updateJson = root instanceof JSONArray ? ((JSONArray)root).toString() : ((JSONObject)root).toString();
         VariableUtil.setScopedVariable(scopedJsonVar, updateJson, skillMetadata, abstractEntity);
         return SkillResult.SUCCESS;
      } catch (JSONException e) {
         return SkillResult.FAILURE;
      }
   }

   private boolean updatePath(Object json, String path, String newValue) throws JSONException {
      String[] tokens = path.split("\\.");
      Object current = json;

      for (int i = 0; i < tokens.length - 1; i++) {
         String token = tokens[i];
         current = this.resolveToken(current, token);
         if (current == null) {
            return SkillResult.FAILURE;
         }
      }

      String lastToken = tokens[tokens.length - 1];
      return this.setValueAtPath(current, lastToken, newValue);
   }

   private Object resolveToken(Object current, String token) throws JSONException {
      String field = token.replaceAll("\\[.*?\\]", "");
      String[] indices = token.split("\\[");
      if (current instanceof JSONObject) {
         current = ((JSONObject)current).opt(field);
      }

      for (int i = 1; i < indices.length; i++) {
         if (!(current instanceof JSONArray)) {
            return null;
         }

         int index = Integer.parseInt(indices[i].replace("]", ""));
         current = ((JSONArray)current).opt(index);
      }

      return current;
   }

   private boolean setValueAtPath(Object current, String token, String value) throws JSONException {
      String field = token.replaceAll("\\[.*?\\]", "");
      String[] parts = token.split("\\[");
      if (current instanceof JSONObject) {
         JSONObject jsonObj = (JSONObject)current;
         if (parts.length == 1) {
            jsonObj.put(field, value);
            return SkillResult.SUCCESS;
         }

         Object inner = jsonObj.opt(field);

         for (int i = 1; i < parts.length - 1; i++) {
            if (!(inner instanceof JSONArray)) {
               return SkillResult.FAILURE;
            }

            int idx = Integer.parseInt(parts[i].replace("]", ""));
            inner = ((JSONArray)inner).opt(idx);
         }

         if (inner instanceof JSONArray) {
            int idx = Integer.parseInt(parts[parts.length - 1].replace("]", ""));
            ((JSONArray)inner).put(idx, value);
            return SkillResult.SUCCESS;
         }
      } else if (current instanceof JSONArray) {
         JSONArray arr = (JSONArray)current;

         for (int i = 0; i < parts.length - 1; i++) {
            int idx = Integer.parseInt(parts[i].replaceAll("[^\\d]", ""));
            current = arr.opt(idx);
            if (!(current instanceof JSONArray)) {
               return SkillResult.FAILURE;
            }

            arr = (JSONArray)current;
         }

         int lastIdx = Integer.parseInt(parts[parts.length - 1].replace("]", ""));
         arr.put(lastIdx, value);
         return SkillResult.SUCCESS;
      }

      return SkillResult.FAILURE;
   }
}
