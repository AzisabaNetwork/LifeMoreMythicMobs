package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SlotJamMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private static final Map<UUID, SlotJamTask> activeJams = new ConcurrentHashMap<>();

    protected final PlaceholderInt duration;
    protected final boolean lockToCurrent;
    protected final int targetSlot;
    protected final boolean blockSwap;

    public SlotJamMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.duration = PlaceholderInt.of(config.getString(new String[]{"duration", "d"}, "100"));
        this.lockToCurrent = config.getBoolean(new String[]{"lockCurrent", "lc"}, true);
        this.targetSlot = config.getInteger(new String[]{"slot", "s"}, 0);
        this.blockSwap = config.getBoolean(new String[]{"blockswap", "bs", "swap"}, true);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!target.isPlayer()) return SkillResult.CONDITION_FAILED;

        Player player = (Player) BukkitAdapter.adapt(target);
        int dur = this.duration.get(data, target);
        int slotToLock = lockToCurrent ? player.getInventory().getHeldItemSlot() : targetSlot;

        if (activeJams.containsKey(player.getUniqueId())) {
            activeJams.get(player.getUniqueId()).refresh(dur, slotToLock);
        } else {
            new SlotJamTask(player, dur, slotToLock, blockSwap);
        }
        return SkillResult.SUCCESS;
    }

    private static class SlotJamTask implements Listener, Runnable {
        private final Player player;
        private int ticksRemaining;
        private int lockedSlot;
        private final boolean blockSwap;
        private final int taskId;

        public SlotJamTask(Player player, int duration, int slot, boolean blockSwap) {
            this.player = player;
            this.ticksRemaining = duration;
            this.lockedSlot = slot;
            this.blockSwap = blockSwap;

            Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
            activeJams.put(player.getUniqueId(), this);
            Bukkit.getPluginManager().registerEvents(this, plugin);
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);

            player.getInventory().setHeldItemSlot(lockedSlot);
        }

        public void refresh(int duration, int slot) {
            this.ticksRemaining = duration;
            this.lockedSlot = slot;
            player.getInventory().setHeldItemSlot(lockedSlot);
        }

        @Override
        public void run() {
            if (!player.isOnline() || ticksRemaining <= 0) {
                stop();
                return;
            }
            ticksRemaining--;
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onSlotChange(PlayerItemHeldEvent event) {
            if (event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;

            boolean isTargetSlot = (event.getSlot() == lockedSlot && event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER);
            boolean isHotbarSwap = (event.getHotbarButton() == lockedSlot);

            if (isTargetSlot || isHotbarSwap) {
                event.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
            if (event.getPlayer().getUniqueId().equals(player.getUniqueId()) && blockSwap) {
                event.setCancelled(true);
            }
        }

        public void stop() {
            Bukkit.getScheduler().cancelTask(taskId);
            HandlerList.unregisterAll(this);
            activeJams.remove(player.getUniqueId());
        }
    }
}