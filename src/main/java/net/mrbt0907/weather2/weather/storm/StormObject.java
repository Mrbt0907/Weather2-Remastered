package net.mrbt0907.weather2.weather.storm;

import java.util.*;

import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.util.*;
import net.minecraft.block.Block;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.IWeatherLayered;
import net.mrbt0907.weather2.api.IWeatherRain;
import net.mrbt0907.weather2.api.IWeatherStages;
import net.mrbt0907.weather2.client.weather.WeatherSystemClient;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.config.ConfigSimulation;
import net.mrbt0907.weather2.config.ConfigSnow;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.entity.EntityIceBall;
import net.mrbt0907.weather2.entity.EntityLightningBolt;
import net.mrbt0907.weather2.network.packets.PacketLightning;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.player.PlayerData;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.server.weather.WeatherSystemServer;
import net.mrbt0907.weather2.util.*;
import net.mrbt0907.weather2.weather.WeatherSystem;
import net.mrbt0907.weather2.weather.WindManager;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorFog;
import extendedrenderer.particle.entity.EntityRotFX;

@SuppressWarnings("deprecation")
public class StormObject extends WeatherObject implements IWeatherRain, IWeatherStages, IWeatherLayered
{
	//Rendering
	@SideOnly(Side.CLIENT)
	public HashMap<Integer, EntityRotFX> lookupParticlesCloud;
	@SideOnly(Side.CLIENT)
	public HashMap<Integer, EntityRotFX> lookupParticlesCloudLower;
	@SideOnly(Side.CLIENT)
	public HashMap<Integer, EntityRotFX> lookupParticlesFunnel;
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesCloud;
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesGround;
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesFunnel;
	@SideOnly(Side.CLIENT)
	public ParticleBehaviorFog particleBehaviorFog;
	public int sizeMaxFunnelParticles = 600;
	
	//Basic Info
	public static int weather_layer_0 = ConfigStorm.cloud_layer_0_height;
	public static int weather_layer_1 = ConfigStorm.cloud_layer_1_height;
	public static int weather_layer_2 = ConfigStorm.cloud_layer_2_height;
	public static List<Integer> layers = new ArrayList<Integer>(Arrays.asList(weather_layer_0, weather_layer_1, weather_layer_2));
	public int layer = 0;
	
	//Storm Info
	/**What player this storm has spawned for. Mainly used to determine who to aim at when spawning*/
	public String player = "";
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
	/**If true, the storm will start to decay*/
	public boolean isDying = false;
	/**If true, the storm has a higher potential to be stronger than a normal storm*/
	public boolean isViolent = false;
	/**If true, the storm is a tornado or a hurricane*/
	public boolean isDeadly = true;
	//Populate sky with stormless/cloudless storm objects in order to allow clear skies with current design
	/**Unknown*/
  	public boolean isCloudless = false;
  	/**If true, the storm will produce a firenado*/
	public boolean isFirenado = false;
	//used for sure, rain is dependant on water level values
	/**If true, the storm will start to drop rain*/
	public boolean isRaining = false;
	/**If true, the storm will produce a water spout in the water below*/
	public boolean isSpout = false;
	//cloud formation data, helps storms
	/**Determines how much rain water is built up in the storm*/
	public float stormRain = 0; //builds over water and humid biomes, causes rainfall (not technically a storm)
	/**Unknown*/
	public float stormWind = 0; //high elevation builds this, plains areas lowers it, 0 = no additional speed ontop of global speed
	/**Unknown*/
	public float stormTemperature = 0; //negative for cold, positive for warm, we subtract 0.7 from vanilla values to make forest = 0, plains 0.1, ocean -0.5, etc
	/**Determines how much rain can build up before it starts to rain*/
	public float stormMaxRain = 100;
	/**Determines which entities get extinguished when under the storm*/
	public List<EntityLivingBase> stormCloseEntities = new ArrayList<>();
	//storm data, used when its determined a storm will happen from cloud front collisions
	/**Determines how strong this storm will be*/
	public int stormStageMax = Stage.NORMAL.getInt(); //calculated from colliding warm and cold fronts, used to determine how crazy a storm _will_ get
	//used to mark difference between land and water based storms
	/**Determines where the storm intensified at. Creates a hurricane with WATER and creates a tornado with LAND*/
	public int stormType = Type.LAND.getInt();
	//revision, ints for each stage of intensity, and a float for the intensity of THAT current stage
	/**Determines how strong a storm currently is*/
	public int stormStage = Stage.NORMAL.getInt();
	/**Determines how strong a storm can possibly be*/
	public int maxStage = Stage.STAGE5.getInt();
	/**Determines how much progression a storm reached in its current stage*/
	public float stormIntensity = 0;
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
	//Enums

	/**Enumerations for detecting what kind of storm it will become
	 * @value LAND = Tornado
	 * @value WATER = Hurricane*/
	public enum Type {
		LAND(0), WATER(1);
	
		private final int integer;
		
		Type(int integer){
			this.integer = integer;
		}
		
		public int getInt() {
			return integer;
		}
	}

	/**Enumerations for each stage of the storm.
	 * Unused in the new progression system*/
	public enum Stage {
		NORMAL(0), THUNDER(1), SEVERE(2), HAIL(3), STAGE0(4), STAGE1(5),
		STAGE2(6), STAGE3(7), STAGE4(8), STAGE5(9), STAGEOTHER(10);

		private final int intensity;
		
		Stage(int intensity) {
			this.intensity = intensity;
		}
		
		public int getInt() {return intensity;}
	}

	/**Unknown value*/
	public float scale = 1F;
	/**Where the top block under the storm is at*/
	public int currentTopYBlock = -1;
	
	public TornadoHelper tornadoHelper = new TornadoHelper(this);
	
	//public Set<ChunkCoordIntPair> doneChunks = new HashSet<ChunkCoordIntPair>();
	public int updateLCG = (new Random()).nextInt();
	
	
	
	public Vec3 pos_funnel_base = new Vec3(pos.xCoord, pos.yCoord, pos.zCoord); //for formation / touchdown progress, where all the ripping methods scan from
	
	//there is an issue with rainstorms sometimes never going away, this is a patch to mend the underlying issue i cant find yet
	public long ticksSinceLastPacketReceived = 0;
	
	//Others
	//used to cache a scan for blocks ahead of storm, to move around
	public float cachedAngleAvoidance = 0;
	
	
	public StormObject(WeatherSystem parManager)
	{
		super(parManager);
		
		pos = new Vec3(0, weather_layer_0, 0);
		size = Maths.random(250,350);
		player = "";
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
		isDeadly = true;
		isCloudless = false;
	  	isFirenado = false;
		isRaining = false;
		isSpout = false;
		stormRain = 0;
		stormWind = 0;
		stormTemperature = 0;
		stormMaxRain = 100;
		stormStageMax = Stage.NORMAL.getInt();
		stormType = Type.LAND.getInt();
		stormStage = Stage.NORMAL.getInt();
		maxStage = Stage.STAGE5.getInt();
		stormIntensity = 0;
		maxIntensity = 0;
		funnel_size = 0;
		stormSizeRate = 1.0F;
		stormSpin = 0.02D;
		stormFormingStrength = 0;
		strength = 100;
		maxHeight = 60;
		canBeDeadly = true;
		
		if (parManager.getWorld().isRemote)
		{
			listParticlesCloud = new ArrayList<EntityRotFX>();
			listParticlesFunnel = new ArrayList<EntityRotFX>();
			listParticlesGround = new ArrayList<EntityRotFX>();
			lookupParticlesCloud = new HashMap<>();
			lookupParticlesCloudLower = new HashMap<>();
			lookupParticlesFunnel = new HashMap<>();
		}
	}
	
	public void init()
	{
		super.init();
		
		//initial setting, more apparent than gradual adjustments
		if (isNatural)
			stormTemperature = 0.0F;//getTemperatureMCToWeatherSys(temp);
		
		stormWind = 0; 
	}
	
	public boolean isStorm() {return stormStage > Stage.NORMAL.getInt();}
	public boolean isSevere() {return stormStage > Stage.THUNDER.getInt();}
	public boolean isDeadly() {return stormStage > Stage.HAIL.getInt();}
	public boolean isTornado() {return stormType == Type.LAND.getInt();}
	public boolean isCyclone() {return stormType == Type.WATER.getInt();}
	
	

	@Override
	public void readFromNBT() {
		CachedNBTTagCompound nbt = this.nbt;
		
		super.readFromNBT();
		readNBT();
		
		overrideAngle = nbt.getBoolean("angleIsOverridden");
		overrideNewAngle = nbt.getFloat("angleMovementTornadoOverride");
		overrideMotion = nbt.getBoolean("speedIsOverridden");
		overrideNewMotion = nbt.getFloat("speedMovementTornadoOverride");
		player = nbt.getString("userSpawnedFor");
	}

	@Override
	public void writeToNBT() {
		CachedNBTTagCompound nbt = this.nbt;
		
		super.writeToNBT();
		writeNBT();

		nbt.setBoolean("angleIsOverridden", overrideAngle);
		nbt.setFloat("angleMovementTornadoOverride", overrideNewAngle);
		nbt.setBoolean("speedIsOverridden", overrideMotion);
		nbt.setFloat("speedMovementTornadoOverride", overrideNewMotion);
		nbt.setString("userSpawnedFor", player);
	}
	
