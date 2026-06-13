package net.azisaba.lifemoremythicmobs.util.CharReorderGui;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.bukkit.MythicBukkit;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CharReorderGuiListener implements Listener {
   private boolean isOurGui(Inventory inv) {
      return inv != null && inv.getHolder() instanceof CharReorderGuiHolder;
   }

   @EventHandler
   public void onClick(InventoryClickEvent e) {
      InventoryView view = e.getView();
      Inventory top = view.getTopInventory();
      if (this.isOurGui(top)) {
         e.setCancelled(true);
         if (e.getWhoClicked() instanceof Player) {
            Player p = (Player)e.getWhoClicked();
            UUID id = p.getUniqueId();
            Optional<CharReorderSession> opt = CharReorderGuiManager.get(id);
            if (opt.isPresent()) {
               CharReorderSession s = opt.get();
               if (e.getRawSlot() < top.getSize()) {
                  int slot = e.getRawSlot();
                  if (slot == s.getConfirmSlot()) {
                     String result = this.buildResultString(s.getInventory(), s.getMaxChars());
                     boolean ok = VariableUtil.setScopedVariable(s.getStoreKey(), result, s.getData(), s.getTarget());
                     s.setDecideTriggered(true);
                     p.closeInventory();
                  } else if (slot >= 0 && slot < s.getMaxChars()) {
                     ItemStack clicked = top.getItem(slot);
                     if (clicked != null) {
                        if (s.getSelectedIndex() < 0) {
                           if (clicked.getType() == Material.LIME_STAINED_GLASS_PANE || clicked.getType() == Material.YELLOW_STAINED_GLASS_PANE) {
                              this.normalizeToLime(top, s.getMaxChars());
                              ItemStack sel = CharReorderGuiManager.charPane(this.getChar(clicked), true);
                              top.setItem(slot, sel);
                              s.setSelectedIndex(slot);
                           }
                        } else {
                           int from = s.getSelectedIndex();
                           if (from == slot) {
                              ItemStack now = top.getItem(slot);
                              if (now != null && now.getType() == Material.YELLOW_STAINED_GLASS_PANE) {
                                 ItemStack lime = CharReorderGuiManager.charPane(this.getChar(now), false);
                                 top.setItem(slot, lime);
                              }

                              s.setSelectedIndex(-1);
                           } else {
                              ItemStack fromItem = top.getItem(from);
                              ItemStack toItem = top.getItem(slot);
                              String fromChar = fromItem == null ? null : this.getChar(fromItem);
                              String toChar = toItem == null ? null : this.getChar(toItem);
                              boolean fromHasChar = fromItem != null
                                 && (fromItem.getType() == Material.YELLOW_STAINED_GLASS_PANE || fromItem.getType() == Material.LIME_STAINED_GLASS_PANE);
                              boolean toHasChar = toItem != null
                                 && (fromItem.getType() == Material.YELLOW_STAINED_GLASS_PANE || toItem.getType() == Material.LIME_STAINED_GLASS_PANE);
                              if (fromHasChar) {
                                 if (!toHasChar) {
                                    top.setItem(slot, CharReorderGuiManager.charPane(fromChar, false));
                                    top.setItem(from, CharReorderGuiManager.pane(Material.GRAY_STAINED_GLASS_PANE, " "));
                                 } else {
                                    top.setItem(slot, CharReorderGuiManager.charPane(fromChar, false));
                                    top.setItem(from, CharReorderGuiManager.charPane(toChar, false));
                                 }
                              }

                              s.setSelectedIndex(-1);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onDrag(InventoryDragEvent e) {
      Inventory top = e.getView().getTopInventory();
      if (this.isOurGui(top)) {
         for (int raw : e.getRawSlots()) {
            if (raw < top.getSize()) {
               e.setCancelled(true);
               return;
            }
         }
      }
   }

   @EventHandler
   public void onClose(InventoryCloseEvent e) {
      Inventory top = e.getView().getTopInventory();
      if (this.isOurGui(top)) {
         if (e.getPlayer() instanceof Player) {
            Player p = (Player)e.getPlayer();
            UUID id = p.getUniqueId();
            Optional<CharReorderSession> opt = CharReorderGuiManager.get(id);
            if (opt.isPresent()) {
               CharReorderSession s = opt.get();
               String skillName = s.getOnDecideSkillName();
               boolean shouldRun = s.isDecideTriggered() && skillName != null && !skillName.trim().isEmpty();
               CharReorderGuiManager.remove(id);
               if (shouldRun) {
                  Bukkit.getScheduler()
                     .runTask(
                        JavaPlugin.getPlugin(LifeMoreMythicMobs.class),
                        () -> MythicBukkit.inst().getSkillManager().getSkill(skillName).ifPresent(sk -> sk.execute(s.getData()))
                     );
               }
            }
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      CharReorderGuiManager.remove(e.getPlayer().getUniqueId());
   }

   private void normalizeToLime(Inventory inv, int maxChars) {
      for (int i = 0; i < maxChars; i++) {
         ItemStack it = inv.getItem(i);
         if (it != null && it.getType() == Material.YELLOW_STAINED_GLASS_PANE) {
            inv.setItem(i, CharReorderGuiManager.charPane(this.getChar(it), false));
         }
      }
   }

   private String getChar(ItemStack is) {
      ItemMeta meta = is.getItemMeta();
      String name = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "";
      return ChatColor.stripColor(name);
   }

   private String buildResultString(Inventory inv, int maxChars) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < maxChars; i++) {
         ItemStack it = inv.getItem(i);
         if (it != null) {
            Material t = it.getType();
            if (t == Material.LIME_STAINED_GLASS_PANE || t == Material.YELLOW_STAINED_GLASS_PANE) {
               sb.append(this.getChar(it));
            }
         }
      }

      return sb.toString();
   }
}
