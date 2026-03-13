package net.mrbt0907.configex.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

<<<<<<< Updated upstream
public class ConfigAnnotations {
	/**Tells the compiler to ignore this variable when getting variables for the config*/
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Ignore {}
	/**Forces all clients to use the server's value instead of their own<br>This also hides the variable from client settings in a multiplayer game*/
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Enforce {}
	/**Adds this config to the client config, which is ignored by the server*/
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface ClientSide {}
	
	/**Adds this config to the world's config, which is ignored by all clients*/
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface ServerSide {}
	
	/**Gives the variable a human readable name in the config*/
=======
public class ConfigAnnotations
{

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Ignore {}
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Enforce {}
	

>>>>>>> Stashed changes
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Name
	{
		String value();
	}
	
<<<<<<< Updated upstream
	/**Gives the variable a tooltip when highlighted over. Supports \n*/
=======

>>>>>>> Stashed changes
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Comment
	{
		String[] value();
	}
	
<<<<<<< Updated upstream
	/**Sets the variable's required permission level to modify it<p>
	 * 0 - All players can modify this
	 * 1 - Moderators can modify this
	 * 2 - Game masters can modify this
	 * 3 - Ops/Admins can modify this
	 * 4 - Server Owner/Server Console can modify this*/
=======

>>>>>>> Stashed changes
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Permission
	{
		int value();
	}
	
<<<<<<< Updated upstream
	/**Hides this variable from all players who do not have permission to edit this variable*/
=======

>>>>>>> Stashed changes
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Hidden {}
	
<<<<<<< Updated upstream
	/**Notifies players that this variable only changes when the world is reloaded*/
=======

>>>>>>> Stashed changes
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface RequiresWorldReload {}
	
<<<<<<< Updated upstream
	/**Limits the range of the variable to a specified minimum and maximum integer value*/
=======

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface RequiresRestart {}
	

>>>>>>> Stashed changes
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface IntegerRange
	{
		int min() default Integer.MIN_VALUE;
		int max() default Integer.MAX_VALUE;
	}
	
<<<<<<< Updated upstream
	/**Limits the range of the variable to a specified minimum and maximum short value*/
=======

>>>>>>> Stashed changes
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface ShortRange
	{
		short min() default Short.MIN_VALUE;
		short max() default Short.MAX_VALUE;
	}
	
<<<<<<< Updated upstream
	/**Limits the range of the variable to a specified minimum and maximum long value*/
=======

>>>>>>> Stashed changes
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface LongRange
	{
		long min() default Long.MIN_VALUE;
		long max() default Long.MAX_VALUE;
	}

<<<<<<< Updated upstream
	/**Limits the range of the variable to a specified minimum and maximum float value*/
=======

>>>>>>> Stashed changes
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface FloatRange
	{
		float min() default -Float.MAX_VALUE;
		float max() default Float.MAX_VALUE;
	}
	
<<<<<<< Updated upstream
	/**Limits the range of the variable to a specified minimum and maximum double value*/
=======

>>>>>>> Stashed changes
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface DoubleRange
	{
		double min() default -Double.MAX_VALUE;
		double max() default Double.MAX_VALUE;
	}
<<<<<<< Updated upstream
}
=======


	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Slider
	{
		double min() default -Double.MAX_VALUE;
		double max() default Double.MAX_VALUE;
	}
}
>>>>>>> Stashed changes
