package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class CasterAttackPlaceholder {
    public static void register(PlaceholderManager manager) {
        manager.register("caster.attack", Placeholder.meta(((placeholderMeta, s) -> {
            Entity entity = BukkitAdapter.adapt(placeholderMeta.getCaster().getEntity());
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                AttributeInstance attr = living.getAttribute(Attribute.ATTACK_DAMAGE);
                if (attr == null) return "0.0";
                return String.valueOf((int) attr.getValue());
            }
            return "0.0";
        })));
    }
}