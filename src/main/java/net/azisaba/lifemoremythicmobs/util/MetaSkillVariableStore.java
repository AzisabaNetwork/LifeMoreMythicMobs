package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractWorld;
import io.lumine.mythic.api.skills.SkillMetadata;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

public class MetaSkillVariableStore {
   private static final Map<String, String> GLOBAL = new ConcurrentHashMap<>();
   private static final Map<String, Map<String, String>> BY_WORLD = new ConcurrentHashMap<>();
   private static final Map<UUID, Map<String, String>> BY_ENTITY = new ConcurrentHashMap<>();
   private static final Map<SkillMetadata, Map<String, String>> BY_SKILL = Collections.synchronizedMap(new WeakHashMap<>());

   private MetaSkillVariableStore() {
   }

   public static MetaSkillVariableStore.Scope parseScope(String raw) {
      if (raw == null) {
         return MetaSkillVariableStore.Scope.CASTER;
      }

      String var1;
      switch ((var1 = raw.trim().toUpperCase(Locale.ROOT)).hashCode()) {
         case -1827576431:
            if (var1.equals("TARGET")) {
               return MetaSkillVariableStore.Scope.TARGET;
            }
            break;
         case 78959153:
            if (var1.equals("SKILL")) {
               return MetaSkillVariableStore.Scope.SKILL;
            }
            break;
         case 82781042:
            if (var1.equals("WORLD")) {
               return MetaSkillVariableStore.Scope.WORLD;
            }
            break;
         case 1980737580:
            if (var1.equals("CASTER")) {
               return MetaSkillVariableStore.Scope.CASTER;
            }
            break;
         case 2105276323:
            if (var1.equals("GLOBAL")) {
               return MetaSkillVariableStore.Scope.GLOBAL;
            }
      }

      return MetaSkillVariableStore.Scope.CASTER;
   }

   public static void setForScope(SkillMetadata data, MetaSkillVariableStore.Scope scope, String varName, String script, @Nullable AbstractEntity preferTarget) {
      switch (scope) {
         case GLOBAL:
            GLOBAL.put(varName, script);
            return;
         case WORLD:
            String worldKey = resolveWorldKey(data, preferTarget);
            if (worldKey != null) {
               BY_WORLD.computeIfAbsent(worldKey, k -> new ConcurrentHashMap<>()).put(varName, script);
               return;
            }
         case TARGET:
            AbstractEntity t = preferTarget != null ? preferTarget : firstEntityTarget(data);
            if (t != null) {
               BY_ENTITY.computeIfAbsent(t.getUniqueId(), k -> new ConcurrentHashMap<>()).put(varName, script);
            }

            return;
         case CASTER:
         default:
            UUID id = data.getCaster() != null ? data.getCaster().getEntity().getUniqueId() : null;
            if (id != null) {
               BY_ENTITY.computeIfAbsent(id, k -> new ConcurrentHashMap<>()).put(varName, script);
            }

            return;
         case SKILL:
            Map<String, String> m = BY_SKILL.computeIfAbsent(data, k -> new ConcurrentHashMap<>());
            m.put(varName, script);
      }
   }

   public static Optional<String> getForScope(SkillMetadata data, MetaSkillVariableStore.Scope scope, String varName, @Nullable AbstractEntity preferTarget) {
      switch (scope) {
         case GLOBAL:
            return Optional.ofNullable(GLOBAL.get(varName));
         case WORLD: {
            String worldKey = resolveWorldKey(data, preferTarget);
            if (worldKey == null) {
               return Optional.empty();
            }

            Map<String, String> m = BY_WORLD.get(worldKey);
            return Optional.ofNullable(m == null ? null : m.get(varName));
         }
         case CASTER:
         default: {
            UUID id = data.getCaster() != null ? data.getCaster().getEntity().getUniqueId() : null;
            if (id == null) {
               return Optional.empty();
            }

            Map<String, String> m = BY_ENTITY.get(id);
            return Optional.ofNullable(m == null ? null : m.get(varName));
         }
         case TARGET: {
            AbstractEntity t = resolveTarget(data, preferTarget);
            if (t == null) {
               return Optional.empty();
            }

            Map<String, String> m = BY_ENTITY.get(t.getUniqueId());
            return Optional.ofNullable(m == null ? null : m.get(varName));
         }
         case SKILL: {
            Map<String, String> m = BY_SKILL.get(data);
            return Optional.ofNullable(m == null ? null : m.get(varName));
         }
      }
   }

   private static void putForEntity(UUID uuid, String varName, String script) {
      BY_ENTITY.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(varName, script);
   }

   private static String getFromEntity(UUID uuid, String varName) {
      Map<String, String> m = BY_ENTITY.get(uuid);
      return m == null ? null : m.get(varName);
   }

   @Nullable
   private static String resolveWorldKey(SkillMetadata data, @Nullable AbstractEntity entityCandidate) {
      try {
         if (data != null && data.getCaster() != null && data.getCaster().getEntity() != null) {
            AbstractWorld w = data.getCaster().getEntity().getWorld();
            if (w != null) {
               return w.getName();
            }
         }

         if (entityCandidate != null && entityCandidate.getWorld() != null) {
            return entityCandidate.getWorld().getName();
         }

         AbstractEntity t = firstEntityTarget(data);
         if (t != null && t.getWorld() != null) {
            return t.getWorld().getName();
         }
      } catch (Throwable var3) {
      }

      return null;
   }

   @Nullable
   private static AbstractEntity resolveTarget(SkillMetadata data, @Nullable AbstractEntity preferTarget) {
      return preferTarget != null ? preferTarget : firstEntityTarget(data);
   }

   @Nullable
   private static AbstractEntity firstEntityTarget(SkillMetadata data) {
      if (data == null) {
         return null;
      }

      Collection<AbstractEntity> targets = data.getEntityTargets();
      return targets != null && !targets.isEmpty() ? targets.iterator().next() : null;
   }

   public enum Scope {
      GLOBAL,
      WORLD,
      CASTER,
      TARGET,
      SKILL;
   }
}
