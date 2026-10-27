package net.azisaba.lifemoremythicmobs.reload;

import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Re-wraps a reloaded MythicItem with MythicCrucible (item skills, triggers, options, ammo ...).
 * Crucible is not a compile dependency, so everything goes through reflection.
 */
final class CrucibleBridge {
    private final Object manager;
    private final Map<MythicItem, Object> itemsByObj;
    private final Map<String, Object> itemsById;
    private final Map<String, Object> itemsByDisplay;
    private final ThreadLocal<Set<MythicItem>> constructing;
    private final Constructor<?> itemConstructor;
    private final Method registerItem;

    private CrucibleBridge(Object manager) throws ReflectiveOperationException {
        this.manager = manager;
        this.itemsByObj = ObjectTransplant.getField(manager, "itemsByObj");
        this.itemsById = ObjectTransplant.getField(manager, "itemsById");
        this.itemsByDisplay = ObjectTransplant.getField(manager, "itemsByDisplay");
        this.constructing = ObjectTransplant.getStatic(manager.getClass(), "CONSTRUCTING");
        Class<?> crucibleItem = Class.forName("io.lumine.mythiccrucible.items.CrucibleItem", true, manager.getClass().getClassLoader());
        this.itemConstructor = crucibleItem.getConstructor(manager.getClass(), MythicItem.class);
        this.registerItem = manager.getClass().getDeclaredMethod("registerItem", MythicItem.class);
        this.registerItem.setAccessible(true);
    }

    static @Nullable CrucibleBridge tryCreate() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicCrucible");
        if (plugin == null || !plugin.isEnabled()) return null;
        try {
            return new CrucibleBridge(plugin.getClass().getMethod("getItemManager").invoke(plugin));
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalStateException("MythicCrucible の内部構造が想定と異なります: " + e, e);
        }
    }

    /**
     * While items are rebuilt, MythicMobs generates item stacks for objects Crucible does not know yet;
     * marking the load as incomplete keeps Crucible from logging wrap failures for them.
     */
    boolean beginLoad(@NotNull ObjectTransplant tx) throws ReflectiveOperationException {
        tx.snapshot(itemsByObj);
        tx.snapshot(itemsById);
        tx.snapshot(itemsByDisplay);
        boolean wasComplete = Boolean.TRUE.equals(ObjectTransplant.getField(manager, "loadComplete"));
        manager.getClass().getMethod("markLoadIncomplete").invoke(manager);
        return wasComplete;
    }

    /** Crucible defers some lookups (ammo bullets, empty-ammo skill) to its own queue, normally run by a full load. */
    void runSecondPass() {
        List<Runnable> queue = ObjectTransplant.getField(manager, "secondPass");
        List<Runnable> run = new ArrayList<>(queue);
        queue.clear();
        run.forEach(Runnable::run);
    }

    void endLoad(boolean wasComplete) throws ReflectiveOperationException {
        if (wasComplete) manager.getClass().getMethod("markLoadComplete").invoke(manager);
    }

    /** Must be called between {@link #beginLoad} and {@link #endLoad}, which snapshot the lookup maps. */
    void refresh(@NotNull MythicItem item, @NotNull Collection<SkillMechanic> staleMechanics, @NotNull ObjectTransplant tx) throws ReflectiveOperationException {
        Object old = itemsById.get(item.getInternalName());
        if (old == null) {
            registerItem.invoke(manager, item);
            return;
        }
        Object created;
        Set<MythicItem> guard = constructing.get();
        guard.add(item); // keep Crucible's generate listener from diagnosing a half-built item
        try {
            created = itemConstructor.newInstance(manager, item);
        } finally {
            guard.remove(item);
        }
        staleMechanics.addAll(ObjectTransplant.collectMechanics(old));
        tx.copyFields(created, old, Collections.emptySet());
        tx.rebind(old, created, old, new ArrayList<>());

        itemsByObj.values().removeIf(v -> v == old);
        itemsByObj.put(item, old);
        itemsByDisplay.values().removeIf(v -> v == old);
        if (item.getMaterial() != null) {
            String name = item.getDisplayName();
            if (name != null && name.startsWith(String.valueOf(ChatColor.WHITE))) name = name.substring(2);
            itemsByDisplay.putIfAbsent(item.getMaterial().toString().toUpperCase() + "_" + name, old);
        }
    }
}
