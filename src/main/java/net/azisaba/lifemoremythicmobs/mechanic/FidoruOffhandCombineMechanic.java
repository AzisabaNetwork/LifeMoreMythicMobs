package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.items.MythicItem;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.util.jnbt.CompoundTag;
import java.util.Arrays;
import javax.annotation.Nullable;
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

public class FidoruOffhandCombineMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private static final int[] ITEM_SLOTS = new int[]{10, 12, 14};
   private static final int CONFIRM_SLOT = 31;
   private static final String GUI_TITLE = ChatColor.DARK_GREEN + "再厳選画面";
   private static final String TARGET_MMID = "Iga_fidoru_offhand";

   public FidoruOffhandCombineMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!target.isPlayer()) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)target.getBukkitEntity();
      IgaDebugLogger.log(this.getClass(), "メカニック開始。プレイヤー: " + player.getName());
      Inventory gui = Bukkit.createInventory(player, 54, GUI_TITLE);
      ItemStack grayPane = this.createPane((short)7, " ");

      for (int i = 0; i < gui.getSize(); i++) {
         gui.setItem(i, grayPane);
      }

      int[] var9 = ITEM_SLOTS;
      int var8 = ITEM_SLOTS.length;

      for (int var7 = 0; var7 < var8; var7++) {
         int slot = var9[var7];
         gui.setItem(slot, null);
      }

      gui.setItem(31, this.createPane((short)5, ChatColor.GREEN + "決定"));
      player.openInventory(gui);
      Bukkit.getPluginManager().registerEvents(new FidoruOffhandCombineMechanic.CombineListener(player, gui), JavaPlugin.getPlugin(LifeMoreMythicMobs.class));
      return SkillResult.SUCCESS;
   }

   private ItemStack createPane(short color, String name) {
      ItemStack item = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE, 1, color);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(name);
      item.setItemMeta(meta);
      return item;
   }

   @Nullable
   public static String getMythicItemIdFromNBT(ItemStack item) {
      if (item != null && item.getType() != Material.AIR) {
         CompoundTag tag = MythicBukkit.inst().getVolatileCodeHandler().getItemHandler().getNBTData(item);
         return tag != null && tag.containsKey("MYTHIC_TYPE") ? tag.getString("MYTHIC_TYPE") : null;
      } else {
         return null;
      }
   }

   private class CombineListener implements Listener {
      private final Player player;
      private final Inventory gui;
      private boolean used = false;

      CombineListener(Player player, Inventory gui) {
         this.player = player;
         this.gui = gui;
      }

      @EventHandler
      public void onClick(InventoryClickEvent e) {
         if (e.getWhoClicked() instanceof Player) {
            if (e.getWhoClicked().equals(this.player)) {
               if (e.getView().getTitle().equals(FidoruOffhandCombineMechanic.GUI_TITLE)) {
                  int slot = e.getRawSlot();
                  IgaDebugLogger.log(this.getClass(), "クリックされたスロット: " + slot);
                  if (slot < this.gui.getSize() && Arrays.stream(FidoruOffhandCombineMechanic.ITEM_SLOTS).noneMatch(i -> i == slot) && slot != 31) {
                     e.setCancelled(true);
                  } else {
                     if (slot == 31) {
                        e.setCancelled(true);
                        if (this.used) {
                           return;
                        }

                        this.used = true;
                        IgaDebugLogger.log(this.getClass(), "決定ボタンが押されました。合成判定を開始。");
                        ItemStack[] inputs = Arrays.stream(FidoruOffhandCombineMechanic.ITEM_SLOTS).mapToObj(this.gui::getItem).toArray(ItemStack[]::new);
                        boolean allFilled = Arrays.stream(inputs).allMatch(itemx -> itemx != null && itemx.getType() != Material.AIR);
                        boolean allMatch = Arrays.stream(inputs).allMatch(itemx -> {
                           String mmid = FidoruOffhandCombineMechanic.getMythicItemIdFromNBT(itemx);
                           IgaDebugLogger.log(this.getClass(), "スロット内アイテムのmmid: " + mmid);
                           return "Iga_fidoru_offhand".equalsIgnoreCase(mmid);
                        });
                        this.gui.clear();
                        if (!allFilled) {
                           IgaDebugLogger.log(this.getClass(), "スロットが未使用のため失敗。");
                        } else if (!allMatch) {
                           IgaDebugLogger.log(this.getClass(), "mmid不一致のため失敗。");
                        }

                        if (allFilled && allMatch) {
                           IgaDebugLogger.log(this.getClass(), "mmid一致。合成成功処理へ。");
                           MythicItem result = (MythicItem)MythicBukkit.inst().getItemManager().getItem("Iga_fidoru_offhand").orElse(null);
                           if (result != null) {
                              ItemStack item = BukkitAdapter.adapt(result.generateItemStack(1));
                              this.player.getInventory().addItem(new ItemStack[]{item});
                              this.player.sendMessage(ChatColor.GOLD + "引き換えに成功しました！");
                              IgaDebugLogger.log(this.getClass(), "合成アイテム付与成功。");
                           } else {
                              this.player.sendMessage(ChatColor.RED + "引き換えに失敗しました。");
                              IgaDebugLogger.log(this.getClass(), "MythicItem[Iga_fidoru_offhand] が見つかりませんでした。");
                           }
                        } else {
                           Arrays.stream(inputs)
                              .filter(itemx -> itemx != null && itemx.getType() != Material.AIR)
                              .forEach(itemx -> this.player.getInventory().addItem(new ItemStack[]{itemx}));
                           this.player.sendMessage(ChatColor.RED + "条件を満たしていません。");
                        }

                        this.player.closeInventory();
                        HandlerList.unregisterAll(this);
                     }
                  }
               }
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent e) {
         if (e.getPlayer().equals(this.player)) {
            IgaDebugLogger.log(this.getClass(), "GUIが閉じられました。未使用アイテムの返却処理。");
            int[] var5;
            int var4 = (var5 = FidoruOffhandCombineMechanic.ITEM_SLOTS).length;

            for (int var3 = 0; var3 < var4; var3++) {
               int slot = var5[var3];
               ItemStack item = this.gui.getItem(slot);
               if (item != null && item.getType() != Material.AIR) {
                  this.player.getInventory().addItem(new ItemStack[]{item});
                  IgaDebugLogger.log(this.getClass(), "スロット[" + slot + "] のアイテムを返却。");
               }
            }

            HandlerList.unregisterAll(this);
         }
      }
   }
}
