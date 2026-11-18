package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.ParryWindowManager;
import org.bukkit.Bukkit;

/**
 * 対象に短時間のパリィ（受け流し）受付状態を付与する。
 * <pre>
 * lmParryWindow{d=12;reduce=1.0;consume=true;onParry=CounterSlash;onExpire=ParryMiss} @self
 * </pre>
 */
public class ParryWindowMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final int duration;
    private final double reduceRatio;
    private final boolean consume;
    private final String onParrySkill;
    private final String onExpireSkill;

    public ParryWindowMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.duration = Math.max(1, config.getInteger(new String[]{"duration", "d"}, 10));
        this.reduceRatio = config.getDouble(new String[]{"reduce", "r"}, 1.0);
        this.consume = config.getBoolean(new String[]{"consume", "c"}, true);
        this.onParrySkill = config.getString(new String[]{"onparry", "op"}, null);
        this.onExpireSkill = config.getString(new String[]{"onexpire", "oe"}, null);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        Runnable r = () -> ParryWindowManager.activate(
                target.getUniqueId(), duration, reduceRatio, consume, onParrySkill, onExpireSkill, data
        );
        if (Bukkit.isPrimaryThread()) r.run();
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), r);
        return SkillResult.SUCCESS;
    }
}
