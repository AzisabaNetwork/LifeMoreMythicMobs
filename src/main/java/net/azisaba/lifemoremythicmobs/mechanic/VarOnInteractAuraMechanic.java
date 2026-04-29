package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderInt;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class VarOnInteractAuraMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private static final Map<String, InteractAura> activeAuras = new ConcurrentHashMap<>();

    protected final PlaceholderString auraName;
    protected final PlaceholderString onInteractSkill;
    protected final PlaceholderInt duration;
    protected final boolean isSwing;

    public VarOnInteractAuraMechanic(MythicLineConfig config, boolean isSwing) {
        super(config.getLine(), config);
        this.isSwing = isSwing;
        this.auraName = PlaceholderString.of(config.getString(new String[]{"auraName", "n"}, isSwing ? "swing_aura" : "use_aura"));
        this.onInteractSkill = PlaceholderString.of(config.getString(new String[]{"onInteract", "oi", "skill", "s"}, ""));
        this.duration = PlaceholderInt.of(config.getString(new String[]{"duration", "d"}, "200"));
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!(BukkitAdapter.adapt(target) instanceof Player)) return false;

        String resolvedAuraName = this.auraName.get(data, target);
        String resolvedSkill = this.onInteractSkill.get(data, target);
        int resolvedDuration = this.duration.get(data, target);

        String id = target.getUniqueId().toString() + ":" + resolvedAuraName;

        if (activeAuras.containsKey(id)) {
            activeAuras.get(id).refresh(resolvedDuration);
        } else {
            new InteractAura((Player) BukkitAdapter.adapt(target), data, id, resolvedSkill, resolvedDuration);
        }
        return true;
    }

    private class InteractAura implements Listener, Runnable {
        private final Player player;
        private final SkillMetadata data;
        private final String id;
        private final String skillToRun;
        private int ticksRemaining;
        private int taskId = -1;

        public InteractAura(Player player, SkillMetadata data, String id, String skillToRun, int duration) {
            this.player = player;
            this.data = data;
            this.id = id;
            this.skillToRun = skillToRun;
            this.ticksRemaining = duration;

            Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
            if (plugin != null) {
                activeAuras.put(id, this);
                Bukkit.getPluginManager().registerEvents(this, plugin);
                this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
            }
        }

        public void refresh(int newDuration) {
            this.ticksRemaining = newDuration;
        }

        @Override
        public void run() {
            if (!player.isOnline() || player.isDead() || ticksRemaining <= 0) {
                stop();
                return;
            }
            ticksRemaining--;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onInteract(PlayerInteractEvent event) {
            if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;

            Action action = event.getAction();
            if (isSwing) {
                if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                    executeSkill(skillToRun);
                }
            } else {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    executeSkill(skillToRun);
                }
            }
        }

        private void stop() {
            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            }
            HandlerList.unregisterAll(this);
            activeAuras.remove(id);
        }

        private void executeSkill(String skillName) {
            if (skillName == null || skillName.isEmpty()) return;
            Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(skillName);
            maybeSkill.ifPresent(skill -> {
                SkillMetadata clone = data.deepClone();
                clone.setTrigger(BukkitAdapter.adapt(player));
                skill.execute(clone);
            });
        }
    }
}