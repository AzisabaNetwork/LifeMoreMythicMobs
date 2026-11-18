package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * プレイヤーの移動速度に上限をかける（2026-11-15: ボス戦中、標準の 1.5 倍より速い人を 1.5 倍に揃える）。
 * <pre>
 * speedCap{max=0.15;d=20} @PIR{r=48}
 * </pre>
 * max は移動速度の属性値（標準 0.1）。走っている時の +30% は数えない（走れば max の 1.3 倍まで出る）。
 * 装備・効果（速度上昇など）を合わせた値が max を超えていれば、超えた分を打ち消す倍率をかける。
 * d tick 呼ばれなければ外れる（ボス戦が終わると元に戻る）。
 */
public class SpeedCapMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private static final Map<UUID, BukkitTask> EXPIRE = new HashMap<>();
    private static final String SPRINT = "minecraft:sprinting";
    private static final boolean DEBUG = Boolean.getBoolean("lmmm.debug");
    private final double max;
    private final int duration;

    public SpeedCapMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.max = config.getDouble(new String[] {"max", "m"}, 0.15);
        this.duration = config.getInteger(new String[] {"duration", "d"}, 20);
    }

    private static NamespacedKey key() {
        return new NamespacedKey(LifeMoreMythicMobs.inst(), "speedcap");
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null || !(target.getBukkitEntity() instanceof Player player)) return SkillResult.INVALID_TARGET;
        if (Bukkit.isPrimaryThread()) apply(player);
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), () -> apply(player));
        return SkillResult.SUCCESS;
    }

    private void apply(Player player) {
        AttributeInstance ai = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (ai == null) return;
        NamespacedKey k = key();
        double add = ai.getBaseValue(), scalar = 0, mult = 1;
        for (AttributeModifier m : ai.getModifiers()) {
            String id = m.getKey().toString();
            if (id.equals(k.toString()) || id.equals(SPRINT)) continue;
            switch (m.getOperation()) {
                case ADD_NUMBER -> add += m.getAmount();
                case ADD_SCALAR -> scalar += m.getAmount();
                case MULTIPLY_SCALAR_1 -> mult *= 1 + m.getAmount();
            }
        }
        double v = add * (1 + scalar) * mult;
        if (DEBUG) Bukkit.getLogger().info("[speedCap] " + player.getName() + " v=" + v + " max=" + max + " now=" + ai.getValue() + " mods=" + ai.getModifiers().size());
        AttributeModifier old = ai.getModifier(k);
        if (old != null) ai.removeModifier(old);
        if (v > max && v > 0) {
            ai.addTransientModifier(new AttributeModifier(k, max / v - 1, AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY));
        }
        BukkitTask t = EXPIRE.remove(player.getUniqueId());
        if (t != null) t.cancel();
        EXPIRE.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(), () -> {
            EXPIRE.remove(player.getUniqueId());
            AttributeInstance a2 = player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (a2 == null) return;
            AttributeModifier m2 = a2.getModifier(key());
            if (m2 != null) a2.removeModifier(m2);
        }, duration));
    }
}
