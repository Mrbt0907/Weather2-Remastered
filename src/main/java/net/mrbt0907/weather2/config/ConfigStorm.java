package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;

import java.io.File;


public class ConfigStorm implements IConfigCategory
{
	@ConfigComment("Whether or not to use the Enhanced Fujita Scale for tornados.")
    public static boolean enable_ef_scale = false;
	@ConfigComment("A multiplier for modifying the exponential growth of the sizes of storms. Use increments of 0.1 as that is considered a big increase.")
	public static double storm_size_curve_mult = 1.2D;
	@ConfigComment("A deadly storm has a 10 in x chance to spawn a water spout in\nhigh wind conditions")
    public static int high_wind_waterspout_10_in_x = 150;
	@ConfigComment("How many weather objects can develop in a dimension?")
	public static int max_weather_objects = 40;
	@ConfigComment("A storm has a 1 in x chance to spawn a lightning bolt.\nHigher numbers means less lightning in storms")
	public static int lightning_bolt_1_in_x = 200;
	@ConfigComment("How far can storms expand up to? Allows funnels to grow larger depending on size.")
	public static int max_storm_size = 1000;
	@ConfigComment("How far can storms expand up to? Allows funnels to grow larger depending on size.")
	public static int min_storm_size = 500;
	@ConfigComment("Tick delay for storms. Higher values means storms have slower development. DO NOT PUT 0")
	public static int storm_tick_delay = 60;
	@ConfigComment("How much water builds up in storms. Higher values = heavier rain faster")
	public static int humidity_buildup_rate = 1;
	@ConfigComment("How much water is lost in a storm when it rains. Higher values = shorter rain time")
	public static int humidity_spend_rate = 3;
	@ConfigComment("Maybe the rate that a storm changes their temps to match another biome's temperature?")
	public static double temperature_adjust_rate = 0.1D;
	@ConfigComment("Percent chance for a storm to develop much stronger and larger than normal")
	public static double chance_for_violent_storm = 5.0D;
	@ConfigComment("How much hail falls from the sky in a storm per tick?")
	public static int hail_stones_per_tick = 2;
	@ConfigComment("A storm has a 10 in x chance to spawn over water. -1 disables this")
	public static int storm_developing_above_ocean_10_in_x = 300;
	@ConfigComment("A storm has a 10 in x chance to spawn in land. -1 disables this")
	public static int storm_developing_above_land_10_in_x = -1;
	@ConfigComment("Percent chance for a storm to become a supercell")
	public static double chance_for_supercell = 35.0D;
	@ConfigComment("Percent chance for a storm to become a thunderstorm")
	public static double chance_for_thunderstorm = 32.5D;
	
	@ConfigComment("See config file for examples. A list of tornado stages with the chances for a storm to reach the stage. Use commas and/or spaces to separate each entry. Do not use quotation marks. Use = without spaces to indicate the chance. Chance goes from 0 (0% Chance) to 1 (100% Chance). Accepted formats - stage=chance")
	public static String chances_for_tornados = "F5=0.1, F4=1.05, F3=6.11, F2=13.61, F1=38.88, F0=37.22";
	
	@ConfigComment("See config file for examples. A list of hurricane stages with the chances for a storm to reach the stage. Use commas and/or spaces to separate each entry. Do not use quotation marks. Use = without spaces to indicate the chance. Chance goes from 0 (0% Chance) to 1 (100% Chance). Accepted formats - stage=chance")
	public static String chances_for_hurricanes = "C5=0.1, C4=2, C3=8.2, C2=10, C1=28, C0=30.5";
	
	
	//per server storm settings
	@ConfigComment("Use global storm instead of per player rates to spawn storms.\nEnable if you want storms to stay at the same rarity no matter how many players are in the server.")
	public static boolean enable_spawn_per_player = false;
	@ConfigComment("Globally a deadly storm has a 1 in x chance to spawn")
	public static int storm_spawn_chance = 30;
	@ConfigComment("The time in ticks it takes for a severe storm to spawn globally")
	public static int storm_spawn_delay = 72000;
	@ConfigComment("Should Weather2 cancel vanilla rainstorms at all times?")
	public static boolean prevent_vanilla_thunderstorms = true;
	//lightning
	@ConfigComment("A lightning bolt has a 10 in x chance to catch something on fire")
	public static int lightning_bolt_sets_fire_10_in_x = 20;
	@ConfigComment("How long in seconds fire can last")
	public static int lightning_bolt_fire_lifetime = 3;
	@ConfigComment("How close does a storm need to be to a player to experience lightning")
	public static int max_lightning_bolt_distance = 256;

