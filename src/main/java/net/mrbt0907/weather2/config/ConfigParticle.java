package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.client.event.ClientTickHandler;

import java.io.File;


public class ConfigParticle implements IConfigCategory
{
	@ConfigComment("Determines the renderer used for storms and clouds. Accepts number ids and renderer ids. Ex: 0 or " + Weather2.MODID + ":normal uses the default renderer.")
	public static String particle_renderer = "0";
	@ConfigComment("Enables on screen debug information about the current particle renderer")
	public static boolean enable_debug_renderer = false;
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
	@ConfigComment("Enables distant downfall in severe storms")
    public static boolean enable_distant_downfall = false;
	@ConfigComment("Enables tornado debris clouds in severe storms")
    public static boolean enable_tornado_debris = true;
	@ConfigComment("Enables tornado clouds to change colors based on what block is picked up")
    public static boolean enable_tornado_block_colors = true;
	@ConfigComment("Enables dust particles to kick up in high wind situations")
    public static boolean enable_wind_particle = true;
	@ConfigComment("Enables vanilla rain and thunder only")
	public static boolean use_vanilla_rain_and_thunder = false;
	@ConfigComment("Particle rates for rain, downfall, and ground splash particle types")
	public static double precipitation_particle_rate = 0.65D;
	@ConfigComment("Particle rates for bush, and sand particle types")
	public static double sandstorm_debris_particle_rate = 0.3D;
	@ConfigComment("Particle rates for dust particles")
	public static double sandstorm_dust_particle_rate = 0.4D;
	@ConfigComment("Particle rates for wind particles")
	public static double wind_particle_rate = 0.2D;
	@ConfigComment("Particle rates for distant downfall particles")
	public static double distant_downfall_particle_rate = 0.2D;
	@ConfigComment("Particle delay in ticks for tornado debris particles")
	public static int ground_debris_particle_delay = 5;
	@ConfigComment("Particle delay in ticks for storm particles\n(Tornado or hurricane particles)")
	public static int funnel_particle_delay = 10;
	@ConfigComment("Particle multiplier used to increase or decrease particle rates")
	public static double particle_multiplier = 1.0D;
	@ConfigComment("The delay in ticks for the particle thread")
	public static int scene_enhancer_thread_delay = 400;
	@ConfigComment("Maximum percent of cloud coverage, supports over 100% for extended full cloud sky coverage")
	public static double max_cloud_coverage_perc = 50.0D;
	@ConfigComment("Minimum percent of cloud coverage, supports negative for extended cloudless sky coverage")
	public static double min_cloud_coverage_perc = 0.0D;
	@ConfigComment("How much to randomly change cloud coverage % amount, performed every 10 seconds")
	public static double cloud_coverage_change_amount = 0.05D;
	@ConfigComment("Should particles render outside of the normal render distance?")
	public static boolean enable_extended_render_distance = false;
	@ConfigComment("Distance that particles can render up to in blocks. Does not work with optifine installed")
	public static double extended_render_distance = 128.0D;
	@ConfigComment("How many weather2 particles can exist at once. Set to -1 for infinite particles. A typical hailstorm spawns around 3500~ particles on ultra settings")
	public static int max_particles = 3000;
	@ConfigComment("Particle multiplier that adjusts how fast rain rates change. Set it higher to make rain change faster")
	public static double rain_change_mult = 1.0D;
	@ConfigComment("Particle scale multiplier that adjusts how big particles should be")
	public static double particle_scale_mult = 1.0D;
	
	//clouds
	@ConfigComment("How many ticks between cloud particle spawning")
	public static int cloud_particle_delay = 3;
	@ConfigComment("Should rain show up?")
	public static boolean enable_vanilla_rain = false;

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
        return Weather2.MODID + File.separator + getName();
    }

    @Override
    public String getCategory() {
        return Weather2.MODID + ":" + getName();
    }

    @Override
    public void hookUpdatedValues()
    {
    	if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
    	{
    		WeatherAPI.refreshRenders(false);
    		if (ClientTickHandler.weatherManager != null)
    			ClientTickHandler.weatherManager.refreshParticleLimit();
    	}
    }
}
