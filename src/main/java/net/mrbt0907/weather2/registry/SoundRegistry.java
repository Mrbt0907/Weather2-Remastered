package net.mrbt0907.weather2.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.mrbt0907.weather2.Weather2;

public class SoundRegistry
{
	//Blocks and Items
	/*Sound that plays when a siren block is activated*/
	public static SoundEvent siren;
	/*Sound that plays when a siren block is activated, playing darude sandstorm*/
	public static SoundEvent sirenDarude;
	/*Sound that plays when an advanced siren block is activated, playing a constant sound*/
	public static SoundEvent sirenAdvanced;
	
	//Ambient
	/*Sound that plays when leaves are nearby and the wind is at least breezy*/
	public static SoundEvent leaves;
	/*Sound that plays when a lot of water is falling nearby*/
	public static SoundEvent waterfall;
	/*Sound that plays when the wind speed is 60MPH and higher*/
	public static SoundEvent windReallyFast;
	/*Sound that plays when the wind speed is 40MPH and higher*/
	public static SoundEvent windFast;
	/*Sound that plays when the wind speed is 20MPH and higher*/
	public static SoundEvent wind;
	/*Sound that plays when the wind speed is 10MPH and higher*/
	public static SoundEvent windSlow;
	/*Sound that plays when the wind speed is 5MPH and higher*/
	public static SoundEvent windBreeze;
	
	//Weather
	/*Sound that plays when a cyclone is damaging blocks nearby*/
	public static SoundEvent debris;
	/*Sound that plays when a strong sandstorm is nearby*/
	public static SoundEvent sandstormFast;
	/*Sound that plays when a sandstorm is nearby*/
	public static SoundEvent sandstorm;
	/*Sound that plays when a weak sandstorm is nearby*/
	public static SoundEvent sandstormSlow;

	public static void init()
	{
		siren = register("block.siren");
		sirenDarude = register("block.siren.darude");
		sirenAdvanced = register("block.siren.advanced");
		leaves = register("ambient.leaves");
		waterfall = register("ambient.waterfall");
		windFast = register("ambient.wind.fast");
		wind = register("ambient.wind");
		debris = register("weather.debris");
		sandstormFast = register("weather.sandstorm.fast");
		sandstorm = register("weather.sandstorm");
		sandstormSlow = register("weather.sandstorm.slow");
	}

	private static SoundEvent register(String path)
	{
		ResourceLocation id = new ResourceLocation(Weather2.OLD_MODID, path);
		SoundEvent sound = new SoundEvent(id).setRegistryName(id);
		ForgeRegistries.SOUND_EVENTS.register(sound);
		return sound;
	}
}
