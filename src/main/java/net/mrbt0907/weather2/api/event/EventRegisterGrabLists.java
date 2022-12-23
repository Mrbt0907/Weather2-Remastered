package net.mrbt0907.weather2.api.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.mrbt0907.weather2.util.ConfigList;

@Cancelable
public class EventRegisterGrabLists extends Event
{
	public final ConfigList grabList;
	public final ConfigList replaceList;
	public final ConfigList entityList;
	public final ConfigList windResistanceList;
	
	/**Register your mod's custom grab rules here. This event fires right when the grab lists are edited in the config. The config file will always overwrite entries set in this event.
	 * <br>Fires on <b>MinecraftForge.EVENT_BUS</b>.
	 * <p><b>Parameters</b>
	 * <br>grabList - A list of blocks that storms can pick up and throw. Add a block to allow it to be picked up.
	 * <br>replaceList - A list of blocks that storms will replace. Add one or more replacement blocks to the block to define what the block changes to.
	 * <br>entityList - A list of entities that storms can pick up. <b><i>(NOT YET IMPLEMENTED!)</b></i>
	 * <br>windResistanceList - A list of wind resistances for blocks. Add a block with a float value to change the wind resistance of your block. Set the value to -1.0F or remove the block to prevent it from being grabbed.*/
	public EventRegisterGrabLists(ConfigList grabList, ConfigList replaceList, ConfigList entityList, ConfigList windResistanceList)
	{
		this.grabList = grabList;
		this.replaceList = replaceList;
		this.entityList = entityList;
		this.windResistanceList = windResistanceList;
	}
}