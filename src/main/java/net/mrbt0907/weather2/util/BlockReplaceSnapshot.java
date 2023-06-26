package net.mrbt0907.weather2.util;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.mrbt0907.weather2.weather.storm.StormObject;

public class BlockReplaceSnapshot extends BlockSnapshot
{
	public final StormObject storm;
	public final IBlockState newState;
	
	public BlockReplaceSnapshot(@Nonnull StormObject storm, IBlockState state, @Nonnull IBlockState oldState, @Nonnull BlockPos pos)
	{
		super(oldState, pos);
		this.storm = storm;
		newState = state;
	}
	
	@Override
	public String toString()
    {
        return "BlockReplaceSnapshot{block=" + block.getRegistryName() + ", replacement=" + (state == null ? "grab" : state.getBlock().getRegistryName()) + ", x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
