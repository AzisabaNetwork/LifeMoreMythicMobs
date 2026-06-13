package net.azisaba.lifemoremythicmobs.listener;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class DamageAuraManager {
   private static final DamageAuraManager instance = new DamageAuraManager();
   private final Map<UUID, Map<String, DamageAuraManager.AuraData>> activeAuras = new ConcurrentHashMap<>();

   public static DamageAuraManager getInstance() {
      return instance;
   }

   public void applyAura(UUID uuid, String auraName, double multiplier, long durationMs) {
      Map<String, DamageAuraManager.AuraData> auraMap = this.activeAuras.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
      auraMap.put(auraName, new DamageAuraManager.AuraData(System.currentTimeMillis() + durationMs, multiplier));
   }

   public double getCombinedMultiplier(UUID uuid) {
      Map<String, DamageAuraManager.AuraData> auraMap = this.activeAuras.get(uuid);
      if (auraMap == null) {
         return 1.0;
      }

      long now = System.currentTimeMillis();
      double totalMultiply = 1.0;
      Iterator<Entry<String, DamageAuraManager.AuraData>> it = auraMap.entrySet().iterator();

      while (it.hasNext()) {
         Entry<String, DamageAuraManager.AuraData> entry = it.next();
         String auraName = entry.getKey();
         DamageAuraManager.AuraData aura = entry.getValue();
         if (now > aura.expireTime) {
            it.remove();
         } else {
            totalMultiply *= aura.multiplier;
         }
      }

      if (auraMap.isEmpty()) {
         this.activeAuras.remove(uuid);
      }

      return totalMultiply;
   }

   private static class AuraData {
      final long expireTime;
      final double multiplier;

      AuraData(long expireTime, double multiplier) {
         this.expireTime = expireTime;
         this.multiplier = multiplier;
      }
   }
}
