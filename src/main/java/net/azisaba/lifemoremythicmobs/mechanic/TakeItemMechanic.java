package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TakeItemMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final int amount;
    protected final ItemStack item;

    public TakeItemMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        String mmid = config.getString(new String[] {"item", "i", "material", "m"}, "null");
        this.amount = config.getInteger(new String[] {"amount", "a"}, 1);
        this.item = MythicBukkit.inst().getItemManager().getItemStack(mmid);
        item.setAmount(amount);

    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        Player bukkitTarget = (Player) BukkitAdapter.adapt(target);
        bukkitTarget.getInventory().removeItem(item);
        return SkillResult.SUCCESS;
    }

}
