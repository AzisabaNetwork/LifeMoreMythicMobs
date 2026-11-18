package net.azisaba.lifemoremythicmobs.condition;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Shared matching helpers for comma-separated OR condition values.
 */
final class ConditionMatchUtil {
    private ConditionMatchUtil() {
    }

    static List<String> parseCommaSeparated(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    static boolean anyStartsWith(String target, List<String> prefixes, boolean ignoreCase) {
        if (target == null || target.isEmpty() || prefixes.isEmpty()) {
            return false;
        }
        String checkTarget = ignoreCase ? target.toLowerCase(Locale.ROOT) : target;
        for (String prefix : prefixes) {
            String checkPrefix = ignoreCase ? prefix.toLowerCase(Locale.ROOT) : prefix;
            if (checkTarget.startsWith(checkPrefix)) {
                return true;
            }
        }
        return false;
    }

    static boolean anyContains(String target, List<String> substrings, boolean ignoreCase) {
        if (target == null || target.isEmpty() || substrings.isEmpty()) {
            return false;
        }
        String checkTarget = ignoreCase ? target.toLowerCase(Locale.ROOT) : target;
        for (String sub : substrings) {
            String checkSub = ignoreCase ? sub.toLowerCase(Locale.ROOT) : sub;
            if (checkTarget.contains(checkSub)) {
                return true;
            }
        }
        return false;
    }

    static boolean anyEquals(String target, List<String> candidates, boolean ignoreCase) {
        if (target == null || candidates.isEmpty()) {
            return false;
        }
        for (String candidate : candidates) {
            if (ignoreCase ? target.equalsIgnoreCase(candidate) : target.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    static boolean isSpearMaterialName(String materialName, boolean includeTrident) {
        if (materialName == null) {
            return false;
        }
        if (materialName.endsWith("_SPEAR")) {
            return true;
        }
        return includeTrident && "TRIDENT".equals(materialName);
    }
}
