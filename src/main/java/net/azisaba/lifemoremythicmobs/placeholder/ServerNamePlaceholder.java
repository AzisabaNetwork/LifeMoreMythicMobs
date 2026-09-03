package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import net.azisaba.lifemoremythicmobs.util.PlaceholderFactory;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;

public class ServerNamePlaceholder {
    public static void register(PlaceholderManager manager) {
        manager.register("server-name", PlaceholderFactory.meta((meta, s) -> LifeMoreMythicMobs.getPlugin(LifeMoreMythicMobs.class).server));
    }
}
