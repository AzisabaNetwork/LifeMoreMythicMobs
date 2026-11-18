package net.azisaba.lifemoremythicmobs.listener;

import net.azisaba.lifemoremythicmobs.util.SkillCheckManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Listens for player input during an active SkillCheck QTE.
 */
public class SkillCheckListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        SkillCheckManager.handleInput(event.getPlayer(), SkillCheckManager.InputType.SNEAK);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Action a = event.getAction();
        if (a != Action.LEFT_CLICK_AIR && a != Action.LEFT_CLICK_BLOCK) return;
        if (SkillCheckManager.handleInput(event.getPlayer(), SkillCheckManager.InputType.CLICK)) {
            event.setCancelled(true);
        }
    }
}
