package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;

public class VarReplaceRegexMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String regex;
    protected final String replacement;
    protected final String from;
    protected final String to;

    public VarReplaceRegexMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.regex = config.getString(new String[] {"regex", "r", "正規表現"});
        this.replacement = config.getString(new String[] {"replacement", "rep", "置換"});
        this.from = config.getString(new String[] {"from"});
        this.to = config.getString(new String[] {"to"});
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        String s = ItemUtil.resolveVariable(skillMetadata, from);
        if (s == null) return false;
        ItemUtil.setVariable(skillMetadata, to, s.replaceAll(regex, replacement));
        return true;
    }
}
