package net.azisaba.lifemoremythicmobs.trigger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class TriggerEvents implements Listener {

    public static class ItemDropTriggerListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onDrop(PlayerDropItemEvent event) {
            if (TriggerHandler.handle(event.getPlayer(), event.getItemDrop().getItemStack(), "~ONDROP")) {
                event.setCancelled(true);
                event.getPlayer().updateInventory();
            }
        }
    }

    public static class ItemSneakTriggerListener implements Listener {
        @EventHandler
        public void onSneak(PlayerToggleSneakEvent event) {
            if (!event.isSneaking()) return;
            TriggerHandler.handle(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand(), "~ONSNEAK");
        }
    }

}
