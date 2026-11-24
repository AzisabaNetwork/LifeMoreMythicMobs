package net.azisaba.lifemoremythicmobs.freefield;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The world free_field (taken over from FFCore): max health x4 with the hearts scaled back to normal, armor
 * toughness for the ripperC chestplates. (FFCore's health / mana action bar is gone: the HoroloCore HUD shows both.)
 */
public final class FreeFieldListener implements Listener {
    private static final String WORLD = "free_field";
    private static final Map<String, Double> RIPPER = Map.of(
            "ripperC", 40.0, "ripperC1", 80.0, "ripperC2", 160.0, "ripperC3", 300.0);

    private final Plugin plugin;

    public FreeFieldListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            if (inFreeField(player)) apply(player);
            else remove(player);
        }, 40L);
    }

    @EventHandler
    public void onChangeWorld(@NotNull PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (inFreeField(player)) {
            apply(player);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!inFreeField(player)) return;
                AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
                if (attr != null) player.setHealth(attr.getValue());
            }, 30L);
        } else if (WORLD.equalsIgnoreCase(event.getFrom().getName())) {
            remove(player);
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    public void onArmorChange(@NotNull PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        if (inFreeField(player)) {
            removeRipper(player);
            ripper(player);
        }
    }

    private void apply(Player player) {
        AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(FFKeys.MAX_HEALTH);
            health.addModifier(new AttributeModifier(FFKeys.MAX_HEALTH, 3.0, Operation.ADD_SCALAR));
        }
        player.setHealthScaled(true);
        if (health != null) player.setHealthScale(health.getValue() / 4.0);
        ripper(player);
    }

    private void remove(Player player) {
        player.setHealthScaled(false);
        AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
        if (health != null) health.removeModifier(FFKeys.MAX_HEALTH);
        removeRipper(player);
    }

    private void ripper(Player player) {
        String id = FFKeys.mythicId(player.getInventory().getChestplate());
        Double value = id == null ? null : RIPPER.get(id);
        if (value == null) return;
        AttributeInstance attr = player.getAttribute(Attribute.ARMOR_TOUGHNESS);
        if (attr == null) return;
        attr.removeModifier(FFKeys.RIPPER_TOUGHNESS);
        attr.addModifier(new AttributeModifier(FFKeys.RIPPER_TOUGHNESS, value, Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST));
    }

    private void removeRipper(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.ARMOR_TOUGHNESS);
        if (attr != null) attr.removeModifier(FFKeys.RIPPER_TOUGHNESS);
    }

    private static boolean inFreeField(Player player) {
        World world = player.getWorld();
        return WORLD.equalsIgnoreCase(world.getName());
    }
}
