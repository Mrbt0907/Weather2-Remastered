package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigSimulation implements IConfigCategory
{
	@ConfigComment({"Should storms use a more realistic storm system? (Does not work rn, don't enable)"})
    public static boolean simulation_enable = false;
	@ConfigComment("Distance storms can go to from players before they are deleted")
	public static int max_storm_distance = 1124;
	@ConfigComment("Distance storms can spawn away from players")
	public static int max_storm_spawning_distance = 1024;
	
    @Override
    public String getName() {
        return "Simulation";
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
