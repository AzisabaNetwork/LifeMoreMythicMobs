package net.azisaba.lifemoremythicmobs.conditions;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
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
import io.lumine.mythic.api.skills.conditions.InvalidCondition;

import java.util.function.Supplier;

public class OrCondition
   extends SkillCondition
   implements ISkillMetaCondition,
   ICasterCondition,
   IEntityCondition,
   ILocationCondition,
   IEntityComparisonCondition,
   ILocationComparisonCondition,
   IEntityLocationComparisonCondition,
   ISkillMetaComparisonCondition {
   private final SkillCondition left;
   private final SkillCondition right;
   private final boolean passOnError;
   private final boolean log;

   public OrCondition(String line, MythicLineConfig config) {
      super(line);
      String rawLeft = config.getString(new String[]{"c1", "cond1", "condition1", "left"}, null, new String[0]);
      String rawRight = config.getString(new String[]{"c2", "cond2", "condition2", "right"}, null, new String[0]);
      this.passOnError = config.getBoolean(new String[]{"passonerror", "failopen"}, false);
      this.log = config.getBoolean(new String[]{"log", "debug"}, false);
      SkillCondition l = null;
      SkillCondition r = null;
      if (rawLeft != null) {
         try {
            l = SkillCondition.getCondition(rawLeft);
            this.logf("init: left='%s', ACTION=%s, actionVar=%s", rawLeft, this.ACTION, this.safeActionVar());
         } catch (Throwable t) {
            IgaDebugLogger.log(this.getClass(), String.format("failed to parse left condition: {0}", rawLeft));
         }
      } else {
         IgaDebugLogger.log(this.getClass(), "No left condition specified (c1/cond1/condition1/left).");
      }

      if (rawRight != null) {
         try {
            r = SkillCondition.getCondition(rawRight);
            this.logf("init: right='%s', ACTION=%s, actionVar=%s", rawRight, this.ACTION, this.safeActionVar());
         } catch (Throwable t) {
            IgaDebugLogger.log(this.getClass(), String.format("failed to parse right condition: {0}", rawRight));
         }
      } else {
         IgaDebugLogger.log(this.getClass(), "No right condition specified (c2/cond2/condition2/right).");
      }

      this.left = l;
      this.right = r;
   }

   public OrCondition(MythicLineConfig config) {
      this(config.getLine(), config);
   }

   private void logf(String fmt, Object... args) {
      if (this.log) {
         IgaDebugLogger.log(this.getClass(), String.format(fmt, args));
      }
   }

   private String safeActionVar() {
      try {
         return this.actionVar != null ? this.actionVar.get() : "null";
      } catch (Throwable t) {
         return "ERR";
      }
   }

   private boolean failPolicy() {
      return this.passOnError;
   }

   private static SkillCondition unwrap(SkillCondition c) {
      return c;
   }

   private boolean evalMeta(SkillCondition sc, SkillMetadata meta) {
      if (sc == null) {
         return this.failPolicy();
      } else {
         SkillCondition c = unwrap(sc);
         if (c instanceof InvalidCondition) {
            return true;
         } else if (c instanceof ISkillMetaCondition) {
            return ((ISkillMetaCondition)c).check(meta);
         } else if (c instanceof ICasterCondition) {
            return ((ICasterCondition)c).check(meta.getCaster());
         } else if (c instanceof IEntityCondition) {
            return ((IEntityCondition)c).check(meta.getCaster().getEntity());
         } else {
            return c instanceof ILocationCondition ? ((ILocationCondition)c).check(meta.getCaster().getLocation()) : false;
         }
      }
   }

   private boolean evalCaster(SkillCondition sc, SkillCaster caster) {
      if (sc == null) {
         return this.failPolicy();
      } else {
         SkillCondition c = unwrap(sc);
         if (c instanceof InvalidCondition) {
            return true;
         } else if (c instanceof ICasterCondition) {
            return ((ICasterCondition)c).check(caster);
         } else if (c instanceof IEntityCondition) {
            return ((IEntityCondition)c).check(caster.getEntity());
         } else {
            return c instanceof ILocationCondition ? ((ILocationCondition)c).check(caster.getLocation()) : false;
         }
      }
   }

   private boolean evalEntity(SkillCondition sc, AbstractEntity e) {
      if (sc == null) {
         return this.failPolicy();
      } else {
         SkillCondition c = unwrap(sc);
         if (c instanceof InvalidCondition) {
            return true;
         } else if (c instanceof IEntityCondition) {
            return ((IEntityCondition)c).check(e);
         } else {
            return c instanceof ILocationCondition ? ((ILocationCondition)c).check(e.getLocation()) : false;
         }
      }
   }

   private boolean evalLocation(SkillCondition sc, AbstractLocation l) {
      if (sc == null) {
         return this.failPolicy();
      } else {
         SkillCondition c = unwrap(sc);
         if (c instanceof InvalidCondition) {
            return true;
         } else {
            return c instanceof ILocationCondition ? ((ILocationCondition)c).check(l) : false;
         }
      }
   }

   private boolean evalEE(SkillCondition sc, AbstractEntity base, AbstractEntity target) {
      if (sc == null) {
         return this.failPolicy();
      } else {
         SkillCondition c = unwrap(sc);
         if (c instanceof InvalidCondition) {
            return true;
         } else if (c instanceof IEntityComparisonCondition) {
            return ((IEntityComparisonCondition)c).check(base, target);
         } else if (c instanceof IEntityCondition) {
            return ((IEntityCondition)c).check(target);
         } else {
            return c instanceof ILocationCondition ? ((ILocationCondition)c).check(target.getLocation()) : false;
         }
      }
   }

   private boolean evalLL(SkillCondition sc, AbstractLocation base, AbstractLocation target) {
      if (sc == null) {
         return this.failPolicy();
      } else {
         SkillCondition c = unwrap(sc);
         if (c instanceof InvalidCondition) {
            return true;
         } else if (c instanceof ILocationComparisonCondition) {
            return ((ILocationComparisonCondition)c).check(base, target);
         } else {
            return c instanceof ILocationCondition ? ((ILocationCondition)c).check(target) : false;
         }
      }
   }

   private boolean evalEL(SkillCondition sc, AbstractEntity base, AbstractLocation target) {
      if (sc == null) {
         return this.failPolicy();
      } else {
         SkillCondition c = unwrap(sc);
         if (c instanceof InvalidCondition) {
            return true;
         } else if (c instanceof IEntityLocationComparisonCondition) {
            return ((IEntityLocationComparisonCondition)c).check(base, target);
         } else {
            return c instanceof ILocationCondition ? ((ILocationCondition)c).check(target) : false;
         }
      }
   }

   private boolean evalMetaE(SkillCondition sc, SkillMetadata meta, AbstractEntity target) {
      if (sc == null) {
         return this.failPolicy();
      } else {
         SkillCondition c = unwrap(sc);
         if (c instanceof InvalidCondition) {
            return true;
         } else if (c instanceof ISkillMetaComparisonCondition) {
            return ((ISkillMetaComparisonCondition)c).check(meta, target);
         } else {
            return c instanceof IEntityCondition ? ((IEntityCondition)c).check(target) : false;
         }
      }
   }

   private boolean or(boolean leftResult, Supplier<Boolean> rightSupplier, String where) {
      if (leftResult) {
         this.logf("%s: left=%s, right=SKIPPED -> OR=true", where, true);
         return true;
      } else {
         boolean rightResult = rightSupplier.get();
         boolean out = rightResult;
         this.logf("%s: left=%s, right=%s -> OR=%s", where, false, rightResult, out);
         return out;
      }
   }

   public boolean handleOutcome(SkillMetadata meta, boolean outcome) {
      boolean r = super.handleOutcome(meta, outcome);
      this.logf("handleOutcome(meta): before=%s, ACTION=%s, actionVar=%s -> after=%s", outcome, this.ACTION, this.safeActionVar(), r);
      return r;
   }

   public boolean check(SkillMetadata meta) {
      boolean l = this.evalMeta(this.left, meta);
      return this.or(l, () -> this.evalMeta(this.right, meta), "check(meta)");
   }

   public boolean check(SkillCaster caster) {
      boolean l = this.evalCaster(this.left, caster);
      return this.or(l, () -> this.evalCaster(this.right, caster), "check(caster)");
   }

   public boolean check(AbstractEntity entity) {
      boolean l = this.evalEntity(this.left, entity);
      return this.or(l, () -> this.evalEntity(this.right, entity), "check(entity)");
   }

   public boolean check(AbstractLocation loc) {
      boolean l = this.evalLocation(this.left, loc);
      return this.or(l, () -> this.evalLocation(this.right, loc), "check(location");
   }

   public boolean check(AbstractEntity base, AbstractEntity target) {
      boolean l = this.evalEE(this.left, base, target);
      return this.or(l, () -> this.evalEE(this.right, base, target), "check(entity,entity)");
   }

   public boolean check(AbstractLocation base, AbstractLocation target) {
      boolean l = this.evalLL(this.left, base, target);
      return this.or(l, () -> this.evalLL(this.right, base, target), "check(location,location)");
   }

   public boolean check(AbstractEntity base, AbstractLocation target) {
      boolean l = this.evalEL(this.left, base, target);
      return this.or(l, () -> this.evalEL(this.right, base, target), "check(entity,location)");
   }

   public boolean check(SkillMetadata meta, AbstractEntity target) {
      boolean l = this.evalMetaE(this.left, meta, target);
      return this.or(l, () -> this.evalMetaE(this.right, meta, target), "check(meta,entity)");
   }
}
