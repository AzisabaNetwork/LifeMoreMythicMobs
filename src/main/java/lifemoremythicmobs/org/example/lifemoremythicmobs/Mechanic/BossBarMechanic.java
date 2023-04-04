package lifemoremythicmobs.org.example.lifemoremythicmobs.Mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import lifemoremythicmobs.org.example.lifemoremythicmobs.Util.BossBarUtil;
import org.bukkit.entity.Player;

public class BossBarMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final boolean always;
    protected final int duration;
    protected final double progress;
    protected final String title;
    protected final String style;
    protected final String color;

    public BossBarMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.always = config.getBoolean(new String[]{"always", "a", "al"}, false);
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.progress = config.getDouble(new String[]{"progress", "pro", "p"}, 0);
        this.title = config.getString(new String[]{"title", "t"}, "BossBar");
        this.style = config.getString(new String[]{"style", "s"}, "SOLID").toUpperCase();
        this.color = config.getString(new String[]{"color", "c"}, "RED").toUpperCase();

    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        Player bukkitTarget = (Player) BukkitAdapter.adapt(target);

        BossBarUtil.bossBar(title, style, color, bukkitTarget, progress, duration, always);

        return true;
    }
}
