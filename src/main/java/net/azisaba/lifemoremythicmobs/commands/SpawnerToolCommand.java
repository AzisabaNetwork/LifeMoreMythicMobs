package net.azisaba.lifemoremythicmobs.commands;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.gui.SpawnerManagerGUI;
import net.azisaba.lifemoremythicmobs.gui.SpawnerToolGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnerToolCommand extends SubCommand {
    private final LifeMoreMythicMobs plugin;

    public SpawnerToolCommand(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "spawner";
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("list")) {
                SpawnerManagerGUI.openMain(player);
                return;
            } else if (args[0].equalsIgnoreCase("create")) {
                openTool(player);
                return;
            }
        }

        player.sendMessage(ChatColor.YELLOW + "使用法: /lmmm spawner <create|list>");
    }

    private void openTool(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "アイテムを手に持ってください。");
            return;
        }

        SpawnerToolGUI.open(player, item, null);
    }

    @Override
    public @NotNull List<String> suggest(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "list").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return super.suggest(player, args);
    }
}
