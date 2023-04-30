package net.mrbt0907.weather2.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.mrbt0907.weather2.weather.storm.StormObject;

public class BlockSnapshot
{
	public StormObject storm;
	public IBlockState state;
	public IBlockState oldState;
	public BlockPos pos;
	
	public BlockSnapshot(StormObject storm, IBlockState state, IBlockState oldState, BlockPos pos)
	{
		this.storm = storm;
		this.state = state;
		this.oldState = oldState;
		this.pos = pos;
	}
	
	public void clear()
	{
		state = null;
		oldState = null;
		pos = null;
	}
}
