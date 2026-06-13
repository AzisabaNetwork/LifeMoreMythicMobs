package net.azisaba.lifemoremythicmobs.conditions;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GamemodeCondition extends SkillCondition implements IEntityCondition {
   private final List<GameMode> validModes = new ArrayList<>();
   private final boolean invert;

   public GamemodeCondition(MythicLineConfig config) {
      super(config.getLine());
      GameMode parsedMode = GameMode.SURVIVAL;
      String modeStr = config.getString(new String[]{"mode", "m"}, "", new String[0]);
      if (modeStr != null) {
         String[] var7;
         int var6 = (var7 = modeStr.split("\\s*,\\s*")).length;

         for (int var5 = 0; var5 < var6; var5++) {
            String modeToken = var7[var5];

            try {
               GameMode gm = GameMode.valueOf(modeToken.trim().toUpperCase());
               this.validModes.add(gm);
            } catch (IllegalArgumentException e) {
               IgaDebugLogger.log(this.getClass(), "Invalid gamemode: " + modeStr);
            }
         }
      }

      if (this.validModes.isEmpty()) {
         this.validModes.add(GameMode.SURVIVAL);
      }

      this.invert = config.getBoolean("invert", false);
   }

   public boolean check(AbstractEntity target) {
      if (!target.isPlayer()) {
         return this.invert;
      }

      Player player = (Player)target.getBukkitEntity();
      boolean match = this.validModes.contains(player.getGameMode());
      return this.invert ^ match;
   }
}
