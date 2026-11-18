package net.azisaba.lifemoremythicmobs.util;

/**
 * Pure zone evaluation and ActionBar rendering for SkillCheck QTE.
 */
public final class SkillCheckMath {
    private SkillCheckMath() {
    }

    public enum CheckResult {
        GREAT, GOOD, FAIL
    }

    public static CheckResult evaluateZone(double pos, double center, double greatRadius, double goodRadius) {
        double dist = Math.abs(pos - center);
        if (dist <= greatRadius) return CheckResult.GREAT;
        if (dist <= goodRadius) return CheckResult.GOOD;
        return CheckResult.FAIL;
    }

    public static String renderBar(String label, double pos, double center, double greatRadius, double goodRadius, int slots) {
        int count = Math.max(10, slots);
        int cursorIdx = Math.max(0, Math.min(count - 1, (int) Math.round(pos * (count - 1))));
        StringBuilder sb = new StringBuilder();
        if (label != null && !label.isEmpty()) {
            sb.append(label).append(" ");
        }
        sb.append("&8[");
        for (int i = 0; i < count; i++) {
            double slotPos = (double) i / (count - 1);
            if (i == cursorIdx) {
                sb.append("&f&l◆");
            } else {
                CheckResult zone = evaluateZone(slotPos, center, greatRadius, goodRadius);
                if (zone == CheckResult.GREAT) {
                    sb.append("&6=");
                } else if (zone == CheckResult.GOOD) {
                    sb.append("&a-");
                } else {
                    sb.append("&7-");
                }
            }
        }
        sb.append("&8]");
        return sb.toString();
    }
}
