package net.azisaba.lifemoremythicmobs.listener;
import java.util.UUID;
public class DamageAuraManager {
    private static final DamageAuraManager INSTANCE = new DamageAuraManager();
    public static DamageAuraManager getInstance() { return INSTANCE; }
    public double getCombinedMultiplier(UUID uuid) { return 1.0; }
}
