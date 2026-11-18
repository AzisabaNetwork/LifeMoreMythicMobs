package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.util.TempElementalDefense;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * プレイヤーに一定時間、属性防御値・属性防御強度値を足す（HoroloCore の装飾品・宝玉に上乗せ）。
 * <pre>
 * elemDefBuff{n=名前;armor=12;toughness=12;d=3600;elements=ALL} @self
 * elemDefBuff{n=名前;remove=true} @self
 * </pre>
 * armor（a）・toughness（t）は値、d は tick。elements（e）は ALL か FIRE,WATER のような並び。
 * 受けるダメージ = 指定値 / (1 + 0.025 × 属性防御値) / (1 + 0.025 × 属性防御強度値)（mobTDamage の計算）。
 */
public class ElementalDefenseBuffMechanic extends SkillMechanic implements ITargetedEntitySkill, INoTargetSkill {
    private final String name;
    private final PlaceholderDouble armor;
    private final PlaceholderDouble toughness;
    private final PlaceholderDouble duration;
    private final Set<String> elements;
    private final boolean remove;

    public ElementalDefenseBuffMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.name = config.getString(new String[] {"name", "n"}, "elemdef");
        this.armor = PlaceholderDouble.of(config.getString(new String[] {"armor", "a"}, "0"));
        this.toughness = PlaceholderDouble.of(config.getString(new String[] {"toughness", "t"}, "0"));
        this.duration = PlaceholderDouble.of(config.getString(new String[] {"duration", "d"}, "100"));
        this.remove = config.getBoolean(new String[] {"remove", "r"}, false);
        String raw = config.getString(new String[] {"elements", "e"}, "ALL");
        Set<String> set = new HashSet<>();
        for (String s : raw.replace("\"", "").split("[,\\s]+")) {
            String v = s.trim().toUpperCase(Locale.ROOT);
            if (!v.isEmpty() && !v.equals("ALL")) set.add(v);
        }
        this.elements = set;
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        return castAtEntity(data, data.getCaster().getEntity());
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null || !(target.getBukkitEntity() instanceof Player player)) return SkillResult.INVALID_TARGET;
        if (remove) {
            TempElementalDefense.remove(player.getUniqueId(), name);
            return SkillResult.SUCCESS;
        }
        double a = armor.get(data, target);
        double t = toughness.get(data, target);
        long ticks = Math.round(duration.get(data, target));
        if (!Double.isFinite(a) || !Double.isFinite(t) || ticks <= 0) return SkillResult.ERROR;
        TempElementalDefense.put(player.getUniqueId(), name, elements, a, t, ticks);
        return SkillResult.SUCCESS;
    }
}
