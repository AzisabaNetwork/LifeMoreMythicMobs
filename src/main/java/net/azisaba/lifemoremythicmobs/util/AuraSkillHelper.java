package net.azisaba.lifemoremythicmobs.util;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.skills.Skill;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.entity.Entity;

import java.util.Optional;

/**
 * オーラ系メカニクスで共通して使用するスキル実行ヘルパー。
 * @self / @trigger / @target が正しくtargetを参照するよう、
 * setCaster / setTrigger / setEntityTarget をまとめて設定する。
 */
public final class AuraSkillHelper {

    private AuraSkillHelper() {}

    /**
     * skillName で指定されたスキルを、target を基準として実行する。
     * target が MythicMob であれば setCaster も設定する。
     */
    public static void executeSkill(String skillName, SkillMetadata data, AbstractEntity target) {
        if (skillName == null || skillName.isEmpty()) return;
        Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(skillName);
        maybeSkill.ifPresent(skill -> {
            SkillMetadata clone = data.deepClone();
            Entity bukkitEntity = BukkitAdapter.adapt(target);
            ActiveMob activeMob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(bukkitEntity);
            if (activeMob != null) {
                clone.setCaster(activeMob);
            }
            clone.setTrigger(target);
            clone.setEntityTarget(target);
            skill.execute(clone);
        });
    }

    /**
     * SkillMetadata に対して、target を基準とした Caster, Trigger, EntityTarget を設定する。
     * target が MythicMob であれば setCaster も設定する。
     */
    public static void setMeta(SkillMetadata meta, AbstractEntity target) {
        Entity bukkitEntity = BukkitAdapter.adapt(target);
        ActiveMob activeMob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(bukkitEntity);
        if (activeMob != null) {
            meta.setCaster(activeMob);
        }
        meta.setTrigger(target);
        meta.setEntityTarget(target);
    }
}
