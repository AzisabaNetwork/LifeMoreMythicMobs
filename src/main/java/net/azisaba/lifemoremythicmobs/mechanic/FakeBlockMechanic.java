package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeBlockMechanic extends SkillMechanic implements ITargetedLocationSkill {
    protected BlockData blockData;
    protected long duration;

    public FakeBlockMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);

        String materialName = config.getString(new String[]{"material", "m"}, "STONE");
        try {
            this.blockData = Material.valueOf(materialName.toUpperCase()).createBlockData();
        } catch (Exception e) {
            this.blockData = Material.STONE.createBlockData();
        }

        this.duration = config.getLong(new String[]{"duration", "d"}, 100L);
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
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
        return SkillResult.SUCCESS;
    }
}