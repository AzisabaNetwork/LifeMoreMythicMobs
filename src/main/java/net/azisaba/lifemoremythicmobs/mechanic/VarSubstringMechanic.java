package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;

public class VarSubstringMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final int start;
    protected final int end;
    protected final String from;
    protected final String to;

    public VarSubstringMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.start = config.getInteger(new String[] {"start"}, 0);
        this.end = config.getInteger(new String[] {"end"}, Integer.MAX_VALUE);
        this.from = config.getString(new String[] {"from"});
        this.to = config.getString(new String[] {"to"});
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        String s = ItemUtil.resolveVariable(skillMetadata, from);
        if (s == null) return false;
        s = s.substring(start, Math.min(s.length(), end));
        ItemUtil.setVariable(skillMetadata, to, s);
        return true;
    }
}
