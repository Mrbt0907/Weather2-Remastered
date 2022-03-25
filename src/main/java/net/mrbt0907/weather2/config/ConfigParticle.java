package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigParticle implements IConfigCategory
{
	@ConfigComment("Enables falling leaves in the wind")
	public static boolean enable_falling_leaves = true;
    @ConfigComment("Particle rates for leaf, waterfall, and fire particles")
	public static double ambient_particle_rate = 0.6D;
    @ConfigComment("Enables water splashes in a waterfall")
	public static boolean enable_waterfall_splash = true;
	//public static boolean Wind_Particle_snow = false;
	@ConfigComment("Enables fire particles that float in the wind")
	public static boolean enable_fire_particle = true;
	@ConfigComment("Enables all precipitation particle types")
	public static boolean enable_precipitation = true;
	@ConfigComment("Enables heavy splashes for rain in severe storms")
    public static boolean enable_precipitation_splash = true;
	@ConfigComment("Enables heavy downfall in severe storms")
    public static boolean enable_heavy_precipitation = true;
	@ConfigComment("Enables vanilla rain and thunder only")
	public static boolean use_vanilla_rain_and_thunder = false;
	@ConfigComment("Particle rates for rain, downfall, and ground splash particle types")
	public static double precipitation_particle_rate = 0.65D;
	@ConfigComment("Particle rates for bush, and sand particle types")
	public static double sandstorm_debris_particle_rate = 0.3D;
	@ConfigComment("Particle rates for dust particles")
	public static double sandstorm_dust_particle_rate = 0.4D;
	@ConfigComment("Particle delay in ticks for storm particles\n(Tornado or hurricane particles)")
	public static int funnel_particle_delay = 5;
	@ConfigComment("Particle multiplier used to increase or decrease particle rates")
	public static double particle_multiplier = 0.5D;
	@ConfigComment("The delay in ticks for the particle thread")
	public static int effect_process_delay = 400;
	@ConfigComment("Maximum percent of cloud coverage, supports over 100% for extended full cloud sky coverage")
	public static double max_cloud_coverage_perc = 50.0D;
	@ConfigComment("Minimum percent of cloud coverage, supports negative for extended cloudless sky coverage")
	public static double min_cloud_coverage_perc = 0.0D;
	@ConfigComment("How much to randomly change cloud coverage % amount, performed every 10 seconds")
	public static double cloud_coverage_change_amount = 0.05D;
	@ConfigComment("Distance between cloud formations, not particles, this includes invisible cloudless formations used during partial cloud coverage")
	public static int min_cloud_distance = 300;
	//clouds
	@ConfigComment("How many ticks between cloud particle spawning")
	public static int cloud_particle_delay = 5;
	@ConfigComment("Should rain show up?")
	public static boolean enable_rain = false;

    @Override
    public String getName() {
        return "Particle";
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
