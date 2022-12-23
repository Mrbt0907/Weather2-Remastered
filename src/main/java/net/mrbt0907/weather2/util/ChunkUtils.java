package net.mrbt0907.weather2.util;

import java.util.function.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorDebug;

public class ChunkUtils
{
	private static final IBlockState AIR = Blocks.AIR.getDefaultState();
	private static Chunk cachedChunk;
	
	public static boolean isValidPos(World world, int y)
	{
		return world != null && y > -1 && y < 256;
	}
	
	public static IBlockState getBlockState(World world, BlockPos pos)
	{
		return getBlockState(world, pos.getX(), pos.getY(), pos.getZ());
	}
	
	public static IBlockState getBlockState(World world, int x, int y, int z)
	{
		IChunkProvider provider = world.getChunkProvider();
		int cX = x >> 4, cZ = z >> 4;
		
		if (cachedChunk == null || cachedChunk != null && cX != cachedChunk.x && cZ != cachedChunk.z)
			cachedChunk = provider.provideChunk(cX, cZ);
		
		if (!isValidPos(world, y))
		{
			return AIR;
		}
		
		if (world.getWorldType() == WorldType.DEBUG_ALL_BLOCK_STATES)
		{
			if (y == 60)
				return Blocks.BARRIER.getDefaultState();
			else if (y == 70)
				return ChunkGeneratorDebug.getBlockStateFor(x, z);
			else
				return AIR;
		}
		else
		{
			try
			{
				ExtendedBlockStorage[] storages = cachedChunk.getBlockStorageArray(); 
				if (y >= 0 && y >> 4 < storages.length)
				{
					ExtendedBlockStorage storage = storages[y >> 4];

					if (storage != Chunk.NULL_BLOCK_STORAGE)
						return storage.get(x & 15, y & 15, z & 15);
				}

				
			}
			catch (Throwable throwable) {}
			
			return AIR;
		}
	}
	
	public static boolean setBlockState(World world, BlockPos pos, IBlockState state)
	{
		return setBlockState(world, pos.getX(), pos.getY(), pos.getZ(), state);
	}
	
	public static boolean setBlockState(World world, int x, int y, int z, IBlockState state)
	{
		if (!isValidPos(world, y) || state == null) return false;
		
		IBlockState oldState = getBlockState(world, x, y, z);
		BlockPos pos = new BlockPos(x, y, z);
		return updateBlockState(world, pos, state, oldState);
	}
	
	public static void setBlockStates(World world, int x, int y, int z, int width, int height, Function<IBlockState, IBlockState> replacement)
	{
		
	}
	
	private static boolean updateBlockState(World world, BlockPos pos, IBlockState state, IBlockState oldState)
	{
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
}
