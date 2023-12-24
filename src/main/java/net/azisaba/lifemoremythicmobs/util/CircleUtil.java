package net.azisaba.lifemoremythicmobs.util;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.Random;

public class CircleUtil {

    public static Vector getCirclePoint(Vector pos, double radian, double radius, Vector v, Vector u) {
        return pos.clone().add(v.clone().multiply(Math.cos(Math.toRadians(radian)) * radius).add(u.clone().multiply(Math.sin(Math.toRadians(radian)) * radius)));
    }

    public static void spawnCircle(
            Location loc,
            int points,
            double radius,
            int rotx,
            int roty,
            int rotz,
            int amount,
            int speed,
            boolean ignoreEntityRotation,
            boolean uniform,
            Particle particle,
            String color) {

        ParticleBuilder builder = particle.builder().count(amount).extra(speed);
        switch(particle) {
            case REDSTONE: builder.color(Color.fromRGB(Integer.parseInt(color, 16)));
            break;
        }
        Vector v = ignoreEntityRotation ?new Vector(rotx, roty, rotz).normalize(): loc.getDirection().
                rotateAroundX(Math.toRadians(rotx)).rotateAroundY(Math.toRadians(roty)).rotateAroundZ(Math.toRadians(rotz));
        Vector u = v.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        Vector w = v.clone().crossProduct(u).normalize();
        for (int i = 0; i < points; i++) {
            Random random = new Random();
            Vector pos = getCirclePoint(loc.toVector(), uniform ?360d/points*i: random.nextInt(360), radius, u, w);
            builder.location(pos.toLocation(loc.getWorld())).spawn();
        }
    }
}

