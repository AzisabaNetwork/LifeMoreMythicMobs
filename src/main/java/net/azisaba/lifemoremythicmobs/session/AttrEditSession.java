package net.azisaba.lifemoremythicmobs.session;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;

public class AttrEditSession {
   public static final double UNSET = -2.0;
   private final Attribute[] order;
   private int index = 0;
   private final Map<Attribute, AttrEditSession.AttrSetting> map = new EnumMap<>(Attribute.class);

   public AttrEditSession(Attribute[] order) {
      this.order = order;
      Attribute[] var5 = order;
      int var4 = order.length;

      for (int var3 = 0; var3 < var4; var3++) {
         Attribute a = var5[var3];
         this.map.put(a, new AttrEditSession.AttrSetting());
      }
   }

   public Attribute currentAttr() {
      return this.order[this.index];
   }

   public int getIndex() {
      return this.index;
   }

   public void setIndex(int i) {
      this.index = (i + this.order.length) % this.order.length;
   }

   public int size() {
      return this.order.length;
   }

   public AttrEditSession.AttrSetting getSetting(Attribute a) {
      return this.map.get(a);
   }

   public AttrEditSession.AttrSetting cur() {
      return this.getSetting(this.currentAttr());
   }

   public Map<Attribute, AttrEditSession.AttrSetting> all() {
      return this.map;
   }

   public static double round2(double v) {
      return Math.round(v * 100.0) / 100.0;
   }

   public static class AttrSetting {
      private double value = -2.0;
      private AttrEditSession.Mode mode = AttrEditSession.Mode.ADD;
      private Double draftValue = null;
      private AttrEditSession.Mode draftMode = null;
      private EquipmentSlot slot = EquipmentSlot.HAND;

      public void beginEdit() {
         this.draftValue = this.value;
         this.draftMode = this.mode;
      }

      public void commitDraft() {
         if (this.draftValue != null) {
            this.value = AttrEditSession.round2(this.draftValue);
         }

         if (this.draftMode != null) {
            this.mode = this.draftMode;
         }

         this.draftValue = null;
         this.draftMode = null;
      }

      public void discardDraft() {
         this.draftValue = null;
         this.draftMode = null;
      }

      public double getSaved() {
         return this.value;
      }

      public AttrEditSession.Mode getSavedMode() {
         return this.mode;
      }

      public double getDraftOrSaved() {
         return this.draftValue != null ? this.draftValue : this.value;
      }

      public AttrEditSession.Mode getDraftOrSavedMode() {
         return this.draftMode != null ? this.draftMode : this.mode;
      }

      public void incDraft(double delta) {
         double base = this.draftValue != null && this.draftValue != -2.0 ? this.draftValue : 0.0;
         this.draftValue = AttrEditSession.round2(base + delta);
      }

      public void setDraft(double v) {
         this.draftValue = AttrEditSession.round2(v);
      }

      public void resetDraftToUnset() {
         this.draftValue = -2.0;
      }

      public void toggleDraftMode() {
         this.draftMode = this.getDraftOrSavedMode() == AttrEditSession.Mode.ADD ? AttrEditSession.Mode.PCT : AttrEditSession.Mode.ADD;
      }

      public double getValue() {
         return this.value;
      }

      public void setValue(double v) {
         this.value = v;
      }

      public AttrEditSession.Mode getMode() {
         return this.mode;
      }

      public void toggleMode() {
         this.mode = this.mode == AttrEditSession.Mode.ADD ? AttrEditSession.Mode.PCT : AttrEditSession.Mode.ADD;
      }

      public EquipmentSlot getSlot() {
         return this.slot;
      }

      public void cycleSlot(int dir) {
         EquipmentSlot[] order = new EquipmentSlot[]{
            EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
         };
         int idx = 0;

         for (int i = 0; i < order.length; i++) {
            if (order[i] == this.slot) {
               idx = i;
               break;
            }
         }

         this.slot = order[(idx + dir + order.length) % order.length];
      }
   }

   public enum Mode {
      ADD,
      PCT;
   }
}
