package net.mrbt0907.weather2.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import CoroUtil.api.weather.IWindHandler;
import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.util.CoroUtilEntOrParticle;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviors;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleTexExtraRender;
import extendedrenderer.particle.entity.ParticleTexFX;
import extendedrenderer.render.RotatingParticleManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WindReader;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.client.entity.particle.EntityWaterfallFX;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.BlockSESnapshot;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilConfig;
import net.mrbt0907.weather2.util.WeatherUtilParticle;
import net.mrbt0907.weather2.util.WeatherUtilSound;
import net.mrbt0907.weather2.util.Maths.Vec;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WindManager;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class NewSceneEnhancer implements Runnable
{
	/**The instance of the scene enhancer*/
	private static final NewSceneEnhancer INSTANCE = new NewSceneEnhancer();
	private final List<BlockPos> RANDOM_POS;
	
	//----- Internal Variables -----\\
	private volatile boolean run = true;
	private int errors = 0, errorsThreaded = 0;
	
	
	//----- Local Variables -----\\
	protected final Minecraft MC;
	/**The cached result of a weather object if it exists*/
	protected volatile WeatherObject cachedSystem;
	/**The cached result of a weather object's distance to the player*/
	protected volatile double cachedSystemDistance;
	protected volatile float cachedWindSpeed, cachedWindDirection;
	protected long ticksExisted, ticksThreadExisted;
	/**Used to detect if the client is in the world to initialize the scene enhancer*/
	protected volatile boolean inGame;
	
	//----- External Information -----\\
	/**Hopefully a thread safe list which cannot be written to if canSpawnParticle is false*/
	public final List<BlockSESnapshot> queue = new ArrayList<BlockSESnapshot>();
	/**Unknown*/
	public final ParticleBehaviors behavior;
	public float rain, rainTarget;
	public float overcast, overcastTarget;
	/**Used to smoothen fog transitions*/
	public float fogMult;
	/**Determines close the fog will be to the player*/
	public boolean enableFog;
	/**Determines how thick the fog will be at 1.0 fogMult*/
	public float fogDensity;
	/**Determines the red color for fog*/
	public float fogRed, fogRedTarget;
	/**Determines the green color for fog*/
	public float fogGreen, fogGreenTarget;
	/**Determines the blue color for fog*/
	public float fogBlue, fogBlueTarget;
	/**Determines how far the sky box should be from the player*/
	public float renderDistance;
	
	NewSceneEnhancer()
	{
		MC = FMLClientHandler.instance().getClient();
		behavior = new ParticleBehaviors(null);
		RANDOM_POS = new ArrayList<BlockPos>();
		RANDOM_POS.add(new BlockPos(0, -1, 0));
		RANDOM_POS.add(new BlockPos(1, 0, 0));
		RANDOM_POS.add(new BlockPos(-1, 0, 0));
		RANDOM_POS.add(new BlockPos(0, 0, 1));
		RANDOM_POS.add(new BlockPos(0, 0, -1));
	}
	
	public static NewSceneEnhancer instance()
	{
		return INSTANCE;
	}
	
	//----- Threaded Methods -----\\
	/**Ran on the scene enhancer thread to deal with computationally heavy tasks<br>
	 *- Find valid particle locations<br>
	 *- Find valid sound locations<br>
	 *- Cache requested fog color<br>
	 *- Cache requested precipitation values<br>
	 *- Cache storm results*/
	protected void tickThread()
	{
		if (MC.world != null && MC.player != null && ClientTickHandler.weatherManager != null && WeatherUtilConfig.isEffectsEnabled(MC.world.provider.getDimension()))
		{
			Vec3 playerPos = new Vec3(MC.player.posX, MC.player.posY, MC.player.posZ);
			Vec playerPos2D = new Vec(MC.player.posX, MC.player.posZ);
			
			if (ticksThreadExisted % 2L == 0L)
				cachedSystem = ClientTickHandler.weatherManager.getClosestWeather(playerPos, renderDistance);
			
			if (cachedSystem != null)
				cachedSystemDistance = cachedSystem.pos.distance(playerPos2D);
			else if (cachedSystemDistance >= 0.0D)
				cachedSystemDistance = -1.0F;
			
			cachedWindDirection = WindReader.getWindAngle(MC.world, playerPos);
			cachedWindSpeed = WindReader.getWindSpeed(MC.world, playerPos);
			
			tickQueuePrecipitation();
			tickQueueFog();
			tickQueueParticles();
			tickQueueSounds();
		}
	}
	
	/**Finds if fog needs to be rendered and sets the target fog if needed*/
	protected void tickQueueFog()
	{
		if (cachedSystem != null)
		{
			float max;
			if (rainTarget > 0.0F)
			{
				max = 0.2F;
				fogDensity = Math.max((rain - 0.31F) / 0.69F, 0.0F) * max * (float) ConfigParticle.fog_mult;
				return;
			}
		}
		fogDensity = 0.0F;
	}
	
	/**Finds if precipitation needs to be rendered and sets the target rain if needed*/
	protected void tickQueuePrecipitation()
	{
		if (cachedSystem != null && cachedSystem instanceof IWeatherRain)
		{
			IWeatherRain system = (IWeatherRain) cachedSystem;
			float size = cachedSystem.size * 0.45F;
			
			if (system.hasDownfall())
			{
				overcastTarget = 1.0F - (float) MathHelper.clamp((cachedSystemDistance - size) / cachedSystem.size, 0.0F, 1.0F);
				rainTarget = ConfigParticle.enable_vanilla_rain || ConfigParticle.precipitation_particle_rate <= 0.1F ? 0.0F : Math.min((system.getDownfall() - IWeatherRain.MINIMUM_DRIZZLE) * overcast * 0.0034F, 1.0F);
				
				if (WeatherUtil.getTemperature(MC.world, MC.player.getPosition()) < 0.0F)
					rainTarget = -rainTarget;
				
				MC.world.getWorldInfo().setRaining(true);
				MC.world.getWorldInfo().setThundering(true);
				return;
			}
		}

		if (ClientTickHandler.clientConfigData.overcastMode && ClientTickHandler.weatherManager.weatherID >= 1)
		{
			overcastTarget = (float) ConfigStorm.min_overcast_rain;
			rainTarget = (float) ConfigStorm.min_overcast_rain;
			MC.world.getWorldInfo().setRaining(true);
			MC.world.getWorldInfo().setThundering(true);
			return;
		}
		
		overcastTarget = 0.0F;
		rainTarget = 0.0F;
		MC.world.getWorldInfo().setRaining(false);
		MC.world.getWorldInfo().setThundering(false);
	}
	
	/**Finds block positions of where particles can spawn and caches the results*/
	protected void tickQueueParticles()
	{
		if (ticksThreadExisted % 10L == 0L)
		{
			BlockPos pos, neighborPos;
			IBlockState state;
			Block block;
			Material material;
			int meta;
			int particleCount;
			int areaWidth = 20, areaHeight = (int) (areaWidth * 0.5F);
			int posX = (int) MC.player.posX, posY = (int) MC.player.posY, posZ = (int) MC.player.posZ;
			float windMult = Math.min(cachedWindSpeed + 0.1F, 1.0F);
			List<BlockSESnapshot> snapshots = new ArrayList<BlockSESnapshot>();
			
			if (ConfigParticle.enable_falling_leaves || ConfigParticle.enable_waterfall_splash)
			{
				particleCount = (int) (30 * windMult * ConfigParticle.ambient_particle_rate) / (MC.gameSettings.particleSetting + 1);
				for (int x = posX - areaWidth; x < posX + areaWidth; x++)
					for (int y = posY - areaHeight; y < posY + areaHeight; y++)
						for (int z = posZ - areaWidth; z < posZ + areaWidth; z++)
						{
							state = getBlockState(x, y, z);
							block = state.getBlock();
							if (block.equals(Blocks.AIR)) continue;
							
							pos = new BlockPos(x, y, z);
							neighborPos = getRandomNeighbor(pos);
							if (neighborPos == null) continue;
							
							material = state.getMaterial();
							meta = block.getMetaFromState(state);
								
							if (ConfigParticle.enable_falling_leaves && (material.equals(Material.LEAVES) || material.equals(Material.VINE) || material.equals(Material.PLANTS)))
								snapshots.add(new BlockSESnapshot(state, neighborPos, 0));
						}
				
				//queue.addAll(snapshots);
			}
				
		}
	}
	
	/**Finds block positions of where sounds can spawn and caches the results*/
	protected void tickQueueSounds()
	{
		
	}
	
	//----- Non Threaded Methods -----\\
	public void tickRender(RenderTickEvent event)
	{
		if (event.phase.equals(Phase.START) && MC.world != null)
		{
			MC.world.setRainStrength(rain);
			MC.world.setThunderStrength(overcast);
		}
	}
	
	/**Ran every game tick to update values based on given variables*/
	protected void tickNonThread()
	{
		if (inGame && MC.world == null)
		{
			inGame = false;
			reset();
		}
		else if (!inGame && MC.world != null && ClientTickHandler.weatherManager != null)
		{
			inGame = true;
			Weather2.debug("Scene Enhancer is online!");
		}
		
		if (inGame)
		{
			MC.profiler.startSection("tickSceneEnhancer");
			if (!MC.isGamePaused())
			{
				if (cachedSystem != null && cachedSystem instanceof StormObject)
				{
					StormObject storm = (StormObject) cachedSystem;
					float strength = 1.0F - (float) Math.min((cachedSystemDistance - storm.funnelSize) / (storm.funnelSize + 64.0F), 1.0F);
					
					if (storm.type.equals(Type.TORNADO) && strength > 0.0F)
						shakeCamera(0.5F * strength);
				}
				MC.profiler.startSection("tickPrecipitation");
				tickPrecipitation();
				MC.profiler.endStartSection("tickAmbiantParticles");
				tickAmbiance();
				MC.profiler.endStartSection("tickAmbiantSounds");
				tickSounds();
				MC.profiler.endSection();
			}
			MC.profiler.endSection();
		}
		
	}
	
	/**Smoothly adjusts precipitation values based on the rain target*/
	protected void tickPrecipitation()
	{
		Biome biome = MC.world.getBiome(MC.player.getPosition());
		float rate = 0.0005F * Math.abs((float) ConfigParticle.rain_change_mult);
		
		if (rainTarget < 0.0F && rain > 0.0F || rainTarget >= 0.0F && rain < 0.0F)
			rain = -rain;
		
		if (rain != rainTarget)
			rain = Maths.adjust(rain, rainTarget, rate);
		
		if (overcast != overcastTarget)
			overcast = Maths.adjust(overcast, overcastTarget, rate);
		
		if (!ConfigParticle.use_vanilla_rain_and_thunder && rain > 0.0F && biome != null && (biome.canRain() || biome.getEnableSnow()))
		{
			ParticleTexFX particle;
			BlockPos pos, posPrecip;
			boolean snowing = rain < 0.0F;
			int particleCount = (int) Math.abs(rain * 15.0F * ConfigParticle.precipitation_particle_rate),
				particleCountSplash, particleCountSheet;
			int spawnArea = 20;
			
			if (particleCount > 200)
				particleCount = 200;
			
			particleCount += 5;
			
			particleCountSheet = ConfigParticle.enable_heavy_precipitation && rain > 0.5F ? (int) (particleCount * 0.2F) : 0;
			particleCountSplash = ConfigParticle.enable_precipitation_splash ? (int)(particleCount * 4) : 0;
			
			if (snowing)
			{
				
			}
			else
			{
				int type = rain > 0.6F ? 2 : rain > 0.3F ? 1 : 0; 
				for (int i = 0; i < particleCount; i++)
				{
					pos = new BlockPos(MC.player.posX + MC.world.rand.nextInt(spawnArea) - MC.world.rand.nextInt(spawnArea), MC.player.posY - 5 + MC.world.rand.nextInt(25), MC.player.posZ + MC.world.rand.nextInt(spawnArea) - MC.world.rand.nextInt(spawnArea));
					posPrecip = MC.world.getPrecipitationHeight(pos);
					if (posPrecip.getY() <= pos.getY())
					{
						particle = new ParticleTexExtraRender(MC.world, pos.getX(), pos.getY(), pos.getZ(), 0D, 0D, 0D, type == 2 ? net.mrbt0907.weather2.registry.ParticleRegistry.rainHeavy : type == 1 ? ParticleRegistry.rain_white : net.mrbt0907.weather2.registry.ParticleRegistry.rainLight);
						particle.setKillWhenUnderTopmostBlock(true);
						particle.setCanCollide(false);
						particle.killWhenUnderCameraAtLeast = 5;
						particle.setTicksFadeOutMaxOnDeath(5);
						particle.setDontRenderUnderTopmostBlock(true);
						((ParticleTexExtraRender)particle).setExtraParticlesBaseAmount(15);
						particle.fastLight = true;
						particle.setSlantParticleToWind(true);
						particle.windWeight = 1F;
		
						if (!RotatingParticleManager.useShaders || !ConfigCoroUtil.particleShaders)
						{
							particle.setFacePlayer(true);
							particle.setSlantParticleToWind(true);
						}
						else
						{
							particle.setFacePlayer(false);
							particle.extraYRotation = MC.world.rand.nextInt(360) - 180F;
						}
		
						particle.setScale(type == 2 ? 6F : 2.0F);
						particle.isTransparent = true;
						particle.setGravity(2.5F);
						//rain.isTransparent = true;
						particle.setMaxAge(50);
						//opted to leave the popin for rain, its not as bad as snow, and using fade in causes less rain visual overall
						particle.setTicksFadeInMax(5);
						particle.setAlphaF(0);
						particle.rotationYaw = MC.world.rand.nextInt(360) - 180F;
						particle.setMotionY(-0.5D);
						particle.renderOrder = 2;
						spawnParticle(particle);
					}
				}
				
				spawnArea = 50;
				for (int i = 0; i < particleCountSplash; i++)
				{
					pos = new BlockPos(MC.player.posX + MC.world.rand.nextInt(spawnArea) - (spawnArea  * 0.5F), MC.player.posY - 5 + MC.world.rand.nextInt(15), MC.player.posZ + MC.world.rand.nextInt(spawnArea) - (spawnArea * 0.5F));
					pos = MC.world.getPrecipitationHeight(pos).down();
					IBlockState state = MC.world.getBlockState(pos);
					AxisAlignedBB axisalignedbb = state.getBoundingBox(MC.world, pos);

					if (MC.world.getPrecipitationHeight(pos).getY() <= pos.up().getY())
					{
						particle = new ParticleTexFX(MC.player.world, pos.getX() + MC.world.rand.nextFloat(), pos.getY() + 0.01D + axisalignedbb.maxY, pos.getZ() + MC.world.rand.nextFloat(), 0D, 0D, 0D, net.mrbt0907.weather2.registry.ParticleRegistry.rainSplash);
						particle.setKillWhenUnderTopmostBlock(true);
						particle.setCanCollide(false);
						particle.killWhenUnderCameraAtLeast = 5;
						boolean upward = MC.world.rand.nextBoolean();

						particle.windWeight = 20F;
						particle.setFacePlayer(upward);
						particle.setScale(1F + MC.world.rand.nextFloat());
						particle.setMaxAge(15);
						particle.setGravity(-0.0F);
						particle.setTicksFadeInMax(0);
						particle.setAlphaF(0);
						particle.setTicksFadeOutMax(4);
						particle.rotationYaw = MC.world.rand.nextInt(360) - 180F;
						particle.rotationPitch = 90;
						particle.setMotionY(0D);
						particle.setMotionX((MC.world.rand.nextFloat() - 0.5F) * 0.01F);
						particle.setMotionZ((MC.world.rand.nextFloat() - 0.5F) * 0.01F);
						spawnParticle(particle);
					}
				}

				spawnArea = 60;
				for (int i = 0; i < particleCountSheet; i++)
				{
					pos = new BlockPos(MC.player.posX + MC.world.rand.nextInt(spawnArea) - (spawnArea * 0.5F), MC.player.posY - 5 + MC.world.rand.nextInt(35), MC.player.posZ + MC.world.rand.nextInt(spawnArea) - (spawnArea * 0.5F));
					posPrecip = MC.world.getPrecipitationHeight(pos);
					if (posPrecip.getY() <= pos.getY())
					{
						particle = new ParticleTexExtraRender(MC.player.world,
								pos.getX() + MC.world.rand.nextFloat(),
								pos.getY() - 1 + 0.01D,
								pos.getZ() + MC.world.rand.nextFloat(),
								0D, 0D, 0D, ParticleRegistry.downfall3);
						particle.setCanCollide(false);
						particle.killWhenUnderCameraAtLeast = 5;
						particle.setKillWhenUnderTopmostBlock(true);
						particle.setKillWhenUnderTopmostBlock_ScanAheadRange(3);
						particle.setTicksFadeOutMaxOnDeath(10);
						((ParticleTexExtraRender)particle).noExtraParticles = true;
						particle.windWeight = 19F;
						particle.setFacePlayer(true);
						particle.facePlayerYaw = true;

						particle.setScale(200F + (MC.world.rand.nextFloat() * 3F));
						particle.setMaxAge(60);
						particle.setGravity(0.35F);
						//opted to leave the popin for particle, its not as bad as snow, and using fade in causes less particle visual overall
						particle.setTicksFadeInMax(20);
						particle.setAlphaF(0);
						particle.setTicksFadeOutMax(20);

						particle.rotationYaw = MC.world.rand.nextInt(360) - 180F;
						particle.rotationPitch = 90;
						//SHADER COMPARE TEST
						particle.rotationPitch = 0;
						particle.setMotionY(-0.3D);
						particle.setMotionX((MC.world.rand.nextFloat() - 0.5F) * 0.01F);
						particle.setMotionZ((MC.world.rand.nextFloat() - 0.5F) * 0.01F);
						spawnParticle(particle);
					}
				}
			}
		}
	}
	
	public void shakeCamera(float magnitude)
	{
		MC.player.rotationYaw += Maths.random(-magnitude, magnitude); MC.player.rotationPitch += Maths.random(-magnitude, magnitude);
	}
	
	/**Processes the cached block positions to spawn ambiance particles*/
	protected void tickAmbiance()
	{
		fogMult = Maths.adjust(fogMult, fogDensity, (fogMult < 0.1F ? 0.00005F : 0.001F) * (float) ConfigParticle.rain_change_mult);
		
		WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
			if (weatherMan == null) return;
		WindManager windMan = weatherMan.windManager;
			if (windMan == null) return;

	Random rand = MC.world.rand;
	MC.profiler.startSection("effectWeather");
	//Weather Effects
	for (int i = 0; i < ClientTickHandler.weatherManager.effectedParticles.size(); i++)
	{
		Particle particle = ClientTickHandler.weatherManager.effectedParticles.get(i);
		
		if (particle == null || !particle.isAlive())
		{
			ClientTickHandler.weatherManager.effectedParticles.remove(i--);
			continue;
		}
		
		if (WindReader.getWindSpeed(MC.world, new Vec3(MC.player.posX, MC.player.posY, MC.player.posZ)) >= 0.10)
		{

			if (particle instanceof EntityRotFX)
			{
				EntityRotFX entity1 = (EntityRotFX) particle;

				if ((WeatherUtilBlock.getPrecipitationHeightSafe(MC.world, new BlockPos(MathHelper.floor(entity1.getPosX()), 0, MathHelper.floor(entity1.getPosZ()))).getY() - 1 < (int)entity1.getPosY() + 1) || (entity1 instanceof ParticleTexFX))
				{
					if (entity1 instanceof IWindHandler)
					{
						if (((IWindHandler)entity1).getParticleDecayExtra() > 0 && WeatherUtilParticle.getParticleAge(entity1) % 2 == 0)
						{
							WeatherUtilParticle.setParticleAge(entity1, WeatherUtilParticle.getParticleAge(entity1) + ((IWindHandler)entity1).getParticleDecayExtra());
						}
					}
					else if (WeatherUtilParticle.getParticleAge(entity1) % 2 == 0)
						WeatherUtilParticle.setParticleAge(entity1, WeatherUtilParticle.getParticleAge(entity1) + 1);

					if ((entity1 instanceof ParticleTexFX) && ((ParticleTexFX)entity1).getParticleTexture() == ParticleRegistry.leaf)
					{
						if (entity1.getMotionX() < 0.01F && entity1.getMotionZ() < 0.01F)
							entity1.setMotionY(entity1.getMotionY() + rand.nextDouble() * 0.02 * ((ParticleTexFX) entity1).particleGravity);
						entity1.setMotionY(entity1.getMotionY() - 0.01F * ((ParticleTexFX) entity1).particleGravity);

					}
				}

				windMan.getEntityWindVectors(entity1, 0.05F, 5.0F);
			}
		}
	}
	MC.profiler.endStartSection("effectParticle");
	//Particles
	if (WeatherUtilParticle.fxLayers != null && windMan.windSpeed >= 0.10)
	{
		//Built in particles
		for (int layer = 0; layer < WeatherUtilParticle.fxLayers.length; layer++)
		{
			for (int i = 0; i < WeatherUtilParticle.fxLayers[layer].length; i++)
			{
				for (Particle entity1 : WeatherUtilParticle.fxLayers[layer][i])
				{
					
					if (ConfigParticle.use_vanilla_rain_and_thunder)
					{
						String className = entity1.getClass().getName();
						if (className.contains("net.minecraft.") || className.contains("weather2.")) {
							
						}
						else
						{
							continue;
						}
					}

					if ((WeatherUtilBlock.getPrecipitationHeightSafe(MC.world, new BlockPos(MathHelper.floor(CoroUtilEntOrParticle.getPosX(entity1)), 0, MathHelper.floor(CoroUtilEntOrParticle.getPosZ(entity1)))).getY() - 1 < (int)CoroUtilEntOrParticle.getPosY(entity1) + 1) || (entity1 instanceof ParticleTexFX))
					{
						if ((entity1 instanceof ParticleFlame))
						{
							if (windMan.windSpeed >= 0.20) {
								entity1.particleAge += 1;
							}
						}
						else if (entity1 instanceof IWindHandler)
						{
							if (((IWindHandler)entity1).getParticleDecayExtra() > 0 && WeatherUtilParticle.getParticleAge(entity1) % 2 == 0)
							{
								entity1.particleAge += ((IWindHandler)entity1).getParticleDecayExtra();
							}
						}

						//rustle!
						if (!(entity1 instanceof EntityWaterfallFX))
						{
							if (CoroUtilEntOrParticle.getMotionX(entity1) < 0.01F && CoroUtilEntOrParticle.getMotionZ(entity1) < 0.01F)
								CoroUtilEntOrParticle.setMotionY(entity1, CoroUtilEntOrParticle.getMotionY(entity1) + rand.nextDouble() * 0.02);
						}
						windMan.getEntityWindVectors(entity1, 1F/20F, 0.5F);
					}
				}
			}
		}
	}
	MC.profiler.endSection();
	}
	
	/**Processes the cached block positions to spawn ambiance sounds*/
	protected void tickSounds()
	{
		Vec3 pos = new Vec3(MC.player.posX, MC.player.posY, MC.player.posZ);
		List<WeatherObject> weather = ClientTickHandler.weatherManager.getWeatherObjects();
		WeatherObject wo;
		float windSpeed = WindReader.getWindSpeed(MC.world, pos);
		int size = weather.size(), success = 0;
		
		WeatherUtilSound.tick();
		
		if (!ConfigParticle.use_vanilla_rain_and_thunder)
		{
			BlockPos playerPos = MC.player.getPosition(), groundPos = MC.world.getPrecipitationHeight(playerPos);
			if (MC.world.rand.nextInt(2) == 0 && playerPos.distanceSq(groundPos) < 16.0D && rain > 0.0D)
			{
				WeatherUtilSound.playForcedSound(SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, MC.player, 0.2F + (rain * 0.6F), 1.0F - (rain * 0.1F), 24.0F, true, false);
			}
		}
		
		for (int i = 0; i < size && success < 4; i++)
		{
			wo = weather.get(i);
			if (wo instanceof StormObject && success < 3)
			{
				if (((StormObject)wo).isDeadly() && ((StormObject)wo).pos_funnel_base.distance(pos) - wo.size + 200.0D <= 0.0D)
				{
					WeatherUtilSound.play2DSound(SoundRegistry.windFast, SoundCategory.WEATHER, ((StormObject)wo).pos_funnel_base, 1000 + i, (float) ConfigVolume.cyclone, ((StormObject)wo).isViolent ? 0.7F : 0.8F, ((StormObject)wo).funnelSize + 350.0F, false);
					WeatherUtilSound.play2DSound(SoundRegistry.debris, SoundCategory.WEATHER, ((StormObject)wo).pos_funnel_base, 2000 + i, (float) ConfigVolume.debris, 1.0F, ((StormObject)wo).funnelSize + 150.0F, false);
					success += 2;
				}
			}
			else if (wo.pos.distance(pos) - wo.size + 100.0D <= 0.0D)
			{
				WeatherUtilSound.play2DSound(SoundRegistry.sandstorm, SoundCategory.WEATHER, wo, 3000 + i, (float) ConfigVolume.cyclone, 1.0F, wo.size + 100.0F, false);
				success++;
			}
		}
		
		if (windSpeed > 6.5F)
		{
			if (WeatherUtilSound.isSoundActive(0, SoundRegistry.wind))
				WeatherUtilSound.stopSound(0);
			WeatherUtilSound.playSound(SoundRegistry.windFast, SoundCategory.WEATHER, 0, (float) ConfigVolume.wind, 1.0F, false);
		}
		else if (windSpeed > 1.4F)
		{
			if (WeatherUtilSound.isSoundActive(0, SoundRegistry.windFast))
				WeatherUtilSound.stopSound(0);
			WeatherUtilSound.playSound(SoundRegistry.wind, SoundCategory.WEATHER, 0, (float) ConfigVolume.wind, 1.0F, false);
		}
	}
	
	//----- Utility -----\\
	public void spawnParticle(Particle particle)
	{
		if (particle instanceof EntityRotFX)
			((EntityRotFX) particle).spawnAsWeatherEffect();
		ClientTickHandler.weatherManager.addEffectedParticle(particle);
	}
	
	public boolean changeFog()
	{
		return fogMult > 0.0F;
	}
	
	public WeatherObject getWeatherObject()
	{
		return cachedSystem;
	}
	
	private IBlockState getBlockState(int x, int y, int z)
	{
		BlockPos pos = new BlockPos(x, y ,z);
		
		if (MC.world.isBlockLoaded(pos))
		{
			IBlockState state = MC.world.getBlockState(pos);
			return state;
		}
		
		return null;
	}
	
	private BlockPos getRandomNeighbor(BlockPos pos)
	{
		BlockPos neighborPos;
		IBlockState state;
		int x, y, z;
		
		Collections.shuffle(RANDOM_POS);
		for(BlockPos randPos : RANDOM_POS)
		{
			neighborPos = pos.add(randPos);
			x = neighborPos.getX();
			y = neighborPos.getY();
			z = neighborPos.getZ();
			state = getBlockState(x, y, z);
			
			if (state != null && state.getBlock().equals(Blocks.AIR))
				return neighborPos;
		}
		
		return null;
	}
	
	//----- Other -----\\
	@Override
	public void run()
	{
		while(true)
		{
			if (run)
				try
				{
					tickThread();
					ticksThreadExisted++;
					errorsThreaded = 0;
					Thread.sleep(ConfigParticle.scene_enhancer_thread_delay);
				}
				catch (Throwable e)
				{
					if (errorsThreaded < 5)
						Weather2.warn("Scene Enhancer tickThread encountered an error. Attempting " + (5 - errorsThreaded) + " more time(s)...");
					else
					{
						Weather2.warn("Scene Enhancer tickThread has failed to run successfuly. Disaling scene enhancer...");
						run = false;
						if (MC.player != null)
							MC.player.sendMessage(new TextComponentString("Scene Enhancer has crashed on the scene thread! Error: " + e.getMessage()));
					}
					
					Weather2.error(e);
					errorsThreaded++;
				}
		}
	}
	
	public void tick()
	{
		if (run)
			try
			{
				tickNonThread();
				ticksExisted++;
				errors = 0;
			}
			catch (Throwable e)
			{
				if (errors < 5)
					Weather2.warn("Scene Enhancer tickNonThread encountered an error. Attempting " + (5 - errors) + " more time(s)...");
				else
				{
					Weather2.warn("Scene Enhancer tickNonThread has failed to run successfuly. Disaling scene enhancer...");
					run = false;
					if (MC.player != null)
						MC.player.sendMessage(new TextComponentString("Scene Enhancer has crashed on the client thread! Error: " + e.getMessage()));
				}
				
				Weather2.error(e);
				errors++;
			}
	}
	
	public synchronized void reset()
	{
		cachedSystem = null;
		errors = 0;
		errorsThreaded = 0;
		rain = rainTarget = overcast = overcastTarget = fogDensity = fogMult = 0.0F;
		Weather2.debug("Scene Enhancer has been reset");
	}
	
	public synchronized void enable()
	{
		if (!run)
		{
			run = true;
			errors = 0;
			errorsThreaded = 0;
			Weather2.debug("Scene Enhancer has been re-enabled");
		}
		else
			Weather2.warn("Scene Enhancer is already running, skipping enable...");
	}
}