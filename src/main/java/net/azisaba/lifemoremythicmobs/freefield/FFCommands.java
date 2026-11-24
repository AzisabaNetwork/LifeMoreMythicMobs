package net.azisaba.lifemoremythicmobs.freefield;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** {@code /scale} and {@code /armors} (taken over from FFCore). */
public final class FFCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみ実行できます。", NamedTextColor.RED));
            return true;
        }
        if (command.getName().equalsIgnoreCase("scale")) {
            player.setHealthScaled(true);
            AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
            if (health != null) player.setHealthScale(health.getValue() / 4.0);
            return true;
        }
        AttributeInstance armor = player.getAttribute(Attribute.ARMOR);
        AttributeInstance toughness = player.getAttribute(Attribute.ARMOR_TOUGHNESS);
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                "&fアーマー値: &b" + (armor == null ? 0 : (int) armor.getValue())
                        + " &fタフネス値: &b" + (toughness == null ? 0 : (int) toughness.getValue())));
        return true;
    }
}
