package net.mrbt0907.configex.api;

<<<<<<< Updated upstream
import net.minecraft.util.ResourceLocation;

public interface IConfigEX
{	
	/**Sets the human readable name of this config*/
	public String getName();
	
	/**Sets the tooltip that will show when the player highlights the name of the config*/
	public String getDescription();
	
	/**Sets the banner texture that is shown at the top of the category in the advanced gui*/
	public default ResourceLocation getBannerTexture()
	{
		return null;
	}
	
	/**Sets the save location of the config. Defaults to getName()*/
=======
public interface IConfigEX
{

	public String getName();

	public String getDescription();

>>>>>>> Stashed changes
	public default String getSaveLocation()
	{
		return getName();
	}
<<<<<<< Updated upstream
	
	/**This gets called when this config detects one or more changes to it's variables. Runs before each changed variable is processes, and after each changed variable is processed<p>
	 * variables - If on phase START, it's how many variables will be checked. If on phase END, it's how many variables were changed*/
	public void onConfigChanged(Phase phase, int variables);
	
	/**This gets called for each variable that was changed when things need to be updated with the change of each variable's values*/
=======

	public void onConfigChanged(Phase phase, int variables);

>>>>>>> Stashed changes
	public void onValueChanged(String variable, Object oldValue, Object newValue);
	
	public static enum Phase
	{
		START, END;
	}
}
