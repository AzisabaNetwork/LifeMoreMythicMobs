package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.logging.MythicLogger;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.UUID;

public class RemoveNamedTotemMechanic extends SkillMechanic implements INoTargetSkill {
   private final PlaceholderString totemNamePS;
   private final PlaceholderString ownerUuidPS;

   public RemoveNamedTotemMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      String rawName = config.getString(new String[]{"totemName", "name", "id"}, null, new String[0]);
      this.totemNamePS = rawName != null ? PlaceholderString.of(rawName) : null;
      String rawOwner = config.getString(new String[]{"owneruuid", "owner", "by"}, null, new String[0]);
      this.ownerUuidPS = rawOwner != null ? PlaceholderString.of(rawOwner) : null;
   }

   public SkillResult cast(SkillMetadata data) {
      try {
         String name = this.totemNamePS != null ? this.totemNamePS.get(data, data.getCaster() != null ? data.getCaster().getEntity() : null) : null;
         if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
               name = null;
            }
         }

         if (name == null) {
            return SkillResult.FAILURE;
         }

         UUID owner = this.resolveOwner(data);
         if (owner == null) {
            return SkillResult.FAILURE;
         }

         int removed = NamedTotemMechanic.TotemRegistry.terminate(name, owner);
         return removed > 0;
      } catch (Exception ex) {
         MythicLogger.error("An error occurred executing RemoveNamedTotemMechanic", ex);
         return SkillResult.FAILURE;
      }
   }

   private UUID resolveOwner(SkillMetadata data) {
      if (this.ownerUuidPS != null) {
         String s = this.ownerUuidPS.get(data, data.getCaster() != null ? data.getCaster().getEntity() : null);
         if (s != null) {
            s = s.trim();

            try {
               return UUID.fromString(s);
            } catch (IllegalArgumentException var7) {
            }
         }
      }

      AbstractEntity ce = data.getCaster() != null ? data.getCaster().getEntity() : null;
      if (ce == null) {
         return null;
      }

      try {
         return ce.getUniqueId();
      } catch (Throwable t) {
         try {
            return ce.getBukkitEntity().getUniqueId();
         } catch (Throwable t2) {
            return null;
         }
      }
   }
}
