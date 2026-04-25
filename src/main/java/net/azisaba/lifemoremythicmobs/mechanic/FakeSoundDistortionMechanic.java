package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderFloat;
import io.lumine.xikage.mythicmobs.utils.Schedulers;
import org.bukkit.Location;

public class FakeSoundDistortionMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {

    protected final String soundName;
    protected final PlaceholderFloat startPitch;
    protected final PlaceholderFloat endPitch;
    protected final PlaceholderFloat volume;
    protected final int duration;

    public FakeSoundDistortionMechanic(MythicLineConfig config) {
        super(config.getLine(), config);
        this.soundName = config.getString(new String[]{"sound", "s"}, "block.note_block.bit");
        this.startPitch = PlaceholderFloat.of(config.getString(new String[]{"startPitch", "sp"}, "2.0"));
        this.endPitch = PlaceholderFloat.of(config.getString(new String[]{"endPitch", "ep"}, "0.5"));
        this.volume = PlaceholderFloat.of(config.getString(new String[]{"volume", "v"}, "1.0"));
        this.duration = config.getInteger(new String[]{"duration", "d"}, 20);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        return playDistortedSound(data, target.getLocation());
    }

    @Override
    public boolean castAtLocation(SkillMetadata data, AbstractLocation target) {
        return playDistortedSound(data, target);
    }

    private boolean playDistortedSound(SkillMetadata data, AbstractLocation loc) {
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
        return true;
    }
}