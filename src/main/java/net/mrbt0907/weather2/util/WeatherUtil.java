package net.mrbt0907.weather2.util;

import java.util.Calendar;

import CoroUtil.util.CoroUtilCompatibility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.FMLClientHandler;

public class WeatherUtil {

	public static boolean isPaused() {return FMLClientHandler.instance().getClient().isGamePaused();}
	
	public static boolean isPausedSideSafe(World world) {return world.isRemote ? isPaused() : false;}
	
	//Terrain grabbing
	/*public static boolean shouldGrabBlock(World parWorld, IBlockState state, StormObject so)
	{
		try
		{
			Block block = state.getBlock();
			boolean isListed = false;
			
			if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
			{
				return false;
			}
			
			if (ConfigTornado.Storm_Tornado_GrabCond_List)
			{
				try
				{
					if (ConfigTornado.Storm_Tornado_RefinedGrabRules || ConfigTornado.Storm_Tornado_GrabCond_StrengthGrabbing)
					{
						if (!ConfigTornado.Storm_Tornado_GrabListBlacklistMode)
						{
							if (((Boolean)WeatherUtilBlock.grab_list.get(block)).booleanValue())
							{
								isListed = true;
							}
						}
						else
						{
							if (!((Boolean)WeatherUtilBlock.grab_list.get(block)).booleanValue())
							{
								isListed = true;
							}
						}
					}
					else
					{
						if (!ConfigTornado.Storm_Tornado_GrabListBlacklistMode)
						{
							if (((Boolean)WeatherUtilBlock.grab_list.get(block)).booleanValue())
							{
								return true;
							}
						}
						else
						{
							if (!((Boolean)WeatherUtilBlock.grab_list.get(block)).booleanValue())
							{
								return true;
							}
						}
					}
					
					
				}
				catch (Exception e)
				{
					//sometimes NPEs, just assume false if so
					return false;
				}
			}
			
			if (ConfigTornado.Storm_Tornado_RefinedGrabRules && (!ConfigTornado.Storm_Tornado_GrabCond_List || (ConfigTornado.Storm_Tornado_GrabCond_List && isListed)))
			{
				if (shouldRemoveBlock(block, so.stormStage))
					return true;
					 
				if (!CoroUtilCompatibility.canTornadoGrabBlockRefinedRules(state))
					return false;
			}
			
			if (block == BlockRegistry.weather_constructor)
				return false;
			
			if (ConfigTornado.Storm_Tornado_GrabCond_StrengthGrabbing && (!ConfigTornado.Storm_Tornado_GrabCond_List || (ConfigTornado.Storm_Tornado_GrabCond_List && isListed)))
			{
				float strMin = 0.0F;
				float strMax = 0.74F * (((so.stormStage - 4) / 1.10F) + 1);

				if (block == null)
					return false;
				else
				{
					float strVsBlock = block.getBlockHardness(block.getDefaultState(), parWorld, new BlockPos(0, 0, 0));
		
					if ((strVsBlock <= strMax && strVsBlock >= strMin) && (!ConfigTornado.Storm_Tornado_RefinedGrabRules || (ConfigTornado.Storm_Tornado_RefinedGrabRules && shouldRemoveBlock(block, so.stormStage))))
							return true;
				}
			}
			
			return false;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}*/
	/*
	public static boolean safetyCheck(Block id)
	{
		return id != Blocks.BEDROCK && id != Blocks.CHEST && id != Blocks.JUKEBOX;
	}
	
	public static boolean shouldRemoveBlock(Block blockID)
	{
		return blockID.getMaterial(blockID.getDefaultState()) != Material.WATER || blockID.getMaterial(blockID.getDefaultState()) != Material.PORTAL;
	}
	
	public static boolean shouldRemoveBlock(Block blockID, int stage)
	{
		if (blockID == Blocks.SNOW || blockID == Blocks.SNOW_LAYER || blockID == Blocks.DIRT || blockID == Blocks.SAND || blockID == Blocks.STONE || blockID == Blocks.STONEBRICK || blockID == Blocks.STONE_BRICK_STAIRS || blockID == Blocks.BRICK_STAIRS || blockID == Blocks.BRICK_BLOCK)
			return false;
		
		switch(stage - 4)
		{
			case 0: return !(blockID == Blocks.FARMLAND || blockID == Blocks.GRASS || blockID == Blocks.COBBLESTONE || blockID == Blocks.COBBLESTONE_WALL || blockID == Blocks.MOSSY_COBBLESTONE || blockID == Blocks.LOG || blockID == Blocks.LOG2);
			case 1: return !(blockID == Blocks.FARMLAND || blockID == Blocks.GRASS || blockID == Blocks.COBBLESTONE || blockID == Blocks.COBBLESTONE_WALL || blockID == Blocks.MOSSY_COBBLESTONE || blockID == Blocks.LOG || blockID == Blocks.LOG2);
			case 2: return blockID == Blocks.FARMLAND || !(blockID == Blocks.GRASS || blockID == Blocks.COBBLESTONE || blockID == Blocks.COBBLESTONE_WALL || blockID == Blocks.MOSSY_COBBLESTONE || blockID == Blocks.LOG || blockID == Blocks.LOG2);
			case 3: return blockID == Blocks.FARMLAND || blockID == Blocks.LOG || blockID == Blocks.LOG2 || !(blockID == Blocks.GRASS || blockID == Blocks.COBBLESTONE || blockID == Blocks.COBBLESTONE_WALL || blockID == Blocks.MOSSY_COBBLESTONE);
			case 4: return true; 
			case 5: return true; 
			default: return !(blockID == Blocks.FARMLAND || blockID == Blocks.GRASS || blockID == Blocks.COBBLESTONE || blockID == Blocks.COBBLESTONE_WALL || blockID == Blocks.MOSSY_COBBLESTONE || blockID == Blocks.LOG || blockID == Blocks.LOG2); 
		}
	}*/
	
