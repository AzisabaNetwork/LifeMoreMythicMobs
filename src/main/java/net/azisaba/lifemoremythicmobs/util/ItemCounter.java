package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.core.skills.SkillString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A number stored on an item (PersistentDataContainer, as a string such as "20.0"), with an optional lore line
 * that shows it. Used by {@code addItemCount} and {@code itemCount}.
 */
public final class ItemCounter {
    private final EquipmentSlot slot;
    private final NamespacedKey key;
    private final @Nullable String lore;

    public ItemCounter(String slot, String key, @Nullable String lore) {
        this.slot = parseSlot(slot);
        NamespacedKey parsed = NamespacedKey.fromString(key.toLowerCase(Locale.ROOT));
        if (parsed == null) {
            throw new IllegalArgumentException("invalid key: " + key);
        }
        this.key = parsed;
        this.lore = lore == null || lore.isEmpty() ? null : text(lore);
    }

    /** A text option as written in a MythicMobs line: surrounding quotes removed, {@code <&sp>} and &-codes resolved. */
    public static String text(String raw) {
        String s = raw;
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1);
        return SkillString.parseMessageSpecialChars(s);
    }

    public static Component component(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public static EquipmentSlot parseSlot(String s) {
        return switch (s.toUpperCase(Locale.ROOT)) {
            case "HAND", "MAINHAND", "MAIN_HAND" -> EquipmentSlot.HAND;
            case "OFFHAND", "OFF_HAND" -> EquipmentSlot.OFF_HAND;
            case "HEAD", "HELMET" -> EquipmentSlot.HEAD;
            case "CHEST", "CHESTPLATE" -> EquipmentSlot.CHEST;
            case "LEGS", "LEGGINGS" -> EquipmentSlot.LEGS;
            case "FEET", "BOOTS" -> EquipmentSlot.FEET;
            default -> throw new IllegalArgumentException("invalid slot: " + s);
        };
    }

    public @Nullable ItemStack item(Player player) {
        ItemStack item = player.getInventory().getItem(slot);
        return item == null || item.getType().isAir() ? null : item;
    }

    /** The stored number, or null when the item or the value is missing. */
    public @Nullable Double get(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String raw = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void set(ItemStack item, double value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, String.valueOf(value));
        if (lore != null) {
            String marker = PlainTextComponentSerializer.plainText().serialize(component(lore.split("\\$count", -1)[0]));
            List<Component> lines = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
            if (!marker.isBlank()) {
                lines.removeIf(c -> PlainTextComponentSerializer.plainText().serialize(c).contains(marker));
            }
            lines.add(component(lore.replace("$count", String.valueOf(value))));
            meta.lore(lines);
        }
        item.setItemMeta(meta);
    }

    public EquipmentSlot slot() {
        return slot;
    }

    public NamespacedKey key() {
        return key;
    }
}
