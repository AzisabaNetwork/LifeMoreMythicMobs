package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * 時計塔 8 層の共鳴反応（刻印＋違う属性の一撃）を、どの Mob でも起こすための入口。
 * プレイヤーが tDamage で属性ダメージを当てたあと、MythicMobs の技 {@code cl8_reso_<属性>} があれば、
 * 当てた相手を唱える人・プレイヤーをきっかけ（@trigger）にして唱えさせる。技が無いサーバーでは何もしない。
 */
public final class ResonanceHook {
    private static final String PREFIX = "cl8_reso_";

    private ResonanceHook() {
    }

    public static void afterTypedDamage(AbstractEntity caster, AbstractEntity target, String element) {
        if (caster == null || target == null || element == null || element.isBlank()) return;
        Entity attacker = caster.getBukkitEntity();
        Entity victim = target.getBukkitEntity();
        if (!(attacker instanceof Player) || !(victim instanceof LivingEntity living) || victim instanceof Player) return;
        if (living.isDead() || !living.isValid()) return;
        String skill = PREFIX + element.trim().toUpperCase(Locale.ROOT);
        try {
            if (MythicBukkit.inst().getSkillManager().getSkill(skill).isEmpty()) return;
            MythicBukkit.inst().getAPIHelper().castSkill(victim, skill, attacker, victim.getLocation(), null, null, 1.0F);
        } catch (RuntimeException ignored) {
            // 反応が起きないだけで、ダメージには影響させない
        }
    }
}
