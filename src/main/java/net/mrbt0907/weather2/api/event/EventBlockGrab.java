package net.mrbt0907.weather2.api.event;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EventBlockGrab extends Event
{
	private final IBlockState state;
	
	/**Not implemented yet*/
	public EventBlockGrab(IBlockState state)
	{
		this.state = state;
	}
	
	public IBlockState getBlockState()
	{
		return state;
	}
}