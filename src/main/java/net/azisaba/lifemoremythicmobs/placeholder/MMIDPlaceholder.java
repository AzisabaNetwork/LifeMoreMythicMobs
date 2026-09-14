package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import net.azisaba.lifemoremythicmobs.util.PlaceholderFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MMIDPlaceholder {
    public static void register(PlaceholderManager manager) {
        manager.register("caster.mmid", PlaceholderFactory.meta((meta, s) -> {
            Entity entity = BukkitAdapter.adapt(meta.getCaster().getEntity());
            if (!(entity instanceof Player)) return null;
            return ItemUtil.getMythicType(((Player) entity).getInventory().getItemInMainHand());
        }));
    }
}
