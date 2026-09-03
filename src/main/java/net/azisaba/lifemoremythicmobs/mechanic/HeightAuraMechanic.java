package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.util.AuraSkillHelper;
import net.azisaba.lifemoremythicmobs.util.CustomAura;

public class HeightAuraMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final String auraName, onHeightIncreaseSkillName;
    private final int duration, interval;

    public HeightAuraMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        auraName = config.getString(new String[]{"auraname", "aura", "name"}, "height_aura");
        onHeightIncreaseSkillName = config.getString("onheightincreaseskill", "");
        duration = config.getInteger(new String[]{"duration", "d"}, 200);
        interval = Math.max(1, config.getInteger(new String[]{"interval", "i"}, 1));
    }

    @Override public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.ERROR;
        CustomAura existing = CustomAura.getActive(target.getUniqueId() + ":" + auraName);
        if (existing instanceof HeightAura) existing.refresh(duration);
        else new HeightAura(target, data.deepClone());
        return SkillResult.SUCCESS;
    }

    private final class HeightAura extends CustomAura {
        private double previousY;
        private HeightAura(AbstractEntity target, SkillMetadata data) {
            super(target, data, HeightAuraMechanic.this.auraName,
                    HeightAuraMechanic.this.duration, HeightAuraMechanic.this.interval);
            previousY = target.getLocation().getY();
        }
        @Override protected void onTick() {
            double currentY = target.getLocation().getY();
            if (currentY > previousY && onHeightIncreaseSkillName != null && !onHeightIncreaseSkillName.isEmpty())
                AuraSkillHelper.executeSkill(onHeightIncreaseSkillName, data, target);
            previousY = currentY;
        }
        @Override protected void onEnd(boolean timeOut) {}
    }
}


