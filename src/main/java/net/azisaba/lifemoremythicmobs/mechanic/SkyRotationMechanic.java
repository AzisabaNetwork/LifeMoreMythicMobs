package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.PacketPlayOutUpdateTime;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkyRotationMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final int duration;
    protected final long speed;

    // プレイヤーごとの実行中タスクを保持するマップ
    private static final Map<UUID, BukkitRunnable> runningTasks = new HashMap<>();

    public SkyRotationMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.speed = config.getLong(new String[]{"speed", "s"}, 500L);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!target.isPlayer()) return false;

        Player player = (Player) BukkitAdapter.adapt(target);
        UUID uuid = player.getUniqueId();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");

        // すでに実行中のタスクがあればキャンセルして延長（上書き）
        if (runningTasks.containsKey(uuid)) {
            runningTasks.get(uuid).cancel();
        }

        BukkitRunnable task = new BukkitRunnable() {
            private int elapsed = 0;
            private long fakeTime = player.getWorld().getTime();

            @Override
            public void run() {
                if (elapsed >= duration || !player.isOnline()) {
                    // 終了時に同期
                    sendTimePacket(player, player.getWorld().getFullTime(), player.getWorld().getTime());
                    runningTasks.remove(uuid);
                    this.cancel();
                    return;
                }

                fakeTime += speed;
                sendTimePacket(player, player.getWorld().getFullTime(), fakeTime % 24000);
                elapsed++;
            }
        };

        // マップに登録して実行
        runningTasks.put(uuid, task);
        task.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void sendTimePacket(Player player, long worldAge, long dayTime) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        if (ep != null && ep.playerConnection != null) {
            PacketPlayOutUpdateTime packet = new PacketPlayOutUpdateTime(worldAge, dayTime, true);
            ep.playerConnection.sendPacket(packet);
        }
    }
}