package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ItemTagPlaceholder {
    public static final Set<String> TAGS = new HashSet<>(Collections.singletonList("CustomModelData"));

    public static void register(PlaceholderManager manager) {
        for (String tag : TAGS) {
            manager.register("caster.mainhand.tag." + tag, Placeholder.meta((meta, s) -> {
                Entity entity = BukkitAdapter.adapt(meta.getCaster().getEntity());
                if (!(entity instanceof Player)) return null;
                ItemStack stack = ((Player) entity).getInventory().getItemInMainHand();
                return String.valueOf(ItemUtil.resolveTagAsString(stack, tag));
            }));
            manager.register("caster.offhand.tag." + tag, Placeholder.meta((meta, s) -> {
                Entity entity = BukkitAdapter.adapt(meta.getCaster().getEntity());
                if (!(entity instanceof Player)) return null;
                ItemStack stack = ((Player) entity).getInventory().getItemInOffHand();
                return String.valueOf(ItemUtil.resolveTagAsString(stack, tag));
            }));
        }
    }
}
