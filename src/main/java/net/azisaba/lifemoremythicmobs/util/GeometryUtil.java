package net.azisaba.lifemoremythicmobs.util;

/**
 * Pure geometry and range-check helpers shared by PvE mechanics.
 */
public final class GeometryUtil {
    private GeometryUtil() {
    }

    public static double pointToSegmentDistance(double px, double py, double pz,
                                                double ax, double ay, double az,
                                                double bx, double by, double bz) {
        double abx = bx - ax;
        double aby = by - ay;
        double abz = bz - az;
        double lenSq = abx * abx + aby * aby + abz * abz;
        if (lenSq < 1.0e-9) {
            double dx = px - ax;
            double dy = py - ay;
            double dz = pz - az;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        double t = ((px - ax) * abx + (py - ay) * aby + (pz - az) * abz) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));
        double cx = ax + abx * t;
        double cy = ay + aby * t;
        double cz = az + abz * t;
        double dx = px - cx;
        double dy = py - cy;
        double dz = pz - cz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static boolean isCountInRange(int count, int requiredMin, int optionalMax) {
        if (count < requiredMin) return false;
        return optionalMax < 0 || count <= optionalMax;
    }

    public static String buildProgressBar(int current, int max) {
        int segments = Math.min(10, Math.max(1, max));
        int filled = (int) Math.round((double) Math.max(0, current) / Math.max(1, max) * segments);
        StringBuilder sb = new StringBuilder("&a");
        for (int i = 0; i < segments; i++) {
            if (i == filled) sb.append("&8");
            sb.append("■");
        }
        return sb.toString();
    }
}
