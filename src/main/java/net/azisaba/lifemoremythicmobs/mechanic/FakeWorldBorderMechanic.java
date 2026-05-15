package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FakeWorldBorderMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {

    protected final double size;
    protected final int duration;
    protected final int warningTime;
    protected final Double forceX;
    protected final Double forceY;
    protected final Double forceZ;

    public FakeWorldBorderMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.size = config.getDouble(new String[]{"size", "s"}, 20.0);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.warningTime = config.getInteger(new String[]{"warning", "w"}, 5);

        this.forceX = config.getPlaceholderString(new String[]{"x"}, null) != null ? config.getDouble("x") : null;
        this.forceY = config.getPlaceholderString(new String[]{"y"}, null) != null ? config.getDouble("y") : null;
        this.forceZ = config.getPlaceholderString(new String[]{"z"}, null) != null ? config.getDouble("z") : null;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (data.getCaster().getEntity().isPlayer()) {
            Player player = (Player) BukkitAdapter.adapt(data.getCaster().getEntity());
            Location loc = BukkitAdapter.adapt(target.getLocation());
            return execute(player, loc);
        }
        return SkillResult.CONDITION_FAILED;
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        if (data.getCaster().getEntity().isPlayer()) {
            Player player = (Player) BukkitAdapter.adapt(data.getCaster().getEntity());
            Location loc = BukkitAdapter.adapt(target);
            return execute(player, loc);
        }
        return SkillResult.CONDITION_FAILED;
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

    private SkillResult execute(Player player, Location center) {
        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(center);
        border.setSize(size);
        border.setWarningDistance(warningTime);

        player.setWorldBorder(border);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.setWorldBorder(null);
            }
        }, duration);
        return SkillResult.SUCCESS;
    }
}