package net.azisaba.lifemoremythicmobs.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Utilities for interoperating with legacy-formatted configuration strings. */
public final class LegacyText {
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    public static final String AQUA = "\u00a7b";
    public static final String BLACK = "\u00a70";
    public static final String BLUE = "\u00a79";
    public static final String DARK_AQUA = "\u00a73";
    public static final String DARK_BLUE = "\u00a71";
    public static final String DARK_GRAY = "\u00a78";
    public static final String DARK_GREEN = "\u00a72";
    public static final String DARK_PURPLE = "\u00a75";
    public static final String DARK_RED = "\u00a74";
    public static final String GOLD = "\u00a76";
    public static final String GRAY = "\u00a77";
    public static final String GREEN = "\u00a7a";
    public static final String LIGHT_PURPLE = "\u00a7d";
    public static final String RED = "\u00a7c";
    public static final String RESET = "\u00a7r";
    public static final String WHITE = "\u00a7f";
    public static final String YELLOW = "\u00a7e";

    private LegacyText() {
    }

    public static @NotNull Component component(@NotNull String text) {
        return SECTION_SERIALIZER.deserialize(text);
    }

    public static @NotNull Component ampersandComponent(@NotNull String text) {
        return AMPERSAND_SERIALIZER.deserialize(text);
    }

    public static @NotNull String plain(@NotNull Component component) {
        return PLAIN_SERIALIZER.serialize(component);
    }

    public static @NotNull String serialize(@NotNull Component component) {
        return SECTION_SERIALIZER.serialize(component);
    }

    public static @NotNull String stripColor(@NotNull String text) {
        return plain(component(text));
    }

    public static @NotNull String translateAlternateColorCodes(char alternateColorChar, @NotNull String text) {
        return alternateColorChar == '&' ? SECTION_SERIALIZER.serialize(ampersandComponent(text)) : text;
    }

    public static @NotNull List<Component> components(@NotNull List<String> lines) {
        return lines.stream().map(LegacyText::component).toList();
    }
}
