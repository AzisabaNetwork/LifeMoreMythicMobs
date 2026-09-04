package net.azisaba.lifemoremythicmobs.condition;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;

import java.util.Optional;

public class IsPetCondition extends SkillCondition implements IEntityCondition {
    private final boolean invert;

    public IsPetCondition(MythicLineConfig config) {
        super(config.getLine());

        this.invert = config.getBoolean(new String[] {"invert", "i", "反転"}, false);
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        if (abstractEntity == null) {
            return false;
        }
        try {
            if (!org.bukkit.Bukkit.getPluginManager().isPluginEnabled("MyPet")) {
                return invert;
            }
        } catch (Throwable ignored) {
            return invert;
        }
        boolean matched = false;
        try {
            for (MyPet pet : MyPetApi.getMyPetManager().getAllActiveMyPets()) {
                Optional<MyPetBukkitEntity> opt = pet.getEntity();
                if (opt.isEmpty()) {
                    continue;
                }
                if (opt.get().getUniqueId().equals(abstractEntity.getUniqueId()) ||
                        opt.get().getHandle().getUniqueID().equals(abstractEntity.getUniqueId())) {
                    matched = true;
                    break;
                }
            }
        } catch (Throwable ignored) {
            return invert;
        }
        return invert != matched;
    }
}
