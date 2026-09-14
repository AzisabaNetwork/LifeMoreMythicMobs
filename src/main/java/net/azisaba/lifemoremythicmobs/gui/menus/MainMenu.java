package net.azisaba.lifemoremythicmobs.gui.menus;

import net.azisaba.lifemoremythicmobs.gui.holder.GuiHolder;
import net.azisaba.lifemoremythicmobs.session.AttrEditSession;
import net.azisaba.lifemoremythicmobs.util.LegacyText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MainMenu {
   private static Attribute[] sOrder;

   public static Inventory build(Player p, AttrEditSession s) {
      Inventory inv = Bukkit.createInventory(
         new GuiHolder(GuiHolder.Type.MAIN), 27, LegacyText.component(LegacyText.GOLD + "Attribute: " + LegacyText.YELLOW + readable(s.currentAttr()))
      );

      for (int i = 0; i < 7; i++) {
         int idx = (s.getIndex() - 3 + i + s.size() * 10) % s.size();
         Attribute a = sOrder[idx];
         boolean center = i == 3;
         ItemStack it = new ItemStack(center ? Material.NETHER_STAR : Material.PAPER);
         ItemMeta im = it.getItemMeta();
         AttrEditSession.AttrSetting set = s.getSetting(a);
         double v = set.getSaved();
         AttrEditSession.Mode m = set.getSavedMode();
         String summary = LegacyText.DARK_GRAY
            + " ["
            + slotName(set.getSlot())
            + "] "
            + (m == AttrEditSession.Mode.ADD ? LegacyText.GRAY + fmtSigned(v) : LegacyText.GRAY + fmtSignedPct(v));
         im.displayName(LegacyText.component((center ? LegacyText.AQUA + "[選択中] " : LegacyText.WHITE + "") + readable(a)));
         im.lore(LegacyText.components(Arrays.asList(LegacyText.DARK_GRAY + "値 " + (set.getValue() == -2.0 ? LegacyText.RED + "未設定" : summary))));
         it.setItemMeta(im);
         inv.setItem(10 + i, it);
      }

      inv.setItem(
         20, named(Material.ARMOR_STAND, LegacyText.YELLOW + "スロット: " + LegacyText.WHITE + slotName(s.cur().getSlot()) + LegacyText.GRAY + "（左/右クリックで切替）")
      );
      inv.setItem(22, named(Material.LIME_CONCRETE, LegacyText.GREEN + "決定（まとめて適用）"));
      inv.setItem(24, named(Material.REPEATER, LegacyText.YELLOW + "数値設定へ"));
      inv.setItem(0, named(Material.BOOK, LegacyText.GRAY + "外側クリック: 左=前 / 右=次"));
      return inv;
   }

   private static ItemStack named(Material m, String name) {
      ItemStack it = new ItemStack(m);
      ItemMeta im = it.getItemMeta();
      im.displayName(LegacyText.component(name));
      it.setItemMeta(im);
      return it;
   }

   private static String readable(Attribute a) {
      return a.getKey().getKey().replace("generic_", "").replace('_', ' ');
   }

   private static String slotName(EquipmentSlot s) {
      switch (s) {
         case HAND:
            return "MAIN_HAND";
         case OFF_HAND:
            return "OFF_HAND";
         case FEET:
            return "FEET";
         case LEGS:
            return "LEGS";
         case CHEST:
            return "CHEST";
         case HEAD:
            return "HEAD";
         default:
            return s.name();
      }
   }

   private static String fmtSigned(double v) {
      return v == -2.0 ? LegacyText.RED + "未設定" : (v > 0.0 ? "+" : "") + String.format("%.2f", v);
   }

   private static String fmtSignedPct(double v) {
      return v == -2.0 ? LegacyText.RED + "未設定" : (v > 0.0 ? "+" : "") + String.format("%.2f%%", v);
   }

   public static void initOrder(Attribute[] order) {
      sOrder = order;
   }
}

