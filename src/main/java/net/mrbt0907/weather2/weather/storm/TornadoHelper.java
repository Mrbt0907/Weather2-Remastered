package net.mrbt0907.weather2.weather.storm;

import java.util.*;

import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.util.UtilMining;
import com.mojang.authlib.GameProfile;
import com.mrcrayfish.vehicle.entity.EntityVehicle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.config.ClientConfigData;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.util.WeatherUtilSound;
import net.mrbt0907.weather2.weather.storm.StormObject.Type;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.Vec3;

@SuppressWarnings("deprecation")
public class TornadoHelper
{
	public static GameProfile fakePlayerProfile = null;
	//static because its a shared list for the whole dimension
	private static HashMap<Integer, Long> flyingBlock_LastQueryTime = new HashMap<Integer, Long>();
	private static HashMap<Integer, Integer> flyingBlock_LastCount = new HashMap<Integer, Integer>();
	//for client player, for use of playing sounds
	private static boolean isOutsideCached = false;
	private StormObject storm;
	private Random random;
	private final List<QueuedBlock> queue = new ArrayList<QueuedBlock>();
	private int grabQueue = 0;
	private int replaceQueue = 0;

	
	//for caching query on if a block damage preventer block is nearby, also assume blocked at first for safety
	private boolean isBlockGrabbingBlockedCached = true;
	private long isBlockGrabbingBlockedCached_LastCheck = 0;
	
	public TornadoHelper(StormObject storm) {this.storm = storm; random = new Random();}
	
