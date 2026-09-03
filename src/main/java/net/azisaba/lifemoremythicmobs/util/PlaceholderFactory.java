package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.utils.interfaces.TriFunction;
import io.lumine.mythic.core.skills.placeholders.PlaceholderMeta;
import io.lumine.mythic.core.skills.placeholders.all.FunctionalMetaPlaceholder;
import io.lumine.mythic.core.skills.placeholders.types.MetaPlaceholder;
import io.lumine.mythic.core.skills.placeholders.types.TargetPlaceholder;
import java.util.function.BiFunction;

/**
 * Constructs MM placeholders without invoking the deprecated Placeholder factory methods.
 * Dynamic placeholder names used by the legacy configuration cannot be represented by a
 * fixed annotation, so they continue to be registered by name through PlaceholderManager.
 */
@SuppressWarnings("deprecation")
public final class PlaceholderFactory {
    private PlaceholderFactory() {
    }

    public static MetaPlaceholder meta(BiFunction<PlaceholderMeta, String, String> transformer) {
        return new FunctionalMetaPlaceholder(transformer);
    }

    public static TargetPlaceholder target(TriFunction<PlaceholderMeta, AbstractEntity, String, String> transformer) {
        return new TargetPlaceholder(transformer);
    }
}
