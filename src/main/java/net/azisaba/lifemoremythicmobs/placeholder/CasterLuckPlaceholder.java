package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.skills.placeholders.Placeholder;
import io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class CasterLuckPlaceholder {
    public static void register(PlaceholderManager manager) {
        manager.register("caster.luck", Placeholder.meta(((placeholderMeta, s) -> {
            Entity entity = BukkitAdapter.adapt(placeholderMeta.getCaster().getEntity());
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                AttributeInstance attr = living.getAttribute(Attribute.GENERIC_LUCK);
                if (attr == null) return "0";
                return String.valueOf((int) attr.getValue());
            }
            return "0";
        })));
    }
}