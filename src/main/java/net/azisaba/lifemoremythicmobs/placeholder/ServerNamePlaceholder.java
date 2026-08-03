package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;

public class ServerNamePlaceholder {
    public static void register(PlaceholderManager manager) {
        for (java.lang.reflect.Method m : Placeholder.class.getMethods()) {
            System.out.println("Placeholder method: " + m);
        }
        for (java.lang.reflect.Method m : PlaceholderManager.class.getMethods()) {
            if (m.getName().startsWith("register")) {
                System.out.println("PlaceholderManager method: " + m);
            }
        }
        manager.register("server-name", Placeholder.meta((meta, s) -> LifeMoreMythicMobs.getPlugin(LifeMoreMythicMobs.class).server));
    }
}
