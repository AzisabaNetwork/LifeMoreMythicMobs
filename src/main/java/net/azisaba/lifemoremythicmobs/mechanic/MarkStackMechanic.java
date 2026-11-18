package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.util.MarkStackManager;
import net.azisaba.lifemoremythicmobs.util.SkillUtil;

/**
 * 対象に期限付きスタック型マーク（印・共鳴・蓄積ゲージ）を付与・操作・起爆するメカニクス。
 * <pre>
 * lmMarkStack{id=burn;mode=ADD;amount=1;max=5;d=120;resetOnMax=true;onMax=BurnBurst;var=lmmm_mark_stacks} @target
 * lmMarkStack{id=burn;mode=DETONATE;onDetonate=BurnDetonate;var=burn_count} @target
 * </pre>
 */
public class MarkStackMechanic extends SkillMechanic implements ITargetedEntitySkill {
    public enum Mode {
        ADD, SET, TAKE, DETONATE, CLEAR
    }

    private final String markId;
    private final Mode mode;
    private final int amount;
    private final int maxStacks;
    private final int durationTicks;
    private final boolean resetOnMax;
    private final boolean requireNonZero;
    private final String varName;
    private final String onMaxSkill;
    private final String onDetonateSkill;

    public MarkStackMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.markId = config.getString(new String[]{"id", "mark", "m"}, "mark");
        Mode parsed = Mode.ADD;
        try {
            parsed = Mode.valueOf(config.getString(new String[]{"mode", "op"}, "ADD").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        this.mode = parsed;
        this.amount = config.getInteger(new String[]{"amount", "a"}, 1);
        this.maxStacks = config.getInteger(new String[]{"max", "mx"}, 0);
        this.durationTicks = config.getInteger(new String[]{"duration", "d"}, 200);
        this.resetOnMax = config.getBoolean(new String[]{"resetonmax", "rom"}, true);
        this.requireNonZero = config.getBoolean(new String[]{"requirenonzero", "rnz"}, true);
        this.varName = config.getString(new String[]{"var", "v"}, "lmmm_mark_stacks");
        this.onMaxSkill = config.getString(new String[]{"onmax", "om"}, null);
        this.onDetonateSkill = config.getString(new String[]{"ondetonate", "od"}, null);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target == null) return SkillResult.INVALID_TARGET;
        long now = System.currentTimeMillis();
        long durationMs = durationTicks > 0 ? durationTicks * 50L : -1L;

        switch (mode) {
            case ADD -> {
                MarkStackManager.AddResult res = MarkStackManager.addStacks(
                        target.getUniqueId(), markId, amount, maxStacks, durationMs, resetOnMax, now
                );
                storeVar(data, res.newStacks);
                if (res.reachedMax && onMaxSkill != null && !onMaxSkill.isEmpty()) {
                    SkillUtil.executeSkill(onMaxSkill, data, target);
                }
            }
            case TAKE -> {
                MarkStackManager.AddResult res = MarkStackManager.addStacks(
                        target.getUniqueId(), markId, -Math.abs(amount), maxStacks, durationMs, false, now
                );
                storeVar(data, res.newStacks);
            }
            case SET -> {
                MarkStackManager.setStacks(target.getUniqueId(), markId, amount, durationMs, now);
                storeVar(data, Math.max(0, amount));
            }
            case DETONATE -> {
                int stacks = MarkStackManager.detonate(target.getUniqueId(), markId, now);
                storeVar(data, stacks);
                if (!requireNonZero || stacks > 0) {
                    SkillUtil.executeSkill(onDetonateSkill, data, target);
                }
            }
            case CLEAR -> {
                MarkStackManager.clear(target.getUniqueId(), markId);
                storeVar(data, 0);
            }
        }
        return SkillResult.SUCCESS;
    }

    private void storeVar(SkillMetadata data, int value) {
        if (varName != null && !varName.isBlank() && data.getVariables() != null) {
            data.getVariables().putInt(varName, value);
        }
    }
}
