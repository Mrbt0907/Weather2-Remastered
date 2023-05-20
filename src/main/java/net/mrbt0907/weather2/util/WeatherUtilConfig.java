package net.mrbt0907.weather2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.EZGuiAPI;
import net.mrbt0907.weather2.api.event.EventEZGuiData;
import net.mrbt0907.weather2.client.gui.GuiEZConfig;
import net.mrbt0907.weather2.config.*;
import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.util.CoroUtilFile;
import modconfig.ConfigMod;

public class WeatherUtilConfig
{
	public static final String version = "2.4";
	public static final Map<String, Integer> CLIENT_DEFAULTS = new HashMap<String, Integer>();
	public static final Map<String, Integer> SERVER_DEFAULTS = new HashMap<String, Integer>();
	private static List<Integer> weatherList = new ArrayList<Integer>();
	private static List<Integer> effectList = new ArrayList<Integer>();
	
	public static final Map<Integer, String> dimNames = new HashMap<Integer, String>();
	
	//actual data that gets written out to disk
	public static NBTTagCompound nbtServerData = new NBTTagCompound();
	public static NBTTagCompound nbtClientData = new NBTTagCompound();
	
	public static void processServerData(NBTTagCompound cache)
	{
		for(String key : cache.getKeySet())
		{
			int value = cache.getInteger(key);
			switch (key)
			{
				case EZGuiAPI.BB_GLOBAL:
					ConfigMisc.overcast_mode = value == 1;
					break;
				case EZGuiAPI.BB_RADAR:
					ConfigMisc.debug_mode_radar = value == 1;
					break;
				case EZGuiAPI.BC_ENABLE_TORNADO:
					ConfigStorm.disable_tornados = value == 0;
					break;
				case EZGuiAPI.BC_ENABLE_CYCLONE:
					ConfigStorm.disable_cyclones = value == 0;
					break;
				case EZGuiAPI.BC_ENABLE_SANDSTORM:
					ConfigSand.disable_sandstorms = value == 0;
					break;
				case EZGuiAPI.BC_FREQUENCY:
					switch(value)
					{
						case 0:
							ConfigFront.max_front_objects = 3;
							ConfigStorm.max_weather_objects = 30;
							ConfigStorm.storm_spawn_chance = 5;
							ConfigStorm.storm_spawn_delay = 2000;
							ConfigSand.sandstorm_spawn_1_in_x = 300;
							ConfigSand.sandstorm_spawn_delay = 8000;
							break;
						case 1:
							ConfigFront.max_front_objects = 3;
							ConfigStorm.max_weather_objects = 30;
							ConfigStorm.storm_spawn_chance = 10;
							ConfigStorm.storm_spawn_delay = 1250;
							ConfigSand.sandstorm_spawn_1_in_x = 200;
							ConfigSand.sandstorm_spawn_delay = 8000;
							break;
						case 2:
							ConfigFront.max_front_objects = 3;
							ConfigStorm.max_weather_objects = 30;
							ConfigStorm.storm_spawn_chance = 15;
							ConfigStorm.storm_spawn_delay = 1000;
							ConfigSand.sandstorm_spawn_1_in_x = 100;
							ConfigSand.sandstorm_spawn_delay = 4000;
							break;
						case 3:
							ConfigFront.max_front_objects = 3;
							ConfigStorm.max_weather_objects = 30;
							ConfigStorm.storm_spawn_chance = 30;
							ConfigStorm.storm_spawn_delay = 750;
							ConfigSand.sandstorm_spawn_1_in_x = 60;
							ConfigSand.sandstorm_spawn_delay = 2000;
							break;
						case 4:
							ConfigFront.max_front_objects = 4;
							ConfigStorm.max_weather_objects = 40;
							ConfigStorm.storm_spawn_chance = 35;
							ConfigStorm.storm_spawn_delay = 650;
							ConfigSand.sandstorm_spawn_1_in_x = 40;
							ConfigSand.sandstorm_spawn_delay = 1500;
							break;
						case 5:
							ConfigFront.max_front_objects = 5;
							ConfigStorm.max_weather_objects = 50;
							ConfigStorm.storm_spawn_chance = 50;
							ConfigStorm.storm_spawn_delay = 500;
							ConfigSand.sandstorm_spawn_1_in_x = 25;
							ConfigSand.sandstorm_spawn_delay = 1200;
							break;
						case 6:
							ConfigFront.max_front_objects = 5;
							ConfigStorm.max_weather_objects = 100;
							ConfigStorm.storm_spawn_chance = 100;
							ConfigStorm.storm_spawn_delay = 200;
							ConfigStorm.storms_aim_at_player = true;
							ConfigStorm.storm_aim_accuracy_in_angle = 0;
							ConfigSand.sandstorm_spawn_1_in_x = 3;
							ConfigSand.sandstorm_spawn_delay = 1000;
							break;
					}
					break;
				case EZGuiAPI.BC_GRAB_BLOCK:
					ConfigGrab.grab_blocks = value == 1;
					break;
				case EZGuiAPI.BC_GRAB_ITEM:
					ConfigGrab.grab_items = value == 1;
					break;
				case EZGuiAPI.BC_GRAB_MOB:
					ConfigGrab.grab_villagers = value == 1;
					ConfigGrab.grab_animals = value == 1;
					ConfigGrab.grab_mobs = value == 1;
					break;
				case EZGuiAPI.BC_GRAB_PLAYER:
					ConfigGrab.grab_players = value == 1;
					break;
				case EZGuiAPI.BC_STORM_PER_PLAYER:
					ConfigStorm.enable_spawn_per_player = value == 0;
					ConfigSand.enable_global_rates_for_sandstorms = value == 0;
					break;
			}
			
			if (!key.equals("dimData"))
			{
				EventEZGuiData event = new EventEZGuiData(key, nbtClientData.getInteger(key), value);
				MinecraftForge.EVENT_BUS.post(event);
			}
		}
		
		if (cache.hasKey("dimData"))
		{
			for(String key : cache.getCompoundTag("dimData").getKeySet())
			{
				if (key.contains("dimb_"))
				{
					int keyN = Integer.parseInt(key.replaceFirst("dimb_", ""));
					
					if (cache.getCompoundTag("dimData").getInteger(key) == 1)
						weatherList.add(keyN);
					else if (weatherList.contains(keyN))
						weatherList.remove(weatherList.indexOf(keyN));
				}
				else if (key.contains("dimc_"))
				{
					int keyN = Integer.parseInt(key.replaceFirst("dimc_", ""));
					
					if (cache.getCompoundTag("dimData").getInteger(key) == 1)
						effectList.add(keyN);
					else if (effectList.contains(keyN))
						effectList.remove(effectList.indexOf(keyN));
				}
			}
			String list = "";
			for (int dimension : weatherList)
				if (list.length() == 0)
					list = dimension + "";
				else
					list += ", " + dimension;
			ConfigMisc.dimensions_weather = list;

			list = "";
			for (int dimension : effectList)
				if (list.length() == 0)
					list = dimension + "";
				else
					list += ", " + dimension;
			ConfigMisc.dimensions_effects = list;
			refreshDimensionRules();
		}
		nbtSaveDataServer();
		ConfigMod.forceSaveAllFilesFromRuntimeSettings();
	}
	
