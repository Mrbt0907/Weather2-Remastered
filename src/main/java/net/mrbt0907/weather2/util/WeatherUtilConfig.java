package net.mrbt0907.weather2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.client.gui.GuiEZConfig;
import net.mrbt0907.weather2.config.*;
import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.util.CoroUtilFile;
import modconfig.ConfigMod;

public class WeatherUtilConfig
{
	public static final String version = "2.2";
	public static final Map<Integer, Integer> DEFAULTS = new HashMap<Integer, Integer>();
	private static List<Integer> weatherList = new ArrayList<Integer>();
	private static List<Integer> effectList = new ArrayList<Integer>();
	
	public static final Map<Integer, String> dimNames = new HashMap<Integer, String>();
	
	//actual data that gets written out to disk
	public static NBTTagCompound nbtServerData = new NBTTagCompound();
	public static NBTTagCompound nbtClientData = new NBTTagCompound();
	
	public static void processServerData(NBTTagCompound cache)
	{
		for(int i = GuiEZConfig.BA_CLOUD; i < GuiEZConfig.BD_MIN; i++)
			if(cache.hasKey(GuiEZConfig.prefix + i))
			{
				int value = cache.getInteger(GuiEZConfig.prefix + i);
				switch (i)
				{
					case GuiEZConfig.BB_GLOBAL:
						ConfigMisc.overcast_mode = value == 1;
						break;
					case GuiEZConfig.BC_ENABLE_TORNADO:
						ConfigStorm.disable_tornados = value == 0;
						break;
					case GuiEZConfig.BC_ENABLE_CYCLONE:
						ConfigStorm.disable_cyclones = value == 0;
						break;
					case GuiEZConfig.BC_ENABLE_SANDSTORM:
						ConfigSand.disable_sandstorms = value == 1;
						break;
					case GuiEZConfig.BC_FREQUENCY:
						switch(value)
						{
							case 0:
								ConfigStorm.max_weather_objects = 10;
								ConfigStorm.storm_spawn_chance = 25;
								ConfigStorm.storm_spawn_delay = 240000;
								ConfigStorm.storm_developing_above_land_10_in_x = -1;
								ConfigStorm.storm_developing_above_ocean_10_in_x = 600;
								ConfigStorm.storms_aim_at_player = false;
								ConfigStorm.storm_aim_accuracy_in_angle = 180;
								ConfigSand.sandstorm_spawn_1_in_x = 30;
								ConfigSand.sandstorm_spawn_delay = 240000;
								break;
							case 1:
								ConfigStorm.max_weather_objects = 10;
								ConfigStorm.storm_spawn_chance = 25;
								ConfigStorm.storm_spawn_delay = 120000;
								ConfigStorm.storm_developing_above_land_10_in_x = -1;
								ConfigStorm.storm_developing_above_ocean_10_in_x = 500;
								ConfigStorm.storms_aim_at_player = false;
								ConfigStorm.storm_aim_accuracy_in_angle = 180;
								ConfigSand.sandstorm_spawn_1_in_x = 30;
								ConfigSand.sandstorm_spawn_delay = 120000;
								break;
							case 2:
								ConfigStorm.max_weather_objects = 15;
								ConfigStorm.storm_spawn_chance = 25;
								ConfigStorm.storm_spawn_delay = 48000;
								ConfigStorm.storm_developing_above_land_10_in_x = -1;
								ConfigStorm.storm_developing_above_ocean_10_in_x = 450;
								ConfigStorm.storms_aim_at_player = false;
								ConfigStorm.storm_aim_accuracy_in_angle = 180;
								ConfigSand.sandstorm_spawn_1_in_x = 30;
								ConfigSand.sandstorm_spawn_delay = 48000;
								break;
							case 3:
								ConfigStorm.max_weather_objects = 20;
								ConfigStorm.storm_spawn_chance = 25;
								ConfigStorm.storm_spawn_delay = 24000;
								ConfigStorm.storm_developing_above_land_10_in_x = -1;
								ConfigStorm.storm_developing_above_ocean_10_in_x = 300;
								ConfigStorm.storms_aim_at_player = false;
								ConfigStorm.storm_aim_accuracy_in_angle = 180;
								ConfigSand.sandstorm_spawn_1_in_x = 30;
								ConfigSand.sandstorm_spawn_delay = 24000;
								break;
							case 4:
								ConfigStorm.max_weather_objects = 25;
								ConfigStorm.storm_spawn_chance = 25;
								ConfigStorm.storm_spawn_delay = 12000;
								ConfigStorm.storm_developing_above_land_10_in_x = 600;
								ConfigStorm.storm_developing_above_ocean_10_in_x = 256;
								ConfigStorm.storms_aim_at_player = true;
								ConfigStorm.storm_aim_accuracy_in_angle = 90;
								ConfigSand.sandstorm_spawn_1_in_x = 30;
								ConfigSand.sandstorm_spawn_delay = 12000;
								break;
							case 5:
								ConfigStorm.max_weather_objects = 50;
								ConfigStorm.storm_spawn_chance = 15;
								ConfigStorm.storm_spawn_delay = 5000;
								ConfigStorm.storm_developing_above_land_10_in_x = 256;
								ConfigStorm.storm_developing_above_ocean_10_in_x = 125;
								ConfigStorm.storms_aim_at_player = true;
								ConfigStorm.storm_aim_accuracy_in_angle = 45;
								ConfigSand.sandstorm_spawn_1_in_x = 25;
								ConfigSand.sandstorm_spawn_delay = 5000;
								break;
							case 6:
								ConfigStorm.max_weather_objects = 100;
								ConfigStorm.storm_spawn_chance = 2;
								ConfigStorm.storm_spawn_delay = 1000;
								ConfigStorm.storm_developing_above_land_10_in_x = 100;
								ConfigStorm.storm_developing_above_ocean_10_in_x = 50;
								ConfigStorm.storms_aim_at_player = true;
								ConfigStorm.storm_aim_accuracy_in_angle = 0;
								ConfigSand.sandstorm_spawn_1_in_x = 3;
								ConfigSand.sandstorm_spawn_delay = 1000;
								break;
						}
						break;
					case GuiEZConfig.BC_GRAB_BLOCK:
						ConfigGrab.grab_blocks = value == 1;
						break;
					case GuiEZConfig.BC_GRAB_ITEM:
						ConfigGrab.grab_items = value == 1;
						break;
					case GuiEZConfig.BC_GRAB_MOB:
						ConfigGrab.grab_villagers = value == 1;
						ConfigGrab.grab_animals = value == 1;
						ConfigGrab.grab_mobs = value == 1;
						break;
					case GuiEZConfig.BC_GRAB_PLAYER:
						ConfigGrab.grab_players = value == 1;
						break;
					case GuiEZConfig.BC_STORM_PER_PLAYER:
						ConfigStorm.enable_spawn_per_player = value == 0;
						ConfigSand.enable_global_rates_for_sandstorms = value == 0;
						break;
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
		for(int i = GuiEZConfig.BA_CLOUD; i < GuiEZConfig.BD_MIN; i++)
			if(cache.hasKey(GuiEZConfig.prefix + i))
			{
				int value = cache.getInteger(GuiEZConfig.prefix + i);
				switch (i)
				{
					case GuiEZConfig.BA_CLOUD:
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
								ConfigParticle.cloud_particle_delay = 45;
								break;
							case 2:
								ConfigParticle.max_cloud_coverage_perc = 25.0D;
								ConfigParticle.min_cloud_coverage_perc = 10.0D;
								ConfigParticle.cloud_particle_delay = 15;
								break;
							case 3:
								ConfigParticle.max_cloud_coverage_perc = 50.0D;
								ConfigParticle.min_cloud_coverage_perc = 20.0D;
								ConfigParticle.cloud_particle_delay = 5;
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
					case GuiEZConfig.BA_FUNNEL:
						switch(value)
						{
							case 0:
								ConfigParticle.sandstorm_debris_particle_rate = 0.0D;
								ConfigParticle.sandstorm_dust_particle_rate = 0.0D;
								ConfigParticle.funnel_particle_delay = 666999;
								break;
							case 1:
								ConfigParticle.sandstorm_debris_particle_rate = 0.025D;
								ConfigParticle.sandstorm_dust_particle_rate = 0.05D;
								ConfigParticle.funnel_particle_delay = 45;
								break;
							case 2:
								ConfigParticle.sandstorm_debris_particle_rate = 0.05D;
								ConfigParticle.sandstorm_dust_particle_rate = 0.1D;
								ConfigParticle.funnel_particle_delay = 15;
								break;
							case 3:
								ConfigParticle.sandstorm_debris_particle_rate = 0.1D;
								ConfigParticle.sandstorm_dust_particle_rate = 0.15D;
								ConfigParticle.funnel_particle_delay = 5;
								break;
							case 4:
								ConfigParticle.sandstorm_debris_particle_rate = 0.15D;
								ConfigParticle.sandstorm_dust_particle_rate = 0.4D;
								ConfigParticle.funnel_particle_delay = 2;
								break;
							case 5:
								ConfigParticle.sandstorm_debris_particle_rate = 0.25D;
								ConfigParticle.sandstorm_dust_particle_rate = 0.6D;
								ConfigParticle.funnel_particle_delay = 1;
								break;
							case 6:
								ConfigParticle.sandstorm_debris_particle_rate = 0.5D;
								ConfigParticle.sandstorm_dust_particle_rate = 1.0D;
								ConfigParticle.funnel_particle_delay = 0;
								break;
						}
						break;
					case GuiEZConfig.BA_PRECIPITATION:
						ConfigParticle.particle_multiplier = 0.5D;
						switch(value)
						{
							case 0:
								ConfigParticle.enable_precipitation = true;
								ConfigParticle.enable_precipitation_splash = false;
								ConfigParticle.enable_heavy_precipitation = false;
								ConfigParticle.use_vanilla_rain_and_thunder = false;
								ConfigParticle.precipitation_particle_rate = 0.00000000000001D;
								break;
							case 1:
								ConfigParticle.enable_precipitation = false;
								ConfigParticle.enable_precipitation_splash = false;
								ConfigParticle.enable_heavy_precipitation = false;
								ConfigParticle.use_vanilla_rain_and_thunder = true;
								ConfigParticle.precipitation_particle_rate = 0.05D;
								break;
							case 2:
								ConfigParticle.enable_precipitation = true;
								ConfigParticle.enable_precipitation_splash = false;
								ConfigParticle.enable_heavy_precipitation = false;
								ConfigParticle.use_vanilla_rain_and_thunder = false;
								ConfigParticle.precipitation_particle_rate = 0.2D;
								break;
							case 3:
								ConfigParticle.enable_precipitation = true;
								ConfigParticle.enable_precipitation_splash = true;
								ConfigParticle.enable_heavy_precipitation = false;
								ConfigParticle.use_vanilla_rain_and_thunder = false;
								ConfigParticle.precipitation_particle_rate = 0.40D;
								break;
							case 4:
								ConfigParticle.enable_precipitation = true;
								ConfigParticle.enable_precipitation_splash = true;
								ConfigParticle.enable_heavy_precipitation = true;
								ConfigParticle.use_vanilla_rain_and_thunder = false;
								ConfigParticle.precipitation_particle_rate = 0.65D;
								break;
							case 5:
								ConfigParticle.enable_precipitation = true;
								ConfigParticle.enable_precipitation_splash = true;
								ConfigParticle.enable_heavy_precipitation = true;
								ConfigParticle.use_vanilla_rain_and_thunder = false;
								ConfigParticle.precipitation_particle_rate = 1.0D;
								break;
							case 6:
								ConfigParticle.enable_precipitation = true;
								ConfigParticle.enable_precipitation_splash = true;
								ConfigParticle.enable_heavy_precipitation = true;
								ConfigParticle.use_vanilla_rain_and_thunder = false;
								ConfigParticle.precipitation_particle_rate = 1.6D;
								break;
							case 7:
								ConfigParticle.enable_precipitation = true;
								ConfigParticle.enable_precipitation_splash = true;
								ConfigParticle.enable_heavy_precipitation = true;
								ConfigParticle.use_vanilla_rain_and_thunder = false;
								ConfigParticle.precipitation_particle_rate = 4.0D;
								break;
						}
						break;
					case GuiEZConfig.BA_EFFECT:
						ConfigParticle.particle_multiplier = 0.5D;
						switch(value)
						{
							case 0:
								ConfigParticle.enable_falling_leaves = false;
								ConfigParticle.enable_fire_particle = false;
								ConfigParticle.enable_waterfall_splash = false;
								ConfigParticle.ambient_particle_rate = 0.0D;
								break;
							case 1:
								ConfigParticle.enable_falling_leaves = true;
								ConfigParticle.enable_fire_particle = false;
								ConfigParticle.enable_waterfall_splash = false;
								ConfigParticle.ambient_particle_rate = 0.1D;
								break;
							case 2:
								ConfigParticle.enable_falling_leaves = true;
								ConfigParticle.enable_fire_particle = true;
								ConfigParticle.enable_waterfall_splash = false;
								ConfigParticle.ambient_particle_rate = 0.2D;
								break;
							case 3:
								ConfigParticle.enable_falling_leaves = true;
								ConfigParticle.enable_fire_particle = true;
								ConfigParticle.enable_waterfall_splash = true;
								ConfigParticle.ambient_particle_rate = 0.35D;
								break;
							case 4:
								ConfigParticle.enable_falling_leaves = true;
								ConfigParticle.enable_fire_particle = true;
								ConfigParticle.enable_waterfall_splash = true;
								ConfigParticle.ambient_particle_rate = 0.6D;
								break;
							case 5:
								ConfigParticle.enable_falling_leaves = true;
								ConfigParticle.enable_fire_particle = true;
								ConfigParticle.enable_waterfall_splash = true;
								ConfigParticle.ambient_particle_rate = 1.0D;
								break;
							case 6:
								ConfigParticle.enable_falling_leaves = true;
								ConfigParticle.enable_fire_particle = true;
								ConfigParticle.enable_waterfall_splash = true;
								ConfigParticle.ambient_particle_rate = 2.0D;
								break;
							case 7:
								ConfigParticle.enable_falling_leaves = true;
								ConfigParticle.enable_fire_particle = true;
								ConfigParticle.enable_waterfall_splash = true;
								ConfigParticle.ambient_particle_rate = 4.0D;
								break;
						}
						break;
					case GuiEZConfig.BA_EF:
						ConfigStorm.enable_ef_scale = value == 1;
						break;
					case GuiEZConfig.BA_RADAR:
						ConfigMisc.debug_mode_radar = value == 1;
						break;
					case GuiEZConfig.BA_SHADER:
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
					case GuiEZConfig.BA_FOLIAGE:
						ConfigCoroUtil.foliageShaders = value == 1;
						break;
				}
			}
		nbtSaveDataClient();
		ConfigMod.forceSaveAllFilesFromRuntimeSettings();
	}
	
	/**Used to get needed information from a client to add to the server data*/
	public static void nbtReceiveServer(NBTTagCompound parNBT)
	{
		NBTTagCompound cache = new NBTTagCompound();
		for (int i = 0; i < GuiEZConfig.BD_MIN; i++)
			if (parNBT.hasKey(GuiEZConfig.prefix + i))
			{
				cache.setInteger(GuiEZConfig.prefix + i, parNBT.getInteger(GuiEZConfig.prefix + i));
				nbtServerData.setInteger(GuiEZConfig.prefix + i, parNBT.getInteger(GuiEZConfig.prefix + i));
			}
		
		//also add dimension feature config, its iterated over
		cache.setTag("dimData", parNBT.getCompoundTag("dimData"));
		nbtServerData.setTag("dimData", parNBT.getCompoundTag("dimData"));
		
		Weather2.debug("Received data from the client for server data: " + parNBT);
		processServerData(cache);
	}
	
	/**Used to get needed information from server and client to add to the client data. Must have command. <br>0 - From Client<br>1 - From Server*/
	public static void nbtReceiveClient(NBTTagCompound parNBT)
	{
		if (parNBT.hasKey("server"))
		{
			if (parNBT.getInteger("server") == 1)
			{
				nbtClientData.setBoolean("op", parNBT.getBoolean("op"));
				for (int i = 0; i < GuiEZConfig.BD_MIN; i++)
					if (parNBT.hasKey(GuiEZConfig.prefix + i))
					 nbtServerData.setInteger(GuiEZConfig.prefix + i, parNBT.getInteger(GuiEZConfig.prefix + i));
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
				Weather2.debug("Received data from the server: " + parNBT);
			}
			else
			{
				NBTTagCompound cache = new NBTTagCompound();
				for (int i = 0; i < GuiEZConfig.BD_MIN; i++)
					if (parNBT.hasKey(GuiEZConfig.prefix + i))
					{
						cache.setInteger(GuiEZConfig.prefix + i, parNBT.getInteger(GuiEZConfig.prefix + i));
						nbtClientData.setInteger(GuiEZConfig.prefix + i, parNBT.getInteger(GuiEZConfig.prefix + i));
					}
				
				Weather2.debug("Received data from the client for client data: " + parNBT);
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
		nbtClientData = nbtReadNBTFromDisk(true);
		nbtServerData = nbtReadNBTFromDisk(false);
		checkVersion();
		
		DEFAULTS.put(GuiEZConfig.BA_CLOUD, 3);
		DEFAULTS.put(GuiEZConfig.BA_FUNNEL, 3);
		DEFAULTS.put(GuiEZConfig.BA_PRECIPITATION, 3);
		DEFAULTS.put(GuiEZConfig.BA_EFFECT, 3);
		DEFAULTS.put(GuiEZConfig.BA_EF, 0);
		DEFAULTS.put(GuiEZConfig.BA_RADAR, 0);
		DEFAULTS.put(GuiEZConfig.BA_SHADER, 0);
		DEFAULTS.put(GuiEZConfig.BA_FOLIAGE, 0);
		DEFAULTS.put(GuiEZConfig.BB_GLOBAL, 0);
		DEFAULTS.put(GuiEZConfig.BC_ENABLE_TORNADO, 1);
		DEFAULTS.put(GuiEZConfig.BC_ENABLE_CYCLONE, 1);
		DEFAULTS.put(GuiEZConfig.BC_ENABLE_SANDSTORM, 1);
		DEFAULTS.put(GuiEZConfig.BC_FREQUENCY, 3);
		DEFAULTS.put(GuiEZConfig.BC_GRAB_BLOCK, 1);
		DEFAULTS.put(GuiEZConfig.BC_GRAB_ITEM, 0);
		DEFAULTS.put(GuiEZConfig.BC_GRAB_MOB, 1);
		DEFAULTS.put(GuiEZConfig.BC_GRAB_PLAYER, 1);
		DEFAULTS.put(GuiEZConfig.BC_STORM_PER_PLAYER, 0);
		
		for (int i = GuiEZConfig.BA_CLOUD; i < GuiEZConfig.BB_GLOBAL; i++)
			if (!nbtClientData.hasKey(GuiEZConfig.prefix + i))
				nbtClientData.setInteger(GuiEZConfig.prefix + i, DEFAULTS.get(i));
		for (int i = GuiEZConfig.BB_GLOBAL; i < GuiEZConfig.BD_MIN; i++)
			if (!nbtServerData.hasKey(GuiEZConfig.prefix + i))
				nbtServerData.setInteger(GuiEZConfig.prefix + i, DEFAULTS.get(i));
	}
	
	public static int getConfigValue(int buttonID)
	{
		if (nbtClientData.hasKey(GuiEZConfig.prefix + buttonID))
			return nbtClientData.getInteger(GuiEZConfig.prefix + buttonID);
		else if (nbtServerData.hasKey(GuiEZConfig.prefix + buttonID))
			return nbtServerData.getInteger(GuiEZConfig.prefix + buttonID);
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
	
	public static void nbtWriteNBTToDisk(NBTTagCompound parData, boolean saveForClient) {
		String fileURL = null;
		if (saveForClient) {
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + "Weather2" + File.separator + "EZGUIConfigClientData.dat";
		} else {
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + "Weather2" + File.separator + "EZGUIConfigServerData.dat";
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(fileURL);
	    	CompressedStreamTools.writeCompressed(parData, fos);
	    	fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			Weather2.debug("Error writing Weather2 EZ GUI data");
		}
	}
	
	public static NBTTagCompound nbtReadNBTFromDisk(boolean loadForClient) {
		NBTTagCompound data = new NBTTagCompound();
		String fileURL = null;
		if (loadForClient) {
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + "Weather2" + File.separator + "EZGUIConfigClientData.dat";
		} else {
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + "Weather2" + File.separator + "EZGUIConfigServerData.dat";
		}
		
		try {
			if ((new File(fileURL)).exists()) {
				data = CompressedStreamTools.readCompressed(new FileInputStream(fileURL));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Weather2.debug("Error reading Weather2 EZ GUI data");
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
