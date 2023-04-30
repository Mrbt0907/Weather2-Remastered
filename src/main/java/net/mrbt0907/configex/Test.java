package net.mrbt0907.configex;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.mrbt0907.configex.config.ConfigInstance;
import net.mrbt0907.configex.api.ConfigAPI;
import net.mrbt0907.configex.api.ConfigAPI.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.utils.ArrayUtils;
import net.mrbt0907.configex.utils.ArrayUtils.MapEntry;

public class Test implements IConfigEX
{

	@ConfigValue
	@ConfigIntegerBounds(min=0, max=50)
	public static int int_example = 0;
	@ConfigValue
	@ConfigShortBounds(min=0, max=60)
	public static short short_example = 0;
	@ConfigValue
	@ConfigLongBounds(min=0, max=70)
	public static long long_example = 0L;
	@ConfigValue
	@ConfigFloatBounds(min=0, max=80)
	public static float float_example = 0.0F;
	@ConfigValue
	@ConfigDoubleBounds(min=0, max=90)
	public static double double_example = 0.0D;
	@ConfigValue
	public static String string_example = "Hi, I am bob";
	@ConfigValue
	public static List<String> list_integer_example = Arrays.asList("a","b","c","dd","ef");
	@ConfigValue
	public static Map<String, String> map_example = ArrayUtils.createMap(MapEntry.Entry("a", "1+1"), MapEntry.Entry("b", "1+2"), MapEntry.Entry("c", "3+1"), MapEntry.Entry("d", "4+4"), MapEntry.Entry("e", "5+9"));
	
	@Override
	public ResourceLocation getConfigID()
	{
		return new ResourceLocation("test");
	}

	@Override
	public void onConifgChanged()
	{
		ConfigEX.info("\n------------------------\nHi! I am a single config file!\n------------------------");
		ConfigInstance instance = ConfigAPI.getConfigInstance(this);
		ConfigEX.info("Value int example: " + (instance == null ? "Null Instance" : instance.getValue("test.int.example") == null ? "Null Variable" : instance.getValue("test.int.example").getValue()));
	}

	@Override
	public void onValueChanged(String configID, Object oldValue, Object newValue)
	{
		ConfigEX.info("Config " + configID + " Value: " + newValue + ",  Old Value: " + oldValue);
	}

}
