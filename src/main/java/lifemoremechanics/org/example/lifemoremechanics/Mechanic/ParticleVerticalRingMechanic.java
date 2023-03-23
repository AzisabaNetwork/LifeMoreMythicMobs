package lifemoremechanics.org.example.lifemoremechanics.Mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;

public class ParticleVerticalRingMechanic implements ITargetedLocationSkill {


    @Override
    public boolean castAtLocation(SkillMetadata skillMetadata, AbstractLocation abstractLocation) {
        return false;
    }
}
