package net.azisaba.lifemoremythicmobs.util;

/** CombatMath-compatible elemental armor calculation. */
public final class ElementalDefenseMath {
    private static final double ARMOR_SCALE = 0.025D;
    private static final double VANILLA_ARMOR_SCALE = 0.25D;

    private ElementalDefenseMath() {
    }

    public static double reduce(
            double damage,
            double vanillaArmor,
            double vanillaToughness,
            double elementalArmor,
            double elementalToughness,
            boolean ignoreVanillaArmor,
            boolean ignoreElementalDefense
    ) {
        double armor = 0.0D;
        double toughness = 0.0D;

        if (!ignoreVanillaArmor) {
            armor += vanillaArmor * VANILLA_ARMOR_SCALE;
            toughness += vanillaToughness;
        }
        if (!ignoreElementalDefense) {
            armor += elementalArmor;
            toughness += elementalToughness;
        }

        double result = damage / (armor * ARMOR_SCALE + 1.0D)
                / (toughness * ARMOR_SCALE + 1.0D);
        return Double.isFinite(result) ? result : 0.0D;
    }

    /**
     * Converts the requested raw damage to the amount which must enter Bukkit's damage pipeline.
     * CombatMath will subsequently apply the full vanilla armor/toughness divisor when ia=false;
     * this method compensates for it so the final result is exactly {@link #reduce} while leaving
     * MythicMobs' ignoresArmor metadata unchanged.
     */
    public static double prepareForCombatMathPipeline(
            double damage,
            double vanillaArmor,
            double vanillaToughness,
            double elementalArmor,
            double elementalToughness,
            boolean ignoreVanillaArmor,
            boolean ignoreElementalDefense
    ) {
        double reduced = reduce(
                damage,
                vanillaArmor,
                vanillaToughness,
                elementalArmor,
                elementalToughness,
                ignoreVanillaArmor,
                ignoreElementalDefense);
        if (ignoreVanillaArmor) {
            return reduced;
        }

        double vanillaDivisor = (vanillaArmor * ARMOR_SCALE + 1.0D)
                * (vanillaToughness * ARMOR_SCALE + 1.0D);
        double result = reduced * vanillaDivisor;
        return Double.isFinite(result) ? result : 0.0D;
    }
}
