package net.azisaba.lifemoremythicmobs.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand {
    public abstract @NotNull String getName();

    public abstract void execute(@NotNull Player player, @NotNull String @NotNull [] args);

    public @NotNull List<@NotNull String> suggest(@NotNull Player player, @NotNull String @NotNull [] args) {
        return Collections.emptyList();
    }
}
