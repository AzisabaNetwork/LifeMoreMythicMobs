package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import net.azisaba.lifemoremythicmobs.util.MarkStackManager;
import net.azisaba.lifemoremythicmobs.util.PlaceholderFactory;

/**
 * Registers <caster.lmmm.mark.ID> and <target.lmmm.mark.ID> placeholders.
 */
public class MarkStacksPlaceholder {
    public static void register(PlaceholderManager manager) {
        manager.register("caster.lmmm.mark", PlaceholderFactory.meta((meta, arg) -> {
            if (meta.getCaster() == null || meta.getCaster().getEntity() == null || arg == null) return "0";
            return String.valueOf(MarkStackManager.getStacks(meta.getCaster().getEntity().getUniqueId(), arg));
        }));
        manager.register("target.lmmm.mark", PlaceholderFactory.target((meta, entity, arg) -> {
            if (entity == null || arg == null) return "0";
            return String.valueOf(MarkStackManager.getStacks(entity.getUniqueId(), arg));
        }));
    }
}
