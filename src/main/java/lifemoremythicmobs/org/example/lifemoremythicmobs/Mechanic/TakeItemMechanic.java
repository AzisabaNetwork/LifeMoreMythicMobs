package lifemoremythicmobs.org.example.lifemoremythicmobs.Mechanic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TakeItemMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final int amount;
    protected final ItemStack item;

    public TakeItemMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        String mmid = config.getString(new String[] {"item", "i", "material", "m"}, "null");

        this.amount = config.getInteger(new String[] {"amount", "a"}, 1);
        this.item = CraftItemStack.asCraftMirror(CraftItemStack.asNMSCopy(MythicMobs.inst().getItemManager().getItemStack(mmid)));


        item.setAmount(amount);

    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        Player bukkitTarget = (Player) BukkitAdapter.adapt(target);

        bukkitTarget.getInventory().removeItem(item);

        return true;
    }

}
