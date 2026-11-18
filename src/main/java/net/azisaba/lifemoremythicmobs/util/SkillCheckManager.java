package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ActionBar QTE (timing skill check) manager for mining, fishing, crafting, and parry minigames.
 */
public final class SkillCheckManager {
    private static final Map<UUID, ActiveCheck> ACTIVE = new ConcurrentHashMap<>();

    private SkillCheckManager() {
    }

    public enum InputType {
        SNEAK, CLICK
    }

    public static final class CheckSpec {
        public final String label;
        public final double speed;
        public final int durationTicks;
        public final double targetCenter; // < 0 means random [0.25, 0.75]
        public final double greatRadius;
        public final double goodRadius;
        public final InputType inputType;
        public final String onGreatSkill;
        public final String onGoodSkill;
        public final String onFailSkill;

        public CheckSpec(String label, double speed, int durationTicks, double targetCenter,
                         double greatRadius, double goodRadius, InputType inputType,
                         String onGreatSkill, String onGoodSkill, String onFailSkill) {
            this.label = label;
            this.speed = Math.max(0.01, Math.min(0.25, speed));
            this.durationTicks = Math.max(10, durationTicks);
            this.targetCenter = targetCenter;
            this.greatRadius = Math.max(0.02, greatRadius);
            this.goodRadius = Math.max(this.greatRadius, goodRadius);
            this.inputType = inputType;
            this.onGreatSkill = onGreatSkill;
            this.onGoodSkill = onGoodSkill;
            this.onFailSkill = onFailSkill;
        }
    }

    public static SkillCheckMath.CheckResult evaluateZone(double pos, double center, double greatRadius, double goodRadius) {
        return SkillCheckMath.evaluateZone(pos, center, greatRadius, goodRadius);
    }

    public static String renderBar(String label, double pos, double center, double greatRadius, double goodRadius, int slots) {
        return SkillCheckMath.renderBar(label, pos, center, greatRadius, goodRadius, slots);
    }

    public static void start(Player player, CheckSpec spec, SkillMetadata data) {
        if (player == null) return;
        ActiveCheck old = ACTIVE.remove(player.getUniqueId());
        if (old != null) old.cancelOnly();

        double center = spec.targetCenter >= 0.0
                ? Math.max(0.1, Math.min(0.9, spec.targetCenter))
                : 0.25 + ThreadLocalRandom.current().nextDouble() * 0.5;

        ActiveCheck check = new ActiveCheck(player, spec, data.deepClone(), center);
        ACTIVE.put(player.getUniqueId(), check);
        check.runTaskTimer(LifeMoreMythicMobs.inst(), 0L, 1L);
    }

    public static boolean handleInput(Player player, InputType type) {
        ActiveCheck check = ACTIVE.get(player.getUniqueId());
        if (check == null || check.spec.inputType != type) return false;
        if (ACTIVE.remove(player.getUniqueId(), check)) {
            check.triggerNow();
        }
        return true;
    }

    public static void clearAll() {
        for (ActiveCheck check : ACTIVE.values()) {
            check.cancelOnly();
        }
        ACTIVE.clear();
    }

    private static final class ActiveCheck extends BukkitRunnable {
        private final Player player;
        private final CheckSpec spec;
        private final SkillMetadata data;
        private final double center;
        private double pos = 0.0;
        private double dir = 1.0;
        private int elapsed = 0;

        ActiveCheck(Player player, CheckSpec spec, SkillMetadata data, double center) {
            this.player = player;
            this.spec = spec;
            this.data = data;
            this.center = center;
        }

        @Override
        public void run() {
            if (!player.isOnline() || player.isDead()) {
                ACTIVE.remove(player.getUniqueId(), this);
                cancelOnly();
                return;
            }
            if (elapsed >= spec.durationTicks) {
                if (ACTIVE.remove(player.getUniqueId(), this)) {
                    finish(SkillCheckMath.CheckResult.FAIL);
                }
                return;
            }

            pos += dir * spec.speed;
            if (pos >= 1.0) {
                pos = 1.0;
                dir = -1.0;
            } else if (pos <= 0.0) {
                pos = 0.0;
                dir = 1.0;
            }

            String hint = spec.inputType == InputType.SNEAK ? " &7[Shift]" : " &7[左クリック]";
            String bar = renderBar(spec.label, pos, center, spec.greatRadius, spec.goodRadius, 24) + hint;
            player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(bar));
            elapsed++;
        }

        void triggerNow() {
            SkillCheckMath.CheckResult result = evaluateZone(pos, center, spec.greatRadius, spec.goodRadius);
            finish(result);
        }

        private void finish(SkillCheckMath.CheckResult result) {
            cancelOnly();
            String msg;
            String skill;
            Sound sound;
            float pitch;
            switch (result) {
                case GREAT -> {
                    msg = "&6&l★ GREAT! ★";
                    skill = spec.onGreatSkill;
                    sound = Sound.ENTITY_PLAYER_LEVELUP;
                    pitch = 1.8f;
                }
                case GOOD -> {
                    msg = "&a&lGOOD!";
                    skill = spec.onGoodSkill;
                    sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                    pitch = 1.3f;
                }
                default -> {
                    msg = "&c&lFAIL...";
                    skill = spec.onFailSkill;
                    sound = Sound.BLOCK_NOTE_BLOCK_BASS;
                    pitch = 0.6f;
                }
            }
            player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
            player.playSound(player.getLocation(), sound, 0.8f, pitch);
            SkillUtil.executeSkill(skill, data, BukkitAdapter.adapt(player));
        }

        void cancelOnly() {
            try {
                this.cancel();
            } catch (IllegalStateException ignored) {
            }
        }
    }
}
