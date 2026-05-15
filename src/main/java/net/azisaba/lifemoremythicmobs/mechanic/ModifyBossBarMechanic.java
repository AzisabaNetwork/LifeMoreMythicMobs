package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class ModifyBossBarMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final double progress;
    protected final String id;
    protected final String color;
    protected final String style;
    protected final String title;

    public ModifyBossBarMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);

        this.progress = config.getDouble(new String[]{"progress", "pro", "p"}, -1);
        this.id = config.getString(new String[]{"id", "i"}, "def");
        this.color = config.getString(new String[]{"color", "c"});
        if ( config.getString(new String[]{"style", "s"}) != null ) {
            this.style = config.getString(new String[]{"style", "s"}).toUpperCase();
        } else {
            this.style = null;
        }
        this.title = config.getString(new String[]{"title", "t"});

    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {

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

        return SkillResult.SUCCESS;
    }
}
