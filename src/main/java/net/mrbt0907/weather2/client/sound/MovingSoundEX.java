package net.mrbt0907.weather2.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class MovingSoundEX extends TickableSound
{
    private static final Minecraft MC = Minecraft.getInstance();
    public Object obj;
    public float maxVolume;
    public double range;
    public int priority;

    public MovingSoundEX(SoundEvent sound, SoundCategory category, int priority, float volume, float pitch)
    {
        this(null, sound, category, priority, volume, pitch, 0.0D);
    }

    public MovingSoundEX(SoundEvent sound, SoundCategory category, int priority, float volume, float pitch, double range)
    {
        this(null, sound, category, priority, volume, pitch, range);
    }

    public MovingSoundEX(Object obj, SoundEvent sound, SoundCategory category, int priority, float volume, float pitch)
    {
        this(obj, sound, category, priority, volume, pitch, 0.0D);
    }

    public MovingSoundEX(Object obj, SoundEvent sound, SoundCategory category, int priority, float volume, float pitch, double range)
    {
        super(sound, category);
        this.obj = obj;
        this.volume = Maths.clamp(volume, 0.0F, 1.0F);
        this.maxVolume = this.volume;
        this.pitch = pitch;
        this.range = range;
        this.priority = priority;
        this.looping = true;
        this.delay = 0;

        if (MC.player != null)
        {
            this.x = (float) MC.player.getX();
            this.y = (float) MC.player.getY();
            this.z = (float) MC.player.getZ();
        }
    }

    @Override
    public void tick()
    {
        if (MC.player == null || MC.level == null)
        {
            this.stop();
            Weather2.error("Unable to tick sound " + getLocation() + " as the world is null");
            return;
        }

        float px = (float) (MC.player.getX() + MC.player.getDeltaMovement().x);
        float py = (float) (MC.player.getY() + MC.player.getDeltaMovement().y);
        float pz = (float) (MC.player.getZ() + MC.player.getDeltaMovement().z);

        this.x = px;
        this.y = py;
        this.z = pz;

        if (obj != null)
        {
            Vec3 pos;

            if (obj instanceof Vec3)
                pos = (Vec3) obj;
            else if (obj instanceof BlockPos)
                pos = new Vec3((BlockPos) obj);
            else if (obj instanceof WeatherObject)
                pos = ((WeatherObject) obj).pos;
            else if (obj instanceof Entity)
            {
                Entity entity = (Entity) obj;
                pos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
            }
            else
            {
                this.stop();
                Weather2.error("Unable to tick sound " + getLocation()
                        + " — unsupported object type: " + obj.getClass().getName());
                return;
            }

            if (range > 0.0D)
            {
                double distSq = pos.distanceSq(MC.player.getX(), MC.player.getY(), MC.player.getZ());
                float multiplier = (float) Maths.clamp((range - distSq) / range, 0.0D, 1.0D);
                this.volume = maxVolume * multiplier;

                this.x = (float) Maths.clamp(pos.posX, px - 6.0D, px + 6.0D);
                this.y = (float) Maths.clamp(pos.posY, py - 6.0D, py + 6.0D);
                this.z = (float) Maths.clamp(pos.posZ, pz - 6.0D, pz + 6.0D);
            }
        }
    }

    @Override
    public boolean canPlaySound()
    {
        return !isStopped();
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }

    public void setRepeat(boolean shouldRepeat)
    {
        this.looping = shouldRepeat;
    }

    public void adjustVolume(float volume)
    {
        this.maxVolume = Maths.clamp(volume, 0.0F, 1.0F);
        if (obj == null || range <= 0.0D)
            this.volume = this.maxVolume;
    }

    public void adjustPitch(float pitch)
    {
        this.pitch = Maths.clamp(pitch, 0.5F, 2.0F);
    }

    public void setDone()
    {
        this.stop();
    }
}