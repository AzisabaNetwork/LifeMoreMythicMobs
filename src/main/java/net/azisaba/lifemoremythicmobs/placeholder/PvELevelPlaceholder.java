package net.azisaba.lifemoremythicmobs.placeholder;

import net.azisaba.lifemoremythicmobs.util.PlaceholderFactory;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class PvELevelPlaceholder {
    public static void register(PlaceholderManager manager) {
        manager.register("caster.pvelevel", PlaceholderFactory.meta((placeholderMeta, s) -> {
            Entity entity = BukkitAdapter.adapt(placeholderMeta.getCaster().getEntity());
            if (entity instanceof Player) {
                return invoke("getLevel", (Player) entity);
            }
            return "0";
        }));

        manager.register("caster.pveexp", PlaceholderFactory.meta((placeholderMeta, s) -> {
            Entity entity = BukkitAdapter.adapt(placeholderMeta.getCaster().getEntity());
            if (entity instanceof Player) {
                return invoke("getExp", (Player) entity);
            }
            return "0";
        }));
    }

    private static String invoke(String methodName, Player player) {
        try {
            Class<?> api = Class.forName("net.azisaba.lifepvelevel.LifePvELevel");
            Method method = api.getMethod(methodName, Player.class);
            return String.valueOf(method.invoke(null, player));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return "0";
        }
    }
}