	public float getTornadoBaseSize()
	{
		if (storm.stormStage > StormObject.Stage.STAGE0.getInt() || storm.stormType == Type.WATER.getInt())
			return (float) Math.min(storm.funnel_size * 1.15F, ConfigStorm.max_storm_damage_size);
		else
			return 14;
	}
	
	
	public void tick(World world)
	{
		world.profiler.startSection("tornadoHelperTick");
		if (!world.isRemote)
		{
			world.profiler.startSection("processQueue");
			if (world.getTotalWorldTime() % (ConfigGrab.grab_list_process_delay > 1 ? ConfigGrab.grab_list_process_delay : 2) == 0)
			{
				Iterator<QueuedBlock> queue = this.queue.iterator();
				QueuedBlock qb = null;
				IBlockState block = null;
				while (queue.hasNext())
				{
					qb = queue.next();
					if (world.provider != null)
					{
						block = world.getBlockState(qb.pos);
						if (ConfigGrab.enable_repair_block_mode)
							if (block.getBlock() == Blocks.AIR && UtilMining.canConvertToRepairingBlock(world, block))
								TileEntityRepairingBlock.replaceBlockAndBackup(world, qb.pos, ConfigGrab.Storm_Tornado_TicksToRepairBlock);
							else
								Weather2.warn("Unable to use repairing block on" + block.getBlock().getLocalizedName());
						else if (qb.shouldRemove())
						{
							if (flyingBlock_LastCount.containsKey(world.provider.getDimension()) && flyingBlock_LastCount.get(world.provider.getDimension()) < ConfigGrab.Storm_Tornado_maxFlyingEntityBlocks)
							{
								world.setBlockState(qb.pos, Blocks.AIR.getDefaultState(), 3);
								EntityMovingBlock entity = new EntityMovingBlock(world, qb.pos.getX(), qb.pos.getY(), qb.pos.getZ(), block, storm);
								entity.motionX += (random.nextDouble() - random.nextDouble()) * 1.0D;
								entity.motionZ += (random.nextDouble() - random.nextDouble()) * 1.0D;
								entity.motionY = 1.0D;
								world.spawnEntity(entity);
							}
						}
						else
							world.setBlockState(qb.pos, Block.getBlockFromName(qb.replacement).getDefaultState(), 3);
					}
					else
						break;
				}
				this.queue.clear();
				grabQueue = 0;
				replaceQueue = 0;
			}
			world.profiler.endSection();
		}
		
		if (storm == null)
		{
			world.profiler.endSection();
			return;
		}
		
		float size = getTornadoBaseSize();
		forceRotate(world, size * 0.65F);
		int max_grabs = ConfigGrab.max_grabbed_blocks_per_tick + ConfigGrab.max_replaced_blocks_per_tick;
		int grabbed = 0;
		int firesPerTickMax = 1;
		
		if (!world.isRemote && ConfigGrab.grab_blocks)
		{
			world.profiler.startSection("addToQueue");
			for (int y = 0; grabbed < max_grabs && y < storm.pos.yCoord && y < 256; y++)
			{
				int y_size = (int) (y * 0.25F);
				int extraTry = (int) storm.funnel_size;
				int loopAmount = 5 + y_size + extraTry;
				//Scan X and Z Axis
				for (int i = 0; grabbed < max_grabs && i < loopAmount; i++)
				{
					if (Maths.chance(0.2D)) continue;
					int x = (int)(storm.pos_funnel_base.xCoord + Maths.random(0.0f, size + (y_size)) - ((size * 0.5F) + (y_size * 0.5F)));
					int z = (int)(storm.pos_funnel_base.zCoord + Maths.random(0.0f, size + (y_size)) - ((size * 0.5F) + (y_size * 0.5F)));
					double dx = storm.pos.xCoord - x;
					double dz = storm.pos.zCoord - z;
					double distance = Math.sqrt(dx * dx + dz * dz);
					
					if (distance < size * 0.5F + y_size * 0.5F && queueBlock(world, x, y, z))
						grabbed++;
				}
			}
			getBlockCountForDim(storm.manager.getWorld());
			world.profiler.endSection();
		}

		//Firenado
		if (!world.isRemote && storm.isFirenado) {
			if (storm.stormStage >= StormObject.Stage.STAGE1.getInt())
			for (int i = 0; i < firesPerTickMax; i++) {
				BlockPos posUp = new BlockPos(storm.posGround.xCoord, storm.posGround.yCoord + Maths.random(30), storm.posGround.zCoord);
				IBlockState state = world.getBlockState(posUp);
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

			int tryX = (int)storm.pos.xCoord + Maths.random(randSize) - randSize/2;

			int tryZ = (int)storm.pos.zCoord + Maths.random(randSize) - randSize/2;
			int tryY = world.getHeight(tryX, tryZ) - 1;

			double d0 = storm.pos.xCoord - tryX;
			double d2 = storm.pos.zCoord - tryZ;
			double dist = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);

			if (dist < size/2 + randSize/2 && grabbed < 300) {
				BlockPos pos = new BlockPos(tryX, tryY, tryZ);
				Block block = world.getBlockState(pos).getBlock();
				BlockPos posUp = new BlockPos(tryX, tryY+1, tryZ);
				Block blockUp = world.getBlockState(posUp).getBlock();

				if (!CoroUtilBlock.isAir(block) && CoroUtilBlock.isAir(blockUp))
				{
					world.setBlockState(posUp, Blocks.FIRE.getDefaultState());
				}
			}
		}

		world.profiler.endSection();
	}
	
	public boolean isNoDigCoord(int x, int y, int z)
	{
		  return false;
	}

