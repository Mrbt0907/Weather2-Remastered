package net.mrbt0907.weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigSand implements IConfigCategory
{
    @ConfigComment("Takes the sand out of sandwiches")
    public static boolean disable_sandstorms = false;
    @ConfigComment("Use Server Storm Deadly Time instead of Sandstorm Rates")
	public static boolean enable_global_rates_for_sandstorms = false;
	@ConfigComment("A Sandstorm has a 1 in x chance to spawn")
	public static int sandstorm_spawn_1_in_x = 30;
	@ConfigComment("Time between sandstorms for either each player or entire server depending on if global rate is on, default: 3 mc days")
	public static int sandstorm_spawn_delay = 20*60*20*3;
    @ConfigComment("Amount of game ticks between sand buildup iterations, keep it high to prevent client side chunk update spam that destroys FPS")
    public static int buildup_tick_delay = 40;
    @ConfigComment("Base amount of loops done per iteration, scaled by the sandstorms intensity (value given here is the max possible)")
    public static int max_buildup_loop_ammount = 800;
    @ConfigComment("Allow layered sand blocks to buildup outside deserty biomes where sandstorm is")
    public static boolean enable_buildup_outside_desert = true;
    @ConfigComment("Enables sirens near sandstorms to play \\\"Darude - Sandstorm\". Meme content")
    public static boolean disable_darude_sandstorm_plz = false;

    @Override
    public String getName() {
        return "Sand";
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
