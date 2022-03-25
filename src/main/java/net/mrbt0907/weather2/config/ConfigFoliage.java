package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigFoliage implements IConfigCategory {

	@ConfigComment("How far foliage shaders can be seen")
    public static int shader_range = 40;
	@ConfigComment("The delay that folage shaders will use in ticks to update itself.")
    public static int shader_process_delay = 1000;
	@ConfigComment("Enable client side grass to appear")
    public static boolean enable_extra_grass = false;

    @Override
    public String getName() {
        return "Foliage";
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