	@SuppressWarnings("unchecked")
	public boolean queueBlock(World world, int x, int y, int z)
	{
		BlockPos pos = WeatherUtilBlock.grabBlockPos(world, x, y ,z);
		boolean[] truth = {false, false};
		if (pos != null && WeatherUtilBlock.canGrabBlock(storm, pos))
		{
			String block = world.getBlockState(pos).getBlock().getRegistryName().toString();
			List<String> list = (List<String>) WeatherAPI.getReplaceList().get(block);
			if (ConfigGrab.enable_replace_list && replaceQueue < ConfigGrab.max_replaced_blocks_per_tick)
			{
				if (list != null && (ConfigGrab.replace_list_strength_matches && WeatherUtilBlock.checkResistance(storm, block) || !ConfigGrab.replace_list_strength_matches))
				{
					if (ConfigGrab.enable_list_sharing)
						truth[0] = true;
					else
					{
						QueuedBlock.add(this, pos, list.get(Maths.random(0,list.size() - 1)));
						return true;
					}
				}
			}
			if (grabQueue < ConfigGrab.max_grabbed_blocks_per_tick)
			{
				if ((ConfigGrab.enable_grab_list && WeatherAPI.getGrabList().exists(block) || !ConfigGrab.enable_grab_list) && (ConfigGrab.grab_list_strength_match && WeatherUtilBlock.checkResistance(storm, block) || !ConfigGrab.grab_list_strength_match))
				{
					if (ConfigGrab.enable_list_sharing)
						truth[1] = true;
					else
					{
						QueuedBlock.add(this, pos, null);
						return true;
					}
				}
			}
			if (!ConfigGrab.enable_grab_list && !ConfigGrab.enable_replace_list)
			{
				if (((ConfigGrab.grab_list_strength_match || ConfigGrab.replace_list_strength_matches) && WeatherUtilBlock.checkResistance(storm, block)) || !ConfigGrab.grab_list_strength_match && !ConfigGrab.replace_list_strength_matches)
				{	
					if (Maths.chance(50) && grabQueue < ConfigGrab.max_grabbed_blocks_per_tick)
					{
						QueuedBlock.add(this, pos, null);
					}
					else if (replaceQueue < ConfigGrab.max_replaced_blocks_per_tick)
					{
						QueuedBlock.add(this, pos, "minecraft:air");
					}
					else
						return false;
				}
			}
			else
				if ((truth[0] && truth[1] && (ConfigGrab.enable_list_sharing && Maths.chance(50) || true)) || truth[0] && !truth[1])
				{
					QueuedBlock.add(this, pos, list.get(Maths.random(0,list.size() - 1)));
					return true;
				}
				else if (truth[1])
				{
					QueuedBlock.add(this, pos, null);
					return true;
				}
				else
					return false;
		}
		return false;
	}
	
