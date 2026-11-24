package net.azisaba.lifemoremythicmobs.freefield;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Mechanics taken over from FFCore (same names and options). */
public final class FFMechanics {
    private FFMechanics() {}

    /** {@code addTeam{a=400000;d=item1,item2}}: marks the mob for the battle score drops. */
    public static final class AddTeam extends SkillMechanic implements ITargetedEntitySkill {
        private final String dropPer;
        private final String drop;

        public AddTeam(SkillExecutor executor, MythicLineConfig config) {
            super(executor, null, config.getLine(), config);
            this.dropPer = config.getString(new String[]{"a", "v", "amount", "value"}, "400000");
            this.drop = config.getString(new String[]{"d", "drop", "t", "type"}, "ffooban");
        }

        @Override
        public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
            Entity entity = target.getBukkitEntity();
            PersistentDataContainer pdc = entity.getPersistentDataContainer();
            pdc.set(FFKeys.TEAM, PersistentDataType.STRING, entity.getUniqueId().toString().substring(0, 10));
            pdc.set(FFKeys.DROP_PER, PersistentDataType.STRING, dropPer);
            pdc.set(FFKeys.DROP, PersistentDataType.STRING, drop);
            return SkillResult.SUCCESS;
        }
    }

    /** {@code noHeal{l=2}}: when this zombie hits a player, the player's heals are divided by l for 5 seconds. */
    public static final class NoHeal extends SkillMechanic implements ITargetedEntitySkill {
        private final int level;

        public NoHeal(SkillExecutor executor, MythicLineConfig config) {
            super(executor, null, config.getLine(), config);
            this.level = config.getInteger(new String[]{"l", "level"}, 0);
        }

        @Override
        public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
            target.getBukkitEntity().getPersistentDataContainer().set(FFKeys.NO_HEAL, PersistentDataType.STRING, String.valueOf(level));
            return SkillResult.SUCCESS;
        }
    }

    /**
     * {@code weakSign}: the 5th hit marks the target for 10 seconds. A marked mob glows blue and takes 1.2x damage
     * from players.
     */
    public static final class WeakSign extends SkillMechanic implements ITargetedEntitySkill {
        static final Set<UUID> MARKED = ConcurrentHashMap.newKeySet();

        public WeakSign(SkillExecutor executor, MythicLineConfig config) {
            super(executor, null, config.getLine(), config);
        }

        @Override
        public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
            Entity entity = target.getBukkitEntity();
            if (MARKED.contains(entity.getUniqueId())) return SkillResult.SUCCESS;
            PersistentDataContainer pdc = entity.getPersistentDataContainer();
            int count = pdc.getOrDefault(FFKeys.WEAK_SIGN, PersistentDataType.INTEGER, 0) + 1;
            if (count < 5) {
                pdc.set(FFKeys.WEAK_SIGN, PersistentDataType.INTEGER, count);
                return SkillResult.SUCCESS;
            }
            pdc.remove(FFKeys.WEAK_SIGN);
            MARKED.add(entity.getUniqueId());
            LifeMoreMythicMobs plugin = LifeMoreMythicMobs.inst();
            if (!entity.isGlowing()) {
                entity.setGlowing(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> entity.setGlowing(false), 200L);
            }
            Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = main.getTeam("blueTeam");
            if (team == null) team = main.registerNewTeam("blueTeam");
            team.color(NamedTextColor.BLUE);
            String entry = entity.getUniqueId().toString();
            team.addEntry(entry);
            Team finalTeam = team;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                MARKED.remove(entity.getUniqueId());
                try {
                    finalTeam.removeEntry(entry);
                    finalTeam.unregister();
                } catch (IllegalStateException ignored) {
                    // already unregistered by another mark
                }
            }, 200L);
            return SkillResult.SUCCESS;
        }
    }
}
