package net.azisaba.lifemoremythicmobs.listener;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class WorldChangeRemovePotionEffectListener implements Listener {
   private final LifeMoreMythicMobs plugin;
   private Set<String> targetWorldsLower;
   private int delayTicks;
   private Set<PotionEffectType> excluded;

   public WorldChangeRemovePotionEffectListener(LifeMoreMythicMobs plugin) {
      this.plugin = plugin;
      this.reloadSettings();
   }

   public void reloadSettings() {
      this.targetWorldsLower = new HashSet<>(
         this.plugin
            .getConfig()
            .getStringList("effects-clear.worlds")
            .stream()
            .filter(Objects::nonNull)
            .map(s -> s.trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet())
      );
      this.delayTicks = this.plugin.getConfig().getInt("effects-clear.delay-ticks", 40);
      this.excluded = this.plugin
         .getConfig()
         .getStringList("effects-clear.exclude-effects")
         .stream()
         .map(s -> s == null ? null : s.trim().toUpperCase(Locale.ROOT))
         .<PotionEffectType>map(PotionEffectType::getByName)
         .filter(Objects::nonNull)
         .collect(Collectors.toSet());
   }

   private boolean enabled() {
      return this.plugin.getConfig().getBoolean("effects-clear.enabled", true);
   }

   private boolean isTargetWorld(World world) {
      return world == null ? false : this.targetWorldsLower.contains(world.getName().toLowerCase(Locale.ROOT));
   }

   private void clearEffectsLater(Player player) {
      if (player != null) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            for (PotionEffect pe : new ArrayList(player.getActivePotionEffects())) {
               if (!this.excluded.contains(pe.getType())) {
                  player.removePotionEffect(pe.getType());
               }
            }
         }, Math.max(0, this.delayTicks));
      }
   }

   @EventHandler
   public void onJoin(@NotNull PlayerJoinEvent event) {
      if (this.enabled()) {
         Player player = event.getPlayer();
         if (this.isTargetWorld(player.getWorld())) {
            this.clearEffectsLater(player);
         }
      }
   }

   @EventHandler
   public void onChangeWorld(@NotNull PlayerChangedWorldEvent event) {
      if (this.enabled()) {
         Player player = event.getPlayer();
         if (this.isTargetWorld(player.getWorld())) {
            this.clearEffectsLater(player);
         }
      }
   }
}
