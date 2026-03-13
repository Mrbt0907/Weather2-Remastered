package net.mrbt0907.weather2.api.event;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEngine;
import net.minecraftforge.client.event.sound.SoundEvent;


public class StopSoundEvent extends SoundEvent {
    private final String name;
    private final ISound sound;

    public StopSoundEvent(SoundEngine engine, ISound sound) {
        super(engine);
        this.sound = sound;
        name = sound.getLocation().getPath();
    }

    public String getName() {
        return name;
    }

    public ISound getSound() {
        return sound;
    }
}