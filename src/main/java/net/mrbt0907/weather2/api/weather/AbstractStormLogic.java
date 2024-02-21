package net.mrbt0907.weather2.api.weather;

import java.util.List;

import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.weather.storm.StormObject;

public abstract class AbstractStormLogic extends AbstractDebugging
{
	/**The storm that the renderer is attached to*/
	public StormObject storm;
	private static long delta;
	
	/**Used to spawn particles based on various variables in a storm. Examples are found in net.mrbt0907.weather2.client.weather.tornado*/
	public AbstractStormLogic(StormObject storm)
	{
		this.storm = storm;
	}
	
	public final void tick()
	{
		int attempts = 0;
		
		if (storm != null)
		{
			delta = System.nanoTime();
			while (attempts > -1)
				try
				{
					onTickProgression();
					attempts = -1;
				}
				catch(Exception e)
				{
					attempts++;
					
					if (attempts < 3)
					{
						Weather2.warn("Storm's logic onTickProgression() has encountered an error. Retrying...");
						e.printStackTrace();
					}
					else
					{
						Weather2.warn("Storm's logic onTickProgression() has failed to run correctly. Disabling storm logic...");
						e.printStackTrace();
						attempts = -1;
					}
				}
		}
	}
	
	/**Used to control how the storm progresses each tick.*/
	public abstract void onTickProgression();
	/**Used to add extra information to the debug renderer. Null is acceptable*/
	public abstract List<String> onDebugInfo();
	/**Used when this storm logic is being removed.*/
	public abstract void cleanupLogic();
	
	public final void cleanup()
	{
		
	}
}
