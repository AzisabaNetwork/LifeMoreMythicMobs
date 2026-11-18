package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillCondition;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.azisaba.lifemoremythicmobs.util.NumericComparison;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Checks whether the entity is currently raising/charging a spear (Minecraft 1.21.11+ spears or KINETIC_WEAPON items).
 * Usage:
 *   ?lmUsingSpear
 *   ?lmUsingSpear{ticks=>=10;hand=MAIN;trident=false;kinetic=true}
 */
public class UsingSpearCondition extends SkillCondition implements IEntityCondition {
    public enum HandFilter {
        ANY, MAIN, OFF
    }

    private final String ticksExpr;
    private final HandFilter handFilter;
    private final boolean includeTrident;
    private final boolean includeKinetic;

    public UsingSpearCondition(MythicLineConfig config) {
        super(config.getLine());
        this.ticksExpr = config.getString(new String[]{"ticks", "t", "time"}, "");
        String rawHand = config.getString(new String[]{"hand", "h"}, "ANY").toUpperCase();
        if (rawHand.equals("HAND") || rawHand.equals("MAIN_HAND") || rawHand.equals("MAIN")) {
            this.handFilter = HandFilter.MAIN;
        } else if (rawHand.equals("OFF_HAND") || rawHand.equals("OFF") || rawHand.equals("OFFHAND")) {
            this.handFilter = HandFilter.OFF;
        } else {
            this.handFilter = HandFilter.ANY;
        }
        this.includeTrident = config.getBoolean(new String[]{"includetrident", "trident"}, false);
        this.includeKinetic = config.getBoolean(new String[]{"includekinetic", "kinetic"}, true);
    }

    @Override
    public boolean check(AbstractEntity entity) {
        if (entity == null) return false;
        Entity bukkit = BukkitAdapter.adapt(entity);
        if (!(bukkit instanceof LivingEntity living)) return false;
        if (!living.hasActiveItem() && !living.isHandRaised()) return false;

        if (handFilter != HandFilter.ANY) {
            EquipmentSlot activeHand = living.getActiveItemHand();
            if (handFilter == HandFilter.MAIN && activeHand != EquipmentSlot.HAND) return false;
            if (handFilter == HandFilter.OFF && activeHand != EquipmentSlot.OFF_HAND) return false;
        }

        ItemStack active = living.getActiveItem();
        if (!isSpearItem(active, includeTrident, includeKinetic)) return false;

        if (ticksExpr != null && !ticksExpr.isBlank()) {
            return NumericComparison.matches(living.getActiveItemUsedTime(), ticksExpr);
        }
        return true;
    }

    public static boolean isSpearItem(ItemStack item, boolean includeTrident, boolean includeKinetic) {
        if (item == null || item.getType().isAir()) return false;
        if (isSpearMaterialName(item.getType().name(), includeTrident)) return true;
        return includeKinetic && item.hasData(DataComponentTypes.KINETIC_WEAPON);
    }

    public static boolean isSpearMaterialName(String materialName, boolean includeTrident) {
        return ConditionMatchUtil.isSpearMaterialName(materialName, includeTrident);
    }
}