	//receiver method
	@Override
	public void readNBT() {
		CachedNBTTagCompound nbt = this.nbt;
		boolean testNetworkData = false;
		
		if (testNetworkData) {
			System.out.println("Received payload from server; length=" + nbt.getNewNBT().getKeySet().size());
			Iterator<String> iterator = nbt.getNewNBT().getKeySet().iterator();
			String keys = "";
			while (iterator.hasNext()) {
				keys = keys.concat(iterator.next() + "; ");
			}
			System.out.println("Received	" + keys);
		}

		super.readNBT();
		stormType = nbt.getInteger("stormType");
		stormStage = nbt.getInteger("levelCurIntensityStage");
		isRaining = nbt.getBoolean("attrib_rain");
		isSpout = nbt.getBoolean("attrib_waterSpout");
		currentTopYBlock = nbt.getInteger("currentTopYBlock");
		stormTemperature = nbt.getFloat("levelTemperature");
		stormRain = nbt.getInteger("levelWater");
		layer = nbt.getInteger("layer");
		stormStageMax = nbt.getInteger("levelStormIntensityMax");
		stormIntensity = nbt.getFloat("levelCurStagesIntensity");
		funnel_size = nbt.getFloat("levelCurStageSize");
		stormWind = nbt.getFloat("levelCurStageWind");
		stormSizeRate = nbt.getFloat("levelCurStageSizeRate");
		isDying = nbt.getBoolean("isDying");
		isDead = nbt.getBoolean("isDead");
		isViolent = nbt.getBoolean("isViolent");
		isCloudless = nbt.getBoolean("cloudlessStorm");
		isFirenado = nbt.getBoolean("isFirenado");
		
		ticksSinceLastPacketReceived = 0;//manager.getWorld().getTotalWorldTime();
		isMachineControlled = nbt.getBoolean("weatherMachineControlled");
	}
	
	//compose nbt data for packet (and serialization in future)
	@Override
	public void writeNBT() {
		CachedNBTTagCompound nbt = this.nbt;
		super.writeNBT();

		nbt.setBoolean("attrib_rain", isRaining);
		nbt.setBoolean("attrib_waterSpout", isSpout);
		nbt.setInteger("currentTopYBlock", currentTopYBlock);
		nbt.setFloat("levelTemperature", stormTemperature);
		nbt.setFloat("levelWater", stormRain);
		nbt.setInteger("layer", layer);
		nbt.setInteger("levelCurIntensityStage", stormStage);
		nbt.setFloat("levelCurStagesIntensity", stormIntensity);
		nbt.setFloat("levelStormIntensityMax", stormStageMax);
		nbt.setFloat("levelCurStageSize", funnel_size);
		nbt.setFloat("levelCurStageWind", stormWind);
		nbt.setFloat("levelCurStageSizeRate", stormSizeRate);
		nbt.setInteger("stormType", stormType);
		nbt.setBoolean("isDying", isDying);
		nbt.setBoolean("isDead", isDead);
		nbt.setBoolean("isViolent", isViolent);
		nbt.setBoolean("cloudlessStorm", isCloudless);
		nbt.setBoolean("isFirenado", isFirenado);
		nbt.setBoolean("weatherMachineControlled", isMachineControlled);
	}
	
