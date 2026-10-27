package net.azisaba.lifemoremythicmobs.commands;

import net.azisaba.lifemoremythicmobs.util.LegacyText;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand {
    public abstract @NotNull String getName();

    /** Short names that also select this subcommand (not shown in help or completion). */
    public @NotNull List<String> getAliases() {
        return Collections.emptyList();
    }

    public boolean matches(@NotNull String label) {
        return getName().equalsIgnoreCase(label) || getAliases().stream().anyMatch(label::equalsIgnoreCase);
    }

    public abstract void execute(@NotNull Player player, @NotNull String[] args);

    /** Entry point used by {@link RootCommand}. Override to also accept the console. */
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LegacyText.RED + "プレイヤーのみが実行できます");
            return;
        }
        execute(player, args);
    }

    public @NotNull List<String> suggest(@NotNull CommandSender sender, @NotNull String[] args) {
        return sender instanceof Player player ? suggest(player, args) : Collections.emptyList();
    }

    public @NotNull List<String> suggest(@NotNull Player player, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
