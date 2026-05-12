package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderFloat;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SetFirstPersonViewMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {

    private static final Map<UUID, CameraSession> activeCameras = new ConcurrentHashMap<>();
    protected final int duration;
    protected final PlaceholderFloat yaw;
    protected final PlaceholderFloat pitch;
    protected final boolean useTargetRotation;
    protected final double yOffset;

    public SetFirstPersonViewMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.yaw = PlaceholderFloat.of(config.getString(new String[]{"yaw", "y"}, "0"));
        this.pitch = PlaceholderFloat.of(config.getString(new String[]{"pitch", "p"}, "0"));
        this.useTargetRotation = config.getBoolean(new String[]{"useTargetRotation", "utr"}, false);
        this.yOffset = config.getDouble(new String[]{"yOffset", "yo"}, 1.6);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!data.getCaster().getEntity().isPlayer()) return false;
        Player player = (Player) BukkitAdapter.adapt(data.getCaster().getEntity());

        Location loc = BukkitAdapter.adapt(target.getLocation()).add(0, yOffset, 0);
        applyRotation(data, loc);

        return startSession(player, loc);
    }

    @Override
    public boolean castAtLocation(SkillMetadata data, AbstractLocation target) {
        if (!data.getCaster().getEntity().isPlayer()) return false;
        Player player = (Player) BukkitAdapter.adapt(data.getCaster().getEntity());

        Location loc = BukkitAdapter.adapt(target).add(0, yOffset, 0);
        applyRotation(data, loc);

        return startSession(player, loc);
    }

    private void applyRotation(SkillMetadata data, Location loc) {
        if (!useTargetRotation) {
            loc.setYaw(yaw.get(data));
            loc.setPitch(pitch.get(data));
        }
    }

    private boolean startSession(Player player, Location cameraLocation) {
        if (activeCameras.containsKey(player.getUniqueId())) {
            activeCameras.get(player.getUniqueId()).stop();
        }
        new CameraSession(player, cameraLocation, duration);
        return true;
    }

    private static class CameraSession implements Runnable {
        private final Player player;
        private final ArmorStand cameraAnchor;
        private final GameMode originalGameMode;
        private final Location returnLocation;
        private final int taskId;
        private int ticksRemaining;

        public CameraSession(Player player, Location cameraLoc, int duration) {
            this.player = player;
            this.ticksRemaining = duration;
            this.originalGameMode = player.getGameMode();
            this.returnLocation = player.getLocation();

            this.cameraAnchor = (ArmorStand) cameraLoc.getWorld().spawnEntity(cameraLoc, EntityType.ARMOR_STAND);
            cameraAnchor.setVisible(false);
            cameraAnchor.setGravity(false);
            cameraAnchor.setMarker(true);
            cameraAnchor.setAI(false);
            cameraAnchor.setRotation(cameraLoc.getYaw(), cameraLoc.getPitch());

            player.setGameMode(GameMode.SPECTATOR);

            Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
            activeCameras.put(player.getUniqueId(), this);

            player.setSpectatorTarget(cameraAnchor);
        }

        @Override
        public void run() {
            if (!player.isOnline() || ticksRemaining <= 0) {
                stop();
                return;
            }
            if (player.getSpectatorTarget() == null || !player.getSpectatorTarget().equals(cameraAnchor)) {
                player.setSpectatorTarget(cameraAnchor);
            }
            ticksRemaining--;
        }

        public void stop() {
            Bukkit.getScheduler().cancelTask(taskId);
            player.setSpectatorTarget(null);
            player.setGameMode(originalGameMode);

            Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) player.teleport(returnLocation);
            }, 1L);

            cameraAnchor.remove();
            activeCameras.remove(player.getUniqueId());
        }
    }
}