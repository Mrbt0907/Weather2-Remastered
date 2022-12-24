package net.mrbt0907.weather2.weather.storm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.util.ChunkCoordinatesBlock;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilCompatibility;
import CoroUtil.util.CoroUtilEntOrParticle;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
import net.mrbt0907.weather2.api.weather.IWeatherLayered;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.client.SceneEnhancer;
import net.mrbt0907.weather2.client.entity.particle.ExtendedEntityRotFX;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.weather.StormNames;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.config.ConfigSimulation;
import net.mrbt0907.weather2.config.ConfigSnow;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.entity.EntityIceBall;
import net.mrbt0907.weather2.entity.EntityLightningBolt;
import net.mrbt0907.weather2.network.packets.PacketLightning;
import net.mrbt0907.weather2.util.*;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WindManager;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorFog;
import extendedrenderer.particle.entity.EntityRotFX;

public class StormObject extends WeatherObject implements IWeatherRain, IWeatherLayered
{
	//Rendering
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesCloud;
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesGround;
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesRain;
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesFunnel;
	@SideOnly(Side.CLIENT)
	public ParticleBehaviorFog particleBehaviorFog;
	public int sizeMaxFunnelParticles = 600;
	public float angle = 0.0F;
	
	//Basic Info
	public int layer = 0;
	
	//Storm Info
	/**If true, the direction of the storm should be overridden by overrideNewAngle*/
	public boolean overrideAngle = false;
	/**The new value that the storm's direction should be*/
	public float overrideNewAngle = 0;
	/**If true, the movement speed of the storm should be overridden by overrideNewMotion*/
	public boolean overrideMotion = false;
	/**The new value that the storm's movement speed should be. Can go beyond the maximum movement speed*/
	public float overrideNewMotion = 0;
	/**If true, the spin speed of the storm should be overridden by overrideNewSpin*/
	public boolean overrideSpin = false;
	/**The new value that the storm spin speed should be. Can go beyond the maximum spin speed*/
	public float overrideNewSpin = 0;
	/**If true, the storm was spawned naturally on its own*/
	public boolean isNatural = true;
	//to prevent things like it progressing to next stage before weather machine undoes it
	/**If true, the storm was spawned from a weather machine or something else*/
	public boolean isMachineControlled = false;
	/**If true, the storm can snow based on the temperature*/
	public boolean canSnowFromCloudTemperature = false;
	/**If true, the storm will guarantee to progress to the highest stage possible*/
	public boolean alwaysProgresses = false;
	/**If true, the storm will never decay after it hits the max lifetime*/
	public boolean neverDissipate = false;
	/**If true, the storm has a higher potential to be stronger than a normal storm*/
	public boolean isViolent = false;
	/**If true, the storm is a tornado or a hurricane*/
	public boolean canProgress;
	//Populate sky with stormless/cloudless storm objects in order to allow clear skies with current design
	/**Unknown*/
  	public boolean isCloudless = false;
  	/**If true, the storm will produce a firenado*/
	public boolean isFirenado = false;
	/**If true, the storm will produce a water spout in the water below*/
	public boolean isSpout = false;
	/**If true, the storm will convert to a hurricane when over water*/
	public boolean shouldConvert = false;
	//cloud formation data, helps storms
	/**Determines how much water is built up in the storm
	 * <br> - 50 to 100 = Drizzle
	 * <br> - 100 to 150 = Rain
	 * <br> - 150 and beyond = Heavy Rain*/
	public float stormHumidity = 0;
	public float stormHumidityRate = 1.0F;
	/**Determines how much rain can build up before it starts to rain*/
	public float stormRainMin = 50.0F;
	public boolean shouldBuildHumidity = false;
	public boolean isHailing;
	/**Unknown*/
	public float stormWind = 0; //high elevation builds this, plains areas lowers it, 0 = no additional speed ontop of global speed
	/**Unknown*/
	public float stormTemperature = 0; //negative for cold, positive for warm, we subtract 0.7 from vanilla values to make forest = 0, plains 0.1, ocean -0.5, etc
	
	/**Determines which entities get extinguished when under the storm*/
	public List<EntityLivingBase> stormCloseEntities = new ArrayList<>();
	//storm data, used when its determined a storm will happen from cloud front collisions
	/**Determines how strong this storm will be*/
	public int stormStageMax = Stage.NORMAL.getStage(); //calculated from colliding warm and cold fronts, used to determine how crazy a storm _will_ get
	//used to mark difference between land and water based storms
	/**Determines where the storm intensified at. Creates a hurricane with WATER and creates a tornado with LAND*/
	public int stormType = StormType.LAND.ordinal();
	//revision, ints for each stage of intensity, and a float for the intensity of THAT current stage
	/**Determines how strong a storm currently is*/
	public int stormStage = Stage.NORMAL.getStage();
	/**Determines how much progression a storm reached in its current stage*/
	public float stormIntensity = 0;
	public int revives = 0;
	public int maxRevives = 0;
	/**Determines how much the storm needs to reach before it can increase stages. Unused in new progression system*/
	public float maxIntensity = 0;
	/**Determines the size of a storm; not the tornado size*/
	public float funnel_size = 0;
	/**Determines how fast a storm grows in size*/
	public float stormSizeRate = 3.5F;
	//spin speed for potential tornado formations, should go up with intensity increase;
	/**Determines how fast a storm is spinning. Visual only in old progression system*/
	public double stormSpin = 0.02D;
	/**Determines whether the storm has touched the ground*/
	public float stormFormingStrength = 0; //for transition from 0 (in clouds) to 1 (touch down)
	/**How strong the winds are in a tornado. Determines how strong and fast a storm can pick up entities*/
	public float strength = 100;
	/**Unknown*/
	public int maxHeight = 60;
	public boolean canBeDeadly = true;
	public String stormName = "";
	
	//Enums

	/**Enumerations for detecting what kind of storm it will become
	 * @value LAND = Tornado
	 * @value WATER = Hurricane*/
	public enum StormType {LAND, WATER;}

	/**Unknown value*/
	public float scale = 1F;
	/**Where the top block under the storm is at*/
	public int currentTopYBlock = -1;
	
	public TornadoHelper tornadoHelper = new TornadoHelper(this);
	
	//public Set<ChunkCoordIntPair> doneChunks = new HashSet<ChunkCoordIntPair>();
	public int updateLCG = (new Random()).nextInt();
	
	
	
	public Vec3 pos_funnel_base = new Vec3(pos.posX, pos.posY, pos.posZ); //for formation / touchdown progress, where all the ripping methods scan from
	
	//there is an issue with rainstorms sometimes never going away, this is a patch to mend the underlying issue i cant find yet
	public long ticksSinceLastPacketReceived = 0;
	
	//Others
	//used to cache a scan for blocks ahead of storm, to move around
	public float cachedAngleAvoidance = 0;
	
	
	public StormObject(FrontObject front)
	{
		super(front);
		
		pos = new Vec3(0, getLayerHeight(), 0);
		size = Maths.random(250,350) + size;
		overrideAngle = false;
		overrideNewAngle = 0;
		overrideMotion = false;
		overrideNewMotion = 0;
		overrideSpin = false;
		overrideNewSpin = 0;
		isNatural = true;
		isMachineControlled = false;
		canSnowFromCloudTemperature = false;
		alwaysProgresses = false;
		neverDissipate = false;
		isDying = false;
		isViolent = false;
		canProgress = false;
		isCloudless = false;
	  	isFirenado = false;
		isSpout = false;
		stormHumidity = 0;
		stormWind = 0;
		stormTemperature = 0;
		stormStageMax = Stage.NORMAL.getStage();
		stormType = StormType.LAND.ordinal();
		stormStage = Stage.NORMAL.getStage();
		stormIntensity = 0;
		maxIntensity = 0;
		funnel_size = 0;
		stormSizeRate = 1.0F;
		stormSpin = 0.02D;
		stormFormingStrength = 0;
		strength = 100;
		maxHeight = 60;
		canBeDeadly = true;
		isHailing = false;
		
		if (front.getWeatherManager().getWorld().isRemote)
		{
			listParticlesCloud = new ArrayList<EntityRotFX>();
			listParticlesFunnel = new ArrayList<EntityRotFX>();
			listParticlesGround = new ArrayList<EntityRotFX>();
			listParticlesRain = new ArrayList<EntityRotFX>();
		}
	}
	
