package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LockInventoryMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private static final Map<String, InventoryLock> activeLocks = new ConcurrentHashMap<>();

    protected final PlaceholderString slots;
    protected final PlaceholderInt duration;
    protected final boolean blockSwap;

    public LockInventoryMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.slots = PlaceholderString.of(config.getString(new String[]{"slots", "s"}, "0"));
        this.duration = PlaceholderInt.of(config.getString(new String[]{"duration", "d"}, "200"));
        this.blockSwap = config.getBoolean(new String[]{"blockswap", "bs", "swap"}, true);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!(BukkitAdapter.adapt(target) instanceof Player)) return SkillResult.CONDITION_FAILED;

        Player player = (Player) BukkitAdapter.adapt(target);
        String resolvedSlots = this.slots.get(data, target);
        int lockDuration = this.duration.get(data, target);

        Set<Integer> targetSlots = parseSlots(resolvedSlots);
        if (targetSlots.isEmpty()) return SkillResult.CONDITION_FAILED;

        String id = player.getUniqueId() + ":" + resolvedSlots;

        if (activeLocks.containsKey(id)) {
            activeLocks.get(id).refresh(lockDuration);
        } else {
            new InventoryLock(player, id, targetSlots, lockDuration, blockSwap);
        }
        return SkillResult.SUCCESS;
    }

    private Set<Integer> parseSlots(String input) {
        Set<Integer> slots = new HashSet<>();
        for (String part : input.split(",")) {
            if (part.contains("-")) {
                String[] range = part.split("-");
                try {
                    int start = Integer.parseInt(range[0].trim());
                    int end = Integer.parseInt(range[1].trim());
                    for (int i = Math.min(start, end); i <= Math.max(start, end); i++) {
                        slots.add(i);
                    }
                } catch (Exception ignored) {}
            } else {
                try {
                    slots.add(Integer.parseInt(part.trim()));
                } catch (Exception ignored) {}
            }
        }
        return slots;
    }

    private static class InventoryLock implements Listener, Runnable {
        private final Player player;
        private final String id;
        private final Set<Integer> lockedSlots;
        private final boolean blockSwap;
        private int ticksRemaining;
        private int taskId = -1;

        public InventoryLock(Player player, String id, Set<Integer> slots, int duration, boolean blockSwap) {
            this.player = player;
            this.id = id;
            this.lockedSlots = slots;
            this.ticksRemaining = duration;
            this.blockSwap = blockSwap;

            Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
            if (plugin != null) {
                activeLocks.put(id, this);
                Bukkit.getPluginManager().registerEvents(this, plugin);
                this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
            }
        }

        public void refresh(int duration) {
            this.ticksRemaining = duration;
        }

        @Override
        public void run() {
            if (!player.isOnline() || ticksRemaining <= 0) {
                stop();
                return;
            }
            ticksRemaining--;
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;

            if (lockedSlots.contains(event.getSlot()) ||
                    lockedSlots.contains(event.getRawSlot()) ||
                    lockedSlots.contains(event.getHotbarButton())) {
                event.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onInventoryDrag(InventoryDragEvent event) {
            if (!event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;

            for (int slot : event.getInventorySlots()) {
                if (lockedSlots.contains(slot)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
            if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;

            if (blockSwap) {
                event.setCancelled(true);
            }
        }

        private void stop() {
            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            }
            HandlerList.unregisterAll(this);
            activeLocks.remove(id);
        }
    }
}