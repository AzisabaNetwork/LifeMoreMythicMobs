package net.azisaba.lifemoremythicmobs.freefield;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Taken over from FFCore.
 * <ul>
 *   <li>Battle score: on a mob marked by {@code addTeam}, each player's damage is counted on a sidebar. When it
 *       dies, every player with a sidebar gets {@code mlg <player> <item> 1 <score / drop_per>} for each drop.</li>
 *   <li>Raid: killing an FFGGR / rapid mob adds 1 (2 on the FFGGR server) to ffggr_under; at 3000 the boss
 *       FFGGR_Under_Boss_1_Spawn appears in free_field.</li>
 * </ul>
 */
public final class BattleScoreListener implements Listener {
    private static final Map<String, Objective> TEAMS = new HashMap<>();

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        event.getPlayer().getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
    }

    @EventHandler
    public void onDamaged(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        String team = event.getEntity().getPersistentDataContainer().get(FFKeys.TEAM, PersistentDataType.STRING);
        if (team == null) return;
        Objective objective = TEAMS.computeIfAbsent(team, id -> {
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Component title = LegacyComponentSerializer.legacyAmpersand().deserialize("&4&l戦闘スコアポイント");
            return board.registerNewObjective(id, Criteria.DUMMY, title);
        });
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        Scoreboard board = objective.getScoreboard();
        if (board == null) return;
        Score score = objective.getScore(player.getName());
        score.setScore((int) (score.getScore() + event.getFinalDamage()));
        player.setScoreboard(board);
    }

    @EventHandler
    public void onDeath(@NotNull MythicMobDeathEvent event) {
        // Same order as FFCore: drops first, so the raid sidebar given to the killer is not read as a battle score.
        giveBattleDrops(event);
        countRaid(event);
    }

    private static void giveBattleDrops(MythicMobDeathEvent event) {
        Entity entity = event.getEntity();
        String team = entity.getPersistentDataContainer().get(FFKeys.TEAM, PersistentDataType.STRING);
        if (team == null) return;
        double dropPer = dropPer(entity);
        String raw = entity.getPersistentDataContainer().get(FFKeys.DROP, PersistentDataType.STRING);
        List<String> drops = raw == null ? List.of("ffooban") : raw.isEmpty() ? List.of() : List.of(raw.split(","));
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objective objective = player.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
            if (objective == null) continue;
            Score score = objective.getScore(player.getName());
            for (String drop : drops) {
                double loot = score.getScore() / dropPer;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mlg " + player.getName() + " " + drop + " 1 " + loot);
            }
            player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        }
        TEAMS.remove(team);
    }

    @EventHandler
    public void onDespawn(@NotNull MythicMobDespawnEvent event) {
        String team = event.getEntity().getPersistentDataContainer().get(FFKeys.TEAM, PersistentDataType.STRING);
        if (team == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        TEAMS.remove(team);
    }

    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        event.getEntity().getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
    }

    private static double dropPer(Entity entity) {
        String raw = entity.getPersistentDataContainer().get(FFKeys.DROP_PER, PersistentDataType.STRING);
        if (raw != null) {
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException ignored) {
            }
        }
        return 1.0e10;
    }

    private static void countRaid(MythicMobDeathEvent event) {
        String name = event.getMobType().getInternalName().toLowerCase();
        if (!name.contains("ffggr") && !name.contains("rapid")) return;
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = main.getObjective("ffggr_under");
        if (objective == null) {
            objective = main.registerNewObjective("ffggr_under", Criteria.create("count"), Component.text("蓄積値"), RenderType.INTEGER);
        }
        Score score = objective.getScore("レイド開催値は3000です");
        int count = (score.isScoreSet() ? score.getScore() : 0) + 1;
        if (isFFGGRServer(main)) count++;
        score.setScore(count);
        if (event.getKiller() instanceof Player player) {
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            player.setScoreboard(main);
        }
        if (count >= 3000) {
            score.setScore(0);
            Bukkit.broadcast(Component.text("FFGGR拠点の門の中から、異音がする…！"));
            World world = Bukkit.getWorld("free_field");
            if (world != null) {
                MythicBukkit.inst().getMobManager().spawnMob("FFGGR_Under_Boss_1_Spawn", new Location(world, -318.0, 12.0, 2668.0));
            }
        }
    }

    private static boolean isFFGGRServer(Scoreboard main) {
        Objective objective = main.getObjective("ffggr");
        if (objective == null) return false;
        Score score = objective.getScore("__GLOBAL__");
        return score.isScoreSet() && score.getScore() == 1;
    }
}
