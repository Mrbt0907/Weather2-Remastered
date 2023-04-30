package net.mrbt0907.configex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.mrbt0907.configex.api.ConfigAPI.ConfigType;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.config.ConfigField;
import net.mrbt0907.configex.config.ConfigInstance;
import net.mrbt0907.configex.network.NetworkHandler;

public class ConfigProcessor
{
	private static List<ConfigInstance> configs = new ArrayList<ConfigInstance>();
	
	
	public static void addConfig(IConfigEX... configList)
	{
		boolean truth = true; 
		for (IConfigEX config : configList)
		{
			for (ConfigInstance c : configs)
				if (c.getConfigID().equals(config.getConfigID()))
				{
					truth = false;
					break;
				}
			
			if (truth)
			{
				configs.add(new ConfigInstance(config));
				ConfigEX.debug("Added new config " + config.getConfigID());
			}
			else
				ConfigEX.warn("Duplicate config " + config.getConfigID());
		}	
	}
	
	public static IConfigEX getConfig(ResourceLocation location)
	{
		if (location != null)
			for (ConfigInstance instance : configs)
				if (instance.getConfigID().equals(location))
					return instance.getConfig();
		
		return null;
	}
	
	public static ConfigInstance getConfigInstance(IConfigEX config)
	{
		
		if (config != null)
			for (ConfigInstance instance : configs)
				if (instance.getConfigID().equals(config.getConfigID()))
					return instance;
		
		return null;
	}
	
	public static void applyServerValues(NBTTagCompound nbt)
	{
		ConfigInstance instance = getConfigInstance(getConfig(new ResourceLocation(nbt.getString("id"))));
		
		if (instance != null)
		{
			NBTTagCompound nbtValues = (NBTTagCompound) nbt.getTag("values");
			
			nbtValues.getKeySet().forEach(id -> {});
		}
	}
	
	public static void sendServerValues(ConfigInstance instance)
	{
		NBTTagCompound nbt = new NBTTagCompound(), nbtValues = new NBTTagCompound(), nbtList, nbtListValues;
		nbt.setString("id", instance.getConfigID().toString());
		
		for (ConfigField<?> field : instance.getValues())
		{
			if (field.getType().equals(ConfigType.SERVER))
			{
				Object value = field.getValue();
				if (value instanceof Integer)
					nbtValues.setInteger(field.getID().getResourcePath(), (int)value);
				else if (value instanceof Short)
					nbtValues.setShort(field.getID().getResourcePath(), (short)value);
				else if (value instanceof Long)
					nbtValues.setLong(field.getID().getResourcePath(), (long)value);
				else if (value instanceof Float)
					nbtValues.setFloat(field.getID().getResourcePath(), (float)value);
				else if (value instanceof Double)
					nbtValues.setDouble(field.getID().getResourcePath(), (double)value);
				else if (value instanceof String)
					nbtValues.setString(field.getID().getResourcePath(), (String)value);
				else if (field.getField().getGenericType().getTypeName().equals("java.util.List<java.lang.String>"))
				{
					nbtList = new NBTTagCompound();
					nbtListValues = new NBTTagCompound();
					
					int size = ((List<?>)value).size(); 
					for (int i = 0; i < size; i++)
						nbtListValues.setString(String.valueOf(i), String.valueOf(((List<?>)value).get(i)));
					
					nbtList.setByte("type", (byte) 0);
					nbtList.setTag("values", nbtListValues);
					nbtValues.setTag(field.getID().getResourcePath(), nbtList);
				}
				else if (field.getField().getGenericType().getTypeName().equals("java.util.Map<java.lang.String, java.lang.String>"));
				{
					nbtList = new NBTTagCompound();
					nbtListValues = new NBTTagCompound();
					
					for (Entry<?,?> entry : ((Map<?,?>)value).entrySet())
						nbtListValues.setString(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));						
					
					nbtList.setByte("type", (byte) 1);
					nbtList.setTag("values", nbtListValues);
					nbtValues.setTag(field.getID().getResourcePath(), nbtList);
				}
			}
		}
		nbt.setTag("values", nbtValues);
		NetworkHandler.sendClientPacket(0, nbt);
	}
	
	public static void saveConfigs()
	{
		configs.forEach(config ->
		{
			if (isConfigChanged(config))
			{
				config.getValues().forEach(value -> value.save());
				onConfigChanged(config);
				config.getConfigFile().save();
			}
			ConfigEX.debug("Saved config " + config.getConfigID());
		});
	}
	
	public static void onConfigChanged(ConfigInstance config)
	{
		if (isConfigChanged(config))
		{
			config.getConfig().onConifgChanged();
			
			config.getValues().forEach(value ->
			{
				if (value.isChanged())
				{
					config.getConfig().onValueChanged(value.getID().getResourcePath(), value.getOldValue(), value.getValue());
					value.updateOldValue();
				}
			});
		}
	}
	
	public static boolean isConfigChanged(ConfigInstance config)
	{
		return config != null && config.isConfigChanged();
	}
}