	public static void processClientData(NBTTagCompound cache)
	{
		for(String key : cache.getKeySet())
		{
			int value = cache.getInteger(key);
			switch (key)
			{
				case EZGuiAPI.BA_CLOUD:
					switch(value)
					{
						case 0:
							ConfigParticle.max_cloud_coverage_perc = 0.0D;
							ConfigParticle.min_cloud_coverage_perc = 0.0D;
							ConfigParticle.cloud_particle_delay = 666999;
							break;
						case 1:
							ConfigParticle.max_cloud_coverage_perc = 15.0D;
							ConfigParticle.min_cloud_coverage_perc = 0.0D;
							ConfigParticle.cloud_particle_delay = 10;
							break;
						case 2:
							ConfigParticle.max_cloud_coverage_perc = 25.0D;
							ConfigParticle.min_cloud_coverage_perc = 10.0D;
							ConfigParticle.cloud_particle_delay = 5;
							break;
						case 3:
							ConfigParticle.max_cloud_coverage_perc = 50.0D;
							ConfigParticle.min_cloud_coverage_perc = 20.0D;
							ConfigParticle.cloud_particle_delay = 3;
							break;
						case 4:
							ConfigParticle.max_cloud_coverage_perc = 80.0D;
							ConfigParticle.min_cloud_coverage_perc = 250.0D;
							ConfigParticle.cloud_particle_delay = 2;
							break;
						case 5:
							ConfigParticle.max_cloud_coverage_perc = 100.0D;
							ConfigParticle.min_cloud_coverage_perc = 50.0D;
							ConfigParticle.cloud_particle_delay = 1;
							break;
						case 6:
							ConfigParticle.max_cloud_coverage_perc = 200.0D;
							ConfigParticle.min_cloud_coverage_perc = 50.0D;
							ConfigParticle.cloud_particle_delay = 0;
							break;
					}
					break;
				case EZGuiAPI.BA_FUNNEL:
					switch(value)
					{
						case 0:
							ConfigParticle.sandstorm_debris_particle_rate = 0.0D;
							ConfigParticle.sandstorm_dust_particle_rate = 0.0D;
							ConfigParticle.funnel_particle_delay = 666999;
							ConfigParticle.ground_debris_particle_delay = 666999;
							break;
						case 1:
							ConfigParticle.sandstorm_debris_particle_rate = 0.025D;
							ConfigParticle.sandstorm_dust_particle_rate = 0.05D;
							ConfigParticle.funnel_particle_delay = 45;
							ConfigParticle.ground_debris_particle_delay = 20;
							break;
						case 2:
							ConfigParticle.sandstorm_debris_particle_rate = 0.05D;
							ConfigParticle.sandstorm_dust_particle_rate = 0.1D;
							ConfigParticle.funnel_particle_delay = 20;
							ConfigParticle.ground_debris_particle_delay = 10;
							break;
						case 3:
							ConfigParticle.sandstorm_debris_particle_rate = 0.1D;
							ConfigParticle.sandstorm_dust_particle_rate = 0.15D;
							ConfigParticle.funnel_particle_delay = 10;
							ConfigParticle.ground_debris_particle_delay = 5;
							break;
						case 4:
							ConfigParticle.sandstorm_debris_particle_rate = 0.15D;
							ConfigParticle.sandstorm_dust_particle_rate = 0.4D;
							ConfigParticle.funnel_particle_delay = 5;
							ConfigParticle.ground_debris_particle_delay = 5;
							break;
						case 5:
							ConfigParticle.sandstorm_debris_particle_rate = 0.25D;
							ConfigParticle.sandstorm_dust_particle_rate = 0.6D;
							ConfigParticle.funnel_particle_delay = 2;
							ConfigParticle.ground_debris_particle_delay = 3;
								break;
						case 6:
							ConfigParticle.sandstorm_debris_particle_rate = 0.5D;
							ConfigParticle.sandstorm_dust_particle_rate = 1.0D;
							ConfigParticle.funnel_particle_delay = 0;
							ConfigParticle.ground_debris_particle_delay = 0;
							break;
					}
					break;
				case EZGuiAPI.BA_PRECIPITATION:
					ConfigParticle.particle_multiplier = 1.0D;
					switch(value)
					{
						case 0:
							ConfigParticle.enable_precipitation = true;
							ConfigParticle.enable_precipitation_splash = false;
							ConfigParticle.enable_heavy_precipitation = false;
							ConfigParticle.use_vanilla_rain_and_thunder = false;
							ConfigParticle.precipitation_particle_rate = 0.00000000000001D;
							ConfigParticle.enable_distant_downfall = false;
							ConfigParticle.distant_downfall_particle_rate = 0.0F;
							break;
						case 1:
							ConfigParticle.enable_precipitation = false;
							ConfigParticle.enable_precipitation_splash = false;
							ConfigParticle.enable_heavy_precipitation = false;
							ConfigParticle.use_vanilla_rain_and_thunder = true;
							ConfigParticle.precipitation_particle_rate = 0.05D;
							ConfigParticle.enable_distant_downfall = false;
							ConfigParticle.distant_downfall_particle_rate = 0.4F;
							break;
						case 2:
							ConfigParticle.enable_precipitation = true;
							ConfigParticle.enable_precipitation_splash = false;
							ConfigParticle.enable_heavy_precipitation = false;
							ConfigParticle.use_vanilla_rain_and_thunder = false;
							ConfigParticle.precipitation_particle_rate = 0.2D;
							ConfigParticle.enable_distant_downfall = false;
							ConfigParticle.distant_downfall_particle_rate = 0.2F;
							break;
						case 3:
							ConfigParticle.enable_precipitation = true;
							ConfigParticle.enable_precipitation_splash = true;
							ConfigParticle.enable_heavy_precipitation = false;
							ConfigParticle.use_vanilla_rain_and_thunder = false;
							ConfigParticle.precipitation_particle_rate = 0.40D;
							ConfigParticle.enable_distant_downfall = false;
							ConfigParticle.distant_downfall_particle_rate = 0.2F;
							break;
						case 4:
							ConfigParticle.enable_precipitation = true;
							ConfigParticle.enable_precipitation_splash = true;
							ConfigParticle.enable_heavy_precipitation = true;
							ConfigParticle.use_vanilla_rain_and_thunder = false;
							ConfigParticle.precipitation_particle_rate = 0.65D;
							ConfigParticle.enable_distant_downfall = true;
							ConfigParticle.distant_downfall_particle_rate = 0.4F;
							break;
						case 5:
							ConfigParticle.enable_precipitation = true;
							ConfigParticle.enable_precipitation_splash = true;
							ConfigParticle.enable_heavy_precipitation = true;
							ConfigParticle.use_vanilla_rain_and_thunder = false;
							ConfigParticle.precipitation_particle_rate = 1.0D;
							ConfigParticle.enable_distant_downfall = true;
							ConfigParticle.distant_downfall_particle_rate = 0.6F;
							break;
						case 6:
							ConfigParticle.enable_precipitation = true;
							ConfigParticle.enable_precipitation_splash = true;
							ConfigParticle.enable_heavy_precipitation = true;
							ConfigParticle.use_vanilla_rain_and_thunder = false;
							ConfigParticle.precipitation_particle_rate = 1.4D;
							ConfigParticle.enable_distant_downfall = true;
							ConfigParticle.distant_downfall_particle_rate = 1.0F;
							break;
						case 7:
							ConfigParticle.enable_precipitation = true;
							ConfigParticle.enable_precipitation_splash = true;
							ConfigParticle.enable_heavy_precipitation = true;
							ConfigParticle.use_vanilla_rain_and_thunder = false;
							ConfigParticle.precipitation_particle_rate = 2.0D;
							ConfigParticle.enable_distant_downfall = true;
							ConfigParticle.distant_downfall_particle_rate = 2.0F;
							break;
					}
					break;
				case EZGuiAPI.BA_EFFECT:
					ConfigParticle.particle_multiplier = 0.5D;
					switch(value)
					{
						case 0:
							ConfigParticle.enable_falling_leaves = false;
							ConfigParticle.enable_fire_particle = false;
							ConfigParticle.enable_waterfall_splash = false;
							ConfigParticle.enable_wind_particle = false;
							ConfigParticle.wind_particle_rate = 0.0D;
							ConfigParticle.ambient_particle_rate = 0.0D;
							break;
						case 1:
							ConfigParticle.enable_falling_leaves = true;
							ConfigParticle.enable_fire_particle = false;
							ConfigParticle.enable_waterfall_splash = false;
							ConfigParticle.enable_wind_particle = false;
							ConfigParticle.wind_particle_rate = 0.0D;
							ConfigParticle.ambient_particle_rate = 0.1D;
							break;
						case 2:
							ConfigParticle.enable_falling_leaves = true;
							ConfigParticle.enable_fire_particle = true;
							ConfigParticle.enable_waterfall_splash = false;
							ConfigParticle.enable_wind_particle = false;
							ConfigParticle.wind_particle_rate = 0.0D;
							ConfigParticle.ambient_particle_rate = 0.2D;
							break;
						case 3:
							ConfigParticle.enable_falling_leaves = true;
							ConfigParticle.enable_fire_particle = true;
							ConfigParticle.enable_waterfall_splash = true;
							ConfigParticle.enable_wind_particle = true;
							ConfigParticle.wind_particle_rate = 0.1D;
							ConfigParticle.ambient_particle_rate = 0.35D;
							break;
						case 4:
							ConfigParticle.enable_falling_leaves = true;
							ConfigParticle.enable_fire_particle = true;
							ConfigParticle.enable_waterfall_splash = true;
							ConfigParticle.enable_wind_particle = true;
							ConfigParticle.wind_particle_rate = 0.2D;
							ConfigParticle.ambient_particle_rate = 0.6D;
							break;
						case 5:
							ConfigParticle.enable_falling_leaves = true;
							ConfigParticle.enable_fire_particle = true;
							ConfigParticle.enable_waterfall_splash = true;
							ConfigParticle.enable_wind_particle = true;
							ConfigParticle.wind_particle_rate = 0.25D;
							ConfigParticle.ambient_particle_rate = 1.0D;
							break;
						case 6:
							ConfigParticle.enable_falling_leaves = true;
							ConfigParticle.enable_fire_particle = true;
							ConfigParticle.enable_waterfall_splash = true;
							ConfigParticle.enable_wind_particle = true;
							ConfigParticle.wind_particle_rate = 0.3D;
							ConfigParticle.ambient_particle_rate = 2.0D;
							break;
						case 7:
							ConfigParticle.enable_falling_leaves = true;
							ConfigParticle.enable_fire_particle = true;
							ConfigParticle.enable_waterfall_splash = true;
							ConfigParticle.enable_wind_particle = true;
							ConfigParticle.wind_particle_rate = 0.5D;
							ConfigParticle.ambient_particle_rate = 4.0D;
							break;
					}
					break;
				case EZGuiAPI.BA_EF:
					ConfigStorm.enable_ef_scale = value == 1;
					break;
				case EZGuiAPI.BA_SHADER:
					switch(value)
					{
						case 0:
							ConfigCoroUtil.particleShaders = true;
							ConfigCoroUtil.useEntityRenderHookForShaders = true;
							ConfigMisc.proxy_render_override = true;
							break;
						case 1:
							ConfigCoroUtil.particleShaders = false;
							ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = true;
							break;
						case 2:
							ConfigCoroUtil.particleShaders = false;
							ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = true;
							break;
						case 3:
							ConfigCoroUtil.particleShaders = true;
							ConfigCoroUtil.useEntityRenderHookForShaders = true;
							ConfigMisc.proxy_render_override = true;
							break;
						case 4:
							ConfigCoroUtil.particleShaders = true;
							ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 5:
							ConfigCoroUtil.particleShaders = true;
							ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 6:
							ConfigCoroUtil.particleShaders = true;
							ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = true;
							break;
						case 7:
							ConfigCoroUtil.particleShaders = true;
							ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 8:
							ConfigCoroUtil.particleShaders = true;
							ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 9:
							ConfigCoroUtil.particleShaders = true;
							ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 10:
							ConfigCoroUtil.particleShaders = true;
							ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
						case 11:
							ConfigCoroUtil.particleShaders = false;
							ConfigCoroUtil.useEntityRenderHookForShaders = false;
							ConfigMisc.proxy_render_override = false;
							break;
					}
					break;
				case EZGuiAPI.BA_FOLIAGE:
					ConfigCoroUtil.foliageShaders = value == 1;
					break;

				case EZGuiAPI.BA_RENDER_DISTANCE:
					switch(value)
					{
						case 0:
							ConfigParticle.enable_extended_render_distance = false;
							ConfigParticle.extended_render_distance = 128.0D;
							ConfigParticle.max_particles = 3000;
							break;
						case 1:
							ConfigParticle.enable_extended_render_distance = true;
							ConfigParticle.extended_render_distance = 128.0D;
							ConfigParticle.max_particles = 3000;
							break;
						case 2:
							ConfigParticle.enable_extended_render_distance = true;
							ConfigParticle.extended_render_distance = 256.0D;
							ConfigParticle.max_particles = 4000;
							break;
						case 3:
							ConfigParticle.enable_extended_render_distance = true;
							ConfigParticle.extended_render_distance = 370.0D;
							ConfigParticle.max_particles = 5000;
							break;
						case 4:
							ConfigParticle.enable_extended_render_distance = true;
							ConfigParticle.extended_render_distance = 512.0D;
							ConfigParticle.max_particles = -1;
							break;
						case 5:
							ConfigParticle.enable_extended_render_distance = true;
							ConfigParticle.extended_render_distance = 750.0D;
							ConfigParticle.max_particles = -1;
							break;
						case 6:
							ConfigParticle.enable_extended_render_distance = true;
							ConfigParticle.extended_render_distance = 1028.0D;
							ConfigParticle.max_particles = -1;
							break;
						case 7:
							ConfigParticle.enable_extended_render_distance = true;
							ConfigParticle.extended_render_distance = 2300.0D;
							ConfigParticle.max_particles = -1;
							break;
					}
				}
			
				if (!key.equals("dimData"))
				{
					EventEZGuiData event = new EventEZGuiData(key, nbtClientData.getInteger(key), value);
					MinecraftForge.EVENT_BUS.post(event);
				}
			}
		
		nbtSaveDataClient();
		ConfigMod.forceSaveAllFilesFromRuntimeSettings();
	}
	
