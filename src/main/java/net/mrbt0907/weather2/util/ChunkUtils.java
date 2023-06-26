package net.mrbt0907.weather2.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.mrbt0907.weather2.config.ConfigGrab;

public class ChunkUtils
{
	private static final IBlockState AIR = Blocks.AIR.getDefaultState();
	private final List<ChunkPosEX> chunks = new ArrayList<ChunkPosEX>();
	
	public void tick()
	{
		if (!chunks.isEmpty())
		{
			Iterator<ChunkPosEX> iterator = chunks.iterator();
			ChunkPosEX pos;
			while(iterator.hasNext())
			{
				pos = iterator.next();
				pos.ticksExisted++;
				if (pos.ticksExisted > ConfigGrab.chunk_cache_lifetime)
					iterator.remove();
			}
		}
	}
	
	public boolean isValidPos(World world, int y)
	{
		return world != null && y > -1 && y < 256;
	}
	
	public IBlockState getBlockState(World world, BlockPos pos)
	{
		return ConfigGrab.disableGrabOptimizations ? world.getBlockState(pos) : getBlockState(world, pos.getX(), pos.getY(), pos.getZ());
	}
	
	public IBlockState getBlockState(World world, int x, int y, int z)
	{
		if (ConfigGrab.disableGrabOptimizations) return world.getBlockState(new BlockPos(x, y ,z));
		if (!isValidPos(world, y))
			return AIR;
		
		IChunkProvider provider = world.getChunkProvider();
		ChunkPosEX pos = getChunkPos(x, z);
		
		if (pos.chunk == null)
		{
			pos.chunk = provider.provideChunk(pos.cX, pos.cZ);
			chunks.add(pos);
			return pos.chunk.getBlockState(z, y, z);
		}
		
		pos.ticksExisted = 0;
		return pos.chunk.getBlockState(x, y, z);
	}
	
	public boolean setBlockState(World world, BlockPos pos, IBlockState state)
	{
		return ConfigGrab.disableGrabOptimizations ? world.setBlockState(pos, state, 3) : setBlockState(world, pos.getX(), pos.getY(), pos.getZ(), state, getBlockState(world, pos.getX(), pos.getY(), pos.getZ()));
		//return world.setBlockState(pos, state, 3);
	}
	
	public boolean setBlockState(World world, int x, int y, int z, IBlockState state)
	{
		return ConfigGrab.disableGrabOptimizations ? world.setBlockState(new BlockPos(x, y, z), state, 3) : setBlockState(world, x, y, z, state, getBlockState(world, x, y, z));
		//return world.setBlockState(new BlockPos(x, y, z), state, 3);
	}
	
	public boolean setBlockState(World world, int x, int y, int z, IBlockState state, IBlockState oldState)
	{
		if (ConfigGrab.disableGrabOptimizations)
			return world.setBlockState(new BlockPos(x, y ,z), state, 3);
		
		if (!isValidPos(world, y) || state == null) return false;
		
		BlockPos pos = new BlockPos(x, y, z);
		return updateBlockState(world, pos, state, oldState);
	}
	
	public void clearCache()
	{
		chunks.clear();
	}
	
	private boolean updateBlockState(World world, BlockPos pos, IBlockState state, IBlockState oldState)
	{
		Chunk cachedChunk = null;
		if (cachedChunk.setBlockState(pos, state) != null)
		{
			if (state.getLightOpacity(world, pos) != oldState.getLightOpacity(world, pos) || state.getLightValue(world, pos) != oldState.getLightValue(world, pos))
				world.checkLight(pos);
			
			if (!world.isRemote)
			{
				if (cachedChunk == null || cachedChunk.isPopulated())
					world.notifyBlockUpdate(pos, oldState, state, 3);
	
				world.notifyNeighborsRespectDebug(pos, oldState.getBlock(), true);
	
				if (state.hasComparatorInputOverride())
					world.updateComparatorOutputLevel(pos, state.getBlock());
			}
			return true;
		}
		else
			return false;
	}
	
	public ChunkPosEX getChunkPos(int x, int z)
	{
		int cX = x >> 4;
		int cZ = z >> 4;
		
		
		for (ChunkPosEX pos : chunks)
			if (cX == pos.cX && cZ == pos.cZ)
				return pos;
		
		return new ChunkPosEX(x, z);
	}
	
	public static class ChunkPosEX
	{
		public final int cX;
		public final int cZ;
		public Chunk chunk;
		public int ticksExisted;
		
		public ChunkPosEX(BlockPos pos)
		{
			this(pos.getX(), pos.getZ());
		}
		
		public ChunkPosEX(int x, int z)
		{
			cX = x >> 4;
			cZ = z >> 4;
		}
		
		public int hashCode()
	    {
	        return (1664525 * cX + 1013904223) ^ (1664525 * (cZ ^ -559038737) + 1013904223);
	    }
		
		public boolean equals(Object obj)
		{
			return obj == this || obj instanceof ChunkPosEX && ((ChunkPosEX)obj).cX == cX && ((ChunkPosEX)obj).cZ == cZ;
		}
		
		public String toString()
	    {
	        return "[" + cX + ", " + cZ + "]";
	    }
	}
}