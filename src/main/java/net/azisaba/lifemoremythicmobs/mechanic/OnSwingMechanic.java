package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.IParentSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.auras.Aura;
import io.lumine.mythic.api.skills.auras.Aura.AuraTracker;
import io.lumine.mythic.utils.Events;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class OnSwingMechanic extends Aura implements ITargetedEntitySkill {
   private Optional<Skill> onSwingSkill = Optional.empty();
   private final String onSwingSkillName;
   private final long debounceMs;

   public OnSwingMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.onSwingSkillName = config.getString(new String[]{"onswingskill", "onswing", "os"}, null, new String[0]);
      this.debounceMs = config.getLong(new String[]{"debounce", "debouncems"}, 80L);
      MythicBukkit.inst().getSkillManager().queueSecondPass(() -> {
         if (this.onSwingSkillName != null) {
            this.onSwingSkill = MythicBukkit.inst().getSkillManager().getSkill(this.onSwingSkillName);
         }
      });
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      new OnSwingMechanic.Tracker(data, target);
      return SkillResult.SUCCESS;
   }

   private class Tracker extends AuraTracker implements IParentSkill, Runnable {
      private final UUID playerId;
      private volatile long lastSwingAt = 0L;

      public Tracker(SkillMetadata data, AbstractEntity entity) {
         super(OnSwingMechanic.this, entity, data);
         if (entity != null && entity.isPlayer()) {
            this.playerId = entity.getUniqueId();
         } else {
            this.playerId = null;
         }

         this.start();
      }

      public void auraStart() {
         if (this.playerId == null) {
            this.terminate();
         } else {
            this.registerAuraComponent(
               Events.subscribe(PlayerAnimationEvent.class).filter(e -> e.getPlayer().getUniqueId().equals(this.playerId)).handler(e -> this.onSwing())
            );
            this.registerAuraComponent(
               Events.subscribe(PlayerInteractEvent.class)
                  .filter(e -> e.getPlayer().getUniqueId().equals(this.playerId))
                  .filter(e -> e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
                  .handler(e -> this.onSwing())
            );
            this.registerAuraComponent(
               Events.subscribe(EntityDamageByEntityEvent.class)
                  .filter(e -> e.getDamager() != null && e.getDamager().getUniqueId().equals(this.playerId))
                  .handler(e -> this.onSwing())
            );
            this.executeAuraSkill(OnSwingMechanic.this.onStartSkill, this.skillMetadata);
         }
      }

      private void onSwing() {
         long now = System.currentTimeMillis();
         if (now - this.lastSwingAt >= OnSwingMechanic.this.debounceMs) {
            this.lastSwingAt = now;
            SkillMetadata meta = this.skillMetadata.deepClone();
            AbstractEntity self = (AbstractEntity)this.entity.get();
            if (self != null) {
               meta.setEntityTarget(self);
            }

            if (this.executeAuraSkill(OnSwingMechanic.this.onSwingSkill, meta)) {
               this.consumeCharge();
            }
         }
      }
   }
}
