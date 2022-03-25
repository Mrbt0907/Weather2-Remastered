package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigStorm implements IConfigCategory
{
	@ConfigComment("Whether or not to use the Enhanced Fujita Scale for tornados.")
    public static boolean enable_ef_scale = false;
	@ConfigComment("A deadly storm has a 10 in x chance to spawn a water spout in\nhigh wind conditions")
    public static int high_wind_waterspout_10_in_x = 150;
	@ConfigComment("How many storms can develop in a dimension?")
	public static int max_storms = 10;
	@ConfigComment("Unknown")
	public static int storm_collide_distance = 128;
	@ConfigComment("A storm has a 1 in x chance to spawn a lightning bolt.\nHigher numbers means less lightning in storms")
	public static int lightning_bolt_1_in_x = 200;
	@ConfigComment("How far can storms expand up to? Allows funnels to grow larger depending on size.")
	public static int max_storm_size = 300;
	@ConfigComment("Tick delay for storms. Higher values means storms have slower development. DO NOT PUT 0")
	public static int storm_tick_delay = 60;
	@ConfigComment("How much water builds up in storms. Higher values = heavier rain faster")
	public static int humidity_buildup_rate = 10;
	@ConfigComment("How much water is lost in a storm when it rains. Higher values = shorter rain time")
	public static int humidity_spend_rate = 3;
	@ConfigComment("A storm has a 1 in x chance to recieve more water from a water source block.\\nHigher numbers means less chance of water build up (aka. Lighter rain storms are more common)")
	public static int humidity_buildup_from_source_1_in_x = 15;
	@ConfigComment("A storm has a 1 in x chance to recieve more water from air.\\nHigher numbers means less chance of water build up (aka. Lighter rain storms are more common)")
	public static int humidity_buildup_from_air_1_in_x = 100;
	@ConfigComment("A storm has a 1 in x chance to recieve more water when the weather is overcast. \\nHigher numbers means less chance of water build up (aka. Lighter rain storms are more common)")
	public static int humidity_buildup_from_overcast_1_in_x = 30;
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
	@ConfigComment("Percent chance for a storm to become a super cell")
	public static double chance_for_supercell = 35.0D;
	@ConfigComment("Percent chance for a storm to become a super cell with hail")
	public static double chance_for_hailing_supercell = 32.5D;
	@ConfigComment("Percent chance for a storm to become an EF0 tornado")
	public static double chance_for_ef0 = 37.22D;
	@ConfigComment("Percent chance for a storm to become a category 0 hurricane")
	public static double chance_for_c0 = 30.5D;
	@ConfigComment("Percent chance for a storm to become an EF1 tornado")
	public static double chance_for_ef1 = 38.88D;
	@ConfigComment("Percent chance for a storm to become a category 1 hurricane")
	public static double chance_for_c1 = 28.0D;
	@ConfigComment("Percent chance for a storm to become an EF2 tornado")
	public static double chance_for_ef2 = 13.61D;
	@ConfigComment("Percent chance for a storm to become a category 2 hurricane")
	public static double chance_for_c2 = 10.0D;
	@ConfigComment("Percent chance for a storm to become an EF3 tornado")
	public static double chance_for_ef3 = 6.11D;
	@ConfigComment("Percent chance for a storm to become a category 3 hurricane")
	public static double chance_for_c3 = 8.2D;
	@ConfigComment("Percent chance for a storm to become an EF4 tornado")
	public static double chance_for_ef4 = 1.05D;
	@ConfigComment("Percent chance for a storm to become a category 4 hurricane")
	public static double chance_for_c4 = 2D;
	@ConfigComment("Percent chance for a storm to become an EF5 tornado")
	public static double chance_for_ef5 = 0.1D;
	@ConfigComment("Percent chance for a storm to become a category 5 hurricane")
	public static double chance_for_c5 = 0.1D;
	//per player storm settings
	@ConfigComment("For each player in the server, a deadly storm has a 1 in x chance to spawn")
	public static int storm_per_player_1_in_x = 30;
	@ConfigComment("The time in ticks it takes for a severe storm to spawn for each player")
	public static int storm_per_player_delay = 20*60*20*3; //3 mc days
	
	//per server storm settings
	@ConfigComment("Use global storm instead of per player rates to spawn storms.\nEnable if you want storms to stay at the same rarity no matter how many players are in the server.")
	public static boolean enable_global_storm_rates = false;
	@ConfigComment("Globally a deadly storm has a 1 in x chance to spawn")
	public static int storm_global_1_in_x = 30;
	@ConfigComment("The time in ticks it takes for a severe storm to spawn globally")
	public static int storm_global_delay = 20*60*20*3;
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
	@ConfigComment("A storm has a 1 in x chance to start raining")
	public static int storm_rains_1_in_x = 150;

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
	@ConfigComment("Make tornados initial heading aimed towards closest player")
	public static boolean storms_aim_at_player = true;
	@ConfigComment("Should tornados spawn?")
	public static boolean disable_tornados = false;
	@ConfigComment("Should cyclones spawn?")
	public static boolean disable_cyclones = false;

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
    	
    }
    
    public static boolean isLayerValid(int layer)
    {
    	switch(layer)
    	{
    		case 1:
    			return enable_cloud_layer_1;
    		case 2:
    			return enable_cloud_layer_2;
    		default:
    			return true;
    	}
    }
}
