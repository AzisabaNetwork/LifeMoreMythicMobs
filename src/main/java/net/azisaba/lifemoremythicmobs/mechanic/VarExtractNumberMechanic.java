package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;

public class VarExtractNumberMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final int start;
    protected final int end;
    protected final String from;
    protected final String to;
    protected final boolean lazy;

    public VarExtractNumberMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);

        this.start = config.getInteger(new String[] {"start"}, 0);
        this.end = config.getInteger(new String[] {"end"}, Integer.MAX_VALUE);
        this.from = config.getString(new String[] {"from"});
        this.to = config.getString(new String[] {"to"});
        this.lazy = config.getBoolean(new String[] {"lazy", "l", "怠惰"}, false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        String s = ItemUtil.resolveVariable(skillMetadata, from);
        if (s == null) return SkillResult.CONDITION_FAILED;
        s = s.substring(start, Math.min(s.length(), end));
        if (lazy) {
            ItemUtil.setVariable(skillMetadata, to, s.replaceAll(".*?(\\d+).*", "$1"));
        } else {
            ItemUtil.setVariable(skillMetadata, to, s.replaceAll("[^0-9]", ""));
        }
        return SkillResult.SUCCESS;
    }
}
