package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigFront implements IConfigCategory
{
	@ConfigComment("How many front objects can develop in a dimension?")
	public static int max_front_objects = 3;
	@ConfigComment("How far can storms expand up to? Allows funnels to grow larger depending on size.")
	public static int max_front_size = 1500;
	@ConfigComment("The tick rate of fronts. Lower numbers means faster progression.")
	public static int tick_rate = 20;
	@ConfigComment("How fast fronts can change speeds. Higher numbers allow for faster acceleration and decceleration of fronts.")
	public static double speed_change_mult = 1.0D;
	@ConfigComment("How fast fronts can change direction. Higher numbers allow for faster direction changes.")
	public static double angle_change_mult = 1.0D;
	@ConfigComment("How fast fronts update their internal varaiables like temperature. Higher numbers make fronts change faster.")
	public static double environment_change_mult = 1.0D;
	@ConfigComment("Percent chance for a storm to spawn in a front")
	public static double chance_to_spawn_storm_in_front = 20.0D;

    @Override
    public String getName()
    {
        return "Front";
    }

    @Override
    public String getRegistryName()
    {
        return Weather2.MODID + getName();
    }

    @Override
    public String getConfigFileName()
    {
        return Weather2.MODID + File.separator + getName();
    }

    @Override
    public String getCategory()
    {
        return Weather2.MODID + ":" + getName();
    }

    @Override
    public void hookUpdatedValues()
    {

    }
}
