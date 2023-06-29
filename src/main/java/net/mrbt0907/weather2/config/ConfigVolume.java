package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigVolume implements IConfigEX
{
	@Permission(0)
	@DoubleRange(min=0.0D, max=1.0D)
	@Comment("How loud lightning sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double lightning = 1.0D;
	@Permission(0)
	@DoubleRange(min=0.0D, max=1.0D)
	@Comment("How loud waterfall sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double waterfall = 0.5D;
	@Permission(0)
	@DoubleRange(min=0.0D, max=1.0D)
	@Comment("How loud wind sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double wind = 0.15D;
	@Permission(0)
	@DoubleRange(min=0.0D, max=1.0D)
	@Comment("How loud leaves are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double leaves = 0.5D;
	@Permission(0)
	@DoubleRange(min=0.0D, max=1.0D)
	@Comment("How loud tornado/hurricane sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double cyclone = 1.0D;
	@Permission(0)
	@DoubleRange(min=0.0D, max=1.0D)
	@Comment("How loud debris sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double debris = 1.0D;
	@Permission(0)
	@DoubleRange(min=0.0D, max=1.0D)
	@Comment("How loud sirens are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double sirens = 1.0D;
	
    @Override
    public String getName()
    {
        return "Weather2 Remastered - Volume";
    }

    @Override
    public String getSaveLocation()
    {
        return Weather2.MODID + File.separator + "ConfigVolume";
    }

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void onConfigChanged(Phase phase, int variables) {
		
	}

	@Override
	public void onValueChanged(String variable, Object oldValue, Object newValue) {
		
	}
}
