package net.mrbt0907.weather2.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.client.sound.MovingSoundEX;

public class WeatherUtilSound
{
	private static final List<MovingSoundEX> allSounds = new ArrayList<MovingSoundEX>();
	private static final Map<Integer, MovingSoundEX> sounds = new HashMap<Integer, MovingSoundEX>();
	
	@SideOnly(Side.CLIENT)
	public static void tick()
	{
		if (sounds.isEmpty()) return;
		
		Minecraft mc = Minecraft.getMinecraft();
		Iterator<MovingSoundEX> soundList = allSounds.iterator();
		Integer key;
		MovingSoundEX sound;
		
		while (soundList.hasNext())
		{
			key = null;
			sound = soundList.next();
			
			if (sound.canRepeat() && sound.isDonePlaying() && sound.ticksExisted > 20L || !sound.canRepeat() && !mc.getSoundHandler().isSoundPlaying(sound) && !sound.isDonePlaying() && sound.ticksExisted > 20L)
			{
				sound.setDone();
				
				for(Entry<Integer, MovingSoundEX> entry : sounds.entrySet())
				{
					if (entry.getValue().equals(sound))
					{
						key = entry.getKey();
						break;
					}
				}
				
				if(key != null)
					sounds.remove(key);
					
				soundList.remove();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isSoundActive(int index)
	{
		boolean truth = sounds.containsKey(index);
		
		if (truth && sounds.get(index).isDonePlaying())
		{
			sounds.remove(index);
			truth = false;
		}
			
		return truth;
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isSoundActive(int index, SoundEvent sound)
	{
		boolean truth = sounds.containsKey(index);
		
		if (truth && sounds.get(index).isDonePlaying())
		{
			sounds.remove(index);
			truth = false;
		}
			
		return truth && sounds.get(index).getSoundLocation().equals(sound.getSoundName());
	}
	
	@SideOnly(Side.CLIENT)
	public static void reset()
	{
		allSounds.forEach(sound -> {
			sound.setDone();
		});
		
		allSounds.clear();
		sounds.clear();
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean stopSound(int index)
	{
		boolean truth = sounds.containsKey(index);
		
		if (truth)
		{
			sounds.get(index).setDone();
			sounds.remove(index);
		}
		
		return truth;
	}
	
	@SideOnly(Side.CLIENT)
	public static MovingSoundEX getActiveSound(int index)
	{	
		if (sounds.containsKey(index) && sounds.get(index).isDonePlaying())
			sounds.remove(index);
		return sounds.get(index);
	}
	
	@SideOnly(Side.CLIENT)
	public static MovingSoundEX playForcedSound(SoundEvent soundEvent, SoundCategory category, Object obj, float volume, float pitch, float range, boolean useY, boolean repeat)
	{
		MovingSoundEX sound = new MovingSoundEX(obj, soundEvent, SoundCategory.WEATHER, volume, pitch, range, useY);
		sound.setRepeat(repeat);
		Minecraft.getMinecraft().getSoundHandler().playSound(sound);
		allSounds.add(sound);
		return sound;
	}
	
	@SideOnly(Side.CLIENT)
	private static MovingSoundEX playSound(SoundEvent soundEvent, SoundCategory category, Object obj, int index, float volume, float pitch, float range, boolean useY, boolean repeat)
	{
		boolean truth = sounds.containsKey(index);
		if (truth && sounds.get(index).isDonePlaying())
		{
			sounds.remove(index);
			truth = false;
		}
		
		if (!truth)
		{
			MovingSoundEX sound = new MovingSoundEX(obj, soundEvent, SoundCategory.WEATHER, volume, pitch, range, useY);

			sound.setRepeat(repeat);
			FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
			allSounds.add(sound);
			sounds.put(index, sound);
			return sound;
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public static MovingSoundEX playSound(SoundEvent sound, SoundCategory category, int index, float volume, float pitch, boolean repeat)
	{
		return playSound(sound, category, null, index, volume, pitch, -1.0F, false, repeat);
	}
	
	@SideOnly(Side.CLIENT)
	public static MovingSoundEX play2DSound(SoundEvent sound, SoundCategory category, Object obj, int index, float volume, float pitch, float range, boolean repeat)
	{	
		return playSound(sound, category, obj, index, volume, pitch, range, false, repeat);
	}
	
	@SideOnly(Side.CLIENT)
	public static MovingSoundEX play3DSound(SoundEvent sound, SoundCategory category, int index, Object obj, float volume, float pitch, float range, boolean repeat)
	{	
		return playSound(sound, category, obj, index, volume, pitch, range, true, repeat);
	}
}
