package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import java.io.File;


public class ConfigGrab implements IConfigCategory
{
	@ConfigComment("Should weather2 use the old getters and setters for blocks?")
	public static boolean disableGrabOptimizations = true;
	@ConfigComment("Should storms grab players?")
	public static boolean grab_players = true;
	@ConfigComment("Should storms grab mobs?")
	public static boolean grab_mobs = true;
	@ConfigComment("Should storms grab animals?")
	public static boolean grab_animals = true;
	@ConfigComment("Should storms grab villagers?")
	public static boolean grab_villagers = true;
	@ConfigComment("Should storms grab blocks?")
	public static boolean grab_blocks = true;
	@ConfigComment("Should storms grab items?")
	public static boolean grab_items = false;
	@ConfigComment("Should storms grab blocks based on a block list?")
	public static boolean enable_grab_list = true;
	@ConfigComment("Should the grab list act as a blacklist?")
	public static boolean grab_list_blacklist = false;
	@ConfigComment("Should the grab list also find similarly named items?")
	public static boolean grab_list_partial_matches = false;
	@ConfigComment("Should storms grab blocks based on wind resistance? Will follow grab lists if enabled.")
	public static boolean grab_list_strength_match = true;
	@ConfigComment("See config file for examples. A list of blocks to be grabbed by storms. Use commas and/or spaces to separate each entry. Do not use quotation marks. \"!\" Will remove entries set by other mods. Accepted formats - modid:name, name")
	public static String grab_list_entries = "planks, minecraft:leaves, cobblestone";
	@ConfigComment("Should similar entries from both lists be used?")
	public static boolean enable_list_sharing = true;
	@ConfigComment("Should tornados replace blocks with other blocks based on a list?")
	public static boolean enable_replace_list = true;
	@ConfigComment("Should the replace list act as a blacklist?")
	public static boolean replace_list_blacklist = false;
	@ConfigComment("Should the replace list also find similarly named items?")
	public static boolean replace_list_partial_matches = false;
	@ConfigComment("Should storms replace blocks based on wind resistance? Will follow grab lists if enabled.")
	public static boolean replace_list_strength_matches = true;
	@ConfigComment("See config file for examples. A list of blocks to be replaced by storms. Use commas and/or spaces to separate each entry. Do not use quotation marks. \"!\" Will remove entries set by other mods. Use = without spaces to indicate the replacement block. Accepted formats - modid:name=modid:replacement, name=replacement, modid:name=replacement, name=modid:replacement")
	public static String replace_list_entries = "leaves=air, leaves2=minecraft:air, minecraft:grass=dirt, minecraft:red_flower=minecraft:air";
	@ConfigComment("Should the wind resistance list also find similarly named items?")
	public static boolean wind_resistance_partial_matches = false;
	@ConfigComment("See config file for examples. A list of blocks to register wind resistances to. The numbers are measured in MPH. 65=EF0, 92=EF1, 119=EF2, 146=EF3, 173=EF4, 200=EF5, and so on. Use commas and/or spaces to separate each entry. Do not use quotation marks. \"!\" Will remove entries set by other mods. Use \"=\" without spaces to indicate the wind resistance of the block. Accepted formats - modid:name=number, name=number")
	public static String wind_resistance_entries = "obsidian=512, iron_bar=200";
	@ConfigComment("Experimental idea, places the WIP repairing block where a tornado\ndoes damage instead of removing the block, causes tornado damage to self repair, recommend setting Storm_Tornado_rarityOfBreakOnFall to 0 to avoid duplicated blocks")
	public static boolean enable_repair_block_mode = false;
	@ConfigComment("How many flying blocks can all tornados have at a time?")
	public static int max_flying_blocks = 120;
	@ConfigComment("How many blocks can all tornados replace at a time?")
	public static int max_replaced_blocks = 1280;
	@ConfigComment("Should blocks picked up by storms damage other entities and players?")
	public static boolean grabbed_blocks_hurt = true;
	@ConfigComment("Used if Storm_Tornado_grabbedBlocksRepairOverTime is true,\nminimum of 600 ticks (30 seconds) required")
	public static int Storm_Tornado_TicksToRepairBlock = 20*60*5;
	@ConfigComment("Percent of how rarely a block will be removed while spinning around a tornado")
	public static int Storm_Tornado_rarityOfBreakOnFall = 5;
	@ConfigComment("Percent of how rarely a block will be removed while spinning around a tornado")
	public static int Storm_Tornado_rarityOfDisintegrate = 15;
	@ConfigComment("How many blocks can be grabbed per tick")
	public static int max_grabbed_blocks_per_tick = 12;
	@ConfigComment("How many blocks can be grabbed per tick")
	public static int max_replaced_blocks_per_tick = 256;
	@ConfigComment("How often do storms process grabbed blocks in ticks?")
	public static int grab_process_delay = 10;
	
	@Override
    public String getName() {
        return "Grab";
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
    	WeatherAPI.refreshGrabRules();
    }
}
