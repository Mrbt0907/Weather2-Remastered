package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigWind implements IConfigCategory {
	@ConfigComment("Enables villagers to detect storms and run inside")
    public static boolean enable = true;
	@ConfigComment("Enables villagers to detect storms and run inside")
    public static boolean enable_low_wind_events = true;
	@ConfigComment("Enables villagers to detect storms and run inside")
    public static boolean enable_high_wind_events = true;

	@ConfigComment("How many ticks does the world wait before trying to have low winds")
    public static int lowWindTimerEnableAmountBase = 20*60*2;
	@ConfigComment("How far can the randomizer add up towards the base value of Low Wind")
    public static int lowWindTimerEnableAmountRnd = 20*60*10;
    @ConfigComment("The world has a 1 in x chance for low wind events to take place")
    public static int lowWindOddsTo1 = 20*200;

    @ConfigComment("How many ticks does the world wait before trying to have high winds")
    public static int highWindTimerEnableAmountBase = 20*60*2;
    @ConfigComment("How far can the randomizer add up towards the base value of High Wind")
    public static int highWindTimerEnableAmountRnd = 20*60*10;
    @ConfigComment("The world has a 1 in x chance for high wind events to take place")
    public static int highWindOddsTo1 = 20*400;

	@ConfigComment("Wind change rates")
    public static double globalWindChangeAmountRate = 1F;

    @ConfigComment("Min wind speed to maintain for storms and clouds. Higher means faster storms.")
    public static double windSpeedMin = 0.0001D;
    @ConfigComment("Max wind speed to maintain for storms and clouds. Higher means faster storms.")
    public static double windSpeedMax = 0.75D;

    @ConfigComment("Min wind speed to maintain if its raining with global overcast mode on, overrides low wind events and windSpeedMin")
    public static double windSpeedMinGlobalOvercastRaining = 0.3D;

    @Override
    public String getName() {
        return "Wind";
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
    public void hookUpdatedValues() {

    }
}
