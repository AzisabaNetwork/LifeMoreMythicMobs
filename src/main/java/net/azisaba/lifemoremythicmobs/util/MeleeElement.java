package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The element of a direct melee hit (ENTITY_ATTACK), set with {@code MeleeElement: FIRE} in a MythicMobs
 * item (the attacker's main-hand item) or mob definition. Without it the hit stays plain ENTITY_ATTACK.
 * Several elements ({@code FIRE,ICE}) are returned as written; the listener picks one per hit.
 */
public final class MeleeElement {
    public static final String KEY = "MeleeElement";

    private MeleeElement() {}

    /** The element of a melee hit by {@code attacker}, or null. */
    public static String of(LivingEntity attacker) {
        if (attacker == null) return null;
        MythicBukkit mm = MythicBukkit.inst();
        if (attacker instanceof Player player) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType().isAir()) return null;
            String id = mm.getItemManager().getMythicTypeFromItem(hand);
            if (id == null || id.isBlank()) return null;
            MythicItem item = mm.getItemManager().getItem(id).orElse(null);
            return item == null ? null : clean(item.getConfig().getString(KEY, null));
        }
        ActiveMob mob = mm.getMobManager().getMythicMobInstance(attacker);
        return mob == null ? null : clean(mob.getType().getConfig().getString(KEY, null));
    }

    private static String clean(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        return value.isEmpty() ? null : value;
    }
}
