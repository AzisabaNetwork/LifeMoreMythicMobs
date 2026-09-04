package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
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
        Optional<Skill> maybeSkill = MythicBukkit.inst().getSkillManager().getSkill(skillName);
        maybeSkill.ifPresent(skill -> {
            SkillMetadata clone = data.deepClone();
            setMeta(clone, target);
            skill.execute(clone);
        });
    }

    /**
     * SkillMetadata に対して、target を基準とした Caster, Trigger, EntityTarget を設定する。
     * target が MythicMob であれば setCaster も設定する。
     */
    public static void setMeta(SkillMetadata meta, AbstractEntity target) {
        Entity bukkitEntity = BukkitAdapter.adapt(target);
        ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(bukkitEntity);
        if (activeMob != null) {
            meta.setCaster(activeMob);
        }
        meta.setTrigger(target);
        meta.setEntityTarget(target);
    }
}
