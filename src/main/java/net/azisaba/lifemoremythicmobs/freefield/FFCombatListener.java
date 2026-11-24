package net.azisaba.lifemoremythicmobs.freefield;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** The damage and heal side of {@code noHeal} and {@code weakSign} (taken over from FFCore). */
public final class FFCombatListener implements Listener {
    private final Map<UUID, Integer> noHeal = new ConcurrentHashMap<>();

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        noHeal.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent event) {
        noHeal.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onChangeWorld(@NotNull PlayerChangedWorldEvent event) {
        noHeal.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeal(@NotNull EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Integer divider = noHeal.get(player.getUniqueId());
        if (divider != null && divider != 0) event.setAmount(event.getAmount() / divider);
    }

    @EventHandler
    public void onDamaged(@NotNull EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof Zombie zombie) {
            String raw = zombie.getPersistentDataContainer().get(FFKeys.NO_HEAL, PersistentDataType.STRING);
            if (raw == null) return;
            try {
                int level = Integer.parseInt(raw);
                UUID id = player.getUniqueId();
                noHeal.put(id, level);
                Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(), () -> noHeal.remove(id, level), 100L);
            } catch (NumberFormatException ignored) {
            }
            return;
        }
        if (!event.isCancelled() && !(event.getEntity() instanceof Player) && event.getDamager() instanceof Player
                && FFMechanics.WeakSign.MARKED.contains(event.getEntity().getUniqueId())) {
            event.setDamage(event.getDamage() * 1.2);
        }
    }
}
