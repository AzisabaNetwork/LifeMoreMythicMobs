package lifemoremythicmobs.org.example.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import lifemoremythicmobs.org.example.lifemoremythicmobs.util.CircleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleVerticalRingMechanic extends SkillMechanic implements ITargetedLocationSkill {

    protected final double radius;
    protected final int rotateX;
    protected final int rotateY;
    protected final int rotateZ;
    protected final int points;
    protected final int amount;
    protected final int speed;
    protected final double startXOffset;
    protected final double startYOffset;
    protected final double startZOffset;
    protected final double size;
    protected final float startSideOffset;
    protected final boolean ignoreEntityRotation;
    protected final boolean uniform;
    protected final Particle particle;
    protected final String color;

    public ParticleVerticalRingMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.radius = config.getDouble(new String[]{"radius", "r"}, 3.0);
        this.points = config.getInteger(new String[]{"points", "po"}, 10);
        this.amount = config.getInteger(new String[]{"amount", "a"}, 1);
        this.speed = config.getInteger(new String[]{"speed", "s"}, 0);
        this.rotateX = config.getInteger(new String[]{"rotatex", "rotx"}, 1);
        this.rotateY = config.getInteger(new String[]{"rotatey", "roty"}, 1);
        this.rotateZ = config.getInteger(new String[]{"rotatez", "rotz"}, 1);
        this.size = config.getInteger(new String[]{"size", "si"}, 1);
        this.startXOffset = config.getDouble(new String[]{"startxoffset", "sxo"}, 0.0);
        this.startYOffset = config.getDouble(new String[]{"startyoffset", "syo"}, 0.0);
        this.startZOffset = config.getDouble(new String[]{"startzoffset", "szo"}, 0.0);
        this.startSideOffset = config.getFloat(new String[]{"startsideoffset", "sso"}, 0);
        this.ignoreEntityRotation = config.getBoolean(new String[]{"ignoreentityrotation", "ier", "i"}, true);
        this.uniform = config.getBoolean(new String[]{"uniform", "uni", "u"}, true);
        this.particle = Particle.valueOf(config.getString(new String[]{"particle", "pa"}, "REDSTONE").toUpperCase()
                .replace("REDDUST", "REDSTONE")
                .replace("RED_DUST", "REDSTONE")
        );
        this.color = config.getString(new String[]{"color", "c"}, "ffffff").replace("#", "");

    }


    @Override
    public boolean castAtLocation(SkillMetadata skillMetadata, AbstractLocation absLoc) {
        Location bukkitLocation = BukkitAdapter.adapt(absLoc);

        bukkitLocation.setX(bukkitLocation.getX() + startXOffset);
        bukkitLocation.setY(bukkitLocation.getY() + startYOffset);
        bukkitLocation.setZ(bukkitLocation.getZ() + startZOffset);

        CircleUtil.spawnCircle(bukkitLocation, points, radius, rotateX, rotateY, rotateZ, amount, speed, ignoreEntityRotation, uniform, particle, color);

        return true;
    }
}
