package net.mrbt0907.weather2.api.event;

import java.util.Map;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EventRegisterDimension extends Event
{
	public final Map<Integer, Boolean> weatherList;
	public final Map<Integer, Boolean> effectList;
	
	/**Register your mod's custom dimension settings here. The config file will always overwrite entries set in this event.
	 * <br>Fires on <b>MinecraftForge.EVENT_BUS</b>.
	 * <p><b>Parameters</b>
	 * <br>weatherList - A list of dimensions with a boolean that indicates whether Weather should be enabled or not.
	 * <br>effectList - A list of dimensions with a boolean that indicates whether Effects should be enabled or not.*/
	public EventRegisterDimension(Map<Integer, Boolean> weatherList, Map<Integer, Boolean> effectList)
	{
		this.weatherList = weatherList;
		this.effectList = effectList;
	}
}