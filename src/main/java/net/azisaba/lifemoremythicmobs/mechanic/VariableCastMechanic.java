package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.skills.variables.Variable;
import io.lumine.xikage.mythicmobs.skills.variables.VariableRegistry;
import io.lumine.xikage.mythicmobs.skills.variables.VariableType;

public class VariableCastMechanic extends SkillMechanic implements ITargetedEntitySkill {
    protected final String varName;
    protected final String targetType;

    public VariableCastMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.varName = config.getString(new String[]{"variable", "var", "v"}, "DefaultVar");
        this.targetType = config.getString(new String[]{"type", "t"}, "INTEGER").toUpperCase();
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        VariableRegistry registry = data.getVariables();
        if (!registry.has(varName)) return false;

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
            return true;
        } catch (NumberFormatException e) {
            // 数字に変換できなかった場合は何もしない
            return false;
        }
    }
}