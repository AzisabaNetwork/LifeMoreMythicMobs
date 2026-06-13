package net.azisaba.lifemoremythicmobs.targeter;

import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.targeters.ILocationSelector;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class SphereTargeter extends ILocationSelector {
    protected double radius;
    protected int points;
    protected boolean random;

    public SphereTargeter(MythicLineConfig config) {
        super(config);
        this.radius = config.getDouble(new String[]{"radius", "r"}, 5.0D);
        this.points = config.getInteger(new String[]{"points", "pts", "p"}, 32);
        this.random = config.getBoolean(new String[]{"random", "rng"}, false);
    }

    @Override
    public HashSet<AbstractLocation> getLocations(SkillMetadata data) {
        HashSet<AbstractLocation> targets = new HashSet<>();

        AbstractLocation absOrigin = data.getOrigin();
        if (absOrigin == null) return targets;

        Location origin = BukkitAdapter.adapt(absOrigin);

        if (random) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int i = 0; i < points; i++) {
                double z = rng.nextDouble(-1.0, 1.0);
                double phi = rng.nextDouble(0, 2 * Math.PI);
                double rAtZ = Math.sqrt(1 - z * z);

                double x = rAtZ * Math.cos(phi);
                double y = rAtZ * Math.sin(phi);

                Vector v = new Vector(x, y, z).multiply(radius);
                targets.add(BukkitAdapter.adapt(origin.clone().add(v)));
            }
        } else {
            double phiAngle = Math.PI * (3.0 - Math.sqrt(5.0));

            for (int i = 0; i < points; i++) {
                double y = 1 - (i / (double) (points - 1)) * 2;
                double rAtY = Math.sqrt(1 - y * y);

                double theta = phiAngle * i;

                double x = Math.cos(theta) * rAtY;
                double z = Math.sin(theta) * rAtY;

                Vector v = new Vector(x, y, z).multiply(radius);
                targets.add(BukkitAdapter.adapt(origin.clone().add(v)));
            }
        }

        return targets;
    }
}