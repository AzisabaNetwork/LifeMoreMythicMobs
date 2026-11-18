package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.GeometryUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PvEMechanicMathTest {
    @Test
    void pointToSegmentDistanceCalculatesPerpendicularAndEndpoints() {
        assertEquals(2.0, GeometryUtil.pointToSegmentDistance(5, 2, 0, 0, 0, 0, 10, 0, 0), 1.0e-6);
        assertEquals(5.0, GeometryUtil.pointToSegmentDistance(-3, 4, 0, 0, 0, 0, 10, 0, 0), 1.0e-6);
        assertEquals(3.0, GeometryUtil.pointToSegmentDistance(13, 0, 0, 0, 0, 0, 10, 0, 0), 1.0e-6);
    }

    @Test
    void towerZoneSatisfiedChecksMinAndOptionalMax() {
        assertFalse(GeometryUtil.isCountInRange(1, 2, -1));
        assertTrue(GeometryUtil.isCountInRange(2, 2, -1));
        assertTrue(GeometryUtil.isCountInRange(4, 2, -1));
        assertTrue(GeometryUtil.isCountInRange(1, 1, 1));
        assertFalse(GeometryUtil.isCountInRange(2, 1, 1));
    }

    @Test
    void resourceNodeBarShowsFilledAndEmptySegments() {
        String bar = GeometryUtil.buildProgressBar(3, 5);
        assertTrue(bar.startsWith("&a"));
        assertTrue(bar.contains("&8"));
        assertEquals(5, bar.replace("&a", "").replace("&8", "").length());
    }
}