	/**Used to get needed information from a client to add to the server data*/
	public static void nbtReceiveServer(NBTTagCompound parNBT)
	{
		NBTTagCompound cache = new NBTTagCompound();
		String newKey;
		for (String key : parNBT.getKeySet())
			if (key.matches("^" + GuiEZConfig.PREFIX + ".+"))
			{
				newKey = key.replaceFirst("^" + GuiEZConfig.PREFIX, "");
				cache.setInteger(newKey, parNBT.getInteger(key));
				nbtServerData.setInteger(newKey, parNBT.getInteger(key));
			}
		
		//also add dimension feature config, its iterated over
		cache.setTag("dimData", parNBT.getCompoundTag("dimData"));
		nbtServerData.setTag("dimData", parNBT.getCompoundTag("dimData"));
		
		Weather2.debug("Received server data from a client: " + parNBT);
		processServerData(cache);
	}
	
	/**Used to get needed information from server and client to add to the client data. Must have command. <br>0 - From Client<br>1 - From Server*/
	public static void nbtReceiveClient(NBTTagCompound parNBT)
	{
		if (parNBT.hasKey("server"))
		{
			String newKey;
			if (parNBT.getInteger("server") == 1)
			{
				nbtClientData.setBoolean("op", parNBT.getBoolean("op"));
				for (String key : parNBT.getKeySet())
					if (key.matches("^" + GuiEZConfig.PREFIX + ".+"))
					{
						newKey = key.replaceFirst("^" + GuiEZConfig.PREFIX, "");
						nbtServerData.setInteger(newKey, parNBT.getInteger(key));
					}
				NBTTagCompound dimensions = parNBT.getCompoundTag("dimData");
				if (dimensions != null)
				{
					weatherList.clear();
					effectList.clear();
					
					for(String name : dimensions.getKeySet())
						if (name.contains("dima_"))
							dimNames.put(Integer.parseInt(name.replaceFirst("dima_", "")), dimensions.getString(name));
						else if (name.contains("dimb_"))
							weatherList.add(Integer.parseInt(name.replaceFirst("dimb_", "")));
						else if (name.contains("dimc_"))
							effectList.add(Integer.parseInt(name.replaceFirst("dimc_", "")));;
				}
				Weather2.debug("Received server data from the server: " + parNBT);
			}
			else
			{
				NBTTagCompound cache = new NBTTagCompound();
				for (String key : parNBT.getKeySet())
					if (key.matches("^" + GuiEZConfig.PREFIX + ".+"))
					{
						newKey = key.replaceFirst("^" + GuiEZConfig.PREFIX, "");
						cache.setInteger(newKey, parNBT.getInteger(key));
						nbtClientData.setInteger(newKey, parNBT.getInteger(key));
					}
				
				Weather2.debug("Received client data from self: " + parNBT);
				processClientData(cache);
			}
		}
	}
	
