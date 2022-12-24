package net.mrbt0907.weather2.weather.storm;

import java.util.*;

import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.util.UtilMining;
import com.mojang.authlib.GameProfile;
import com.mrcrayfish.vehicle.entity.EntityVehicle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.util.ChunkUtils;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.storm.StormObject.StormType;
import CoroUtil.util.CoroUtilBlock;

@SuppressWarnings("deprecation")
public class TornadoHelper
{
	private static final IBlockState AIR = Blocks.AIR.getDefaultState();
	public static GameProfile fakePlayerProfile = null;
	//static because its a shared list for the whole dimension
	public static HashMap<Integer, Long> flyingBlock_LastQueryTime = new HashMap<Integer, Long>();
	public static HashMap<Integer, Integer> flyingBlock_LastCount = new HashMap<Integer, Integer>();
	public StormObject storm;
	private Random random;
	private int grabbed, replaced;
	
	//for caching query on if a block damage preventer block is nearby, also assume blocked at first for safety
	private boolean isBlockGrabbingBlockedCached = true;
	private long isBlockGrabbingBlockedCached_LastCheck = 0;
	
	public TornadoHelper(StormObject storm) {this.storm = storm; random = new Random();}
	
	public float getTornadoBaseSize()
	{
		if (storm.stormStage > Stage.TORNADO.getStage() || storm.stormType == StormType.WATER.ordinal())
			return (float) Math.min(storm.funnel_size * 1.15F, ConfigStorm.max_storm_damage_size);
		else
			return 14;
	}
	
	public void tick(World world)
	{
		world.profiler.startSection("tornadoHelperTick");
		float size = getTornadoBaseSize();
		forceRotate(world, size * 0.65F);
		int maxGrabs = ConfigGrab.max_grabbed_blocks_per_tick < 0 ? Integer.MAX_VALUE : ConfigGrab.max_grabbed_blocks_per_tick;
		int maxReplaces = ConfigGrab.max_replaced_blocks_per_tick < 0 ? Integer.MAX_VALUE : ConfigGrab.max_replaced_blocks_per_tick;
		int firesPerTickMax = 1;
		int loopAmount;
		double loopSize;
		boolean shouldGrab = true, shouldReplace = true, shouldContinue = true;
		BlockPos pos;
		IBlockState state;
		
		if (storm == null)
		{
			world.profiler.endSection();
			return;
		}
		
		if (!world.isRemote)
		{
			world.profiler.startSection("processQueue");
			if (ConfigGrab.grab_blocks && world.getTotalWorldTime() % (ConfigGrab.grab_process_delay > 0 ? ConfigGrab.grab_process_delay : 1) == 0)
			{
				grabbed = 0; replaced = 0;
				
					for (int y = 0; shouldContinue && y < storm.pos.posY && y < 256; y++)
					{
						loopAmount = (int) (5.0F + (y * 0.25F) + storm.funnel_size);
						loopSize = size * 0.25D + y * 0.25D;
						
						for (int i = 0; shouldContinue && i < loopAmount; i++)
						{
							int x = (int)(storm.pos_funnel_base.posX + Maths.random(-loopSize, loopSize));
							int z = (int)(storm.pos_funnel_base.posZ + Maths.random(-loopSize, loopSize));
							
							pos = new BlockPos(x, y, z);
							state = ChunkUtils.getBlockState(world, x, y, z);
								
							shouldGrab = grabbed < maxGrabs;
							shouldReplace = replaced < maxReplaces;
								
							if (shouldGrab && grabBlock(world, pos, state))
							{
								grabbed++;
								shouldReplace = false;
							}
								
							if (shouldReplace)
								replaced += replaceBlock(world, pos, state) ? 1 : 0;
								
							shouldContinue = shouldGrab || shouldReplace;
						}
				}
			}
			//Firenado
			if (storm.isFirenado)
			{
				if (storm.stormStage >= Stage.TORNADO.getStage() + 1)
				for (int i = 0; i < firesPerTickMax; i++) {
					BlockPos posUp = new BlockPos(storm.posGround.posX, storm.posGround.posY + Maths.random(30), storm.posGround.posZ);
					state = ChunkUtils.getBlockState(world, posUp);
					if (CoroUtilBlock.isAir(state.getBlock())) {
						EntityMovingBlock mBlock = new EntityMovingBlock(world, posUp.getX(), posUp.getY(), posUp.getZ(), Blocks.FIRE.getDefaultState(), storm);
						mBlock.metadata = 15;
						double speed = 2D;
						mBlock.motionX += (Maths.random(1.0D) - Maths.random(1.0D)) * speed;
						mBlock.motionZ += (Maths.random(1.0D) - Maths.random(1.0D)) * speed;
						mBlock.motionY = 1D;
						mBlock.mode = 0;
						world.spawnEntity(mBlock);
					}
				}


				int randSize = 10;

				int tryX = (int)storm.pos.posX + Maths.random(randSize) - randSize/2;

				int tryZ = (int)storm.pos.posZ + Maths.random(randSize) - randSize/2;
				int tryY = world.getHeight(tryX, tryZ) - 1;

				double d0 = storm.pos.posX - tryX;
				double d2 = storm.pos.posZ - tryZ;
				double dist = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);

				if (dist < size/2 + randSize/2 && grabbed < 300) {
					pos = new BlockPos(tryX, tryY, tryZ);
					Block block = ChunkUtils.getBlockState(world, pos).getBlock();
					BlockPos posUp = new BlockPos(tryX, tryY+1, tryZ);
					Block blockUp = ChunkUtils.getBlockState(world, posUp).getBlock();

					if (!CoroUtilBlock.isAir(block) && CoroUtilBlock.isAir(blockUp))
					{
						ChunkUtils.setBlockState(world, posUp, Blocks.FIRE.getDefaultState());
					}
				}
			}
			
			getBlockCountForDim(storm.manager.getWorld());
			world.profiler.endSection();
		}

