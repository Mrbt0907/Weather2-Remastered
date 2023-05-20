package net.mrbt0907.weather2.config;

import java.io.File;
import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;


public class ConfigMisc implements IConfigCategory {
	//misc
	@ConfigComment("Disabling this will fix particles not rendering over water")
	public static boolean proxy_render_override = true;
	@ConfigComment("Enables the mod to turn vanilla clouds off")
	public static boolean enable_forced_clouds_off = true;
	@ConfigComment("How often does the mod save storms in ticks?")
	public static int auto_save_interval = 20*60*30;
	@ConfigComment("Enables the mod to output data into the console for debugging")
	public static boolean debug_mode = false;
	@ConfigComment("Enables the mod to output data into all radars about storms")
	public static boolean debug_mode_radar = false;
	@ConfigComment("How far do Radars sense storms?")
	public static double radar_range = 512.0D;
	@ConfigComment("How far do Doppler Radars sense storms?")
	public static double doppler_radar_range = 1024.0D;
	@ConfigComment("How far do Pulse Doppler Radars sense storms?")
	public static double pulse_doppler_radar_range = 2048.0D;
	
	//Weather
	@ConfigComment("How often does the mod sync storms in ticks? Fixes desync when ran")
	public static int sync_interval = 2400;
	@ConfigComment("If true, storms are removed when no players are in the dimension")
	public static boolean remove_storms_if_no_players = false;
	@ConfigComment("If true, lets server side do vanilla weather rules, weather2 will only make storms when server side says 'rain' is on")
	public static boolean overcast_mode = false;
	@ConfigComment("Used if overcastMode is off, 1 = lock weather on, 0 = lock weather off, -1 = dont lock anything, let server do whatever")
	public static int server_weather_mode = 0; //is only used if overcastMode is off
	//blocks
	@ConfigComment("How far can sirens detect a storm")
	public static double siren_scan_range = 256.0D;
	@ConfigComment("How far can sensors detect a storm")
	public static double sensor_scan_range = 512.0D;
	@ConfigComment("Disables the Weather Machine's ability to create tornados or hurricanes")
	public static boolean disable_weather_machine_cyclones = false;
	@ConfigComment("Disables the Weather Machine's recipe")
	public static boolean disable_weather_machine = false;
	@ConfigComment("Disables the Tornado Sensor's recipe")
	public static boolean disable_sensor = false;
	@ConfigComment("Disables the Tornado Siren's recipe")
	public static boolean disable_siren = false;
	@ConfigComment("Disables the Manual Tornado Siren's recipe")
	public static boolean disable_manual_siren = false;
	@ConfigComment("Disables the Wind Vane's recipe")
	public static boolean disable_wind_vane = false;
	@ConfigComment("Disables the Anemometer's recipe")
	public static boolean disable_anemometer = false;
	@ConfigComment("Disables the Weather Forcast's recipe")
	public static boolean disable_weather_radar = false;
	@ConfigComment("Disables the Weather Deflector's recipe")
	public static boolean disable_weather_deflector = false;
	@ConfigComment("Disables the Sand Layer's recipe")
	public static boolean disable_sand_layer = false;
	@ConfigComment("Disables Sand's recipe")
	public static boolean disable_sand = false;
	@ConfigComment("Disables the Pocket Sand item's recipe")
	public static boolean disable_pocket_sand = false;
	@ConfigComment("Disabling this recipe will keep them from using other recipes since it depends on this item")
	public static boolean disable_weather_item = false;

	
	//dimension settings
	@ConfigComment("List of dimensions the mod can use for weather")
	public static String dimensions_weather = "0, -127";
	@ConfigComment("List of dimensions the mod can use for effects")
	public static String dimensions_effects = "0, -127";

	@ConfigComment("With repair mode on, enabling this will allow blocks to instantly repair when a tornado is not damaging it")
	public static boolean block_instant_repair = false;

	@ConfigComment("If true, will cancel vanilla behavior of setting clear weather when the player sleeps, for global overcast mode")
	public static boolean disable_rain_reset_upon_sleep = true;

	@ConfigComment("Use if you are on a server with weather but want it ALL off client side for performance reasons, overrides basically every client based setting")
	public static boolean toaster_pc_mode = false;

	@ConfigComment("Server and client side, Locks down the mod to only do wind, leaves, foliage shader if on, etc. No weather systems, turns overcast mode on")
	public static boolean aesthetic_mode = false;

	@Override
	public String getName() {
		return "Misc";
	}

	@Override
	public String getRegistryName() {
		return Weather2.MODID + getName();
	}

	@Override
	public String getConfigFileName() {
		return Weather2.MODID + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return Weather2.MODID + ":" + getName();
	}

	@Override
	public void hookUpdatedValues()
	{
		WeatherAPI.refreshDimensionRules();
	}

}
