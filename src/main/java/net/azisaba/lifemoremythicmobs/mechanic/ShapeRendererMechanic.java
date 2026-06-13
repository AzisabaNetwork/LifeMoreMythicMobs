package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ShapeRendererMechanic extends SkillMechanic implements ITargetedLocationSkill, ITargetedEntitySkill {
    private final String shape;
    private final Particle particle;
    private final double radius;
    private final int points;
    private final double density;
    private final double rotation;
    private final Color color;

    private final String expX;
    private final String expY;
    private final String expZ;
    private final double tMin;
    private final double tMax;

    private final Expression eX;
    private final Expression eY;
    private final Expression eZ;

    public ShapeRendererMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.shape = config.getString(new String[]{"shape", "s"}, "circle").toLowerCase();
        this.particle = Particle.valueOf(config.getString(new String[]{"particle", "p"}, "REDSTONE").toUpperCase());
        this.radius = config.getDouble(new String[]{"radius", "r"}, 2.0);
        this.points = config.getInteger(new String[]{"points", "pt"}, 5);
        this.density = config.getDouble(new String[]{"density", "d"}, 0.2);
        this.rotation = Math.toRadians(config.getDouble(new String[]{"rotation", "rot"}, 0));

        this.expX = config.getString(new String[]{"expX", "ex"}, "r*cos(t)").replace("\"", "").replace("<&fs>", "/").replace("<&as>", "*").replace("<&sp>", " ");
        this.expY = config.getString(new String[]{"expY", "ey"}, "0").replace("\"", "").replace("<&fs>", "/").replace("<&as>", "*").replace("<&sp>", " ");
        this.expZ = config.getString(new String[]{"expZ", "ez"}, "r*sin(t)").replace("\"", "").replace("<&fs>", "/").replace("<&as>", "*").replace("<&sp>", " ");
        this.tMin = config.getDouble(new String[]{"tMin", "tmin"}, 0);
        this.tMax = config.getDouble(new String[]{"tMax", "tmax"}, Math.PI * 2);

        String colorStr = config.getString(new String[]{"color", "c"}, "#FFFFFF");
        this.color = Color.fromRGB(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16)
        );

        if (this.shape.equals("custom")) {
            this.eX = new ExpressionBuilder(expX).variables("t", "r", "pi").build();
            this.eY = new ExpressionBuilder(expY).variables("t", "r", "pi").build();
            this.eZ = new ExpressionBuilder(expZ).variables("t", "r", "pi").build();
        } else {
            this.eX = null;
            this.eY = null;
            this.eZ = null;
        }
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        return castAtLocation(data, target.getLocation());
    }

    @Override
    public boolean castAtLocation(SkillMetadata data, AbstractLocation target) {
        Location center = BukkitAdapter.adapt(target);
        double casterYaw = Math.toRadians(data.getCaster().getLocation().getYaw());

        switch (shape) {
            case "circle":
                drawCircle(center, casterYaw);
                break;
            case "polygon":
                drawPolygon(center, points, casterYaw);
                break;
            case "star":
                drawStar(center, points, casterYaw);
                break;
            case "custom":
                drawCustom(center, casterYaw);
                break;
        }
        return true;
    }

    private void spawn(Location loc) {
        if (particle == Particle.REDSTONE) {
            loc.getWorld().spawnParticle(particle, loc, 1, new Particle.DustOptions(color, 1.0f));
        } else {
            loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    private void drawCircle(Location center, double yaw) {
        for (double t = 0; t < Math.PI * 2; t += density / radius) {
            double lx = Math.cos(t + rotation) * radius;
            double lz = Math.sin(t + rotation) * radius;
            spawn(center.clone().add(rotate(lx, 0, lz, yaw)));
        }
    }

    private void drawPolygon(Location center, int vertices, double yaw) {
        for (int i = 0; i < vertices; i++) {
            double angle1 = (Math.PI * 2 / vertices) * i + rotation;
            double angle2 = (Math.PI * 2 / vertices) * (i + 1) + rotation;
            Vector v1 = rotate(Math.cos(angle1) * radius, 0, Math.sin(angle1) * radius, yaw);
            Vector v2 = rotate(Math.cos(angle2) * radius, 0, Math.sin(angle2) * radius, yaw);
            drawLine(center.clone().add(v1), center.clone().add(v2));
        }
    }

    private void drawStar(Location center, int vertices, double yaw) {
        for (int i = 0; i < vertices; i++) {
            double angle1 = (Math.PI * 2 / vertices) * i + rotation;
            double angle2 = (Math.PI * 2 / vertices) * (i + 2) + rotation;
            Vector v1 = rotate(Math.cos(angle1) * radius, 0, Math.sin(angle1) * radius, yaw);
            Vector v2 = rotate(Math.cos(angle2) * radius, 0, Math.sin(angle2) * radius, yaw);
            drawLine(center.clone().add(v1), center.clone().add(v2));
        }
    }

    private void drawCustom(Location center, double yaw) {
        if (eX == null || eY == null || eZ == null) return;
        try {
            eX.setVariable("r", radius).setVariable("pi", Math.PI);
            eY.setVariable("r", radius).setVariable("pi", Math.PI);
            eZ.setVariable("r", radius).setVariable("pi", Math.PI);

            for (double t = tMin; t <= tMax; t += density) {
                double lx = eX.setVariable("t", t + rotation).evaluate();
                double ly = eY.setVariable("t", t + rotation).evaluate();
                double lz = eZ.setVariable("t", t + rotation).evaluate();
                spawn(center.clone().add(rotate(lx, ly, lz, yaw)));
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[LifeMoreMythicMobs] Custom Shape error: " + e.getMessage());
        }
    }

    private Vector rotate(double x, double y, double z, double yaw) {
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        double nx = x * cos - z * sin;
        double nz = x * sin + z * cos;
        return new Vector(nx, y, nz);
    }

    private void drawLine(Location start, Location end) {
        Vector dir = end.toVector().subtract(start.toVector());
        double len = dir.length();
        dir.normalize();
        for (double i = 0; i < len; i += density) {
            spawn(start.clone().add(dir.clone().multiply(i)));
        }
    }
}