package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import net.azisaba.lifemoremythicmobs.util.CustomAura;
import net.azisaba.lifemoremythicmobs.util.SkillUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

public class OnDeathAuraMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String auraName;
    protected final String onDeathSkill;
    protected final String onTickSkill;
    protected final String onEndSkill;
    protected final int duration;
    protected final int tickInterval;

    public OnDeathAuraMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.auraName = config.getString(new String[]{"auraName", "aura", "n"}, "death_aura");
        this.onDeathSkill = config.getString(new String[]{"onDeath", "od"}, null);
        this.onTickSkill = config.getString(new String[]{"onTick", "ot"}, null);
        this.onEndSkill = config.getString(new String[]{"onEnd", "oe"}, null);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 200);
        this.tickInterval = config.getInteger(new String[]{"tickInterval", "ti"}, 1);
    }

    public static void remove(AbstractEntity target, String auraName) {
        CustomAura.remove(target, auraName);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        String id = target.getUniqueId().toString() + ":" + this.auraName;

        CustomAura existing = CustomAura.getActive(id);
        if (existing instanceof DeathAura) {
            existing.refresh(this.duration);
            return true;
        }
        
        new DeathAura(target, data, auraName, duration, tickInterval);
        return true;
    }

    private class DeathAura extends CustomAura {
        public DeathAura(AbstractEntity target, SkillMetadata data, String auraName, int duration, int tickInterval) {
            super(target, data, auraName, duration, tickInterval);
        }

        @Override
        protected void onTick() {
            if (onTickSkill != null && ticksRemaining % tickInterval == 0) {
                SkillUtil.executeSkill(onTickSkill, data, target);
            }
        }

        @Override
        protected void onEnd(boolean timeOut) {
            if (timeOut) SkillUtil.executeSkill(onEndSkill, data, target);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent event) {
            if (event.getEntity().getUniqueId().equals(target.getUniqueId())) {
                SkillUtil.executeSkill(onDeathSkill, data, target);
                stop(false);
            }
        }
    }
}