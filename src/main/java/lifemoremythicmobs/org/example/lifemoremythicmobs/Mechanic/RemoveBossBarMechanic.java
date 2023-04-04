package lifemoremythicmobs.org.example.lifemoremythicmobs.Mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import lifemoremythicmobs.org.example.lifemoremythicmobs.Util.BossBarUtil;
import org.bukkit.entity.Player;

public class RemoveBossBarMechanic extends SkillMechanic implements ITargetedEntitySkill {
    public RemoveBossBarMechanic(MythicLineConfig config) {

        super(config.getLine(), config);

    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        Player bukkitTarget = (Player) BukkitAdapter.adapt(target);

        BossBarUtil.removeBossBar(bukkitTarget);

        return true;
    }
}
