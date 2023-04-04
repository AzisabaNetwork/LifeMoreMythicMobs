package lifemoremythicmobs.org.example.lifemoremythicmobs.Mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class ModifyBossBarMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final double progress;
    protected final String id;
    protected final String color;
    protected final String style;
    protected final String title;

    public ModifyBossBarMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.progress = config.getDouble(new String[]{"progress", "pro", "p"}, -1);
        this.id = config.getString(new String[]{"id", "i"}, "def");
        this.color = config.getString(new String[]{"id", "i"}).toUpperCase();
        this.style = config.getString(new String[]{"style", "s"}).toUpperCase();
        this.title = config.getString(new String[]{"title", "t"});

    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {

        BossBar bar = BossBarMechanic.bars.get(id);

        if ( progress != -1 ) {
            bar.setProgress(progress);
        }
        if ( color != null ) {
            bar.setColor(BarColor.valueOf(color));
        }
        if ( style != null ) {
            bar.setStyle(BarStyle.valueOf(style));
        }
        if ( title != null ) {
            bar.setTitle(title);
        }


        return true;
    }
}
