package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Optional;
import java.util.UUID;

public class SkillUtil {

    public static Optional<Skill> resolveSkill(String name) {
        if (name == null || name.isEmpty()) return Optional.empty();
        return MythicBukkit.inst().getSkillManager().getSkill(name);
    }

    public static void executeSkill(String skillName, SkillMetadata data, AbstractEntity trigger) {
        if (skillName == null || skillName.isEmpty()) return;
        resolveSkill(skillName).ifPresent(skill -> {
            SkillMetadata clone = data.deepClone();
            if (trigger != null) {
                clone.setTrigger(trigger);
            }
            skill.execute(clone);
        });
    }

    public static AbstractEntity getAbstractEntity(UUID uuid) {
        for (World world : Bukkit.getWorlds()) {
            Entity e = world.getEntity(uuid);
            if (e != null) return BukkitAdapter.adapt(e);
        }
        return null;
    }
}
