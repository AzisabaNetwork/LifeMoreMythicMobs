package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class MMLuckEvalMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected final String to;
    protected final PlaceholderString source;

    public MMLuckEvalMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);

        this.to = config.getString(new String[] {"to", "var", "t", "v"});
        this.source = config.getPlaceholderString(new String[] {"script", "source", "src", "s"}, "");
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (!(abstractEntity.getBukkitEntity() instanceof Player)) {
            return SkillResult.CONDITION_FAILED;
        }
        try {
            String script = source.get(skillMetadata, abstractEntity);
            LifeMoreMythicMobs.getPlugin(LifeMoreMythicMobs.class).getSLF4JLogger().info("Evaluating script: " + script);
            Method method = Class.forName("com.github.mori01231.mmluck.utils.Expr")
                    .getMethod("eval", Player.class, String.class);
            Object value = method.invoke(null, abstractEntity.getBukkitEntity(), script);
            ItemUtil.setVariable(skillMetadata, to, String.valueOf(value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return SkillResult.SUCCESS;
    }
}
