package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import net.azisaba.lifemoremythicmobs.util.MetaSkillVariableStore;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.ArrayList;
import java.util.List;

public class SetMetaSkillVariableMechanic extends SkillMechanic implements INoTargetSkill, ITargetedEntitySkill, ITargetedLocationSkill {
   private final PlaceholderString varNamePS;
   private final PlaceholderString valRawPS;
   private final String scopeRaw;
   private final String origLine;
   private final boolean log;

   public SetMetaSkillVariableMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.varNamePS = config.getPlaceholderString(new String[]{"name", "n", "variable", "var", "k"}, null, new String[0]);
      this.valRawPS = config.getPlaceholderString(new String[]{"val", "value"}, null, new String[0]);
      this.scopeRaw = config.getString(new String[]{"scope", "s"}, null, new String[0]);
      this.origLine = config.getLine();
      this.log = config.getBoolean(new String[]{"log"}, false);
   }

   public SkillResult cast(SkillMetadata data) {
      return this.apply(data, null);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      return this.apply(data, target);
   }

   public boolean castAtLocation(SkillMetadata data, AbstractLocation location) {
      return this.apply(data, null);
   }

   public boolean apply(SkillMetadata data, AbstractEntity preferTarget) {
      if (this.varNamePS != null && this.valRawPS != null) {
         String rawVar = this.varNamePS.get(data, data.getCaster().getEntity());
         if (rawVar != null && !rawVar.isEmpty()) {
            SetMetaSkillVariableMechanic.VarScopePair vs = resolveScopeAndVar(rawVar, this.scopeRaw);
            String varName = vs.var;
            MetaSkillVariableStore.Scope scope = vs.scope;
            String raw = this.valRawPS.get(data, data.getCaster().getEntity());
            if (raw != null && !raw.trim().isEmpty()) {
               List<String> lines = normalizeMetaSkillBlock(raw);
               if (lines.isEmpty()) {
                  return SkillResult.FAILURE;
               }

               String script = String.join("\n", lines);
               MetaSkillVariableStore.setForScope(data, scope, varName, script, preferTarget);
               if (this.log) {
                  IgaDebugLogger.log(this.getClass(), "saved: scope=" + scope + " var=" + varName + " lines=" + lines.size());
               }

               return SkillResult.SUCCESS;
            } else {
               return SkillResult.FAILURE;
            }
         } else {
            return SkillResult.FAILURE;
         }
      } else {
         return SkillResult.FAILURE;
      }
   }

   private static List<String> normalizeMetaSkillBlock(String raw) {
      String s = raw.trim();
      if (s.endsWith("]}")) {
         s = s.substring(0, s.length() - 2).trim();
      }

      if (s.endsWith("]")) {
         s = s.substring(0, s.length() - 1).trim();
      }

      if (s.startsWith("val=")) {
         s = s.substring(4).trim();
      }

      if (s.startsWith("[")) {
         s = s.substring(1).trim();
      }

      s = s.replaceAll("[\\t ]+-\\s+", "\n- ");
      String[] split = s.split("\\r?\\n");
      List<String> out = new ArrayList<>();
      String[] var7 = split;
      int var6 = split.length;

      for (int var5 = 0; var5 < var6; var5++) {
         String line = var7[var5];
         String t = line.trim();
         if (!t.isEmpty()) {
            if (!t.startsWith("- ")) {
               t = "- " + t;
            }

            out.add(t);
         }
      }

      return out;
   }

   private static SetMetaSkillVariableMechanic.VarScopePair resolveScopeAndVar(String param0, String param1) {
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
      // 18: ifnonnull 20
      // 1b: ldc ""
      // 1d: goto 24
      // 20: aload 0
      // 21: invokevirtual java/lang/String.trim ()Ljava/lang/String;
      // 24: astore 3
      // 25: aconst_null
      // 26: astore 4
      // 28: aload 3
      // 29: bipush 46
      // 2b: invokevirtual java/lang/String.indexOf (I)I
      // 2e: istore 5
      // 30: iload 5
      // 32: ifle d9
      // 35: aload 3
      // 36: bipush 0
      // 37: iload 5
      // 39: invokevirtual java/lang/String.substring (II)Ljava/lang/String;
      // 3c: invokevirtual java/lang/String.trim ()Ljava/lang/String;
      // 3f: astore 6
      // 41: aload 6
      // 43: getstatic java/util/Locale.ROOT Ljava/util/Locale;
      // 46: invokevirtual java/lang/String.toUpperCase (Ljava/util/Locale;)Ljava/lang/String;
      // 49: dup
      // 4a: astore 7
      // 4c: invokevirtual java/lang/String.hashCode ()I
      // 4f: lookupswitch 138 5 -1827576431 49 78959153 63 82781042 77 1980737580 91 2105276323 105
      // 80: aload 7
      // 82: ldc_w "TARGET"
      // 85: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 88: ifne c6
      // 8b: goto d9
      // 8e: aload 7
      // 90: ldc_w "SKILL"
      // 93: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 96: ifne c6
      // 99: goto d9
      // 9c: aload 7
      // 9e: ldc_w "WORLD"
      // a1: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // a4: ifne c6
      // a7: goto d9
      // aa: aload 7
      // ac: ldc_w "CASTER"
      // af: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // b2: ifne c6
      // b5: goto d9
      // b8: aload 7
      // ba: ldc_w "GLOBAL"
      // bd: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // c0: ifne c6
      // c3: goto d9
      // c6: aload 6
      // c8: invokestatic com/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore.parseScope (Ljava/lang/String;)Lcom/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore$Scope;
      // cb: astore 4
      // cd: aload 3
      // ce: iload 5
      // d0: bipush 1
      // d1: iadd
      // d2: invokevirtual java/lang/String.substring (I)Ljava/lang/String;
      // d5: invokevirtual java/lang/String.trim ()Ljava/lang/String;
      // d8: astore 3
      // d9: aload 2
      // da: ifnull e1
      // dd: aload 2
      // de: goto ee
      // e1: aload 4
      // e3: ifnull eb
      // e6: aload 4
      // e8: goto ee
      // eb: getstatic com/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore$Scope.CASTER Lcom/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore$Scope;
      // ee: astore 6
      // f0: new com/ten/iga/igacustommythicmobs/mechanic/SetMetaSkillVariableMechanic$VarScopePair
      // f3: dup
      // f4: aload 6
      // f6: aload 3
      // f7: invokespecial com/ten/iga/igacustommythicmobs/mechanic/SetMetaSkillVariableMechanic$VarScopePair.<init> (Lcom/ten/iga/igacustommythicmobs/util/MetaSkillVariableStore$Scope;Ljava/lang/String;)V
      // fa: areturn
   }

   private static String safeTrim(String x) {
      return x == null ? null : x.trim();
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
