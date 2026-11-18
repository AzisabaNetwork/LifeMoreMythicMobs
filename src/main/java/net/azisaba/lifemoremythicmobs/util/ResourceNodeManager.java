package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages interactive custom resource nodes (mining veins, herbs, chests, switches).
 */
public final class ResourceNodeManager {
    private static final Map<UUID, NodeInstance> NODES = new ConcurrentHashMap<>();

    private ResourceNodeManager() {
    }

    public enum ClickMode {
        BOTH, RIGHT, LEFT
    }

    public static final class NodeSpec {
        public final String label;
        public final BlockData blockData;
        public final BlockData depletedBlockData;
        public final float scale;
        public final float hitboxWidth;
        public final float hitboxHeight;
        public final int maxHp;
        public final int respawnTicks;
        public final int durationTicks;
        public final int hitCooldownTicks;
        public final ClickMode clickMode;
        public final List<String> requiredMmids;
        public final Color glowColor;
        public final String onHitSkill;
        public final String onBreakSkill;
        public final String onRespawnSkill;
        public final String onWrongToolSkill;

        public NodeSpec(String label, BlockData blockData, BlockData depletedBlockData,
                        float scale, float hitboxWidth, float hitboxHeight,
                        int maxHp, int respawnTicks, int durationTicks, int hitCooldownTicks,
                        ClickMode clickMode, List<String> requiredMmids, Color glowColor,
                        String onHitSkill, String onBreakSkill, String onRespawnSkill, String onWrongToolSkill) {
            this.label = label;
            this.blockData = blockData;
            this.depletedBlockData = depletedBlockData;
            this.scale = scale;
            this.hitboxWidth = hitboxWidth;
            this.hitboxHeight = hitboxHeight;
            this.maxHp = Math.max(1, maxHp);
            this.respawnTicks = respawnTicks;
            this.durationTicks = durationTicks;
            this.hitCooldownTicks = Math.max(1, hitCooldownTicks);
            this.clickMode = clickMode;
            this.requiredMmids = requiredMmids;
            this.glowColor = glowColor;
            this.onHitSkill = onHitSkill;
            this.onBreakSkill = onBreakSkill;
            this.onRespawnSkill = onRespawnSkill;
            this.onWrongToolSkill = onWrongToolSkill;
        }
    }

    public static final class NodeInstance {
        private final UUID interactionId;
        private final Interaction interaction;
        private final BlockDisplay blockDisplay;
        private final TextDisplay textDisplay;
        private final NodeSpec spec;
        private final SkillMetadata baseData;
        private final Location originLoc;
        private final Map<UUID, Long> playerCooldowns = new ConcurrentHashMap<>();

        private int hp;
        private boolean depleted;
        private BukkitTask respawnTask;
        private BukkitTask expireTask;

        NodeInstance(Interaction interaction, BlockDisplay blockDisplay, TextDisplay textDisplay,
                     NodeSpec spec, SkillMetadata baseData, Location originLoc) {
            this.interactionId = interaction.getUniqueId();
            this.interaction = interaction;
            this.blockDisplay = blockDisplay;
            this.textDisplay = textDisplay;
            this.spec = spec;
            this.baseData = baseData.deepClone();
            this.baseData.setOrigin(BukkitAdapter.adapt(originLoc));
            this.originLoc = originLoc.clone();
            this.hp = spec.maxHp;
            this.depleted = false;
        }

        public void updateVisuals() {
            if (blockDisplay != null && blockDisplay.isValid()) {
                if (depleted) {
                    if (spec.depletedBlockData != null) {
                        blockDisplay.setBlock(spec.depletedBlockData);
                        blockDisplay.setTransformation(centeredTransform(spec.scale * 0.8f));
                    } else {
                        blockDisplay.setTransformation(centeredTransform(0.01f));
                    }
                } else {
                    blockDisplay.setBlock(spec.blockData);
                    blockDisplay.setTransformation(centeredTransform(spec.scale));
                }
            }
            if (textDisplay != null && textDisplay.isValid()) {
                String text;
                if (depleted) {
                    text = (spec.label.isEmpty() ? "" : spec.label + "\n") + "&7[再生中...]";
                } else {
                    String bar = buildBar(hp, spec.maxHp);
                    text = (spec.label.isEmpty() ? "" : spec.label + "\n") + bar + " &f" + hp + "/" + spec.maxHp;
                }
                textDisplay.text(LegacyComponentSerializer.legacyAmpersand().deserialize(text));
            }
        }

        void handleClick(Player player, boolean isRightClick) {
            if (depleted) return;
            if (spec.clickMode == ClickMode.RIGHT && !isRightClick) return;
            if (spec.clickMode == ClickMode.LEFT && isRightClick) return;

            long now = System.currentTimeMillis();
            long minIntervalMs = spec.hitCooldownTicks * 50L;
            Long last = playerCooldowns.get(player.getUniqueId());
            if (last != null && now - last < minIntervalMs) return;
            playerCooldowns.put(player.getUniqueId(), now);

            if (!spec.requiredMmids.isEmpty()) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                String mmid = ItemUtil.getMythicType(hand);
                boolean matched = mmid != null && spec.requiredMmids.stream().anyMatch(req -> req.equalsIgnoreCase(mmid));
                if (!matched) {
                    SkillUtil.executeSkill(spec.onWrongToolSkill, baseData, BukkitAdapter.adapt(player));
                    return;
                }
            }

