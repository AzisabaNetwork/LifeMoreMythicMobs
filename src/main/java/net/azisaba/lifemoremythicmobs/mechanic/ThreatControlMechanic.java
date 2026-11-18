package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;

/**
 * MythicMob のヘイト（ThreatTable）を操作するメカニクス。
 * ターゲットが MythicMob の場合はターゲットのヘイトテーブルを、そうでなければキャスターのヘイトテーブルを操作する。
 * <pre>
 * lmThreat{mode=TAUNT;a=500} @trigger
 * lmThreat{mode=MULTIPLY;a=0.5} @target
 * </pre>
 */
public class ThreatControlMechanic extends SkillMechanic implements ITargetedEntitySkill {
    public enum Mode {
        TAUNT, ADD, SET, MULTIPLY, DROP, CLEAR
    }

    private final Mode mode;
    private final double amount;

    public ThreatControlMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        Mode parsed = Mode.ADD;
        try {
            parsed = Mode.valueOf(config.getString(new String[]{"mode", "m"}, "ADD").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        this.mode = parsed;
        this.amount = config.getDouble(new String[]{"amount", "a"}, 100.0);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.INVALID_TARGET;

        ActiveMob mob = null;
        AbstractEntity threatHolder = target;
        if (data.getCaster() instanceof ActiveMob casterMob && casterMob.hasThreatTable()) {
            mob = casterMob;
        }
        if (mob == null) {
            return SkillResult.CONDITION_FAILED;
        }

        var table = mob.getThreatTable();
        switch (mode) {
            case TAUNT -> {
                double topVal = table.getTopTargetThreat();
                double curVal = table.getThreat(threatHolder);
                double targetThreat = Math.max(curVal + amount, topVal + amount);
                table.threatSet(threatHolder, targetThreat);
                table.Taunt(threatHolder);
            }
            case ADD -> {
                if (amount >= 0) table.threatGain(threatHolder, amount);
                else table.threatLoss(threatHolder, -amount);
            }
            case SET -> table.threatSet(threatHolder, Math.max(0.0, amount));
            case MULTIPLY -> {
                double cur = table.getThreat(threatHolder);
                table.threatSet(threatHolder, Math.max(0.0, cur * amount));
            }
            case DROP -> table.observeDeath(threatHolder);
            case CLEAR -> table.clearTarget();
        }
        return SkillResult.SUCCESS;
    }
}
