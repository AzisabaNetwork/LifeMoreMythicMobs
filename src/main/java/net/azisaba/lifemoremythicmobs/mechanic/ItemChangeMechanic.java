package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.items.ItemManager;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/** Replaces a player's main-hand MythicItem with another MythicItem. */
public class ItemChangeMechanic extends SkillMechanic implements ITargetedEntitySkill {

    private final String fromItemName;
    private final String toItemName;

    public ItemChangeMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.fromItemName = config.getString(new String[]{"from", "f"}, "");
        this.toItemName = config.getString(new String[]{"to", "t"}, "");
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!target.isPlayer()) {
            return SkillResult.CONDITION_FAILED;
        }
        if (fromItemName.isBlank() || toItemName.isBlank()) {
            return SkillResult.INVALID_CONFIG;
        }

        Player player = (Player) BukkitAdapter.adapt(target);
        ItemStack currentItem = player.getInventory().getItemInMainHand();
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return SkillResult.CONDITION_FAILED;
        }

        ItemManager itemManager = MythicBukkit.inst().getItemManager();
        String currentMythicType = itemManager.getMythicTypeFromItem(currentItem);
        if (currentMythicType == null || !currentMythicType.equalsIgnoreCase(fromItemName)) {
            return SkillResult.CONDITION_FAILED;
        }

        Optional<MythicItem> replacement = itemManager.getItem(toItemName);
        if (replacement.isEmpty()) {
            return SkillResult.INVALID_CONFIG;
        }

        ItemStack replacementStack = BukkitAdapter.adapt(replacement.get().generateItemStack(currentItem.getAmount()));
        player.getInventory().setItemInMainHand(replacementStack);
        return SkillResult.SUCCESS;
    }
}
