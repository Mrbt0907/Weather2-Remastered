package net.mrbt0907.weather2.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mrbt0907.weather2.api.event.StopSoundEvent;

@Mod.EventBusSubscriber(value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class SoundHandler
{
    private static final Minecraft MC = Minecraft.getInstance();

    private static final int RETRY_COOLDOWN_TICKS = 40;

    private static final int POOL_SIZE = 32;

    private static final SlotEntry[] slots = new SlotEntry[POOL_SIZE];

    static {
        for (int i = 0; i < POOL_SIZE; i++)
            slots[i] = new SlotEntry();
    }

    private static int currentTick = 0;

    public static void tick()
    {
        currentTick++;
    }

    private static class SlotEntry
    {
        ISound sound = null;
        int lastSubmitTick = -999;
        int priority = 0;

        boolean isEmpty() { return sound == null; }

        void set(ISound s, int prio)
        {
            sound = s;
            priority = prio;
            lastSubmitTick = currentTick;
        }

        void clear()
        {
            sound = null;
            priority = 0;
            lastSubmitTick = -999;
        }

        boolean isLive()
        {
            if (sound == null) return false;
            if (MC.getSoundManager().isActive(sound)) return true;
            return (currentTick - lastSubmitTick) < RETRY_COOLDOWN_TICKS;
        }
    }

    @SubscribeEvent
    public static void onSoundLoad(SoundLoadEvent event)
    {
        for (SlotEntry slot : slots) slot.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSoundStop(StopSoundEvent event)
    {
        ISound sound = event.getSound();
        if (sound == null) return;

        for (SlotEntry slot : slots)
        {
            if (sound.equals(slot.sound))
            {
                slot.clear();
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSoundPlay(PlaySoundEvent event)
    {
        ISound sound = event.getResultSound();
        if (sound == null) return;

        if (sound.getSound() == null || !sound.getSound().shouldStream())
            return;

        int priority = getSoundPriority(sound);
        int freeSlot = -1;
        int evictSlot = -1;
        int evictPriority = Integer.MAX_VALUE;

        for (int i = 0; i < slots.length; i++)
        {
            SlotEntry slot = slots[i];
            if (!slot.isLive())
            {
                if (freeSlot < 0) freeSlot = i;
            }
            else if (slot.priority < priority && slot.priority < evictPriority)
            {
                evictSlot = i;
                evictPriority = slot.priority;
            }
        }

        if (freeSlot >= 0)
        {
            slots[freeSlot].set(sound, priority);
        }
        else if (evictSlot >= 0)
        {
            MC.getSoundManager().stop(slots[evictSlot].sound);
            slots[evictSlot].set(sound, priority);
        }
        else
        {
            event.setResultSound(null);
        }
    }

    public static int getSoundPriority(ISound sound)
    {
        return sound instanceof MovingSoundEX ? ((MovingSoundEX) sound).priority : 0;
    }

    public static void pruneStaleSounds()
    {
        for (SlotEntry slot : slots)
        {
            if (slot.sound != null && !slot.isLive())
                slot.clear();
        }
    }

    public static ISound getSound(SoundEvent sound, int priority)
    {
        for (SlotEntry slot : slots)
        {
            if (slot.sound != null
                    && slot.sound.getLocation().toString().equals(sound.getRegistryName().toString())
                    && slot.priority == priority)
                return slot.sound;
        }
        return null;
    }

    public static boolean contains(SoundEvent sound, int priority)
    {
        return getSound(sound, priority) != null;
    }

    public static boolean canPlaySound(SoundEvent sound, int priority)
    {
        if (contains(sound, priority)) return false;
        for (SlotEntry slot : slots)
        {
            if (!slot.isLive() || slot.priority < priority)
                return true;
        }
        return false;
    }

    public static void stopSound(SoundEvent sound, int priority)
    {
        for (SlotEntry slot : slots)
        {
            if (slot.sound != null
                    && slot.sound.getLocation().toString().equals(sound.getRegistryName().toString())
                    && slot.priority == priority)
            {
                MC.getSoundManager().stop(slot.sound);
                slot.clear();
                return;
            }
        }
    }

    public static MovingSoundEX playStaticSound(SoundEvent sound, SoundCategory category, int priority, float volume, float pitch)
    {
        return playSound(null, sound, category, priority, volume, pitch, 0.0D);
    }

    public static MovingSoundEX playStaticSound(SoundEvent sound, SoundCategory category, int priority, float volume, float pitch, double range)
    {
        return playSound(null, sound, category, priority, volume, pitch, range);
    }

    public static MovingSoundEX playMovingSound(Object obj, SoundEvent sound, SoundCategory category, int priority, float volume, float pitch)
    {
        return playSound(obj, sound, category, priority, volume, pitch, 0.0D);
    }

    public static MovingSoundEX playMovingSound(Object obj, SoundEvent sound, SoundCategory category, int priority, float volume, float pitch, double range)
    {
        return playSound(obj, sound, category, priority, volume, pitch, range);
    }

    private static MovingSoundEX playSound(Object obj, SoundEvent sound, SoundCategory category, int priority, float volume, float pitch, double range)
    {
        if (!canPlaySound(sound, priority)) return null;
        MovingSoundEX streamingSound = new MovingSoundEX(obj, sound, category, priority, volume, pitch, range);
        MC.getSoundManager().play(streamingSound);
        return streamingSound;
    }
}