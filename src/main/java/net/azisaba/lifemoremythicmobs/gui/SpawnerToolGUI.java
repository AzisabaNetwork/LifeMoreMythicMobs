package net.azisaba.lifemoremythicmobs.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpawnerToolGUI {
    public static final String TITLE = "スポナー設置ツールの設定";

    public static void open(Player player, ItemStack item, Map<String, String> data) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        String currentMob = (data != null && data.containsKey("mob")) ? data.get("mob") : "未設定";
        String currentSpawner = (data != null && data.containsKey("spawner")) ? data.get("spawner") : "未設定";
        String currentGroup = (data != null && data.containsKey("group")) ? data.get("group") : "未設定";

        ItemStack icon = item.clone();
        ItemMeta iconMeta = icon.getItemMeta();
        if (iconMeta != null) {
            iconMeta.setDisplayName(ChatColor.WHITE + "対象アイテム");
            icon.setItemMeta(iconMeta);
        }
        inv.setItem(0, icon);

        inv.setItem(1, createSettingItem(Material.ZOMBIE_SPAWN_EGG, "モブ名の設定", "出現させるモブのIDを指定します", currentMob));
        inv.setItem(3, createSettingItem(Material.NAME_TAG, "スポナー名の設定", "スポナーのベース名(接頭辞)を指定します", currentSpawner));
        inv.setItem(5, createSettingItem(Material.BOOK, "グループの設定", "スポナーを所属させるグループを指定します", currentGroup));

        ItemStack completeItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta completeMeta = completeItem.getItemMeta();
        completeMeta.setDisplayName(ChatColor.GREEN + "作成完了");
        completeMeta.setLore(Collections.singletonList(ChatColor.GRAY + "手に持っているアイテムをツール化します"));
        completeItem.setItemMeta(completeMeta);
        inv.setItem(8, completeItem);

        Map<String, String> optDesc = new HashMap<>();
        optDesc.put("MaxMobs", "最大出現数");
        optDesc.put("MobLevel", "モブのレベル");
        optDesc.put("Radius", "出現半径");
        optDesc.put("ActivationRange", "稼働範囲");
        optDesc.put("LeashRange", "紐付け範囲(離れられる距離)");
        optDesc.put("Cooldown", "再出現の待機時間(秒)");
        optDesc.put("Warmup", "出現前の溜め時間(秒)");
        optDesc.put("MaxDistance", "プレイヤーとの最大距離");
        optDesc.put("CheckForPlayers", "プレイヤーの有無を確認(true/false)");

        String[] optionsRow2 = {"MaxMobs", "MobLevel", "Radius", "ActivationRange", "LeashRange", "Cooldown", "Warmup", "MaxDistance", "CheckForPlayers"};
        for (int i = 0; i < optionsRow2.length; i++) {
            String opt = optionsRow2[i];
            String val = (data != null && data.containsKey("opt." + opt)) ? data.get("opt." + opt) : "デフォルト";
            inv.setItem(9 + i, createSettingItem(Material.RED_WOOL, "設定: " + opt, optDesc.get(opt), val, false));
        }

        String[] optionsRow3 = {"UseWorldScaling", "PlayerRange", "Amount"};
        Map<String, String> optDesc3 = new HashMap<>();
        optDesc3.put("UseWorldScaling", "ワールドの難易度設定に従う(true/false)");
        optDesc3.put("PlayerRange", "プレイヤーを検知する範囲");
        optDesc3.put("Amount", "一度に出現するモブの数");

        for (int i = 0; i < optionsRow3.length; i++) {
            String opt = optionsRow3[i];
            String val = (data != null && data.containsKey("opt." + opt)) ? data.get("opt." + opt) : "デフォルト";
            inv.setItem(18 + i, createSettingItem(Material.RED_WOOL, "設定: " + opt, optDesc3.get(opt), val, false));
        }

        ItemStack cancelItem = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ChatColor.RED + "キャンセル");
            cancelMeta.setLore(Collections.singletonList(ChatColor.GRAY + "設定を破棄して閉じます"));
            cancelItem.setItemMeta(cancelMeta);
        }
        inv.setItem(26, cancelItem);

        player.openInventory(inv);
    }

    private static ItemStack createSettingItem(Material material, String name, String desc, String current) {
        return createSettingItem(material, name, desc, current, true);
    }

    private static ItemStack createSettingItem(Material material, String name, String desc, String current, boolean allowEnchant) {
        boolean isSet = current != null && !current.equals("未設定") && !current.equals("デフォルト");
        
        Material finalMaterial = material;
        if (material == Material.RED_WOOL && isSet) {
            finalMaterial = Material.LIME_WOOL;
        }

        ItemStack item = new ItemStack(finalMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name);
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + desc,
                    ChatColor.WHITE + "左クリックしてチャットで入力",
                    ChatColor.WHITE + "右クリックしてリセット",
                    ChatColor.AQUA + "現在の設定: " + current
            ));

            if (isSet && allowEnchant) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }
        return item;
    }
}