	@SideOnly(Side.CLIENT)
	public void tickRender(float partialTick) {
		super.tickRender(partialTick);
		//TODO: consider only putting funnel in this method since its the fast part, the rest might be slow enough to only need to do per gametick

		if (!WeatherUtil.isPaused()) {
			@SuppressWarnings("unused")
			int count = 8+1;

			Iterator<Map.Entry<Integer, EntityRotFX>> it = lookupParticlesCloud.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, EntityRotFX> entry = it.next();
				EntityRotFX ent = entry.getValue();
				if (!ent.isAlive()) {
					it.remove();
				} else {
					int i = entry.getKey();
					Vec3 tryPos = null;
					double spawnRad = 120;
					double speed = 2D / (spawnRad);
					if (isSevere()) {
						speed = 50D / (spawnRad);
					}
					ent.rotationSpeedAroundCenter = (float)speed;
					if (i == 0) {
						tryPos = new Vec3(pos.xCoord, layers.get(layer), pos.zCoord);
						ent.rotationYaw = ent.rotationAroundCenter;
					} else {
						double rad = Math.toRadians(ent.rotationAroundCenter - ent.rotationSpeedAroundCenter + (ent.rotationSpeedAroundCenter * partialTick));
						double x = -Math.sin(rad) * spawnRad;
						double z = Math.cos(rad) * spawnRad;
						tryPos = new Vec3(pos.xCoord + x, layers.get(layer), pos.zCoord + z);

						double var16 = this.pos.xCoord - ent.getPosX();
						double var18 = this.pos.zCoord - ent.getPosZ();
						ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
					}
					ent.setPosition(tryPos.xCoord, tryPos.yCoord, tryPos.zCoord);
				}
			}

			count = 16*2;
			it = lookupParticlesCloudLower.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, EntityRotFX> entry = it.next();
				EntityRotFX ent = entry.getValue();
				if (!ent.isAlive()) {
					it.remove();
				} else {
					int i = entry.getKey();
					Vec3 tryPos = null;

					ent.setScale(800);

					int layerRot = i / 16;
					double spawnRad = 80;
					if (layerRot == 1) {
						spawnRad = 60;
						ent.setScale(600);
					}
					double speed = 50D / (spawnRad * 2D);

					ent.rotationSpeedAroundCenter = (float)speed;
					double rad = Math.toRadians(ent.rotationAroundCenter - ent.rotationSpeedAroundCenter + (ent.rotationSpeedAroundCenter * partialTick));
					double x = -Math.sin(rad) * spawnRad;
					double z = Math.cos(rad) * spawnRad;
					tryPos = new Vec3(pos.xCoord + x, layers.get(layer) - 20, pos.zCoord + z);

					ent.setPosition(tryPos.xCoord, tryPos.yCoord, tryPos.zCoord);

					double var16 = this.pos.xCoord - ent.getPosX();
					double var18 = this.pos.zCoord - ent.getPosZ();
					ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
					ent.rotationPitch = -20F;
				}
			}
		}
	}
	
	public void tick() {
		super.tick();
		manager.getWorld().profiler.startSection("stormObjectTick");
		//adjust posGround to be pos with the ground Y pos for convinient usage
		posGround = new Vec3(pos.xCoord, pos.yCoord, pos.zCoord);
		posGround.yCoord = currentTopYBlock;
		
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT)
		{
			manager.getWorld().profiler.startSection("clientTick");
			if (!WeatherUtil.isPaused())
			{
				ticksSinceLastPacketReceived++;
				tickClient();
				
				if (isDeadly())
					tornadoHelper.tick(manager.getWorld());

				
				if (stormStage >= Stage.SEVERE.getInt())
					if (manager.getWorld().isRemote)
						tornadoHelper.soundUpdates(true, isDeadly());

				tickMovementClient();
			}
			manager.getWorld().profiler.endSection();
		}
		else
		{
			manager.getWorld().profiler.startSection("serverTick");
			if (isCloudless)
				if (ConfigMisc.overcast_mode && manager.getWorld().isRaining()) 
					isCloudless = false;

			if (isDeadly())
				tornadoHelper.tick(manager.getWorld());

			if (stormStage >= Stage.SEVERE.getInt()) 
				if (manager.getWorld().isRemote)
					tornadoHelper.soundUpdates(true, isDeadly());
			manager.getWorld().profiler.startSection("tickMovement");
			if (!isMachineControlled)
				tickMovement();
			manager.getWorld().profiler.endSection();
			
			if (layer == 0)
				if (!isCloudless)
				{
					manager.getWorld().profiler.startSection("tickWeather");
					tickWeatherEvents();
					manager.getWorld().profiler.endStartSection("tickProgression");
					if (ConfigSimulation.simulation_enable)
						tickNewProgression();
					else
						tickProgression();
					manager.getWorld().profiler.endStartSection("tickSnowFall");
					tickSnowFall();
					manager.getWorld().profiler.endSection();
				}
			else
				//make layer 1 max size for visuals
				size = ConfigStorm.max_storm_size;		
			manager.getWorld().profiler.endSection();
		}
		
		if (layer == 0) {
			//sync X Y Z, Y gets changed below
			pos_funnel_base = new Vec3(pos.xCoord, pos.yCoord, pos.zCoord);
	
			if (stormStage > Stage.HAIL.getInt()) 
			{
				if (stormStage > Stage.STAGE0.getInt())
				{
					stormFormingStrength = 1;
					pos_funnel_base.yCoord = posGround.yCoord;
				}
				else
				{	
					//make it so storms touchdown at 0.5F intensity instead of 1 then instantly start going back up, keeps them down for a full 1F worth of intensity val
					float intensityAdj = Math.min(1F, stormIntensity * 2F);
					
					//shouldnt this just be intensityAdj?
					float val = (stormStage + intensityAdj) - Stage.STAGE0.getInt();
					stormFormingStrength = val;
					double yDiff = pos.yCoord - posGround.yCoord;
					pos_funnel_base.yCoord = pos.yCoord - (yDiff * stormFormingStrength);
				}
			}
			else
				if (stormStage == Stage.SEVERE.getInt())
				{
					stormFormingStrength = 1;
					pos_funnel_base.yCoord = posGround.yCoord;
				}
				else
				{
					stormFormingStrength = 0;
					pos_funnel_base.yCoord = pos.yCoord;
				}
		}
		manager.getWorld().profiler.endSection();
	}
	
	public void tickMovement() {
		//storm movement via wind
		float angle = getAdjustedAngle();

		if (overrideAngle)
			angle = overrideNewAngle;

		//despite overridden angle, still avoid obstacles
		//slight randomness to angle
		Random rand = new Random();
		angle += (rand.nextFloat() - rand.nextFloat()) * 0.15F;

		//avoid large obstacles
		double scanDist = 50;
		double scanX = this.pos.xCoord + (-Math.sin(Math.toRadians(angle)) * scanDist);
		double scanZ = this.pos.zCoord + (Math.cos(Math.toRadians(angle)) * scanDist);
		int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), new BlockPos(scanX, 0, scanZ)).getY();

		if (this.pos.yCoord < height)
		{
			float angleAdj = 45;
			//if (this.id % 2 == 0)
				//angleAdj = -45;
			angle += angleAdj;
		}
		
		double vecX = -Math.sin(Math.toRadians(angle));
		double vecZ = Math.cos(Math.toRadians(angle));
		float cloudSpeedAmp = 0.2F;
		float finalSpeed = getAdjustedSpeed() * cloudSpeedAmp;
		
		if (stormStage > Stage.HAIL.getInt())
			finalSpeed = 0.2F;
		else if (stormStage > Stage.NORMAL.getInt())
			finalSpeed = 0.05F;
		
		if (stormStage > Stage.HAIL.getInt())
			finalSpeed /= ((float)(stormStage-Stage.STAGE0.getInt()+1F));
		
		if (overrideMotion) finalSpeed = overrideNewMotion / ((float)(stormStage-Stage.STAGE0.getInt()+1F));
		
		if (!isMachineControlled)
		{
			motion.xCoord = vecX * finalSpeed;
			motion.zCoord = vecZ * finalSpeed;

			//actually move storm
			pos.xCoord += motion.xCoord;
			pos.zCoord += motion.zCoord;
		}
	}

	public void tickMovementClient() {
		if (!isMachineControlled)
		{
			pos.xCoord += motion.xCoord;
			pos.zCoord += motion.zCoord;
		}
	}
	
	public void tickWeatherEvents() {
		Random rand = new Random();
		World world = manager.getWorld();
		
		currentTopYBlock = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(MathHelper.floor(pos.xCoord), 0, MathHelper.floor(pos.zCoord))).getY();
		
		if (stormStage > Stage.NORMAL.getInt())
		{
			if (rand.nextInt((int)Math.max(1, ConfigStorm.lightning_bolt_1_in_x - (stormStage * 10))) == 0)
			{
				int x = (int) (pos.xCoord + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) (pos.zCoord + rand.nextInt(size) - rand.nextInt(size));
				
				if (world.isBlockLoaded(new BlockPos(x, 0, z)))
				{
					int y = world.getPrecipitationHeight(new BlockPos(x, 0, z)).getY();
					addWeatherEffectLightning(new EntityLightningBolt(world, (double)x, (double)y, (double)z), false);
				}
			}
		}
		
		//dont forget, this doesnt account for storm size, so small storms have high concentration of hail, as it grows, it appears to lessen in rate
		if (isRaining && stormStage > Stage.SEVERE.getInt() && stormType == Type.LAND.getInt())
			for (int i = 0; i < ConfigStorm.hail_stones_per_tick; i++)
			{
				int x = (int) (pos.xCoord + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) (pos.zCoord + rand.nextInt(size) - rand.nextInt(size));
				
				if (world.isBlockLoaded(new BlockPos(x, weather_layer_0, z)) && (world.getClosestPlayer(x, 50, z, 80, false) != null))
				{
					EntityIceBall hail = new EntityIceBall(world);
					
					hail.setPosition(x, layers.get(layer), z);
					world.spawnEntity(hail);
				}
			}

		trackAndExtinguishEntities();
	}

	public void trackAndExtinguishEntities() {

		if (ConfigStorm.storm_rain_extinguish_delay <= 0) return;

		if (isRaining)
		{
			//efficient caching
			if ((manager.getWorld().getTotalWorldTime()/* + (id * 20)*/) % ConfigStorm.storm_rain_extinguish_delay == 0)
			{
				stormCloseEntities.clear();
				BlockPos posBP = new BlockPos(posGround.xCoord, posGround.yCoord, posGround.zCoord);
				List<EntityLivingBase> listEnts = manager.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(posBP).grow(size));
				
				for (EntityLivingBase ent : listEnts)
					if (ent.world.canBlockSeeSky(ent.getPosition()))
						stormCloseEntities.add(ent);
			}

			for (EntityLivingBase ent : stormCloseEntities)
				ent.extinguish();
		}
	}
	
	public void tickSnowFall() {
		
		if (!ConfigSnow.Snow_PerformSnowfall) return;
		if (!isRaining) return;
		
		World world = manager.getWorld();
		int xx = 0;
		int zz = 0;
		
		for (xx = (int) (pos.xCoord - size/2); xx < pos.xCoord + size/2; xx+=16)
		{
			for (zz = (int) (pos.zCoord - size/2); zz < pos.zCoord + size/2; zz+=16)
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
					double d0 = pos.xCoord - (xx + xxx);
					double d2 = pos.zCoord - (zz + zzz);
					if ((double)MathHelper.sqrt(d0 * d0 + d2 * d2) > size)
						continue;
					
					setBlockHeight = world.getPrecipitationHeight(new BlockPos(xxx + x, 0, zzz + z)).getY();
					if (canSnowAtBody(xxx + x, setBlockHeight, zzz + z) && Blocks.SNOW.canPlaceBlockAt(world, new BlockPos(xxx + x, setBlockHeight, zzz + z)))
					{
						boolean betterBuildup = true;
						
						if (betterBuildup)
						{
							WindManager windMan = manager.windMan;
							float angle = windMan.getWindAngleForClouds();
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
		float temperature = CoroUtilCompatibility.getAdjustedTemperature(world, biomegenbase, pos);

		if ((canSnowFromCloudTemperature && stormTemperature > 0) || (!canSnowFromCloudTemperature && temperature > 0.15F))
			return false;
		else
		{
			if (par2 >= 0 && par2 < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10)
			{
				IBlockState iblockstate1 = world.getBlockState(pos);

				//TODO: incoming new way to detect if blocks can be snowed on https://github.com/MinecraftForge/MinecraftForge/pull/4569/files
				//might not require any extra work from me?
				if ((iblockstate1.getBlock().isAir(iblockstate1, world, pos) || iblockstate1.getBlock() == Blocks.SNOW_LAYER) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos))
					return true;
			}

			return false;
		}
	}
	
	public void tickNewProgression()
	{
		World world = manager.getWorld();
		if (world.getTotalWorldTime() % ConfigStorm.storm_tick_delay == 0 && ConfigStorm.storm_tick_delay > 0)
		{
			
		}
	}
	
	public void tickProgression() {
		World world = manager.getWorld();	
		
		if(manager.getWorld().getTotalWorldTime() % ConfigStorm.storm_tick_delay == 0)
		{
			if (stormStage > Stage.STAGE0.getInt())
				funnel_size = (float) Math.min(Math.pow((stormIntensity - 3.0F) * 14, 1.2) * stormSizeRate, ConfigStorm.max_storm_size);
			else if(funnel_size != 14.0F)
				funnel_size = 14.0F;
			size = (int) MathHelper.clamp(funnel_size * 1.5,250, ConfigStorm.max_storm_size);
			stormWind = 65.0F + 27.0F * (stormIntensity - 3.0F);
		}

		float tempAdjustRate = (float)ConfigStorm.temperature_adjust_rate;
		int levelWaterBuildRate = ConfigStorm.humidity_buildup_rate;
		int levelWaterSpendRate = ConfigStorm.humidity_spend_rate;
		int randomChanceOfWaterBuildFromWater = ConfigStorm.humidity_buildup_from_source_1_in_x;
		int randomChanceOfWaterBuildFromNothing = ConfigStorm.humidity_buildup_from_air_1_in_x;
		int randomChanceOfWaterBuildFromOvercastRaining = ConfigStorm.humidity_buildup_from_overcast_1_in_x;
		randomChanceOfWaterBuildFromOvercastRaining = 10;
		
		boolean isInOcean = false;
		boolean isOverWater = false;
		
		if (world.getTotalWorldTime() % ConfigStorm.storm_tick_delay == 0 && ConfigStorm.storm_tick_delay > 0)
		{	
			NBTTagCompound playerNBT = PlayerData.getPlayerNBT(player);	
			long lastStormDeadlyTime = playerNBT.getLong("lastStormDeadlyTime");
			Biome biome = world.getBiome(new BlockPos(MathHelper.floor(pos.xCoord), 0, MathHelper.floor(pos.zCoord)));
			
			//temperature scan
			if (biome != null) {
				
				isInOcean = biome.biomeName.contains("Ocean") || biome.biomeName.contains("ocean");
				
				//float biomeTempAdj = getTemperatureMCToWeatherSys(bgb.getFloatTemperature(new BlockPos(MathHelper.floor(pos.xCoord), MathHelper.floor(pos.yCoord), MathHelper.floor(pos.zCoord))));
				float biomeTempAdj = getTemperatureMCToWeatherSys(CoroUtilCompatibility.getAdjustedTemperature(manager.getWorld(), biome, new BlockPos(MathHelper.floor(pos.xCoord), MathHelper.floor(pos.yCoord), MathHelper.floor(pos.zCoord))));
				if (stormTemperature > biomeTempAdj) 
					stormTemperature -= tempAdjustRate; 
				else if (stormTemperature < biomeTempAdj)
					stormTemperature += tempAdjustRate;
			}
			
			boolean performBuildup = false;
			
			Random rand = new Random();
			
			if (!isRaining && rand.nextInt(randomChanceOfWaterBuildFromNothing) == 0) {
				performBuildup = true;
			}

			if (!isRaining && ConfigMisc.overcast_mode && manager.getWorld().isRaining() &&
					rand.nextInt(randomChanceOfWaterBuildFromOvercastRaining) == 0) {
				performBuildup = true;
			}
			
			Block blockID = world.getBlockState(new BlockPos(MathHelper.floor(pos.xCoord), currentTopYBlock-1, MathHelper.floor(pos.zCoord))).getBlock();
			if (!CoroUtilBlock.isAir(blockID)) {
				//Block block = Block.blocksList[blockID];
				if (blockID.getMaterial(blockID.getDefaultState()) instanceof MaterialLiquid) {
					isOverWater = true;
				}
			}
			
			//water scan - dont build up if raining already
			if (!performBuildup && !isRaining && rand.nextInt(randomChanceOfWaterBuildFromWater) == 0) {
				if (isOverWater) {
					performBuildup = true;
				}
				
				if (!performBuildup && biome != null && (isInOcean || biome.biomeName.contains("Swamp") || biome.biomeName.contains("Jungle") || biome.biomeName.contains("River"))) {
					performBuildup = true;
				}
			}
			
			if (performBuildup) {
				//System.out.println("RAIN BUILD TEMP OFF");
				stormRain += levelWaterBuildRate;
				Weather2.debug("building rain: " + stormRain);
			}
			
			//water values adjust when raining
			if (isRaining) {
				stormRain -= levelWaterSpendRate;
				
				//TEMP!!!
				/*System.out.println("TEMP!!!");
				levelWater = 0;*/
				
				if (stormRain < 0) stormRain = 0;
				
				if (stormRain <= 0) {
					isRaining = false;
					Weather2.debug("ending raining for: " + getUUID().toString());
				}
			} else {
				if (stormRain >= stormMaxRain) {
					if (ConfigMisc.overcast_mode) {
						if (manager.getWorld().isRaining()) {
							if (ConfigStorm.overcast_1_in_x != -1 && rand.nextInt(ConfigStorm.overcast_1_in_x) == 0) {
								isRaining = true;
								Weather2.debug("starting raining for: " + getUUID().toString());
							}
						}
					} else {
						if (ConfigStorm.storm_rains_1_in_x != -1 && rand.nextInt(ConfigStorm.storm_rains_1_in_x) == 0) {
							isRaining = true;
							Weather2.debug("starting raining for: " + getUUID().toString());
						}
					}
				}

			}
			
			//actual storm formation chance
			WeatherSystemServer wm = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
			
			boolean tryFormStorm = false;
			
			if (canBeDeadly && stormStage == Stage.NORMAL.getInt()) {
				if (ConfigStorm.enable_global_storm_rates) {
					if (ConfigStorm.storm_global_delay != -1) {
						if (wm.ticksStormFormed == 0 || wm.ticksStormFormed + ConfigStorm.storm_global_delay < world.getTotalWorldTime()) {
							tryFormStorm = true;
						}
					}
				} else {
					if (ConfigStorm.storm_per_player_delay != -1) {
						if (lastStormDeadlyTime == 0 || lastStormDeadlyTime + ConfigStorm.storm_per_player_delay < world.getTotalWorldTime()) {
							tryFormStorm = true;
						}
					}
				}
			}

			if (isMachineControlled) {
				return;
			}

			if (((ConfigMisc.overcast_mode && manager.getWorld().isRaining()) || !ConfigMisc.overcast_mode) && WeatherUtilConfig.isWeatherEnabled(wm.getDimension()) && tryFormStorm) {
				int stormFrontCollideDist = ConfigStorm.storm_collide_distance;
				int randomChanceOfCollide = ConfigStorm.storm_per_player_1_in_x;

				if (ConfigStorm.enable_global_storm_rates) {
					randomChanceOfCollide = ConfigStorm.storm_global_1_in_x;
				}

				if (isInOcean && (ConfigStorm.storm_developing_above_ocean_10_in_x > 0 && rand.nextInt(ConfigStorm.storm_developing_above_ocean_10_in_x) == 0)) {
					EntityPlayer entP = world.getPlayerEntityByName(player);

					if (entP != null) {
						initRealStorm(entP, null);
					} else {
						initRealStorm(null, null);
					}

					if (ConfigStorm.enable_global_storm_rates) {
						wm.ticksStormFormed = world.getTotalWorldTime();
					} else {
						playerNBT.setLong("lastStormDeadlyTime", world.getTotalWorldTime());
					}
				} else if (!isInOcean && ConfigStorm.storm_developing_above_land_10_in_x > 0 && rand.nextInt(ConfigStorm.storm_developing_above_land_10_in_x) == 0) {
					EntityPlayer entP = world.getPlayerEntityByName(player);

					if (entP != null) {
						initRealStorm(entP, null);
					} else {
						initRealStorm(null, null);
					}

					if (ConfigStorm.enable_global_storm_rates) {
						wm.ticksStormFormed = world.getTotalWorldTime();
					} else {
						playerNBT.setLong("lastStormDeadlyTime", world.getTotalWorldTime());
					}
				} else if (rand.nextInt(randomChanceOfCollide) == 0) {
					for (int i = 0; i < manager.getWeatherObjects().size(); i++) {
						WeatherObject wo = manager.getWeatherObjects().get(i);

						if (wo instanceof StormObject) {
							StormObject so = (StormObject) wo;



							boolean startStorm = false;

							if (so.getUUID().toString() != this.getUUID().toString() && so.stormStage <= 0 && !so.isCloudless && !so.isMachineControlled) {
								if (so.pos.distanceTo(pos) < stormFrontCollideDist) {
									if (this.stormTemperature < 0) {
										if (so.stormTemperature > 0) {
											startStorm = true;
										}
									} else if (this.stormTemperature > 0) {
										if (so.stormTemperature < 0) {
											startStorm = true;
										}
									}
								}
							}

							if (startStorm) {

								//Weather.dbg("start storm!");

								playerNBT.setLong("lastStormDeadlyTime", world.getTotalWorldTime());

								//EntityPlayer entP = manager.getWorld().getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, -1);
								EntityPlayer entP = world.getPlayerEntityByName(player);

								if (entP != null) {
									initRealStorm(entP, so);
								} else {
									initRealStorm(null, so);
									//can happen, chunkloaded emtpy overworld, let the storm do what it must without a player
									//Weather.dbg("Weather2 WARNING!!!! Failed to get a player object for new tornado, this shouldnt happen");
								}

								break;
							}
						}

					}
				}
			}
			
			if (isStorm()) {
				
				//force storms to die if its no longer raining while overcast mode is active
				if (ConfigMisc.overcast_mode) {
					if (!manager.getWorld().isRaining()) {
						isDying = true;
					}
				}
				
				//force rain on while real storm and not dying
				if (!isDying) {
					stormRain = stormMaxRain;
					isRaining = true;
				}

				//temp
				//levelWater = 0;
				//isRaining = false;
				
				if ((stormStage == Stage.SEVERE.getInt() || stormStage == Stage.HAIL.getInt()) && isOverWater) {
					if (ConfigStorm.high_wind_waterspout_10_in_x != 0 && rand.nextInt(ConfigStorm.high_wind_waterspout_10_in_x) == 0) {
						isSpout = true;
					}
				} else {
					isSpout = false;
				}
				
				float levelStormIntensityRate = 0.03F;
				
				//speed up forming and greater progression when past forming state
				if (stormStage >= Stage.STAGE0.getInt()) {
					levelStormIntensityRate *= 3;
				}
				
				if (!isDying) {
					stormIntensity += levelStormIntensityRate;
					
					if ((!ConfigStorm.disable_tornados || stormStage < Stage.STAGE0.getInt()-1))
					{
						if (stormIntensity - (stormStage - 1) > 1)
						{
							if (alwaysProgresses || stormStage < stormStageMax)
							{
								stageNext();
								Weather2.debug("storm ID: " + this.getUUID().toString() + " - growing, stage: " + stormStage);
								//mark is tropical cyclone if needed! and never unmark it!
								if (isInOcean) {
									//make it ONLY allow to change during forming stage, so it locks in
									if (stormStage == Stage.STAGE0.getInt()) {
										Weather2.debug("storm ID: " + this.getUUID().toString() + " marked as tropical cyclone!");
										stormType = Type.WATER.getInt();

										//reroll dice on ocean storm since we only just define it here
										stormStageMax = rollDiceOnMaxIntensity();
										Weather2.debug("rerolled odds for ocean storm, max stage will be: " + stormStageMax);
									}
								}
							}
						}
					}
					if (stormStage >= stormStageMax && stormIntensity - (stormStage - 1) > 1) {
						Weather2.debug("storm peaked at: " + stormStage);
						isDying = true;
					}
				}
				else
				{
					if (!neverDissipate)
					{
						if (ConfigMisc.overcast_mode && manager.getWorld().isRaining())
							stormIntensity -= levelStormIntensityRate * 0.5F;
						else
							stormIntensity -= levelStormIntensityRate * 0.2F;	
						
						if (stormIntensity - (stormStage - 1) <= 0) {
							stagePrev();
							Weather2.debug("storm ID: " + this.getUUID().toString() + " - dying, stage: " + stormStage);
							if (stormStage <= 0)
								setNoStorm();
						}
					}
				}
			} else {
				if (ConfigMisc.overcast_mode) {
					if (!manager.getWorld().isRaining()) {
						if (isRaining) {
							isRaining = false;
						}
					}
				}
			}
		}
	}
	
	public WeatherEntityConfig getWeatherEntityConfigForStorm() {
		//default spout
		WeatherEntityConfig weatherConfig = WeatherTypes.weatherEntTypes.get(0);
		if (stormStage >= Stage.STAGE5.getInt()) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(5);
		} else if (stormStage >= Stage.STAGE4.getInt()) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(4);
		} else if (stormStage >= Stage.STAGE3.getInt()) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(3);
		} else if (stormStage >= Stage.STAGE2.getInt()) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(2);
		} else if (stormStage >= Stage.STAGE1.getInt()) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(1);
		} else if (stormStage >= Stage.STAGE0.getInt()) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(0);
		}
		return weatherConfig;
	}
	
	public void updateType()
	{
		switch(stormStage)
		{
			case 0:
				type = WeatherEnum.Type.RAIN;
				break;
			case 1:
				type = WeatherEnum.Type.THUNDER;
				break;
			case 2:
				type = WeatherEnum.Type.SUPERCELL;
				break;
			case 3:
				type = WeatherEnum.Type.SUPERCELL;
				break;
			default:
				if (stormType == 1)
					type = WeatherEnum.Type.HURRICANE;
				else
					type = WeatherEnum.Type.TORNADO;
		}
	}
	
	public void stageNext() {
		stormStage += 1;
		if (ConfigStorm.storms_aim_at_player) {
			if (!isDying && stormStage == Stage.STAGE0.getInt()) {
				aimStormAtClosestOrProvidedPlayer(null);
			}
		}
		updateType();
	}
	
	public void stagePrev() {
		stormStage -= 1;
		updateType();
	}
	
	public void initRealStorm(EntityPlayer entP, StormObject stormToAbsorb) {
		//new way of storm progression
		if (stormStage < 1 || stormStage > 1)
		{
			stormStage = Stage.THUNDER.getInt();
			stormIntensity = 0.0F;
		}	
		
		if (isNatural && !ConfigSimulation.simulation_enable)
			this.stormRain = this.stormMaxRain * 2;

		if (stormStageMax < 1)
			stormStageMax = rollDiceOnMaxIntensity();
		
		stormSizeRate = Maths.random(0.75F, 1.35F);
		if (isViolent || Maths.chance(ConfigStorm.chance_for_violent_storm / 100.0D))
		{
			isViolent = true;
			stormSizeRate += Maths.random(0.25F, 1.65F);
			if (stormStageMax < 9)
				stormStageMax += 1;
		}
			
		if (stormStageMax > 3)
			Weather2.debug("New Deadly Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stormStageMax + " (EF" + (stormStageMax - 4) + ")\nIntensity Multiplier: " + stormSizeRate * 100 + "%");
		else
			Weather2.debug("New Normal Storm: \nIs Violent: " + isViolent + "\nMax Stage: " + stormStageMax + "\nIntensity Multiplier: " + stormSizeRate * 100 + "%");
		this.isRaining = true;

		if (stormToAbsorb != null) {
			Weather2.debug("stormfront collision happened between ID " + this.getUUID().toString() + " and " + stormToAbsorb.getUUID().toString());
			manager.removeStormObject(stormToAbsorb.getUUID());
			PacketWeatherObject.remove(manager.getDimension(), stormToAbsorb);
		} else
			Weather2.debug("ocean storm happened, ID " + this.getUUID().toString());
		
		if (ConfigStorm.storms_aim_at_player)
			aimStormAtClosestOrProvidedPlayer(entP);
		updateType();
	}

	public int rollDiceOnMaxIntensity() {
		if (stormType == Type.LAND.getInt()) {
			if (Maths.chance(ConfigStorm.chance_for_supercell / 100.0D)) {
				return Stage.SEVERE.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_hailing_supercell / 100.0D)) {
				return Stage.HAIL.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_ef0 / 100.0D)) {
				return Stage.STAGE0.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_ef1 / 100.0D)) {
				return Stage.STAGE1.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_ef2 / 100.0D)) {
				return  Stage.STAGE2.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_ef3 / 100.0D)) {
				return  Stage.STAGE3.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_ef4 / 100.0D)) {
				return  Stage.STAGE4.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_ef5 / 100.0D)) {
				return Stage.STAGE5.getInt();
			}
		} else if (stormType == Type.WATER.getInt()) {
			if (Maths.chance(ConfigStorm.chance_for_supercell / 100.0D)) {
				return  Stage.SEVERE.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_hailing_supercell / 100.0D)) {
				return Stage.HAIL.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_c0 / 100.0D)) {
				return Stage.STAGE0.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_c1 / 100.0D)) {
				return Stage.STAGE1.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_c2 / 100.0D)) {
				return  Stage.STAGE2.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_c3 / 100.0D)) {
				return  Stage.STAGE3.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_c4 / 100.0D)) {
				return  Stage.STAGE4.getInt();
			} else if (Maths.chance(ConfigStorm.chance_for_c5 / 100.0D)) {
				return Stage.STAGE5.getInt();
			}
		}

		return Stage.THUNDER.getInt();
	}
	
	public int rollDiceOnMaxIntensityAlt() {
		if (stormType == Type.LAND.getInt()) {
			if (stormStage < 5 && Maths.chance(ConfigStorm.chance_for_ef1 / 100.0D)) {
				return Stage.STAGE1.getInt();
			} else if (stormStage < 6 && Maths.chance(ConfigStorm.chance_for_ef2 / 100.0D)) {
				return  Stage.STAGE2.getInt();
			} else if (stormStage < 7 && Maths.chance(ConfigStorm.chance_for_ef3 / 100.0D)) {
				return  Stage.STAGE3.getInt();
			} else if (stormStage < 8 && Maths.chance(ConfigStorm.chance_for_ef4 / 100.0D)) {
				return  Stage.STAGE4.getInt();
			} else if (stormStage < 9 && Maths.chance(ConfigStorm.chance_for_ef5 / 100.0D)) {
				return Stage.STAGE5.getInt();
			}
		} else if (stormType == Type.WATER.getInt()) {
			if (stormStage < 5 && Maths.chance(ConfigStorm.chance_for_c1 / 100.0D)) {
				return Stage.STAGE1.getInt();
			} else if (stormStage < 6 && Maths.chance(ConfigStorm.chance_for_c2 / 100.0D)) {
				return  Stage.STAGE2.getInt();
			} else if (stormStage < 7 && Maths.chance(ConfigStorm.chance_for_c3 / 100.0D)) {
				return  Stage.STAGE3.getInt();
			} else if (stormStage < 8 && Maths.chance(ConfigStorm.chance_for_c4 / 100.0D)) {
				return  Stage.STAGE4.getInt();
			} else if (stormStage < 9 && Maths.chance(ConfigStorm.chance_for_c5 / 100.0D)) {
				return Stage.STAGE5.getInt();
			}
		}

		return Stage.STAGE0.getInt();
	}
	
	public void aimStormAtClosestOrProvidedPlayer(EntityPlayer entP) {
		
		if (entP == null) {
			entP = manager.getWorld().getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, -1, false);
		}
		
		if (entP != null) {
			Random rand = new Random();
			double var11 = entP.posX - pos.xCoord;
			double var15 = entP.posZ - pos.zCoord;
			float yaw = -(float)(Math.atan2(var11, var15) * 180.0D / Math.PI);
			//weather override!
			//yaw = weatherMan.wind.direction;
			int size = ConfigStorm.storm_aim_accuracy_in_angle;
			if (size > 0) {
				yaw += rand.nextInt(size) - (size / 2);
			}
			
			overrideAngle = true;
			overrideNewAngle = yaw;
			
			Weather2.debug("stormfront aimed at player " + CoroUtilEntity.getName(entP));
		}
	}
	
	//FYI rain doesnt count as storm
	public void setNoStorm() {
		Weather2.debug("storm ID: " + this.getUUID().toString() + " - ended storm event");
		stormStage = Stage.NORMAL.getInt();
		stormIntensity = 0;
		isDead = true;
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient() {

		if (isCloudless) return;

		if (particleBehaviorFog == null) {
			particleBehaviorFog = new ParticleBehaviorFog(new Vec3(pos.xCoord, pos.yCoord, pos.zCoord));
			//particleBehaviorFog.sourceEntity = this;
		} else {
			if (!Minecraft.getMinecraft().isSingleplayer() || !(Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu)) {
				particleBehaviorFog.tickUpdateList();
			}
		}
		
		EntityPlayer entP = Minecraft.getMinecraft().player;
		
		double spinSpeedMax = 0.25D;
		if (ConfigSimulation.simulation_enable)
			stormSpin = Math.min(spinSpeedMax, Math.max(stormIntensity * 0.0001F, 0.03F));
		else
			stormSpin = Math.min(spinSpeedMax, Math.max(0.015D * stormStage, 0.03D));

		
		//bonus!
		if (stormType == Type.WATER.getInt()) {
			stormSpin += 0.025D;
		}
		
		if (size == 0) size = 1;
		int delay = Math.max(1, (int)(100F / size * 1F));
		int loopSize = 1;//(int)(1 * size * 0.1F);
		
		int extraSpawning = 0;
		
		if (isSevere()) {
			loopSize += 4;
			extraSpawning = Math.max(95 * (int)((funnel_size / 100) * 2.5), 1000);
		}
		
		//adjust particle creation rate for upper tropical cyclone work
		if (stormType == Type.WATER.getInt()) {
			if (stormStage >= Stage.STAGE5.getInt()) {
				loopSize = 10;
				extraSpawning = 800;
			} else if (stormStage >= Stage.STAGE4.getInt()) {
				loopSize = 8;
				extraSpawning = 700;
			} else if (stormStage >= Stage.STAGE3.getInt()) {
				loopSize = 6;
				extraSpawning = 500; 
			} else if (stormStage >= Stage.STAGE2.getInt()) {
				loopSize = 4;
				extraSpawning = 400;
			} else {
				extraSpawning = 300;
			}
		}
		
		//Weather.dbg("size: " + size + " - delay: " + delay); 
		
		Random rand = new Random();
		
		Vec3 playerAdjPos = new Vec3(entP.posX, pos.yCoord, entP.posZ);
		double maxSpawnDistFromPlayer = 512;
		


		//maintain clouds new system


		//spawn clouds
		if (ConfigCoroUtil.optimizedCloudRendering) {

			//1 in middle, 8 around it
			int count = 8+1;

			for (int i = 0; i < count; i++) {
				if (!lookupParticlesCloud.containsKey(i)) {

					//position doesnt matter, set by renderer while its invisible still
					Vec3 tryPos = new Vec3(pos.xCoord, layers.get(layer), pos.zCoord);
					EntityRotFX particle;
					if (WeatherUtil.isAprilFoolsDay()) {
						particle = spawnFogParticle(tryPos.xCoord, tryPos.yCoord, tryPos.zCoord, 0, ParticleRegistry.chicken);
					} else {
						particle = spawnFogParticle(tryPos.xCoord, tryPos.yCoord, tryPos.zCoord, 0, ParticleRegistry.cloud256_test);
					}

					//offset starting rotation for even distribution except for middle one
					if (i != 0) {
						double rotPos = (i - 1);
						float radStart = (float) ((360D / 8D) * rotPos);
						particle.rotationAroundCenter = radStart;
					}

					lookupParticlesCloud.put(i, particle);
				}
			}

			if (isSevere()) {

				//2 layers of 16
				count = 16*2;

				for (int i = 0; i < count; i++) {
					if (!lookupParticlesCloudLower.containsKey(i)) {

						//position doesnt matter, set by renderer while its invisible still
						Vec3 tryPos = new Vec3(pos.xCoord, layers.get(layer), pos.zCoord);
						EntityRotFX particle;
						if (WeatherUtil.isAprilFoolsDay()) {
							particle = spawnFogParticle(tryPos.xCoord, tryPos.yCoord, tryPos.zCoord, 1, ParticleRegistry.chicken);
						} else {
							particle = spawnFogParticle(tryPos.xCoord, tryPos.yCoord, tryPos.zCoord, 1, ParticleRegistry.cloud256_test);
						}

						//set starting offset for even distribution
						double rotPos = i % 15;
						float radStart = (float) ((360D / 16D) * rotPos);
						particle.rotationAroundCenter = radStart;

						lookupParticlesCloudLower.put(i, particle);
					}
				}
			}
		}

		if (this.manager.getWorld().getTotalWorldTime() % (delay + (isSevere() ? ConfigParticle.funnel_particle_delay : ConfigParticle.cloud_particle_delay)) == 0) {
			for (int i = 0; i < loopSize; i++) {
				if (!ConfigCoroUtil.optimizedCloudRendering && listParticlesCloud.size() < (size + extraSpawning) / 1F) {
					double spawnRad = size;
					Vec3 tryPos = new Vec3(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), layers.get(layer), pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
						if (getAvoidAngleIfTerrainAtOrAheadOfPosition(getAdjustedAngle(), tryPos) == 0) {
							EntityRotFX particle;
							if (WeatherUtil.isAprilFoolsDay()) {
								particle = spawnFogParticle(tryPos.xCoord, tryPos.yCoord, tryPos.zCoord, 0, ParticleRegistry.chicken);
							} else {

								particle = spawnFogParticle(tryPos.xCoord, tryPos.yCoord, tryPos.zCoord, 0);
								if (isFirenado && isSevere()) {
										particle.setParticleTexture(ParticleRegistry.cloud256_fire);
										particle.setRBGColorF(1F, 1F, 1F);
								}
							}

							listParticlesCloud.add(particle);
						}
					}
				}
				
				
			}
		}
		
		//ground effects
		if (!ConfigCoroUtil.optimizedCloudRendering && stormStage >= Stage.SEVERE.getInt()) {
			for (int i = 0; i < (stormType == Type.WATER.getInt() ? 50 : 3)/*loopSize/2*/; i++) {
				if (listParticlesGround.size() < (stormType == Type.WATER.getInt() ? 600 : 150)/*size + extraSpawning*/) {
					double spawnRad = size/4*3;
					
					if (stormType == Type.WATER.getInt()) {
						spawnRad = size*2.0D;
					}
					
					//Weather.dbg("listParticlesCloud.size(): " + listParticlesCloud.size());
					
					Vec3 tryPos = new Vec3(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), posGround.yCoord, pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
						int groundY = WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(), new BlockPos((int)tryPos.xCoord, 0, (int)tryPos.zCoord)).getY();
						EntityRotFX particle;
						if (WeatherUtil.isAprilFoolsDay()) {
							particle = spawnFogParticle(tryPos.xCoord, groundY + 3, tryPos.zCoord, 0, ParticleRegistry.potato);
						} else {
							particle = spawnFogParticle(tryPos.xCoord, groundY + 3, tryPos.zCoord, 0);
						}
						
						particle.setScale(200);
						particle.rotationYaw = rand.nextInt(360);
						particle.rotationPitch = rand.nextInt(360);
						
						listParticlesGround.add(particle);
					}
				}
				
				
			}
		}
		;
		
		delay = 1;
		loopSize = 2;
		double spawnRad = funnel_size/48;
		
		if (stormStage >= Stage.STAGE1.getInt()) 
		{
			spawnRad *= 48.25D;
			sizeMaxFunnelParticles = 4000;
			loopSize *= stormStage - 3; 
		}
		
		//spawn funnel
		if (isDeadly() && stormType == 0 || (isSpout)) {
			if (ConfigParticle.funnel_particle_delay < 0) {delay = Math.abs(ConfigParticle.funnel_particle_delay) + 1; loopSize += Math.abs(ConfigParticle.funnel_particle_delay);}
			if (this.manager.getWorld().getTotalWorldTime() % (delay + ConfigParticle.funnel_particle_delay) == 0) {
				for (int i = 0; i < loopSize && listParticlesFunnel.size() <= sizeMaxFunnelParticles; i++) {
					//temp comment out
					//if (attrib_tornado_severity > 0) {
					
					//Weather.dbg("spawn");
					
					//trim!
					//if (listParticlesFunnel.size() >= sizeMaxFunnelParticles) {
					//	listParticlesFunnel.get(0).setExpired();
					//	listParticlesFunnel.remove(0);
					//}
					
					if (listParticlesFunnel.size() < sizeMaxFunnelParticles) {
						Vec3 tryPos = new Vec3(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), pos.yCoord, pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
						//int y = entP.world.getPrecipitationHeight((int)tryPos.xCoord, (int)tryPos.zCoord);
						
						if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
							EntityRotFX particle;
							if (!isFirenado/* && false*/) {
								if (WeatherUtil.isAprilFoolsDay()) {
									particle = spawnFogParticle(tryPos.xCoord, pos_funnel_base.yCoord, tryPos.zCoord, 1, ParticleRegistry.potato);
								} else {
									particle = spawnFogParticle(tryPos.xCoord, pos_funnel_base.yCoord, tryPos.zCoord, 1);
								}
							} else {
								particle = spawnFogParticle(tryPos.xCoord, pos_funnel_base.yCoord, tryPos.zCoord, 1, ParticleRegistry.cloud256_fire);

							}

							
							//move these to a damn profile damnit!
							particle.setMaxAge(150 + ((stormStage-1) * 100) + rand.nextInt(100));
							
							float baseBright = 0.3F;
							float randFloat = (rand.nextFloat() * 0.6F);
							
							particle.rotationYaw = rand.nextInt(360);
							
							float finalBright = Math.min(1F, baseBright+randFloat);
							
							//highwind aka spout in this current code location
							if (stormStage == Stage.SEVERE.getInt()) {
								particle.setScale(150);
								particle.setRBGColorF(finalBright-0.2F, finalBright-0.2F, finalBright);
							} else {
								particle.setScale(250);
								particle.setRBGColorF(finalBright, finalBright, finalBright);
							}

							if (isFirenado) {
								particle.setRBGColorF(1F, 1F, 1F);
								particle.setScale(particle.getScale() * 0.7F);
							}
							
							
							listParticlesFunnel.add(particle);
							
							//System.out.println(listParticlesFunnel.size());
						}
					} else {
						//Weather.dbg("particles maxed");
					}
				}
			}
		}
		
		for (int i = 0; i < listParticlesFunnel.size(); i++) {
			EntityRotFX ent = listParticlesFunnel.get(i);
			//System.out.println(ent.getPosY());
			if (!ent.isAlive()) {
				listParticlesFunnel.remove(ent);
			} else if (ent.getPosY() > pos.yCoord) {
				ent.setExpired();
				listParticlesFunnel.remove(ent);
				//System.out.println("asd");
			} else {
				 double var16 = this.pos.xCoord - ent.getPosX();
				 double var18 = this.pos.zCoord - ent.getPosZ();
				 ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
				 ent.rotationYaw += ent.getEntityId() % 90;
				 ent.rotationPitch = -30F;
				 
				 //fade spout blue to grey
				 if (stormStage == Stage.SEVERE.getInt()) {
					 int fadingDistStart = 30;
					 if (ent.getPosY() > posGround.yCoord + fadingDistStart) {
						 float maxVal = ent.getBlueColorF();
						 float fadeRate = 0.002F;
						 ent.setRBGColorF(Math.min(maxVal, ent.getRedColorF()+fadeRate), Math.min(maxVal, ent.getGreenColorF()+fadeRate), maxVal);
					 }
				 }
				 
				 spinEntity(ent);
			}
		}
		
		for (int i = 0; i < listParticlesCloud.size(); i++) {
			EntityRotFX ent = listParticlesCloud.get(i);
			if (!ent.isAlive()) {
				listParticlesCloud.remove(ent);
			} else {
				//ent.posX = pos.xCoord + i*10;
				/*float radius = 50 + (i/1F);
				float posX = (float) Math.sin(ent.getEntityId());
				float posZ = (float) Math.cos(ent.getEntityId());
				ent.setPosition(pos.xCoord + posX*radius, ent.posY, pos.zCoord + posZ*radius);*/
				
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
				
				double curDist = 10D;//ent.getDistance(pos.xCoord, ent.getPosY(), pos.zCoord);

				float dropDownRange = 0.25F;
				float extraDropCalc = 0;
				
				if (curDist < 200 && ent.getEntityId() % 20 < 5) {
					//cyclone and hurricane dropdown modifications here
					extraDropCalc = ((ent.getEntityId() % 20) * dropDownRange);
					if (isDeadly() && stormType == 1) {
						extraDropCalc = ((ent.getEntityId() % 20) * dropDownRange * 28F);
						//Weather.dbg("extraDropCalc: " + extraDropCalc);
					}
				}
				
				
				
				if (isSevere()) {
					double speed = stormSpin + (rand.nextDouble() * 0.02D);
					double distt = Math.max(ConfigStorm.max_storm_size, funnel_size + 20.0D);//300D;
					
					if (stormType == 1)
						distt *= 2.0D;
					
					double vecX = ent.getPosX() - pos.xCoord;
					double vecZ = ent.getPosZ() - pos.zCoord;
					float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
					//System.out.println("angle: " + angle);
					
					//fix speed causing inner part of formation to have a gap
					angle += speed * 50D;
					//angle += 20;
					
					angle -= (ent.getEntityId() % 10) * 3D;
					
					//random addition
					angle += rand.nextInt(10) - rand.nextInt(10);
					
					if (curDist > distt) {
						//System.out.println("curving");
						angle += 40;
						//speed = 1D;
					}
					
					//keep some near always - this is the lower formation part
					if (ent.getEntityId() % 40 < 5) {
						if (stormStage >= Stage.STAGE0.getInt()) {
							if (stormType == Type.WATER.getInt()) {
								angle += 40 + ((ent.getEntityId() % 5) * 4);
								if (curDist > 150 + ((stormStage-Stage.STAGE0.getInt()+1) * 30)) {
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
						
						double var16 = this.pos.xCoord - ent.getPosX();
						double var18 = this.pos.zCoord - ent.getPosZ();
						ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
						ent.rotationPitch = -20F - (ent.getEntityId() % 10);
					}
					
					if (curSpeed < speed * 20D) {
						ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
						ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
					}
				} else {
					float cloudMoveAmp = 0.2F * (1 + layer);
					
					float speed = getAdjustedSpeed() * cloudMoveAmp;
					float angle = getAdjustedAngle();

					//TODO: prevent new particles spawning inside or near solid blocks

					if ((manager.getWorld().getTotalWorldTime()/*+this.id*/) % 40 == 0) {
						ent.avoidTerrainAngle = getAvoidAngleIfTerrainAtOrAheadOfPosition(angle, ent.getPos());
					}

					angle += ent.avoidTerrainAngle;

					if (ent.avoidTerrainAngle != 0) {
						/*float angleAdj = 90;
						if (this.ID % 2 == 0) {
							angleAdj = -90;
						}
						angle += angleAdj;*/

						speed *= 0.5D;
					}
					
					dropDownRange = 5;
					if (/*curDist < 200 && */ent.getEntityId() % 20 < 5) {
						extraDropCalc = ((ent.getEntityId() % 20) * dropDownRange);
					}
					
					if (curSpeed < speed * 1D) {
						ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
						ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
					}
				}
				
				if (Math.abs(ent.getPosY() - (pos.yCoord - extraDropCalc)) > 2F) {
					if (ent.getPosY() < pos.yCoord - extraDropCalc) {
						ent.setMotionY(ent.getMotionY() + 0.1D);
					} else {
						ent.setMotionY(ent.getMotionY() - 0.1D);
					}
				}
				
				float dropDownSpeedMax = 0.5F;
				
				if (stormType == 1 || stormStage > 8) {
					dropDownSpeedMax = 1.9F;
				}
				
				if (ent.getMotionY() < -dropDownSpeedMax) {
					ent.setMotionY(-dropDownSpeedMax);
				}
				
				if (ent.getMotionY() > dropDownSpeedMax) {
					ent.setMotionY(dropDownSpeedMax);
				}
				
				//double distToGround = ent.world.getHeightValue((int)pos.xCoord, (int)pos.zCoord);
				
				//ent.setPosition(ent.posX, pos.yCoord, ent.posZ);
			}
			/*if (ent.getAge() > 300) {
				ent.setDead();
				listParticles.remove(ent);
			}*/
		}
		
		for (int i = 0; i < listParticlesGround.size() && stormType == Type.WATER.getInt(); i++) {
			EntityRotFX ent = listParticlesGround.get(i);
			
			double curDist = ent.getDistance(pos.xCoord, ent.getPosY(), pos.zCoord);
			
			if (!ent.isAlive()) {
				listParticlesGround.remove(ent);
			} else {
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
				double speed = Math.max(0.2F, 5F * stormSpin) + (rand.nextDouble() * 0.01D);
				double vecX = ent.getPosX() - pos.xCoord;
				double vecZ = ent.getPosZ() - pos.zCoord;
				float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
				
				angle += 85;
				
				int maxParticleSize = 60;
				
				if (stormType == Type.WATER.getInt()) {
					maxParticleSize = 150;
					speed /= 5D;
				}
				
				ent.setScale((float) Math.min(maxParticleSize, curDist * 2F));
				
				if (curDist < 20) {
					ent.setExpired();
				}

				//double var16 = this.pos.xCoord - ent.getPosX();
				//double var18 = this.pos.zCoord - ent.getPosZ();
				//ent.rotationYaw += 5;//(float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
				//ent.rotationPitch = 0;//-20F - (ent.getEntityId() % 10);
				
				if (curSpeed < speed * 20D) {
					ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
					ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
				}
			}
		}
		
		//System.out.println("size: " + listParticlesCloud.size());
	}
	
	public float getAdjustedSpeed() {
		return manager.windMan.getWindSpeedForClouds();
	}
	
	public float getAdjustedAngle() {
		float angle = manager.windMan.getWindAngleForClouds();
		
		float angleAdjust = Math.max(10, Math.min(45, 45F * stormTemperature * 0.2F));
		float targetYaw = 0;
		
		//coldfronts go south to 0, warmfronts go north to 180
		if (stormTemperature > 0) {
			//Weather.dbg("warmer!");
			targetYaw = 180;
		} else {
			//Weather.dbg("colder!");
			targetYaw = 0;
		}
		
		float bestMove = MathHelper.wrapDegrees(targetYaw - angle);
		
		if (Math.abs(bestMove) < 180/* - (angleAdjust * 2)*/) {
			if (bestMove > 0) angle -= angleAdjust;
			if (bestMove < 0) angle += angleAdjust;
		}
		
		//Weather.dbg("ID: " + ID + " - " + manager.windMan.getWindAngleForClouds() + " - final angle: " + angle);
		
		return angle;
	}

	public float getAvoidAngleIfTerrainAtOrAheadOfPosition(float angle, Vec3 pos) {
		double scanDistMax = 120;
		for (int scanAngle = -20; scanAngle < 20; scanAngle += 10) {
			for (double scanDistRange = 20; scanDistRange < scanDistMax; scanDistRange += 10) {
				double scanX = pos.xCoord + (-Math.sin(Math.toRadians(angle + scanAngle)) * scanDistRange);
				double scanZ = pos.zCoord + (Math.cos(Math.toRadians(angle + scanAngle)) * scanDistRange);

				int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), new BlockPos(scanX, 0, scanZ)).getY();

				if (pos.yCoord < height) {
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
	
	public void spinEntity(Object entity1) {
		
		StormObject entT = this;
		StormObject entity = this;
		WeatherEntityConfig conf = getWeatherEntityConfigForStorm();//WeatherTypes.weatherEntTypes.get(curWeatherType);
		
		Random rand = new Random();
		
		/*if (entity instanceof EntTornado) {
			entT = (EntTornado) entity;
		}*/
	
		boolean forTornado = true;//entT != null;
		
		World world = CoroUtilEntOrParticle.getWorld(entity1);
		long worldTime = world.getTotalWorldTime();
		
		Entity ent = null;
		if (entity1 instanceof Entity) {
			ent = (Entity) entity1;
		}
		
		//ConfigTornado.Storm_Tornado_height;
		double radius = 10D;
		double scale = conf.tornadoWidthScale;
		double d1 = entity.pos.xCoord - CoroUtilEntOrParticle.getPosX(entity1);
		double d2 = entity.pos.zCoord - CoroUtilEntOrParticle.getPosZ(entity1);
		
		if (conf.type == WeatherEntityConfig.TYPE_SPOUT) {
			float range = 30F * (float) Math.sin((Math.toRadians(((worldTime * 0.5F)/* + (id * 50)*/) % 360)));
			float heightPercent = (float) (1F - ((CoroUtilEntOrParticle.getPosY(entity1) - posGround.yCoord) / (pos.yCoord - posGround.yCoord)));
			float posOffsetX = (float) Math.sin((Math.toRadians(heightPercent * 360F)));
			float posOffsetZ = (float) -Math.cos((Math.toRadians(heightPercent * 360F)));
			//Weather.dbg("posOffset: " + posOffset);
			//d1 += 50F*heightPercent*posOffset;
			d1 += range*posOffsetX;
			d2 += range*posOffsetZ;
		}
		
		float f = (float)((Math.atan2(d2, d1) * 180D) / Math.PI) - 90F;
		float f1;

		for (f1 = f; f1 < -180F; f1 += 360F) { }

		for (; f1 >= 180F; f1 -= 360F) { }

		double distY = entity.pos.yCoord - CoroUtilEntOrParticle.getPosY(entity1);
		double distXZ = Math.sqrt(Math.abs(d1)) + Math.sqrt(Math.abs(d2));

		if (CoroUtilEntOrParticle.getPosY(entity1) - entity.pos.yCoord < 0.0D)
		{
			distY = 1.0D;
		}
		else
		{
			distY = CoroUtilEntOrParticle.getPosY(entity1) - entity.pos.yCoord;
		}

		if (distY > maxHeight)
		{
			distY = maxHeight;
		}

		float weight = WeatherUtilEntity.getWeight(entity1, forTornado);
		double grab = (10D / weight)/* / ((distY / maxHeight) * 1D)*/ * ((Math.abs((maxHeight - distY)) / maxHeight));
		float pullY = 0.0F;

		//some random y pull
		if (rand.nextInt(5) != 0)
		{
			//pullY = 0.035F;
		}

		if (distXZ > 5D)
		{
			grab = grab * (radius / distXZ);
		}
		
		//Weather.dbg("TEMP!!!!");
		//WeatherTypes.initWeatherTypes();

		pullY += (float)(conf.tornadoLiftRate / (weight / 2F)/* * (Math.abs(radius - distXZ) / radius)*/);
		
		
		if (entity1 instanceof EntityPlayer)
		{
			double adjPull = 0.2D / ((weight * ((distXZ + 1D) / radius)));
			/*if (!entity1.onGround) {
				adjPull /= (((float)(((double)playerInAirTime+1D) / 200D)) * 15D);
			}*/
			pullY += adjPull;
			//0.2D / ((getWeight(entity1) * ((distXZ+1D) / radius)) * (((distY) / maxHeight)) * 3D);
			//grab = grab + (10D * ((distY / maxHeight) * 1D));
			double adjGrab = (10D * (((float)(((double)WeatherUtilEntity.playerInAirTime + 1D) / 400D))));

			if (adjGrab > 50)
			{
				adjGrab = 50D;
			}
			
			if (adjGrab < -50)
			{
				adjGrab = -50D;
			}

			grab = grab - adjGrab;

			if (CoroUtilEntOrParticle.getMotionY(entity1) > -0.8)
			{
				//System.out.println(entity1.motionY);
				ent.fallDistance = 0F;
			}

			
		}
		else if (entity1 instanceof EntityLivingBase)
		{
			double adjPull = 0.005D / ((weight * ((distXZ + 1D) / radius)));
			/*if (!entity1.onGround) {
				adjPull /= (((float)(((double)playerInAirTime+1D) / 200D)) * 15D);
			}*/
			pullY += adjPull;
			//0.2D / ((getWeight(entity1) * ((distXZ+1D) / radius)) * (((distY) / maxHeight)) * 3D);
			//grab = grab + (10D * ((distY / maxHeight) * 1D));
			int airTime = ent.getEntityData().getInteger("timeInAir");
			double adjGrab = (10D * (((float)(((double)(airTime) + 1D) / 400D))));

			if (adjGrab > 50)
			{
				adjGrab = 50D;
			}
			
			if (adjGrab < -50)
			{
				adjGrab = -50D;
			}

			grab = grab - adjGrab;

			if (ent.motionY > -1.5)
			{
				ent.fallDistance = 0F;
			}
			
			if (ent.motionY > 0.3F) ent.motionY = 0.3F;

			if (forTornado) ent.onGround = false;

			//its always raining during these, might as well extinguish them
			ent.extinguish();

			//System.out.println(adjPull);
		}
		
		
		grab += conf.relTornadoSize;
		
		double profileAngle = Math.max(1, (75D + grab - (10D * scale)));
		
		f1 = (float)((double)f1 + profileAngle);
		
		if (entT != null) {
			
			if (entT.scale != 1F) f1 += 20 - (20 * entT.scale);
		}
		
		float f3 = (float)Math.cos(-f1 * 0.01745329F - (float)Math.PI);
		float f4 = (float)Math.sin(-f1 * 0.01745329F - (float)Math.PI);
		float f5 = conf.tornadoPullRate * 1;
		
		if (entT != null) {
			if (entT.scale != 1F) f5 *= entT.scale * 1.2F;
		}

		if (entity1 instanceof EntityLivingBase)
		{
			f5 /= (WeatherUtilEntity.getWeight(entity1, forTornado) * ((distXZ + 1D) / radius));
		}
		
		//if player and not spout
		if (entity1 instanceof EntityPlayer && conf.type != 0) {
			//System.out.println("grab: " + f5);
			if (ent.onGround) {
				f5 *= 10.5F;
			} else {
				f5 *= 5F;
			}
			//if (entity1.world.rand.nextInt(2) == 0) entity1.onGround = false;
		} else if (entity1 instanceof EntityLivingBase && conf.type != 0) {
			f5 *= 1.5F;
		}

		if (conf.type == WeatherEntityConfig.TYPE_SPOUT && entity1 instanceof EntityLivingBase) {
			f5 *= 0.3F;
		}
		
		float moveX = f3 * f5;
		float moveZ = f4 * f5;
		//tornado strength changes
		float str = 1F;

		/*if (entity instanceof EntTornado)
		{
			str = ((EntTornado)entity).strength;
		}*/
		
		str = strength * 1.25f;
		
		if	(entity1 instanceof EntityLivingBase)
		{
			if (conf.type == WeatherEntityConfig.TYPE_SPOUT) str *= 0.3F;
			
		}
		if (stormType == Type.WATER.getInt()) str *= 0.25F;
		pullY *= str / 100F;
		
		if (entT != null) {
			if (entT.scale != 1F) {
				pullY *= entT.scale * 1.0F;
				pullY += 0.002F;
			}
		}
		
		//prevent double+ pull on entities
		if (entity1 instanceof Entity) {
			long lastPullTime = ent.getEntityData().getLong("lastPullTime");
			if (lastPullTime == worldTime)
				pullY = 0;
			ent.getEntityData().setLong("lastPullTime", worldTime);
		}
		
		setVel(entity1, -moveX, pullY, moveZ);
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
	public EntityRotFX spawnFogParticle(double x, double y, double z, int parRenderOrder) {
		return spawnFogParticle(x, y, z, parRenderOrder, ParticleRegistry.cloud256);
	}
	
	@SideOnly(Side.CLIENT)
	public EntityRotFX spawnFogParticle(double x, double y, double z, int parRenderOrder, TextureAtlasSprite tex) {
		double speed = 0D;
		Random rand = new Random();
		EntityRotFX entityfx = particleBehaviorFog.spawnNewParticleIconFX(Minecraft.getMinecraft().world, tex, x, y, z, (rand.nextDouble() - rand.nextDouble()) * speed, 0.0D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed, parRenderOrder);
		particleBehaviorFog.initParticle(entityfx);
		
		entityfx.setCanCollide(false);
		entityfx.callUpdatePB = false;
		
		boolean debug = false;
		
		if (stormStage == Stage.NORMAL.getInt())
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
		
		float randFloat = (rand.nextFloat() * 0.6F);
		if (ConfigCoroUtil.optimizedCloudRendering)
			randFloat = (rand.nextFloat() * 0.4F);
		
		float baseBright = 0.7F;
		
		if (stormStage > Stage.NORMAL.getInt())
			baseBright = 0.2F;
		else if (isRaining)
			baseBright = 0.2F;
		else if (((WeatherSystemClient)manager).weatherID > 0)
			baseBright = 0.2F;
		else
			baseBright -= Math.min(1F, stormRain / stormMaxRain) * 0.6F;;
		
		float finalBright = Math.min(1F, baseBright+randFloat);

		entityfx.setRBGColorF(finalBright, finalBright, finalBright);
		
		//DEBUG
		if (debug)
			if (stormTemperature < 0)
				entityfx.setRBGColorF(0, 0, finalBright);
			else if (stormTemperature > 0)
				entityfx.setRBGColorF(finalBright, 0, 0);
		
		ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
		particleBehaviorFog.particles.add(entityfx);
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
	public void cleanupClient() {
		super.cleanupClient();
		listParticlesCloud.clear();
		listParticlesFunnel.clear();
		if (particleBehaviorFog != null && particleBehaviorFog.particles != null) particleBehaviorFog.particles.clear();
			particleBehaviorFog = null;
	}
	
	public float getTemperatureMCToWeatherSys(float parOrigVal) {
		//-0.7 to make 0 be the middle average
		parOrigVal -= 0.7;
		//multiply by 2 for an increased difference, for more to work with
		parOrigVal *= 2F;
		return parOrigVal;
	}
	
	public void addWeatherEffectLightning(EntityLightningBolt parEnt, boolean custom) {
		manager.getWorld().weatherEffects.add(parEnt);
		PacketLightning.send(manager.getDimension(), parEnt, custom);
	}
	
	@Override
	public int getNetRate() {
		if (stormStage >= StormObject.Stage.SEVERE.getInt()) {
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

	@Override
	public void setStage(int stage)
	{
		stormStage = stage;
		updateType();
	}

	@Override
	public boolean isRaining()
	{
		return isRaining;
	}

	@Override
	public int getStage() {
		return stormStage;
	}

	@Override
	public int getCurrentLayer() {
		return layer;
	}
}
