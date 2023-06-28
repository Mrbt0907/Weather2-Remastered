package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigVolume implements IConfigEX
{
	@Comment("How loud lightning sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double lightning = 1.0D;
	@Comment("How loud waterfall sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double waterfall = 0.5D;
	@Comment("How loud wind sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double wind = 0.15D;
	@Comment("How loud leaves are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double leaves = 0.5D;
	@Comment("How loud tornado/hurricane sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double cyclone = 1.0D;
	@Comment("How loud debris sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double debris = 1.0D;
	@Comment("How loud sirens are ingame. 1.0 is full volume, higher is louder, and lower is softer")
	public static double sirens = 1.0D;
	
    @Override
    public String getName()
    {
        return "Volume";
    }

    @Override
    public String getSaveLocation()
    {
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
