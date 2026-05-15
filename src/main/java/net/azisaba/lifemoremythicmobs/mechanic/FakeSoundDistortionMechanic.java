package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderFloat;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.utils.Schedulers;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.Location;

public class FakeSoundDistortionMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {

    protected final String soundName;
    protected final PlaceholderFloat startPitch;
    protected final PlaceholderFloat endPitch;
    protected final PlaceholderFloat volume;
    protected final int duration;

    public FakeSoundDistortionMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.soundName = config.getString(new String[]{"sound", "s"}, "block.note_block.bit");
        this.startPitch = PlaceholderFloat.of(config.getString(new String[]{"startPitch", "sp"}, "2.0"));
        this.endPitch = PlaceholderFloat.of(config.getString(new String[]{"endPitch", "ep"}, "0.5"));
        this.volume = PlaceholderFloat.of(config.getString(new String[]{"volume", "v"}, "1.0"));
        this.duration = config.getInteger(new String[]{"duration", "d"}, 20);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        return playDistortedSound(data, target.getLocation());
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        return playDistortedSound(data, target);
    }

    private SkillResult playDistortedSound(SkillMetadata data, AbstractLocation loc) {
        Location bukkitLoc = BukkitAdapter.adapt(loc);
        float sPitch = startPitch.get(data);
        float ePitch = endPitch.get(data);
        float vol = volume.get(data);

        for (int i = 0; i <= duration; i++) {
            final int tick = i;
            Schedulers.sync().runLater(() -> {
                float currentPitch = sPitch + (ePitch - sPitch) * ((float) tick / (float) duration);

                currentPitch = Math.max(0.5f, Math.min(2.0f, currentPitch));

                bukkitLoc.getWorld().playSound(bukkitLoc, soundName, vol, currentPitch);
            }, tick);
        }
        return SkillResult.SUCCESS;
    }
}