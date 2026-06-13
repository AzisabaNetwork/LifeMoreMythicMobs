package net.azisaba.lifemoremythicmobs.listener;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class KillMessageDamageListener implements Listener {
   private final Plugin plugin;

   public KillMessageDamageListener(Plugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
   public void onDamage(EntityDamageEvent e) {
      Entity victim = e.getEntity();
      if (victim instanceof Player) {
         boolean hasPending = victim.hasMetadata("ICMM_KM_PENDING");
         boolean hasConfirmed = victim.hasMetadata("ICMM_KM_CONFIRMED");
         double rawDamage = e.getDamage();
         double finalDamage = e.getFinalDamage();
         double health = ((Player)victim).getHealth();
         if (hasPending) {
            if (e.isCancelled()) {
               victim.removeMetadata("ICMM_KM_PENDING", this.plugin);
            } else {
               if (finalDamage >= health) {
                  List<MetadataValue> vals = victim.getMetadata("ICMM_KM_PENDING");
                  if (!vals.isEmpty()) {
                     String packed = vals.get(0).asString();
                     victim.setMetadata("ICMM_KM_CONFIRMED", new FixedMetadataValue(this.plugin, packed));
                  }
               }

               victim.removeMetadata("ICMM_KM_PENDING", this.plugin);
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerDeath(PlayerDeathEvent e) {
      Player victim = e.getEntity();
      if (victim.hasMetadata("ICMM_KM_CONFIRMED")) {
         String packed = ((MetadataValue)e.getEntity().getMetadata("ICMM_KM_CONFIRMED").get(0)).asString();
         String[] parts = packed.split("\u0001", 4);
         String msg = parts.length > 0 ? parts[0] : null;
         String scope = parts.length > 1 ? parts[1] : "WORLD";
         double radius = parts.length > 2 ? parseDoubleSafe(parts[2], 30.0) : 30.0;
         UUID damagerId = null;
         if (parts.length > 3 && !parts[3].isEmpty()) {
            try {
               damagerId = UUID.fromString(parts[3]);
            } catch (Exception var14) {
            }
         }

         e.setDeathMessage(null);
         label69:
         if (msg != null && !msg.isEmpty()) {
            String var10 = scope;
            switch (scope.hashCode()) {
               case -1885249390:
                  if (var10.equals("RADIUS")) {
                     World w = victim.getWorld();
                     Iterator var17 = w.getPlayers().iterator();

                     while (true) {
                        if (!var17.hasNext()) {
                           break label69;
                        }

                        Player p = (Player)var17.next();
                        if (p.getLocation().distanceSquared(victim.getLocation()) <= radius * radius) {
                           p.sendMessage(msg);
                        }
                     }
                  }
                  break;
               case -1852497085:
                  if (var10.equals("SERVER")) {
                     Bukkit.getServer().broadcastMessage(msg);
                     break label69;
                  }
                  break;
               case 82781042:
                  if (!var10.equals("WORLD")) {
                  }
            }

            for (Player p : victim.getWorld().getPlayers()) {
               p.sendMessage(msg);
            }
         }

         victim.removeMetadata("ICMM_KM_CONFIRMED", this.plugin);
      }
   }

   private static double parseDoubleSafe(String s, double def) {
      try {
         return Double.parseDouble(s);
      } catch (Exception e) {
         return def;
      }
   }
}
