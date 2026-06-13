package net.azisaba.lifemoremythicmobs.commands;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.gui.UpgradeGUI;
import net.azisaba.lifemoremythicmobs.upgrade.UpgradeStatManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UpgradeCommand extends SubCommand {

    private final LifeMoreMythicMobs plugin;

    public UpgradeCommand(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "upgrade";
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (!LifeMoreMythicMobs.inst().server.equalsIgnoreCase("lifeevent")) {
            player.sendMessage(ChatColor.RED + "このサーバーでは能力強化を使用できません。");
            return;
        }
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("clear")) {
                if (!player.hasPermission("lifemoremythicmobs.lmmm")) {
                    player.sendMessage(ChatColor.RED + "権限がありません。");
                    return;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "使用法: /lmmm upgrade clear <player> <profile>");
                    return;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "プレイヤーが見つかりません。");
                    return;
                }
                String profile = args[2];
                UpgradeStatManager.clearProfile(target, profile);
                player.sendMessage(ChatColor.GREEN + target.getName() + " のプロファイル '" + profile + "' をクリアしました。");
                return;
            }

            // 引数がプロファイル名として扱われる
            String profile = args[0];
            UpgradeGUI.open(player, profile);
            return;
        }

        // 引数なしは hw2026 プロファイル
        UpgradeGUI.open(player, "hw2026");
    }

    @Override
    public @NotNull List<String> suggest(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new java.util.ArrayList<>(UpgradeStatManager.ALL_PROFILES);
            suggestions.add("clear");
            return suggestions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("clear")) {
            return UpgradeStatManager.ALL_PROFILES.stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return super.suggest(player, args);
    }
}
