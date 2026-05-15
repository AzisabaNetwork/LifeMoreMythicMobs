package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;


import java.util.Objects;

public class ChangeItemNBTMechanic extends SkillMechanic implements ITargetedEntitySkill {
    protected final String tag;
    protected final PlaceholderString value;

    public ChangeItemNBTMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.tag = config.getString(new String[]{"tag", "t"}, "DefaultTag");
        this.value = PlaceholderString.of(config.getString(new String[]{"val", "v"}, "0"));
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!target.isPlayer()) return SkillResult.CONDITION_FAILED;

        Player player = (Player) BukkitAdapter.adapt(target);
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) return SkillResult.CONDITION_FAILED;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return SkillResult.CONDITION_FAILED;

        String resolvedValue = this.value.get(data, target);

        NamespacedKey key = new NamespacedKey("lifemoremythicmobs", tag.toLowerCase().replaceAll("[^a-z0-9/._-]", ""));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, resolvedValue);

        item.setItemMeta(meta);
        return SkillResult.SUCCESS;
    }
}