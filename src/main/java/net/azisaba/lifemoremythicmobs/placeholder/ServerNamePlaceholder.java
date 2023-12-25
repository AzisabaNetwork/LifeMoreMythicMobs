package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.xikage.mythicmobs.skills.placeholders.Placeholder;
import io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderManager;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;

public class ServerNamePlaceholder {
    public static void register(PlaceholderManager manager) {
        manager.register("server-name", Placeholder.meta((meta, s) -> LifeMoreMythicMobs.getPlugin(LifeMoreMythicMobs.class).server));
    }
}
