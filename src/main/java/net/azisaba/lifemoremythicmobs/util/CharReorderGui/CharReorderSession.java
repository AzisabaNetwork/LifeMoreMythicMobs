package net.azisaba.lifemoremythicmobs.util.CharReorderGui;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillMetadata;
import java.util.UUID;
import org.bukkit.inventory.Inventory;

public class CharReorderSession {
   private final UUID playerId;
   private final Inventory inventory;
   private final String storeKey;
   private final String onDecideSkillName;
   private final SkillMetadata data;
   private final AbstractEntity target;
   private final int maxChars;
   private final int confirmSlot;
   private int selectedIndex = -1;
   private boolean decideTriggered = false;

   public CharReorderSession(
      UUID playerId, Inventory inventory, String storeKey, String onDecideSkillName, SkillMetadata data, AbstractEntity target, int maxChars, int confirmSlot
   ) {
      this.playerId = playerId;
      this.inventory = inventory;
      this.storeKey = storeKey;
      this.onDecideSkillName = onDecideSkillName;
      this.data = data;
      this.target = target;
      this.maxChars = maxChars;
      this.confirmSlot = confirmSlot;
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public String getStoreKey() {
      return this.storeKey;
   }

   public String getOnDecideSkillName() {
      return this.onDecideSkillName;
   }

   public SkillMetadata getData() {
      return this.data;
   }

   public AbstractEntity getTarget() {
      return this.target;
   }

   public int getMaxChars() {
      return this.maxChars;
   }

   public int getConfirmSlot() {
      return this.confirmSlot;
   }

   public int getSelectedIndex() {
      return this.selectedIndex;
   }

   public void setSelectedIndex(int idx) {
      this.selectedIndex = idx;
   }

   public boolean isDecideTriggered() {
      return this.decideTriggered;
   }

   public void setDecideTriggered(boolean decideTriggered) {
      this.decideTriggered = decideTriggered;
   }
}
