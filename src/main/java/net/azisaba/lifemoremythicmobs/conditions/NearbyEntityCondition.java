package net.azisaba.lifemoremythicmobs.conditions;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class NearbyEntityCondition extends SkillCondition implements IEntityCondition {
   private final double radius;
   private final int minAmount;
   private final int maxAmount;
   private final List<SkillCondition> conditions;
   private final boolean invert;

   public NearbyEntityCondition(MythicLineConfig config) {
      super(config.getLine());
      this.radius = config.getDouble(new String[]{"radius", "r"}, 5.0);
      String amountStr = config.getString(new String[]{"amount", "a"}, "1", new String[0]).replace("to", "~");
      String[] split = amountStr.split("~");
      if (split.length == 2) {
         this.minAmount = Integer.parseInt(split[0]);
         this.maxAmount = Integer.parseInt(split[1]);
      } else {
         int fixed = Integer.parseInt(split[0]);
         this.minAmount = fixed;
         this.maxAmount = fixed;
      }

      String conditionStr = config.getString("conditions", null);
      this.conditions = conditionStr != null ? MythicBukkit.inst().getSkillManager().getConditions(conditionStr) : null;
      this.invert = config.getBoolean("invert", false);
   }

   public boolean check(AbstractEntity abstractEntity) {
      if (!(abstractEntity.getBukkitEntity() instanceof LivingEntity)) {
         return false;
      }

      LivingEntity base = (LivingEntity)abstractEntity.getBukkitEntity();
      int count = 0;

      for (Entity e : base.getWorld().getNearbyEntities(base.getLocation(), this.radius, this.radius, this.radius)) {
         if (e != base && e instanceof LivingEntity) {
            AbstractEntity target = BukkitAdapter.adapt((LivingEntity)e);
            boolean passed = true;
            if (this.conditions != null) {
               for (SkillCondition cond : this.conditions) {
                  if (!cond.evaluateEntity(target)) {
                     passed = false;
                     break;
                  }
               }
            }

            if (passed) {
               count++;
            }
         }
      }

      boolean result = count >= this.minAmount && count <= this.maxAmount;
      return this.invert ? !result : result;
   }
}
