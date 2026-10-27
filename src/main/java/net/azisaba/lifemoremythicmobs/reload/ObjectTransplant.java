package net.azisaba.lifemoremythicmobs.reload;

import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.packs.Pack;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillHolder;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Moves the state of a freshly constructed MythicMobs object into the instance that is already loaded.
 * <p>
 * MythicMobs and MythicCrucible keep direct references to loaded skills, mob types and items
 * (skill{s=...}, summon, drops, auras, active mobs, Crucible wrappers ...). Replacing the registry entry
 * would leave all of those pointing at the stale object, so instead the new state is copied into the old
 * instance and every back-reference to the new instance is redirected to the old one.
 * <p>
 * Every write is recorded so that a failed reload can be rolled back with {@link #undo()}.
 */
final class ObjectTransplant {
    private static final int MAX_DEPTH = 5;
    private static final int MAX_VISITS = 200_000;
    /** Shared registries (thousands of entries) never hold the mechanics of a single skill, mob or item. */
    private static final int MAX_COLLECTION = 2_000;
    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private final Deque<Runnable> undo = new ArrayDeque<>();

    /** Copies every instance field (including inherited ones) from {@code from} into {@code to}. */
    void copyFields(@NotNull Object from, @NotNull Object to, @NotNull Set<String> skip) {
        if (from.getClass() != to.getClass()) {
            throw new IllegalArgumentException("class mismatch: " + from.getClass() + " / " + to.getClass());
        }
        for (Field field : fields(from.getClass())) {
            if (skip.contains(field.getName())) continue;
            try {
                set(field, to, field.get(from));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("cannot copy " + field, e);
            }
        }
    }

    /**
     * Walks the object graph owned by {@code root} and replaces references to {@code from} with {@code to}.
     * Mechanics whose parent is {@code from} are re-parented. Every mechanic seen is added to {@code mechanics}.
     */
    void rebind(@NotNull Object root, @Nullable Object from, @Nullable Object to, @NotNull Collection<SkillMechanic> mechanics) {
        new Walker(root, from, to, mechanics).scanFields(root, 0);
    }

    /** Records how to restore a map that is about to be modified. */
    <K, V> void snapshot(@NotNull Map<K, V> map) {
        Map<K, V> copy = new java.util.HashMap<>(map);
        undo.push(() -> {
            map.clear();
            map.putAll(copy);
        });
    }

    /** Records how to restore a collection that is about to be modified. */
    <E> void snapshot(@NotNull Collection<E> collection) {
        List<E> copy = new ArrayList<>(collection);
        undo.push(() -> {
            collection.clear();
            collection.addAll(copy);
        });
    }

    /** Reverts every recorded change, newest first. */
    void undo() {
        while (!undo.isEmpty()) {
            try {
                undo.pop().run();
            } catch (RuntimeException ignored) {
            }
        }
    }

    private void set(Field field, Object owner, Object value) throws IllegalAccessException {
        Object previous = field.get(owner);
        field.set(owner, value);
        undo.push(() -> {
            try {
                field.set(owner, previous);
            } catch (IllegalAccessException ignored) {
            }
        });
    }

    private void setParent(SkillMechanic mechanic, SkillHolder parent, Optional<SkillHolder> previous) {
        mechanic.setParent(parent);
        undo.push(() -> mechanic.setParent(previous.orElse(null)));
    }

    /** Collects mechanics reachable from {@code root} without changing anything. */
    static @NotNull List<SkillMechanic> collectMechanics(@NotNull Object root) {
        List<SkillMechanic> list = new ArrayList<>();
        new ObjectTransplant().rebind(root, null, null, list);
        return list;
    }

    static @NotNull List<Field> fields(@NotNull Class<?> type) {
        return FIELD_CACHE.computeIfAbsent(type, t -> {
            List<Field> list = new ArrayList<>();
            for (Class<?> c = t; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    try {
                        field.setAccessible(true);
                    } catch (RuntimeException e) {
                        continue; // not opened to us (JDK internals)
                    }
                    list.add(field);
                }
            }
            return Collections.unmodifiableList(list);
        });
    }

    @SuppressWarnings("unchecked")
    static <T> T getField(@NotNull Object owner, @NotNull String name) {
        for (Field field : fields(owner.getClass())) {
            if (field.getName().equals(name)) {
                try {
                    return (T) field.get(owner);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        throw new IllegalStateException("field '" + name + "' not found in " + owner.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    static <T> T getStatic(@NotNull Class<?> owner, @NotNull String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("static field '" + name + "' not found in " + owner.getName(), e);
        }
    }

    private final class Walker {
        private final Object root;
        private final Object from;
        private final Object to;
        private final Collection<SkillMechanic> mechanics;
        private final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        private Walker(Object root, Object from, Object to, Collection<SkillMechanic> mechanics) {
            this.root = root;
            this.from = from;
            this.to = to;
            this.mechanics = mechanics;
        }

        private void scanFields(Object owner, int depth) {
            if (!visited.add(owner) || visited.size() > MAX_VISITS) return;
            for (Field field : fields(owner.getClass())) {
                // A skill's parents are its callers, which belong to other holders
                if (field.getName().equals("parents")) continue;
                Object value;
                try {
                    value = field.get(owner);
                } catch (IllegalAccessException e) {
                    continue;
                }
                if (value == null) continue;
                if (from != null && value == from) {
                    try {
                        set(field, owner, to);
                    } catch (IllegalAccessException ignored) {
                    }
                    continue;
                }
                visit(value, depth + 1);
            }
        }

        private void visit(Object value, int depth) {
            if (value == null || depth > MAX_DEPTH) return;
            if (value instanceof SkillMechanic mechanic) {
                mechanics.add(mechanic);
                if (from != null && to instanceof SkillHolder holder) {
                    Optional<SkillHolder> parent = mechanic.getParent();
                    if (parent != null && parent.isPresent() && parent.get() == from) setParent(mechanic, holder, parent);
                }
                return;
            }
            if (value instanceof Optional<?> optional) {
                optional.ifPresent(v -> visit(v, depth));
                return;
            }
            if (value instanceof Map<?, ?> map) {
                if (map.size() > MAX_COLLECTION || !visited.add(value)) return;
                for (Object v : map.values().toArray()) visit(v, depth + 1);
                return;
            }
            if (value instanceof Collection<?> collection) {
                if (collection.size() > MAX_COLLECTION || !visited.add(value)) return;
                for (Object v : collection.toArray()) visit(v, depth + 1);
                return;
            }
            if (value.getClass().isArray()) {
                if (value.getClass().getComponentType().isPrimitive() || !visited.add(value)) return;
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) visit(Array.get(value, i), depth + 1);
                return;
            }
            if (isBoundary(value)) return;
            scanFields(value, depth);
        }

        /** Objects owned by something else (other skills, mobs, items, plugins, managers) are never entered. */
        private boolean isBoundary(Object value) {
            if (value == root) return true;
            if (value instanceof Enum<?> || value instanceof Skill || value instanceof MythicMob || value instanceof MythicItem
                    || value instanceof ActiveMob || value instanceof Plugin
                    || value instanceof Pack || value instanceof MythicConfig) return true;
            String name = value.getClass().getName();
            if (!(name.startsWith("io.lumine.") || name.startsWith("net.azisaba."))) return true;
            if (name.equals("io.lumine.mythiccrucible.items.CrucibleItem")) return true;
            String simple = value.getClass().getSimpleName();
            return simple.endsWith("Executor") || simple.endsWith("Manager") || simple.endsWith("Clock");
        }
    }
}
