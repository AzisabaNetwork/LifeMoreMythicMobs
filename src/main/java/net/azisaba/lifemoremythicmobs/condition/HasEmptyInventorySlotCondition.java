package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class HasEmptyInventorySlotCondition extends SkillCondition implements IEntityCondition {
    private final boolean invert;

    public HasEmptyInventorySlotCondition(MythicLineConfig config) {
        super(config.getLine());

        this.invert = config.getBoolean(new String[] {"invert", "i", "反転"}, false);
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        Entity entity = abstractEntity.getBukkitEntity();
        if (!(entity instanceof Player)) {
            return false;
        }
        Player player = (Player) entity;
        return invert == (player.getInventory().firstEmpty() == -1);
    }
}
