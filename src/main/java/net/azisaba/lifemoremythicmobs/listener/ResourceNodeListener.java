package net.azisaba.lifemoremythicmobs.listener;

import net.azisaba.lifemoremythicmobs.util.ResourceNodeManager;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Handles right/left clicks on custom resource nodes.
 */
public class ResourceNodeListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;
        if (ResourceNodeManager.handleInteraction(interaction.getUniqueId(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Interaction interaction)) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (ResourceNodeManager.handleInteraction(interaction.getUniqueId(), player, false)) {
            event.setCancelled(true);
        }
    }
}
