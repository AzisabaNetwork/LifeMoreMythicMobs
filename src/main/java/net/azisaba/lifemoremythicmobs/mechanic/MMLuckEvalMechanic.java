package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class MMLuckEvalMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String to;
    protected final PlaceholderString source;

    public MMLuckEvalMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.to = config.getString(new String[] {"to", "var", "t", "v"});
        this.source = config.getPlaceholderString(new String[] {"script", "source", "src", "s"}, "");
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (!(abstractEntity.getBukkitEntity() instanceof Player)) {
            return false;
        }
        try {
            String script = source.get(skillMetadata, abstractEntity);
            LifeMoreMythicMobs.getPlugin(LifeMoreMythicMobs.class).getSLF4JLogger().info("Evaluating script: " + script);
            Method method = Class.forName("com.github.mori01231.mmluck.utils.Expr")
                    .getMethod("eval", Player.class, String.class);
            //noinspection JavaReflectionInvocation
            Object value = method.invoke(null, abstractEntity.getBukkitEntity(), script);
            ItemUtil.setVariable(skillMetadata, to, String.valueOf(value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
