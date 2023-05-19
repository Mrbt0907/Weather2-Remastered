package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigWind implements IConfigCategory {
	@ConfigComment("Enables villagers to detect storms and run inside")
    public static boolean enable = true;
	@ConfigComment("Enables the ability for wind to push entities.")
	public static boolean enableWindAffectsEntities = true;
	@ConfigComment("The multiplier for players weight while being pushed by wind. Lower values makes the player lighter.")
	public static double windPlayerWeightMult = 1.0F;
	@ConfigComment("The multiplier for entities other than players weight while being pushed by wind. Lower values makes the player lighter.")
	public static double windEntityWeightMult = 1.0F;
	@ConfigComment("The multiplier for all entities weight while being pushed by wind while swimming. Lower values makes all entities lighter while in liquid. Stacks ontop of the other two multipliers")
	public static double windSwimmingWeightMult = 1.0F;
	
	@ConfigComment("The maximum angle in degrees that wind angle can change with.")
    public static double windAngleChangeMax = 30.0F;
	@ConfigComment("The multiplier for how smooth wind can change. Lower values increases the smoothness.")
    public static double windChangeMult = 1.0F;
	
    @ConfigComment("Min wind speed to maintain for storms and clouds. Higher means faster storms.")
    public static double windSpeedMin = 0.0D;
    @ConfigComment("Max wind speed to maintain for storms and clouds. Higher means faster storms.")
    public static double windSpeedMax = 2.5D;
    @ConfigComment("The minimum amount of time in ticks it can take for wind to change speed and direction.")
    public static int windRefreshMin = 100;
    @ConfigComment("The maximum amount of time in ticks it can take for wind to change speed and direction.")
    public static int windRefreshMax = 400;

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
        return Weather2.MODID + File.separator + getName();
    }

    @Override
    public String getCategory() {
        return Weather2.MODID + ":" + getName();
    }

    @Override
    public void hookUpdatedValues() {

    }
}