	@ConfigComment("Should lightning start fires?")
	public static boolean enable_lightning_bolt_fires = false;

	@ConfigComment("How far a Storm Deflector can remove storms")
	public static int storm_deflector_range = 150;

    @ConfigComment("The minimum stage a storm has to be at to be removed,\nstages are: 0 = anything, 1 = thunder, 2 = high wind, 3 = hail, 4 = F0/C0, 5 = F1/C1, 6 = F2/C2, 7 = F3/C3, 8 = F4/C4, 9 = F5/C5")
    public static int storm_deflector_minimum_stage = 1;
    @ConfigComment("Should any Storm Deflector prevent rainstorms?")
    public static boolean storm_deflector_removes_rain = false;
    @ConfigComment("Should any Storm Deflector prevent sandstorms?")
    public static boolean storm_deflector_removes_sandstorms = true;

	@ConfigComment("Minimum amount of visual rain shown when its raining globally during overcast mode")
    public static double min_overcast_rain = 0.01D;
	@ConfigComment("A storm in overcast mode has a 1 in x chance to start raining")
	public static int overcast_1_in_x = 50;

	@ConfigComment("How often in ticks, a rainstorm updates its list of\nentities under the rainstorm to extinguish. Extinguishes entities under rainclouds when globalOvercast is off. Set to 0 or less to disable")
	public static int storm_rain_extinguish_delay = 200;
	@ConfigComment("How far can Villagers see storms")
	public static int villager_detection_range = 256;
	@ConfigComment("Enables Villagers to detect storms and run inside")
	public static boolean enable_villagers_take_cover = true;
	@ConfigComment("How high should storms and clouds in the upper layer spawn at?")
	public static int cloud_layer_2_height = 500;
	@ConfigComment("How high should storms and clouds in the middle layer spawn at?")
	public static int cloud_layer_1_height = 350;
	@ConfigComment("How high should storms and clouds in the lower layer spawn at?")
	public static int cloud_layer_0_height = 200;
	@ConfigComment("For a third layer of passive non storm progressing clouds")
	public static boolean enable_cloud_layer_2 = false;
	@ConfigComment("For a second layer of passive non storm progressing clouds")
	public static boolean enable_cloud_layer_1 = false;
	@ConfigComment("How large can a storm's damage path be? Higher values will impact preformance.")
	public static double max_storm_damage_size = 300.0D;
	@ConfigComment("Accuracy of tornado aimed at player in degrees.\nHigher values means less accuracy up to 360 degrees")
	public static int storm_aim_accuracy_in_angle = 5;

	@ConfigComment("Accuracy of tornado aimed at player in degrees.\nHigher values means less accuracy up to 360 degrees")
	public static int spawningTickRate = 20;
	@ConfigComment("Make tornados initial heading aimed towards closest player")
	public static boolean storms_aim_at_player = true;
	@ConfigComment("Should tornados spawn?")
	public static boolean disable_tornados = false;
	@ConfigComment("Should cyclones spawn?")
	public static boolean disable_cyclones = false;
	@ConfigComment("How big can a storm grow up to? 1.0 means 100% normal size, 0.0 means 0.0% of normal size")
	public static double max_size_growth = 1.35D;
	@ConfigComment("How small can a storm grow up to? 1.0 means 100% normal size, 0.0 means 0.0% of normal size")
	public static double min_size_growth = 0.75D;
	@ConfigComment("How much does a violent storm add onto a storm's size at a maximum?")
	public static double max_violent_size_growth = 1.65D;
	@ConfigComment("How much does a violent storm add onto a storm's size at a minimum?")
	public static double min_violent_size_growth = 0.25D;
	@ConfigComment("How many times can any storm revive?")
	public static int max_storm_revives = 2;
	@ConfigComment("Percent chance for a storm to revive after it is nearly dead")
	public static double chance_for_storm_revival = 0.25D;
	
    @Override
    public String getName() {
        return "Storm";
    }

    @Override
    public String getRegistryName() {
        return Weather2.MODID + getName();
    }

    @Override
    public String getConfigFileName() {
        return "Weather2" + File.separator + getName();
    }

    @Override
    public String getCategory() {
        return "Weather2: " + getName();
    }

    @Override
    public void hookUpdatedValues()
    {
    	WeatherAPI.refreshStages();
    }
    
    public static boolean isLayerValid(int layer)
    {
    	switch(layer)
    	{
    		case 0: 
    			return true;
    		case 1:
    			return enable_cloud_layer_1;
    		case 2:
    			return enable_cloud_layer_2;
    		default:
    			return false;
    	}
    }
}