		world.profiler.endSection();
	}
	
	public boolean grabBlock(World world, BlockPos pos, IBlockState state)
	{
		if (ConfigGrab.enable_grab_list && WeatherUtilBlock.canGrabBlock(storm, pos, state))
		{
			String id = state.getBlock().getRegistryName().toString();
			boolean shouldGrab = WeatherAPI.getGrabList().exists(id) || !ConfigGrab.enable_grab_list && !ConfigGrab.enable_replace_list;
			
			if (shouldGrab)
			{
				if (ConfigGrab.enable_repair_block_mode)
					return placeDamageBlock(world, pos, state);
				else
				{
					if ((ConfigGrab.enable_list_sharing && Maths.chance(50) || !ConfigGrab.enable_list_sharing) && (ConfigGrab.grab_list_strength_match && WeatherUtilBlock.checkResistance(storm, id) || !ConfigGrab.grab_list_strength_match) && flyingBlock_LastCount.containsKey(world.provider.getDimension()) && flyingBlock_LastCount.get(world.provider.getDimension()) < ConfigGrab.Storm_Tornado_maxFlyingEntityBlocks)
					{
						ChunkUtils.setBlockState(world, pos, AIR);
						EntityMovingBlock entity = new EntityMovingBlock(world, pos.getX(), pos.getY(), pos.getZ(), state, storm);
						entity.motionX += (random.nextDouble() - random.nextDouble()) * 1.0D;
						entity.motionZ += (random.nextDouble() - random.nextDouble()) * 1.0D;
						entity.motionY = 1.0D;
						world.spawnEntity(entity);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean replaceBlock(World world, BlockPos pos, IBlockState state)
	{
		if (ConfigGrab.enable_replace_list && WeatherUtilBlock.canGrabBlock(storm, pos, state))
		{
			String id = state.getBlock().getRegistryName().toString();
			Object[] list = WeatherAPI.getReplaceList().getValues(id);
			boolean shouldReplace = list != null && list.length > 0;
			
			if (shouldReplace)
			{
				if ((ConfigGrab.replace_list_strength_matches && WeatherUtilBlock.checkResistance(storm, id) || !ConfigGrab.grab_list_strength_match))
				{
					ChunkUtils.setBlockState(world, pos, Block.getBlockFromName((String) list[Maths.random(0, list.length)]).getDefaultState());
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean placeDamageBlock(World world, BlockPos pos, IBlockState state)
	{
		if (state != AIR && UtilMining.canConvertToRepairingBlock(world, state))
		{
			TileEntityRepairingBlock.replaceBlockAndBackup(world, pos, ConfigGrab.Storm_Tornado_TicksToRepairBlock);
			return true;
		}
		else
		{
			Weather2.warn("Unable to use repairing block on" + state.getBlock().getLocalizedName());
			return false;
		}
	}
	
	public boolean canGrabEntity(Entity ent)
	{
		if (ent instanceof EntityPlayer)
			return ConfigGrab.grab_players;
		if (ent instanceof INpc)
			return ConfigGrab.grab_villagers;
		if (ent instanceof EntityItem)
			return ConfigGrab.grab_items;
		if (ent instanceof IMob)
			return ConfigGrab.grab_mobs;
		if (ent instanceof EntityAnimal)
			return ConfigGrab.grab_animals;
		if (Loader.isModLoaded("vehicle") && ent instanceof EntityVehicle)
			return true;
				
		//for moving blocks, other non livings
		return true;
	}

	
	public boolean forceRotate(World parWorld, float size)
	{
		AxisAlignedBB aabb = new AxisAlignedBB(storm.pos.posX, storm.currentTopYBlock, storm.pos.posZ, storm.pos.posX, storm.currentTopYBlock, storm.pos.posZ);
		aabb = aabb.grow(size, this.storm.maxHeight * 3, size);
		List<Entity> list = parWorld.getEntitiesWithinAABB(Entity.class, aabb);
		boolean foundEnt = false;

		if (list != null)
		{
			for (int i = 0; i < list.size(); i++)
			{
				Entity entity1 = (Entity)list.get(i);
				if (canGrabEntity(entity1))
				{
					if (getDistanceXZ(storm.pos_funnel_base, entity1.posX, entity1.posY, entity1.posZ) < size)
					{
						if ((entity1 instanceof EntityMovingBlock && !((EntityMovingBlock)entity1).collideFalling) || WeatherUtilEntity.isEntityOutside(entity1, !(entity1 instanceof EntityPlayer)))
						{
								//trying only server side to fix warp back issue (which might mean client and server are mismatching for some rules)
								storm.spinEntity(entity1);
								foundEnt = true;
						}
					}
				}
			}
		}

		return foundEnt;
	}
	
	public double getDistanceXZ(Vec3 parVec, double var1, double var3, double var5)
	{
		double var7 = parVec.posX - var1;
		//double var9 = ent.posY - var3;
		double var11 = parVec.posZ - var5;
		return (double)MathHelper.sqrt(var7 * var7 + var11 * var11);
	}
	
	public double getDistanceXZ(Entity ent, double var1, double var3, double var5)
	{
		double var7 = ent.posX - var1;
		//double var9 = ent.posY - var3;
		double var11 = ent.posZ - var5;
		return (double)MathHelper.sqrt(var7 * var7/* + var9 * var9*/ + var11 * var11);
	}
	
	/**
	 * Will abort out of counting if it hits the min amount required as per config
	 *
	 * @param world
	 * @return
	 */
	public static int getBlockCountForDim(World world)
	{
		int queryRate = 20;
		boolean perform = false;
		int flyingBlockCount = 0;
		int dimID = world.provider.getDimension();
		if (!flyingBlock_LastCount.containsKey(dimID) || !flyingBlock_LastQueryTime.containsKey(dimID))
			perform = true;
		else if (flyingBlock_LastQueryTime.get(dimID) + queryRate < world.getTotalWorldTime())
			perform = true;

		if (perform)
		{
			List<Entity> entities = world.loadedEntityList;
			for (int i = 0; i < entities.size(); i++)
			{
				Entity ent = entities.get(i);
				if (ent instanceof EntityMovingBlock)
				{
					flyingBlockCount++;

					if (flyingBlockCount > ConfigGrab.Storm_Tornado_maxFlyingEntityBlocks) {
						break;
					}
					//save time if we already hit the max
				}
			}

			flyingBlock_LastQueryTime.put(dimID, world.getTotalWorldTime());
			flyingBlock_LastCount.put(dimID, flyingBlockCount);
		}

		return flyingBlock_LastCount.get(dimID);
	}

	public boolean isBlockGrabbingBlocked(World world, IBlockState state, BlockPos pos) {
		int queryRate = 40;
		if (isBlockGrabbingBlockedCached_LastCheck + queryRate < world.getTotalWorldTime()) {
			isBlockGrabbingBlockedCached_LastCheck = world.getTotalWorldTime();

			isBlockGrabbingBlockedCached = false;

			for (Long hash : storm.manager.getListWeatherBlockDamageDeflector()) {
				BlockPos posDeflect = BlockPos.fromLong(hash);

				if (pos.distanceSq(posDeflect) < ConfigStorm.storm_deflector_range * ConfigStorm.storm_deflector_range) {
					isBlockGrabbingBlockedCached = true;
					break;
				}
			}
		}
		return isBlockGrabbingBlockedCached;
	}

	public void cleanup()
	{
		storm = null;
	}
}
