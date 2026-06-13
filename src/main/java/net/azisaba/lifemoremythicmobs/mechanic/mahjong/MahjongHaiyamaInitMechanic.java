package net.azisaba.lifemoremythicmobs.mechanic.mahjong;

import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.json.JSONArray;

public class MahjongHaiyamaInitMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final String varName;
   private final Map<String, PlaceholderString> akahaiVariable;

   public MahjongHaiyamaInitMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.varName = config.getString("var", "");
      this.akahaiVariable = new HashMap<>();
      String[] var5;
      int var4 = (var5 = new String[]{"m", "p", "s"}).length;

      for (int var3 = 0; var3 < var4; var3++) {
         String s = var5[var3];
         String key = "akahai_" + s;
         this.akahaiVariable.put(s, PlaceholderString.of(config.getString(key, "0")));
      }
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      List<String> hai = new ArrayList<>();
      String scopedValueKey = this.varName;
      if (scopedValueKey.startsWith("<") && scopedValueKey.endsWith(">")) {
         scopedValueKey = scopedValueKey.substring(1, scopedValueKey.length() - 1);
      }

      if (!scopedValueKey.matches("^(caster|target|skill|global)\\.var\\..+$")) {
         return SkillResult.FAILURE;
      }

      for (String s : Arrays.asList("m", "p", "s", "z")) {
         int maxNum = s.equals("z") ? 7 : 9;
         int redCount = 0;
         if (this.akahaiVariable.containsKey(s)) {
            try {
               String eval = this.akahaiVariable.get(s).get(data, target);
               if (eval.startsWith("<") && eval.endsWith(">")) {
                  eval = eval.substring(1, eval.length() - 1);
               }

               PlaceholderString inner = PlaceholderString.of(eval);
               String evaluated = inner.get(data, target);
               redCount = Integer.parseInt(evaluated);
            } catch (NumberFormatException e) {
               redCount = 0;
            }
         }

         for (int n = 1; n <= maxNum; n++) {
            for (int i = 0; i < 4; i++) {
               if (!s.equals("z") && n == 5 && i < redCount) {
                  hai.add(s + "0");
               } else {
                  hai.add(s + n);
               }
            }
         }
      }

      List<String> yama = new ArrayList<>();
      Random random = new Random();

      while (!hai.isEmpty()) {
         int idx = random.nextInt(hai.size());
         yama.add(hai.remove(idx));
      }

      JSONArray jsonArray = new JSONArray(yama);
      String result = jsonArray.toString();
      VariableUtil.setScopedVariable(scopedValueKey, result, data, target);
      return SkillResult.SUCCESS;
   }
}
