package net.azisaba.lifemoremythicmobs.conditions;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.mobs.ActiveMob;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import java.util.Optional;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Future;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HutagoAngelNearbyCondition extends SkillCondition implements ICasterCondition {
   private final double radius;
   private final boolean invert;

   public HutagoAngelNearbyCondition(MythicLineConfig config) {
      super(config.getLine());
      this.radius = config.getDouble("radius", 10.0);
      this.invert = config.getBoolean("invert", false);
   }

   public boolean check(SkillCaster caster) {
      try {
         Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> this.runCheckSync(caster));
         return future.get();
      } catch (Exception e) {
         IgaDebugLogger.log(this.getClass(), "同期実行時に例外発生: " + e.getClass().getSimpleName() + " - " + e.getMessage());
         return this.invert;
      }
   }

   private boolean runCheckSync(SkillCaster caster) {
      try {
         IgaDebugLogger.log(this.getClass(), "=== 開始 ===");
         AbstractEntity casterEntity = caster.getEntity();
         if (casterEntity != null && casterEntity.isValid()) {
            IgaDebugLogger.log(this.getClass(), "entity: " + casterEntity.getUniqueId() + ", radius=" + this.radius + ", invert=" + this.invert);
            UUID casterUUID = casterEntity.getUniqueId();
            AbstractLocation casterLoc = casterEntity.getLocation();
            if (casterLoc != null && casterLoc.getWorld() != null) {
               Collection<ActiveMob> allMobs = MythicBukkit.inst().getMobManager().getActiveMobs();
               IgaDebugLogger.log(this.getClass(), "→ ActiveMob数: " + allMobs.size());

               for (ActiveMob mob : allMobs) {
                  if (mob.getType().getInternalName().toLowerCase().contains("iga_hutago_angel")) {
                     AbstractEntity mobEntity = mob.getEntity();
                     if (mobEntity != null && mobEntity.isValid() && !mobEntity.isDead()) {
                        AbstractLocation mobLoc = mobEntity.getLocation();
                        if (mobLoc.getWorld().equals(casterLoc.getWorld()) && !(mobLoc.distanceSquared(casterLoc) > this.radius * this.radius)) {
                           Optional<UUID> ownerOpt = mob.getOwner();
                           if (ownerOpt.isPresent() && ((UUID)ownerOpt.get()).equals(casterUUID)) {
                              IgaDebugLogger.log(this.getClass(), "→ 該当モブ発見！mobId=" + mob.getType().getInternalName() + ", owner=" + ownerOpt.get());
                              boolean result = !this.invert;
                              IgaDebugLogger.log(this.getClass(), "→ 条件評価結果: " + !this.invert + " → invert後: " + result);
                              return result;
                           }
                        }
                     }
                  }
               }

               IgaDebugLogger.log(this.getClass(), "→ 該当モブなし");
               return this.invert;
            } else {
               IgaDebugLogger.log(this.getClass(), "→ caster.getLocation() が null または world が null");
               return this.invert;
            }
         } else {
            IgaDebugLogger.log(this.getClass(), "→ casterEntityがnullまたは無効。結果: " + this.invert);
            return this.invert;
         }
      } catch (Throwable t) {
         IgaDebugLogger.log(this.getClass(), "例外発生！: " + t.getClass().getSimpleName() + " - " + t.getMessage());
         t.printStackTrace();
         return this.invert;
      }
   }
}