	public boolean canGrabEntity(Entity ent) {
		if (ent.world.isRemote)
			return canGrabEntityClient(ent);
		else
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

	@SideOnly(Side.CLIENT)
	public boolean canGrabEntityClient(Entity ent)
	{
		ClientConfigData clientConfig = ClientTickHandler.clientConfigData;
		if (ent instanceof EntityPlayer)
			return clientConfig.Storm_Tornado_grabPlayer;
		if (ent instanceof INpc)
			return clientConfig.Storm_Tornado_grabVillagers;
		if (ent instanceof EntityItem)
			return clientConfig.Storm_Tornado_grabItems;
		if (ent instanceof IMob)
			return clientConfig.Storm_Tornado_grabMobs;
		if (ent instanceof EntityAnimal)
			return clientConfig.Storm_Tornado_grabAnimals;
		if (Loader.isModLoaded("vehicle") && ent instanceof EntityVehicle)
			return true;
		//for moving blocks, other non livings
		return true;
	}
	
	public boolean forceRotate(World parWorld, float size)
	{
		AxisAlignedBB aabb = new AxisAlignedBB(storm.pos.xCoord, storm.currentTopYBlock, storm.pos.zCoord, storm.pos.xCoord, storm.currentTopYBlock, storm.pos.zCoord);
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
		double var7 = parVec.xCoord - var1;
		//double var9 = ent.posY - var3;
		double var11 = parVec.zCoord - var5;
		return (double)MathHelper.sqrt(var7 * var7/* + var9 * var9*/ + var11 * var11);
	}
	
	public double getDistanceXZ(Entity ent, double var1, double var3, double var5)
	{
		double var7 = ent.posX - var1;
		//double var9 = ent.posY - var3;
		double var11 = ent.posZ - var5;
		return (double)MathHelper.sqrt(var7 * var7/* + var9 * var9*/ + var11 * var11);
	}
	
	@SideOnly(Side.CLIENT)
	public void soundUpdates(boolean playFarSound, boolean playNearSound)
	{
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		
		if (mc.player == null)
		{
			return;
		}

		//close sounds
		int far = Math.max((int)(storm.funnel_size * 3.0), 200);
		int close = Math.max((int)(storm.funnel_size * 1.50), 100);
		if (storm.stormType == StormObject.Type.WATER.getInt()) {
			far += 100;
			close += 100;
		}
		Vec3 plPos = new Vec3(mc.player.posX, mc.player.posY, mc.player.posZ);
		
		double distToPlayer = this.storm.posGround.distanceTo(plPos);
		
		float volScaleFar = (float) ((far - distToPlayer/*this.getDistanceToEntity(mc.player)*/) / far);
		float volScaleClose = (float) ((close - distToPlayer/*this.getDistanceToEntity(mc.player)*/) / close);

		if (volScaleFar < 0F)
			volScaleFar = 0.0F;

		if (volScaleClose < 0F)
			volScaleClose = 0.0F;

		if (distToPlayer < close) {
		} else {
		}

		if (distToPlayer < far)
		{
			if (playFarSound) {
				if (mc.world.getTotalWorldTime() % 40 == 0) {
					isOutsideCached = WeatherUtilEntity.isPosOutside(mc.world,
							new Vec3(mc.player.getPosition().getX()+0.5F, mc.player.getPosition().getY()+0.5F, mc.player.getPosition().getZ()+0.5F));
				}
				if (isOutsideCached) {
					tryPlaySound(WeatherUtilSound.snd_wind_far, 2, mc.player, volScaleFar, far);
				}
			}
			if (playNearSound) tryPlaySound(WeatherUtilSound.snd_wind_close, 1, mc.player, volScaleClose, close);

			if (storm.stormStage >= StormObject.Stage.STAGE0.getInt() && storm.stormType == StormObject.Type.LAND.getInt()/*getStorm().type == getStorm().TYPE_TORNADO*/)
				tryPlaySound(WeatherUtilSound.snd_tornado_dmg_close, 0, mc.player, volScaleClose, close);
		}
	}

	public boolean tryPlaySound(String[] sound, int arrIndex, Entity source, float vol, float parCutOffRange)
	{
		Random rand = new Random();
		
		if (WeatherUtilSound.soundTimer[arrIndex] <= System.currentTimeMillis())
		{
			WeatherUtilSound.playMovingSound(storm, new StringBuilder().append("streaming." + sound[WeatherUtilSound.snd_rand[arrIndex]]).toString(), vol, 1.0F, parCutOffRange);
			int length = (Integer)WeatherUtilSound.soundToLength.get(sound[WeatherUtilSound.snd_rand[arrIndex]]);
			//-500L, for blending
			WeatherUtilSound.soundTimer[arrIndex] = System.currentTimeMillis() + length - 500L;
			WeatherUtilSound.snd_rand[arrIndex] = rand.nextInt(3);
		}

		return false;
	}

	/**
	 * Will abort out of counting if it hits the min amount required as per config
	 *
	 * @param world
	 * @return
	 */
	public static int getBlockCountForDim(World world) {
		int queryRate = 20;
		boolean perform = false;
		int flyingBlockCount = 0;
		int dimID = world.provider.getDimension();
		if (!flyingBlock_LastCount.containsKey(dimID) || !flyingBlock_LastQueryTime.containsKey(dimID)) {
			perform = true;
		} else if (flyingBlock_LastQueryTime.get(dimID) + queryRate < world.getTotalWorldTime()) {
			perform = true;
		}

		if (perform) {

			//Weather.dbg("getting moving block count");

			List<Entity> entities = world.loadedEntityList;
			for (int i = 0; i < entities.size(); i++) {
				Entity ent = entities.get(i);
				if (ent instanceof EntityMovingBlock) {
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

	public void cleanup() {
		queue.clear();
		storm = null;
	}
	
	public static class QueuedBlock
	{
		public BlockPos pos;
		public String replacement;
		
		QueuedBlock(BlockPos pos, String replacement)
		{
			this.pos = pos;
			this.replacement = replacement;
		}
		
		public static QueuedBlock add(TornadoHelper tn, BlockPos pos, String replacement)
		{
			QueuedBlock block = new QueuedBlock(pos, replacement);
			tn.queue.add(block);
			
			if (block.replacement == null)
				 tn.grabQueue++;
			else
				tn.replaceQueue++;
			
			return block;
		}
		
		public boolean shouldRemove() {return replacement == null;}
	}
}
