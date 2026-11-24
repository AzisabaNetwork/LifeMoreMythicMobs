package net.azisaba.lifemoremythicmobs.freefield;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Keys taken over from FFCore. They keep the {@code ffcore:} namespace so data already saved on players, items and
 * mobs (attribute modifiers, stored counts, battle teams) keeps working after FFCore is removed.
 */
public final class FFKeys {
    public static final NamespacedKey MAX_HEALTH = key("free_field_max_health");
    public static final NamespacedKey RIPPER_TOUGHNESS = key("ripper_armor_toughness");
    public static final NamespacedKey TEAM = key("team");
    public static final NamespacedKey DROP = key("drop");
    public static final NamespacedKey DROP_PER = key("drop_per");
    public static final NamespacedKey NO_HEAL = key("no_heal");
    public static final NamespacedKey WEAK_SIGN = key("weak_sign");

    private FFKeys() {}

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String name) {
        return new NamespacedKey("ffcore", name);
    }

    public static @Nullable String mythicId(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        return MythicBukkit.inst().getItemManager().getMythicTypeFromItem(item);
    }
}
