package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.SkillCheckManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * ActionBar 上のタイミングバー（QTE）を開始し、入力結果に応じてスキルを分岐実行する。
 * <pre>
 * lmSkillCheck{label="&e採掘";speed=0.045;d=80;input=SNEAK;onGreat=MineCrit;onGood=MineNormal;onFail=MineMiss} @trigger
 * </pre>
 */
public class SkillCheckMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final SkillCheckManager.CheckSpec spec;

    public SkillCheckMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        String label = config.getString(new String[]{"label", "l"}, "&eタイミング");
        double speed = config.getDouble(new String[]{"speed", "sp"}, 0.045);
        int duration = config.getInteger(new String[]{"duration", "d"}, 80);
        double center = config.getDouble(new String[]{"center", "c"}, -1.0);
        double greatR = config.getDouble(new String[]{"great", "gr"}, 0.08);
        double goodR = config.getDouble(new String[]{"good", "go"}, 0.20);
        SkillCheckManager.InputType input = SkillCheckManager.InputType.SNEAK;
        try {
            input = SkillCheckManager.InputType.valueOf(config.getString(new String[]{"input", "in"}, "SNEAK").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        String onGreat = config.getString(new String[]{"ongreat", "ogr"}, null);
        String onGood = config.getString(new String[]{"ongood", "ogo"}, null);
        String onFail = config.getString(new String[]{"onfail", "of"}, null);
        this.spec = new SkillCheckManager.CheckSpec(
                label, speed, duration, center, greatR, goodR, input, onGreat, onGood, onFail
        );
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null || !(BukkitAdapter.adapt(target) instanceof Player player)) {
            return SkillResult.INVALID_TARGET;
        }
        Runnable r = () -> SkillCheckManager.start(player, spec, data);
        if (Bukkit.isPrimaryThread()) r.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), r);
        return SkillResult.SUCCESS;
    }
}
