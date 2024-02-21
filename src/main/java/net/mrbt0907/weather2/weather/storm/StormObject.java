package net.mrbt0907.weather2.weather.storm;

import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import CoroUtil.util.ChunkCoordinatesBlock;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilCompatibility;
import CoroUtil.util.CoroUtilEntOrParticle;
import CoroUtil.util.CoroUtilEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.api.weather.AbstractWeatherRenderer;
import net.mrbt0907.weather2.api.weather.IWeatherLayered;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.client.weather.StormNames;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigSnow;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.entity.EntityIceBall;
import net.mrbt0907.weather2.entity.EntityLightningBolt;
import net.mrbt0907.weather2.network.packets.PacketLightning;
import net.mrbt0907.weather2.util.*;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WindManager;

public class StormObject extends WeatherObject implements IWeatherRain, IWeatherLayered
{
	//-----Rendering-----\\
	public AbstractWeatherRenderer particleRenderer;
	public ResourceLocation particleRendererId;
	public float angle = 0.0F;
	
	//-----Storm-----\\
	/***/
	public int layer = 0;
	/**If true, the direction of the storm should be overridden by overrideNewAngle*/
	public boolean overrideAngle = false;
	/**If true, the movement speed of the storm should be overridden by overrideNewMotion*/
	public boolean overrideMotion = false;
	/**If true, the storm was spawned naturally on its own*/
	public boolean isNatural = true;
	/**If true, the storm will guarantee to progress to the highest stage possible*/
	public boolean alwaysProgresses = false;
	/**If true, the storm will never decay after it hits the max lifetime*/
	public boolean neverDissipate = false;
	/**If true, the storm has a higher potential to be stronger than a normal storm*/
	public boolean isViolent = false;
	/**If true, the storm is a tornado or a hurricane*/
	public boolean canProgress;
  	/**If true, the storm will produce a firenado*/
	public boolean isFirenado = false;
	/**If true, the storm will produce a water spout in the water below*/
	public boolean isSpout = false;
	/**If true, the storm will convert to a hurricane when over water*/
	public boolean shouldConvert = true;
	/**Determines how much water is built up in the storm
	 * <br> - 100 to 199 = Drizzle
	 * <br> - 100 to 299 = Rain
	 * <br> - 300 and beyond = Heavy Rain*/
	public float rain = 0;
	/***/
	public float rainRate = 0.0F;
	/***/
	public float hail = 0.0F;
	/***/
	public float hailRate = 0.0F;
	/***/
	public boolean shouldBuildHumidity = false;
	/**The ambient wind speed that the storm creates.*/
	public float windSpeed = 0;
	/**The temperature of the storm. Used to determine whether it snows or rains*/
	public float temperature = 0;
	/**Determines how strong this storm will be*/
	public int stageMax = Stage.NORMAL.getStage(); //calculated from colliding warm and cold fronts, used to determine how crazy a storm _will_ get
	/**Determines where the storm intensified at. Creates a hurricane with WATER and creates a tornado with LAND*/
	public int stormType = StormType.LAND.ordinal();
	/**Determines how strong a storm currently is*/
	public int stage = Stage.NORMAL.getStage();
	/**Determines how much progression a storm reached in its current stage*/
	public float intensity = 0.0F;
	/**Determines how fast storms will intensify. 0.03 by default*/
	public float intensityRate = 0.03F;
	/***/
	public int revives = 0;
	/***/
	public int maxRevives = 0;
	/***/
	public float lightning = 0.5F;
	/**Determines how much the storm needs to reach before it can increase stages. Unused in new progression system*/
	public float intensityMax = 0;
	/**Determines the size of a storm; not the tornado size*/
	public float funnelSize = 0;
	/**Determines how fast a storm grows in size*/
	public float sizeRate = -1.0F;
	//spin speed for potential tornado formations, should go up with intensity increase;
	/**Determines how fast a storm is spinning. Visual only in old progression system*/
	public double spin = 0.02D;
	/**Determines whether the storm has touched the ground*/
	public float formingStrength = 0; //for transition from 0 (in clouds) to 1 (touch down)
	/**How strong the winds are in a tornado. Determines how strong and fast a storm can pick up entities*/
	public float strength = 100;
	/**Unknown*/
	public int maxHeight = 60;
	/***/
	public String name = "";
	
	//Enums
	/**Enumerations for detecting what kind of storm it will become
	 * @value LAND = Tornado
	 * @value WATER = Hurricane*/
	public enum StormType {LAND, WATER;}

	/**Where the top block under the storm is at*/
	public int currentTopYBlock = -1;
	
	public TornadoHelper tornadoHelper = new TornadoHelper(this);
	
	public int updateLCG = (new Random()).nextInt();
	public Vec3 pos_funnel_base = new Vec3(pos.posX, pos.posY, pos.posZ);
	
	public StormObject(FrontObject front)
	{
		super(front);
		
		pos = new Vec3(0, getLayerHeight(), 0);
		size = Maths.random(100,200) + size;
	}
	
	public void init()
	{
		super.init();
		
		if (isNatural)
			temperature = 0.0F;
		windSpeed = 0.0F;
	}
	
	public boolean isStorm() {return canProgress;}
	public boolean isSevere() {return stage > Stage.THUNDER.getStage();}
	public boolean isDeadly() {return stormType == StormType.LAND.ordinal() ? stage > Stage.SEVERE.getStage() : stage > Stage.TROPICAL_DISTURBANCE.getStage();}
	public boolean isTornado() {return stormType == StormType.LAND.ordinal();}
	public boolean isCyclone() {return stormType == StormType.WATER.ordinal();}

