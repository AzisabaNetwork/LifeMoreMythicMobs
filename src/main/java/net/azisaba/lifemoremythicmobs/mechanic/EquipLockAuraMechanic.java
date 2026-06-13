package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.EquipLockManager;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.IParentSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.auras.Aura;
import io.lumine.mythic.api.skills.auras.Aura.AuraTracker;

public class EquipLockAuraMechanic extends Aura implements ITargetedEntitySkill {
   public EquipLockAuraMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (target != null && target.isPlayer()) {
         new EquipLockAuraMechanic.Tracker(data, target);
         return SkillResult.SUCCESS;
      } else {
         return SkillResult.FAILURE;
      }
   }

   private class Tracker extends AuraTracker implements IParentSkill, Runnable {
      public Tracker(SkillMetadata data, AbstractEntity entity) {
         super(EquipLockAuraMechanic.this, entity, data);
         this.start();
      }

      public void auraTick() {
         this.executeAuraSkill(EquipLockAuraMechanic.this.onTickSkill, this.skillMetadata);
      }

      public void auraStart() {
         super.auraStart();
         if (this.entity.isPresent()) {
            AbstractEntity abs = (AbstractEntity)this.entity.get();
            if (abs.isPlayer()) {
               EquipLockManager.getInstance().addLock(abs.getUniqueId());
            }
         }
      }

      public void auraStop() {
         try {
            super.auraStop();
         } finally {
            if (this.entity.isPresent()) {
               AbstractEntity abs = (AbstractEntity)this.entity.get();
               if (abs.isPlayer()) {
                  EquipLockManager.getInstance().removeLock(abs.getUniqueId());
               }
            }
         }
      }
   }
}
