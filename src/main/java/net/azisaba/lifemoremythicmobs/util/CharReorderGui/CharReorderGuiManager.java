package net.azisaba.lifemoremythicmobs.util.CharReorderGui;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillMetadata;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CharReorderGuiManager {
   private static final Map<UUID, CharReorderSession> SESSIONS = new ConcurrentHashMap<>();
   private static final int INVENTORY_SIZE = 27;
   private static final int CONFIRM_SLOT = 26;

   private CharReorderGuiManager() {
   }

   public static CharReorderSession openFor(
      Player player, List<String> chars, String storeKey, String title, String onDecideSkillName, SkillMetadata data, AbstractEntity target, int maxChars
   ) {
      close(player);
      CharReorderGuiHolder holder = new CharReorderGuiHolder(player.getUniqueId());
      Inventory inv = Bukkit.createInventory(holder, 27, ChatColor.translateAlternateColorCodes('&', title));
      ItemStack filter = pane(Material.GRAY_STAINED_GLASS_PANE, " ");

      for (int i = 0; i < inv.getSize(); i++) {
         inv.setItem(i, filter);
      }

      int put = Math.min(maxChars, chars.size());

      for (int i = 0; i < put; i++) {
         inv.setItem(i, charPane(chars.get(i), false));
      }

      for (int i = put; i < maxChars; i++) {
         inv.setItem(i, pane(Material.GRAY_STAINED_GLASS_PANE, " "));
      }

      inv.setItem(26, decideButton());
      CharReorderSession session = new CharReorderSession(player.getUniqueId(), inv, storeKey, onDecideSkillName, data, target, maxChars, 26);
      SESSIONS.put(player.getUniqueId(), session);
      player.openInventory(inv);
      return session;
   }

   public static Optional<CharReorderSession> get(UUID playerId) {
      return Optional.ofNullable(SESSIONS.get(playerId));
   }

   public static void close(Player player) {
      CharReorderSession old = SESSIONS.remove(player.getUniqueId());
      if (old != null && player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() == old.getInventory()) {
         player.closeInventory();
      }
   }

   public static void remove(UUID playerId) {
      SESSIONS.remove(playerId);
   }

   public static ItemStack charPane(String ch, boolean selected) {
      Material m = selected ? Material.YELLOW_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE;
      return pane(m, ChatColor.GREEN + ch);
   }

   public static ItemStack pane(Material mat, String name) {
      ItemStack is = new ItemStack(mat);
      ItemMeta meta = is.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name == null ? " " : name);
         is.setItemMeta(meta);
      }

      return is;
   }

   public static ItemStack decideButton() {
      ItemStack is = new ItemStack(Material.EMERALD_BLOCK);
      ItemMeta meta = is.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(ChatColor.AQUA + "決定");
         is.setItemMeta(meta);
      }

      return is;
   }
}
