package net.mrbt0907.weather2.util;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.mrbt0907.weather2.Weather2;

@SuppressWarnings("deprecation")
public class ReflectionHelper
{
	public static <T, E> void setAlt(Class <? extends T> classToAccess, T instance, E value, String... fieldNames)
	{
		Field field = net.minecraftforge.fml.relauncher.ReflectionHelper.findField(classToAccess, ObfuscationReflectionHelper.remapFieldNames(classToAccess.getName(), fieldNames));
		try
		{
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			field.set(instance, value);
		}

		catch (Exception e)
		{
		}
	}

	public static <T, E> void set(Class <T> classToAccess, T instance, E value, String... fieldNames)
	{
		Field field = net.minecraftforge.fml.relauncher.ReflectionHelper.findField(classToAccess, ObfuscationReflectionHelper.remapFieldNames(classToAccess.getName(), fieldNames));
		try
		{
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			field.set(instance, value);
		}

		catch (Exception e)
		{
		}
	}

	public static <T> Object get(Class <? super T > classToAccess, T instance, String... fieldNames)
	{
		Field field = net.minecraftforge.fml.relauncher.ReflectionHelper.findField(classToAccess, ObfuscationReflectionHelper.remapFieldNames(classToAccess.getName(), fieldNames));
		try
		{
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			return field.get(instance);
		}

		catch (Exception e)
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> List<String> view(String... classes)
	{
		List<String> found = new ArrayList<String>();
		for (int i = 0; i < classes.length; i++)
			try
			{
				Class <? extends T> clazz = (Class<? extends T>) Class.forName(classes[i]);
				Field fields[] = clazz.getDeclaredFields();
				
				for (int ii = 0; ii < fields.length; ii++)	
				try
				{
					Weather2.debug("Found variable:  " + Modifier.toString(fields[ii].getModifiers()) + " " + fields[ii].getType().getSimpleName() + " " + fields[ii].getName() + ";");
					found.add("Found variable:  " + Modifier.toString(fields[ii].getModifiers()) + " " + fields[ii].getType().getSimpleName() + " " + fields[ii].getName() + ";");
				}
				catch (Exception e)
				{
				}
			}
			catch (Exception e)
			{
			}
		return found;
	}
	
}


