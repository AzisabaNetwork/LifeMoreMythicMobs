package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.bukkit.events.MythicTriggerEvent;
import io.lumine.mythic.core.skills.SkillTriggers;
import io.lumine.mythic.core.skills.triggers.meta.EntityAttackMetadata;
import net.azisaba.lifemoremythicmobs.util.CritRoll;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Makes {@code critChanceBuff} count on melee hits that run no {@code critChance} line: when an attacker with an
 * active buff fires its attack trigger on a non-player, the hit gets a {@link CritRoll}.
 */
public final class CritBuffListener implements Listener {
    @EventHandler
    public void onTrigger(MythicTriggerEvent event) {
        if (event.getTrigger() != SkillTriggers.ATTACK) return;
        if (!(event.getTriggerMetadata() instanceof EntityAttackMetadata meta)) return;
        if (meta.getDamager() == null || meta.getDamaged() == null || meta.getDamaged().isPlayer()) return;
        if (!CritRoll.hasBuff(meta.getDamager().getUniqueId())) return;
        CritRoll.of(meta, meta.getDamager().getUniqueId());
    }
}
