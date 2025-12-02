package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.items.ItemManager;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ItemMMIDContainsCondition extends SkillCondition implements IEntityCondition {

    protected final String contains;
    protected final boolean ignoreCase;
    protected final boolean hotBar;
    protected final int hotBarNum;
    protected final boolean offHand;

    private final ItemManager itemManager;

    public ItemMMIDContainsCondition(MythicLineConfig config) {
        super(config.getLine());

        this.itemManager = MythicMobs.inst().getItemManager();

        this.contains = config.getString(new String[]{"mmid", "id"}, "");
        this.ignoreCase = config.getBoolean(new String[]{"ignorecase", "ic"}, false);
        this.hotBar = config.getBoolean(new String[]{"hotbar", "hb"}, false);
        this.offHand = config.getBoolean(new String[]{"offhand", "oh"}, false);

        if (this.hotBar) {
            this.hotBarNum = config.getInteger(new String[]{"hotbarnum", "hbn"}, 1);
        } else {
            this.hotBarNum = config.getInteger(new String[]{"hotbarnum", "hbn"}, -1);
        }
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        if (abstractEntity == null || !abstractEntity.isPlayer() || abstractEntity.isDead()) {
            return false;
        }

        Player player = (Player) abstractEntity.getBukkitEntity();
        PlayerInventory inv = player.getInventory();

        ItemStack item;
        if (this.offHand) {
            item = inv.getItemInOffHand();
        } else if (this.hotBar) {
            int slotIndex = this.hotBarNum - 1;
            if (slotIndex < 0 || slotIndex > 8) return false;
            item = inv.getItem(slotIndex);
        } else {
            item = inv.getItemInMainHand();
        }

        if (item == null || item.getAmount() == 0) {
            return false;
        }

        net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (!nmsItem.hasTag()) return false;
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null || !tag.hasKey("MYTHIC_TYPE")) return false;
        String mmid = tag.getString("MYTHIC_TYPE");
        if (mmid.isEmpty()) return false;

        String checkID = this.ignoreCase ? mmid.toLowerCase() : mmid;
        String checkContains = this.ignoreCase ? this.contains.toLowerCase() : this.contains;

        return checkID.contains(checkContains);
    }
}