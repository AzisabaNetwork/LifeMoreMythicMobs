package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;

public class VarReplaceRegexMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String regex;
    protected final String replacement;
    protected final String from;
    protected final String to;

    public VarReplaceRegexMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);

        this.regex = config.getString(new String[] {"regex", "r", "正規表現"});
        this.replacement = config.getString(new String[] {"replacement", "rep", "置換"});
        this.from = config.getString(new String[] {"from"});
        this.to = config.getString(new String[] {"to"});
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        String s = ItemUtil.resolveVariable(skillMetadata, from);
        if (s == null) return SkillResult.CONDITION_FAILED;
        ItemUtil.setVariable(skillMetadata, to, s.replaceAll(regex, replacement));
        return SkillResult.SUCCESS;
    }
}
