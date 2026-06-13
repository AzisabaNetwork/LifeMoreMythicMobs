package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.PacketPlayOutPosition;
import net.minecraft.server.v1_15_R1.PacketPlayOutPosition.EnumPlayerTeleportFlags;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
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
         return SkillResult.FAILURE;
      }

      try {
         Player player = (Player)BukkitAdapter.adapt(target);
         EntityPlayer ep = ((CraftPlayer)player).getHandle();
         float currentYaw = target.getLocation().getYaw();
         float currentPitch = target.getLocation().getPitch();
         float yawDelta = (float)this.yawOffset.get(data);
         float pitchDelta = (float)this.pitchOffset.get(data);
         float newYaw = currentYaw + yawDelta;
         float newPitch = this.clampPitch(currentPitch + pitchDelta);
         Set<EnumPlayerTeleportFlags> flags = EnumSet.of(EnumPlayerTeleportFlags.X, EnumPlayerTeleportFlags.Y, EnumPlayerTeleportFlags.Z);
         PacketPlayOutPosition packet = new PacketPlayOutPosition(0.0, 0.0, 0.0, newYaw, newPitch, flags, 0);
         ep.playerConnection.sendPacket(packet);
         return SkillResult.SUCCESS;
      } catch (Exception e) {
         e.printStackTrace();
         return SkillResult.FAILURE;
      }
   }

   private float clampPitch(float pitch) {
      return Math.max(-90.0F, Math.min(90.0F, pitch));
   }
}
