package com.strangesmell.valorant;

import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class LeizhiLoopSoundInstance extends EntityBoundSoundInstance {
    public LeizhiLoopSoundInstance(SoundEvent soundEvent, SoundSource source, Entity entity, long seed) {
        super(soundEvent, source, 1.0F, 1.0F, entity, seed);
        this.looping = true;
        this.delay = 0;
    }
}
