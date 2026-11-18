package net.azisaba.lifemoremythicmobs.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure weighted loot table parser and luck-weighted selector.
 */
public final class LootTableUtil {
    private LootTableUtil() {
    }

    public enum EntryKind {
        ITEM, SKILL, NOTHING
    }

    public static final class LootEntry {
        public final EntryKind kind;
        public final String target;
        public final int amount;
        public final double baseWeight;

        public LootEntry(EntryKind kind, String target, int amount, double baseWeight) {
            this.kind = kind;
            this.target = target;
            this.amount = Math.max(1, amount);
            this.baseWeight = Math.max(0.0, baseWeight);
        }
    }

    public static List<LootEntry> parseEntries(String raw) {
        List<LootEntry> list = new ArrayList<>();
        if (raw == null || raw.isBlank()) return list;
        for (String token : raw.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split(":");
            if (parts.length < 2) continue;

            try {
                if (parts[0].equalsIgnoreCase("skill") && parts.length >= 3) {
                    double w = Double.parseDouble(parts[2].trim());
                    list.add(new LootEntry(EntryKind.SKILL, parts[1].trim(), 1, w));
                } else if (parts[0].equalsIgnoreCase("nothing")) {
                    double w = Double.parseDouble(parts[1].trim());
                    list.add(new LootEntry(EntryKind.NOTHING, "", 1, w));
                } else if (parts[0].equalsIgnoreCase("item") && parts.length >= 3) {
                    int amt = parts.length >= 4 ? Integer.parseInt(parts[3].trim()) : 1;
                    double w = Double.parseDouble(parts[2].trim());
                    list.add(new LootEntry(EntryKind.ITEM, parts[1].trim(), amt, w));
                } else {
                    double w = Double.parseDouble(parts[1].trim());
                    int amt = parts.length >= 3 ? Integer.parseInt(parts[2].trim()) : 1;
                    list.add(new LootEntry(EntryKind.ITEM, parts[0].trim(), amt, w));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return list;
    }

    public static LootEntry pickEntry(List<LootEntry> entries, double luck, double luckFactor, double rollUnit) {
        if (entries.isEmpty()) return null;
        double avgWeight = entries.stream().mapToDouble(e -> e.baseWeight).average().orElse(0.0);
        double[] weights = new double[entries.size()];
        double total = 0.0;
        for (int i = 0; i < entries.size(); i++) {
            LootEntry e = entries.get(i);
            double w = e.baseWeight;
            if (luckFactor != 0.0 && luck != 0.0 && w > 0 && w < avgWeight) {
                w = Math.max(0.01, w * (1.0 + luck * luckFactor));
            }
            weights[i] = w;
            total += w;
        }
        if (total <= 0.0) return null;

        double target = Math.max(0.0, Math.min(0.9999999, rollUnit)) * total;
        double acc = 0.0;
        for (int i = 0; i < entries.size(); i++) {
            acc += weights[i];
            if (target < acc) return entries.get(i);
        }
        return entries.get(entries.size() - 1);
    }
}
