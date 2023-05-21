package net.mrbt0907.weather2.api.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.mrbt0907.weather2.util.ConfigList;

@Cancelable
public class EventRegisterStages extends Event
{
	public final ConfigList tornadoStageList;
	public final ConfigList hurricaneStageList;
	
	/**Event used to register custom tornado chances to the game.*/
	public EventRegisterStages(ConfigList tornadoStageList, ConfigList hurricaneStageList)
	{
		this.tornadoStageList = tornadoStageList;
		this.hurricaneStageList = hurricaneStageList;
	}
}