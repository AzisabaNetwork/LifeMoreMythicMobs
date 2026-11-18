package net.azisaba.lifemoremythicmobs.util;

/**
 * Evaluates MythicMobs-style numeric comparison expressions such as
 * "5", ">=5", "<=10", ">3", "<8", "==4", "!=0", or range "3to7" / "3-7".
 */
public final class NumericComparison {
    private NumericComparison() {
    }

    public static boolean matches(double actual, String expr) {
        if (expr == null || expr.isBlank()) {
            return false;
        }
        String s = expr.trim();
        try {
            if (s.startsWith(">=")) {
                return actual >= Double.parseDouble(s.substring(2).trim());
            }
            if (s.startsWith("<=")) {
                return actual <= Double.parseDouble(s.substring(2).trim());
            }
            if (s.startsWith("!=")) {
                return Double.compare(actual, Double.parseDouble(s.substring(2).trim())) != 0;
            }
            if (s.startsWith("==")) {
                return Double.compare(actual, Double.parseDouble(s.substring(2).trim())) == 0;
            }
            if (s.startsWith(">")) {
                return actual > Double.parseDouble(s.substring(1).trim());
            }
            if (s.startsWith("<")) {
                return actual < Double.parseDouble(s.substring(1).trim());
            }
            if (s.startsWith("=")) {
                return Double.compare(actual, Double.parseDouble(s.substring(1).trim())) == 0;
            }
            int toIdx = s.indexOf("to");
            if (toIdx > 0) {
                double min = Double.parseDouble(s.substring(0, toIdx).trim());
                double max = Double.parseDouble(s.substring(toIdx + 2).trim());
                return actual >= Math.min(min, max) && actual <= Math.max(min, max);
            }
            int dashIdx = s.indexOf('-', 1);
            if (dashIdx > 0) {
                double min = Double.parseDouble(s.substring(0, dashIdx).trim());
                double max = Double.parseDouble(s.substring(dashIdx + 1).trim());
                return actual >= Math.min(min, max) && actual <= Math.max(min, max);
            }
            return Double.compare(actual, Double.parseDouble(s)) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
