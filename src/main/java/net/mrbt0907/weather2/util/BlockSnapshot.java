package net.mrbt0907.weather2.util;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlockSnapshot
{
	public final IBlockState state;
	public final Block block;
	public final BlockPos pos;
	public final int meta, x, y, z;
	
	public BlockSnapshot(@Nonnull IBlockState state, BlockPos pos)
	{
		this.state = state;
		this.pos = pos;
		block = state.getBlock();
		meta = block.getMetaFromState(state);
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
	}
	
	public double distance(BlockSnapshot snapshot)
	{
		return distance(snapshot.x, snapshot.y, snapshot.z);
	}
	
	public double distance(double x, double y, double z)
	{
		return Maths.distanceSq(this.x, this.y, this.z, x, y, z);
	}
	
	public int hashCode()
	{
		return x + z << 8 + y << 16;
	}
	
	public boolean equals(Object object)
	{
		return object instanceof BlockSnapshot && ((BlockSnapshot)object).hashCode() == hashCode();
	}
	
	public String toString()
    {
        return "BlockSnapshot{block=" + block.getRegistryName() + ", x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
