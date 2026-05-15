package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;

public class ServerNamePlaceholder {
    public static void register(PlaceholderManager manager) {
        manager.register("server-name", Placeholder.meta((meta, s) -> LifeMoreMythicMobs.getPlugin(LifeMoreMythicMobs.class).server));
    }
}
