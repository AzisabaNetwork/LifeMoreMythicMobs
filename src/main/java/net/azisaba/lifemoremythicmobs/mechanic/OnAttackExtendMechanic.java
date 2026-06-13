package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.adapters.bukkit.BukkitTriggerMetadata;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.IParentSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.auras.Aura.AuraTracker;
import io.lumine.mythic.api.skills.mechanics.OnAttackMechanic;
import io.lumine.mythic.utils.Events;
import java.util.Optional;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class OnAttackExtendMechanic extends OnAttackMechanic {
   private final boolean useFinal;

   public OnAttackExtendMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.useFinal = config.getBoolean(new String[]{"usefinal", "final", "usefinaldamage"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      new OnAttackExtendMechanic.Tracker(data, target);
      return SkillResult.SUCCESS;
   }

   private class Tracker extends AuraTracker implements IParentSkill, Runnable {
      public Tracker(SkillMetadata data, AbstractEntity entity) {
         super(OnAttackExtendMechanic.this, entity, data);
         this.start();
      }

      public void auraStart() {
         this.registerAuraComponent(
            Events.subscribe(EntityDamageByEntityEvent.class)
               .filter(ev -> ev.getCause() == DamageCause.ENTITY_ATTACK)
               .filter(ev -> ev.getDamager().getUniqueId().equals(((AbstractEntity)this.entity.get()).getUniqueId()))
               .filter(ev -> {
                  Optional<Object> md = BukkitAdapter.adapt(ev.getDamager()).getMetadata("doing-skill-damage");
                  return md.<Boolean>map(o -> !(Boolean)o).orElse(true);
               })
               .handler(ev -> {
                  SkillMetadata meta = this.skillMetadata.deepClone();
                  AbstractEntity target = BukkitAdapter.adapt(ev.getEntity());
                  meta.setEntityTarget(target);
                  if (!OnAttackExtendMechanic.this.useFinal) {
                     BukkitTriggerMetadata.apply(meta, ev);
                  }

                  boolean executed = this.executeAuraSkill(OnAttackExtendMechanic.this.onAttackSkill, meta);
                  if (executed) {
                     this.consumeCharge();
                     if (OnAttackExtendMechanic.this.cancelDamage) {
                        ev.setCancelled(true);
                        if (OnAttackExtendMechanic.this.useFinal) {
                           meta.getVariables().putString("damage-amount", "0");
                        }
                     } else if (OnAttackExtendMechanic.this.modDamage) {
                        double newDamage = OnAttackExtendMechanic.this.calculateDamage(meta, target, ev.getDamage());
                        ev.setDamage(newDamage);
                        if (OnAttackExtendMechanic.this.useFinal) {
                           meta.getVariables().putString("damage-amount", String.valueOf(newDamage));
                        }
                     } else if (OnAttackExtendMechanic.this.useFinal) {
                        meta.getVariables().putString("damage-amount", String.valueOf(ev.getFinalDamage()));
                     }
                  }
               })
         );
         this.executeAuraSkill(OnAttackExtendMechanic.this.onStartSkill, this.skillMetadata);
      }
   }
}
