package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import net.azisaba.lifemoremythicmobs.util.MetaSkillVariableStore;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class VSkillMechanic extends SkillMechanic implements INoTargetSkill, ITargetedEntitySkill, ITargetedLocationSkill {
   private static final ConcurrentHashMap<String, Skill> INLINE_CACHE = new ConcurrentHashMap<>();
   private final PlaceholderString varNamePS;
   private final String scopeRaw;
   private final boolean stopOnFail;
   private final boolean log;
   private static Method reflectExecuteInLine;
   private static boolean reflectResolved = false;

   public VSkillMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.varNamePS = config.getPlaceholderString(new String[]{"name", "n", "variable", "var", "key", "k"}, null, new String[0]);
      this.scopeRaw = config.getString(new String[]{"scope", "s"}, null, new String[0]);
      this.stopOnFail = config.getBoolean(new String[]{"stoponfail", "stop", "halt"}, false);
      this.log = config.getBoolean(new String[]{"log"}, false);
   }

   public SkillResult cast(SkillMetadata data) {
      return this.run(data, null);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      return this.run(data, target);
   }

   public boolean castAtLocation(SkillMetadata data, AbstractLocation location) {
      return this.run(data, null);
   }

   private boolean run(SkillMetadata data, AbstractEntity preferTarget) {
      if (this.varNamePS == null) {
         return SkillResult.FAILURE;
      }

      String rawVar = this.varNamePS.get(data, data.getCaster().getEntity());
      if (rawVar != null && !rawVar.trim().isEmpty()) {
         VSkillMechanic.VarScopePair vs = resolveScopeAndVar(rawVar, this.scopeRaw);
         String varName = vs.var;
         MetaSkillVariableStore.Scope scope = vs.scope;
         Optional<String> optScript = MetaSkillVariableStore.getForScope(data, scope, varName, preferTarget);
         if (!optScript.isPresent()) {
            if (this.log) {
               IgaDebugLogger.log(this.getClass(), "script not found: scope=" + scope + " var=" + varName);
            }

            return SkillResult.FAILURE;
         } else {
            String script = optScript.get();
            String inlineBlock = "[\n" + script + "\n ]";
            Skill skill = INLINE_CACHE.computeIfAbsent(script, s -> {
               try {
                  Optional<Skill> sk = MythicBukkit.inst().getSkillManager().getSkill(inlineBlock);
                  return sk.orElse(null);
               } catch (Throwable t) {
                  return null;
               }
            });
            if (skill == null) {
               if (this.log) {
                  IgaDebugLogger.log(this.getClass(), "failed to build inline skill from script.");
               }

               return SkillResult.FAILURE;
            } else {
               try {
                  SkillMetadata sData = data.deepClone();
                  skill.execute(sData);
                  if (this.log) {
                     IgaDebugLogger.log(this.getClass(), "executed inline skill (lines=" + script.split("\\r?\\n").length + ")");
                  }

                  return SkillResult.SUCCESS;
               } catch (Throwable ex) {
                  IgaDebugLogger.log(this.getClass(), "error executing inline block: " + ex.getMessage());
                  return SkillResult.FAILURE;
               }
            }
         }
      } else {
         return SkillResult.FAILURE;
      }
   }

   private boolean executeOneLine(String line, SkillMetadata data) {
      String skillName = tryExtractSkillName(line);
      if (skillName != null) {
         Optional<Skill> skOpt = MythicBukkit.inst().getSkillManager().getSkill(skillName);
         if (!skOpt.isPresent()) {
            return SkillResult.FAILURE;
         }

         try {
            skOpt.get().execute(data);
            return SkillResult.SUCCESS;
         } catch (Throwable ex) {
            return SkillResult.FAILURE;
         }
      } else if (reflectExecuteInLine != null) {
         try {
            Object sm = MythicBukkit.inst().getSkillManager();
            Object r = reflectExecuteInLine.invoke(sm, data, line);
            return r instanceof Boolean ? (Boolean)r : true;
         } catch (Throwable ex) {
            return SkillResult.FAILURE;
         }
      } else {
         return SkillResult.FAILURE;
      }
   }

   private static String tryExtractSkillName(String line) {
      String l = line.toLowerCase(Locale.ROOT);
      if (!l.startsWith("skill{") && !l.startsWith("skill {")) {
         return null;
      }

      int braceOpen = line.indexOf(123);
      int braceClose = line.lastIndexOf(125);
      if (braceOpen >= 0 && braceClose >= braceOpen) {
         String inside = line.substring(braceOpen + 1, braceClose).trim();
         String[] parts = inside.split("[;,:]");
         String[] var9 = parts;
         int var8 = parts.length;

         for (int var7 = 0; var7 < var8; var7++) {
            String p = var9[var7];
            String kv = p.trim();
            int eq = kv.indexOf(61);
            if (eq > 0) {
               String key = kv.substring(0, eq).trim().toLowerCase(Locale.ROOT);
               String val = kv.substring(eq + 1).trim();
               if (key.equals("s") || key.equals("skill") || key.equals("name")) {
                  if (val.startsWith("\"") && val.endsWith("\"") || val.startsWith("'") && val.endsWith("'")) {
                     val = val.substring(1, val.length() - 1);
                  }

                  return val;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static void ensureReflectResolved() {
      if (!reflectResolved) {
         reflectResolved = true;

         try {
            Class<?> smClz = MythicBukkit.inst().getSkillManager().getClass();

            try {
               reflectExecuteInLine = smClz.getMethod("executeSkill", SkillMetadata.class, String.class);
               return;
            } catch (NoSuchMethodException var2) {
            }
         } catch (Throwable ignored) {
            reflectExecuteInLine = null;
         }
      }
   }

   private static VSkillMechanic.VarScopePair resolveScopeAndVar(String param0, String param1) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.ClassCastException: class org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent cannot be cast to class org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent (org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent and org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent are in unnamed module of loader 'app')
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.findSyntheticDupVar(SwitchHelper.java:430)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper$StringSwitch.isValid(SwitchHelper.java:929)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper$Merged.match(SwitchHelper.java:1111)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper$StringSwitch.match(SwitchHelper.java:901)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.trySimplifyStringSwitch(SwitchHelper.java:221)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:210)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:410)
      //
      // Bytecode:
      // 00: aload 1
      // 01: ifnull 0e
      // 04: aload 1
      // 05: invokevirtual java/lang/String.trim ()Ljava/lang/String;
      // 08: invokevirtual java/lang/String.isEmpty ()Z
      // 0b: ifeq 12
      // 0e: aconst_null
      // 0f: goto 16
      // 12: aload 1
      // 13: invokestatic com/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore.parseScope (Ljava/lang/String;)Lcom/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore$Scope;
      // 16: astore 2
      // 17: aload 0
      // 18: ifnonnull 21
      // 1b: ldc_w ""
      // 1e: goto 25
      // 21: aload 0
      // 22: invokevirtual java/lang/String.trim ()Ljava/lang/String;
      // 25: astore 3
      // 26: aconst_null
      // 27: astore 4
      // 29: aload 3
      // 2a: bipush 46
      // 2c: invokevirtual java/lang/String.indexOf (I)I
      // 2f: istore 5
      // 31: iload 5
      // 33: ifle dd
      // 36: aload 3
      // 37: bipush 0
      // 38: iload 5
      // 3a: invokevirtual java/lang/String.substring (II)Ljava/lang/String;
      // 3d: invokevirtual java/lang/String.trim ()Ljava/lang/String;
      // 40: astore 6
      // 42: aload 6
      // 44: getstatic java/util/Locale.ROOT Ljava/util/Locale;
      // 47: invokevirtual java/lang/String.toUpperCase (Ljava/util/Locale;)Ljava/lang/String;
      // 4a: dup
      // 4b: astore 7
      // 4d: invokevirtual java/lang/String.hashCode ()I
      // 50: lookupswitch 141 5 -1827576431 52 78959153 66 82781042 80 1980737580 94 2105276323 108
      // 84: aload 7
      // 86: ldc_w "TARGET"
      // 89: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 8c: ifne ca
      // 8f: goto dd
      // 92: aload 7
      // 94: ldc_w "SKILL"
      // 97: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 9a: ifne ca
      // 9d: goto dd
      // a0: aload 7
      // a2: ldc_w "WORLD"
      // a5: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // a8: ifne ca
      // ab: goto dd
      // ae: aload 7
      // b0: ldc_w "CASTER"
      // b3: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // b6: ifne ca
      // b9: goto dd
      // bc: aload 7
      // be: ldc_w "GLOBAL"
      // c1: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // c4: ifne ca
      // c7: goto dd
      // ca: aload 6
      // cc: invokestatic com/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore.parseScope (Ljava/lang/String;)Lcom/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore$Scope;
      // cf: astore 4
      // d1: aload 3
      // d2: iload 5
      // d4: bipush 1
      // d5: iadd
      // d6: invokevirtual java/lang/String.substring (I)Ljava/lang/String;
      // d9: invokevirtual java/lang/String.trim ()Ljava/lang/String;
      // dc: astore 3
      // dd: aload 2
      // de: ifnull e5
      // e1: aload 2
      // e2: goto f2
      // e5: aload 4
      // e7: ifnull ef
      // ea: aload 4
      // ec: goto f2
      // ef: getstatic com/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore$Scope.CASTER Lcom/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore$Scope;
      // f2: astore 6
      // f4: new com/ten/iga/igacustommythicmobs/mechanic/VSkillMechanic$VarScopePair
      // f7: dup
      // f8: aload 6
      // fa: aload 3
      // fb: invokespecial com/ten/iga/igacustommythicmobs/mechanic/VSkillMechanic$VarScopePair.<init> (Lcom/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore$Scope;Ljava/lang/String;)V
      // fe: areturn
   }

   private static String trimOrNull(String x) {
      return x == null ? null : x.trim();
   }

   private static boolean isBlank(String x) {
      return x == null || x.trim().isEmpty();
   }

   private static final class VarScopePair {
      final MetaSkillVariableStore.Scope scope;
      final String var;

      VarScopePair(MetaSkillVariableStore.Scope scope, String var) {
         this.scope = scope;
         this.var = var;
      }
   }
}
