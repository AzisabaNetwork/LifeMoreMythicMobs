package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FakeWorldBorderMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {

    protected final double size;
    protected final int duration;
    protected final int warningTime;
    protected final Double forceX; // null を許容
    protected final Double forceY;
    protected final Double forceZ;

    public FakeWorldBorderMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.size = config.getDouble(new String[]{"size", "s"}, 20.0);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.warningTime = config.getInteger(new String[]{"warning", "w"}, 5);

        // getPlaceholderString を使って、指定がなければ null、あれば Double に変換
        this.forceX = config.getPlaceholderString(new String[]{"x"}, null) != null ? config.getDouble("x") : null;
        this.forceY = config.getPlaceholderString(new String[]{"y"}, null) != null ? config.getDouble("y") : null;
        this.forceZ = config.getPlaceholderString(new String[]{"z"}, null) != null ? config.getDouble("z") : null;
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (data.getCaster().getEntity().isPlayer()) {
            Player player = (Player) BukkitAdapter.adapt(data.getCaster().getEntity());
            Location loc = BukkitAdapter.adapt(target.getLocation());
            return execute(player, applyCustomLocation(loc));
        }
        return false;
    }

    @Override
    public boolean castAtLocation(SkillMetadata data, AbstractLocation target) {
        if (data.getCaster().getEntity().isPlayer()) {
            Player player = (Player) BukkitAdapter.adapt(data.getCaster().getEntity());
            Location loc = BukkitAdapter.adapt(target);
            return execute(player, applyCustomLocation(loc));
        }
        return false;
    }

    /**
     * パラメータで座標が指定されている場合、元の座標を上書きする
     */
    private Location applyCustomLocation(Location base) {
        double x = (forceX != null) ? forceX : base.getX();
        double y = (forceY != null) ? forceY : base.getY();
        double z = (forceZ != null) ? forceZ : base.getZ();
        return new Location(base.getWorld(), x, y, z);
    }

    private boolean execute(Player player, Location center) {
        try {
            sendBorderPacket(player, center, size, warningTime);

            Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    resetBorder(player);
                }
            }, duration);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendBorderPacket(Player player, Location loc, double size, int warning) {
        WorldServer worldServer = ((CraftWorld) loc.getWorld()).getHandle();
        WorldBorder nmsBorder = new WorldBorder();
        nmsBorder.world = worldServer;

        nmsBorder.setCenter(loc.getX(), loc.getZ());
        nmsBorder.setSize(size);
        nmsBorder.setWarningTime(warning);
        nmsBorder.setDamageAmount(0.0);

        PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(nmsBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
        sendPacket(player, packet);
    }

    private void resetBorder(Player player) {
        WorldBorder nmsBorder = ((CraftWorld) player.getWorld()).getHandle().getWorldBorder();
        PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(nmsBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
        sendPacket(player, packet);
    }

    private void sendPacket(Player player, Packet<?> packet) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        if (ep != null && ep.playerConnection != null) {
            ep.playerConnection.sendPacket(packet);
        }
    }
}