package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.CharReorderGui.CharReorderGuiManager;
import net.azisaba.lifemoremythicmobs.util.CharReorderGui.CharReorderSession;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CharReorderGuiMechanic extends SkillMechanic implements INoTargetSkill, ITargetedEntitySkill {
   private final PlaceholderString text;
   private final PlaceholderString storeKey;
   private final PlaceholderString title;
   private final PlaceholderString onDecideSkillName;
   private static final int MAX_CHARS_CONFIG = 23;
   private static final int MAX_CHARS_HARD_LIMIT = 26;
   private static final int MAX_CHARS = Math.max(1, Math.min(23, 26));

   public CharReorderGuiMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.text = PlaceholderString.of(config.getString(new String[]{"text", "t"}, "", new String[0]));
      this.storeKey = PlaceholderString.of(config.getString(new String[]{"store", "var", "key"}, "", new String[0]));
      this.title = PlaceholderString.of(config.getString(new String[]{"title"}, "文字の並び替え", new String[0]));
      this.onDecideSkillName = PlaceholderString.of(
         config.getString(new String[]{"ondecideskill", "onDecide", "ondecide", "oD", "od", "ods"}, "", new String[0])
      );
   }

   public SkillResult cast(SkillMetadata data) {
      AbstractEntity caster = data.getCaster().getEntity();
      Entity bukkitEntity = caster.getBukkitEntity();
      if (!(bukkitEntity instanceof Player)) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)bukkitEntity;
      String resolvedText = safeResolve(this.text, data);
      String resolvedKey = safeResolve(this.storeKey, data);
      String resolvedTitle = safeResolve(this.title, data);
      String resolvedDecide = safeResolve(this.onDecideSkillName, data);
      List<String> chars = toCodePointList(resolvedText, MAX_CHARS);
      Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> {
         AbstractEntity target = data.getTrigger();
         CharReorderSession var7x = CharReorderGuiManager.openFor(player, chars, resolvedKey, resolvedTitle, resolvedDecide, data, target, MAX_CHARS);
      });
      return SkillResult.SUCCESS;
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      Entity ent = target != null ? target.getBukkitEntity() : null;
      if (!(ent instanceof Player)) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)ent;
      String resolvedText = safeResolve(this.text, data);
      String resolvedKey = safeResolve(this.storeKey, data);
      String resolvedTitle = safeResolve(this.title, data);
      String resolvedDecide = safeResolve(this.onDecideSkillName, data);
      List<String> chars = toCodePointList(resolvedText, MAX_CHARS);
      Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(LifeMoreMythicMobs.class), () -> {
         CharReorderSession var7x = CharReorderGuiManager.openFor(player, chars, resolvedKey, resolvedTitle, resolvedDecide, data, target, MAX_CHARS);
      });
      return SkillResult.SUCCESS;
   }

   private static String safeResolve(PlaceholderString ps, SkillMetadata data) {
      try {
         return ps.get(data, data.getCaster().getEntity());
      } catch (Throwable t) {
         return ps.toString();
      }
   }

   private static List<String> toCodePointList(String src, int max) {
      if (src == null) {
         src = "";
      }

      int[] cps = src.codePoints().toArray();
      List<String> list = new ArrayList<>();
      int limit = Math.min(max, cps.length);

      for (int i = 0; i < limit; i++) {
         list.add(new String(Character.toChars(cps[i])));
      }

      return list;
   }
}
