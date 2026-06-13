package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;

import java.util.Objects;

public class ChangeItemNBTMechanic extends SkillMechanic implements ITargetedEntitySkill {
    protected final String tag;
    protected final PlaceholderString value;

    public ChangeItemNBTMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.tag = config.getString(new String[]{"tag", "t"}, "DefaultTag");
        this.value = PlaceholderString.of(config.getString(new String[]{"val", "v"}, "0"));
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!target.isPlayer()) return false;

        Player player = (Player) BukkitAdapter.adapt(target);
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) return false;

        String resolvedValue = this.value.get(data, target);

        net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        Objects.requireNonNull(compound).setString(tag, resolvedValue);
        nmsItem.setTag(compound);

        player.getInventory().setItemInMainHand(CraftItemStack.asBukkitCopy(nmsItem));
        return true;
    }
}