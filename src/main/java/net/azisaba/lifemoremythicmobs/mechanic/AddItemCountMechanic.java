package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.util.ItemCounter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * {@code addItemCount{slot=OFFHAND;key=ns:name;amount=20;perStack=HAND;lore="&f回数: &b$count";
 * message="+$added";totalMessage="合計 $count"}}: adds to the number stored on the item in {@code slot}.
 * {@code perStack} multiplies the amount by the stack size in that slot. {@code lore} keeps one lore line showing
 * the number. The messages are sent to the player ({@code $added} / {@code $count}).
 */
public class AddItemCountMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final ItemCounter counter;
    private final PlaceholderDouble amount;
    private final EquipmentSlot perStack;
    private final String message;
    private final String totalMessage;

    public AddItemCountMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, null, config.getLine(), config);
        this.counter = new ItemCounter(config.getString(new String[]{"slot", "s"}, "HAND"),
                config.getString(new String[]{"key", "k"}, "lifemoremythicmobs:count"),
                config.getString(new String[]{"lore", "l"}, ""));
        this.amount = config.getPlaceholderDouble(new String[]{"amount", "a"}, 1.0D);
        String per = config.getString(new String[]{"perStack", "ps"}, "");
        this.perStack = per.isEmpty() ? null : ItemCounter.parseSlot(per);
        this.message = ItemCounter.text(config.getString(new String[]{"message", "m"}, ""));
        this.totalMessage = ItemCounter.text(config.getString(new String[]{"totalMessage", "tm"}, ""));
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!(target.getBukkitEntity() instanceof Player player)) {
            return SkillResult.INVALID_TARGET;
        }
        ItemStack item = counter.item(player);
        if (item == null) {
            return SkillResult.CONDITION_FAILED;
        }
        double added = amount.get(data, target);
        if (perStack != null) {
            ItemStack stack = player.getInventory().getItem(perStack);
            added *= stack == null || stack.getType().isAir() ? 0 : stack.getAmount();
        }
        Double current = counter.get(item);
        double total = (current == null ? 0.0D : current) + added;
        send(player, message, added, total);
        counter.set(item, total);
        send(player, totalMessage, added, total);
        return SkillResult.SUCCESS;
    }

    private static void send(Player player, String text, double added, double total) {
        if (text.isEmpty()) return;
        player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                text.replace("$added", String.valueOf(added)).replace("$count", String.valueOf(total))));
    }
}
