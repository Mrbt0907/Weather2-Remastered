package net.mrbt0907.configex.config;

<<<<<<< Updated upstream
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.Hidden;
import net.mrbt0907.configex.api.ConfigAnnotations.Name;
import net.mrbt0907.weather2remastered.Weather2Remastered;

public class ConfigMaster implements IConfigEX
{
	@Hidden
	@Name("Enable Debug Mode")
	@Comment("Enables the displaying of various debugging information in the console")
=======
import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.IConfigEX;

public class ConfigMaster implements IConfigEX
{
	@Name("Enable Debug Mode")
	@Comment("Enables the displaying of various debugging information in the console")
	@Hidden
>>>>>>> Stashed changes
	public static boolean debug_mode = false;
	
	@Override
	public String getName()
	{
		return "ConfigEX - Master";
	}

	@Override
	public String getDescription()
	{
		return "This is the master config file for the config mod.";
	}

	@Override
	public String getSaveLocation()
	{
<<<<<<< Updated upstream
		return ConfigModEX.MODID + "/master";
	}

	@Override
	public void onConfigChanged(Phase phase, int variables)
	{
		Weather2Remastered.info("onConfigChanged: " + phase + ", " + variables);
	}

	@Override
	public void onValueChanged(String variable, Object oldValue, Object newValue)
	{
		Weather2Remastered.info("onValueChanged [" + variable + "]: " + oldValue + ", " + newValue);
	}

=======
		return ConfigModEX.MODID + "/" + getName();
	}

	@Override
	public void onConfigChanged(Phase phase, int variables) {}

	@Override
	public void onValueChanged(String variable, Object oldValue, Object newValue) {}
>>>>>>> Stashed changes
}
