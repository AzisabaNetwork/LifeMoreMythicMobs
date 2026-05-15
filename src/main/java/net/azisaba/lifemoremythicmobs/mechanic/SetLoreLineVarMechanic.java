package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class SetLoreLineVarMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final int lineNumber;
    protected final String varName;
    protected final boolean stripColor;
    protected final boolean offhand;
    protected final String slotType;

    public SetLoreLineVarMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.lineNumber = config.getInteger(new String[] {"line", "l", "行"}, 0);
        this.varName = config.getString(new String[] {"variable", "var", "v", "変数"});
        this.stripColor = config.getBoolean(new String[] {"stripcolor", "sc", "色削除"}, false);
        this.offhand = config.getBoolean(new String[] {"offhand", "o", "オフハンド"}, false);
        this.slotType = config.getString(new String[] {"slot", "s", "スロット"}, "DEFAULT").toUpperCase();
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        String line = "";
        Entity entity = abstractEntity.getBukkitEntity();
        if (entity instanceof LivingEntity) {
            EntityEquipment equipment = ((LivingEntity) entity).getEquipment();
            if (equipment != null) {
                ItemStack stack = null;
                boolean isArmorSlot = false;
                if (!slotType.equals("DEFAULT")) {
                    switch (slotType) {
                        case "MAINHAND":
                            stack = equipment.getItemInMainHand();
                            break;
                        case "OFFHAND":
                            stack = equipment.getItemInOffHand();
                            break;
                        case "HEAD":
                        case "HELMET":
                            stack = equipment.getHelmet();
                            isArmorSlot = true;
                            break;
                        case "CHEST":
                        case "CHESTPLATE":
                            stack = equipment.getChestplate();
                            isArmorSlot = true;
                            break;
                        case "LEGS":
                        case "LEGGINGS":
                            stack = equipment.getLeggings();
                            isArmorSlot = true;
                            break;
                        case "FEET":
                        case "BOOTS":
                            stack = equipment.getBoots();
                            isArmorSlot = true;
                            break;
                        default:
                            break;
                    }
                }
                if (stack == null && !isArmorSlot) {
                    if (offhand) {
                        stack = equipment.getItemInOffHand();
                    } else {
                        stack = equipment.getItemInMainHand();
                    }
                }
                if (stack != null) {
                    line = ItemUtil.getLoreLine(stack, lineNumber);
                }
            }
        }
        if (stripColor) line = ChatColor.stripColor(line);
        ItemUtil.setVariable(skillMetadata, varName, line);
        return SkillResult.SUCCESS;
    }
}
