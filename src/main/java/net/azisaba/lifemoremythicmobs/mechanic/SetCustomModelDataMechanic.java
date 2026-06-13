package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class SetCustomModelDataMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderInt modelData;
   private final PlaceholderString slot;

   public SetCustomModelDataMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.modelData = PlaceholderInt.of(config.getString(new String[]{"model", "m"}, "-1", new String[0]));
      this.slot = PlaceholderString.of(config.getString(new String[]{"slot", "s"}, "offhand", new String[0]).toLowerCase());
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      Integer modelData = this.modelData.get(data);
      String slot = this.slot.get(data);
      if (target.isPlayer() && modelData >= 0) {
         Player player = (Player)BukkitAdapter.adapt(target);
         ItemStack item = null;
         String meta = slot;
         switch (slot.hashCode()) {
            case -1548738978:
               if (!meta.equals("offhand")) {
                  return SkillResult.FAILURE;
               }

               item = player.getInventory().getItemInOffHand();
               break;
            case -1220934547:
               if (!meta.equals("helmet")) {
                  return SkillResult.FAILURE;
               }

               item = player.getInventory().getHelmet();
               break;
            case -7847512:
               if (!meta.equals("mainhand")) {
                  return SkillResult.FAILURE;
               }

               item = player.getInventory().getItemInMainHand();
               break;
            case 93922241:
               if (!meta.equals("boots")) {
                  return SkillResult.FAILURE;
               }

               item = player.getInventory().getBoots();
               break;
            case 1069952181:
               if (!meta.equals("chestplate")) {
                  return SkillResult.FAILURE;
               }

               item = player.getInventory().getChestplate();
               break;
            case 1735676010:
               if (meta.equals("leggings")) {
                  item = player.getInventory().getLeggings();
                  break;
               }

               return SkillResult.FAILURE;
            default:
               return SkillResult.FAILURE;
         }

         if (item != null && item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
               ((LifeMoreMythicMobs)JavaPlugin.getPlugin(LifeMoreMythicMobs.class))
                  .getLogger()
                  .info("[SetCustomModelDataMechanic] No ItemMeta in " + slot + " for player " + player.getName());
               return SkillResult.FAILURE;
            }

            label63: {
               meta.setCustomModelData(modelData);
               item.setItemMeta(meta);
               String var8 = slot;
               switch (slot.hashCode()) {
                  case -1548738978:
                     if (var8.equals("offhand")) {
                        player.getInventory().setItemInOffHand(item);
                        break label63;
                     }
                     break;
                  case -1220934547:
                     if (var8.equals("helmet")) {
                        player.getInventory().setHelmet(item);
                        break label63;
                     }
                     break;
                  case -7847512:
                     if (var8.equals("mainhand")) {
                        player.getInventory().setItemInOffHand(item);
                        break label63;
                     }
                     break;
                  case 93922241:
                     if (var8.equals("boots")) {
                        player.getInventory().setBoots(item);
                        break label63;
                     }
                     break;
                  case 1069952181:
                     if (var8.equals("chestplate")) {
                        player.getInventory().setChestplate(item);
                        break label63;
                     }
                     break;
                  case 1735676010:
                     if (var8.equals("leggings")) {
                        player.getInventory().setLeggings(item);
                        break label63;
                     }
               }

               ((LifeMoreMythicMobs)JavaPlugin.getPlugin(LifeMoreMythicMobs.class))
                  .getLogger()
                  .info("[SetCustomModelDataMechanic] Unknown slot '" + slot + "' for player " + player.getName());
            }

            ((LifeMoreMythicMobs)JavaPlugin.getPlugin(LifeMoreMythicMobs.class))
               .getLogger()
               .info("[SetCustomModelDataMechanic] Set modelData=" + modelData + " on " + slot + " for player " + player.getName());
            return SkillResult.SUCCESS;
         } else {
            return SkillResult.FAILURE;
         }
      } else {
         return SkillResult.FAILURE;
      }
   }
}