	@Override
	public void readFromNBT()
	{
		super.readFromNBT();
		stormType = nbt.getInteger("stormType");
		stage = nbt.getInteger("levelCurIntensityStage");
		isSpout = nbt.getBoolean("attrib_waterSpout");
		currentTopYBlock = nbt.getInteger("currentTopYBlock");
		temperature = nbt.getFloat("levelTemperature");
		rain = nbt.getInteger("levelWater");
		hail = nbt.getInteger("hail");
		hailRate = nbt.getInteger("hailRate");
		layer = nbt.getInteger("layer");
		stageMax = nbt.getInteger("levelStormIntensityMax");
		intensity = nbt.getFloat("levelCurStagesIntensity");
		intensityRate = nbt.getFloat("intensityRate");
		funnelSize = nbt.getFloat("levelCurStageSize");
		windSpeed = nbt.getFloat("levelCurStageWind");
		sizeRate = nbt.getFloat("levelCurStageSizeRate");
		lightning = nbt.getFloat("lightning");
		isViolent = nbt.getBoolean("isViolent");
		isFirenado = nbt.getBoolean("isFirenado");
		name = nbt.getString("stormName");
		shouldConvert = nbt.getBoolean("shouldConvert");
		shouldBuildHumidity = nbt.getBoolean("shouldBuildHumidity");
		canProgress = nbt.getBoolean("canProgress");
		neverDissipate = nbt.getBoolean("neverDissipate");
		alwaysProgresses = nbt.getBoolean("alwaysProgresses");
		overrideAngle = nbt.getBoolean("overrideAngle");
		overrideMotion = nbt.getBoolean("overrideMotion");
		maxRevives = nbt.getInteger("maxRevives");
	}

	@Override
	public CachedNBTTagCompound writeToNBT()
	{
		super.writeToNBT();
		nbt.setBoolean("attrib_waterSpout", isSpout);
		nbt.setInteger("currentTopYBlock", currentTopYBlock);
		nbt.setFloat("levelTemperature", temperature);
		nbt.setFloat("levelWater", rain);
		nbt.setFloat("hail", hail);
		nbt.setFloat("hailRate", hailRate);
		nbt.setInteger("layer", layer);
		nbt.setInteger("levelCurIntensityStage", stage);
		nbt.setFloat("levelCurStagesIntensity", intensity);
		nbt.setFloat("intensityRate", intensityRate);
		nbt.setFloat("levelStormIntensityMax", stageMax);
		nbt.setFloat("levelCurStageSize", funnelSize);
		nbt.setFloat("levelCurStageWind", windSpeed);
		nbt.setFloat("levelCurStageSizeRate", sizeRate);
		nbt.setInteger("stormType", stormType);
		nbt.setString("stormName", name);
		nbt.setFloat("lightning", lightning);
		nbt.setBoolean("isViolent", isViolent);
		nbt.setBoolean("shouldConvert", shouldConvert);
		nbt.setBoolean("shouldBuildHumidity", shouldBuildHumidity);
		nbt.setBoolean("isFirenado", isFirenado);
		nbt.setBoolean("canProgress", canProgress);
		nbt.setBoolean("neverDissipate", neverDissipate);
		nbt.setBoolean("alwaysProgresses", alwaysProgresses);
		nbt.setBoolean("overrideAngle", overrideAngle);
		nbt.setBoolean("overrideMotion", overrideMotion);
		nbt.setInteger("maxRevives", maxRevives);
		return nbt;
	}
	
	@SideOnly(Side.CLIENT)
	public void tickRender(float partialTick)
	{
		super.tickRender(partialTick);
	}
	
	public void tick()
	{
		super.tick();
		//adjust posGround to be pos with the ground Y pos for convinient usage
		posGround = new Vec3(pos.posX, pos.posY, pos.posZ);
		posGround.posY = currentTopYBlock;
		if (manager.getWorld().isRemote)
		{
			if (!WeatherUtil.isPaused())
			{
				tickClient();
				
				if (isDeadly())
					tornadoHelper.tick(manager.getWorld());

				tickMovementClient();
			}
		}
		else
		{
			
			if (isDeadly())
				tornadoHelper.tick(manager.getWorld());

			tickMovement();
			tickWeatherEvents();
			tickProgressionNormal();
			tickSnowFall();
		}
		
		if (layer == 0)
		{
			//sync X Y Z, Y gets changed below
			pos_funnel_base = new Vec3(pos.posX, pos.posY, pos.posZ);
	
			if (stage >= Stage.TORNADO.getStage()) 
			{
				if (stage > Stage.TORNADO.getStage())
				{
					formingStrength = 1;
					pos_funnel_base.posY = posGround.posY;
				}
				else
				{
					//make it so storms touchdown at 0.5F intensity instead of 1 then instantly start going back up, keeps them down for a full 1F worth of intensity val
					float intensityAdj = Math.min(1F, intensity - Stage.SEVERE.getStage());
					//shouldnt this just be intensityAdj?
					float val = (stage + intensityAdj) - Stage.TORNADO.getStage();
					formingStrength = val;
					double yDiff = pos.posY - posGround.posY;
					pos_funnel_base.posY = pos.posY - (yDiff * formingStrength);
				}
			}
			else
				if (stage == Stage.SEVERE.getStage())
				{
					formingStrength = 0;
					pos_funnel_base.posY = posGround.posY;
				}
				else
				{
					formingStrength = 0;
					pos_funnel_base.posY = pos.posY;
				}
		}
	}
	
	public void tickMovement()
	{
		if (front.equals(manager.getGlobalFront()))
		{
			if (!overrideAngle)
			{
				//despite overridden angle, still avoid obstacles
				//slight randomness to angle
				Random rand = new Random();
				angle += (rand.nextFloat() - rand.nextFloat()) * 0.15F;
		
				//avoid large obstacles
				double scanDist = 50;
				double scanX = this.pos.posX + (-Maths.fastSin(Math.toRadians(angle)) * scanDist);
				double scanZ = this.pos.posZ + (Maths.fastCos(Math.toRadians(angle)) * scanDist);
				int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), new BlockPos(scanX, 0, scanZ)).getY();
		
				if (this.pos.posY < height)
				{
					float angleAdj = 45;
					angle += angleAdj;
				}
			}
			
			if (!overrideMotion)
			{
				float finalSpeed;
				double vecX = -Maths.fastSin(Math.toRadians(angle));
				double vecZ = Maths.fastCos(Math.toRadians(angle));
				float cloudSpeedAmp = 0.2F;
				
				if (stage > Stage.SEVERE.getStage() + 1)
					finalSpeed = 0.2F;
				else if (stage > Stage.NORMAL.getStage())
					finalSpeed = 0.05F;
				else
					finalSpeed = getSpeed() * cloudSpeedAmp;
				
				if (stage > Stage.SEVERE.getStage() + 1)
					finalSpeed /= ((float)(stage-Stage.TORNADO.getStage()+1F));
				
				
				
				motion.posX = vecX * finalSpeed;
				motion.posZ = vecZ * finalSpeed;
			}
			
