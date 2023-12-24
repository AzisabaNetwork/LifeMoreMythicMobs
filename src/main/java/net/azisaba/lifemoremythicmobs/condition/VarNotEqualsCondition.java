package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.conditions.ISkillMetaCondition;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;

import java.util.Objects;

public class VarNotEqualsCondition extends SkillCondition implements ISkillMetaCondition {
    private final boolean invert;
    private final String varName;
    private final String value;

    public VarNotEqualsCondition(MythicLineConfig config) {
        super(config.getLine());

        this.invert = config.getBoolean(new String[] {"invert", "i", "逆転"}, false);
        this.varName = config.getString(new String[] {"variable", "var", "v", "変数"});
        this.value = config.getString(new String[] {"value", "val", "値"});
    }

    @Override
    public boolean check(SkillMetadata skillMetadata) {
        String actualValue =
                value.startsWith("<") && value.endsWith(">")
                        ? ItemUtil.resolveVariable(skillMetadata, value.substring(1, value.length() - 1))
                        : value;
        String varValue = ItemUtil.resolveVariable(skillMetadata, varName);
        return invert == Objects.equals(varValue, actualValue);
    }
}