	public static boolean isOp()
	{
		return nbtClientData.getBoolean("op");
	}
	
	public static void nbtSaveDataClient()
	{
		nbtWriteNBTToDisk(nbtClientData, true);
	}
	
	public static void nbtSaveDataServer()
	{
		nbtWriteNBTToDisk(nbtServerData, false);
	}
	
	public static void loadNBT()
	{
		EZGuiAPI.refreshOptions();
		nbtClientData = nbtReadNBTFromDisk(true);
		nbtServerData = nbtReadNBTFromDisk(false);
		checkVersion();
		TriMapEx<String, List<String>, Integer> options = EZGuiAPI.getOptions();
		Map<String, Integer> optionCategories = EZGuiAPI.getOptionCategories();
		String index;
		for(Entry<String, Integer> entry : optionCategories.entrySet())
		{
			index = entry.getKey();
			if (entry.getValue() == 0)
				CLIENT_DEFAULTS.put(index, options.getB(index));
			else
				SERVER_DEFAULTS.put(index, options.getB(index));
		}
		
		for (String key : CLIENT_DEFAULTS.keySet())
			if (!nbtClientData.hasKey(key))
				nbtClientData.setInteger(key, CLIENT_DEFAULTS.get(key));
		for (String key : SERVER_DEFAULTS.keySet())
			if (!nbtServerData.hasKey(key))
				nbtServerData.setInteger(key, SERVER_DEFAULTS.get(key));
	}
	
