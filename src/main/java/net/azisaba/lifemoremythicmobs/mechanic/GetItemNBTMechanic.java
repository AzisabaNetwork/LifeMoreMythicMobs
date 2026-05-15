package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GetItemNBTMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String tag;
    protected final String varName;

    public GetItemNBTMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.tag = config.getString(new String[] {"tag", "t"}, "DefaultTag");
        this.varName = config.getString(new String[] {"variable", "var", "v"}, "変数");
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!target.isPlayer()) return SkillResult.CONDITION_FAILED;

        Player player = (Player) BukkitAdapter.adapt(target);
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) return SkillResult.CONDITION_FAILED;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return SkillResult.CONDITION_FAILED;

        NamespacedKey key = new NamespacedKey("lifemoremythicmobs", tag.toLowerCase());
        String value = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        if (value != null) {
            ItemUtil.setVariable(data, varName, value);
            return SkillResult.SUCCESS;
        }
        return SkillResult.CONDITION_FAILED;
    }
}