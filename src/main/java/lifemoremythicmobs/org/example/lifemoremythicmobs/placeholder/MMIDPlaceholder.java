package lifemoremythicmobs.org.example.lifemoremythicmobs.placeholder;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.skills.placeholders.Placeholder;
import io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderManager;
import lifemoremythicmobs.org.example.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MMIDPlaceholder {

    public static void register() {
        PlaceholderManager manager = MythicMobs.inst().getPlaceholderManager();
        manager.register("caster.mmid", Placeholder.meta((meta, s) -> {
            Entity entity = BukkitAdapter.adapt(meta.getCaster().getEntity());
            if (!(entity instanceof Player)) return null;
            return ItemUtil.getMythicType(((Player) entity).getInventory().getItemInMainHand());
        }));
    }

}
