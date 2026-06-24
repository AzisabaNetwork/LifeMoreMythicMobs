package net.azisaba.lifemoremythicmobs.placeholder;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.skills.placeholders.Placeholder;
import io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionLevelPlaceholder {
    private static String getPotionLevel(AbstractEntity abstractEntity, PotionEffectType type) {
        Entity entity = BukkitAdapter.adapt(abstractEntity);
        if (!(entity instanceof LivingEntity)) return "0";
        LivingEntity living = (LivingEntity) entity;
        PotionEffect effect = living.getPotionEffect(type);
        if (effect == null) return "0";
        return String.valueOf(effect.getAmplifier() + 1);
    }

    public static void register(PlaceholderManager manager) {
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type == null) continue;
            String name = type.getName().toLowerCase();
            manager.register("target." + name + ".level", Placeholder.target((meta, abstractEntity, s) ->
                    getPotionLevel(abstractEntity, type)));
            manager.register("caster." + name + ".level", Placeholder.meta((meta, s) ->
                    meta.getCaster() != null ? getPotionLevel(meta.getCaster().getEntity(), type) : "0"));
            manager.register("skill." + name + ".level", Placeholder.meta((meta, s) ->
                    meta.getCaster() != null ? getPotionLevel(meta.getCaster().getEntity(), type) : "0"));
        }
    }
}
