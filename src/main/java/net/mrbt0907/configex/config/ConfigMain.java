package net.mrbt0907.configex.config;

import net.minecraft.util.ResourceLocation;
import net.mrbt0907.configex.ConfigEX;
import net.mrbt0907.configex.api.ConfigAPI.ConfigType;
import net.mrbt0907.configex.api.ConfigAPI.ConfigValue;
import net.mrbt0907.configex.api.IConfigEX;

public class ConfigMain implements IConfigEX
{
	private ResourceLocation id = new ResourceLocation(ConfigEX.MODID, "configex");
	
	@ConfigValue(type=ConfigType.SERVER)
	public static boolean debug = false;
	
	@Override
	public ResourceLocation getConfigID()
	{
		return id;
	}

	@Override
	public void onConifgChanged() {}

	@Override
	public void onValueChanged(String configID, Object oldValue, Object newValue) {}

}
