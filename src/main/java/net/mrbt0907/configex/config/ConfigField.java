package net.mrbt0907.configex.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
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

public class ConfigField <T>
{
	private IConfigEX config;
	private Field field;
	private Property property;
	private ResourceLocation id;
	private String name;
	private String description;
	private ConfigType type;
	private T defaultValue;
	private T oldValue;
	private T serverValue;
	
	public ConfigField(Field field, Property property, IConfigEX config)
	{
		this.type = ConfigType.COMMON;
		this.config = config;
		this.field = field;
		this.property = property;
		this.id = new ResourceLocation(config.getConfigID().getResourceDomain(), config.getConfigID().getResourcePath() + "." + field.getName().replaceAll("\\_", "."));
		this.defaultValue = getValue();
		this.oldValue = defaultValue;
		serverValue = null;
		name = id.getResourcePath();
	}
	
	public void save()
	{
		if (type == ConfigType.SERVER && (FMLCommonHandler.instance().getEffectiveSide().equals(Side.SERVER) || serverValue == null))
		{
			T value = getValue();
			
			try
			{	
				if (value instanceof Integer)
				{
					ConfigIntegerBounds bound = field.getAnnotation(ConfigIntegerBounds.class);
					int min = bound == null ? Integer.MIN_VALUE : bound.min();
					int max = bound == null ? Integer.MAX_VALUE : bound.max();
	
					property.setValue(MathHelper.clamp((int) value, min, max));
					field.setInt(config, property.getInt());
				}
				else if (value instanceof Short)
				{
					ConfigShortBounds bound = field.getAnnotation(ConfigShortBounds.class);
					int min = bound == null ? Short.MIN_VALUE : bound.min();
					int max = bound == null ? Short.MAX_VALUE : bound.max();
	
					property.setValue(MathHelper.clamp((short) value, min, max));
					field.setShort(config, (short) property.getInt());
				}
				else if (value instanceof Long)
				{
					ConfigLongBounds bound = field.getAnnotation(ConfigLongBounds.class);
					double min = bound == null ? Long.MIN_VALUE : bound.min();
					double max = bound == null ? Long.MAX_VALUE : bound.max();
	
					property.setValue(MathHelper.clamp((long) value, min, max));
					field.setLong(config,(long) property.getDouble());
				}
				else if (value instanceof Float)
				{
					ConfigFloatBounds bound = field.getAnnotation(ConfigFloatBounds.class);
					double min = bound == null ? Float.MIN_VALUE : bound.min();
					double max = bound == null ? Float.MAX_VALUE : bound.max();
					
					property.setValue(MathHelper.clamp((float) value, min, max));
					field.setFloat(config, (float) property.getDouble());
				}
				else if (value instanceof Double)
				{
					ConfigDoubleBounds bound = field.getAnnotation(ConfigDoubleBounds.class);
					double min = bound == null ? Double.MIN_VALUE : bound.min();
					double max = bound == null ? Double.MAX_VALUE : bound.max();
	
					property.setValue(MathHelper.clamp((double) value, min, max));
					field.setDouble(config, property.getDouble());
				}
				else if (value instanceof String)
					property.setValue((String) value);
				else if (field.getGenericType().getTypeName().equals("java.util.List<java.lang.String>"))
				{	
					List<String> array = new ArrayList<String>();
					
					for (Object obj : (List<?>)value)
						array.add(String.valueOf(obj));
					
					String[] list = new String[array.size()];
					
					property.setValues(array.toArray(list));
				}
	
				else if (field.getGenericType().getTypeName().equals("java.util.Map<java.lang.String, java.lang.String>"))
				{
					List<String> array = new ArrayList<String>();
					
					for (Entry<?, ?> obj : ((Map<?, ?>)value).entrySet())
						array.add(String.valueOf(obj.getKey()).replaceAll("\\=", "") + "=" + String.valueOf(obj.getValue()).replaceAll("\\=", ""));
					
					String[] list = new String[array.size()];
					
					property.setValues(array.toArray(list));
					ConfigEX.debug("Successfully added config value " + field.getName() + " as a map");
				}
				else
				{
					ConfigEX.debug("Failed to save config value " + field.getName() + " to the config because value is an invalid type");
					return;
				}
				
				ConfigEX.debug("Successfully saved config value " + field.getName() + " to the config");
			}
			catch (Exception e)
			{
				ConfigEX.debug("Failed save config value " + field.getName() + " to the config because value could not be updated");
				e.printStackTrace();
			}
		}
	}
	
	public ResourceLocation getID()
	{
		return id;
	}
	
	public boolean isChanged()
	{
		return !getValue().equals(oldValue);
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public T getDefaultValue()
	{
		return defaultValue;
	}
	
	public void setServerValue(T value)
	{
		serverValue = value;
	}
	
	@SuppressWarnings("unchecked")
	public T getValue()
	{
		try
		{
			return type == ConfigType.SERVER && serverValue != null ? serverValue : (T) field.get(config);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public void updateOldValue()
	{
		oldValue = getValue();
	}
	
	public T getOldValue()
	{
		return oldValue;
	}
	
	public void clearServerValue()
	{
		serverValue = null;
	}
	
	public ConfigField<T> setName(String name)
	{
		this.name = name;
		return this;
	}
	
	public ConfigField<T> setDescription(String description)
	{
		this.description = description;
		return this;
	}
	
	public ConfigField<T> setType(ConfigType type)
	{
		this.type = type;
		return this;
	}
	
	public ConfigType getType()
	{
		return type;
	}

	public Field getField()
	{
		return field;
	}
	
	public void setToDefault()
	{
		oldValue = getValue();
		try
		{
			field.set(config, defaultValue);
		}
		catch (Exception e)
		{
			ConfigEX.debug("Failed config value " + field.getName() + " to default because value could not be updated");
			e.printStackTrace();
		}
	}
}
