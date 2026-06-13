package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class GiveOwnHeadMechanic extends SkillMechanic implements ITargetedEntitySkill {
   public GiveOwnHeadMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
   }

   public boolean castAtEntity(SkillMetadata data, AbstractEntity entity) {
      AbstractEntity caster = data.getCaster().getEntity();
      if (!caster.isPlayer()) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)BukkitAdapter.adapt(caster);
      if (player.getInventory().firstEmpty() == -1) {
         return SkillResult.FAILURE;
      }

      ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
      SkullMeta meta = (SkullMeta)head.getItemMeta();
      if (meta != null) {
         meta.setOwningPlayer(player);
         meta.setDisplayName("§e§o" + player.getName());
         head.setItemMeta(meta);
      }

      player.getInventory().addItem(new ItemStack[]{head});
      return SkillResult.SUCCESS;
   }
}
