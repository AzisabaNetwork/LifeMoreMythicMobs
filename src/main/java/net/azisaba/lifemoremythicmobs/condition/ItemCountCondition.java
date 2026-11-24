package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import net.azisaba.lifemoremythicmobs.util.ItemCounter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code itemCount{slot=HAND;key=ns:name;min=1;consume=1;window=2;lore=...}}: true when the number stored on the
 * item in {@code slot} is at least {@code min}. With {@code consume} it also takes that much; for {@code window}
 * ticks after taking, it stays true without taking again (so the same cast is not charged twice).
 */
public class ItemCountCondition extends SkillCondition implements IEntityCondition {
    private static final Map<String, Long> RECENT = new ConcurrentHashMap<>();

    private final ItemCounter counter;
    private final double min;
    private final double consume;
    private final int window;

    public ItemCountCondition(MythicLineConfig config) {
        super(config.getLine());
        this.counter = new ItemCounter(config.getString(new String[]{"slot", "s"}, "HAND"),
                config.getString(new String[]{"key", "k"}, "lifemoremythicmobs:count"),
                config.getString(new String[]{"lore", "l"}, ""));
        this.min = config.getDouble(new String[]{"min", "amount", "a"}, 1.0D);
        this.consume = config.getDouble(new String[]{"consume", "c"}, 0.0D);
        this.window = config.getInteger(new String[]{"window", "w"}, 2);
    }

    @Override
    public boolean check(AbstractEntity target) {
        if (!(target.getBukkitEntity() instanceof Player player)) {
            return false;
        }
        String recentKey = player.getUniqueId() + "|" + counter.key();
        if (consume > 0) {
            Long until = RECENT.get(recentKey);
            if (until != null && until >= Bukkit.getCurrentTick()) {
                return true;
            }
        }
        ItemStack item = counter.item(player);
        Double count = counter.get(item);
        if (count == null || count < min) {
            return false;
        }
        if (consume > 0) {
            counter.set(item, count - consume);
            RECENT.put(recentKey, (long) Bukkit.getCurrentTick() + window);
        }
        return true;
    }
}
