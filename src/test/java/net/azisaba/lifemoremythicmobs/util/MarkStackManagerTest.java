package net.azisaba.lifemoremythicmobs.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MarkStackManagerTest {
    @AfterEach
    void cleanup() {
        MarkStackManager.clearAll();
    }

    @Test
    void addStacksRespectsMaxAndResetOnMax() {
        UUID id = UUID.randomUUID();
        var r1 = MarkStackManager.addStacks(id, "burn", 2, 5, 5000L, true, 1000L);
        assertEquals(2, r1.newStacks);
        assertFalse(r1.reachedMax);

        var r2 = MarkStackManager.addStacks(id, "burn", 3, 5, 5000L, true, 2000L);
        assertEquals(0, r2.newStacks);
        assertTrue(r2.reachedMax);
        assertEquals(0, MarkStackManager.getStacks(id, "burn", 2000L));
    }

    @Test
    void expiresAfterDuration() {
        UUID id = UUID.randomUUID();
        MarkStackManager.addStacks(id, "frost", 2, 10, 1000L, false, 1000L);
        assertEquals(2, MarkStackManager.getStacks(id, "frost", 1500L));
        assertEquals(0, MarkStackManager.getStacks(id, "frost", 2500L));
    }

    @Test
    void detonateReturnsCurrentAndClears() {
        UUID id = UUID.randomUUID();
        MarkStackManager.setStacks(id, "shock", 4, 5000L, 1000L);
        assertEquals(4, MarkStackManager.detonate(id, "shock", 2000L));
        assertEquals(0, MarkStackManager.getStacks(id, "shock", 2000L));
    }
}
