package net.mrbt0907.weather2.util;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mrbt0907.weather2.Weather2;

public class ReflectionHelper
{
	private static final Map<String, Field> FIELDS = new HashMap<String, Field>();
	private static Field modifiers = null;
	private static boolean enabled = false;
	
	@SuppressWarnings("deprecation")
	private static Field getField(Class <?> clazz, String fieldName, String fieldObfName)
	{
		Field field = FIELDS.get(fieldObfName);
		
		if (field == null)
		{
			field = net.minecraftforge.fml.relauncher.ReflectionHelper.findField(clazz, fieldName, fieldObfName);
			
			if (field != null)
				FIELDS.put(fieldObfName, field);
		}
			
		if (modifiers == null)
			try
			{
				modifiers = Field.class.getDeclaredField("modifiers");
				modifiers.setAccessible(true);
				enabled = true;
			}
			catch (Exception e) {}
		
		return field;
	}
	
	private static <T, E> void setValue(Field field, T instance, E value)
	{
		if (enabled && field != null)
			try
			{
				modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
				field.set(instance, value);
			}
			catch (Exception e) {}
	}
	
	private static <T> Object getValue(Field field, T instance)
	{
		if (enabled && field != null)
			try
			{
				modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
				return field.get(instance);
			}
			catch (Exception e) {}
		
		return null;
	}
	
	public static <T, E> void set(Class <?> clazz, T instance, String fieldName, String fieldObfName, E value)
	{
		if (clazz.isAssignableFrom(instance.getClass()))
			setValue(getField(clazz, fieldName, fieldObfName), instance, value);
	}

	public static <T> Object get(Class <?> clazz, T instance, String fieldName, String fieldObfName)
	{
		if (clazz.isAssignableFrom(instance.getClass()))
			return getValue(getField(clazz, fieldName, fieldObfName), instance);
		return null;
	}

	public static List<String> view(String... classes)
	{
		List<String> found = new ArrayList<String>();
		for (int i = 0; i < classes.length; i++)
			try
			{
				Class <?> clazz = (Class<?>) Class.forName(classes[i]);
				Field fields[] = clazz.getDeclaredFields();
				
				for (int ii = 0; ii < fields.length; ii++)	
				try
				{
					Weather2.debug("Found variable:  " + Modifier.toString(fields[ii].getModifiers()) + " " + fields[ii].getType().getSimpleName() + " " + fields[ii].getName() + ";");
					found.add("Found variable:  " + Modifier.toString(fields[ii].getModifiers()) + " " + fields[ii].getType().getSimpleName() + " " + fields[ii].getName() + ";");
				}
				catch (Exception e) {}
			}
			catch (Exception e) {}
		return found;
	}
}