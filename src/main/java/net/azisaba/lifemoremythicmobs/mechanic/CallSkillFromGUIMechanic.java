package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CallSkillFromGUIMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString title;
   private final Map<Integer, PlaceholderString> slotDisplays = new HashMap<>();
   private final Map<Integer, PlaceholderString> slotSkills = new HashMap<>();

   public CallSkillFromGUIMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.title = PlaceholderString.of(config.getString("title", "&a選択してください"));

      for (int i = 1; i <= 54; i++) {
         String displayKey = "slot" + i;
         String skillKey = "skill" + i;
         String displayValue = config.getString(displayKey);
         if (displayValue != null) {
            this.slotDisplays.put(i - 1, PlaceholderString.of(displayValue));
         }

         String skillValue = config.getString(skillKey);
         if (skillValue != null) {
            this.slotSkills.put(i - 1, PlaceholderString.of(skillValue));
         }
      }
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!target.isPlayer()) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)target.getBukkitEntity();
      String titleStr = ChatColor.translateAlternateColorCodes('&', this.title.get(data));
      Inventory gui = Bukkit.createInventory(player, 54, titleStr);

      for (int slot = 0; slot < 54; slot++) {
         PlaceholderString name = this.slotDisplays.get(slot);
         PlaceholderString skill = this.slotSkills.get(slot);
         ItemStack item = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE, 1, (short)(name != null && skill != null ? 5 : 7));
         ItemMeta meta = item.getItemMeta();
         if (name != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name.get(data)));
         }

         item.setItemMeta(meta);
         gui.setItem(slot, item);
      }

      player.openInventory(gui);
      Bukkit.getPluginManager()
         .registerEvents(new CallSkillFromGUIMechanic.CallSkillFromGUIListener(player, titleStr, data.deepClone()), JavaPlugin.getPlugin(LifeMoreMythicMobs.class));
      return SkillResult.SUCCESS;
   }

   private class CallSkillFromGUIListener implements Listener {
      private final Player player;
      private final String guiTitle;
      private final SkillMetadata metadata;

      public CallSkillFromGUIListener(Player player, String guiTitle, SkillMetadata metadata) {
         this.player = player;
         this.guiTitle = guiTitle;
         this.metadata = metadata;
      }

      @EventHandler
      public void onInventoryClick(InventoryClickEvent event) {
         if (event.getWhoClicked() instanceof Player) {
            if (event.getWhoClicked().equals(this.player)) {
               if (event.getView().getTitle().equals(this.guiTitle)) {
                  event.setCancelled(true);
                  int slot = event.getSlot();
                  PlaceholderString skillName = CallSkillFromGUIMechanic.this.slotSkills.get(slot);
                  if (skillName != null) {
                     this.player.closeInventory();
                     if (skillName != null) {
                        Skill skill = (Skill)MythicBukkit.inst().getSkillManager().getSkill(skillName.get(this.metadata)).orElse(null);
                        if (skill != null) {
                           AbstractEntity trigger = BukkitAdapter.adapt(this.player);
                           this.metadata.setTrigger(trigger);
                           skill.execute(this.metadata);
                        }
                     }
                  }
               }
            }
         }
      }

      @EventHandler
      public void onInventoryClose(InventoryCloseEvent event) {
         if (event.getPlayer().equals(this.player)) {
            HandlerList.unregisterAll(this);
         }
      }
   }
}
