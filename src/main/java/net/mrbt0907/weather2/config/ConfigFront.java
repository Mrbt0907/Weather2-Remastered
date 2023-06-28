package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigFront implements IConfigEX
{
	@Comment("How many front objects can develop in a dimension?")
	public static int max_front_objects = 3;
	@Comment("How far can storms expand up to? Allows funnels to grow larger depending on size.")
	public static int max_front_size = 1500;
	@Comment("The tick rate of fronts. Lower numbers means faster progression.")
	public static int tick_rate = 20;
	@Comment("How fast fronts can change speeds. Higher numbers allow for faster acceleration and decceleration of fronts.")
	public static double speed_change_mult = 1.0D;
	@Comment("How fast fronts can change direction. Higher numbers allow for faster direction changes.")
	public static double angle_change_mult = 1.0D;
	@Comment("How fast fronts update their internal varaiables like temperature. Higher numbers make fronts change faster.")
	public static double environment_change_mult = 1.0D;
	@Comment("Percent chance for a storm to spawn in a front")
	public static double chance_to_spawn_storm_in_front = 20.0D;

    @Override
    public String getName()
    {
        return "Front";
    }

    @Override
    public String getSaveLocation()
    {
        return Weather2.MODID + File.separator + getName();
    }

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public void onConfigChanged(Phase phase, int variables) {}

	@Override
	public void onValueChanged(String variable, Object oldValue, Object newValue) {}
}
