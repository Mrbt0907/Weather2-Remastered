package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigSnow implements IConfigCategory {


	//snow
	@ConfigComment("Should snow build up in a snowstorm?")
	public static boolean Snow_PerformSnowfall = false;
	//public static boolean Snow_ExtraPileUp = false;
	@ConfigComment("Snow layers have a 1 in x chance to spawn durring a snowstorm")
	public static int Snow_RarityOfBuildup = 64;
	//public static int Snow_MaxBlockBuildupHeight = 3;
	//public static boolean Snow_SmoothOutPlacement = false;

    @Override
    public String getName() {
        return "Snow";
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
