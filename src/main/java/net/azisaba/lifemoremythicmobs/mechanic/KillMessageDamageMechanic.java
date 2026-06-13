package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.damage.DamagingMechanic;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class KillMessageDamageMechanic extends DamagingMechanic implements ITargetedEntitySkill {
   private final Plugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());
   private final PlaceholderDouble amount;
   private final PlaceholderString message;
   private final KillMessageDamageMechanic.Scope scope;
   private final double radius;
   private final boolean stripResistance;
   public static final String META_PENDING = "ICMM_KM_PENDING";
   public static final String META_CONFIRMED = "ICMM_KM_CONFIRMED";

   public KillMessageDamageMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.amount = PlaceholderDouble.of(config.getString(new String[]{"amount", "a"}, "1", new String[0]));
      this.message = PlaceholderString.of(config.getString(new String[]{"message", "msg", "m"}, "&c{victim}&7 was slain by &c{caster}&7.", new String[0]));

      KillMessageDamageMechanic.Scope tmp;
      try {
         tmp = KillMessageDamageMechanic.Scope.valueOf(config.getString(new String[]{"scope"}, "WORLD", new String[0]).toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         tmp = KillMessageDamageMechanic.Scope.WORLD;
      }

      this.scope = tmp;
      this.radius = config.getDouble(new String[]{"radius", "r"}, 30.0);
      this.stripResistance = config.getBoolean(new String[]{"stripresistance", "clearresistance", "removeresist", "rs"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (target.isDead()) {
         return SkillResult.FAILURE;
      }

      if (data.getCaster().isUsingDamageSkill()) {
         return SkillResult.FAILURE;
      }

      if (target.isLiving() && !(target.getHealth() > 0.0)) {
         return SkillResult.FAILURE;
      }

      double dmg = this.amount.get(data, target) * data.getPower();
      Entity bt = BukkitAdapter.adapt(target);
      Entity bc = BukkitAdapter.adapt(data.getCaster().getEntity());
      String msgRaw = this.message.get(data);
      String msg = ChatColor.translateAlternateColorCodes(
         '&', msgRaw.replace("{caster}", bc != null ? bc.getName() : "Unknown").replace("{victim}", bt.getName())
      );
      boolean appliedPending = false;
      if (bt instanceof Player) {
         String packed = msg + "\u0001" + this.scope.name() + "\u0001" + this.radius + "\u0001" + (bc != null ? bc.getUniqueId().toString() : "");
         bt.setMetadata("ICMM_KM_PENDING", new FixedMetadataValue(this.plugin, packed));
         appliedPending = true;
         if (this.stripResistance) {
            Player p = (Player)bt;
            if (p.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
               p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            }
         }
      }

      try {
         this.doDamage(data.getCaster(), target, dmg);
         return SkillResult.SUCCESS;
      } catch (Throwable t) {
         if (appliedPending) {
            bt.removeMetadata("ICMM_KM_PENDING", this.plugin);
         }

         IgaDebugLogger.log(this.getClass(), "doDamage failed: " + t.getMessage());
         return SkillResult.FAILURE;
      }
   }

   private enum Scope {
      SERVER,
      WORLD,
      RADIUS;
   }
}
