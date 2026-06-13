package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import net.azisaba.lifemoremythicmobs.util.VariableUtil;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

public class SelectionFidoruGUIMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private static final String[][] OPTIONS = new String[][]{
      {"盾たるエルベ", "沈めるネーベル"}, {"導きのゼーレ", "内に秘めしアルカナム", "満ちるガイスト"}, {"飛翔せしヴィレ", "囁くギフト", "光差すゼーゲン"}, {"祝福のヴォーゲ", "燃えるケルン", "煌きのクラフトフェルト"}
   };
   private static final int GUI_SIZE = 54;
   private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "能力設定メニュー";
   private static final int DECIDE_SLOT = 49;
   private final Map<UUID, int[]> selections = new HashMap<>();
   private final Map<UUID, Inventory> openInventories = new HashMap<>();
   private final String[] variableNames;
   private final String onEndSkillName;
   private SkillMetadata currentMetadata;

   public SelectionFidoruGUIMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.variableNames = new String[]{config.getString("var1", ""), config.getString("var2", ""), config.getString("var3", ""), config.getString("var4", "")};
      this.onEndSkillName = config.getString(new String[]{"onEndSkill", "onEnd", "oe"}, "", new String[0]);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!target.isPlayer()) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)target.getBukkitEntity();
      Inventory gui = Bukkit.createInventory(player, 54, GUI_TITLE);
      int[] selection = new int[]{-1, -1, -1, -1};
      this.buildGUI(player, gui, selection);
      player.openInventory(gui);
      Bukkit.getPluginManager()
         .registerEvents(new SelectionFidoruGUIMechanic.SelectionListener(player, gui, data.deepClone(), selection), JavaPlugin.getPlugin(LifeMoreMythicMobs.class));
      return SkillResult.SUCCESS;
   }

   public void buildGUI(Player player, Inventory gui, int[] sel) {
      gui.clear();

      for (int row = 0; row < OPTIONS.length; row++) {
         String[] rowOptions = OPTIONS[row];

         for (int i = 0; i < rowOptions.length; i++) {
            int slot = row * 9 + 2 + i;
            boolean selected = sel[row] == i;
            gui.setItem(slot, this.createOptionPane(rowOptions[i], selected));
         }
      }

      for (int i = 0; i < 54; i++) {
         if (gui.getItem(i) == null) {
            gui.setItem(i, this.createDummyPane());
         }
      }

      ItemStack confirm = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE);
      confirm.setDurability((short)5);
      ItemMeta meta = confirm.getItemMeta();
      meta.setDisplayName(ChatColor.GREEN + "決定");
      confirm.setItemMeta(meta);
      gui.setItem(49, confirm);
   }

   private ItemStack createOptionPane(String name, boolean selected) {
      ItemStack item = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE);
      item.setDurability((short)(selected ? 10 : 7));
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(ChatColor.YELLOW + name);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack createDummyPane() {
      ItemStack item = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE);
      item.setDurability((short)15);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(" ");
      item.setItemMeta(meta);
      return item;
   }

   private class SelectionListener implements Listener {
      private final Player player;
      private final Inventory gui;
      private final int[] selection;
      private final SkillMetadata metadata;
      private boolean alreadySaved = false;

      public SelectionListener(Player player, Inventory gui, SkillMetadata metadata, int[] selection) {
         this.player = player;
         this.gui = gui;
         this.metadata = metadata;
         this.selection = selection;
      }

      @EventHandler
      public void onInventoryClick(InventoryClickEvent event) {
         if (event.getWhoClicked().equals(this.player)) {
            if (event.getView().getTitle().equals(SelectionFidoruGUIMechanic.GUI_TITLE)) {
               event.setCancelled(true);
               int slot = event.getRawSlot();
               IgaDebugLogger.log(this.getClass(), "Clicked slot: " + slot);
               if (slot == 49) {
                  IgaDebugLogger.log(this.getClass(), "決定ボタンが押されました。保存処理に進みます。");
                  if (!this.alreadySaved) {
                     this.saveSelections();
                     this.alreadySaved = true;
                     if (!SelectionFidoruGUIMechanic.this.onEndSkillName.isEmpty()) {
                        Skill onEndSkill = (Skill)MythicBukkit.inst().getSkillManager().getSkill(SelectionFidoruGUIMechanic.this.onEndSkillName).orElse(null);
                        if (onEndSkill != null) {
                           AbstractEntity entity = BukkitAdapter.adapt(this.player);
                           this.metadata.setTrigger(entity);
                           IgaDebugLogger.log(this.getClass(), "onEndSkill を実行: " + onEndSkill.getInternalName());
                           onEndSkill.execute(this.metadata);
                        } else {
                           IgaDebugLogger.log(this.getClass(), "onEndSkill が見つかりません: " + SelectionFidoruGUIMechanic.this.onEndSkillName);
                        }
                     }
                  }

                  this.player.closeInventory();
               } else {
                  for (int row = 0; row < SelectionFidoruGUIMechanic.OPTIONS.length; row++) {
                     int baseSlot = row * 9 + 2;

                     for (int i = 0; i < SelectionFidoruGUIMechanic.OPTIONS[row].length; i++) {
                        if (slot == baseSlot + i) {
                           this.selection[row] = i;
                           IgaDebugLogger.log(this.getClass(), "row=" + row + ", col=" + i + ", 選択: " + SelectionFidoruGUIMechanic.OPTIONS[row][i]);
                           SelectionFidoruGUIMechanic.this.buildGUI(this.player, this.gui, this.selection);
                           return;
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

      private void saveSelections() {
         AbstractEntity entity = BukkitAdapter.adapt(this.player);

         for (int i = 0; i < 4; i++) {
            String var = SelectionFidoruGUIMechanic.this.variableNames[i];
            if (var != null && !var.isEmpty()) {
               int sel = this.selection[i];
               String value = sel >= 0 && sel < SelectionFidoruGUIMechanic.OPTIONS[i].length ? SelectionFidoruGUIMechanic.OPTIONS[i][sel] : "";
               IgaDebugLogger.log(this.getClass(), "setScopedVariable: var=" + var + ", value=" + value);
               VariableUtil.setScopedVariable(var, value, this.metadata, entity);
            } else {
               IgaDebugLogger.log(this.getClass(), "変数名が未設定: index=" + i);
            }
         }
      }
   }
}
