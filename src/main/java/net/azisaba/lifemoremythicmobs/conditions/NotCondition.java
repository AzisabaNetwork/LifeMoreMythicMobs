package net.azisaba.lifemoremythicmobs.conditions;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.logging.MythicLogger;
import io.lumine.mythic.logging.MythicLogger.DebugLevel;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.api.skills.conditions.IEntityComparisonCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.IEntityLocationComparisonCondition;
import io.lumine.mythic.api.skills.conditions.ILocationComparisonCondition;
import io.lumine.mythic.api.skills.conditions.ILocationCondition;
import io.lumine.mythic.api.skills.conditions.ISkillMetaComparisonCondition;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;


public class NotCondition
   extends SkillCondition
   implements ISkillMetaCondition,
   ICasterCondition,
   IEntityCondition,
   ILocationCondition,
   IEntityComparisonCondition,
   ILocationComparisonCondition,
   IEntityLocationComparisonCondition,
   ISkillMetaComparisonCondition {
   private final SkillCondition inner;
   private final boolean passOnError;
   private final boolean log;

   public NotCondition(String line, MythicLineConfig config) {
      super(line);
      String raw = config.getString(new String[]{"c", "condition", "cond"}, null, new String[0]);
      this.passOnError = config.getBoolean(new String[]{"passonerror", "failopen"}, false);
      this.log = config.getBoolean(new String[]{"log", "debug"}, false);
      SkillCondition parsed = null;
      if (raw != null) {
         try {
            parsed = SkillCondition.getCondition(raw);
            this.logf("init: inner='%s', ACTION=%s, actionVar=%s", raw, this.ACTION, this.safeActionVar());
         } catch (Throwable t) {
            MythicLogger.debug(DebugLevel.SKILL_CHECK, "[NotCondition] failed to parse inner condition: {0}", new Object[]{raw});
         }
      } else {
         MythicLogger.debug(DebugLevel.SKILL_CHECK, "[NotCondition] No inner condition specified (c/condition/cond).", new Object[0]);
      }

      this.inner = parsed;
   }

   public NotCondition(MythicLineConfig config) {
      this(config.getLine(), config);
   }

   private String safeActionVar() {
      try {
         return this.actionVar != null ? this.actionVar.get() : "null";
      } catch (Throwable t) {
         return "ERR";
      }
   }

   private void logf(String fmt, Object... args) {
      if (this.log) {
         IgaDebugLogger.log(this.getClass(), String.format(fmt, args));
      }
   }

   private static SkillCondition unwrap(SkillCondition c) {
      return c;
   }

   private boolean failPolicy() {
      return this.passOnError;
   }

   public boolean handleOutcome(SkillMetadata meta, boolean outcome) {
      boolean r = super.handleOutcome(meta, outcome);
      this.logf("handleOutcome(meta): before=%s, ACTION=%s, actionVar=%s -> after=%s", outcome, this.ACTION, this.safeActionVar(), r);
      return r;
   }

   private boolean not(boolean raw) {
      return !raw;
   }

   public boolean check(SkillMetadata meta) {
      if (this.inner == null) {
         return this.passOnError;
      } else {
         SkillCondition ic = unwrap(this.inner);
         if (ic instanceof ISkillMetaCondition) {
            return this.not(((ISkillMetaCondition)ic).check(meta));
         } else if (ic instanceof ICasterCondition) {
            return this.not(((ICasterCondition)ic).check(meta.getCaster()));
         } else if (ic instanceof IEntityCondition) {
            return this.not(((IEntityCondition)ic).check(meta.getCaster().getEntity()));
         } else {
            return ic instanceof ILocationCondition ? this.not(((ILocationCondition)ic).check(meta.getCaster().getLocation())) : false;
         }
      }
   }

   public boolean check(SkillCaster caster) {
      if (this.inner == null) {
         return this.passOnError;
      } else {
         SkillCondition ic = unwrap(this.inner);
         if (ic instanceof ICasterCondition) {
            return this.not(((ICasterCondition)ic).check(caster));
         } else if (ic instanceof IEntityCondition) {
            return this.not(((IEntityCondition)ic).check(caster.getEntity()));
         } else {
            return ic instanceof ILocationCondition ? this.not(((ILocationCondition)ic).check(caster.getEntity().getLocation())) : false;
         }
      }
   }

   public boolean check(AbstractEntity entity) {
      if (this.inner == null) {
         return this.passOnError;
      } else {
         SkillCondition ic = unwrap(this.inner);
         if (ic instanceof IEntityCondition) {
            return this.not(((IEntityCondition)ic).check(entity));
         } else {
            return ic instanceof ILocationCondition ? this.not(((ILocationCondition)ic).check(entity.getLocation())) : false;
         }
      }
   }

   public boolean check(AbstractLocation loc) {
      if (this.inner == null) {
         return this.passOnError;
      }

      SkillCondition ic = unwrap(this.inner);
      return ic instanceof ILocationCondition ? this.not(((ILocationCondition)ic).check(loc)) : false;
   }

   public boolean check(AbstractEntity base, AbstractEntity target) {
      if (this.inner == null) {
         return this.passOnError;
      } else {
         SkillCondition ic = unwrap(this.inner);
         if (ic instanceof IEntityComparisonCondition) {
            return this.not(((IEntityComparisonCondition)ic).check(base, target));
         } else if (ic instanceof IEntityCondition) {
            return this.not(((IEntityCondition)ic).check(target));
         } else {
            return ic instanceof ILocationCondition ? this.not(((ILocationCondition)ic).check(target.getLocation())) : false;
         }
      }
   }

   public boolean check(AbstractLocation base, AbstractLocation target) {
      if (this.inner == null) {
         return this.passOnError;
      } else {
         SkillCondition ic = unwrap(this.inner);
         if (ic instanceof ILocationComparisonCondition) {
            return this.not(((ILocationComparisonCondition)ic).check(base, target));
         } else {
            return ic instanceof ILocationCondition ? this.not(((ILocationCondition)ic).check(target)) : false;
         }
      }
   }

   public boolean check(AbstractEntity base, AbstractLocation target) {
      if (this.inner == null) {
         return this.passOnError;
      } else {
         SkillCondition ic = unwrap(this.inner);
         if (ic instanceof IEntityLocationComparisonCondition) {
            return this.not(((IEntityLocationComparisonCondition)ic).check(base, target));
         } else {
            return ic instanceof ILocationCondition ? this.not(((ILocationCondition)ic).check(target)) : false;
         }
      }
   }

   public boolean check(SkillMetadata meta, AbstractEntity target) {
      if (this.inner == null) {
         return this.passOnError;
      } else {
         SkillCondition ic = unwrap(this.inner);
         if (ic instanceof ISkillMetaComparisonCondition) {
            return this.not(((ISkillMetaComparisonCondition)ic).check(meta, target));
         } else {
            return ic instanceof IEntityCondition ? this.not(((IEntityCondition)ic).check(target)) : false;
         }
      }
   }
}
