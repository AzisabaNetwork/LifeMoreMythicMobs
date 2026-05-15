package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkyRotationMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final int duration;
    protected final long speed;

    private static final Map<UUID, BukkitRunnable> runningTasks = new HashMap<>();

    public SkyRotationMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.speed = config.getLong(new String[]{"speed", "s"}, 500L);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!target.isPlayer()) return SkillResult.CONDITION_FAILED;

        Player player = (Player) BukkitAdapter.adapt(target);
        UUID uuid = player.getUniqueId();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        if (plugin == null) return SkillResult.ERROR;

        if (runningTasks.containsKey(uuid)) {
            runningTasks.get(uuid).cancel();
        }

        BukkitRunnable task = new BukkitRunnable() {
            private int elapsed = 0;
            private long fakeTime = player.getPlayerTime();

            @Override
            public void run() {
                if (elapsed >= duration || !player.isOnline()) {
                    player.resetPlayerTime();
                    runningTasks.remove(uuid);
                    this.cancel();
                    return;
                }
                fakeTime += speed;
                player.setPlayerTime(fakeTime % 24000, false);
                elapsed++;
            }
        };

        runningTasks.put(uuid, task);
        task.runTaskTimer(plugin, 0L, 1L);

        return SkillResult.SUCCESS;
    }
}