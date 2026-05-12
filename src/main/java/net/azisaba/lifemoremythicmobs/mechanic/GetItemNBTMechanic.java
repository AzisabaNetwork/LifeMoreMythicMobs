package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetItemNBTMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String tag;
    protected final String varName;

    public GetItemNBTMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.tag = config.getString(new String[] {"tag", "t"}, "DefaultTag");
        this.varName = config.getString(new String[] {"variable", "var", "v", "変数"});
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!target.isPlayer()) return false;

        Player player = (Player) BukkitAdapter.adapt(target);
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) return false;

        net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (!nmsItem.hasTag()) return false;

        NBTTagCompound compound = nmsItem.getTag();
        if (compound != null && compound.hasKey(tag)) {
            String value = compound.get(tag).asString();

            ItemUtil.setVariable(data, varName, value);
            return true;
        }
        return false;
    }
}