	public void init()
	{
		super.init();
		
		if (isNatural)
			stormTemperature = 0.0F;
		stormWind = 0.0F;
	}
	
	public boolean isStorm() {return canProgress;}
	public boolean isSevere() {return stormStage > Stage.THUNDER.getStage();}
	public boolean isDeadly() {return stormType == StormType.LAND.ordinal() ? stormStage > Stage.SEVERE.getStage() : stormStage > Stage.TROPICAL_DISTURBANCE.getStage();}
	public boolean isTornado() {return stormType == StormType.LAND.ordinal();}
	public boolean isCyclone() {return stormType == StormType.WATER.ordinal();}

	@Override
	public void readFromNBT()
	{
		super.readFromNBT();
		readNBT();
	}

	@Override
	public void writeToNBT()
	{
		super.writeToNBT();
		writeNBT();
	}
	
	//receiver method
	@Override
	public void readNBT()
	{
		CachedNBTTagCompound nbt = this.nbt;
		super.readNBT();
		stormType = nbt.getInteger("stormType");
		stormStage = nbt.getInteger("levelCurIntensityStage");
		isSpout = nbt.getBoolean("attrib_waterSpout");
		currentTopYBlock = nbt.getInteger("currentTopYBlock");
		stormTemperature = nbt.getFloat("levelTemperature");
		stormHumidity = nbt.getInteger("levelWater");
		layer = nbt.getInteger("layer");
		stormStageMax = nbt.getInteger("levelStormIntensityMax");
		stormIntensity = nbt.getFloat("levelCurStagesIntensity");
		funnel_size = nbt.getFloat("levelCurStageSize");
		stormWind = nbt.getFloat("levelCurStageWind");
		stormSizeRate = nbt.getFloat("levelCurStageSizeRate");
		isViolent = nbt.getBoolean("isViolent");
		isCloudless = nbt.getBoolean("cloudlessStorm");
		isFirenado = nbt.getBoolean("isFirenado");
		stormName = nbt.getString("stormName");
		shouldConvert = nbt.getBoolean("shouldConvert");
		shouldBuildHumidity = nbt.getBoolean("shouldBuildHumidity");
		ticksSinceLastPacketReceived = 0;
		isMachineControlled = nbt.getBoolean("weatherMachineControlled");
		isHailing = nbt.getBoolean("isHailing");
		canProgress = nbt.getBoolean("canProgress");
		neverDissipate = nbt.getBoolean("neverDissipate");
		alwaysProgresses = nbt.getBoolean("alwaysProgresses");
		overrideAngle = nbt.getBoolean("overrideAngle");
		overrideNewAngle = nbt.getFloat("overrideNewAngle");
		overrideMotion = nbt.getBoolean("overrideMotion");
		overrideNewMotion = nbt.getFloat("overrideNewMotion");
		maxRevives = nbt.getInteger("maxRevives");
	}
	
	//compose nbt data for packet (and serialization in future)
	@Override
	public CachedNBTTagCompound writeNBT() {
		nbt.setBoolean("attrib_waterSpout", isSpout);
		nbt.setInteger("currentTopYBlock", currentTopYBlock);
		nbt.setFloat("levelTemperature", stormTemperature);
		nbt.setFloat("levelWater", stormHumidity);
		nbt.setInteger("layer", layer);
		nbt.setInteger("levelCurIntensityStage", stormStage);
		nbt.setFloat("levelCurStagesIntensity", stormIntensity);
		nbt.setFloat("levelStormIntensityMax", stormStageMax);
		nbt.setFloat("levelCurStageSize", funnel_size);
		nbt.setFloat("levelCurStageWind", stormWind);
		nbt.setFloat("levelCurStageSizeRate", stormSizeRate);
		nbt.setInteger("stormType", stormType);
		nbt.setString("stormName", stormName);
		nbt.setBoolean("isViolent", isViolent);
		nbt.setBoolean("shouldConvert", shouldConvert);
		nbt.setBoolean("cloudlessStorm", isCloudless);
		nbt.setBoolean("shouldBuildHumidity", shouldBuildHumidity);
		nbt.setBoolean("isFirenado", isFirenado);
		nbt.setBoolean("weatherMachineControlled", isMachineControlled);
		nbt.setBoolean("isHailing", isHailing);
		nbt.setBoolean("canProgress", canProgress);
		nbt.setBoolean("neverDissipate", neverDissipate);
		nbt.setBoolean("alwaysProgresses", alwaysProgresses);
		nbt.setBoolean("overrideAngle", overrideAngle);
		nbt.setFloat("overrideNewAngle", overrideNewAngle);
		nbt.setBoolean("overrideMotion", overrideMotion);
		nbt.setFloat("overrideNewMotion", overrideNewMotion);
		nbt.setInteger("maxRevives", maxRevives);
		return super.writeNBT();
	}
	
	@SideOnly(Side.CLIENT)
	public void tickRender(float partialTick)
	{
		super.tickRender(partialTick);
	}
	
	public void tick() {
		super.tick();
		manager.getWorld().profiler.startSection("stormObjectTick");
		//adjust posGround to be pos with the ground Y pos for convinient usage
		posGround = new Vec3(pos.posX, pos.posY, pos.posZ);
		posGround.posY = currentTopYBlock;
		if (manager.getWorld().isRemote)
		{
			manager.getWorld().profiler.startSection("clientTick");
			if (!WeatherUtil.isPaused())
			{
				ticksSinceLastPacketReceived++;
				tickClient();
				
				if (isDeadly())
					tornadoHelper.tick(manager.getWorld());

				tickMovementClient();
			}
			manager.getWorld().profiler.endSection();
		}
		else
		{
			manager.getWorld().profiler.startSection("serverTick");
			
			if (isDeadly())
				tornadoHelper.tick(manager.getWorld());
			if (isCloudless)
				if (ConfigMisc.overcast_mode && manager.getWorld().isRaining()) 
					isCloudless = false;

			manager.getWorld().profiler.startSection("tickMovement");
			if (!isMachineControlled)
				tickMovement();
			manager.getWorld().profiler.endSection();
			
			if (!isCloudless)
			{
				manager.getWorld().profiler.startSection("tickWeather");
				tickWeatherEvents();
				manager.getWorld().profiler.endStartSection("tickProgression");
				if (ConfigSimulation.simulation_enable)
					tickProgressionSimulation();
				else
					tickProgressionNormal();
				manager.getWorld().profiler.endStartSection("tickSnowFall");
				tickSnowFall();
				manager.getWorld().profiler.endSection();
			}
			else
				size = ConfigStorm.min_storm_size;		
			manager.getWorld().profiler.endSection();
		}
		
		if (layer == 0) {
			//sync X Y Z, Y gets changed below
			pos_funnel_base = new Vec3(pos.posX, pos.posY, pos.posZ);
	
			if (stormStage >= Stage.TORNADO.getStage()) 
			{
				if (stormStage > Stage.TORNADO.getStage())
				{
					stormFormingStrength = 1;
					pos_funnel_base.posY = posGround.posY;
				}
				else
				{
					//make it so storms touchdown at 0.5F intensity instead of 1 then instantly start going back up, keeps them down for a full 1F worth of intensity val
					float intensityAdj = Math.min(1F, (stormIntensity % 1.0F) * 2F);
					//shouldnt this just be intensityAdj?
					float val = (stormStage + intensityAdj) - Stage.TORNADO.getStage();
					stormFormingStrength = val;
					double yDiff = pos.posY - posGround.posY;
					pos_funnel_base.posY = pos.posY - (yDiff * stormFormingStrength);
				}
			}
			else
				if (stormStage == Stage.SEVERE.getStage())
				{
					stormFormingStrength = 0;
					pos_funnel_base.posY = posGround.posY;
				}
				else
				{
					stormFormingStrength = 0;
					pos_funnel_base.posY = pos.posY;
				}
		}
		manager.getWorld().profiler.endSection();
	}
	