	public static int getConfigValue(String buttonID)
	{
		if (nbtClientData.hasKey(buttonID))
			return nbtClientData.getInteger(buttonID);
		else if (nbtServerData.hasKey(buttonID))
			return nbtServerData.getInteger(buttonID);
		else
			return 0;
	}
	
	public static List<Integer> parseList(String intList)
	{
		String[] arrStr = intList.split("[\\s\\,]+");
		List<Integer> arrInt = new ArrayList<Integer>();
		for (int i = 0; i < arrStr.length; i++)
		{
			try {arrInt.add(Integer.parseInt(arrStr[i]));}
			catch (Exception ex) {Weather2.debug("Entry was not an integer: " + arrStr[i]);}
		}
		return arrInt;
	}
	
	private static void checkVersion()
	{
		if (!nbtServerData.hasKey("version") || !nbtServerData.getString("version").equalsIgnoreCase(version))
		{
			Weather2.debug("Detected old EZ server data, reseting everything to default...");
			nbtServerData = new NBTTagCompound();
			nbtServerData.setString("version", version);
			nbtSaveDataServer();
		}
		if (!nbtClientData.hasKey("version") || !nbtClientData.getString("version").equalsIgnoreCase(version))
		{
			Weather2.debug("Detected old EZ client data, reseting everything to default...");
			nbtClientData = new NBTTagCompound();
			nbtClientData.setString("version", version);
			nbtSaveDataClient();
		}
	}
	
