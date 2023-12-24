package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.entity.Player;

public class RemoveBossBarMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String id;

    public RemoveBossBarMechanic(MythicLineConfig config) {

        super(config.getLine(), config);

        this.id = config.getString(new String[]{"id", "i"}, "def");

    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        Player bukkitTarget = (Player) BukkitAdapter.adapt(target);

        BossBarMechanic.bars.get(id).removePlayer(bukkitTarget);

        return true;
    }
}