	public void tickMovement()
	{
		if (front.equals(manager.getGlobalFront()))
		{
			//storm movement via wind
			angle = getAdjustedAngle();
	
			if (overrideAngle)
				angle = overrideNewAngle;
	
			//despite overridden angle, still avoid obstacles
			//slight randomness to angle
			Random rand = new Random();
			angle += (rand.nextFloat() - rand.nextFloat()) * 0.15F;
	
			//avoid large obstacles
			double scanDist = 50;
			double scanX = this.pos.posX + (-Math.sin(Math.toRadians(angle)) * scanDist);
			double scanZ = this.pos.posZ + (Math.cos(Math.toRadians(angle)) * scanDist);
			int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), new BlockPos(scanX, 0, scanZ)).getY();
	
			if (this.pos.posY < height)
			{
				float angleAdj = 45;
				angle += angleAdj;
			}
			
			double vecX = -Math.sin(Math.toRadians(angle));
			double vecZ = Math.cos(Math.toRadians(angle));
			float cloudSpeedAmp = 0.2F;
			float finalSpeed = getAdjustedSpeed() * cloudSpeedAmp;
			
			if (stormStage > Stage.SEVERE.getStage() + 1)
				finalSpeed = 0.2F;
			else if (stormStage > Stage.NORMAL.getStage())
				finalSpeed = 0.05F;
			
			if (stormStage > Stage.SEVERE.getStage() + 1)
				finalSpeed /= ((float)(stormStage-Stage.TORNADO.getStage()+1F));
			
			if (overrideMotion) finalSpeed = overrideNewMotion;
			
