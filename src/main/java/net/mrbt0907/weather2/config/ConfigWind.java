package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigWind implements IConfigEX {
	@Comment("Enables villagers to detect storms and run inside")
    public static boolean enable = true;
	@Comment("Enables the ability for wind to push entities.")
	public static boolean enableWindAffectsEntities = true;
	@Comment("The multiplier for players weight while being pushed by wind. Lower values makes the player lighter.")
	public static double windPlayerWeightMult = 1.0F;
	@Comment("The multiplier for entities other than players weight while being pushed by wind. Lower values makes the player lighter.")
	public static double windEntityWeightMult = 1.0F;
	@Comment("The multiplier for all entities weight while being pushed by wind while swimming. Lower values makes all entities lighter while in liquid. Stacks ontop of the other two multipliers")
	public static double windSwimmingWeightMult = 1.0F;
	
	@Comment("The maximum angle in degrees that wind angle can change with.")
    public static double windAngleChangeMax = 30.0F;
	@Comment("The multiplier for how smooth wind can change. Lower values increases the smoothness.")
    public static double windChangeMult = 1.0F;
	
    @Comment("Min wind speed to maintain for storms and clouds. Higher means faster storms.")
    public static double windSpeedMin = 0.0D;
    @Comment("Max wind speed to maintain for storms and clouds. Higher means faster storms.")
    public static double windSpeedMax = 2.5D;
    @Comment("The minimum amount of time in ticks it can take for wind to change speed and direction.")
    public static int windRefreshMin = 100;
    @Comment("The maximum amount of time in ticks it can take for wind to change speed and direction.")
    public static int windRefreshMax = 400;

    @Override
    public String getName() {
        return "Wind";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + getName();
    }

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onConfigChanged(Phase phase, int variables) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onValueChanged(String variable, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}
}
