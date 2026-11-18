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
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.ResourceNodeManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.Arrays;
import java.util.List;

/**
 * 採取・採掘・ギミックスイッチ用のインタラクティブ資源ノードを生成する。
 * <pre>
 * lmResourceNode{label="&b蒼晶鉱脈";b=diamond_ore;db=bedrock;hp=5;respawn=200;onHit=MineHit;onBreak=MineDrop;glow=#00FFFF} @targetlocation
 * </pre>
 */
public class ResourceNodeMechanic extends SkillMechanic implements ITargetedLocationSkill, ITargetedEntitySkill {
    private final ResourceNodeManager.NodeSpec spec;
    private final double yOffset;

    public ResourceNodeMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        String label = config.getString(new String[]{"label", "l"}, "");
        BlockData block = ResourceNodeManager.matchBlock(config.getString(new String[]{"block", "b"}, "diamond_ore"), Material.DIAMOND_ORE);
        String dbName = config.getString(new String[]{"depletedblock", "db"}, "cobblestone");
        BlockData depletedBlock = dbName.equalsIgnoreCase("none") ? null : ResourceNodeManager.matchBlock(dbName, Material.COBBLESTONE);
        float scale = (float) Math.max(0.2, config.getDouble(new String[]{"scale", "s"}, 1.0));
        float hitboxW = (float) Math.max(0.4, config.getDouble(new String[]{"hitboxwidth", "hw"}, scale + 0.15));
        float hitboxH = (float) Math.max(0.4, config.getDouble(new String[]{"hitboxheight", "hh"}, scale + 0.15));
        int hp = Math.max(1, config.getInteger(new String[]{"hp", "health"}, 5));
        int respawn = config.getInteger(new String[]{"respawn", "rs"}, 0);
        int duration = config.getInteger(new String[]{"duration", "d"}, 0);
        int cooldown = Math.max(1, config.getInteger(new String[]{"cooldown", "cd"}, 5));
        this.yOffset = config.getDouble(new String[]{"yoffset", "y"}, 0.0);

        ResourceNodeManager.ClickMode clickMode = ResourceNodeManager.ClickMode.BOTH;
        try {
            clickMode = ResourceNodeManager.ClickMode.valueOf(config.getString(new String[]{"click", "c"}, "BOTH").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }

        String rawReq = config.getString(new String[]{"reqmmid", "tool", "req"}, "");
        List<String> reqMmids = rawReq.isBlank() ? List.of() : Arrays.stream(rawReq.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();

        Color glow = parseColor(config.getString(new String[]{"glow", "gl"}, ""));
        String onHit = config.getString(new String[]{"onhit", "oh"}, null);
        String onBreak = config.getString(new String[]{"onbreak", "ob"}, null);
        String onRespawn = config.getString(new String[]{"onrespawn", "or"}, null);
        String onWrongTool = config.getString(new String[]{"onwrongtool", "owt"}, null);

        this.spec = new ResourceNodeManager.NodeSpec(
                label, block, depletedBlock, scale, hitboxW, hitboxH,
                hp, respawn, duration, cooldown, clickMode, reqMmids, glow,
                onHit, onBreak, onRespawn, onWrongTool
        );
    }

    private static Color parseColor(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            String hex = raw.replace("#", "").replace("\"", "").trim();
            if (hex.length() == 6) return Color.fromRGB(Integer.parseInt(hex, 16));
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        return castAtLocation(data, target.getLocation());
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        Location loc = BukkitAdapter.adapt(target).clone().add(0, yOffset, 0);
        Runnable r = () -> ResourceNodeManager.spawnNode(loc, spec, data);
        if (Bukkit.isPrimaryThread()) r.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), r);
        return SkillResult.SUCCESS;
    }
}
