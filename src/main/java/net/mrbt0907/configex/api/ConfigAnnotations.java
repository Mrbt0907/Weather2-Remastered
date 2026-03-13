package net.mrbt0907.configex.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ConfigAnnotations
{

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Ignore {}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Enforce {}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Name
	{
		String value();
	}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Comment
	{
		String[] value();
	}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Permission
	{
		int value();
	}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Hidden {}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface RequiresWorldReload {}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface RequiresRestart {}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface IntegerRange
	{
		int min() default Integer.MIN_VALUE;
		int max() default Integer.MAX_VALUE;
	}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface ShortRange
	{
		short min() default Short.MIN_VALUE;
		short max() default Short.MAX_VALUE;
	}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface LongRange
	{
		long min() default Long.MIN_VALUE;
		long max() default Long.MAX_VALUE;
	}


	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface FloatRange
	{
		float min() default -Float.MAX_VALUE;
		float max() default Float.MAX_VALUE;
	}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface DoubleRange
	{
		double min() default -Double.MAX_VALUE;
		double max() default Double.MAX_VALUE;
	}


	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Slider
	{
		double min() default -Double.MAX_VALUE;
		double max() default Double.MAX_VALUE;
	}
}