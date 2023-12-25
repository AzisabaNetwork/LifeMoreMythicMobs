package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class SetDisplayNameVarMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String varName;
    protected final boolean stripColor;
    protected final boolean offhand;

    public SetDisplayNameVarMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.varName = config.getString(new String[] {"variable", "var", "v", "変数"});
        this.stripColor = config.getBoolean(new String[] {"stripcolor", "sc", "色削除"}, false);
        this.offhand = config.getBoolean(new String[] {"offhand", "o", "オフハンド"}, false);
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        String name = "";
        Entity entity = abstractEntity.getBukkitEntity();
        if (entity instanceof LivingEntity) {
            EntityEquipment equipment = ((LivingEntity) entity).getEquipment();
            if (equipment != null) {
                ItemStack stack = offhand ? equipment.getItemInOffHand() : equipment.getItemInMainHand();
                name = ItemUtil.getDisplayName(stack);
            }
        }
        if (stripColor) name = ChatColor.stripColor(name);
        ItemUtil.setVariable(skillMetadata, varName, name);
        return true;
    }
}