	public static void nbtWriteNBTToDisk(NBTTagCompound parData, boolean saveForClient)
	{
		String fileURL = null;
		
		if (saveForClient)
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + Weather2.MODID + File.separator + "EZGUIConfigClientData.dat";
		else
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + Weather2.MODID + File.separator + "EZGUIConfigServerData.dat";
		
		try
		{
			FileOutputStream fos = new FileOutputStream(fileURL);
	    	CompressedStreamTools.writeCompressed(parData, fos);
	    	fos.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Weather2.debug("Error writing Weather2 EZ GUI data, unable to save data");
		}
	}
	
	public static NBTTagCompound nbtReadNBTFromDisk(boolean loadForClient)
	{
		NBTTagCompound data = new NBTTagCompound();
		String fileURL = null;
		if (loadForClient)
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + Weather2.MODID + File.separator + "EZGUIConfigClientData.dat";
		else
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + Weather2.MODID + File.separator + "EZGUIConfigServerData.dat";
		
		try
		{
			if ((new File(fileURL)).exists())
				data = CompressedStreamTools.readCompressed(new FileInputStream(fileURL));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Weather2.debug("Error reading Weather2 EZ GUI data, resetting data to default...");
		}
		return data;
	}

	public static void setOvercastModeServerSide(boolean val)
	{
		nbtSaveDataServer();
	}
	
	public static boolean isWeatherEnabled(int dimension)
	{
		return weatherList.contains(dimension);
	}
	
	public static boolean isEffectsEnabled(int dimension)
	{
		return effectList.contains(dimension);
	}
	
	public static void refreshDimensionRules()
	{
		weatherList = parseList(ConfigMisc.dimensions_weather);
		effectList = parseList(ConfigMisc.dimensions_effects);
		nbtServerData.setTag("dimData", new NBTTagCompound());
		String list = "Dimension Rules have been refreshed\nWeather:";
		for (int dim : weatherList)
		{
			nbtServerData.getCompoundTag("dimData").setInteger("dimb_" + dim, 1);
			list += " " + dim;
		}
		list += "\nEffects:";
		for (int dim : effectList)
		{
			nbtServerData.getCompoundTag("dimData").setInteger("dimc_" + dim, 1);
			list += " " + dim;
		}
		Weather2.debug(list);
	}
}
