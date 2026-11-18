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
import net.azisaba.lifemoremythicmobs.util.GeometryUtil;
import net.azisaba.lifemoremythicmobs.util.ManagedEntityRegistry;
import net.azisaba.lifemoremythicmobs.util.SkillUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 塔踏み・頭割り・指定人数待機ゾーンギミック。
 * <pre>
 * lmTowerZone{r=3.5;req=2;d=100;label="&e塔";onSuccess=TowerClear;onFail=TowerExplode} @selflocation
 * </pre>
 */
public class TowerZoneMechanic extends SkillMechanic implements ITargetedLocationSkill, ITargetedEntitySkill {
    private final double radius;
    private final int requiredPlayers;
    private final int maxPlayers;
    private final int duration;
    private final int interval;
    private final boolean instantSuccess;
    private final boolean snapGround;
    private final String label;
    private final BlockData pendingBlock;
    private final BlockData readyBlock;
    private final String onSuccessSkill;
    private final String onFailSkill;
    private final String onEnterSkill;
    private final String onLeaveSkill;
    private final String onTickSkill;

    public TowerZoneMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.radius = Math.max(0.5, config.getDouble(new String[]{"radius", "r"}, 3.0));
        this.requiredPlayers = Math.max(0, config.getInteger(new String[]{"required", "req"}, 1));
        this.maxPlayers = config.getInteger(new String[]{"maxplayers", "max"}, -1);
        this.duration = Math.max(1, config.getInteger(new String[]{"duration", "d"}, 100));
        this.interval = Math.max(1, config.getInteger(new String[]{"interval", "i"}, 2));
        this.instantSuccess = config.getBoolean(new String[]{"instant", "inst"}, false);
        this.snapGround = config.getBoolean(new String[]{"ground", "g"}, true);
        this.label = config.getString(new String[]{"label", "l"}, "");
        this.pendingBlock = parseBlock(config.getString(new String[]{"pendingblock", "pb"}, "red_concrete"), Material.RED_CONCRETE);
        this.readyBlock = parseBlock(config.getString(new String[]{"readyblock", "rb"}, "lime_concrete"), Material.LIME_CONCRETE);
        this.onSuccessSkill = config.getString(new String[]{"onsuccess", "os"}, null);
        this.onFailSkill = config.getString(new String[]{"onfail", "of"}, null);
        this.onEnterSkill = config.getString(new String[]{"onenter", "oen"}, null);
        this.onLeaveSkill = config.getString(new String[]{"onleave", "ol"}, null);
        this.onTickSkill = config.getString(new String[]{"ontick", "ot"}, null);
    }

    private static BlockData parseBlock(String name, Material fallback) {
        Material m = Material.matchMaterial(name);
        return (m == null || !m.isBlock() ? fallback : m).createBlockData();
    }

    public static boolean isSatisfied(int count, int required, int max) {
        return GeometryUtil.isCountInRange(count, required, max);
    }

    private static Location toGround(Location loc) {
        World w = loc.getWorld();
        if (w == null) return loc;
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int y = loc.getBlockY();
        for (int up = 0; up < 4 && !w.getBlockAt(x, y, z).isPassable(); up++) y++;
        for (int dn = 0; dn < 32; dn++) {
            if (!w.getBlockAt(x, y - 1, z).isPassable()) break;
            y--;
        }
        Location out = loc.clone();
        out.setY(y + 0.05);
        return out;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        return castAtLocation(data, target.getLocation());
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        Location loc = BukkitAdapter.adapt(target).clone();
        if (snapGround) loc = toGround(loc);
        Location finalLoc = loc;
        Runnable start = () -> new TowerTask(data, finalLoc).start();
        if (Bukkit.isPrimaryThread()) start.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), start);
        return SkillResult.SUCCESS;
    }

    private final class TowerTask extends BukkitRunnable {
        private final SkillMetadata data;
        private final Location center;
        private final List<BlockDisplay> ringDisplays = new ArrayList<>();
        private final Set<UUID> insidePlayers = new HashSet<>();
        private TextDisplay textDisplay;
        private boolean lastReadyState = false;
        private int elapsed = 0;

        TowerTask(SkillMetadata data, Location center) {
            this.data = data.deepClone();
            this.data.setOrigin(BukkitAdapter.adapt(center));
            this.center = center;
        }

        void start() {
            World w = center.getWorld();
            if (w == null) return;

            int segments = Math.max(16, (int) Math.ceil(radius * 6));
            float segLen = (float) (2.0 * Math.PI * radius / segments) + 0.08f;
            float thickness = 0.22f;
            for (int i = 0; i < segments; i++) {
                double angle = 2.0 * Math.PI * i / segments;
                double x = center.getX() + Math.cos(angle) * radius;
                double z = center.getZ() + Math.sin(angle) * radius;
                Location segLoc = new Location(w, x, center.getY(), z);
                BlockDisplay bd = w.spawn(segLoc, BlockDisplay.class, d -> {
                    d.setBlock(pendingBlock);
                    d.setBrightness(new Display.Brightness(15, 15));
                    d.setTransformation(new Transformation(
                            new Vector3f(-thickness / 2f, 0f, -segLen / 2f),
                            new Quaternionf(new AxisAngle4f((float) -angle, 0, 1, 0)),
                            new Vector3f(thickness, 0.05f, segLen),
                            new Quaternionf()
                    ));
                    ManagedEntityRegistry.track(d);
                });
                ringDisplays.add(bd);
            }

            Location textLoc = center.clone().add(0, 2.2, 0);
            textDisplay = w.spawn(textLoc, TextDisplay.class, td -> {
                td.setBillboard(Display.Billboard.CENTER);
                td.setShadowed(true);
                td.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new Quaternionf(),
                        new Vector3f(1.25f, 1.25f, 1.25f),
                        new Quaternionf()
                ));
                ManagedEntityRegistry.track(td);
            });

            updateVisuals(0);
            this.runTaskTimer(LifeMoreMythicMobs.inst(), 0L, interval);
        }

        private void updateVisuals(int count) {
            boolean ready = isSatisfied(count, requiredPlayers, maxPlayers);
            if (ready != lastReadyState) {
                lastReadyState = ready;
                BlockData bd = ready ? readyBlock : pendingBlock;
                for (BlockDisplay d : ringDisplays) {
                    if (d.isValid()) d.setBlock(bd);
                }
            }
            if (textDisplay != null && textDisplay.isValid()) {
                double remSec = Math.max(0, (duration - elapsed) / 20.0);
                String color = ready ? "&a" : "&c";
                String reqText = maxPlayers >= 0 && maxPlayers != requiredPlayers
                        ? requiredPlayers + "-" + maxPlayers
                        : String.valueOf(requiredPlayers);
                String prefix = label.isEmpty() ? "" : label + " &r";
                String msg = String.format("%s%s%d / %s &7(%.1fs)", prefix, color, count, reqText, remSec);
                textDisplay.text(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
            }
        }

        @Override
        public void run() {
            World w = center.getWorld();
            if (w == null) {
                cleanup();
                return;
            }

            Set<UUID> current = new HashSet<>();
            List<Player> currentPlayers = new ArrayList<>();
            double rSq = radius * radius;
            for (Entity e : w.getNearbyEntities(center, radius + 0.5, 3.0, radius + 0.5)) {
                if (!(e instanceof Player p) || p.isDead()) continue;
                double dx = p.getLocation().getX() - center.getX();
                double dz = p.getLocation().getZ() - center.getZ();
                if (dx * dx + dz * dz <= rSq) {
                    current.add(p.getUniqueId());
                    currentPlayers.add(p);
                }
            }

            for (Player p : currentPlayers) {
                if (!insidePlayers.contains(p.getUniqueId())) {
                    SkillUtil.executeSkill(onEnterSkill, data, BukkitAdapter.adapt(p));
                }
            }
            for (UUID prev : insidePlayers) {
                if (!current.contains(prev)) {
                    Player p = Bukkit.getPlayer(prev);
                    if (p != null) {
                        SkillUtil.executeSkill(onLeaveSkill, data, BukkitAdapter.adapt(p));
                    }
                }
            }
            insidePlayers.clear();
            insidePlayers.addAll(current);

            int count = currentPlayers.size();
            boolean ready = isSatisfied(count, requiredPlayers, maxPlayers);
            updateVisuals(count);
            SkillUtil.executeSkill(onTickSkill, data, null);

            if (instantSuccess && ready) {
                fireOutcome(onSuccessSkill, currentPlayers);
                cleanup();
                return;
            }

            if (elapsed >= duration) {
                if (ready) {
                    fireOutcome(onSuccessSkill, currentPlayers);
                } else {
                    fireOutcome(onFailSkill, currentPlayers);
                }
                cleanup();
                return;
            }
            elapsed += interval;
        }

        private void fireOutcome(String skill, List<Player> players) {
            if (skill == null || skill.isEmpty()) return;
            if (players.isEmpty()) {
                SkillUtil.executeSkill(skill, data, null);
            } else {
                for (Player p : players) {
                    SkillUtil.executeSkill(skill, data, BukkitAdapter.adapt(p));
                }
            }
        }

        private void cleanup() {
            for (BlockDisplay bd : ringDisplays) {
                ManagedEntityRegistry.removeAndUntrack(bd);
            }
            ringDisplays.clear();
            ManagedEntityRegistry.removeAndUntrack(textDisplay);
            this.cancel();
        }
    }
}
