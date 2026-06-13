package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import net.azisaba.lifemoremythicmobs.util.CustomAura;
import net.azisaba.lifemoremythicmobs.util.SkillUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class NullRecoveryMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String auraName;
    protected final String onStart;
    protected final String onHeal;
    protected final String onTickSkill;
    protected final String onEnd;
    protected final int duration;
    protected final int tickInterval;
    protected final String amount;

    public NullRecoveryMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.auraName = config.getString(new String[]{"auraName", "aura", "n", "名前"}, "default");
        this.onStart = config.getString(new String[]{"onStart", "os"}, null);
        this.onHeal = config.getString(new String[]{"onHeal", "oh"}, null);
        this.onTickSkill = config.getString(new String[]{"onTick", "ot"}, null);
        this.onEnd = config.getString(new String[]{"onEnd", "oe"}, null);
        this.duration = config.getInteger(new String[]{"duration", "d", "持続時間"}, 100);
        this.tickInterval = config.getInteger(new String[]{"tickInterval", "ti"}, 1);
        this.amount = config.getString(new String[]{"amount", "a", "量"}, "100%");
    }

    public static void remove(AbstractEntity target, String auraName) {
        CustomAura.remove(target, auraName);
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        String identifier = abstractEntity.getUniqueId().toString() + ":" + this.auraName;

        CustomAura existing = CustomAura.getActive(identifier);
        if (existing instanceof NullRecoveryAura) {
            existing.refresh(this.duration);
            return true;
        }

        new NullRecoveryAura(abstractEntity, skillMetadata, auraName, duration, tickInterval);
        return true;
    }

    private class NullRecoveryAura extends CustomAura {
        private double lastHealth;

        public NullRecoveryAura(AbstractEntity target, SkillMetadata data, String auraName, int duration, int tickInterval) {
            super(target, data, auraName, duration, tickInterval);
            this.lastHealth = target.getHealth();
            SkillUtil.executeSkill(onStart, data, target);
        }

        @Override
        protected void onTick() {
            if (onTickSkill != null && ticksRemaining % tickInterval == 0) {
                SkillUtil.executeSkill(onTickSkill, data, target);
            }

            double currentHealth = target.getHealth();
            if (currentHealth > lastHealth) {
                double diff = currentHealth - lastHealth;
                double reduce;
                try {
                    if (amount.endsWith("%")) {
                        reduce = diff * (Double.parseDouble(amount.replace("%", "")) / 100.0);
                    } else {
                        reduce = Double.parseDouble(amount);
                    }
                } catch (Exception e) { reduce = 0; }

                double finalHealth = Math.max(lastHealth, currentHealth - reduce);
                target.setHealth(finalHealth);
                if (reduce > 0) SkillUtil.executeSkill(onHeal, data, target);
                this.lastHealth = finalHealth;
            } else {
                this.lastHealth = currentHealth;
            }
        }

        @Override
        protected void onEnd(boolean timeOut) {
            SkillUtil.executeSkill(onEnd, data, target);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onRegain(EntityRegainHealthEvent event) {
            if (event.getEntity().getUniqueId().equals(target.getUniqueId())) {
                this.lastHealth = target.getHealth();
            }
        }
    }
}