			pos.posX += motion.posX;
			pos.posZ += motion.posZ;
		}
		else
		{
			if (!overrideMotion)
				motion = front.motion;
			
			pos.posX += motion.posX;
			pos.posZ += motion.posZ;
		}
	}

	public void tickMovementClient()
	{
		pos.posX += motion.posX;
		pos.posZ += motion.posZ;
	}
	
	public void tickWeatherEvents()
	{
		World world = manager.getWorld();
		EntityPlayer player;
		int amount = (int)Maths.clamp(ConfigStorm.hail_stones_per_tick * hail * 0.0001F, 1.0F, ConfigStorm.hail_stones_per_tick);
		currentTopYBlock = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(MathHelper.floor(pos.posX), 0, MathHelper.floor(pos.posZ))).getY();
		
		for (int i = 0; i < world.playerEntities.size(); i++)
		{
			player = world.playerEntities.get(Maths.random(0, world.playerEntities.size() - 1));
			if (pos.distanceSq(player.posX, pos.posY, player.posZ) < size)
			{
				if (isHailing())
				{
					for (int ii = 0 ; ii < amount; ii++)
					{
						int x = (int) (player.posX + Maths.random(-128, 128));
						int z = (int) (player.posZ + Maths.random(-128, 128));
							
						if (world.isBlockLoaded(new BlockPos(x, getLayerHeight(), z)))
						{
							EntityIceBall hail = new EntityIceBall(world);
							hail.setPosition(x, getLayerHeight(), z);
							world.spawnEntity(hail);
						}
					}
				}
				
				if (stage > Stage.RAIN.getStage() && Maths.random(0, ConfigStorm.lightning_bolt_1_in_x - (int)(ConfigStorm.lightning_bolt_1_in_x * lightning)) == 0)
				{
					int x = (int) (player.posX + Maths.random(-ConfigStorm.max_lightning_bolt_distance, ConfigStorm.max_lightning_bolt_distance));
					int z = (int) (player.posZ + Maths.random(-ConfigStorm.max_lightning_bolt_distance, ConfigStorm.max_lightning_bolt_distance));
						
					if (world.isBlockLoaded(new BlockPos(x, 0, z)))
						addWeatherEffectLightning(new EntityLightningBolt(world, (double)x, (double)world.getPrecipitationHeight(new BlockPos(x, 0, z)).getY(), (double)z), false);
					else
						PacketLightning.spawnInvisibleLightning(manager.getDimension(), x, getLayerHeight(), z);
				}
			}
		}
		
		trackAndExtinguishEntities();
	}

	public void trackAndExtinguishEntities()
	{
		if (ConfigStorm.storm_rain_extinguish_delay <= 0) return;

		if (isRaining() && manager.getWorld().getTotalWorldTime() % ConfigStorm.storm_rain_extinguish_delay == 0)
		{
			BlockPos posBP = new BlockPos(posGround.posX, posGround.posY, posGround.posZ);
			List<EntityLivingBase> listEnts = manager.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(posBP).grow(size));

			for (EntityLivingBase ent : listEnts)
				if (ent.world.canBlockSeeSky(ent.getPosition()))
					ent.extinguish();
		}
	}
	
	public void tickSnowFall()
	{
		if (!ConfigSnow.Snow_PerformSnowfall || !hasDownfall()) return;
		
		World world = manager.getWorld();
		int xx = 0;
		int zz = 0;
		
		for (xx = (int) (pos.posX - size/2); xx < pos.posX + size/2; xx+=16)
		{
			for (zz = (int) (pos.posZ - size/2); zz < pos.posZ + size/2; zz+=16)
			{
				int chunkX = xx / 16;
				int chunkZ = zz / 16;
				int x = chunkX * 16;
				int z = chunkZ * 16;
				
				//afterthought, for weather 2.3.7
				if (!world.isBlockLoaded(new BlockPos(x, 128, z)))
					continue;
				
				Chunk chunk = world.getChunk(chunkX, chunkZ);
				int i1;
				int xxx;
				int zzz;
				int setBlockHeight;
				
				if (world.provider.canDoRainSnowIce(chunk) && (ConfigSnow.Snow_RarityOfBuildup == 0 || world.rand.nextInt(ConfigSnow.Snow_RarityOfBuildup) == 0))
				{
					updateLCG = updateLCG * 3 + 1013904223;
					i1 = updateLCG >> 2;
					xxx = i1 & 15;
					zzz = i1 >> 8 & 15;
					double d0 = pos.posX - (xx + xxx);
					double d2 = pos.posZ - (zz + zzz);
					if ((double)MathHelper.sqrt(d0 * d0 + d2 * d2) > size)
						continue;
					
					setBlockHeight = world.getPrecipitationHeight(new BlockPos(xxx + x, 0, zzz + z)).getY();
					if (canSnowAtBody(xxx + x, setBlockHeight, zzz + z) && Blocks.SNOW.canPlaceBlockAt(world, new BlockPos(xxx + x, setBlockHeight, zzz + z)))
					{
						boolean betterBuildup = true;
						
						if (betterBuildup)
						{
							WindManager windMan = manager.windManager;
							float angle = windMan.windAngle;
							Vec3 vecPos = new Vec3(xxx + x, setBlockHeight, zzz + z);
							
							if (!world.isBlockLoaded(vecPos.toBlockPos())) 
								continue;

							//make sure vanilla style 1 layer of snow everywhere can also happen
							//but only when we arent in global overcast mode
							//TODO: consider letting this run outside of ConfigSnow.Snow_PerformSnowfall config option
							//since our version canSnowAtBody returns true for existing snow layers, we need to check we have air here for basic 1 layer place
							if (!ConfigMisc.overcast_mode)
								if (world.isAirBlock(vecPos.toBlockPos()))
										world.setBlockState(vecPos.toBlockPos(), Blocks.SNOW_LAYER.getDefaultState());

								//do wind/wall based snowfall
							WeatherUtilBlock.fillAgainstWallSmoothly(world, vecPos, angle, 15, 2, Blocks.SNOW_LAYER);
						}
					}
				}
			}
		}
	}
	
	//questionably efficient code, but really there isnt much better options
	public ChunkCoordinatesBlock getSnowfallEvenOutAdjustCheck(int x, int y, int z, int sourceMeta)
	{
		//filter out diagonals
		ChunkCoordinatesBlock attempt;
		attempt = getSnowfallEvenOutAdjust(x-1, y, z, sourceMeta);
		
		if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
			attempt = getSnowfallEvenOutAdjust(x+1, y, z, sourceMeta);
		if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
			attempt = getSnowfallEvenOutAdjust(x, y, z-1, sourceMeta);
		if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
			attempt = getSnowfallEvenOutAdjust(x, y, z+1, sourceMeta);
		if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
		
		return new ChunkCoordinatesBlock(0, 0, 0, Blocks.AIR, 0);
	}
	
	//return relative values, id 0 (to mark its ok to start snow here) or id snow (to mark check meta), and meta of detected snow if snow (dont increment it, thats handled after this)
	public ChunkCoordinatesBlock getSnowfallEvenOutAdjust(int x, int y, int z, int sourceMeta)
	{
		
		//only check down once, if air, check down one more time, if THAT is air, we dont allow spread out, because we dont want to loop all the way down to bottom of some cliff
		//could use getHeight but then we'd have to difference check the height and that might complicate things...
		
		World world = manager.getWorld();
		Block checkID = world.getBlockState(new BlockPos(x, y, z)).getBlock();
		
		//check for starting with no snow
		if (CoroUtilBlock.isAir(checkID))
		{
			Block checkID2 = world.getBlockState(new BlockPos(x, y-1, z)).getBlock();
			
			//make sure somethings underneath it - we shouldnt need to check deeper because we spread out while meta of snow is halfway, before it can start a second pile
			if (CoroUtilBlock.isAir(checkID2))
				return new ChunkCoordinatesBlock(0, 0, 0, Blocks.AIR, 0); 
			else
				//return that its an open area to start snow at
				return new ChunkCoordinatesBlock(x, y, z, Blocks.AIR, 0);
		}
		else if (checkID == Blocks.SNOW)
		{
			IBlockState state = world.getBlockState(new BlockPos(x, y, z));
			int checkMeta = state.getBlock().getMetaFromState(state);
			
			//if detected snow is shorter, return with detected meta val!
			//adjusting to <=
			if (checkMeta < sourceMeta)
				return new ChunkCoordinatesBlock(x, y, z, checkID, checkMeta);
		}
		else
			return new ChunkCoordinatesBlock(0, 0, 0, Blocks.AIR, 0);
		
		return new ChunkCoordinatesBlock(0, 0, 0, Blocks.AIR, 0);
	}
	
	public boolean canSnowAtBody(int par1, int par2, int par3)
	{
		World world = manager.getWorld();
		Biome biomegenbase = world.getBiome(new BlockPos(par1, 0, par3));
		BlockPos pos = new BlockPos(par1, par2, par3);
		
		if (biomegenbase == null) return false;
		float temperature = WeatherUtil.getTemperature(world, pos);

		if (temperature > 0.15F)
			return false;
		else
		{
			if (par2 >= 0 && par2 < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10)
			{
				IBlockState iblockstate1 = Weather2.getChunkUtil(world).getBlockState(world, pos);
				if ((iblockstate1.getBlock().isAir(iblockstate1, world, pos) || iblockstate1.getBlock() == Blocks.SNOW_LAYER) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos))
					return true;
			}

			return false;
		}
	}
	
	public void tickProgressionNormal()
	{
		World world = manager.getWorld();
			
		if (ticks % ConfigStorm.storm_tick_delay == 0)
		{
			Biome biome = world.getBiome(new BlockPos(MathHelper.floor(pos.posX), 0, MathHelper.floor(pos.posZ)));
			float tempAdjustRate = (float)ConfigStorm.temperature_adjust_rate;
			boolean hasWater, hasOcean = false;
			
			if (stage > Stage.TORNADO.getStage() || stormType == StormType.WATER.ordinal() && stage > Stage.TROPICAL_DEPRESSION.getStage())
				funnelSize = (float) Math.min(Math.pow((intensity - 3.0F) * 14, ConfigStorm.storm_size_curve_mult) * (stormType == StormType.LAND.ordinal() ? sizeRate : sizeRate * 1.5F), ConfigStorm.max_storm_size);
			else if(funnelSize != 14.0F)
				funnelSize = 14.0F;
			
			size = (int) Maths.clamp((funnelSize + ConfigStorm.min_storm_size) * (stormType == StormType.LAND.ordinal() ? 1.5F : 3.0F), ConfigStorm.min_storm_size, ConfigStorm.max_storm_size);
			windSpeed = Math.max(6.73F + (stormType == StormType.WATER.ordinal() ? 1.7F : 2.8F) * (intensity - 3.0F), 0.0F);
			
			//temperature scan
			if (biome != null)
			{
				hasOcean = biome.biomeName.toLowerCase().contains("ocean");
				float biomeTempAdj = getTemperatureMCToWeatherSys(CoroUtilCompatibility.getAdjustedTemperature(manager.getWorld(), biome, new BlockPos(MathHelper.floor(pos.posX), 64, MathHelper.floor(pos.posZ))));
				if (temperature > biomeTempAdj)
					temperature -= tempAdjustRate; 
				else if (temperature < biomeTempAdj)
					temperature += tempAdjustRate;
			}
			IBlockState blockID = world.getBlockState(new BlockPos(MathHelper.floor(pos.posX), currentTopYBlock-1, MathHelper.floor(pos.posZ)));
			hasWater = blockID.getMaterial() instanceof MaterialLiquid;
			
			if (isStorm())
			{
				if (shouldBuildHumidity)
				{
					if (!isDying)
					{
						rain += ConfigStorm.humidity_buildup_rate * WeatherUtil.getHumidity(world, pos.toBlockPos());
						if (hailRate > 0.0F && hailRate < 200.0F)
							hail += hailRate * WeatherUtil.getHumidity(world, pos.toBlockPos()) * 2.5F;
					}
					else
					{
						if (rain > 0.0F)
							rain -= ConfigStorm.humidity_spend_rate * WeatherUtil.getHumidity(world, pos.toBlockPos());
						if (hailRate > 0.0F)
							hail -= hailRate * WeatherUtil.getHumidity(world, pos.toBlockPos()) * 2.0F;
					}
					if (stage < WeatherEnum.Stage.SEVERE.getStage() && hail > 125.0F)
						hail = 125.0F;
					if (rain < 0.0F)
					{
						Weather2.debug("Storm " + getUUID().toString() + " has stopped raining");
						rain = 0.0F;
						shouldBuildHumidity = false;
					}
				}
				
				if (rain < MINIMUM_DRIZZLE && stage > 0)
					rain = MINIMUM_DRIZZLE;
				
				//force storms to die if its no longer raining while overcast mode is active
				if (ConfigMisc.overcast_mode && isNatural && !neverDissipate && !manager.getWorld().isRaining())
				{
					Weather2.debug("Storm " + getUUID().toString() + " was forced to dissipate because of overcast mode at stage " + stage + " and is now dying");
					isDying = true;
				}
				else if (ConfigStorm.disable_tornados && stormType == StormType.LAND.ordinal() || ConfigStorm.disable_cyclones && stormType == StormType.WATER.ordinal())
				{
					Weather2.debug("Storm " + getUUID().toString() + " was forced to dissipate because it was disabled at stage " + stage + " and is now dying");
					isDying = true;
				}
					
				if (stage == Stage.SEVERE.getStage() && hasWater)
				{
					if (ConfigStorm.high_wind_waterspout_10_in_x != 0 && Maths.random(ConfigStorm.high_wind_waterspout_10_in_x) == 0)
						isSpout = true;
				}
				else
					isSpout = false;
				
				float intensityRate = isDeadly() ? this.intensityRate : 0.03F;
				boolean intensify = intensity - (stage - 1) > 1.0F;
				
				//speed up forming and greater progression when past forming state
				if (stage >= Stage.TORNADO.getStage())
					intensityRate *= 3;

				if (!isDying)
				{
					if (neverDissipate && (!intensify && stage <= stageMax || alwaysProgresses) || !neverDissipate)
						intensity += intensityRate;
					
					if (intensify && (stage < stageMax || alwaysProgresses))
					{
						stageNext();
						Weather2.debug("Storm " + getUUID().toString() + " has intensified to stage " + stage);
						
						if (ConfigStorm.storms_aim_at_player && front.isGlobal() && stage == Stage.TORNADO.getStage())
						{
							lightning = Math.max(Maths.random(0.01F, 0.95F), lightning);
							aimStormAtPlayer(null);
						}
						
						if (shouldConvert && !ConfigStorm.disable_cyclones && (stage < WeatherEnum.Stage.SEVERE.getStage() && hasOcean || ConfigStorm.disable_tornados))
						{
							Weather2.debug("Storm " + getUUID().toString() + " was converted into a tropical cyclone");
							lightning = Maths.random(0.01F, 0.30F);
							stormType = StormType.WATER.ordinal();
							updateType();
						}
					}
					
					else if (!(neverDissipate || alwaysProgresses) && stage >= stageMax && intensify)
					{
						Weather2.debug("Storm " + getUUID().toString() + " has peaked at stage " + stage + " and is now dying");
						isDying = true;
					}
				}
				else
				{
					if (ConfigMisc.overcast_mode && manager.getWorld().isRaining())
						intensity -= intensityRate * 0.5F;
					else
						intensity -= intensityRate * 0.2F;
						
					if (intensity - (stage - 1) <= 0)
					{
						stagePrev();
						Weather2.debug("Storm " + getUUID().toString() + " has weakened to stage " + stage);
						if (stage == 2 && revives < maxRevives)
						{
							isDying = false;
							revives++;
							resetStorm();
						}
						else if (stage <= 0)
							setNoStorm();
					}
				}
			}
		}
	}
	
	public WeatherEntityConfig getWeatherEntityConfigForStorm()
	{
		return WeatherTypes.weatherEntTypes.get(Maths.clamp(stage - Stage.TORNADO.getStage(), 0, 6));
	}
	
	public void updateType()
	{
		switch(stage)
		{
			case 0:
				type = WeatherEnum.Type.CLOUD;
				break;
			case 1:
				type = WeatherEnum.Type.RAIN;
				break;
			case 2:
				if (stormType == 1)
					type = WeatherEnum.Type.TROPICAL_DISTURBANCE;
				else
					type = WeatherEnum.Type.THUNDER;
				break;
			case 3:
				if (stormType == 1)
					type = WeatherEnum.Type.TROPICAL_DEPRESSION;
				else
					type = WeatherEnum.Type.SUPERCELL;
				break;
			default:
				if (stormType == 1)
					type = stage == Stage.TROPICAL_STORM.getStage() ? WeatherEnum.Type.TROPICAL_STORM : WeatherEnum.Type.HURRICANE;
				else
					type = WeatherEnum.Type.TORNADO;
		}
		
		if (stormType == 1 && name.length() == 0)
			name = StormNames.get();
	}
	
	public void stageNext() {
		stage += 1;
		updateType();
	}
	
	public void stagePrev() {
		stage -= 1;
		updateType();
	}
	
	public void resetStorm()
	{
		shouldBuildHumidity = true;
		sizeRate = Maths.random(0.75F, 1.35F);
		isViolent = Maths.chance(ConfigStorm.chance_for_violent_storm * 0.01D * 0.25D);
		stageMax = Math.max(rollDiceOnMaxIntensity(), WeatherEnum.Stage.TORNADO.getStage());
		intensityRate = Maths.random(ConfigStorm.storm_lifespan_min <= 0.0D ? 0.003F : (float)ConfigStorm.storm_lifespan_min, ConfigStorm.storm_lifespan_max <= 0.0D ? 0.06F : (float)ConfigStorm.storm_lifespan_max);

		Biome biome = world.getBiome(new BlockPos(MathHelper.floor(pos.posX), 0, MathHelper.floor(pos.posZ)));
		if (shouldConvert)
			stormType = biome != null && biome.biomeName.toLowerCase().contains("ocean") ? StormType.WATER.ordinal() : StormType.LAND.ordinal();
		
		if (isViolent)
		{
			sizeRate += Maths.random(0.25F, 1.65F);
			
			if (stageMax < 9)
				stageMax += 1;
		}
		
		updateType();
		Weather2.debug("Revived Into Deadly Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stageMax + " (EF" + (stageMax - 4) + ")\nSize Multiplier: " + sizeRate * 100 + "%");
	}
	
	public void initRealStorm()
	{
		shouldBuildHumidity = true;
		//new way of storm progression
		if (stage != Stage.RAIN.getStage())
		{
			stage = Stage.RAIN.getStage();
			intensity = 0.0F;
		}

		lightning = Maths.random(0.01F, 0.5F);
		
		if (stageMax < 1)
			stageMax = rollDiceOnMaxIntensity();
		
		if (sizeRate < 0.0F)
			sizeRate = (float) Maths.random(ConfigStorm.min_size_growth, ConfigStorm.max_size_growth);
		
		if (isViolent || Maths.chance(ConfigStorm.chance_for_violent_storm / 100.0D))
		{
			isViolent = true;
			sizeRate += Maths.random(ConfigStorm.min_violent_size_growth, ConfigStorm.max_violent_size_growth);
			if (stageMax < Stage.TORNADO.getStage() + 4)
				stageMax += 1;
		}
		
		while(Maths.chance(ConfigStorm.chance_for_storm_revival * 0.01D) && revives < ConfigStorm.max_storm_revives)
			revives++;
		
		if (Maths.chance(ConfigStorm.chance_for_hail * 0.01D))
			hailRate = (float) Maths.random(ConfigStorm.hail_max_buildup_rate);
		
		if (stageMax > Stage.SEVERE.getStage())
		{
			intensityRate = Maths.random(ConfigStorm.storm_lifespan_min <= 0.0D ? 0.003F : (float)ConfigStorm.storm_lifespan_min, ConfigStorm.storm_lifespan_max <= 0.0D ? 0.06F : (float)ConfigStorm.storm_lifespan_max);
			Weather2.debug("New Deadly Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stageMax + " (EF" + (stageMax - 4) + ")\nSize Multiplier: " + sizeRate * 100 + "%\nLifespan Multiplier: " + intensityRate * 100);
		}
		else
			Weather2.debug("New Normal Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stageMax + "\nSize Multiplier: " + sizeRate * 100 + "%");
		canProgress = true;
		updateType();
	}

	public int rollDiceOnMaxIntensity()
	{
		if (!Maths.chance(ConfigStorm.chance_for_thunderstorm * 0.01D)) return Stage.RAIN.getStage();
		else if (!Maths.chance(ConfigStorm.chance_for_supercell * 0.01D)) return Stage.THUNDER.getStage();
		 
			
		ConfigList list = new ConfigList();
		if (stormType == StormType.LAND.ordinal())
		{
			if (!ConfigStorm.disable_tornados)
				list = WeatherAPI.getTornadoStageList();
		}
		else
		{
			if (!ConfigStorm.disable_cyclones)
				list = WeatherAPI.getHurricaneStageList();
		}
		
		for (Entry<String, Object[]> entry : list.toMap().entrySet())
		{
			String key = entry.getKey();
			
			if (entry.getValue().length > 0)
			{
				double value = entry.getValue()[0] instanceof String && ((String)entry.getValue()[0]).matches("^[\\d\\.]+$") ? Double.parseDouble((String)entry.getValue()[0]) : entry.getValue()[0] instanceof Double ? (double) entry.getValue()[0] : 0.0D; 
				boolean chance = Maths.chance(value * 0.01D);
				if (key.matches("^\\d+$") && chance)
					return Integer.parseInt(key) + 4;
			}
		}
		
		return Stage.SEVERE.getStage();
	}
	
	//FYI rain doesnt count as storm
	public void setNoStorm() {
		Weather2.debug("Storm " + this.getUUID().toString() + " was terminated");
		stage = Stage.NORMAL.getStage();
		intensity = 0;
		isDead = true;
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient()
	{
		double spinSpeedMax = 0.4D;
		spin = Math.min(spinSpeedMax, Math.max(0.007D * stage, 0.03D));
		
		if (stormType == StormType.WATER.ordinal())
			spin += 0.025D;
				
		if (size == 0) size = 1;
		
		ResourceLocation id = WeatherAPI.getParticleRendererId();
		if (particleRendererId != id)
		{
			if (particleRenderer != null)
			{
				particleRenderer.cleanup();
				particleRenderer = null;
			}
			particleRendererId = id;
			particleRenderer = WeatherAPI.getParticleRenderer(this);
		}
			
		if (particleRenderer != null)
			particleRenderer.tick();
	}
	
	@Override
	public float getSpeed()
	{
		return overrideMotion ? (float) motion.speedSq() : manager.windManager.windSpeed;
	}
	
	@Override
	public float getAngle()
	{
		if (overrideAngle) return angle;
		
		float angle = manager.windManager.windAngle;
		
		float angleAdjust = Math.max(10, Math.min(45, 45F * temperature * 0.2F));
		float targetYaw = 0;
		
		//coldfronts go south to 0, warmfronts go north to 180
		if (temperature > 0)
			targetYaw = 180;
		else
			targetYaw = 0;
		
		float bestMove = Maths.wrapDegrees(targetYaw - angle);
		
		if (Math.abs(bestMove) < 180)
		{
			if (bestMove > 0) angle -= angleAdjust;
			if (bestMove < 0) angle += angleAdjust;
		}
		
		return angle;
	}

	public float getAvoidAngleIfTerrainAtOrAheadOfPosition(float angle, Vec3 pos) {
		double scanDistMax = 120;
		for (int scanAngle = -20; scanAngle < 20; scanAngle += 10) {
			for (double scanDistRange = 20; scanDistRange < scanDistMax; scanDistRange += 10) {
				double scanX = pos.posX + (-Maths.fastSin(Math.toRadians(angle + scanAngle)) * scanDistRange);
				double scanZ = pos.posZ + (Maths.fastCos(Math.toRadians(angle + scanAngle)) * scanDistRange);

				int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), new BlockPos(scanX, 0, scanZ)).getY();

				if (pos.posY < height) {
					if (scanAngle <= 0) {
						return 90;
					} else {
						return -90;
					}
				}
			}
		}
		return 0;
	}
	
	public void spinEntity(Object obj)
	{
		float weight = WeatherUtilEntity.getWeight(obj);
		if (weight < 0.0F) return;
		
		WeatherEntityConfig conf = getWeatherEntityConfigForStorm();
		float heightMult = getLayerHeight() * (world.isRemote && obj instanceof Particle ? 0.004F : 0.0034F);
		float rotationMult = heightMult * 0.5F * ((isViolent ? 3.1F : 1.55F) + Math.min((stage - 5.0F) / 3.0F, 2.0F));
		World world = CoroUtilEntOrParticle.getWorld(obj);
		long worldTime = world.getTotalWorldTime();
		
		Entity ent = null;
		if (obj instanceof Entity)
			ent = (Entity) obj;
		
		double radius = 10D, scale = conf.tornadoWidthScale * (world.isRemote && obj instanceof Particle ? 1D : 8.0D);
		double d1 = pos.posX - CoroUtilEntOrParticle.getPosX(obj), d2 = pos.posZ - CoroUtilEntOrParticle.getPosZ(obj);
		
		
		
		if (conf.type == WeatherEntityConfig.TYPE_SPOUT)
		{
			float range = 30F * (float) Maths.fastSin((Math.toRadians(((worldTime * 0.5F)) % 360)));
			float heightPercent = (float) (1F - ((CoroUtilEntOrParticle.getPosY(obj) - posGround.posY) / (pos.posY - posGround.posY)));
			float posOffsetX = (float) Maths.fastSin((Math.toRadians(heightPercent * 360F)));
			float posOffsetZ = (float) -Maths.fastCos((Math.toRadians(heightPercent * 360F)));
			d1 += range*posOffsetX;
			d2 += range*posOffsetZ;
		}
		
		float f = (float)((Maths.fastATan2(d2, d1) * 180D) / Math.PI) - 90F;

		for (; f < -180F; f += 360F);
		for (; f >= 180F; f -= 360F);

		double distY = pos.posY - CoroUtilEntOrParticle.getPosY(obj);
		double distXZ = Math.sqrt(Math.abs(d1)) + Math.sqrt(Math.abs(d2));

		if (CoroUtilEntOrParticle.getPosY(obj) - pos.posY < 0.0D)
			distY = 1.0D;
		else
			distY = CoroUtilEntOrParticle.getPosY(obj) - pos.posY;

		if (distY > maxHeight)
			distY = maxHeight;

		
		double grab = (10D / weight) * ((Math.abs((maxHeight - distY)) / maxHeight));
		float pullY = 0.0F;

		if (distXZ > 5D)
			grab = grab * (radius / distXZ);

		pullY += (float)(conf.tornadoLiftRate / (weight * 0.5F));
		double adjPull = 0.005D / ((weight * ((distXZ + 1D) / radius)));
		double airTime = 0.0D;
		pullY += adjPull;
		
		if (obj instanceof EntityPlayer)
		{
			airTime = WeatherUtilEntity.playerInAirTime + 1.0D;

			if (CoroUtilEntOrParticle.getMotionY(obj) > 0.0D)
				ent.fallDistance = 0F;
		}
		else if (obj instanceof EntityLivingBase)
		{
			airTime = ent.getEntityData().getInteger("timeInAir") + 1.0D;

			if (ent.motionY > 0.0D) ent.fallDistance = 0F;
			//if (ent.motionY > 0.3F) ent.motionY = 0.3F;
			ent.onGround = false;
		}
		
		if (airTime > 0.0D)
			grab = grab - Maths.clamp(10D * (((float)(airTime / 400D))), -50.0D, 50.0D);
		
		grab += conf.relTornadoSize;
		double profileAngle = Math.max(1, (75D + grab - (10D * scale)));
		
		f = (float)((double)f + profileAngle);
		float f3 = (float)Maths.fastCos(-f * 0.01745329F - (float)Math.PI);
		float f4 = (float)Maths.fastSin(-f * 0.01745329F - (float)Math.PI);
		float f5 = conf.tornadoPullRate * 1.5F;

		//if player and not spout
		if (conf.type != 0 && (obj instanceof EntityLivingBase))
			f5 *= ent.onGround ? 2F : 7.0F;
		
		if (obj instanceof EntityLivingBase)
		{
			switch (conf.type)
			{
				case 0:
					f5 *= 0.3F;
					break;
				case 2:
					f5 *= 4.0F;
					break;
			}
		}
		
		float moveX = f3 * f5;
		float moveZ = f4 * f5;
		
		//tornado strength changes
		float str = strength * 1.25f;
		
		if (conf.type == WeatherEntityConfig.TYPE_SPOUT)
			str *= 0.3F;
		
		if (stormType == StormType.WATER.ordinal())
		{
			str *= 0.55F;
			pullY = Math.min(pullY , 0.0275F);
		}
		
		if (world.isRemote && obj instanceof Particle)
			pullY *= str * 0.01F;
		else
			pullY *= str * 0.085F;
		
		//prevent double+ pull on entities
		if (obj instanceof Entity)
		{
			long lastPullTime = ent.getEntityData().getLong("lastPullTime");
			if (lastPullTime == worldTime) pullY = 0;
			ent.getEntityData().setLong("lastPullTime", worldTime);
		}
		
		setVel(obj, -moveX * rotationMult, pullY * heightMult, moveZ * rotationMult);
	}
	
	public void setVel(Object entity, float f, float f1, float f2)
	{
		CoroUtilEntOrParticle.setMotionX(entity, CoroUtilEntOrParticle.getMotionX(entity) + f);
		CoroUtilEntOrParticle.setMotionY(entity, CoroUtilEntOrParticle.getMotionY(entity) + f1);
		CoroUtilEntOrParticle.setMotionZ(entity, CoroUtilEntOrParticle.getMotionZ(entity) + f2);

		if (entity instanceof EntitySquid)
		{
			Entity ent = (Entity) entity;
			ent.setPosition(ent.posX + ent.motionX * 5F, ent.posY, ent.posZ + ent.motionZ * 5F);
		}
	}

	@Override
	public void cleanup() {
		super.cleanup();
		if (tornadoHelper != null)
			tornadoHelper.cleanup();
		
		tornadoHelper = null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void cleanupClient(boolean wipe)
	{	
		if (wipe && particleRenderer != null)
		{
			particleRenderer.cleanup();
			particleRenderer = null;
		}
	}
	
	public void aimStormAtPlayer(EntityPlayer entP)
	{
		if (entP == null)
			entP = manager.getWorld().getClosestPlayer(pos.posX, pos.posY, pos.posZ, -1, false);
		
		if (entP != null)
		{
			float yaw = -(float)(Maths.fastATan2(entP.posX - pos.posX, entP.posZ - pos.posZ) * 180.0D / Math.PI);
			int size = ConfigStorm.storm_aim_accuracy_in_angle;
			if (size > 0)
				yaw += Maths.random(size) - (size / 2);
			
			overrideAngle = true;
			angle = yaw;
			
			Weather2.debug("Storm " + getUUID() + " was aimed at player " + CoroUtilEntity.getName(entP));
		}
	}
	
	public float getTemperatureMCToWeatherSys(float parOrigVal) {
		return parOrigVal - 0.3F;
	}
	
	public void addWeatherEffectLightning(EntityLightningBolt parEnt, boolean custom) {
		manager.getWorld().weatherEffects.add(parEnt);
		PacketLightning.spawnLightning(manager.getDimension(), parEnt, custom);
	}
	
	@Override
	public int getNetRate() {
		if (stage >= Stage.SEVERE.getStage()) {
			return 2;
		} else {
			return super.getNetRate();
		}
	}
	
	//TODO: Force storm to utilize angle value
	public void setAngle(float angle)
	{
		overrideAngle = true;
		this.angle = angle % 360.0F;
	}
	
	//TODO: Force storm to utilize speed value
	public void setSpeed(float speed)
	{
		overrideMotion = true;
		motion.posX = -Maths.fastSin(Math.toRadians(angle)) * speed;
		motion.posZ = Maths.fastCos(Math.toRadians(angle)) * speed;
	}

	public void setStage(int stage)
	{
		this.stage = stage;
		updateType();
	}

	public boolean isDrizzling()
	{
		return rain >= MINIMUM_DRIZZLE && rain < MINIMUM_RAIN;
	}
	
	public boolean isRaining()
	{
		return rain >= MINIMUM_RAIN;
	}

	public boolean hasDownfall()
	{
		return rain >= MINIMUM_DRIZZLE;
	}
	
	@Override
	public int getStage()
	{
		return stage;
	}

	@Override
	public int getLayer()
	{
		return layer;
	}

	@Override
	public int getLayerHeight()
	{
		switch (layer)
		{
			case 1: return ConfigStorm.cloud_layer_1_height;
			case 2: return ConfigStorm.cloud_layer_2_height;
			default: return ConfigStorm.cloud_layer_0_height;
		}
	}

	public boolean isHailing()
	{
		return hail > 100.0F;
	}
	
	@Override
	public float getDownfall()
	{
		return rain;
	}

	@Override
	public float getDownfall(Vec3 pos)
	{
		return rain;
	}

	@Override
	public float getDownfall(BlockPos pos)
	{
		return rain;
	}

	@Override
	public boolean hasDownfall(Vec3 pos)
	{
		return hasDownfall();
	}

	@Override
	public boolean hasDownfall(BlockPos pos)
	{
		return hasDownfall();
	}

	@Override
	public float getWindSpeed()
	{
		return windSpeed;
	}
	
	@Override
	public String getName()
	{
		return getName(false);
	}
	
	public String getName(boolean getEF)
	{
		boolean truth = name.length() == 0, isHailing = isHailing();
		
		switch(type)
		{
			case CLOUD:
				return (truth ? "" : name + " ") + (isHailing ? "Hailing " : "") + "Cloud";
			case RAIN:
				return (truth ? "" : name + " ") + (isHailing ? "Hailing " : "") + (hasDownfall() ? temperature <= 0.0F ? "Snowstorm": "Rainstorm" : "Cloud");
			case THUNDER:
				return (truth ? "" : name + " ") + (isHailing ? "Hailing " : "") + "Thunderstorm";
			case SUPERCELL:
				return (truth ? "" : name + " ") + (isHailing ? "Hailing " : "") + "Supercell";
			case TROPICAL_DISTURBANCE:
				return  (isHailing ? "Hailing " : "") + "Tropical Disturbance" + (truth ? "" : " " + name);
			case TROPICAL_DEPRESSION:
				return (isHailing ? "Hailing " : "") + "Tropical Depression" + (truth ? "" : " " + name);
			case TROPICAL_STORM:
				return (isHailing ? "Hailing " : "") + "Tropical Storm " + name;
			case TORNADO:
				return (truth ? "" : name + " ") + (ConfigStorm.enable_ef_scale || getEF ? "EF" + (stage - Stage.TORNADO.getStage()) : "F" + (int)Maths.clamp(Math.floor(funnelSize * 0.0206611570247933884297520661157F), 0, Integer.MAX_VALUE)) + " " + (isHailing ? "Hailing " : "") + "Tornado";
			case HURRICANE:
				return (isHailing ? "Hailing " : "") + "Hurricane " + name + " - Category " + (stage - Stage.TORNADO.getStage());
			default:
				return (isHailing ? "Hailing " : "") + "Unknown Storm";
		}
	}
	
	@Override
	public String getTypeName()
	{
		boolean truth = name.length() == 0;
		
		switch(type)
		{
			case TORNADO:
				return (truth ? "" : name + " ") + (ConfigStorm.enable_ef_scale ? "EF" + (stage - Stage.TORNADO.getStage()) : "F" + (int)Maths.clamp(Math.floor(funnelSize * 0.0206611570247933884297520661157F), 0, stageMax - Stage.TORNADO.getStage()));
			case HURRICANE:
				return name + " C" + (stage - Stage.TORNADO.getStage());
			case TROPICAL_DISTURBANCE:
				return "TD1";
			case TROPICAL_DEPRESSION:
				return "TD2";
			case TROPICAL_STORM:
				return "TS";
			default:
				return "";
		}
	}
}
