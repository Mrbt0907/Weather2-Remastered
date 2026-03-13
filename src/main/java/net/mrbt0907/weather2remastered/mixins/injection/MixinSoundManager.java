package net.mrbt0907.weather2.mixins.injection;

import net.minecraft.client.audio.ChannelManager;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEngine;
import net.minecraftforge.common.MinecraftForge;
import net.mrbt0907.weather2.api.event.StopSoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SoundEngine.class)
public class MixinSoundManager
{
    @Shadow
    private boolean loaded;

    @Shadow
    private Map<ISound, ChannelManager.Entry> instanceToChannel;

    @Inject(method = "tick(Z)V", at = @At("HEAD"))
    public void tick(boolean paused, CallbackInfo ci)
    {
        if (!loaded || instanceToChannel == null) return;

        ISound[] sounds = instanceToChannel.keySet().toArray(new ISound[0]);
        for (ISound sound : sounds)
        {
            if (instanceToChannel.get(sound) == null)
                MinecraftForge.EVENT_BUS.post(new StopSoundEvent((SoundEngine)(Object)this, sound));
        }
    }

    @Inject(method = "stop(Lnet/minecraft/client/audio/ISound;)V", at = @At("HEAD"))
    public void stop(ISound sound, CallbackInfo ci)
    {
        if (loaded && sound != null && instanceToChannel.containsKey(sound))
            MinecraftForge.EVENT_BUS.post(new StopSoundEvent((SoundEngine)(Object)this, sound));
    }

    @Inject(method = "stopAll()V", at = @At("HEAD"))
    public void stopAll(CallbackInfo ci)
    {
        if (loaded && instanceToChannel != null)
            instanceToChannel.keySet().forEach(sound ->
                    MinecraftForge.EVENT_BUS.post(new StopSoundEvent((SoundEngine)(Object)this, sound)));
    }
}