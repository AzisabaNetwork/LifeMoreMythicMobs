package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import org.bukkit.entity.Player;

public class RecoilViewMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderDouble pitchOffset;
   private final PlaceholderDouble yawOffset;

   public RecoilViewMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.pitchOffset = PlaceholderDouble.of(config.getString(new String[]{"pitch", "p"}, "0", new String[0]));
      this.yawOffset = PlaceholderDouble.of(config.getString(new String[]{"yaw", "y"}, "0", new String[0]));
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!(target instanceof AbstractPlayer)) {
         return SkillResult.ERROR;
      }

      try {
         Player player = (Player)BukkitAdapter.adapt(target);
         float currentYaw = target.getLocation().getYaw();
         float currentPitch = target.getLocation().getPitch();
         float yawDelta = (float)this.yawOffset.get(data);
         float pitchDelta = (float)this.pitchOffset.get(data);
         float newYaw = currentYaw + yawDelta;
         float newPitch = this.clampPitch(currentPitch + pitchDelta);
         player.setRotation(newYaw, newPitch);
         return SkillResult.SUCCESS;
      } catch (Exception e) {
         e.printStackTrace();
         return SkillResult.ERROR;
      }
   }

   private float clampPitch(float pitch) {
      return Math.max(-90.0F, Math.min(90.0F, pitch));
   }
}


