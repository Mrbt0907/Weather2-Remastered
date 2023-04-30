package net.mrbt0907.configex.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.util.ResourceLocation;
import net.mrbt0907.configex.ConfigEX;
import net.mrbt0907.configex.ConfigProcessor;
import net.mrbt0907.configex.config.ConfigField;
import net.mrbt0907.configex.config.ConfigInstance;

public class ConfigAPI
{
	public static void addConfig(IConfigEX... configs)
	{
		ConfigProcessor.addConfig(configs);
	}
	
	public static IConfigEX getConfig(ResourceLocation configID)
	{
		return ConfigProcessor.getConfig(configID);
	}
	
	public static ConfigInstance getConfigInstance(IConfigEX config)
	{
		return ConfigProcessor.getConfigInstance(config);
	}
	
	public static void resetToDefault(IConfigEX config, String... ids)
	{
		if (config == null)
		{
			ConfigEX.error("Failed to set values of a config to default because the config was null");
			return;
		}
		
		ConfigInstance instance = ConfigProcessor.getConfigInstance(config);
		
		if (instance == null)
		{
			ConfigEX.error("Failed to set values of config " + config.getConfigID() + " to default because config was not registered");
			return;
		}
		
		if (ids.length == 0)
			instance.setToDefault();
		else
		{
			ConfigField<?> field;
			for (String id : ids)
			{
				field = instance.getValue(id);
				if (field != null)
					field.setToDefault();
			}
		}
		ConfigEX.debug("Successfully set all values of config " + config.getConfigID() + " to default");
	}
	
	public static void saveConfigChanges()
	{
		ConfigProcessor.saveConfigs();
	}
	
	public static enum ConfigType
	{
		CLIENT, COMMON, SERVER;
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigValue
	{
		ConfigType type() default ConfigType.COMMON;
		String name() default "";
		String description() default "";
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigShortBounds
	{
		short min() default Short.MIN_VALUE;
		short max() default Short.MAX_VALUE;
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigIntegerBounds
	{
		int min() default Integer.MIN_VALUE;
		int max() default Integer.MAX_VALUE;
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigLongBounds
	{
		long min() default Long.MIN_VALUE;
		long max() default Long.MAX_VALUE;
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigFloatBounds
	{
		float min() default Float.MIN_VALUE;
		float max() default Float.MAX_VALUE;
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigDoubleBounds
	{
		double min() default Double.MIN_VALUE;
		double max() default Double.MAX_VALUE;
	}
}
