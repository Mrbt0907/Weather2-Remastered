package net.mrbt0907.weather2.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlockSESnapshot extends BlockSnapshot
{
	public final int type;
	
	public BlockSESnapshot(IBlockState state, BlockPos pos, int type)
	{
		super(state, pos);
		this.type = type;
	}
}
