package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
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
        ItemStack[] contents;
        switch (where) {
            case "INV":
            case "INVENTORY":
                contents = player.getInventory().getContents();
                break;
            case "OFFHAND":
                contents = new ItemStack[]{player.getInventory().getItemInOffHand()};
                break;
            case "HELMET":
            case "HEAD":
                contents = new ItemStack[]{player.getInventory().getHelmet()};
                break;
            case "CHEST":
            case "CHESTPLATE":
                contents = new ItemStack[]{player.getInventory().getChestplate()};
                break;
            case "LEGS":
            case "LEGGINGS":
                contents = new ItemStack[]{player.getInventory().getLeggings()};
                break;
            case "BOOTS":
            case "FEET":
                contents = new ItemStack[]{player.getInventory().getBoots()};
                break;
            case "ARMOR":
                contents = player.getInventory().getArmorContents();
                break;
            case "HAND":
            case "MAINHAND":
            default:
                contents = new ItemStack[]{player.getInventory().getItemInMainHand()};
                break;
        }
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
        String mmId = getMMIDFromNBT(item);
        for (String key : targetItems) {
            if (mmId != null && mmId.equalsIgnoreCase(key)) return true;
            if (item.getType().name().equalsIgnoreCase(key)) return true;
        }
        return false;
    }

    private String getMMIDFromNBT(ItemStack item) {
        try {
            net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            if (!nmsItem.hasTag()) return null;
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null || !tag.hasKey("MYTHIC_TYPE")) return null;
            return tag.getString("MYTHIC_TYPE");
        } catch (Exception e) {
            return null;
        }
    }
}