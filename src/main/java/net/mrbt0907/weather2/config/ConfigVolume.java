package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigVolume implements IConfigCategory
{
	@ConfigComment("How loud wind sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double wind = 0.05D;
	@ConfigComment("How loud waterfall sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double waterfall = 0.5D;
	@ConfigComment("How loud leaf sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double trees = 0.5D;
	@ConfigComment("How loud lightning sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double lightning = 1D;

    @Override
    public String getName()
    {
        return "Volume";
    }

    @Override
    public String getRegistryName()
    {
        return Weather2.MODID + getName();
    }

    @Override
    public String getConfigFileName()
    {
        return "Weather2" + File.separator + getName();
    }

    @Override
    public String getCategory()
    {
        return "Weather2: " + getName();
    }

    @Override
    public void hookUpdatedValues()
    {

    }
}
