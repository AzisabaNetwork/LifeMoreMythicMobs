package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.core.skills.variables.VariableRegistry;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class BowForceListener implements Listener {
    @EventHandler
    public void onShootBow(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            VariableRegistry registry = ItemUtil.getPlayerVariable((Player) e.getEntity());
            if (registry != null) {
                registry.putFloat("bow-tension", e.getForce());
            }
        }
    }
}

