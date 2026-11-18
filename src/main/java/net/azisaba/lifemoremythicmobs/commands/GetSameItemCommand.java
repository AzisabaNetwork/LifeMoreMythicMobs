package net.azisaba.lifemoremythicmobs.commands;

import io.lumine.mythic.bukkit.MythicBukkit;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.LegacyText;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * /lmmm getSame [個数]: 手に持っているアイテムと同じ MMID のアイテムを、今の定義で新しく生成して渡す。
 * 入りきらない分は足元に落とす。
 */
public class GetSameItemCommand extends SubCommand {

    private final LifeMoreMythicMobs plugin;

    public GetSameItemCommand(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "getSame";
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("gs");
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            player.sendMessage(LegacyText.RED + "手にアイテムを持ってください。");
            return;
        }
        String id = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(hand);
        if (id == null || id.isBlank()) {
            player.sendMessage(LegacyText.RED + "手に持っているアイテムは MM アイテムではありません（MMID がありません）。");
            return;
        }
        int amount = 1;
        if (args.length > 0) {
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(LegacyText.RED + "個数は数字で指定してください: /lmmm getSame [個数]");
                return;
            }
            if (amount < 1 || amount > 2304) {
                player.sendMessage(LegacyText.RED + "個数は 1〜2304 で指定してください。");
                return;
            }
        }
        ItemStack template = MythicBukkit.inst().getItemManager().getItemStack(id);
        if (template == null || template.getType().isAir()) {
            player.sendMessage(LegacyText.RED + "MMID " + id + " は今の MythicMobs にありません。");
            return;
        }
        int given = 0;
        int dropped = 0;
        int max = Math.max(1, template.getMaxStackSize());
        int left = amount;
        while (left > 0) {
            ItemStack stack = template.clone();
            int count = Math.min(left, max);
            stack.setAmount(count);
            Map<Integer, ItemStack> rest = player.getInventory().addItem(stack);
            for (ItemStack overflow : rest.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), overflow);
                dropped += overflow.getAmount();
            }
            given += count;
            left -= count;
        }
        player.sendMessage(LegacyText.GREEN + id + " を " + given + " 個取得しました。"
                + (dropped > 0 ? LegacyText.YELLOW + "（" + dropped + " 個は入りきらず足元に落としました）" : ""));
    }

    @Override
    public @NotNull List<String> suggest(@NotNull Player player, @NotNull String[] args) {
        return args.length == 1 ? List.of("1", "16", "64") : List.of();
    }
}
