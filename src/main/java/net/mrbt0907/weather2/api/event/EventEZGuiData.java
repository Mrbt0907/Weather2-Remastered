package net.mrbt0907.weather2.api.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class EventEZGuiData extends Event
{
	public final String id;
	public final int oldValue;
	public final int newValue;
	
	/**Used to process EZ Gui options that were changed. Use this to change config values based on the option that was selected.*/
	public EventEZGuiData(String id, int oldValue, int newValue)
	{
		this.id = id;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
}