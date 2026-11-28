package net.azisaba.lifemoremythicmobs.mechanic;

import com.google.common.collect.Multimap;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

/**
 * Replaces the item in the player's hand with another MythicMobs item, keeping the attribute modifiers of the current item.
 *
 * <pre>
 * swapItemKeepAttr{id=new_item} @self                 replace the main-hand item
 * swapItemKeepAttr{id=new_item;from=old_item} @self   only when the held item is old_item
 * swapItemKeepAttr{id=new_item;hand=OFF_HAND} @self   use the off hand
 * swapItemKeepAttr{id=new_item;keepAmount=false} @self   use the new item's default amount
 * </pre>
 *
 * All attribute modifiers of the held item (including ones added with /itemattr) are copied, replacing the
 * new item's own modifiers. Name, lore, model and skills come from the new item.
 */
public class SwapItemKeepAttributesMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private static final NamespacedKey MYTHIC_TYPE = new NamespacedKey("mythicmobs", "type");

    private final PlaceholderString id;
    private final String from;
    private final EquipmentSlot slot;
    private final boolean keepAmount;

    public SwapItemKeepAttributesMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.id = config.getPlaceholderString(new String[]{"id", "item", "i"}, "");
        this.from = config.getString(new String[]{"from", "only"}, "");
        String hand = config.getString(new String[]{"hand", "slot"}, "HAND").toUpperCase(Locale.ROOT);
        this.slot = hand.equals("OFF_HAND") || hand.equals("OFFHAND") ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND;
        this.keepAmount = config.getBoolean(new String[]{"keepAmount", "ka"}, true);
        setAsyncSafe(false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!(target.getBukkitEntity() instanceof Player player)) return SkillResult.INVALID_TARGET;
        ItemStack hand = player.getInventory().getItem(slot);
        if (hand == null || hand.getType().isAir()) return SkillResult.CONDITION_FAILED;
        if (!from.isEmpty() && !from.equals(mythicType(hand))) return SkillResult.CONDITION_FAILED;
        String newId = id.get(data, target);
        if (newId == null || newId.isBlank()) return SkillResult.INVALID_CONFIG;
        ItemStack next = MythicBukkit.inst().getItemManager().getItemStack(newId.trim());
        if (next == null || next.getType().isAir()) return SkillResult.INVALID_CONFIG;
        if (keepAmount) next.setAmount(hand.getAmount());
        copyAttributes(hand, next);
        player.getInventory().setItem(slot, next);
        return SkillResult.SUCCESS;
    }

    private static String mythicType(ItemStack item) {
        if (!item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(MYTHIC_TYPE, PersistentDataType.STRING);
    }

    private static void copyAttributes(ItemStack from, ItemStack to) {
        ItemMeta src = from.getItemMeta();
        ItemMeta dst = to.getItemMeta();
        if (src == null || dst == null) return;
        Multimap<Attribute, AttributeModifier> mods = src.getAttributeModifiers();
        dst.setAttributeModifiers(null);
        if (mods != null) mods.forEach(dst::addAttributeModifier);
        to.setItemMeta(dst);
    }
}
