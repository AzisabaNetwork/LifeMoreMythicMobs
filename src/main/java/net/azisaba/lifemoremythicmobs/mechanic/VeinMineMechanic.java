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
import net.azisaba.lifemoremythicmobs.util.SkillUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * 隣接する同種ブロックや指定ブロック群を一括採掘・置換するメカニクス（鉱脈一括破壊・伐採向け）。
 * <pre>
 * lmVeinMine{m=COAL_ORE,DEEPSLATE_COAL_ORE;limit=24;drop=true;useTool=true;var=vein_count;onBlock=MineSpark} @targetlocation
 * </pre>
 */
public class VeinMineMechanic extends SkillMechanic implements ITargetedLocationSkill, ITargetedEntitySkill {
    private final Set<Material> targetMaterials;
    private final int limit;
    private final int maxRadius;
    private final boolean diagonal;
    private final boolean drop;
    private final boolean useTool;
    private final boolean respectProtection;
    private final Material replaceMaterial;
    private final String countVar;
    private final String onBlockSkill;

    public VeinMineMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        String rawMats = config.getString(new String[]{"materials", "m"}, "");
        Set<Material> mats = EnumSet.noneOf(Material.class);
        if (!rawMats.isBlank()) {
            for (String s : rawMats.split(",")) {
                Material m = Material.matchMaterial(s.trim());
                if (m != null && m.isBlock()) mats.add(m);
            }
        }
        this.targetMaterials = mats;
        this.limit = Math.max(1, Math.min(256, config.getInteger(new String[]{"limit", "l"}, 16)));
        this.maxRadius = Math.max(1, Math.min(24, config.getInteger(new String[]{"radius", "r"}, 8)));
        this.diagonal = config.getBoolean(new String[]{"diagonal", "diag"}, true);
        this.drop = config.getBoolean(new String[]{"drop", "d"}, true);
        this.useTool = config.getBoolean(new String[]{"usetool", "ut"}, true);
        this.respectProtection = config.getBoolean(new String[]{"respectprotection", "rp"}, true);
        String rep = config.getString(new String[]{"replace", "rep"}, "");
        Material repMat = rep.isBlank() ? null : Material.matchMaterial(rep.trim());
        this.replaceMaterial = (repMat != null && repMat.isBlock()) ? repMat : null;
        this.countVar = config.getString(new String[]{"var", "v"}, "lmmm_vein_count");
        this.onBlockSkill = config.getString(new String[]{"onblock", "ob"}, null);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        return castAtLocation(data, target.getLocation());
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        Location loc = BukkitAdapter.adapt(target);
        Runnable r = () -> executeVeinMine(data, loc);
        if (Bukkit.isPrimaryThread()) r.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), r);
        return SkillResult.SUCCESS;
    }

    private void executeVeinMine(SkillMetadata data, Location origin) {
        World world = origin.getWorld();
        if (world == null) return;
        Block start = origin.getBlock();
        Material startType = start.getType();
        if (startType.isAir()) return;
        if (!targetMaterials.isEmpty() && !targetMaterials.contains(startType)) return;

        Set<Material> matchSet = targetMaterials.isEmpty() ? EnumSet.of(startType) : targetMaterials;
        List<Block> collected = collectVein(start, matchSet, limit, maxRadius, diagonal);

        Player player = null;
        if (data.getCaster() != null && data.getCaster().getEntity() != null) {
            Entity e = BukkitAdapter.adapt(data.getCaster().getEntity());
            if (e instanceof Player p) player = p;
        }
        ItemStack tool = (player != null && useTool) ? player.getInventory().getItemInMainHand() : null;

        int affected = 0;
        for (Block b : collected) {
            if (respectProtection && player != null) {
                BlockBreakEvent check = new BlockBreakEvent(b, player);
                check.setDropItems(drop);
                Bukkit.getPluginManager().callEvent(check);
                if (check.isCancelled()) continue;
            }
            Location blockCenter = b.getLocation().add(0.5, 0.5, 0.5);
            if (replaceMaterial != null) {
                if (drop) {
                    if (tool != null) b.getDrops(tool, player).forEach(is -> world.dropItemNaturally(blockCenter, is));
                    else b.getDrops().forEach(is -> world.dropItemNaturally(blockCenter, is));
                }
                b.setType(replaceMaterial);
            } else if (drop) {
                if (tool != null) b.breakNaturally(tool);
                else b.breakNaturally();
            } else {
                b.setType(Material.AIR);
            }
            affected++;

            if (onBlockSkill != null && !onBlockSkill.isEmpty()) {
                SkillMetadata clone = data.deepClone();
                clone.setOrigin(BukkitAdapter.adapt(blockCenter));
                SkillUtil.executeSkill(onBlockSkill, clone, data.getCaster() != null ? data.getCaster().getEntity() : null);
            }
        }

        if (countVar != null && !countVar.isBlank() && data.getVariables() != null) {
            data.getVariables().putInt(countVar, affected);
        }
    }

    public static List<Block> collectVein(Block start, Set<Material> validMaterials, int limit, int maxRadius, boolean diagonal) {
        List<Block> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<Block> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(packBlockKey(start.getX(), start.getY(), start.getZ()));

        int sx = start.getX(), sy = start.getY(), sz = start.getZ();
        while (!queue.isEmpty() && result.size() < limit) {
            Block cur = queue.poll();
            if (!validMaterials.contains(cur.getType())) continue;
            result.add(cur);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        if (!diagonal && Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1) continue;
                        int nx = cur.getX() + dx;
                        int ny = cur.getY() + dy;
                        int nz = cur.getZ() + dz;
                        if (Math.abs(nx - sx) > maxRadius || Math.abs(ny - sy) > maxRadius || Math.abs(nz - sz) > maxRadius) {
                            continue;
                        }
                        long key = packBlockKey(nx, ny, nz);
                        if (visited.add(key)) {
                            Block nb = cur.getWorld().getBlockAt(nx, ny, nz);
                            if (validMaterials.contains(nb.getType())) {
                                queue.add(nb);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static long packBlockKey(int x, int y, int z) {
        return (((long) x & 0x3FFFFFFL) << 38) | (((long) z & 0x3FFFFFFL) << 12) | ((long) y & 0xFFFL);
    }
}
