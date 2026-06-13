package net.azisaba.lifemoremythicmobs.gui;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.gui.holder.GuiHolder;
import net.azisaba.lifemoremythicmobs.gui.menus.MainMenu;
import net.azisaba.lifemoremythicmobs.gui.menus.ValueMenu;
import net.azisaba.lifemoremythicmobs.session.AttrEditSession;
import net.azisaba.lifemoremythicmobs.util.AttrLists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AttrGuiManager implements Listener {
   private final LifeMoreMythicMobs plugin;
   private final Map<UUID, AttrEditSession> sessions = new HashMap<>();
   private static final String MOD_PREFIX = "ICMM-ATTR";

   public AttrGuiManager(LifeMoreMythicMobs plugin) {
      this.plugin = plugin;
      MainMenu.initOrder(AttrLists.genericOrder());
   }

   public void openMainMenu(Player p) {
      this.sessions.put(p.getUniqueId(), new AttrEditSession(AttrLists.genericOrder()));
      Bukkit.getScheduler().runTask(this.plugin, () -> p.openInventory(MainMenu.build(p, this.sessions.get(p.getUniqueId()))));
   }

   private void openValueMenu(Player p) {
      AttrEditSession s = this.sessions.get(p.getUniqueId());
      if (s != null) {
         s.cur().beginEdit();
         Bukkit.getScheduler().runTask(this.plugin, () -> p.openInventory(ValueMenu.build(p, s)));
      }
   }

   @EventHandler(ignoreCancelled = true)
   public void onClick(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         Player p = (Player)e.getWhoClicked();
         if (e.getInventory().getHolder() instanceof GuiHolder) {
            e.setCancelled(true);
            AttrEditSession s = this.sessions.get(p.getUniqueId());
            if (s != null) {
               GuiHolder holder = (GuiHolder)e.getInventory().getHolder();
               if (e.getSlot() == -999 && holder.getType() == GuiHolder.Type.MAIN) {
                  if (e.getClick() == ClickType.LEFT) {
                     s.setIndex(s.getIndex() - 1);
                     Bukkit.getScheduler().runTask(this.plugin, () -> p.openInventory(MainMenu.build(p, s)));
                  }

                  if (e.getClick() == ClickType.RIGHT) {
                     s.setIndex(s.getIndex() + 1);
                     Bukkit.getScheduler().runTask(this.plugin, () -> p.openInventory(MainMenu.build(p, s)));
                  }
               }

               if (e.getClickedInventory() == null || !e.getClickedInventory().equals(p.getInventory())) {
                  switch (holder.getType()) {
                     case MAIN:
                        if (e.getRawSlot() == 20) {
                           this.clickSlotToggle(p, s, e.getClick());
                           return;
                        }

                        this.handleMainClick(p, s, e.getRawSlot());
                        break;
                     case VALUE:
                        this.handleValueClick(p, s, e.getRawSlot());
                  }
               }
            }
         }
      }
   }

   private void handleMainClick(Player p, AttrEditSession s, int slot) {
      if (slot == 24) {
         this.openValueMenu(p);
      } else if (slot == 22) {
         this.applyAll(p, s);
         p.openInventory(MainMenu.build(p, s));
      } else {
         if (slot >= 10 && slot <= 16) {
            int offset = slot - 13;
            s.setIndex(s.getIndex() + offset);
            p.openInventory(MainMenu.build(p, s));
         }
      }
   }

   private void clickSlotToggle(Player p, AttrEditSession s, ClickType type) {
      int dir = type == ClickType.RIGHT ? 1 : -1;
      s.cur().cycleSlot(dir);
      p.openInventory(MainMenu.build(p, s));
   }

   private void handleValueClick(Player p, AttrEditSession s, int slot) {
      switch (slot) {
         case 1:
            s.cur().incDraft(-100.0);
            this.refreshValueHeader(p, s);
            break;
         case 2:
            s.cur().incDraft(-10.0);
            this.refreshValueHeader(p, s);
            break;
         case 3:
            s.cur().incDraft(-1.0);
            this.refreshValueHeader(p, s);
         case 4:
         case 8:
         case 9:
         case 10:
         case 13:
         case 16:
         case 17:
         case 19:
         default:
            break;
         case 5:
            s.cur().incDraft(1.0);
            this.refreshValueHeader(p, s);
            break;
         case 6:
            s.cur().incDraft(10.0);
            this.refreshValueHeader(p, s);
            break;
         case 7:
            s.cur().incDraft(100.0);
            this.refreshValueHeader(p, s);
            break;
         case 11:
            s.cur().incDraft(-0.1);
            this.refreshValueHeader(p, s);
            break;
         case 12:
            s.cur().incDraft(-0.01);
            this.refreshValueHeader(p, s);
            break;
         case 14:
            s.cur().incDraft(0.01);
            this.refreshValueHeader(p, s);
            break;
         case 15:
            s.cur().incDraft(0.1);
            this.refreshValueHeader(p, s);
            break;
         case 18:
            s.cur().toggleDraftMode();
            this.reopenValue(p, s);
            break;
         case 20:
            s.cur().discardDraft();
            Bukkit.getScheduler().runTask(this.plugin, () -> p.openInventory(MainMenu.build(p, s)));
            break;
         case 21:
            s.cur().resetDraftToUnset();
            this.reopenValue(p, s);
            break;
         case 22:
            s.cur().commitDraft();
            Bukkit.getScheduler().runTask(this.plugin, () -> p.openInventory(MainMenu.build(p, s)));
      }
   }

   private void addValue(AttrEditSession s, int delta) {
      double base = s.cur().getValue();
      if (base == -2.0) {
         base = 0.0;
      }

      s.cur().setValue(round2(base + delta));
   }

   private void reopenValue(Player p, AttrEditSession s) {
      Bukkit.getScheduler().runTask(this.plugin, () -> p.openInventory(ValueMenu.build(p, s)));
   }

   @EventHandler
   public void onClose(InventoryCloseEvent e) {
      if (e.getPlayer() instanceof Player) {
         Player p = (Player)e.getPlayer();
         if (e.getInventory().getHolder() instanceof GuiHolder) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               Inventory top = p.getOpenInventory() != null ? p.getOpenInventory().getTopInventory() : null;
               boolean stillOurGui = top != null && top.getHolder() instanceof GuiHolder;
               if (!stillOurGui) {
                  this.sessions.remove(p.getUniqueId());
               }
            }, 1L);
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      this.sessions.remove(e.getPlayer().getUniqueId());
   }

   private void applyAll(Player p, AttrEditSession s) {
      ItemStack hand = p.getInventory().getItemInMainHand();
      if (hand != null && hand.getType() != Material.AIR) {
         ItemMeta meta = hand.getItemMeta();
         if (meta == null) {
            p.sendMessage(ChatColor.RED + "このアイテムには属性を付与できません。");
         } else {
            int applied = 0;

            for (Entry<Attribute, AttrEditSession.AttrSetting> en : s.all().entrySet()) {
               Attribute attr = en.getKey();
               AttrEditSession.AttrSetting set = en.getValue();
               double saved = set.getSaved();
               if (saved != -2.0) {
                  AttrEditSession.Mode mode = set.getSavedMode();
                  double amount = mode == AttrEditSession.Mode.ADD ? saved : saved / 100.0;
                  Operation op = mode == AttrEditSession.Mode.ADD ? Operation.ADD_NUMBER : Operation.MULTIPLY_SCALAR_1;
                  Collection<AttributeModifier> mods = meta.getAttributeModifiers(attr);
                  if (mods != null && !mods.isEmpty()) {
                     for (AttributeModifier m : new ArrayList<>(mods)) {
                        if (m.getSlot() == null || m.getSlot() == set.getSlot()) {
                           meta.removeAttributeModifier(attr, m);
                        }
                     }
                  } else {
                     try {
                        meta.removeAttributeModifier(attr);
                     } catch (Throwable var19) {
                     }
                  }

                  AttributeModifier mod = new AttributeModifier(UUID.randomUUID(), attr.name(), amount, op, set.getSlot());
                  meta.addAttributeModifier(attr, mod);
                  applied++;
               }
            }

            if (applied > 0) {
               hand.setItemMeta(meta);
               p.sendMessage(ChatColor.GREEN + "適用: " + ChatColor.WHITE + applied + ChatColor.GRAY + " 件の属性を更新しました。");

               for (AttrEditSession.AttrSetting set : s.all().values()) {
                  set.setValue(-2.0);
               }

               Bukkit.getScheduler().runTask(this.plugin, p::closeInventory);
            } else {
               p.sendMessage(ChatColor.GRAY + "適用対象がありません（すべて未設定）。");
            }
         }
      } else {
         p.sendMessage(ChatColor.RED + "メインハンドにアイテムを持ってください。");
      }
   }

   private static double round2(double v) {
      return Math.round(v * 100.0) / 100.0;
   }

   private static String readable(Attribute a) {
      return a.name().replace("GENERIC_", "").toLowerCase().replace('_', ' ');
   }

   private void refreshValueHeader(Player p, AttrEditSession s) {
      Inventory inv = p.getOpenInventory().getTopInventory();
      if (inv.getHolder() instanceof GuiHolder) {
         if (((GuiHolder)inv.getHolder()).getType() == GuiHolder.Type.VALUE) {
            double display = s.cur().getDraftOrSaved();
            ItemStack it = new ItemStack(Material.BOOK);
            ItemMeta im = it.getItemMeta();
            im.setDisplayName(ChatColor.GRAY + "現在値: " + ChatColor.WHITE + (display == -2.0 ? ChatColor.RED + "未設定" : String.format("%.2f", display)));
            im.setLore(Collections.singletonList(ChatColor.DARK_GRAY + " -『未設定に戻す』で未設定へ"));
            it.setItemMeta(im);
            inv.setItem(4, it);
         }
      }
   }
}
