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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class OnConsumeAuraMechanic extends SkillMechanic implements ITargetedEntitySkill {
    protected final String auraName;
    protected final String onStartSkill;
    protected final String onConsumeSkill;
    protected final String onTickSkill;
    protected final String onEndSkill;
    protected final int duration;
    protected final int tickInterval;

    public OnConsumeAuraMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.auraName = config.getString(new String[]{"auraName", "aura", "n"}, "consume_aura");
        this.onStartSkill = config.getString(new String[]{"onStart", "oS"}, null);
        this.onConsumeSkill = config.getString(new String[]{"onConsume", "oc", "oC"}, null);
        this.onTickSkill = config.getString(new String[]{"onTick", "oT"}, null);
        this.onEndSkill = config.getString(new String[]{"onEnd", "oE"}, null);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 200);
        this.tickInterval = config.getInteger(new String[]{"tickInterval", "ti"}, 1);
    }

    public static void remove(AbstractEntity target, String auraName) {
        CustomAura.remove(target, auraName);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        String id = target.getUniqueId().toString() + ":" + this.auraName;
        CustomAura existing = CustomAura.getActive(id);
        if (existing instanceof ConsumeAura) {
            existing.refresh(this.duration);
            return SkillResult.SUCCESS;
        }

        new ConsumeAura(target, data, auraName, duration, tickInterval, onStartSkill);
        return SkillResult.SUCCESS;
    }

    private class ConsumeAura extends CustomAura {
        public ConsumeAura(AbstractEntity target, SkillMetadata data, String auraName, int duration, int tickInterval, String onStartSkill) {
            super(target, data, auraName, duration, tickInterval);
            AuraSkillHelper.executeSkill(onStartSkill, data, target);
        }

        @Override
        protected void onTick() {
            if (onTickSkill != null && ticksRemaining % tickInterval == 0) {
                AuraSkillHelper.executeSkill(onTickSkill, data, target);
            }
        }

        @Override
        protected void onEnd(boolean timeOut) {
            if (timeOut) AuraSkillHelper.executeSkill(onEndSkill, data, target);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onConsume(PlayerItemConsumeEvent event) {
            if (event.getPlayer().getUniqueId().equals(target.getUniqueId())) {
                AuraSkillHelper.executeSkill(onConsumeSkill, data, target);
            }
        }
    }
}