			if (!isMachineControlled)
			{
				motion.posX = vecX * finalSpeed;
				motion.posZ = vecZ * finalSpeed;
	
				pos.posX += motion.posX;
				pos.posZ += motion.posZ;
			}
		}
		else
		{
			if (!isMachineControlled)
			{
				motion = front.motion;
				pos.posX += motion.posX;
				pos.posZ += motion.posZ;
			}
		}
	}

	public void tickMovementClient()
	{
		if (!isMachineControlled)
		{
			pos.posX += motion.posX;
			pos.posZ += motion.posZ;
		}
	}
	
	public void tickWeatherEvents()
	{
		Random rand = new Random();
		World world = manager.getWorld();
		
		currentTopYBlock = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(MathHelper.floor(pos.posX), 0, MathHelper.floor(pos.posZ))).getY();
		
		if (stormStage > Stage.RAIN.getStage())
		{
			if (rand.nextInt((int)Math.max(1, ConfigStorm.lightning_bolt_1_in_x - (stormStage * 10))) == 0)
			{
				int x = (int) (pos.posX + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) (pos.posZ + rand.nextInt(size) - rand.nextInt(size));
				
				if (world.isBlockLoaded(new BlockPos(x, 0, z)))
				{
					int y = world.getPrecipitationHeight(new BlockPos(x, 0, z)).getY();
					addWeatherEffectLightning(new EntityLightningBolt(world, (double)x, (double)y, (double)z), false);
				}
			}
		}
		
		if (stormHumidity >= 200.0F)
			isHailing = true;
		else
			isHailing = false;
		
		if (isHailing)
		{
			int amount = (int)MathHelper.clamp(ConfigStorm.hail_stones_per_tick * stormHumidity * 0.0001F, 1.0F, ConfigStorm.hail_stones_per_tick);
			EntityPlayer player;
			for (int i = 0; i < amount && world.playerEntities.size() > 0; i++)
			{
				player = world.playerEntities.get(Maths.random(0, world.playerEntities.size()));
				if(pos.distance(player.posX, pos.posY, player.posZ) < size)
				{
					int x = (int) (player.posX + rand.nextInt(64) - rand.nextInt(64));
					int z = (int) (player.posZ + rand.nextInt(64) - rand.nextInt(64));
					
					if (world.isBlockLoaded(new BlockPos(x, getLayerHeight(), z)))
					{
						EntityIceBall hail = new EntityIceBall(world);
						hail.setPosition(x, getLayerHeight(), z);
						world.spawnEntity(hail);
					}
				}
			}
		}
		
		trackAndExtinguishEntities();
	}

	public void trackAndExtinguishEntities()
	{
		if (ConfigStorm.storm_rain_extinguish_delay <= 0) return;

		if (isRaining())
		{
			//efficient caching
			if ((manager.getWorld().getTotalWorldTime()/* + (id * 20)*/) % ConfigStorm.storm_rain_extinguish_delay == 0)
			{
				stormCloseEntities.clear();
				BlockPos posBP = new BlockPos(posGround.posX, posGround.posY, posGround.posZ);
				List<EntityLivingBase> listEnts = manager.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(posBP).grow(size));
				
				for (EntityLivingBase ent : listEnts)
					if (ent.world.canBlockSeeSky(ent.getPosition()))
						stormCloseEntities.add(ent);
			}

			for (EntityLivingBase ent : stormCloseEntities)
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
				
				Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
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
	public ChunkCoordinatesBlock getSnowfallEvenOutAdjustCheck(int x, int y, int z, int sourceMeta) {
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
	public ChunkCoordinatesBlock getSnowfallEvenOutAdjust(int x, int y, int z, int sourceMeta) {
		
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

		if ((canSnowFromCloudTemperature && stormTemperature > 0) || (!canSnowFromCloudTemperature && temperature > 0.15F))
			return false;
		else
		{
			if (par2 >= 0 && par2 < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10)
			{
				IBlockState iblockstate1 = ChunkUtils.getBlockState(world, pos);

				//TODO: incoming new way to detect if blocks can be snowed on https://github.com/MinecraftForge/MinecraftForge/pull/4569/files
				//might not require any extra work from me?
				if ((iblockstate1.getBlock().isAir(iblockstate1, world, pos) || iblockstate1.getBlock() == Blocks.SNOW_LAYER) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos))
					return true;
			}

			return false;
		}
	}
	
	public void tickProgressionSimulation()
	{
		World world = manager.getWorld();
		if (world.getTotalWorldTime() % ConfigStorm.storm_tick_delay == 0 && ConfigStorm.storm_tick_delay > 0)
		{
			//Crickets...
		}
	}
	
	public void tickProgressionNormal()
	{
		World world = manager.getWorld();
		
		if (ticks < ConfigStorm.storm_tick_delay)
			ticks = ConfigStorm.storm_tick_delay;
			
		if (world.getTotalWorldTime() % ConfigStorm.storm_tick_delay == 0 && ConfigStorm.storm_tick_delay > 0)
		{
			Biome biome = world.getBiome(new BlockPos(MathHelper.floor(pos.posX), 0, MathHelper.floor(pos.posZ)));
			float tempAdjustRate = (float)ConfigStorm.temperature_adjust_rate;
			boolean hasWater, hasOcean = false;
			
			if (stormStage > Stage.TORNADO.getStage() || stormType == StormType.WATER.ordinal() && stormStage > Stage.TROPICAL_DEPRESSION.getStage())
				funnel_size = (float) Math.min(Math.pow((stormIntensity - 3.0F) * 14, ConfigStorm.storm_size_curve_mult) * (stormType == StormType.LAND.ordinal() ? stormSizeRate : stormSizeRate * 1.5F), ConfigStorm.max_storm_size);
			else if(funnel_size != 14.0F)
				funnel_size = 14.0F;
			
			size = (int) MathHelper.clamp((funnel_size + ConfigStorm.min_storm_size) * (stormType == StormType.LAND.ordinal() ? 1.5F : 3.0F), ConfigStorm.min_storm_size, ConfigStorm.max_storm_size);
			stormWind = Math.max(6.73F + 2.8F * (stormIntensity - 3.0F), 0.0F);
			
			//temperature scan
			if (biome != null)
			{
				hasOcean = biome.biomeName.toLowerCase().contains("ocean");
				float biomeTempAdj = getTemperatureMCToWeatherSys(CoroUtilCompatibility.getAdjustedTemperature(manager.getWorld(), biome, new BlockPos(MathHelper.floor(pos.posX), 64, MathHelper.floor(pos.posZ))));
				if (stormTemperature > biomeTempAdj)
					stormTemperature -= tempAdjustRate; 
				else if (stormTemperature < biomeTempAdj)
					stormTemperature += tempAdjustRate;
			}
			IBlockState blockID = world.getBlockState(new BlockPos(MathHelper.floor(pos.posX), currentTopYBlock-1, MathHelper.floor(pos.posZ)));
			hasWater = blockID.getMaterial() instanceof MaterialLiquid;
			
			if (isStorm())
			{
				if (shouldBuildHumidity)
				{
					if (!isDying || revives < maxRevives)
						stormHumidity += ConfigStorm.humidity_buildup_rate * WeatherUtil.getHumidity(world, pos.toBlockPos());
					else if (stormHumidity > 0.0F)
						stormHumidity -= ConfigStorm.humidity_spend_rate * WeatherUtil.getHumidity(world, pos.toBlockPos());
					
					if (stormHumidity < 0.0F)
					{
						stormHumidity = 0.0F;
						shouldBuildHumidity = false;
					}
				}
				if (stormHumidity < 50.0F && stormStage > 0)
					stormHumidity = 60.0F;
				
				//force storms to die if its no longer raining while overcast mode is active
				if (ConfigMisc.overcast_mode && !neverDissipate && !manager.getWorld().isRaining())
					isDying = true;
				
				//force rain on while real storm and not dying
				if (!isDying && stormHumidity < stormRainMin)
					stormHumidity = stormRainMin;
				
				if (stormStage == Stage.SEVERE.getStage() && hasWater)
				{
					if (ConfigStorm.high_wind_waterspout_10_in_x != 0 && Maths.random(ConfigStorm.high_wind_waterspout_10_in_x) == 0)
						isSpout = true;
				}
				else
					isSpout = false;
				
				float intensityRate = 0.03F;
				boolean intensify = stormIntensity - (stormStage - 1) > 1.0F;
				
				//speed up forming and greater progression when past forming state
				if (stormStage >= Stage.TORNADO.getStage())
					intensityRate *= 3;

				if (!isDying)
				{
					if (neverDissipate && (!intensify && stormStage <= stormStageMax || alwaysProgresses) || !neverDissipate)
						stormIntensity += intensityRate;
					
					if (intensify && (stormStage < stormStageMax || alwaysProgresses))
					{
						stageNext();
						Weather2.debug("storm ID: " + getUUID().toString() + " - growing, stage: " + stormStage);
								
						//mark is tropical cyclone if needed! and never unmark it!
						if (shouldConvert && stormStage <= WeatherEnum.Stage.THUNDER.getStage() && hasOcean)
						{
							Weather2.debug("storm ID: " + getUUID().toString() + " marked as tropical cyclone!");
							stormType = StormType.WATER.ordinal();
							updateType();
						}
					}
					
					if (!(neverDissipate || alwaysProgresses) && stormStage >= stormStageMax)
					{
						Weather2.debug("storm peaked at: " + stormStage);
						isDying = true;
					}
				}
				else
				{
					if (ConfigMisc.overcast_mode && manager.getWorld().isRaining())
						stormIntensity -= intensityRate * 0.5F;
					else
						stormIntensity -= intensityRate * 0.2F;
						
					if (stormIntensity - (stormStage - 1) <= 0)
					{
						stagePrev();
						Weather2.debug("storm ID: " + this.getUUID().toString() + " - dying, stage: " + stormStage);
						if (stormStage == 2 && revives < maxRevives)
						{
							isDying = false;
							revives++;
							resetStorm();
						}
						else if (stormStage <= 0)
							setNoStorm();
					}
				}
			}
		}
	}
	
	public WeatherEntityConfig getWeatherEntityConfigForStorm()
	{
		return WeatherTypes.weatherEntTypes.get(MathHelper.clamp(stormStage - Stage.TORNADO.getStage(), 0, 6));
	}
	
	public void updateType()
	{
		switch(stormStage)
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
					type = stormStage == Stage.TROPICAL_STORM.getStage() ? WeatherEnum.Type.TROPICAL_STORM : WeatherEnum.Type.HURRICANE;
				else
					type = WeatherEnum.Type.TORNADO;
		}
		
		if (stormType == 1 && stormName.length() == 0)
			stormName = StormNames.get();
	}
	
	public void stageNext() {
		stormStage += 1;
		updateType();
	}
	
	public void stagePrev() {
		stormStage -= 1;
		updateType();
	}
	
	public void resetStorm()
	{
		shouldBuildHumidity = true;
		stormSizeRate = Maths.random(0.75F, 1.35F);
		isViolent = Maths.chance(ConfigStorm.chance_for_violent_storm * 0.01D * 0.25D);
		
		stormStageMax = Math.max(rollDiceOnMaxIntensity(), WeatherEnum.Stage.TORNADO.getStage());
		
		if (isViolent)
		{
			stormSizeRate += Maths.random(0.25F, 1.65F);
			
			if (stormStageMax < 9)
				stormStageMax += 1;
		}
		
		updateType();
		Weather2.debug("Revived Into Deadly Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stormStageMax + " (EF" + (stormStageMax - 4) + ")\nSize Multiplier: " + stormSizeRate * 100 + "%");
	}
	
	public void initRealStorm()
	{
		shouldBuildHumidity = true;
		//new way of storm progression
		if (stormStage != Stage.RAIN.getStage())
		{
			stormStage = Stage.RAIN.getStage();
			stormIntensity = 0.0F;
		}
		
		if (stormStageMax < 1)
			stormStageMax = rollDiceOnMaxIntensity();
		
		if (stormSizeRate < 0.0F)
			stormSizeRate = (float) Maths.random(ConfigStorm.min_size_growth, ConfigStorm.max_size_growth);
		
		if (isViolent || Maths.chance(ConfigStorm.chance_for_violent_storm / 100.0D))
		{
			isViolent = true;
			stormSizeRate += Maths.random(ConfigStorm.min_violent_size_growth, ConfigStorm.max_violent_size_growth);
			if (stormStageMax < Stage.TORNADO.getStage() + 4)
				stormStageMax += 1;
		}
		
		while(Maths.chance(ConfigStorm.chance_for_storm_revival * 0.01D) && revives < ConfigStorm.max_storm_revives)
			revives++;
			
		if (stormStageMax > Stage.SEVERE.getStage())
			Weather2.debug("New Deadly Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stormStageMax + " (EF" + (stormStageMax - 4) + ")\nSize Multiplier: " + stormSizeRate * 100 + "%");
		else
			Weather2.debug("New Normal Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stormStageMax + "\nSize Multiplier: " + stormSizeRate * 100 + "%");
		canProgress = true;
		updateType();
	}

	public int rollDiceOnMaxIntensity()
	{
		if (!Maths.chance(ConfigStorm.chance_for_thunderstorm * 0.01D)) return Stage.RAIN.getStage();
		else if (!Maths.chance(ConfigStorm.chance_for_supercell * 0.01D)) return Stage.THUNDER.getStage();
		 
			
		ConfigList list;
		if (stormType == StormType.LAND.ordinal())
			list = WeatherAPI.getTornadoStageList();
		else
			list = WeatherAPI.getHurricaneStageList();
		
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
		Weather2.debug("storm ID: " + this.getUUID().toString() + " - ended storm event");
		stormStage = Stage.NORMAL.getStage();
		stormIntensity = 0;
		isDead = true;
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient()
	{
		if (isCloudless) return;
		if (particleBehaviorFog == null)
			particleBehaviorFog = new ParticleBehaviorFog(pos.toVec3Coro());
		else if (!Minecraft.getMinecraft().isSingleplayer() || !(Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu))
				particleBehaviorFog.tickUpdateList();
		
		EntityPlayer entP = Minecraft.getMinecraft().player;
		IBlockState state = ConfigCoroUtil.optimizedCloudRendering ? Blocks.AIR.getDefaultState() : ChunkUtils.getBlockState(manager.getWorld(), (int) pos_funnel_base.posX, (int) pos_funnel_base.posY - 1, (int) pos_funnel_base.posZ);
		Material material = state.getMaterial();
		double maxRenderDistance = SceneEnhancer.fogDistance;
		double spinSpeedMax = 0.4D;
		float sizeCloudMult = Math.min(Math.max(size * 0.0015F, 1.0F), getLayerHeight() * 0.02F);
		float sizeFunnelMult = Math.min(Math.max(funnel_size * (isViolent ? 0.016F : 0.008F), 1.0F), getLayerHeight() * 0.02F);
		float sizeOtherMult = Math.min(Math.max(size * 0.003F, 1.0F), getLayerHeight() * 0.035F);
		float heightMult = getLayerHeight() * 0.00290625F;
		float rotationMult = Math.max(heightMult * 0.45F, 1.0F);
		float r = -1.0F, g = -1.0F, b = -1.0F;
		if (!ConfigCoroUtil.optimizedCloudRendering && state.getBlock().equals(Blocks.AIR))
		{
			state = Blocks.DIRT.getDefaultState();
			material = state.getMaterial();
		}
			
		if (material.equals(Material.GROUND) || material.equals(Material.GRASS) || material.equals(Material.LEAVES) || material.equals(Material.PLANTS))
		{
			r = 0.3F; g = 0.25F; b = 0.15F;
		}
		else if (material.equals(Material.SAND))
		{
			r = 0.5F; g = 0.45F; b = 0.35F;
		}
		else if (material.equals(Material.SNOW))
		{
			r = 0.5F; g = 0.5F; b = 0.7F;
		}
		else if (material.equals(Material.WATER) || isSpout)
		{
			r = 0.3F; g = 0.35F; b = 0.7F;
		}
		else if (material.equals(Material.LAVA))
		{
			r = 1.0F; g = 0.45F; b = 0.35F;
		}
		
		if (ConfigSimulation.simulation_enable)
			stormSpin = Math.min(spinSpeedMax, Math.max(stormIntensity * 0.0001F * sizeCloudMult, 0.03F));
		else
			stormSpin = Math.min(spinSpeedMax, Math.max(0.007D * stormStage * sizeCloudMult, 0.03D));
		
		//bonus!
		if (stormType == StormType.WATER.ordinal())
			stormSpin += 0.025D;
		
		if (size == 0) size = 1;
		int delay = Math.max(1, (int)(100F / size));
		int loopSize = 1;
		int extraSpawning = 0;
		
		if (isSevere())
		{
			loopSize += 4;
			extraSpawning = (int) Math.min(funnel_size * 2.375F, 1000.0F);
		}
		
		Random rand = new Random();
		Vec3 playerAdjPos = new Vec3(entP.posX, pos.posY, entP.posZ);
		
		//spawn clouds
		if (ConfigCoroUtil.optimizedCloudRendering)
		{
			/*int count = 9;

			for (int i = 0; i < count && canSpawnParticle(); i++)
			{
				if (!lookupParticlesCloud.containsKey(i))
				{
					//position doesnt matter, set by renderer while its invisible still
					Vec3 tryPos = new Vec3(pos.posX, layers.get(layer), pos.posZ);
					EntityRotFX particle;
					if (WeatherUtil.isAprilFoolsDay())
						particle = spawnFogParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, ParticleRegistry.chicken);
					else
						particle = spawnFogParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_test);
					//offset starting rotation for even distribution except for middle one
					if (i != 0)
					{
						double rotPos = (i - 1);
						float radStart = (float) ((360D / 8D) * rotPos);
						particle.rotationAroundCenter = radStart;
					}

					lookupParticlesCloud.put(i, particle);
					listParticles.add(particle);
				}
			}
			
			if (isSevere())
			{
				count = 32;

				for (int i = 0; i < count && canSpawnParticle(); i++)
				{
					if (!lookupParticlesCloudLower.containsKey(i)) {

						//position doesnt matter, set by renderer while its invisible still
						Vec3 tryPos = new Vec3(pos.posX, layers.get(layer) + (rand.nextDouble() * size * 0.4D), pos.posZ);
						EntityRotFX particle;
						if (WeatherUtil.isAprilFoolsDay())
							particle = spawnFogParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 1, ParticleRegistry.chicken);
						else
							particle = spawnFogParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 1, net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_test);
						

						//set starting offset for even distribution
						double rotPos = i % 15;
						float radStart = (float) ((360D / 16D) * rotPos);
						particle.rotationAroundCenter = radStart;

						lookupParticlesCloudLower.put(i, particle);
						listParticles.add(particle);
					}
				}
			}*/
		}
		else
		{
			if (this.manager.getWorld().getTotalWorldTime() % (delay + ConfigParticle.cloud_particle_delay) == 0) {
				for (int i = 0; i < loopSize && canSpawnParticle(); i++)
				{
					if (!ConfigCoroUtil.optimizedCloudRendering && listParticlesCloud.size() < (size + extraSpawning) / 1F) {
						double spawnRad = size * 1.2D;
						Vec3 tryPos = new Vec3(pos.posX + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), getLayerHeight() + (rand.nextDouble() * 40.0F), pos.posZ + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
						if (tryPos.distance(playerAdjPos) < maxRenderDistance) {
							if (getAvoidAngleIfTerrainAtOrAheadOfPosition(getAdjustedAngle(), tryPos) == 0) {
								ExtendedEntityRotFX particle;
								if (WeatherUtil.isAprilFoolsDay()) {
									particle = spawnFogParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, ParticleRegistry.chicken);
									particle.setColor(1F, 1F, 1F);
								}
								else
								{
									float finalBright = Math.min(0.7F, 0.5F + (rand.nextFloat() * 0.2F)) + (stormStage >= Stage.RAIN.getStage() ? -0.2F : 0.0F );
									particle = spawnFogParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0);
									particle.setColor(finalBright, finalBright, finalBright);
									
									if (isFirenado && isSevere())
									{
											particle.setParticleTexture(net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_fire);
											particle.setColor(1F, 1F, 1F);
									}
								}

								particle.rotationPitch = Maths.random(70.0F, 110.0F);
								particle.setScale(600.0F * sizeCloudMult);
								listParticlesCloud.add(particle);
							}
						}
					}
				}
			}
		}
		
		//ground effects
		if (!ConfigCoroUtil.optimizedCloudRendering && stormType == StormType.LAND.ordinal() && stormStage > Stage.SEVERE.getStage() && r >= 0.0F && !material.isLiquid())
		{
			for (int i = 0; i < 4 && canSpawnParticle(); i++)
			{
				if (listParticlesGround.size() < 300)
				{
					double spawnRad = funnel_size + 150.0D;
					
					Vec3 tryPos = new Vec3(pos_funnel_base.posX + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), posGround.posY, pos_funnel_base.posZ + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distance(playerAdjPos) < maxRenderDistance)
					{
						int groundY = WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(), new BlockPos((int)tryPos.posX, 0, (int)tryPos.posZ)).getY();
						ExtendedEntityRotFX particle;
						if (WeatherUtil.isAprilFoolsDay())
							particle = spawnFogParticle(tryPos.posX, groundY + 3, tryPos.posZ, 1, ParticleRegistry.potato);
						else
							particle = spawnFogParticle(tryPos.posX, groundY + 3, tryPos.posZ, 1);
						particle.setColor(r, g, b);
						particle.setTicksFadeInMax(40);
						particle.setTicksFadeOutMax(80);
						particle.setGravity(0.01F);
						particle.setMaxAge(100);
						particle.setScale(190.0F * sizeFunnelMult);
						particle.rotationYaw = rand.nextInt(360);
						particle.rotationPitch = rand.nextInt(80);
						
						listParticlesGround.add(particle);
					}
				}
			}
		}
		
		delay = 1;
		loopSize = 3 + (int)(funnel_size / 80);
		double spawnRad = funnel_size * 0.02F;
		
		if (stormStage >= Stage.TORNADO.getStage() + 1) 
		{
			spawnRad *= 48.25D;
			sizeMaxFunnelParticles = 2000;
		}
		
		//spawn funnel
		if (isDeadly() && stormType == 0 || (isSpout))
		{
			if (this.manager.getWorld().getTotalWorldTime() % (delay + ConfigParticle.funnel_particle_delay) == 0)
			{
				for (int i = 0; i < loopSize && listParticlesFunnel.size() <= sizeMaxFunnelParticles && canSpawnParticle(); i++)
				{
					if (listParticlesFunnel.size() < sizeMaxFunnelParticles)
					{
						Vec3 tryPos = new Vec3(pos_funnel_base.posX + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), pos.posY, pos_funnel_base.posZ + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
						
						if (tryPos.distance(playerAdjPos) < maxRenderDistance)
						{
							ExtendedEntityRotFX particle;
							if (!isFirenado)
								if (WeatherUtil.isAprilFoolsDay())
									particle = spawnFogParticle(tryPos.posX, pos_funnel_base.posY, tryPos.posZ, listParticlesRain.size() > 100 ? 1 : 2, ParticleRegistry.potato);
								else
									particle = spawnFogParticle(tryPos.posX, pos_funnel_base.posY, tryPos.posZ, listParticlesRain.size() > 100 ? 1 : 2);
							else
								particle = spawnFogParticle(tryPos.posX, pos_funnel_base.posY, tryPos.posZ, listParticlesRain.size() > 100 ? 1 : 2, net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_fire);

							
							//move these to a damn profile damnit!
							particle.setMaxAge(150 + ((stormStage-1) * 10) + rand.nextInt(100));
							particle.rotationYaw = rand.nextInt(360);
							
							float finalBright = Math.min(0.7F, 0.35F + (rand.nextFloat() * 0.2F));
							
							//highwind aka spout in this current code location
							if (stormStage == Stage.SEVERE.getStage())
							{
								particle.setScale(250.0F * sizeFunnelMult);
								particle.setColor(finalBright, finalBright, finalBright);
							}
							else
							{
								particle.setScale(500.0F * sizeFunnelMult);
								if (r >= 0.0F)
								{
									particle.setColor(r, g, b);
									particle.setFinalColor(1.0F - stormFormingStrength, finalBright, finalBright, finalBright);
									particle.setColorFade(0.75F);
								}
								else
									particle.setColor(finalBright, finalBright, finalBright);
							}

							if (isFirenado)
							{
								particle.setRBGColorF(1F, 1F, 1F);
								particle.setScale(particle.getScale() * 0.7F);
							}
							
							listParticlesFunnel.add(particle);
						}
					}
				}
			}
		}
		
		if (ConfigParticle.enable_distant_downfall && ticks % 20 == 0 && ConfigParticle.distant_downfall_particle_rate > 0.0F && listParticlesRain.size() < 1000 && stormStage > Stage.THUNDER.getStage() && isRaining())
		{
			int particleCount = (int)Math.ceil(stormHumidity * stormStage * ConfigParticle.distant_downfall_particle_rate * 0.005F);
			
			for (int i = 0; i < particleCount && listParticlesRain.size() < 1000 && canSpawnParticle(); i++)
			{
				double spawnRad2 = size;
				Vec3 tryPos = new Vec3(pos.posX + (rand.nextDouble()*spawnRad2) - (rand.nextDouble()*spawnRad2), pos.posY + rand.nextInt(50), pos.posZ + (rand.nextDouble()*spawnRad2) - (rand.nextDouble()*spawnRad2));
				if (tryPos.distance(playerAdjPos) < maxRenderDistance)
				{
					ExtendedEntityRotFX particle;
					float finalBright = Math.min(0.7F, 0.5F + (rand.nextFloat() * 0.2F)) + (stormStage >= Stage.RAIN.getStage() ? -0.2F : 0.0F );
					particle = spawnFogParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 2);
					particle.setMaxAge(200 + rand.nextInt(100));
					particle.setScale(550.0F * sizeOtherMult);
					particle.setAlphaF(0.0F);
					particle.setColor(finalBright, finalBright, finalBright);
					particle.facePlayer = false;
					listParticlesRain.add(particle);
				}
			}
		}
		
		for (int i = 0; i < listParticlesFunnel.size(); i++)
		{
			EntityRotFX ent = listParticlesFunnel.get(i);
			if (!ent.isAlive() || ent.getPosY() > getLayerHeight() + 200)
			{
				ent.setExpired();
				listParticlesFunnel.remove(ent);
			}
			else
			{
				 double var16 = this.pos.posX - ent.getPosX();
				 double var18 = this.pos.posZ - ent.getPosZ();
				 ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
				 ent.rotationYaw += ent.getEntityId() % 90;
				 ent.rotationPitch = -30F;
				 
				 spinEntity(ent);
			}
		}
		
		for (int i = 0; i < listParticlesCloud.size(); i++)
		{
			EntityRotFX ent = listParticlesCloud.get(i);
			if (!ent.isAlive() || stormType == StormType.WATER.ordinal() && ent.getDistance(pos.posX, ent.posY, pos.posZ) < funnel_size)
			{
				ent.setExpired();
				listParticlesCloud.remove(ent);
			}
			else
			{
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
				double curDist = 10D;
				
				if (isSevere())
				{
					double speed = stormSpin + (rand.nextDouble() * 0.04D) * rotationMult;
					double distt = Math.max(ConfigStorm.max_storm_size, funnel_size + 20.0D);//300D;
					
					if (stormType == 1)
					{
						speed *= 2.0D;
						distt *= 2.0D;
					}
					
					double vecX = ent.getPosX() - pos.posX;
					double vecZ = ent.getPosZ() - pos.posZ;
					float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
					
					//fix speed causing inner part of formation to have a gap
					angle += speed * 50D;
					
					angle -= (ent.getEntityId() % 10) * 3D;
					
					//random addition
					angle += rand.nextInt(10) - rand.nextInt(10);
					
					if (curDist > distt)
					{
						angle += 40;
					}
					
					//keep some near always - this is the lower formation part
					if (ent.getEntityId() % 40 < 5) {
						if (stormStage >= Stage.TORNADO.getStage()) {
							if (stormType == StormType.WATER.ordinal()) {
								angle += 40 + ((ent.getEntityId() % 5) * 4);
								if (curDist > 150 + ((stormStage-Stage.TORNADO.getStage()+1) * 30)) {
									angle += 10;
								}
							} else {
								angle += 30 + ((ent.getEntityId() % 5) * 4);
							}
							
						} else {
							//make a wider spinning lower area of cloud, for high wind
							if (curDist > 150) {
								angle += 50 + ((ent.getEntityId() % 5) * 4);
							}
						}
						
						double var16 = this.pos.posX - ent.getPosX();
						double var18 = this.pos.posZ - ent.getPosZ();
						ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
						ent.rotationPitch = -30F - (ent.getEntityId() % 10);
					}
					else
					{
						ent.rotationPitch = (float) (90.0F - (90.0f * Math.min(ent.getPosY() / (getLayerHeight() + ent.getScale() * 0.75F), 1.0F)));
					}
					
					if (curSpeed < speed * 20D)
					{
						ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
						ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
					}
				}
				else
				{
					float cloudMoveAmp = 0.2F * (1 + layer);
					
					float speed = getAdjustedSpeed() * cloudMoveAmp;
					float angle = getAdjustedAngle();

					//TODO: prevent new particles spawning inside or near solid blocks

					if ((manager.getWorld().getTotalWorldTime()) % 40 == 0) {
						ent.avoidTerrainAngle = getAvoidAngleIfTerrainAtOrAheadOfPosition(angle, new Vec3(ent.getPos()));
					}

					angle += ent.avoidTerrainAngle;

					if (ent.avoidTerrainAngle != 0)
						speed *= 0.5D;
					
					
					if (curSpeed < speed * 1D)
					{
						ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
						ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
					}
				}
				
				
				float dropDownSpeedMax = 0.5F;
				
				if (stormType == 1 || stormStage > 8)
					dropDownSpeedMax = 1.9F;
				
				if (ent.getMotionY() < -dropDownSpeedMax)
					ent.setMotionY(-dropDownSpeedMax);
				
				
				if (ent.getMotionY() > dropDownSpeedMax) 
					ent.setMotionY(dropDownSpeedMax);
				
			}
		}
		
		
		
		for (int i = 0; i < listParticlesRain.size(); i++)
		{
			EntityRotFX ent = listParticlesRain.get(i);
			if (!ent.isAlive())
				listParticlesRain.remove(ent);
			
			if (ent.motionY > -1.0D * heightMult)
				ent.motionY = ent.motionY - (0.1D);
			
			double speed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
			double spin = stormSpin + 0.04F;
			double vecX = ent.getPosX() - pos.posX;
			double vecZ = ent.getPosZ() - pos.posZ;
			float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
			ent.rotationPitch = 0.0F;
			ent.rotationYaw = angle + 90;
			
			if (ent.getAlphaF() < 0.01F)
				ent.setAlphaF(ent.getAlphaF() + 0.01F);
			if (speed < spin * 15.0D * rotationMult)
			{
				ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * spin);
				ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * spin);
			}
		}
		
		for (int i = 0; i < listParticlesGround.size(); i++)
		{
			EntityRotFX ent = listParticlesGround.get(i);
			
			if (!ent.isAlive())
				listParticlesGround.remove(ent);
			else
				 spinEntity(ent);
		}
	}
	
	public float getAdjustedSpeed() {
		return manager.windManager.windSpeed;
	}
	
	public float getAdjustedAngle() {
		float angle = manager.windManager.windAngle;
		
		float angleAdjust = Math.max(10, Math.min(45, 45F * stormTemperature * 0.2F));
		float targetYaw = 0;
		
		//coldfronts go south to 0, warmfronts go north to 180
		if (stormTemperature > 0)
			targetYaw = 180;
		else
			targetYaw = 0;
		
		float bestMove = MathHelper.wrapDegrees(targetYaw - angle);
		
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
				double scanX = pos.posX + (-Math.sin(Math.toRadians(angle + scanAngle)) * scanDistRange);
				double scanZ = pos.posZ + (Math.cos(Math.toRadians(angle + scanAngle)) * scanDistRange);

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
		StormObject entT = this;
		StormObject entity = this;
		WeatherEntityConfig conf = getWeatherEntityConfigForStorm();
		
		float heightMult = getLayerHeight() * 0.00490625F;
		float rotationMult = heightMult * 0.5F * ((isViolent ? 3.1F : 1.55F) + Math.min((stormStage - 5.0F) / 3.0F, 2.0F));
		
		World world = CoroUtilEntOrParticle.getWorld(obj);
		long worldTime = world.getTotalWorldTime();
		
		Entity ent = null;
		if (obj instanceof Entity)
			ent = (Entity) obj;
		
		double radius = 10D, scale = conf.tornadoWidthScale;
		double d1 = entity.pos.posX - CoroUtilEntOrParticle.getPosX(obj), d2 = entity.pos.posZ - CoroUtilEntOrParticle.getPosZ(obj);
		
		if (conf.type == WeatherEntityConfig.TYPE_SPOUT)
		{
			float range = 30F * (float) Math.sin((Math.toRadians(((worldTime * 0.5F)) % 360)));
			float heightPercent = (float) (1F - ((CoroUtilEntOrParticle.getPosY(obj) - posGround.posY) / (pos.posY - posGround.posY)));
			float posOffsetX = (float) Math.sin((Math.toRadians(heightPercent * 360F)));
			float posOffsetZ = (float) -Math.cos((Math.toRadians(heightPercent * 360F)));
			d1 += range*posOffsetX;
			d2 += range*posOffsetZ;
		}
		
		float f = (float)((Math.atan2(d2, d1) * 180D) / Math.PI) - 90F;

		for (; f < -180F; f += 360F);
		for (; f >= 180F; f -= 360F);

		double distY = entity.pos.posY - CoroUtilEntOrParticle.getPosY(obj);
		double distXZ = Math.sqrt(Math.abs(d1)) + Math.sqrt(Math.abs(d2));

		if (CoroUtilEntOrParticle.getPosY(obj) - entity.pos.posY < 0.0D)
			distY = 1.0D;
		else
			distY = CoroUtilEntOrParticle.getPosY(obj) - entity.pos.posY;

		if (distY > maxHeight)
			distY = maxHeight;

		float weight = WeatherUtilEntity.getWeight(obj, true);
		double grab = (10D / weight) * ((Math.abs((maxHeight - distY)) / maxHeight));
		float pullY = 0.0F;

		if (distXZ > 5D)
			grab = grab * (radius / distXZ);
		

		pullY += (float)(conf.tornadoLiftRate / (weight / 2F));
		
		if (obj instanceof EntityPlayer)
		{
			double adjPull = 0.2D / ((weight * ((distXZ + 1D) / radius)));
			pullY += adjPull;
			double adjGrab = MathHelper.clamp(10D * (((float)(((double)WeatherUtilEntity.playerInAirTime + 1D) / 400D))), -50.0D, 50.0D);
			grab = grab - adjGrab;

			if (CoroUtilEntOrParticle.getMotionY(obj) > -0.8)
				ent.fallDistance = 0F;
		}
		else if (obj instanceof EntityLivingBase)
		{
			double adjPull = 0.005D / ((weight * ((distXZ + 1D) / radius)));
			pullY += adjPull;
			int airTime = ent.getEntityData().getInteger("timeInAir");
			double adjGrab = MathHelper.clamp(10D * (((float)(((double)(airTime) + 1D) / 400D))), -50.0D, 50.0D);

			grab = grab - adjGrab;

			if (ent.motionY > -1.5) ent.fallDistance = 0F;
			if (ent.motionY > 0.3F) ent.motionY = 0.3F;
			ent.onGround = false;
		}
		
		
		grab += conf.relTornadoSize;
		double profileAngle = Math.max(1, (75D + grab - (10D * scale)));
		
		f = (float)((double)f + profileAngle);

		if (entT != null && entT.scale != 1F) f += 20 - (20 * entT.scale);
		
		float f3 = (float)Math.cos(-f * 0.01745329F - (float)Math.PI);
		float f4 = (float)Math.sin(-f * 0.01745329F - (float)Math.PI);
		float f5 = conf.tornadoPullRate * 1.5F;
		
		if (entT != null && entT.scale != 1F) f5 *= entT.scale * 1.2F;

		if (obj instanceof EntityLivingBase)
			f5 /= (WeatherUtilEntity.getWeight(obj, true) * ((distXZ + 1D) / radius));
		
		//if player and not spout
		if (obj instanceof EntityPlayer && conf.type != 0)
			f5 *= ent.onGround ? 10.5F : 5.0F;
		else if (obj instanceof EntityLivingBase && conf.type != 0)
			f5 *= 1.5F;

		if (conf.type == WeatherEntityConfig.TYPE_SPOUT && obj instanceof EntityLivingBase)
			f5 *= 0.3F;
		
		float moveX = f3 * f5;
		float moveZ = f4 * f5;
		//tornado strength changes
		float str = 1F;
		
		str = strength * 1.25f;
		
		if (obj instanceof EntityLivingBase && conf.type == WeatherEntityConfig.TYPE_SPOUT) str *= 0.3F;
		if (stormType == StormType.WATER.ordinal()) str *= 0.25F;
		pullY *= str / 100F;
		
		if (entT != null && entT.scale != 1F)
		{
			pullY *= entT.scale * 1.0F;
			pullY += 0.002F;
		}
		
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

	@SideOnly(Side.CLIENT)
	public ExtendedEntityRotFX spawnFogParticle(double x, double y, double z, int parRenderOrder) {
		return spawnFogParticle(x, y, z, parRenderOrder, net.mrbt0907.weather2.registry.ParticleRegistry.cloud256);
	}
	
	@SideOnly(Side.CLIENT)
	public ExtendedEntityRotFX spawnFogParticle(double x, double y, double z, int parRenderOrder, TextureAtlasSprite tex) {
		double speed = 0D;
		Random rand = new Random();
		ExtendedEntityRotFX entityfx = new ExtendedEntityRotFX(manager.getWorld(), x, y, z, (rand.nextDouble() - rand.nextDouble()) * speed, 0.0D, (rand.nextDouble() - rand.nextDouble()) * speed, tex);
		entityfx.pb = particleBehaviorFog;
		entityfx.renderOrder = parRenderOrder;
		particleBehaviorFog.initParticle(entityfx);
		
		entityfx.setCanCollide(false);
		entityfx.callUpdatePB = false;
		
		if (stormStage == Stage.NORMAL.getStage())
			entityfx.setMaxAge(300 + rand.nextInt(100));
		else
			entityfx.setMaxAge((size/2) + rand.nextInt(100));
		
		//pieces that move down with funnel need render order shift, also only for relevant storm formations
		if (entityfx.getEntityId() % 20 < 5 && isSevere())
		{
			entityfx.renderOrder = 1;
			entityfx.setMaxAge((size) + rand.nextInt(100));
		}

		//temp?
		if (ConfigCoroUtil.optimizedCloudRendering)
			entityfx.setMaxAge(400);
		
		ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
		particleBehaviorFog.particles.add(entityfx);
		if (ClientTickHandler.weatherManager != null)
			ClientTickHandler.weatherManager.addWeatherParticle(entityfx);
		
		return entityfx;
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
		if (!wipe)
		{
			for (EntityRotFX particle : listParticlesCloud) particle.setExpired();
				listParticlesCloud.clear();
			for (EntityRotFX particle : listParticlesFunnel) particle.setExpired();
				listParticlesFunnel.clear();
			for (EntityRotFX particle : listParticlesGround) particle.setExpired();
				listParticlesGround.clear();
			for (EntityRotFX particle : listParticlesRain) particle.setExpired();
				listParticlesRain.clear();	
		}
		
		if (particleBehaviorFog != null)
			particleBehaviorFog.particles.clear();
		
		if (wipe)
			particleBehaviorFog = null;
	}
	
	public float getTemperatureMCToWeatherSys(float parOrigVal) {
		return parOrigVal - 0.3F;
	}
	
	public void addWeatherEffectLightning(EntityLightningBolt parEnt, boolean custom) {
		manager.getWorld().weatherEffects.add(parEnt);
		PacketLightning.send(manager.getDimension(), parEnt, custom);
	}
	
	@Override
	public int getNetRate() {
		if (stormStage >= Stage.SEVERE.getStage()) {
			return 2;
		} else {
			return super.getNetRate();
		}
	}
	
	public void setAngle(float Angle)
	{
		overrideAngle = true;
		overrideNewAngle = Math.max(Math.min(Angle, 360.0F), 0.0F);
	}
	
	public void setSpeed(float Speed)
	{
		overrideMotion = true;
		overrideNewMotion = Math.max(Speed, 0.0F); 
	}
	
	public void setSpin(float Speed)
	{
		overrideSpin = true;
		overrideNewSpin = Math.max(Speed, 0.0F);
	}

	public void setStage(int stage)
	{
		stormStage = stage;
		updateType();
	}

	public boolean isDrizzling()
	{
		return stormHumidity >= 50.0F && stormHumidity < 100.0F;
	}
	
	public boolean isRaining()
	{
		return stormHumidity >= 100.0F;
	}

	public boolean hasDownfall()
	{
		return stormHumidity >= 50.0F;
	}
	
	@Override
	public int getStage()
	{
		return stormStage;
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

	@Override
	public float getDownfall()
	{
		return stormHumidity;
	}

	@Override
	public float getDownfall(Vec3 pos)
	{
		return stormHumidity ;
	}

	@Override
	public float getDownfall(BlockPos pos)
	{
		return stormHumidity;
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
		return stormWind;
	}
	
	@Override
	public String getName()
	{
		boolean truth = stormName.length() == 0;
		switch(type)
		{
			case CLOUD:
				return (truth ? "" : stormName + " ") + (isHailing ? "Hailing " : "") + "Cloud";
			case RAIN:
				return (truth ? "" : stormName + " ") + (isHailing ? "Hailing " : "") + (hasDownfall() ? stormTemperature <= 0.0F ? "Snowstorm": "Rainstorm" : "Cloud");
			case THUNDER:
				return (truth ? "" : stormName + " ") + (isHailing ? "Hailing " : "") + "Thunderstorm";
			case SUPERCELL:
				return (truth ? "" : stormName + " ") + (isHailing ? "Hailing " : "") + "Supercell";
			case TROPICAL_DISTURBANCE:
				return  (isHailing ? "Hailing " : "") + "Tropical Disturbance" + (truth ? "" : " " + stormName);
			case TROPICAL_DEPRESSION:
				return (isHailing ? "Hailing " : "") + "Tropical Depression" + (truth ? "" : " " + stormName);
			case TROPICAL_STORM:
				return (isHailing ? "Hailing " : "") + "Tropical Storm " + stormName;
			case TORNADO:
				return (truth ? "" : stormName + " ") + (ConfigStorm.enable_ef_scale ? "EF" + (stormStage - Stage.TORNADO.getStage()) : "F" + (int)MathHelper.clamp(Math.floor(funnel_size * 0.0206611570247933884297520661157F), 0, stormStageMax - 4)) + " " + (isHailing ? "Hailing " : "") + "Tornado";
			case HURRICANE:
				return (isHailing ? "Hailing " : "") + "Hurricane " + stormName + " - Category " + (stormStage - Stage.TORNADO.getStage());
			default:
				return (isHailing ? "Hailing " : "") + "Unknown Storm";
		}
	}
	
	@Override
	public String getTypeName()
	{
		boolean truth = stormName.length() == 0;
		
		switch(type)
		{
			case TORNADO:
				return (truth ? "" : stormName + " ") + (ConfigStorm.enable_ef_scale ? "EF" + (stormStage - Stage.TORNADO.getStage()) : "F" + (int)MathHelper.clamp(Math.floor(funnel_size * 0.0206611570247933884297520661157F), 0, stormStageMax - Stage.TORNADO.getStage()));
			case HURRICANE:
				return stormName + " C" + (stormStage - Stage.TORNADO.getStage());
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

	@Override
	public float getAngle()
	{
		return angle;
	}

	@Override
	public float getSpeed()
	{
		return (float) motion.speed();
	}

	@Override
	public int getParticleCount()
	{
		return particleBehaviorFog == null ? 0 : particleBehaviorFog.particles.size();
	}

	@Override
	public boolean canSpawnParticle()
	{
		return ConfigParticle.max_particles < 0 || particleBehaviorFog.particles.size() < ConfigParticle.max_particles;
	}
}
