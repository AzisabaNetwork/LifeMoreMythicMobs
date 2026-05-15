package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class HasItemCondition extends SkillCondition implements IEntityCondition {

    private final Set<String> targetItems;
    private final int amount;
    private final String where;

    public HasItemCondition(MythicLineConfig config) {
        super(config.getLine());
        String rawItem = config.getString(new String[]{"mmid", "id", "item", "i", "material", "m", "type"}, "null");
        this.targetItems = new HashSet<>();
        for (String s : rawItem.split(",")) {
            this.targetItems.add(s.trim());
        }
        this.amount = config.getInteger(new String[]{"amount", "a"}, 1);
        this.where = config.getString(new String[]{"where", "w", "slot", "s"}, "MAINHAND").toUpperCase();
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        if (!abstractEntity.isPlayer()) return false;
        Player player = (Player) abstractEntity.getBukkitEntity();
        ItemStack[] contents = switch (where) {
            case "INV", "INVENTORY" -> player.getInventory().getContents();
            case "OFFHAND" -> new ItemStack[]{player.getInventory().getItemInOffHand()};
            case "HELMET", "HEAD" -> new ItemStack[]{player.getInventory().getHelmet()};
            case "CHEST", "CHESTPLATE" -> new ItemStack[]{player.getInventory().getChestplate()};
            case "LEGS", "LEGGINGS" -> new ItemStack[]{player.getInventory().getLeggings()};
            case "BOOTS", "FEET" -> new ItemStack[]{player.getInventory().getBoots()};
            case "ARMOR" -> player.getInventory().getArmorContents();
            default -> new ItemStack[]{player.getInventory().getItemInMainHand()};
        };
        int foundTotal = 0;
        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (matchesAny(item)) {
                foundTotal += item.getAmount();
            }
        }
        return foundTotal >= amount;
    }

    private boolean matchesAny(ItemStack item) {
        String mmId = ItemUtil.getMythicType(item);
        for (String key : targetItems) {
            if (mmId != null && mmId.equalsIgnoreCase(key)) return true;
            if (item.getType().name().equalsIgnoreCase(key)) return true;
        }
        return false;
    }
}