            hp--;
            pulseBlock();
            SkillUtil.executeSkill(spec.onHitSkill, baseData, BukkitAdapter.adapt(player));

            if (hp <= 0) {
                depleted = true;
                updateVisuals();
                SkillUtil.executeSkill(spec.onBreakSkill, baseData, BukkitAdapter.adapt(player));

                if (spec.respawnTicks > 0) {
                    respawnTask = Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(), () -> {
                        if (!interaction.isValid()) return;
                        hp = spec.maxHp;
                        depleted = false;
                        updateVisuals();
                        SkillUtil.executeSkill(spec.onRespawnSkill, baseData, null);
                    }, spec.respawnTicks);
                } else {
                    removeNode(interactionId);
                }
            } else {
                updateVisuals();
            }
        }

        private void pulseBlock() {
            if (blockDisplay == null || !blockDisplay.isValid() || depleted) return;
            blockDisplay.setInterpolationDuration(2);
            blockDisplay.setInterpolationDelay(0);
            blockDisplay.setTransformation(centeredTransform(spec.scale * 0.85f));
            Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(), () -> {
                if (blockDisplay.isValid() && !depleted) {
                    blockDisplay.setInterpolationDuration(3);
                    blockDisplay.setInterpolationDelay(0);
                    blockDisplay.setTransformation(centeredTransform(spec.scale));
                }
            }, 2L);
        }

        void destroy() {
            if (respawnTask != null) respawnTask.cancel();
            if (expireTask != null) expireTask.cancel();
            ManagedEntityRegistry.removeAndUntrack(interaction);
            ManagedEntityRegistry.removeAndUntrack(blockDisplay);
            ManagedEntityRegistry.removeAndUntrack(textDisplay);
        }
    }

    public static String buildBar(int current, int max) {
        return GeometryUtil.buildProgressBar(current, max);
    }

    private static Transformation centeredTransform(float scale) {
        float half = scale / 2f;
        return new Transformation(
                new Vector3f(-half, 0f, -half),
                new Quaternionf(),
                new Vector3f(scale, scale, scale),
                new Quaternionf()
        );
    }

    public static NodeInstance spawnNode(Location loc, NodeSpec spec, SkillMetadata data) {
        if (loc == null || loc.getWorld() == null) return null;
        Location base = loc.clone();

        Interaction interaction = base.getWorld().spawn(base, Interaction.class, inter -> {
            inter.setInteractionWidth(spec.hitboxWidth);
            inter.setInteractionHeight(spec.hitboxHeight);
            inter.setResponsive(true);
            ManagedEntityRegistry.track(inter);
        });

        BlockDisplay blockDisplay = base.getWorld().spawn(base, BlockDisplay.class, bd -> {
            bd.setBlock(spec.blockData);
            bd.setTransformation(centeredTransform(spec.scale));
            bd.setBrightness(new Display.Brightness(15, 15));
            if (spec.glowColor != null) {
                bd.setGlowing(true);
                bd.setGlowColorOverride(spec.glowColor);
            }
            ManagedEntityRegistry.track(bd);
        });

        TextDisplay textDisplay = base.getWorld().spawn(base.clone().add(0, spec.hitboxHeight + 0.35, 0), TextDisplay.class, td -> {
            td.setBillboard(Display.Billboard.CENTER);
            td.setShadowed(true);
            ManagedEntityRegistry.track(td);
        });

        NodeInstance instance = new NodeInstance(interaction, blockDisplay, textDisplay, spec, data, base);
        instance.updateVisuals();
        NODES.put(interaction.getUniqueId(), instance);

        if (spec.durationTicks > 0) {
            instance.expireTask = Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(),
                    () -> removeNode(interaction.getUniqueId()), spec.durationTicks);
        }
        return instance;
    }

    public static boolean handleInteraction(UUID entityId, Player player, boolean isRightClick) {
        NodeInstance node = NODES.get(entityId);
        if (node == null) return false;
        node.handleClick(player, isRightClick);
        return true;
    }

    public static void removeNode(UUID interactionId) {
        NodeInstance node = NODES.remove(interactionId);
        if (node != null) {
            node.destroy();
        }
    }

    public static void clearAll() {
        for (NodeInstance node : NODES.values()) {
            node.destroy();
        }
        NODES.clear();
    }

    public static int getActiveCount() {
        return NODES.size();
    }

    public static BlockData matchBlock(String name, Material fallback) {
        if (name == null || name.equalsIgnoreCase("none") || name.isBlank()) return null;
        Material m = Material.matchMaterial(name);
        return (m == null || !m.isBlock() ? fallback : m).createBlockData();
    }
}
