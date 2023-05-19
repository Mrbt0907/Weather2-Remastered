package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigVolume implements IConfigCategory
{
	@ConfigComment("How loud lightning sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double lightning = 1.0D;
	@ConfigComment("How loud waterfall sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double waterfall = 0.5D;
	@ConfigComment("How loud wind sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double wind = 0.15D;
	@ConfigComment("How loud leaves are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double leaves = 0.5D;
	@ConfigComment("How loud tornado/hurricane sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double cyclone = 1.0D;
	@ConfigComment("How loud debris sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double debris = 1.0D;
	@ConfigComment("How loud sirens are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double sirens = 1.0D;
	
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
