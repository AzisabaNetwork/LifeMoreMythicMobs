package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.conditions.ISkillMetaCondition;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import io.lumine.xikage.mythicmobs.util.annotations.MythicCondition;
import io.lumine.xikage.mythicmobs.util.annotations.MythicField;
import io.lumine.xikage.mythicmobs.utils.numbers.RangedDouble;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;

@MythicCondition(
        author = "Phil",
        name = "bowTension",
        aliases = {"bowshoottension"},
        description = "Tests the bow tension when shooting from a bow"
)
public class BowTensionCondition extends SkillCondition implements ISkillMetaCondition {
    @MythicField(
            name = "force",
            aliases = {"f", "value", "v", "val"},
            description = "The amount of force to check",
            defValue = ">0"
    )
    private final PlaceholderString force;
    private final boolean invert;

    public BowTensionCondition(MythicLineConfig config) {
        super(config.getLine());
        this.force = config.getPlaceholderString(new String[]{"force", "f", "value", "v", "val", "あたい", "値"}, ">0");
        this.invert = config.getBoolean(new String[]{"i", "invert", "逆転"}, false);
    }

    public boolean check(SkillMetadata data) {
        String actualForceString = ItemUtil.resolveVariable(data, "caster.var.bow-tension");
        if (actualForceString == null) {
            return false;
        }
        double actualForce = Float.parseFloat(actualForceString);
        //noinspection EqualsBetweenInconvertibleTypes
        boolean value = (new RangedDouble(this.force.get(data, data.getCaster().getEntity()))).equals(actualForce);
        return this.invert != value;
    }
}
