package net.mrbt0907.configex.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.IConfigEX;

public class ConfigMaster implements IConfigEX
{
	@Name("Yee ol number")
	@Comment({"Tis a number", "And you shall obey\nle number"})
	@Enforce
	@IntegerRange(min=0, max=69)
	public static int yee = 5;
	
	@Name("Enable Debug Mode")
	@Comment("Enables the displaying of various debugging information in the console")
	public static boolean debug_mode = false;
	
	@Name("Test 1")
	@Comment("Integer Test")
	@Permission(0)
	public static int test_1 = 1;
	@Name("Test 2")
	@Permission(0)
	public static short test_2 = 2;
	@Name("Test 3")
	@Comment("Test variable")
	@Permission(0)
	public static long test_3 = 3L;
	@Name("Test 4")
	@Comment("Test variable")
	@Permission(0)
	public static float test_4 = 4.0F;
	@Name("Test 5")
	@Comment("Test variable")
	@Permission(0)
	public static double test_5 = -5.0D;
	@Comment("Test variable")
	@Permission(0)
	public static String test_6 = "Potato";
	@Comment("Test variable")
	@Permission(0)
	public static boolean test_7 = false;
	@Comment("Test variable")
	public static boolean test_8 = false;
	@Comment("Test variable")
	@Hidden
	public static boolean test_9 = false;
	@Comment("Test variable")
	@Hidden
	public static boolean test_10 = false;
	@Comment("Test variable")
	@Ignore
	public static boolean test_11 = false;
	@Comment("Test variable")
	@Enforce
	@Hidden
	public static boolean test_12 = false;
	
	@Override
	public String getName()
	{
		return "ConfigEX Master";
	}

	@Override
	public String getDescription()
	{
		return "This is the master config file for the config mod.";
	}

	@Override
	public String getSaveLocation()
	{
		return ConfigModEX.MODID + "/" + getName();
	}

	@Override
	public void onConfigChanged(Phase phase, int variables)
	{
		ConfigModEX.debug("Config has changed! Phase: " + phase + ", Number: " + variables);
	}

	@Override
	public void onValueChanged(String variable, Object oldValue, Object newValue)
	{
		ConfigModEX.debug("Config variable " + variable + " -> Old:" + oldValue + ", New:" + newValue);
	}
}
