package net.azisaba.lifemoremythicmobs.commands;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FindMythicItemCommand extends SubCommand {
    @Override
    public @NotNull String getName() {
        return "findMythicItem";
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull [] args) {
        for (MythicItem item : MythicMobs.inst().getItemManager().getItems()) {
            if (item.getDisplayName() == null) continue;
            String strippedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', item.getDisplayName()));
            if (strippedName.toLowerCase().contains(args[0].toLowerCase())) {
                player.sendMessage(item.getInternalName() + " -> " + item.getDisplayName());
            }
        }
    }
}
