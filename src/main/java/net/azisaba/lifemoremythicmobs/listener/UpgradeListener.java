package net.azisaba.lifemoremythicmobs.listener;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.gui.UpgradeGUI;
import net.azisaba.lifemoremythicmobs.upgrade.UpgradeStatManager;
import net.azisaba.lifemoremythicmobs.upgrade.UpgradeStatManager.StatType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class UpgradeListener implements Listener {

    private final LifeMoreMythicMobs plugin;

    public UpgradeListener(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(UpgradeGUI.TITLE_PREFIX)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String profile = title.substring(UpgradeGUI.TITLE_PREFIX.length());
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        java.util.List<StatType> displayedTypes = UpgradeGUI.getDisplayedTypes(profile);
        StatType selectedType = null;

        for (int i = 0; i < UpgradeGUI.SLOTS.length && i < displayedTypes.size(); i++) {
            if (UpgradeGUI.SLOTS[i] == slot) {
                selectedType = displayedTypes.get(i);
                break;
            }
        }

        if (selectedType != null) {
            int currentLevel = UpgradeStatManager.getLevel(player, profile, selectedType);

            Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
            String objectiveName = profile.equalsIgnoreCase("common") ? "upgrade_point" : profile.toUpperCase() + "_point";
            Objective obj = sb.getObjective(objectiveName);

            if (obj == null) {
                player.sendMessage(ChatColor.RED + "エラー: スコアボード項目 '" + objectiveName + "' が見つかりません。");
                return;
            }

            Score score = obj.getScore(player.getName());
            int currentPoints = score.getScore();

            if (event.isLeftClick()) {
                if (currentLevel >= UpgradeStatManager.MAX_LEVEL) {
                    player.sendMessage(ChatColor.RED + "既に最大レベルに達しています。");
                    return;
                }
                int cost = (currentLevel + 1) * 5;
                if (currentPoints >= cost) {
                    score.setScore(currentPoints - cost);
                    UpgradeStatManager.setLevel(player, profile, selectedType, currentLevel + 1);
                    player.sendMessage(ChatColor.GREEN + selectedType.displayName + " をレベル " + (currentLevel + 1) + " に強化しました！");
                    UpgradeGUI.open(player, profile); // GUIを更新
                } else {
                    player.sendMessage(ChatColor.RED + "ポイントが足りません！ (必要: " + cost + ")");
                }
            } else if (event.isRightClick()) {
                if (currentLevel > 0) {
                    int refund = (currentLevel * 5) / 2;
                    score.setScore(currentPoints + refund);
                    UpgradeStatManager.setLevel(player, profile, selectedType, currentLevel - 1);
                    player.sendMessage(ChatColor.YELLOW + selectedType.displayName + " の強化を取り消しました。(レベル " + (currentLevel - 1) + ")");
                    if (refund > 0) {
                        player.sendMessage(ChatColor.GRAY + "ポイントが " + refund + " 返却されました。");
                    }
                    UpgradeGUI.open(player, profile); // GUIを更新
                } else {
                    player.sendMessage(ChatColor.RED + "これ以上レベルを下げることはできません。");
                }
            }
        }
    }
}