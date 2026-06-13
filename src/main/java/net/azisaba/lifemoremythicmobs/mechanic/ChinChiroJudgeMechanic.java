package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Arrays;

public class ChinChiroJudgeMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString[] dice;
   private final PlaceholderString resultVar;
   private final PlaceholderString multiplierVar;

   public ChinChiroJudgeMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.dice = new PlaceholderString[]{
         PlaceholderString.of(config.getString("d1")),
         PlaceholderString.of(config.getString("d2")),
         PlaceholderString.of(config.getString("d3")),
         PlaceholderString.of(config.getString("d4")),
         PlaceholderString.of(config.getString("d5")),
         PlaceholderString.of(config.getString("d6"))
      };
      this.resultVar = PlaceholderString.of(config.getString("resultVar"));
      this.multiplierVar = PlaceholderString.of(config.getString("multiplierVar"));
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      AbstractEntity caster = data.getCaster().getEntity();
      int[] child = new int[3];
      int[] parent = new int[3];

      for (int i = 0; i < 3; i++) {
         child[i] = Integer.parseInt(this.dice[i].get(data));
      }

      for (int i = 0; i < 3; i++) {
         parent[i] = Integer.parseInt(this.dice[i + 3].get(data));
      }

      ChinChiroJudgeMechanic.HandResult childResult = this.evaluate(child);
      ChinChiroJudgeMechanic.HandResult parentResult = this.evaluate(parent);
      String result;
      int multiplier;
      if (childResult.rank < parentResult.rank) {
         result = "win";
         multiplier = childResult.multiplier;
      } else if (childResult.rank > parentResult.rank) {
         result = "lose";
         multiplier = parentResult.multiplier;
      } else if (childResult.strength > parentResult.strength) {
         result = "win";
         multiplier = childResult.multiplier;
      } else {
         result = "lose";
         multiplier = parentResult.multiplier;
      }

      VariableUtil.setScopedVariable(this.resultVar.get(data), result, data, target);
      VariableUtil.setScopedVariable(this.multiplierVar.get(data), String.valueOf(multiplier), data, target);
      return SkillResult.SUCCESS;
   }

   private ChinChiroJudgeMechanic.HandResult evaluate(int[] dice) {
      Arrays.sort(dice);
      int a = dice[0];
      int b = dice[1];
      int c = dice[2];
      if (a == 1 && b == 1 && c == 1) {
         return new ChinChiroJudgeMechanic.HandResult(ChinChiroJudgeMechanic.HandType.PINZORO, 1, 1, 10);
      } else if (a == b && b == c) {
         return new ChinChiroJudgeMechanic.HandResult(ChinChiroJudgeMechanic.HandType.ZOROME, 2, a, 3);
      } else if (Arrays.equals(dice, new int[]{4, 5, 6})) {
         return new ChinChiroJudgeMechanic.HandResult(ChinChiroJudgeMechanic.HandType.SHIGORO, 3, 6, 2);
      } else if (Arrays.equals(dice, new int[]{1, 2, 3})) {
         return new ChinChiroJudgeMechanic.HandResult(ChinChiroJudgeMechanic.HandType.HIFUMI, 6, 1, 2);
      } else if (a == b) {
         return new ChinChiroJudgeMechanic.HandResult(ChinChiroJudgeMechanic.HandType.ARASHIME, 4, c, 1);
      } else {
         return b == c
            ? new ChinChiroJudgeMechanic.HandResult(ChinChiroJudgeMechanic.HandType.ARASHIME, 4, a, 1)
            : new ChinChiroJudgeMechanic.HandResult(ChinChiroJudgeMechanic.HandType.MENASHI, 5, 0, 1);
      }
   }

   private static class HandResult {
      final ChinChiroJudgeMechanic.HandType type;
      final int rank;
      final int strength;
      final int multiplier;

      HandResult(ChinChiroJudgeMechanic.HandType type, int rank, int strength, int multiplier) {
         this.type = type;
         this.rank = rank;
         this.strength = strength;
         this.multiplier = multiplier;
      }
   }

   private enum HandType {
      PINZORO,
      ZOROME,
      SHIGORO,
      ARASHIME,
      MENASHI,
      HIFUMI;
   }
}
