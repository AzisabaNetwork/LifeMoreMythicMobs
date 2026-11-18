package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.LootTableUtil;
import net.azisaba.lifemoremythicmobs.util.LootTableUtil.EntryKind;
import net.azisaba.lifemoremythicmobs.util.LootTableUtil.LootEntry;
import net.azisaba.lifemoremythicmobs.util.SkillUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 重み付きランダムで MythicItem 付与またはスキル実行を行うルート抽選メカニクス。
 * Luck 属性値に応じてレア枠（重みが小さい枠）の確率をブーストするオプション付き。
 * <pre>
 * lmLootRoll{entries="StoneShard:60,GoldCrystal:30,skill:JackpotSkill:5,nothing:5";rolls=2;luckFactor=0.05} @trigger
 * </pre>
 */
public class LootRollMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final List<LootEntry> entries;
    private final int rolls;
    private final double luckFactor;

    public LootRollMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        String raw = config.getString(new String[]{"entries", "e", "table"}, "");
        this.entries = LootTableUtil.parseEntries(raw);
        this.rolls = Math.max(1, Math.min(64, config.getInteger(new String[]{"rolls", "r"}, 1)));
        this.luckFactor = config.getDouble(new String[]{"luckfactor", "lf"}, 0.0);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null || entries.isEmpty()) return SkillResult.INVALID_TARGET;
        Entity entity = BukkitAdapter.adapt(target);
        if (!(entity instanceof Player player)) return SkillResult.INVALID_TARGET;

        Runnable r = () -> executeRolls(data, target, player);
        if (Bukkit.isPrimaryThread()) r.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), r);
        return SkillResult.SUCCESS;
    }

    private void executeRolls(SkillMetadata data, AbstractEntity target, Player player) {
        double luck = 0.0;
        AttributeInstance attr = player.getAttribute(Attribute.LUCK);
        if (attr != null) {
            luck = attr.getValue();
        }

        for (int i = 0; i < rolls; i++) {
            LootEntry picked = LootTableUtil.pickEntry(entries, luck, luckFactor, ThreadLocalRandom.current().nextDouble());
            if (picked == null || picked.kind == EntryKind.NOTHING) continue;

            if (picked.kind == EntryKind.SKILL) {
                SkillUtil.executeSkill(picked.target, data, target);
            } else if (picked.kind == EntryKind.ITEM) {
                MythicBukkit.inst().getItemManager().getItem(picked.target).ifPresent(mythicItem -> {
                    ItemStack stack = BukkitAdapter.adapt(mythicItem.generateItemStack(picked.amount));
                    Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
                    for (ItemStack rem : leftover.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), rem);
                    }
                });
            }
        }
    }
}
