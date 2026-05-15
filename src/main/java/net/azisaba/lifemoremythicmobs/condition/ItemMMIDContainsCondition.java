package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.items.ItemManager;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.shadows.nbt.NBTTagCompound;
import io.lumine.mythic.core.skills.SkillCondition;
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

        this.itemManager = MythicBukkit.inst().getItemManager();

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

        if (item == null || item.getAmount() == 0 || item.getType().isAir()) {
            return false;
        }
        String mmid = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(item);
        if (mmid.isEmpty()) return false;

        String checkID = this.ignoreCase ? mmid.toLowerCase() : mmid;
        String checkContains = this.ignoreCase ? this.contains.toLowerCase() : this.contains;

        return checkID.contains(checkContains);
    }
}