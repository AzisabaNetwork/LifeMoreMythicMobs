package net.azisaba.lifemoremythicmobs.gui.menus;

import net.azisaba.lifemoremythicmobs.gui.holder.GuiHolder;
import net.azisaba.lifemoremythicmobs.session.AttrEditSession;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ValueMenu {
   public static Inventory build(Player p, AttrEditSession s) {
      Attribute a = s.currentAttr();
      Inventory inv = Bukkit.createInventory(
         new GuiHolder(GuiHolder.Type.VALUE),
         27,
         ChatColor.BLUE
            + "値: "
            + ChatColor.YELLOW
            + readable(a)
            + ChatColor.DARK_GRAY
            + " ["
            + (s.cur().getDraftOrSavedMode() == AttrEditSession.Mode.ADD ? "加算" : "+%")
            + "]"
      );
      double display = s.cur().getDraftOrSaved();
      inv.setItem(4, lore(named(Material.BOOK, ChatColor.GRAY + "現在値: " + ChatColor.WHITE + fmtDisplay(display)), ChatColor.DARK_GRAY + " -『未設定に戻す』で未設定へ"));
      inv.setItem(1, named(Material.RED_CONCRETE, ChatColor.RED + "-100"));
      inv.setItem(2, named(Material.RED_CONCRETE_POWDER, ChatColor.RED + "-10"));
      inv.setItem(3, named(Material.ORANGE_CONCRETE, ChatColor.GOLD + "-1"));
      inv.setItem(5, named(Material.LIGHT_BLUE_CONCRETE, ChatColor.AQUA + "+1"));
      inv.setItem(6, named(Material.BLUE_CONCRETE_POWDER, ChatColor.AQUA + "+10"));
      inv.setItem(7, named(Material.BLUE_CONCRETE, ChatColor.AQUA + "+100"));
      inv.setItem(11, named(Material.GUNPOWDER, ChatColor.RED + "-0.1"));
      inv.setItem(12, named(Material.REDSTONE, ChatColor.RED + "-0.01"));
      inv.setItem(14, named(Material.SUGAR, ChatColor.AQUA + "+0.01"));
      inv.setItem(15, named(Material.GLOWSTONE_DUST, ChatColor.AQUA + "+0.1"));
      inv.setItem(18, named(Material.COMPARATOR, ChatColor.YELLOW + "モード切替（加算/%）"));
      inv.setItem(20, named(Material.BARRIER, ChatColor.GRAY + "戻る（反映しない）"));
      inv.setItem(21, named(Material.GRAY_CONCRETE, ChatColor.GRAY + "未設定に戻す"));
      inv.setItem(22, named(Material.LIME_CONCRETE, ChatColor.GREEN + "決定（保存）"));
      return inv;
   }

   private static ItemStack named(Material m, String name) {
      ItemStack it = new ItemStack(m);
      ItemMeta im = it.getItemMeta();
      im.setDisplayName(name);
      it.setItemMeta(im);
      return it;
   }

   private static ItemStack lore(ItemStack it, String... lines) {
      ItemMeta im = it.getItemMeta();
      im.setLore(Arrays.asList(lines));
      it.setItemMeta(im);
      return it;
   }

   private static String readable(Attribute a) {
      return a.name().replace("GENERIC_", "").toLowerCase().replace('_', ' ');
   }

   private static String fmtDisplay(double v) {
      return v == -2.0 ? ChatColor.RED + "未設定" + ChatColor.DARK_GRAY + "（0.00から）" : String.format("%.2f", v);
   }
}
