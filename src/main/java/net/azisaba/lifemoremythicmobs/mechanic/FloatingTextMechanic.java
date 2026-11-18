package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.ManagedEntityRegistry;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 空間やエンティティ頭上に TextDisplay を表示・追従・動的更新するメカニクス。
 * <pre>
 * lmFloatingText{id=cast;t="&e詠唱中... &f<caster.var.cast_sec>";y=2.4;d=40;i=2;follow=true;scale=1.2} @self
 * lmFloatingText{id=cast;remove=true} @self
 * </pre>
 */
public class FloatingTextMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {
    private static final Map<String, FloatingEntry> ACTIVE = new ConcurrentHashMap<>();

    private final String id;
    private final PlaceholderString text;
    private final double yOffset;
    private final int duration;
    private final int interval;
    private final boolean follow;
    private final float scale;
    private final Display.Billboard billboard;
    private final boolean shadow;
    private final boolean seeThrough;
    private final Color backgroundColor;
    private final int lineWidth;
    private final boolean remove;

    public FloatingTextMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.id = config.getString(new String[]{"id"}, "ft");
        this.text = PlaceholderString.of(config.getString(new String[]{"text", "t"}, ""));
        this.yOffset = config.getDouble(new String[]{"yoffset", "y"}, 2.3);
        this.duration = Math.max(1, config.getInteger(new String[]{"duration", "d"}, 60));
        this.interval = Math.max(1, config.getInteger(new String[]{"interval", "i"}, 5));
        this.follow = config.getBoolean(new String[]{"follow", "f"}, true);
        this.scale = (float) Math.max(0.1, config.getDouble(new String[]{"scale", "s"}, 1.0));
        this.billboard = parseBillboard(config.getString(new String[]{"billboard", "bb"}, "CENTER"));
        this.shadow = config.getBoolean(new String[]{"shadow", "sh"}, true);
        this.seeThrough = config.getBoolean(new String[]{"seethrough", "st"}, false);
        this.backgroundColor = parseBackground(config.getString(new String[]{"background", "bg"}, ""));
        this.lineWidth = Math.max(40, config.getInteger(new String[]{"linewidth", "lw"}, 200));
        this.remove = config.getBoolean(new String[]{"remove", "r"}, false);
    }

    private static Display.Billboard parseBillboard(String raw) {
        try {
            return Display.Billboard.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Display.Billboard.CENTER;
        }
    }

    private static Color parseBackground(String raw) {
        if (raw == null || raw.isBlank()) return null;
        if (raw.equalsIgnoreCase("transparent") || raw.equalsIgnoreCase("none")) {
            return Color.fromARGB(0, 0, 0, 0);
        }
        try {
            String hex = raw.replace("#", "").replace("\"", "").trim();
            if (hex.length() == 8) {
                long argb = Long.parseLong(hex, 16);
                return Color.fromARGB(
                        (int) ((argb >> 24) & 0xFF),
                        (int) ((argb >> 16) & 0xFF),
                        (int) ((argb >> 8) & 0xFF),
                        (int) (argb & 0xFF)
                );
            }
            if (hex.length() == 6) {
                return Color.fromRGB(Integer.parseInt(hex, 16));
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        String key = id + ":" + target.getUniqueId();
        Runnable r = () -> spawnOrUpdate(key, data, target, BukkitAdapter.adapt(target).getLocation());
        if (Bukkit.isPrimaryThread()) r.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), r);
        return SkillResult.SUCCESS;
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        Location loc = BukkitAdapter.adapt(target);
        String key = id + ":" + (loc.getWorld() != null ? loc.getWorld().getName() : "w")
                + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
        Runnable r = () -> spawnOrUpdate(key, data, null, loc);
        if (Bukkit.isPrimaryThread()) r.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), r);
        return SkillResult.SUCCESS;
    }

    private void spawnOrUpdate(String key, SkillMetadata data, AbstractEntity target, Location baseLoc) {
        if (remove) {
            FloatingEntry old = ACTIVE.remove(key);
            if (old != null) old.cleanup();
            return;
        }
        if (baseLoc == null || baseLoc.getWorld() == null) return;

        FloatingEntry existing = ACTIVE.get(key);
        if (existing != null && existing.display.isValid()) {
            existing.refresh(data, target, renderText(data, target));
            return;
        }
        if (existing != null) {
            existing.cleanup();
            ACTIVE.remove(key);
        }

        Location spawnLoc = baseLoc.clone().add(0, yOffset, 0);
        TextDisplay display = spawnLoc.getWorld().spawn(spawnLoc, TextDisplay.class, td -> {
            td.setBillboard(billboard);
            td.setShadowed(shadow);
            td.setSeeThrough(seeThrough);
            td.setLineWidth(lineWidth);
            if (backgroundColor != null) {
                td.setBackgroundColor(backgroundColor);
            }
            td.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf(),
                    new Vector3f(scale, scale, scale),
                    new Quaternionf()
            ));
            td.text(LegacyComponentSerializer.legacyAmpersand().deserialize(renderText(data, target)));
            ManagedEntityRegistry.track(td);
        });

        FloatingEntry entry = new FloatingEntry(key, display, data.deepClone(), target);
        ACTIVE.put(key, entry);
        entry.runTaskTimer(LifeMoreMythicMobs.inst(), interval, interval);
    }

    private String renderText(SkillMetadata data, AbstractEntity target) {
        String raw = target != null ? text.get(data, target) : text.get(data);
        return raw.replace("<nl>", "\n").replace("\\n", "\n");
    }

    private final class FloatingEntry extends BukkitRunnable {
        private final String key;
        private final TextDisplay display;
        private SkillMetadata data;
        private AbstractEntity target;
        private int remaining;

        FloatingEntry(String key, TextDisplay display, SkillMetadata data, AbstractEntity target) {
            this.key = key;
            this.display = display;
            this.data = data;
            this.target = target;
            this.remaining = duration;
        }

        void refresh(SkillMetadata newData, AbstractEntity newTarget, String rendered) {
            this.data = newData.deepClone();
            this.target = newTarget;
            this.remaining = duration;
            display.text(LegacyComponentSerializer.legacyAmpersand().deserialize(rendered));
        }

        @Override
        public void run() {
            remaining -= interval;
            if (remaining <= 0 || !display.isValid()) {
                ACTIVE.remove(key, this);
                cleanup();
                return;
            }
            if (follow && target != null) {
                Entity bukkitTarget = BukkitAdapter.adapt(target);
                if (bukkitTarget == null || !bukkitTarget.isValid() || bukkitTarget.isDead()) {
                    ACTIVE.remove(key, this);
                    cleanup();
                    return;
                }
                display.teleport(bukkitTarget.getLocation().clone().add(0, yOffset, 0));
            }
            display.text(LegacyComponentSerializer.legacyAmpersand().deserialize(renderText(data, target)));
        }

        void cleanup() {
            try {
                this.cancel();
            } catch (IllegalStateException ignored) {
            }
            ManagedEntityRegistry.removeAndUntrack(display);
        }
    }
}
