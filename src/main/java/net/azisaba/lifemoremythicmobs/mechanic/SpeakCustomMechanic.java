package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.holograms.types.SpeechBubble;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.auras.Aura;
import io.lumine.mythic.api.skills.auras.Aura.AuraTracker;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SpeakCustomMechanic extends Aura implements INoTargetSkill, ITargetedEntitySkill {
   private final PlaceholderString message;
   private final PlaceholderString linePrefix;
   private final PlaceholderInt duration;
   private final float offset;
   private final int radius;
   private final int maxLineLength;
   private final boolean sendChatMessage;

   public SpeakCustomMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.message = PlaceholderString.of(config.getString(new String[]{"message", "m"}, "hello!", new String[0]));
      this.linePrefix = PlaceholderString.of(config.getString(new String[]{"lineprefix", "lp"}, "", new String[0]));
      this.duration = PlaceholderInt.of(config.getString(new String[]{"duration", "d"}, "60", new String[0]));
      this.offset = config.getFloat(new String[]{"offset", "o"}, 1.0F);
      this.radius = config.getInteger(new String[]{"radius", "r"}, 10);
      this.maxLineLength = config.getInteger(new String[]{"maxlinelength", "ll", "mll", "ml", "linelength"}, 22);
      this.sendChatMessage = config.getBoolean(new String[]{"sendchatmessage", "chatmessage", "chat"}, true);
      this.auraName = Optional.of("#speakcustom");
      this.charges = PlaceholderInt.of("1");
      this.maxStacks = PlaceholderInt.of("1");
      this.mergeSameCaster = false;
      this.overwriteCaster = true;
      this.refreshDuration = false;
   }

   public SkillResult cast(SkillMetadata data) {
      new SpeakCustomMechanic.SpeakCustomTracker(data);
      return SkillResult.SUCCESS;
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      new SpeakCustomMechanic.SpeakCustomTracker(target, data);
      return SkillResult.SUCCESS;
   }

   public class SpeakCustomTracker extends AuraTracker {
      private SpeechBubble bubble;
      private final String msg;
      private final String prefix;
      private final Collection<AbstractPlayer> players;
      private boolean bubbleTerminated = false;

      public SpeakCustomTracker(SkillMetadata data) {
         super(SpeakCustomMechanic.this, data);
         this.msg = SpeakCustomMechanic.this.message.get(data);
         this.prefix = SpeakCustomMechanic.this.linePrefix.get(data);
         this.players = this.getNearByPlayers(data.getCaster().getEntity(), SpeakCustomMechanic.this.radius);
         if (this.start()) {
            this.sendChatIfNeeded();
         }
      }

      public SpeakCustomTracker(AbstractEntity target, SkillMetadata data) {
         super(SpeakCustomMechanic.this, target, data);
         this.msg = SpeakCustomMechanic.this.message.get(data, target);
         this.prefix = SpeakCustomMechanic.this.linePrefix.get(data, target);
         this.players = this.getNearByPlayers(target, SpeakCustomMechanic.this.radius);
         if (this.start()) {
            this.sendChatIfNeeded();
         }
      }

      public void auraStart() {
         if (MythicBukkit.inst().getHologramManager().isActive()) {
            SkillCaster caster = this.skillMetadata.getCaster();
            this.bubble = MythicBukkit.inst().getHologramManager().createSpeechBubble(caster);
            if (this.bubble != null) {
               this.bubble.setYOffset(SpeakCustomMechanic.this.offset);
               this.bubble.setLinePrefix(this.prefix);
               this.bubble.setLineLength(SpeakCustomMechanic.this.maxLineLength);
               this.bubble.setText(this.msg);
            }
         }
      }

      public void auraStop() {
         if (this.bubble != null && !this.bubbleTerminated) {
            try {
               this.bubble.terminate();
               this.bubbleTerminated = true;
            } catch (IllegalArgumentException var2) {
            }
         }
      }

      private void sendChatIfNeeded() {
         if (SpeakCustomMechanic.this.sendChatMessage) {
            for (AbstractPlayer p : this.players) {
               p.sendMessage(this.prefix + this.msg);
            }
         }
      }

      private Collection<AbstractPlayer> getNearByPlayers(AbstractEntity entity, double radius) {
         Location loc = BukkitAdapter.adapt(entity.getLocation());
         List<AbstractPlayer> players = new ArrayList<>();

         for (Player p : loc.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(loc) <= radius * radius) {
               players.add(BukkitAdapter.adapt(p));
            }
         }

         return players;
      }
   }
}
