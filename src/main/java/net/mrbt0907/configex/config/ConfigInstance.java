package net.mrbt0907.configex.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import modconfig.ConfigMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.mrbt0907.configex.ConfigEX;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.api.ConfigAPI.ConfigDoubleBounds;
import net.mrbt0907.configex.api.ConfigAPI.ConfigFloatBounds;
import net.mrbt0907.configex.api.ConfigAPI.ConfigIntegerBounds;
import net.mrbt0907.configex.api.ConfigAPI.ConfigLongBounds;
import net.mrbt0907.configex.api.ConfigAPI.ConfigShortBounds;
import net.mrbt0907.configex.api.ConfigAPI.ConfigType;
import net.mrbt0907.configex.api.ConfigAPI.ConfigValue;
import net.mrbt0907.configex.utils.ArrayUtils;

public class ConfigInstance
{
	IConfigEX config;
	List <ConfigField<?>> values = new ArrayList<ConfigField<?>>();
	Configuration file;
	
	public ConfigInstance(IConfigEX config)
	{
		ConfigEX.debug("Creating config instance for config " + config.getConfigID());
		ResourceLocation location = config.getConfigID();
		String category = "general";
		this.config = config;
		file = new Configuration(new File(ConfigMod.getSaveFolderPath() + "config/" + location.getResourceDomain() + "/" + location.getResourcePath() + ".cfg"));
		file.load();
		config.onConifgChanged();
		
		Field[] fields = config.getClass().getFields();
		ConfigValue annotation;
		Object value = null;
		
		
		for (Field field : fields)
		{
			annotation = field.getAnnotation(ConfigValue.class);
			
			if (annotation != null)
			{
				Side side = FMLCommonHandler.instance().getEffectiveSide();
				ConfigType type = annotation.type();
				ConfigField<?> fieldValue = null;
				Property property;
				
				if (type.equals(ConfigType.SERVER) && side == Side.CLIENT)
					continue;
				
				try
				{
					value = field.get(config);
				}
				catch (Exception e)
				{
					ConfigEX.error("Unable to get config value from " + field.getName() + ". Skipping...");
					e.printStackTrace();
					continue;
				}
				
				try 
				{
					if (value instanceof Integer)
					{
						ConfigIntegerBounds bound = field.getAnnotation(ConfigIntegerBounds.class);
						int min = bound == null ? Integer.MIN_VALUE : bound.min();
						int max = bound == null ? Integer.MAX_VALUE : bound.max();

						property = file.get(category, field.getName(), (int)value, annotation.description(), min, max);
						property.setValue(MathHelper.clamp(property.getInt(), min, max));
						fieldValue = new ConfigField<Integer>(field, property, config);
						field.setInt(config, property.getInt());
						ConfigEX.debug("Successfully added config value " + field.getName() + " as a integer");
					}
					else if (value instanceof Short)
					{
						ConfigShortBounds bound = field.getAnnotation(ConfigShortBounds.class);
						int min = bound == null ? Short.MIN_VALUE : bound.min();
						int max = bound == null ? Short.MAX_VALUE : bound.max();

						property = file.get(category, field.getName(), (short)value, annotation.description(), min, max);
						property.setValue(MathHelper.clamp(property.getInt(), min, max));
						fieldValue = new ConfigField<Short>(field, property, config);
						field.setShort(config, (short) property.getInt());
						ConfigEX.debug("Successfully added config value " + field.getName() + " as a short");
					}
					else if (value instanceof Long)
					{
						ConfigLongBounds bound = field.getAnnotation(ConfigLongBounds.class);
						double min = bound == null ? Long.MIN_VALUE : bound.min();
						double max = bound == null ? Long.MAX_VALUE : bound.max();

						property = file.get(category, field.getName(), (long)value, annotation.description(), min, max);
						property.setValue((long) MathHelper.clamp(property.getDouble(), min, max));
						fieldValue = new ConfigField<Long>(field, property, config);
						field.setLong(config,(long) property.getDouble());
						ConfigEX.debug("Successfully added config value " + field.getName() + " as a long");
					}
					else if (value instanceof Float)
					{
						ConfigFloatBounds bound = field.getAnnotation(ConfigFloatBounds.class);
						double min = bound == null ? Float.MIN_VALUE : bound.min();
						double max = bound == null ? Float.MAX_VALUE : bound.max();

						property = file.get(category, field.getName(), (float)value, annotation.description(), min, max);
						property.setValue(MathHelper.clamp(property.getDouble(), min, max));
						fieldValue = new ConfigField<Float>(field, property, config);
						field.setFloat(config, (float) property.getDouble());
						ConfigEX.debug("Successfully added config value " + field.getName() + " as a float");
					}
					else if (value instanceof Double)
					{
						ConfigDoubleBounds bound = field.getAnnotation(ConfigDoubleBounds.class);
						double min = bound == null ? Double.MIN_VALUE : bound.min();
						double max = bound == null ? Double.MAX_VALUE : bound.max();

						property = file.get(category, field.getName(), (double)value, annotation.description(), min, max);
						property.setValue(MathHelper.clamp(property.getDouble(), min, max));
						fieldValue = new ConfigField<Double>(field, property, config);
						field.setDouble(config, property.getDouble());
						ConfigEX.debug("Successfully added config value " + field.getName() + " as a double");
					}
					else if (value instanceof String)
					{
						property = file.get(category, field.getName(), (String)value, annotation.description());
						fieldValue = new ConfigField<String>(field, property, config);
						field.set(config, property.getString());
						ConfigEX.debug("Successfully added config value " + field.getName() + " as a string");
						
					}
					else if (field.getGenericType().getTypeName().equals("java.util.List<java.lang.String>"))
					{	
						List<String> array = new ArrayList<String>();
						
						for (Object obj : (List<?>)value)
							array.add(String.valueOf(obj));
						
						String[] list = new String[array.size()];
						
						property = file.get(category, field.getName(), array.toArray(list), annotation.description());
						fieldValue = new ConfigField<List<String>>(field, property, config);
						field.set(config, Arrays.asList(property.getStringList()));
						ConfigEX.debug("Successfully added config value " + field.getName() + " as a list");
					}
					else if (field.getGenericType().getTypeName().equals("java.util.Map<java.lang.String, java.lang.String>"))
					{
						List<String> array = new ArrayList<String>();
						
						for (Entry<?, ?> obj : ((Map<?, ?>)value).entrySet())
							array.add(String.valueOf(obj.getKey()).replaceAll("\\=", "") + "=" + String.valueOf(obj.getValue()).replaceAll("\\=", ""));
						
						String[] list = new String[array.size()];
						
						property = file.get(category, field.getName(), array.toArray(list), annotation.description());
						fieldValue = new ConfigField<Map<String, String>>(field, property, config);
						field.set(config, ArrayUtils.getMap(Arrays.asList(property.getStringList())));
						ConfigEX.debug("Successfully added config value " + field.getName() + " as a map");
					}
					else
					{
						ConfigEX.error("Unable to add config value " + field.getName() + " because of an invalid type. Skipping...");
						continue;
					}
				}
				catch (Exception e)
				{
					ConfigEX.error("Unable to add config value " + field.getName() + " because value cannot be set. Skipping...");
					e.printStackTrace();
					continue;
				}

				if (!annotation.name().isEmpty())
					fieldValue.setName(annotation.name());
				fieldValue.setDescription(annotation.description());
				
				fieldValue.setType(annotation.type());
				config.onValueChanged(fieldValue.getID().getResourcePath(), fieldValue.getOldValue(), fieldValue.getValue());
				fieldValue.updateOldValue();
				ConfigEX.debug("New config value \"" + field.getName() + "\". Default Value: " + String.valueOf(fieldValue.getDefaultValue()) + ", Current Value: " + String.valueOf(fieldValue.getValue()));
				values.add(fieldValue);
			}
		}
		file.save();
	}
	
	public void setToDefault()
	{
		values.forEach(field -> field.setToDefault());
	}
	
	public ConfigField<?> getValue(String key)
	{
		for (ConfigField<?> field : values)
			if (field.getID().getResourcePath().equals(key))
				return field;
		return null;
	}
	
	public List<ConfigField<?>> getValues()
	{
		return values;
	}
	
	public boolean isConfigChanged()
	{
		for (ConfigField<?> value : values)
			if (value.isChanged())
				return true;
		return false;
	}
	
	public Configuration getConfigFile()
	{
		return file;
	}
	
	public IConfigEX getConfig()
	{
		return config;
	}
	
	public ResourceLocation getConfigID()
	{
		return config.getConfigID();
	}
}