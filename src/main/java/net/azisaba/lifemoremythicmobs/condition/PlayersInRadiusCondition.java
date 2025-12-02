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
        switch (operator) {
            case OP_GREATER_THAN:
                return actual > required;
            case OP_LESS_THAN:
                return actual < required;
            case OP_GREATER_THAN_EQUAL:
                return actual >= required;
            case OP_LESS_THAN_EQUAL:
                return actual <= required;
            case OP_NOT_EQUAL:
                return actual != required;
            case OP_EQUAL:
            default:
                return actual == required;
        }
    }
}