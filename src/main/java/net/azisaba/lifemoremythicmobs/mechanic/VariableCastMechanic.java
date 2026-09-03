package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.core.skills.variables.Variable;
import io.lumine.mythic.core.skills.variables.VariableRegistry;
import io.lumine.mythic.core.skills.variables.VariableType;

public class VariableCastMechanic extends SkillMechanic implements ITargetedEntitySkill {
    protected final String varName;
    protected final String targetType;

    public VariableCastMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.varName = config.getString(new String[]{"variable", "var", "v"}, "DefaultVar");
        this.targetType = config.getString(new String[]{"type", "t"}, "INTEGER").toUpperCase();
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        VariableRegistry registry = data.getVariables();
        if (!registry.has(varName)) return SkillResult.CONDITION_FAILED;

        // 現在の値を文字列として取得し、ダブルクォーテーションを徹底的に除去
        String rawValue = registry.get(varName).toString().replace("\"", "");
        try {
            Variable newVar;
            if (targetType.equals("FLOAT") || targetType.equals("DOUBLE")) {
                float f = Float.parseFloat(rawValue);
                newVar = Variable.ofType(VariableType.FLOAT, f);
            } else {
                // デフォルトは整数
                int i = (int) Float.parseFloat(rawValue); // 小数点が含まれていても整数化
                newVar = Variable.ofType(VariableType.INTEGER, i);
            }

            // 変換した「純粋な数字型」の変数で上書き保存
            registry.put(varName, newVar);
            return SkillResult.SUCCESS;
        } catch (NumberFormatException e) {
            return SkillResult.ERROR;
        }
    }
}


