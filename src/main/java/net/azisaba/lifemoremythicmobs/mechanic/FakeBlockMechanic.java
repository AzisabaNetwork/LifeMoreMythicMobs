package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeBlockMechanic extends SkillMechanic implements ITargetedLocationSkill {
    protected BlockData blockData;
    protected long duration;

    public FakeBlockMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        String materialName = config.getString(new String[]{"material", "m"}, "STONE");
        try {
            this.blockData = Material.valueOf(materialName.toUpperCase()).createBlockData();
        } catch (Exception e) {
            this.blockData = Material.STONE.createBlockData();
        }

        this.duration = config.getLong(new String[]{"duration", "d"}, 100L);
    }

    @Override
    public boolean castAtLocation(SkillMetadata data, AbstractLocation target) {
        final org.bukkit.Location loc = BukkitAdapter.adapt(target);

        if (data.getCaster().getEntity().isPlayer()) {
            final Player player = (Player) data.getCaster().getEntity().getBukkitEntity();

            player.sendBlockChange(loc, blockData);

            JavaPlugin plugin = JavaPlugin.getPlugin(LifeMoreMythicMobs.class);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendBlockChange(loc, loc.getBlock().getBlockData());
                }
            }, duration);
        }
        return true;
    }
}