	/*public static boolean isOceanBlock(Block blockID)
	{
		return false;
	}
	
	public static boolean isSolidBlock(Block id)
	{
		return (id == Blocks.STONE ||
				id == Blocks.COBBLESTONE ||
				id == Blocks.SANDSTONE);	
	}*/
	
	public static boolean isAprilFoolsDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		return calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1;
	}
	
	public static float toFahrenheit(float temperature)
	{
		return temperature * 80.0F;
	}
	
	public static float toCelsius(float temperature)
	{
		return ((temperature * 80.0F) - 32.0F) * 5.0F / 9.0F;
	}
	
	public static float getTemperature(World world, BlockPos pos)
	{
		world.profiler.startSection("getTemperature");
		float temp = CoroUtilCompatibility.getAdjustedTemperature(world, world.getBiome(pos), pos);
		float time_bonus = 0.0F;
		float[] time_table = {0.1F, 0.0F, 0.1F, 0.2F};
		long time = world.getWorldTime() % 24000L;
		
		time_bonus = (time_table[(int)time / 6000]);
		world.profiler.endSection();
		return temp - time_bonus;
		
	}
	
	public static float getHumidity(World world, BlockPos pos)
	{
		world.profiler.startSection("getHumidity");
		Biome biome = world.getBiome(pos);
		float temp = CoroUtilCompatibility.getAdjustedTemperature(world, biome, pos) - 0.3F;
		float bonus = 0.0F;
		float time_bonus = 0.0F;
		float[] time_table = {0.1F, 0.0F, 0.1F, 0.2F};
		long time = world.getWorldTime() % 24000L;
		
		time_bonus = (time_table[(int)time / 6000]);
		
		BlockPos top_block = new BlockPos(pos.getX(), 255, pos.getZ());
		boolean start = false;
		
		for(IBlockState block = world.getBlockState(top_block); top_block.getY() >= 0; block = world.getBlockState(top_block))
		{
			if ((block.getMaterial().isSolid() || block.getMaterial().isLiquid()) && !block.getBlock().isFoliage(world, top_block))
				start = true;
			
			if (start)
			{
				if (!block.getMaterial().isLiquid())
					break;
				bonus += 0.025F;
			}
			
			top_block = top_block.down();
		}
		
		if (biome.biomeName.contains("Ocean"))
			if (biome.biomeName.contains("Deep"))
				bonus *= 140.5F;
			else
				bonus *= 80.0F;
		else if (biome.biomeName.contains("River"))
			bonus *= 20.0F;
		world.profiler.endSection();
		return Math.max(((temp / 0.5F) + bonus) - time_bonus, 0.0F); 
	}
}
