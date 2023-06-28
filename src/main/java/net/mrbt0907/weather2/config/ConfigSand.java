package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigSand implements IConfigEX
{
    @Comment("Takes the sand out of sandwiches")
    public static boolean disable_sandstorms = false;
    @Comment("Use Server Storm Deadly Time instead of Sandstorm Rates")
	public static boolean enable_global_rates_for_sandstorms = false;
	@Comment("A Sandstorm has a 1 in x chance to spawn")
	public static int sandstorm_spawn_1_in_x = 30;
	@Comment("Time between sandstorms for either each player or entire server depending on if global rate is on, default: 3 mc days")
	public static int sandstorm_spawn_delay = 20*60*20*3;
    @Comment("Amount of game ticks between sand buildup iterations, keep it high to prevent client side chunk update spam that destroys FPS")
    public static int buildup_tick_delay = 40;
    @Comment("Base amount of loops done per iteration, scaled by the sandstorms intensity (value given here is the max possible)")
    public static int max_buildup_loop_ammount = 800;
    @Comment("Allow layered sand blocks to buildup outside deserty biomes where sandstorm is")
    public static boolean enable_buildup_outside_desert = true;
    @Comment("Enables sirens near sandstorms to play \\\"Darude - Sandstorm\". Meme content")
    public static boolean disable_darude_sandstorm_plz = false;

    @Override
    public String getName()
    {
        return "Sand";
    }

    @Override
    public String getSaveLocation()
    {
        return Weather2.MODID + File.separator + getName();
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
