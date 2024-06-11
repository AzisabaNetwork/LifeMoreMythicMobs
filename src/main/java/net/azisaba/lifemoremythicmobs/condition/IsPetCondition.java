package net.azisaba.lifemoremythicmobs.condition;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;

import java.util.Optional;

public class IsPetCondition extends SkillCondition implements IEntityCondition {
    private final boolean invert;

    public IsPetCondition(MythicLineConfig config) {
        super(config.getLine());

        this.invert = config.getBoolean(new String[] {"invert", "i", "反転"}, false);
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        boolean matched = false;
        for (MyPet pet : MyPetApi.getMyPetManager().getAllActiveMyPets()) {
            Optional<MyPetBukkitEntity> opt = pet.getEntity();
            if (!opt.isPresent()) {
                continue;
            }
            if (opt.get().getUniqueId().equals(abstractEntity.getUniqueId()) ||
                    opt.get().getHandle().getUniqueID().equals(abstractEntity.getUniqueId())) {
                matched = true;
                break;
            }
        }
        return invert != matched;
    }
}
