package net.azisaba.lifemoremythicmobs.listener;

import net.azisaba.lifemoremythicmobs.util.ParryWindowManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Triggers active parry windows when an entity takes damage.
 */
public class ParryWindowListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity defender = event.getEntity();
        Entity attacker = event.getDamager();
        if (attacker instanceof Projectile proj) {
            ProjectileSource shooter = proj.getShooter();
            if (shooter instanceof Entity shooterEntity) {
                attacker = shooterEntity;
            }
        }
        double mult = ParryWindowManager.tryParry(defender.getUniqueId(), attacker);
        if (mult < 0.0) return;
        if (mult <= 0.0) {
            event.setCancelled(true);
        } else {
            event.setDamage(event.getDamage() * mult);
        }
    }
}
