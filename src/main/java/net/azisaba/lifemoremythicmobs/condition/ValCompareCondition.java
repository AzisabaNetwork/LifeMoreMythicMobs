package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.conditions.ISkillMetaCondition;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;

public class ValCompareCondition extends SkillCondition implements ISkillMetaCondition {

    private final boolean invert;
    private final String val1;
    private final String val2;
    private final String operator;

    private static final String OP_GREATER_THAN = ">";
    private static final String OP_LESS_THAN = "<";
    private static final String OP_GREATER_THAN_EQUAL = ">=";
    private static final String OP_LESS_THAN_EQUAL = "<=";
    private static final String OP_EQUAL = "==";
    private static final String OP_NOT_EQUAL = "!=";
    private static final String OP_EQUAL_SINGLE_PARSE_BUG = "=";

    public ValCompareCondition(MythicLineConfig config) {
        super(config.getLine());

        this.val1 = config.getString(new String[] {"value1", "val1", "v1", "数値1"});
        this.val2 = config.getString(new String[] {"value2", "val2", "v2", "数値2"});
        this.operator = config.getString(new String[] {"operator", "op", "比較"}, OP_EQUAL);
        this.invert = config.getBoolean(new String[] {"invert", "i", "逆転"}, false);
    }

    private String resolveValue(SkillMetadata skillMetadata, String rawConfigValue) {
        if (rawConfigValue == null) return null;

        if (rawConfigValue.startsWith("<") && rawConfigValue.endsWith(">")) {
            String varKey = rawConfigValue.substring(1, rawConfigValue.length() - 1);
            return ItemUtil.resolveVariable(skillMetadata, varKey);
        } else {
            return rawConfigValue;
        }
    }

    @Override
    public boolean check(SkillMetadata skillMetadata) {
        String resolvedVal1 = resolveValue(skillMetadata, val1);
        String resolvedVal2 = resolveValue(skillMetadata, val2);

        boolean result;

        if (resolvedVal1 == null || resolvedVal1.isEmpty() || resolvedVal2 == null || resolvedVal2.isEmpty()) {
            return invert;
        }

        try {
            double doubleVal1 = Double.parseDouble(resolvedVal1);
            double doubleVal2 = Double.parseDouble(resolvedVal2);

            switch (operator) {
                case OP_GREATER_THAN:
                    result = doubleVal1 > doubleVal2;
                    break;
                case OP_LESS_THAN:
                    result = doubleVal1 < doubleVal2;
                    break;
                case OP_GREATER_THAN_EQUAL:
                    result = doubleVal1 >= doubleVal2;
                    break;
                case OP_LESS_THAN_EQUAL:
                    result = doubleVal1 <= doubleVal2;
                    break;
                case OP_EQUAL:
                case OP_EQUAL_SINGLE_PARSE_BUG:
                    result = doubleVal1 == doubleVal2;
                    break;
                case OP_NOT_EQUAL:
                    result = doubleVal1 != doubleVal2;
                    break;
                default:
                    return invert;
            }

        } catch (NumberFormatException e) {

            int comparisonResult = resolvedVal1.compareTo(resolvedVal2);

            switch (operator) {
                case OP_GREATER_THAN:
                    result = comparisonResult > 0;
                    break;
                case OP_LESS_THAN:
                    result = comparisonResult < 0;
                    break;
                case OP_GREATER_THAN_EQUAL:
                    result = comparisonResult >= 0;
                    break;
                case OP_LESS_THAN_EQUAL:
                    result = comparisonResult <= 0;
                    break;
                case OP_EQUAL:
                case OP_EQUAL_SINGLE_PARSE_BUG:
                    result = comparisonResult == 0;
                    break;
                case OP_NOT_EQUAL:
                    result = comparisonResult != 0;
                    break;
                default:
                    return invert;
            }
        }

        return invert != result;
    }
}