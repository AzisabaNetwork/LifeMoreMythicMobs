package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class HasEmptyInventorySlotCondition extends SkillCondition implements IEntityCondition {
    public HasEmptyInventorySlotCondition(String line) {
        super(line);
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        Entity entity = abstractEntity.getBukkitEntity();
        if (!(entity instanceof Player)) {
            return false;
        }
        Player player = (Player) entity;
        return player.getInventory().firstEmpty() != -1;
    }
}
