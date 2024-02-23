package net.azisaba.lifemoremythicmobs.condition;


import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.ILocationCondition;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayersInRadiusCondition extends SkillCondition implements ILocationCondition {
    private final int amount;
    private final double distance;
    private final boolean ignoreSpectator;


    public PlayersInRadiusCondition(@NotNull MythicLineConfig mlc) {
        super(mlc.getLine());
        this.amount = mlc.getPlaceholderInteger(new String[]{"amount", "a"}, "1").get();
        this.distance = mlc.getPlaceholderDouble(new String[]{"radius", "r", "distance", "d"}, "32").get();
        this.ignoreSpectator = mlc.getBoolean(new String[]{"ignoreSpectator", "is"}, true);
    }

    @Override
    public boolean check(AbstractLocation abstractLocation) {
        Location loc = BukkitAdapter.adapt(abstractLocation);
        int i = 0;
        for (Player p : loc.getWorld().getPlayers()) {
            if (p == null) continue;

            if (p.getGameMode().equals(GameMode.SPECTATOR) && ignoreSpectator) continue;
            Location pLOC = p.getLocation();
            if (loc.distance(pLOC) <= distance) i++;
        }
        return i >= amount;
    }
}
