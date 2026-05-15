package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import net.azisaba.lifepvelevel.LifePvELevel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PvELevelPlaceholder {
    public static void register(PlaceholderManager manager) {
        manager.register("caster.pvelevel", Placeholder.meta((placeholderMeta, s) -> {
            Entity entity = BukkitAdapter.adapt(placeholderMeta.getCaster().getEntity());
            if (entity instanceof Player) {
                return String.valueOf(LifePvELevel.getLevel((Player) entity));
            }
            return "0";
        }));

        manager.register("caster.pveexp", Placeholder.meta((placeholderMeta, s) -> {
            Entity entity = BukkitAdapter.adapt(placeholderMeta.getCaster().getEntity());
            if (entity instanceof Player) {
                return String.valueOf(LifePvELevel.getExp((Player) entity));
            }
            return "0";
        }));
    }
}
