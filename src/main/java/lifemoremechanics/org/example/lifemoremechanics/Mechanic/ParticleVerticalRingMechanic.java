package lifemoremechanics.org.example.lifemoremechanics.Mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import lifemoremechanics.org.example.lifemoremechanics.Util.CircleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleVerticalRingMechanic extends SkillMechanic implements ITargetedLocationSkill {

    protected final int radius;
    protected final int rotateX;
    protected final int rotateY;
    protected final int rotateZ;
    protected final int points;
    protected final int amount;
    protected final int speed;
    protected final boolean ignoreEntityRotation;
    protected final boolean uniform;
    protected final Particle particle;
    protected final String color;

    public ParticleVerticalRingMechanic(MythicLineConfig config) {
        super(config.getLine(), config);

        this.radius = config.getInteger(new String[]{"radius", "r"}, 3);
        this.points = config.getInteger(new String[]{"points", "po"}, 10);
        this.amount = config.getInteger(new String[]{"amount", "a"}, 1);
        this.speed = config.getInteger(new String[]{"speed", "s"}, 0);
        this.rotateX = config.getInteger(new String[]{"rotatex", "rotx"}, 1);
        this.rotateY = config.getInteger(new String[]{"rotatey", "roty"}, 1);
        this.rotateZ = config.getInteger(new String[]{"rotatez", "rotz"}, 1);
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

        CircleUtil.spawnCircle(bukkitLocation, points, radius, rotateX, rotateY, rotateZ, amount, speed, ignoreEntityRotation, uniform, particle, color);

        return true;
    }
}
