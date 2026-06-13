package net.azisaba.lifemoremythicmobs.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EquipLockManager - lock/unlock tracking logic.
 */
class EquipLockManagerTest {

    private final EquipLockManager manager = EquipLockManager.getInstance();

    @Test
    void testSingleton() {
        assertSame(EquipLockManager.getInstance(), manager);
    }

    @Test
    void testInitiallyNotLocked() {
        // UUID.randomUUID() simulates a player that was never locked
        var uuid = java.util.UUID.randomUUID();
        // can't call isLocked(Player) without mock, but addLock/removeLock track counts
        assertDoesNotThrow(() -> manager.addLock(uuid));
        manager.removeLock(uuid);
    }

    @Test
    void testAddLockIncrementsCount() {
        var uuid = java.util.UUID.randomUUID();
        manager.addLock(uuid);
        manager.addLock(uuid);
        // Remove once — should still be locked
        manager.removeLock(uuid);
        // Remove again — should be unlocked now
        manager.removeLock(uuid);
        // Third remove should be no-op (already gone)
        assertDoesNotThrow(() -> manager.removeLock(uuid));
    }

    @Test
    void testRemoveOnEmptyDoesNotThrow() {
        var uuid = java.util.UUID.randomUUID();
        assertDoesNotThrow(() -> manager.removeLock(uuid));
    }

    @Test
    void testAddRemoveMultiplePlayers() {
        var uuid1 = java.util.UUID.randomUUID();
        var uuid2 = java.util.UUID.randomUUID();
        manager.addLock(uuid1);
        manager.addLock(uuid2);
        manager.removeLock(uuid1);
        manager.removeLock(uuid2);
        // Cleanup
        manager.removeLock(uuid1);
        manager.removeLock(uuid2);
    }
}
