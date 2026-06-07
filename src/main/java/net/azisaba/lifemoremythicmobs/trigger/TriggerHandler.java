package net.azisaba.lifemoremythicmobs.trigger;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.GenericCaster;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TriggerHandler {

    public static boolean handle(Player player, ItemStack item, String triggerName) {
        String mythicType = ItemUtil.getMythicType(item);
        if (mythicType == null) return false;

        return MythicMobs.inst().getItemManager().getItem(mythicType)
                .map(mythicItem -> {
                    boolean handled = false;
                    for (String line : mythicItem.getConfig().getStringList("Skills")) {
                        if (line.toUpperCase().contains(triggerName.toUpperCase())) {
                            executeSkill(player, line, triggerName);
                            handled = true;
                        }
                    }
                    return handled;
                }).orElse(false);
    }

    private static void executeSkill(Player player, String line, String triggerName) {
        String pureMechanicStr = line.replaceAll("(?i)" + triggerName, "").replaceFirst("^- ?", "").trim();
        SkillMechanic mechanic = MythicMobs.inst().getSkillManager().getSkillMechanic(pureMechanicStr);
        if (mechanic != null) {
            GenericCaster caster = new GenericCaster(BukkitAdapter.adapt(player));
            mechanic.execute(new SkillMetadata(SkillTrigger.CUSTOM, caster, BukkitAdapter.adapt(player)));
        }
    }
}
