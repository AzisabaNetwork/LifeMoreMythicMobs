package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.ILocationCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillCondition;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayersInRadiusCondition extends SkillCondition implements ILocationCondition {

    private final int requiredAmount;
    private final double distance;
    private final boolean ignoreSpectator;
    private final String comparisonOperator;

    private static final String OP_GREATER_THAN = ">";
    private static final String OP_LESS_THAN = "<";
    private static final String OP_GREATER_THAN_EQUAL = ">=";
    private static final String OP_LESS_THAN_EQUAL = "<=";
    private static final String OP_EQUAL = "=";
    private static final String OP_NOT_EQUAL = "!=";

    public PlayersInRadiusCondition(@NotNull MythicLineConfig mlc) {
        super(mlc.getLine());
        this.distance = mlc.getPlaceholderDouble(new String[]{"radius", "r", "distance", "d"}, "32").get();
        this.ignoreSpectator = mlc.getBoolean(new String[]{"ignoreSpectator", "is"}, true);
        String amountString = mlc.getString(new String[]{"amount", "a"}, "1");

        AmountSetting parsedSetting = parseAmountSetting(amountString);
        this.comparisonOperator = parsedSetting.operator;
        this.requiredAmount = parsedSetting.amount;
    }
    private AmountSetting parseAmountSetting(String amountString) {
        String operator = OP_EQUAL;
        int amount = 1;
        String numberPart = amountString;

        if (amountString.startsWith(OP_GREATER_THAN_EQUAL)) {
            operator = OP_GREATER_THAN_EQUAL;
            numberPart = amountString.substring(2);
        } else if (amountString.startsWith(OP_LESS_THAN_EQUAL)) {
            operator = OP_LESS_THAN_EQUAL;
            numberPart = amountString.substring(2);
        } else if (amountString.startsWith(OP_NOT_EQUAL)) {
            operator = OP_NOT_EQUAL;
            numberPart = amountString.substring(2);
        } else if (amountString.startsWith(OP_GREATER_THAN)) {
            operator = OP_GREATER_THAN;
            numberPart = amountString.substring(1);
        } else if (amountString.startsWith(OP_LESS_THAN)) {
            operator = OP_LESS_THAN;
            numberPart = amountString.substring(1);
        } else if (amountString.startsWith(OP_EQUAL)) {
            numberPart = amountString.substring(1);
        }

        try {
            amount = Integer.parseInt(numberPart.trim());
        } catch (NumberFormatException ignored) {
        }

        return new AmountSetting(operator, amount);
    }

    private static class AmountSetting {
        final String operator;
        final int amount;

        AmountSetting(String operator, int amount) {
            this.operator = operator;
            this.amount = amount;
        }
    }

    @Override
    public boolean check(AbstractLocation abstractLocation) {
        Location loc = BukkitAdapter.adapt(abstractLocation);
        int playersFound = 0;

        for (Player p : loc.getWorld().getPlayers()) {
            if (p == null) continue;

            if (ignoreSpectator && p.getGameMode().equals(GameMode.SPECTATOR)) continue;

            Location pLOC = p.getLocation();
            if (loc.distance(pLOC) <= distance) {
                playersFound++;
            }
        }

        return compareAmount(playersFound, requiredAmount, comparisonOperator);
    }

    private boolean compareAmount(int actual, int required, String operator) {
        return switch (operator) {
            case OP_GREATER_THAN -> actual > required;
            case OP_LESS_THAN -> actual < required;
            case OP_GREATER_THAN_EQUAL -> actual >= required;
            case OP_LESS_THAN_EQUAL -> actual <= required;
            case OP_NOT_EQUAL -> actual != required;
            default -> actual == required;
        };
    }
}