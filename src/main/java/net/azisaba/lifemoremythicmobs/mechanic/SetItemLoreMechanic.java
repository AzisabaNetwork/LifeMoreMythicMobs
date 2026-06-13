package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SetItemLoreMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString slot;
   private final PlaceholderString text;
   private final int line;

   public SetItemLoreMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.slot = PlaceholderString.of(config.getString(new String[]{"slot", "s"}, "MAINHAND", new String[0]));
      this.text = PlaceholderString.of(config.getString(new String[]{"text", "t"}, "デフォルトォ", new String[0]));
      this.line = config.getInteger(new String[]{"line", "l"}, 0);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!target.isPlayer()) {
         return SkillResult.FAILURE;
      }

      AbstractPlayer abstractPlayer = (AbstractPlayer)target;
      Player player = BukkitAdapter.adapt(abstractPlayer);
      String slotString = this.slot.get(data);
      ItemStack item = this.getItemInSlot(player, slotString);
      if (item != null && item.getType() != Material.AIR) {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return SkillResult.FAILURE;
         } else {
            List<String> lore = meta.getLore();
            if (lore == null) {
               return SkillResult.FAILURE;
            } else if (this.line >= 0 && this.line < lore.size()) {
               String newText = ChatColor.translateAlternateColorCodes('&', this.text.get(data));
               lore.set(this.line, newText);
               meta.setLore(lore);
               item.setItemMeta(meta);
               return SkillResult.SUCCESS;
            } else {
               return SkillResult.FAILURE;
            }
         }
      } else {
         return SkillResult.FAILURE;
      }
   }

   private ItemStack getItemInSlot(Player player, String slot) {
      String var3;
      switch ((var3 = slot.toUpperCase()).hashCode()) {
         case -830756290:
            if (var3.equals("OFFHAND")) {
               return player.getInventory().getItemInOffHand();
            }
            break;
         case 2153902:
            if (var3.equals("FEET")) {
               return player.getInventory().getBoots();
            }
            break;
         case 2213344:
            if (var3.equals("HEAD")) {
               return player.getInventory().getHelmet();
            }
            break;
         case 2332709:
            if (var3.equals("LEGS")) {
               return player.getInventory().getLeggings();
            }
            break;
         case 64089825:
            if (var3.equals("CHEST")) {
               return player.getInventory().getChestplate();
            }
            break;
         case 774779304:
            if (var3.equals("MAINHAND")) {
               return player.getInventory().getItemInMainHand();
            }
      }

      return null;
   }
}
