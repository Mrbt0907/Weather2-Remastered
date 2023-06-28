package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigFoliage implements IConfigEX {
	@Permission(0)
	@Comment("How far foliage shaders can be seen")
    public static int shader_range = 40;
	@Permission(0)
	@IntegerRange(min=0)
	@Comment("The delay that folage shaders will use in ticks to update itself.")
    public static int shader_process_delay = 1000;
	@Permission(0)
	@Comment("Enable client side grass to appear")
    public static boolean enable_extra_grass = false;

    @Override
    public String getName() {
        return "Weather2 Remastered - Foliage";
    }

    @Override
    public String getSaveLocation()
    {
        return Weather2.MODID + File.separator + "ConfigFoliage";
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
