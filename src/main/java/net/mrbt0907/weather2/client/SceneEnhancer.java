package net.mrbt0907.weather2.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.forge.CULog;
import CoroUtil.util.ChunkCoordinatesBlock;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntOrParticle;
import CoroUtil.util.CoroUtilMisc;
import CoroUtil.util.CoroUtilPhysics;
import extendedrenderer.EventHandler;
import extendedrenderer.particle.behavior.*;
import extendedrenderer.render.RotatingParticleManager;
import extendedrenderer.shader.Matrix4fe;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.api.WindReader;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.client.entity.particle.EntityWaterfallFX;
import net.mrbt0907.weather2.client.entity.particle.ParticleSandstorm;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.client.weather.tornado.TornadoFunnel;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilConfig;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.util.WeatherUtilParticle;
import net.mrbt0907.weather2.util.WeatherUtilSound;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.WindManager;
import net.mrbt0907.weather2.weather.storm.SandstormObject;

import CoroUtil.api.weather.IWindHandler;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleTexExtraRender;
import extendedrenderer.particle.entity.ParticleTexFX;
import extendedrenderer.particle.entity.ParticleTexLeafColor;

import javax.vecmath.Vector3f;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public class SceneEnhancer implements Runnable {
	
	//this is for the thread we make
	public World lastWorldDetected = null;

	//used for acting on fire/smoke
	public static ParticleBehaviors pm;
	public static List<Particle> spawnQueueNormal = new ArrayList<Particle>();
	public static List<Particle> spawnQueue = new ArrayList<Particle>();
	
	public static long threadLastWorldTickTime;
	public static int lastTickFoundBlocks;
	public static long lastTickAmbient;
	public static long lastTickAmbientThreaded;
	
	//consider caching somehow without desyncing or overflowing
	//WE USE 0 TO MARK WATER, 1 TO MARK LEAVES
	public static ArrayList<ChunkCoordinatesBlock> soundLocations = new ArrayList<ChunkCoordinatesBlock>();
	public static HashMap<ChunkCoordinatesBlock, Long> soundTimeLocations = new HashMap<ChunkCoordinatesBlock, Long>();
	
	public static Block SOUNDMARKER_WATER = Blocks.WATER;
	public static Block SOUNDMARKER_LEAVES = Blocks.LEAVES;
	
	public static float curPrecipStr = 0F;
	public static float curPrecipStrTarget = 0F;
	public static float curOvercastStr = 0F;
	public static float curOvercastStrTarget = 0F;
	public static float curDampness;
	
	//testing
	public static ParticleBehaviorMiniTornado miniTornado;
	public static ParticleBehaviorFogGround particleBehaviorFog;
	public static Vec3d vecWOP = null;
	
	//sandstorm fog state
	public static double distToStormThreshold = 100;
	public static double distToStorm = distToStormThreshold + 50;
	public static float fogRed = 0;
	public static float fogRedTarget = 0;
	public static float fogRedOrig = 0;
	public static float fogGreen = 0;
	public static float fogGreenTarget = 0;
	public static float fogGreenOrig = 0;
	public static float fogBlue = 0;
	public static float fogBlueTarget = 0;
	public static float fogBlueOrig = 0;
	public static float fogDensity = 0;
	public static float fogDensityTarget = 0;
	public static float fogStart = 0;
	public static float fogEnd = 0;
	public static float fogMult = 0F;
	public static float fogMultTarget = 0F;
	public static float fogDistance = 0.0F;
	public static float adjustAmountTargetPocketSandOverride = 0F;
	public static boolean isPlayerOutside = true;

	public static ParticleBehaviorSandstorm particleBehavior;
	public static ParticleTexExtraRender testParticle;

	public static EntityRotFX testParticle2;
	private static List<BlockPos> listPosRandom = new ArrayList<>();
	public static List<EntityRotFX> testParticles = new ArrayList<>();

	public static Matrix4fe matrix = new Matrix4fe();
	public static Matrix4fe matrix2 = new Matrix4fe();
	public static Vector3f vec = new Vector3f();
	public static Vector3f vec2 = new Vector3f();
	public static TornadoFunnel funnel;
	private static WeatherObject wo;

	public SceneEnhancer() {
		pm = new ParticleBehaviors(null);

		listPosRandom.clear();
		listPosRandom.add(new BlockPos(0, -1, 0));
		listPosRandom.add(new BlockPos(1, 0, 0));
		listPosRandom.add(new BlockPos(-1, 0, 0));
		listPosRandom.add(new BlockPos(0, 0, 1));
		listPosRandom.add(new BlockPos(0, 0, -1));
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				tickClientThreaded();
				Thread.sleep(ConfigParticle.effect_process_delay);
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		}
	}

	//run from client side _mc_ thread
	public void tickClient()
	{
		if (!WeatherUtil.isPaused() && !ConfigMisc.toaster_pc_mode)
		{
			Minecraft mc = FMLClientHandler.instance().getClient();
			mc.mcProfiler.startSection("particleSpawning");
			tryParticleSpawning();
			mc.mcProfiler.endStartSection("tickRain");
			tickRainRates();
			mc.mcProfiler.endStartSection("tickPrecipitation");
			tickParticlePrecipitation();
			mc.mcProfiler.endStartSection("tickSoundA");
			trySoundPlaying();
			mc.mcProfiler.endStartSection("tickSoundB");
			tickSounds();

			if (mc.world != null && lastWorldDetected != mc.world)
			{
				lastWorldDetected = mc.world;
				reset();
			}

			mc.mcProfiler.endStartSection("tickWind");
			tryWind(mc.world);

			mc.mcProfiler.endStartSection("tickFog");
			tickFog();

			mc.mcProfiler.endStartSection("tickStormFog");
			tickStormFog();

			mc.mcProfiler.endStartSection("tickParticle");
			if (particleBehavior == null) {
				particleBehavior = new ParticleBehaviorSandstorm(null);
			}
			particleBehavior.tickUpdateList();

			if (ConfigCoroUtil.foliageShaders && EventHandler.queryUseOfShaders()) {
				if (!FoliageEnhancerShader.useThread) {
					if (mc.world.getTotalWorldTime() % 40 == 0) {
						FoliageEnhancerShader.tickClientThreaded();
					}
				}

				if (mc.world.getTotalWorldTime() % 5 == 0) {
					FoliageEnhancerShader.tickClientCloseToPlayer();
				}
			}

			mc.mcProfiler.endSection();
		}
	}
	
	//run from our newly created thread
	public void tickClientThreaded() {
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (mc.world != null && mc.player != null && WeatherUtilConfig.isEffectsEnabled(mc.world.provider.getDimension()) && ClientTickHandler.weatherManager != null)
		{
			if (mc.world.getTotalWorldTime() % 10L == 0L)
			{
				wo = ClientTickHandler.weatherManager.getClosestWeather(new Vec3(mc.player.posX, mc.player.posY, mc.player.posZ), ConfigParticle.render_distance);
			}
			profileSurroundings();
			tryAmbientSounds();
		}
	}
	
	public static void tickFog()
	{
		Minecraft mc = Minecraft.getMinecraft();
		float sunBrightness = mc.world.getSunBrightness(1F);
		float fogBoundary = fogDistance;
		float fogBoundaryClose = fogBoundary - (fogBoundary * 0.3F);
		
		//make it be full intensity once storm is halfway there
		fogMultTarget = (float) (1.0D - (distToStorm / distToStormThreshold));
		fogMultTarget *= isPlayerOutside ? 2.0F : 1.0F;
				
		if (fogMultTarget < 0F)
			fogMultTarget = 0F;
		else if (fogMultTarget > 1F)
			fogMultTarget = 1F;
		
		fogMult = CoroUtilMisc.adjVal(fogMult, fogMultTarget, 0.003F);
		fogRed = CoroUtilMisc.adjVal(fogRed, fogRedTarget * sunBrightness, 0.006F);
		fogGreen = CoroUtilMisc.adjVal(fogGreen, fogGreenTarget * sunBrightness, 0.006F);
		fogBlue = CoroUtilMisc.adjVal(fogBlue, fogBlueTarget * sunBrightness, 0.006F);
		fogDensity = CoroUtilMisc.adjVal(fogDensity, fogDensityTarget * fogMult, 0.006F);
		fogStart = CoroUtilMisc.adjVal(fogStart, fogBoundaryClose - (fogBoundaryClose * fogDensity * fogMult), fogBoundary * 0.5F);
		fogEnd = CoroUtilMisc.adjVal(fogEnd, fogBoundary - (fogBoundary * fogDensity * fogMult), fogBoundary * 0.5F);
		if (mc.world.getTotalWorldTime() % 200 == 0)
			try
			{
				Object fogState = ObfuscationReflectionHelper.getPrivateValue(GlStateManager.class, null, "field_179155_g");
				Class<?> innerClass = Class.forName("net.minecraft.client.renderer.GlStateManager$FogState");
				Field fieldEnd = null;
				try
				{
					fieldEnd = innerClass.getField("field_179046_e");
					fieldEnd.setAccessible(true);
				}
				catch (Exception e)
				{
					fieldEnd = innerClass.getField("end");
					fieldEnd.setAccessible(true);
				}
				fogEnd = fieldEnd.getFloat(fogState);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	}
	
	public synchronized void trySoundPlaying()
	{
		try
		{
			if (lastTickAmbient < System.currentTimeMillis())
			{
				lastTickAmbient = System.currentTimeMillis() + 500;
				
				Minecraft mc = FMLClientHandler.instance().getClient();
				
				World worldRef = mc.world;
				EntityPlayer player = mc.player;
				
				int size = 32;
				//int hsize = size / 2;
				int curX = (int)player.posX;
				int curY = (int)player.posY;
				int curZ = (int)player.posZ;
				
				Random rand = new Random();
				
				//trim out distant sound locations, also update last time played
				for (int i = 0; i < soundLocations.size(); i++)
				{
					ChunkCoordinatesBlock cCor = soundLocations.get(i);
					
					if (Math.sqrt(cCor.getDistanceSquared(curX, curY, curZ)) > size) {
						soundLocations.remove(i--);
						soundTimeLocations.remove(cCor);
						//System.out.println("trim out soundlocation");
					}
					else
					{
						Block block = getBlock(worldRef, cCor.posX, cCor.posY, cCor.posZ);//Block.blocksList[id];
						
						if (block == null || (block.getMaterial(block.getDefaultState()) != Material.WATER && block.getMaterial(block.getDefaultState()) != Material.LEAVES)) {
							soundLocations.remove(i);
							soundTimeLocations.remove(cCor);
						} else {
							
							long lastPlayTime = 0;
							
							
							
							if (soundTimeLocations.containsKey(cCor)) {
								lastPlayTime = soundTimeLocations.get(cCor);
							}
							
							//System.out.println(Math.sqrt(cCor.getDistanceSquared(curX, curY, curZ)));
							if (lastPlayTime < System.currentTimeMillis())
							{
								if (cCor.block == SOUNDMARKER_WATER)
								{
									soundTimeLocations.put(cCor, System.currentTimeMillis() + 2500 + rand.nextInt(50));
									mc.world.playSound(cCor.toBlockPos(), SoundRegistry.waterfall, SoundCategory.AMBIENT, (float)ConfigVolume.waterfall, 0.75F + (rand.nextFloat() * 0.05F), false);
								}
								else if (cCor.block == SOUNDMARKER_LEAVES)
								{	
									float windSpeed = WindReader.getWindSpeed(mc.world, new Vec3(cCor.posX, cCor.posY, cCor.posZ));
									soundTimeLocations.put(cCor, System.currentTimeMillis() + 12000 + rand.nextInt(50));
									mc.world.playSound(cCor.toBlockPos(), SoundRegistry.leaves, SoundCategory.AMBIENT, (float)(windSpeed * 2F * ConfigVolume.leaves), 0.70F + (rand.nextFloat() * 0.1F), false);									
								}
							}
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			System.out.println("Weather2: Error handling sound play queue: ");
			ex.printStackTrace();
		}
	}
	
	//Threaded function
	@SideOnly(Side.CLIENT)
	public static void tryAmbientSounds()
	{
		Minecraft mc = FMLClientHandler.instance().getClient();
		
		World worldRef = mc.world;
		EntityPlayer player = mc.player;
		
		//Random rand = new Random();
		
		if (lastTickAmbientThreaded < System.currentTimeMillis()) {
			lastTickAmbientThreaded = System.currentTimeMillis() + 500;
			
			int size = 32;
			int hsize = size / 2;
			int curX = (int)player.posX;
			int curY = (int)player.posY;
			int curZ = (int)player.posZ;
			
			//soundLocations.clear();
			
			
			
			for (int xx = curX - hsize; xx < curX + hsize; xx++)
			{
				for (int yy = curY - (hsize / 2); yy < curY + hsize; yy++)
				{
					for (int zz = curZ - hsize; zz < curZ + hsize; zz++)
					{
						Block block = getBlock(worldRef, xx, yy, zz);
						
						if (block != null) {
							
							//Waterfall
							if (ConfigParticle.enable_waterfall_splash && ((block.getMaterial(block.getDefaultState()) == Material.WATER))) {
								
								int meta = getBlockMetadata(worldRef, xx, yy, zz);
								if ((meta & 8) != 0) {
									
									int bottomY = yy;
									int index = 0;
									
									//this scans to bottom till not water, kinda overkill? owell lets keep it, and also add rule if index > 4 (waterfall height of 4)
									while (yy-index > 0) {
										Block id2 = getBlock(worldRef, xx, yy-index, zz);
										if (id2 != null && !(id2.getMaterial(id2.getDefaultState()) == Material.WATER)) {
											break;
										}
										index++;
									}
									
									bottomY = yy-index+1;
									
									//check if +10 from here is water with right meta too
									int meta2 = getBlockMetadata(worldRef, xx, bottomY+10, zz);
									Block block2 = getBlock(worldRef, xx, bottomY+10, zz);;
									
									if (index >= 4 && (block2 != null && block2.getMaterial(block2.getDefaultState()) == Material.WATER && (meta2 & 8) != 0)) {
										boolean proxFail = false;
										for (int j = 0; j < soundLocations.size(); j++) {
											if (Math.sqrt(soundLocations.get(j).getDistanceSquared(xx, bottomY, zz)) < 5) {
												proxFail = true;
												break;
											}
										}
										
										if (!proxFail) {
											soundLocations.add(new ChunkCoordinatesBlock(xx, bottomY, zz, SOUNDMARKER_WATER, 0));
											//System.out.println("add waterfall");
										}
									}
								}
							} else if (ConfigVolume.leaves > 0 && ((block.getMaterial(block.getDefaultState()) == Material.LEAVES))) {
								boolean proxFail = false;
								for (int j = 0; j < soundLocations.size(); j++) {
									if (Math.sqrt(soundLocations.get(j).getDistanceSquared(xx, yy, zz)) < 15) {
										proxFail = true;
										break;
									}
								}
								
								if (!proxFail) {
									soundLocations.add(new ChunkCoordinatesBlock(xx, yy, zz, SOUNDMARKER_LEAVES, 0));
									//System.out.println("add leaves sound location");
								}
							}
						}
					}
				}
			}
		}
	}

	public void reset()
	{
		curPrecipStr = 0.0F;
		curPrecipStrTarget = 0.0F;
		curOvercastStr = 0.0F;
		curOvercastStrTarget = 0.0F;
		curDampness = 0.0F;
		wo = null;
		//reset particle data, discard dead ones as that was a bug from weather1
		lastWorldDetected.weatherEffects.clear();
		
		if (WeatherUtilParticle.fxLayers == null) {
			WeatherUtilParticle.getFXLayers();
		}
	}
	
	public void tickParticlePrecipitation()
	{
		if (ConfigParticle.enable_precipitation)
		{
			EntityPlayer entP = FMLClientHandler.instance().getClient().player;
			
			if (entP.posY >= ConfigStorm.cloud_layer_0_height) return;

			WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
			if (weatherMan == null) return;
			WindManager windMan = weatherMan.windManager;
			if (windMan == null) return;

			float curPrecipVal = getRainStrengthAndControlVisuals(entP);
			boolean shouldSnow = curPrecipVal <= 0.0F;
			float maxPrecip = 1.0F;
			Biome biomegenbase = entP.world.getBiome(new BlockPos(MathHelper.floor(entP.posX), 0, MathHelper.floor(entP.posZ)));
			World world = entP.world;
			Random rand = entP.world.rand;

			double particleAmp = 1F;
			if (RotatingParticleManager.useShaders && ConfigCoroUtil.particleShaders)
				particleAmp = ConfigParticle.particle_multiplier;

			if (funnel == null)
			{
				funnel = new TornadoFunnel();
				funnel.pos = new Vec3d(entP.posX, entP.posY, entP.posZ);
			}

			//check rules same way vanilla texture precip does
			if (biomegenbase != null && (biomegenbase.canRain() || biomegenbase.getEnableSnow()))
			{
				//now absolute it for ez math
				curPrecipVal = Math.min(maxPrecip, Math.abs(curPrecipVal));
				
				if (curPrecipVal > 0)
				{
					int spawnCount;
					int spawnNeed = (int)(curPrecipVal * 20F * ConfigParticle.precipitation_particle_rate * particleAmp);
					int safetyCutout = 100;

					int extraRenderCount = 15;

					//attempt to fix the cluttering issue more noticable when barely anything spawning
					if (curPrecipVal < 0.1 && ConfigParticle.precipitation_particle_rate > 0) {
						//swap rates
						int oldVal = extraRenderCount;
						extraRenderCount = spawnNeed;
						spawnNeed = oldVal;
					}

					//rain
					if (!shouldSnow)
					{
						spawnCount = 0;
						int spawnAreaSize = 20;

						if (spawnNeed > 0)
						{
							for (int i = 0; i < safetyCutout; i++)
							{
								BlockPos pos = new BlockPos(
										entP.posX + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
										entP.posY - 5 + rand.nextInt(25),
										entP.posZ + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

								//EntityRenderer.addRainParticles doesnt actually use isRainingAt,
								//switching to match what that method does to improve consistancy and tough as nails compat
								if (canPrecipitateAt(world, pos))
								{
									ParticleTexExtraRender rain = new ParticleTexExtraRender(entP.world,
											pos.getX(),
											pos.getY(),
											pos.getZ(),
											0D, 0D, 0D, ParticleRegistry.rain_white);
									rain.setKillWhenUnderTopmostBlock(true);
									rain.setCanCollide(false);
									rain.killWhenUnderCameraAtLeast = 5;
									rain.setTicksFadeOutMaxOnDeath(5);
									rain.setDontRenderUnderTopmostBlock(true);
									rain.setExtraParticlesBaseAmount(extraRenderCount);
									rain.fastLight = true;
									rain.setSlantParticleToWind(true);
									rain.windWeight = 1F;

									if (!RotatingParticleManager.useShaders || !ConfigCoroUtil.particleShaders) {
										//old slanty rain way
										rain.setFacePlayer(true);
										rain.setSlantParticleToWind(true);
									} else {
										//new slanty rain way
										rain.setFacePlayer(false);
										rain.extraYRotation = rain.getWorld().rand.nextInt(360) - 180F;
									}

									//rain.setFacePlayer(true);
									rain.setScale(2F);
									rain.isTransparent = true;
									rain.setGravity(2.5F);
									//rain.isTransparent = true;
									rain.setMaxAge(50);
									//opted to leave the popin for rain, its not as bad as snow, and using fade in causes less rain visual overall
									rain.setTicksFadeInMax(5);
									rain.setAlphaF(0);
									rain.rotationYaw = rain.getWorld().rand.nextInt(360) - 180F;
									rain.setMotionY(-0.5D);
									rain.renderOrder = 2;
									rain.spawnAsWeatherEffect();
									ClientTickHandler.weatherManager.addEffectedParticle(rain);

									spawnCount++;
									if (spawnCount >= spawnNeed) {
										break;
									}
								}
							}
						}

						boolean groundSplash = ConfigParticle.enable_precipitation_splash;
						boolean downfall = ConfigParticle.enable_heavy_precipitation;

						//TODO: make ground splash and downfall use spawnNeed var style design

						spawnAreaSize = 100;
						//ground splash
						if (groundSplash == true && curPrecipVal > 0.15F) {
							for (int i = 0; i < 30F * curPrecipVal * ConfigParticle.precipitation_particle_rate * particleAmp * 5F; i++) {
								BlockPos pos = new BlockPos(
										entP.posX + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
										entP.posY - 5 + rand.nextInt(15),
										entP.posZ + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));


								//get the block on the topmost ground
								pos = world.getPrecipitationHeight(pos).down()/*.add(0, 1, 0)*/;

								IBlockState state = world.getBlockState(pos);
								AxisAlignedBB axisalignedbb = state.getBoundingBox(world, pos);

								if (pos.getDistance(MathHelper.floor(entP.posX), MathHelper.floor(entP.posY), MathHelper.floor(entP.posZ)) > spawnAreaSize / 2)
									continue;

								//block above topmost ground
								if (canPrecipitateAt(world, pos.up()))
								{
									ParticleTexFX rain = new ParticleTexFX(entP.world,
											pos.getX() + rand.nextFloat(),
											pos.getY() + 0.01D + axisalignedbb.maxY,
											pos.getZ() + rand.nextFloat(),
											0D, 0D, 0D, ParticleRegistry.cloud256_6);
									//rain.setCanCollide(true);
									rain.setKillWhenUnderTopmostBlock(true);
									rain.setCanCollide(false);
									rain.killWhenUnderCameraAtLeast = 5;
									boolean upward = rand.nextBoolean();

									rain.windWeight = 20F;
									rain.setFacePlayer(upward);
									//SHADER COMPARE TEST
									//rain.setFacePlayer(false);

									rain.setScale(3F + (rand.nextFloat() * 3F));
									rain.setMaxAge(15);
									rain.setGravity(-0.0F);
									//opted to leave the popin for rain, its not as bad as snow, and using fade in causes less rain visual overall
									rain.setTicksFadeInMax(0);
									rain.setAlphaF(0);
									rain.setTicksFadeOutMax(4);

									rain.rotationYaw = rain.getWorld().rand.nextInt(360) - 180F;
									rain.rotationPitch = 90;
									rain.setMotionY(0D);
									rain.setMotionX((rand.nextFloat() - 0.5F) * 0.01F);
									rain.setMotionZ((rand.nextFloat() - 0.5F) * 0.01F);
									rain.spawnAsWeatherEffect();
									ClientTickHandler.weatherManager.addEffectedParticle(rain);
								}
							}
						}
						spawnAreaSize = 20;
						//downfall - at just above 0.3 cause rainstorms lock at 0.3 but flicker a bit above and below
						if (downfall == true && curPrecipVal >= 0.5) {

							int scanAheadRange = 0;
							//quick is outside check, prevent them spawning right near ground
							//and especially right above the roof so they have enough space to fade out
							//results in not seeing them through roofs
							if (entP.world.canBlockSeeSky(entP.getPosition()))
								scanAheadRange = 3;
							else
								scanAheadRange = 10;

							for (int i = 0; i < 1.5F * curPrecipVal * ConfigParticle.precipitation_particle_rate; i++)
							{
								BlockPos pos = new BlockPos(
										entP.posX + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
										entP.posY + 5 + rand.nextInt(15),
										entP.posZ + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

								if (entP.getDistanceSq(pos) < 10D * 10D) continue;

								if (canPrecipitateAt(world, pos.up(-scanAheadRange)))
								{
									ParticleTexExtraRender rain = new ParticleTexExtraRender(entP.world,
											pos.getX() + rand.nextFloat(),
											pos.getY() - 1 + 0.01D,
											pos.getZ() + rand.nextFloat(),
											0D, 0D, 0D, ParticleRegistry.downfall3);
									rain.setCanCollide(false);
									rain.killWhenUnderCameraAtLeast = 5;
									rain.setKillWhenUnderTopmostBlock(true);
									rain.setKillWhenUnderTopmostBlock_ScanAheadRange(scanAheadRange);
									rain.setTicksFadeOutMaxOnDeath(10);
									rain.noExtraParticles = true;
									rain.windWeight = 8F;
									rain.setFacePlayer(true);
									rain.facePlayerYaw = true;

									rain.setScale(90F + (rand.nextFloat() * 3F));
									rain.setMaxAge(60);
									rain.setGravity(0.35F);
									//opted to leave the popin for rain, its not as bad as snow, and using fade in causes less rain visual overall
									rain.setTicksFadeInMax(20);
									rain.setAlphaF(0);
									rain.setTicksFadeOutMax(20);

									rain.rotationYaw = rain.getWorld().rand.nextInt(360) - 180F;
									rain.rotationPitch = 90;
									//SHADER COMPARE TEST
									rain.rotationPitch = 0;
									rain.setMotionY(-0.3D);
									rain.setMotionX((rand.nextFloat() - 0.5F) * 0.01F);
									rain.setMotionZ((rand.nextFloat() - 0.5F) * 0.01F);
									rain.spawnAsWeatherEffect();
									ClientTickHandler.weatherManager.addEffectedParticle(rain);
								}
							}
						}
					//snow
					}
					else
					{
						//Weather.dbg("rate: " + curPrecipVal * 5F * ConfigMisc.Particle_Precipitation_effect_rate);

						spawnCount = 0;
						//less for snow, since it falls slower so more is on screen longer
						spawnNeed = (int)(curPrecipVal * 40F * ConfigParticle.precipitation_particle_rate * particleAmp);

						int spawnAreaSize = 50;

						if (spawnNeed > 0) {
							for (int i = 0; i < safetyCutout/*curPrecipVal * 20F * ConfigParticle.Precipitation_Particle_effect_rate*/; i++) {
								BlockPos pos = new BlockPos(
										entP.posX + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
										entP.posY - 5 + rand.nextInt(25),
										entP.posZ + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

								if (canPrecipitateAt(world, pos)) {
									ParticleTexExtraRender snow = new ParticleTexExtraRender(entP.world, pos.getX(), pos.getY(), pos.getZ(),
											0D, 0D, 0D, ParticleRegistry.snow);

									snow.setCanCollide(false);
									snow.setKillWhenUnderTopmostBlock(true);
									snow.setTicksFadeOutMaxOnDeath(5);
									snow.setDontRenderUnderTopmostBlock(true);
									snow.setExtraParticlesBaseAmount(10);
									snow.killWhenFarFromCameraAtLeast = 20;

									snow.setMotionY(-0.1D);
									snow.setScale(1.3F);
									snow.setGravity(0.1F);
									snow.windWeight = 0.2F;
									snow.setMaxAge(40);
									snow.setFacePlayer(false);
									snow.setTicksFadeInMax(5);
									snow.setAlphaF(0);
									snow.setTicksFadeOutMax(5);
									//snow.setCanCollide(true);
									//snow.setKillOnCollide(true);
									snow.rotationYaw = snow.getWorld().rand.nextInt(360) - 180F;
									snow.spawnAsWeatherEffect();
									ClientTickHandler.weatherManager.addEffectedParticle(snow);

									spawnCount++;
									if (spawnCount >= spawnNeed) {
										break;
									}
								}

							}
						}

					}
				}
			}

		}
	}

	public static boolean canPrecipitateAt(World world, BlockPos strikePosition)
	{
		return world.getPrecipitationHeight(strikePosition).getY() <= strikePosition.getY();
	}
	
	public static float getRainStrengthAndControlVisuals(EntityPlayer entP) {
		return getRainStrengthAndControlVisuals(entP, false);
	}

	/**
	 * Returns value between -1 to 1
	 * -1 is full on snow
	 * 1 is full on rain
	 * 0 is no precipitation
	 *
	 * also controls the client side raining and thundering values for vanilla
	 *
	 * @param entP
	 * @param forOvercast
	 * @return
	 */
	public static float getRainStrengthAndControlVisuals(EntityPlayer entP, boolean forOvercast)
	{
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		double stormDist = 9999;
		float tempAdj = 1F;
		float sizeToUse = 0;
		float overcastModeMinPrecip = (float)ConfigStorm.min_overcast_rain;
		Vec3 plPos = new Vec3(entP.posX, ConfigStorm.cloud_layer_0_height, entP.posZ);
		StormObject storm = null;
		
		ClientTickHandler.checkClientWeather();
		
		if (WeatherUtilConfig.isWeatherEnabled(ClientTickHandler.weatherManager.getDimension()))
		{
			if (wo instanceof StormObject)
				storm = (StormObject)wo; 
			//evaluate if storms size is big enough to be over player
			if (storm != null)
			{
				sizeToUse = (float) (storm.size * 0.50F);
				
				if (forOvercast)
					sizeToUse *= 2F;
				
				stormDist = Math.max(storm.pos.distance(plPos) - sizeToUse, 0.0D);
				double overcastIntensity = 1.0F - Math.min(stormDist / 100.0F, 1.0F);
				curOvercastStrTarget = (float) overcastIntensity;
				
				if (storm.hasDownfall() && sizeToUse > stormDist)
				{
					double rainIntensity = ConfigParticle.enable_vanilla_rain ? 0.0D : overcastIntensity * Math.min((storm.rain - IWeatherRain.MINIMUM_DRIZZLE) / 150, 1.0F);
					
					tempAdj = storm.temperature > 0 ? 1F : -1F;
					
					//limit plain rain clouds to light intensity
					if (storm.stage < Stage.THUNDER.getStage())
						if (rainIntensity > 0.35) rainIntensity = 0.35;
		
				   if (ConfigMisc.overcast_mode && rainIntensity < overcastModeMinPrecip)
						rainIntensity = overcastModeMinPrecip;
					
					mc.world.getWorldInfo().setRaining(true);
					mc.world.getWorldInfo().setThundering(true);
					curPrecipStrTarget = Math.min((float) rainIntensity, 1.0F);
			   }
				else
				{
					if (!ClientTickHandler.clientConfigData.overcastMode)
					{
						mc.world.getWorldInfo().setRaining(false);
						mc.world.getWorldInfo().setThundering(false);
						
						curPrecipStrTarget = 0;
					}
					else
					{
						if (ClientTickHandler.weatherManager.weatherID >= 1)
						{
							mc.world.getWorldInfo().setRaining(true);
							mc.world.getWorldInfo().setThundering(true);
							
							if (forOvercast)
								curOvercastStrTarget = overcastModeMinPrecip;
							else
								curPrecipStrTarget = overcastModeMinPrecip;
						}
						else
							curPrecipStrTarget = 0;
					}
				}
			}
			else
			{
				curPrecipStrTarget = 0;
				curOvercastStrTarget = 0;
			}
		}
		else
		{
			switch(ClientTickHandler.weatherManager.weatherID)
			{
				case 1:
					curOvercastStrTarget = 0.8F;
					curPrecipStrTarget = 0.30F;
					break;
				case 2:
					curOvercastStrTarget = 1.0F;
					curPrecipStrTarget = 1.0F;
					break;
				default:
					curOvercastStrTarget = 0;
					curPrecipStrTarget = 0;
			}
		}
		
		if (forOvercast)
		{
			if (curOvercastStr < 0.001 && curOvercastStr > -0.001F)
				return 0;
			else
				return curOvercastStr * tempAdj;
		}
		else
		{
			if (curPrecipStr < 0.001 && curPrecipStr > -0.001F)
				return 0;
			else
				return curPrecipStr * tempAdj;
		}
	}

	public static void tickRainRates() {

		float rateChange = (float) (0.0005F * ConfigParticle.rain_change_mult);

		curOvercastStr = CoroUtilMisc.adjVal(curOvercastStr, curOvercastStrTarget, rateChange);
		curPrecipStr = CoroUtilMisc.adjVal(curPrecipStr, curPrecipStrTarget, rateChange);
		
		if (curPrecipStr > 0.1F && curDampness < 1.0F)
			curDampness += 0.01F;
		else if (curDampness > 0.0F)
			curDampness -= 0.01F;
	}

	public static float getPrecipStrength(EntityPlayer entP, boolean forOvercast) {
		StormObject storm = getClosestStormCached(entP);
		if (storm != null)
		{
			float tempAdj = storm.temperature > 0.0F ? 1.0F : -1.0F;

			if (forOvercast)
				return curOvercastStr * tempAdj;
			else
				return curPrecipStr * tempAdj;
		}

		return 0;
	}

	public static void controlVanillaPrecipVisuals(EntityPlayer entP, boolean forOvercast) {}

	public static StormObject getClosestStormCached(EntityPlayer entP)
	{
		if (WeatherManagerClient.closestStormCached == null || entP.world.getTotalWorldTime() % 5 == 0) {
			//Minecraft mc = FMLClientHandler.instance().getClient();

			double maxStormDist = 512 / 4 * 3;
			Vec3 plPos = new Vec3(entP.posX, ConfigStorm.cloud_layer_0_height, entP.posZ);

			ClientTickHandler.checkClientWeather();

			WeatherManagerClient.closestStormCached = (StormObject) ClientTickHandler.weatherManager.getClosestWeather(plPos, maxStormDist, Stage.TORNADO.getStage(), Integer.MAX_VALUE, Type.BLIZZARD, Type.SANDSTORM);
		}

		return WeatherManagerClient.closestStormCached;
	}
	
	public synchronized void tryParticleSpawning()
	{
		if (spawnQueue.size() > 0) {
			//System.out.println("spawnQueue.size(): " + spawnQueue.size());
		}
		
		try {
			for (int i = 0; i < spawnQueue.size(); i++)
			{
				Particle ent = spawnQueue.get(i);
	
				if (ent != null/* && ent.world != null*/) {
				
					if (ent instanceof EntityRotFX)
					{
						((EntityRotFX) ent).spawnAsWeatherEffect();
					}/*
					else
					{
						ent.world.addWeatherEffect(ent);
					}*/
					ClientTickHandler.weatherManager.addEffectedParticle(ent);
				}
			}
			for (int i = 0; i < spawnQueueNormal.size(); i++)
			{
				Particle ent = spawnQueueNormal.get(i);
	
				if (ent != null/* && ent.world != null*/) {
				
					Minecraft.getMinecraft().effectRenderer.addEffect(ent);
				}
			}
		} catch (Exception ex) {
			CULog.err("Weather2: Error handling particle spawn queue: ");
			ex.printStackTrace();
		}

		spawnQueue.clear();
		spawnQueueNormal.clear();
	}
	
	public void profileSurroundings()
	{
		//tryClouds();
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		World world = lastWorldDetected;
		EntityPlayer player = FMLClientHandler.instance().getClient().player;
		WeatherManagerClient manager = ClientTickHandler.weatherManager;
		
		if (world == null || player == null || manager == null || manager.windManager == null)
		{
			try {
				Thread.sleep(1000L);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return;
		}

		if (threadLastWorldTickTime == world.getTotalWorldTime())
		{
			return;
		}

		threadLastWorldTickTime = world.getTotalWorldTime();
		
		Random rand = new Random();
		
		//mining a tree causes leaves to fall
		int size = 40;
		int hsize = size / 2;
		int curX = (int)player.posX;
		int curY = (int)player.posY;
		int curZ = (int)player.posZ;
		//if (true) return;
		
		float windStr = WindReader.getWindSpeed(world, new Vec3(curX, curY, curZ));

		if ((!ConfigParticle.enable_falling_leaves && !ConfigParticle.enable_waterfall_splash)) {return;}

		//Wind requiring code goes below
		int spawnRate = (int)(30 / (windStr + 0.001));
		float lastBlockCount = lastTickFoundBlocks;
		float particleCreationRate = (float) ConfigParticle.ambient_particle_rate;
		float maxScaleSample = 15000;
		if (lastBlockCount > maxScaleSample) lastBlockCount = maxScaleSample-1;
		float scaleRate = (maxScaleSample - lastBlockCount) / maxScaleSample;
		
		spawnRate = (int) ((spawnRate / (scaleRate + 0.001F)) / (particleCreationRate + 0.001F));
		
		int BlockCountRate = (int)(((300 / scaleRate + 0.001F)) / (particleCreationRate + 0.001F)); 
		
		spawnRate *= (mc.gameSettings.particleSetting+1);
		BlockCountRate *= (mc.gameSettings.particleSetting+1);
		
		//since reducing threaded ticking to 200ms sleep, 1/4 rate, must decrease rand size
		spawnRate /= 2;
		
		//performance fix
		if (spawnRate < 40)
			spawnRate = 40;
		
		//performance fix
		if (BlockCountRate < 80) BlockCountRate = 80;
		//patch for block counts over 15000
		if (BlockCountRate > 5000) BlockCountRate = 5000;
		
		lastTickFoundBlocks = 0;

		double particleAmp = 1F;
		if (RotatingParticleManager.useShaders && ConfigCoroUtil.particleShaders)
			particleAmp = ConfigParticle.particle_multiplier * 2D;

		spawnRate = (int)((double)spawnRate / particleAmp);
		
		//Semi intensive area scanning code
		for (int xx = curX - hsize; xx < curX + hsize; xx++)
		{
			for (int yy = curY - (hsize / 2); yy < curY + hsize; yy++)
			{
				for (int zz = curZ - hsize; zz < curZ + hsize; zz++)
				{
					Block block = getBlock(world, xx, yy, zz);
					
					if (block != null)
					{
						Material material = block.getDefaultState().getMaterial();
						
						if (material == Material.LEAVES || material == Material.VINE || material  == Material.PLANTS)
						{
							lastTickFoundBlocks++;
							
							if (world.rand.nextInt(spawnRate) == 0)
							{
								//far out enough to avoid having the AABB already inside the block letting it phase through more
								//close in as much as we can to make it look like it came from the block
								double relAdj = 0.70D;
	
								BlockPos pos = getRandomWorkingPos(world, new BlockPos(xx, yy, zz));
								double xRand = 0;
								double yRand = 0;
								double zRand = 0;
	
								if (pos != null)
								{
									//further limit the spawn position along the face side to prevent it clipping into perpendicular blocks
									float particleAABB = 0.1F;
									float particleAABBAndBuffer = particleAABB + 0.05F;
									float invert = 1F - (particleAABBAndBuffer * 2F);
	
									if (pos.getY() != 0)
									{
										xRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
										zRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
									}
									else if (pos.getX() != 0)
									{
										yRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
										zRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
									}
									else if (pos.getZ() != 0)
									{
										yRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
										xRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
									}
	
									EntityRotFX var31 = new ParticleTexLeafColor(world, xx, yy, zz, 0D, 0D, 0D, ParticleRegistry.leaf);
									var31.setPosition(xx + 0.5D + (pos.getX() * relAdj) + xRand,
									yy + 0.5D + (pos.getY() * relAdj) + yRand,
									zz + 0.5D + (pos.getZ() * relAdj) + zRand);
									var31.setPrevPosX(var31.posX);
									var31.setPrevPosY(var31.posY);
									var31.setPrevPosZ(var31.posZ);
									var31.setMotionX(0);
									var31.setMotionY(0);
									var31.setMotionZ(0);
									var31.setGravity(0.05F);
									var31.setCanCollide(true);
									var31.setKillOnCollide(false);
									var31.collisionSpeedDampen = false;
									var31.killWhenUnderCameraAtLeast = 20;
									var31.killWhenFarFromCameraAtLeast = 20;
									var31.isTransparent = false;
									var31.rotationYaw = rand.nextInt(360);
									var31.rotationPitch = rand.nextInt(360);
									var31.updateQuaternion(null);
									spawnQueue.add(var31);
								}
							}
						}
						else if (ConfigParticle.enable_waterfall_splash && player.getDistance(xx,  yy, zz) < 16 && (block != null && block.getMaterial(block.getDefaultState()) == Material.WATER))
						{	
							int meta = getBlockMetadata(world, xx, yy, zz);
							
							if ((meta & 8) != 0)
							{
								lastTickFoundBlocks += 70; //adding more to adjust for the rate 1 waterfall block spits out particles
								int chance = (int)(1.0F + (BlockCountRate / 120F));
								
								Block block2 = getBlock(world, xx, yy-1, zz);
								int meta2 = getBlockMetadata(world, xx, yy-1, zz);
								Block block3 = getBlock(world, xx, yy+10, zz);
								
								if ((((block2 == null || block2.getDefaultState().getMaterial() != Material.WATER) || (meta2 & 8) == 0) && (block3 != null && block3.getDefaultState().getMaterial() == Material.WATER)) || world.rand.nextInt(chance) == 0)
								{
									float range = 0.5F;
									
									EntityRotFX waterP;
									waterP = new EntityWaterfallFX(world, (double)xx + 0.5F + ((rand.nextFloat() * range) - (range/2)), (double)yy + 0.5F + ((rand.nextFloat() * range) - (range/2)), (double)zz + 0.5F + ((rand.nextFloat() * range) - (range/2)), 0D, 0D, 0D, 6D, 2);
									
									if (((block2 == null || block2.getDefaultState().getMaterial() != Material.WATER) || (meta2 & 8) == 0) && (block3 != null && block3.getDefaultState().getMaterial() == Material.WATER))
									{										
										range = 2F;
										float speed = 0.2F;
										
										for (int i = 0; i < 10; i++) {
											if (world.rand.nextInt(chance / 2) == 0) {
												waterP = new EntityWaterfallFX(world, 
														(double)xx + 0.5F + ((rand.nextFloat() * range) - (range/2)), 
														(double)yy + 0.7F + ((rand.nextFloat() * range) - (range/2)), 
														(double)zz + 0.5F + ((rand.nextFloat() * range) - (range/2)),
														((rand.nextFloat() * speed) - (speed/2)),
														((rand.nextFloat() * speed) - (speed/2)),
														((rand.nextFloat() * speed) - (speed/2)),
														2D, 3);
												waterP.setMotionY(4.5F);
												spawnQueueNormal.add(waterP);
											}
											
										}
									}
									else
									{
										waterP = new EntityWaterfallFX(world, 
												(double)xx + 0.5F + ((rand.nextFloat() * range) - (range/2)), 
												(double)yy + 0.5F + ((rand.nextFloat() * range) - (range/2)), 
												(double)zz + 0.5F + ((rand.nextFloat() * range) - (range/2)), 0D, 0D, 0D, 6D, 2);
										
										waterP.setMotionY(0.5F);
										spawnQueueNormal.add(waterP);
									}
								}
							}
							
						}
						else if (ConfigParticle.enable_fire_particle && block == Blocks.FIRE)
						{
							lastTickFoundBlocks++;
							
							if (world.rand.nextInt(Math.max(1, (spawnRate / 100))) == 0)
							{
								double speed = 0.15D;
								EntityRotFX entityfx = pm.spawnNewParticleIconFX(world, ParticleRegistry.smoke, xx + rand.nextDouble(), yy + 0.2D + rand.nextDouble() * 0.2D, zz + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D, (rand.nextDouble() - rand.nextDouble()) * speed);//pm.spawnNewParticleWindFX(worldRef, ParticleRegistry.smoke, xx + rand.nextDouble(), yy + 0.2D + rand.nextDouble() * 0.2D, zz + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D, (rand.nextDouble() - rand.nextDouble()) * speed);
								ParticleBehaviors.setParticleRandoms(entityfx, true, true);
								ParticleBehaviors.setParticleFire(entityfx);
								entityfx.setMaxAge(100+rand.nextInt(300));
								spawnQueueNormal.add(entityfx);
							}
						}
						if (ConfigParticle.enable_wind_particle && windStr > 3.0F && curDampness < 0.35F && (material == Material.GRASS || material == Material.GROUND || material == Material.SAND))
						{
							lastTickFoundBlocks++;
							float windStrAlt = Math.min(windStr * 0.6F, 1.0F);
							
							if (world.rand.nextInt((int)((spawnRate * (2.0F - windStrAlt)) / ConfigParticle.wind_particle_rate)/4) == 0)
							{
								BlockPos pos = new BlockPos(xx, yy ,zz);
								AxisAlignedBB axisalignedbb = block.getDefaultState().getBoundingBox(world, pos);
	
								//block above topmost ground
								if (canPrecipitateAt(world, pos.up()))
								{
									ParticleTexFX rain = new ParticleTexFX(world, xx + rand.nextFloat(), yy + 1.01D + axisalignedbb.maxY, zz + rand.nextFloat(), 0D, 0D, 0D, ParticleRegistry.cloud256_6);
									rain.setKillWhenUnderTopmostBlock(true);
									rain.setCanCollide(false);
									rain.windWeight = 12F;
									rain.setFacePlayer(false);
									rain.setScale((10F + (rand.nextFloat() * 10F)) * windStrAlt);
									rain.setMaxAge(60);
									rain.setMotionY(Maths.random(0.01F, 0.05F) * windStrAlt);
									rain.setGravity(-0.01F * windStrAlt);
									rain.setTicksFadeInMax(4);
									rain.setAlphaF(0);
									rain.setRBGColorF(0.4F, 0.35F, 0.25F);
									rain.setTicksFadeOutMax(4);
									rain.renderOrder = 2;
									rain.rotationYaw = rain.getWorld().rand.nextInt(360) - 180F;
									rain.rotationPitch = Maths.random(0.0F, 90.0F);
									rain.spawnAsWeatherEffect();
									ClientTickHandler.weatherManager.addEffectedParticle(rain);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the successful relative position
	 *
	 * @param world
	 * @param posOrigin
	 * @return
	 */
	public static BlockPos getRandomWorkingPos(World world, BlockPos posOrigin) {
		Collections.shuffle(listPosRandom);
		for (BlockPos posRel : listPosRandom) {
			Block blockCheck = getBlock(world, posOrigin.add(posRel));

			if (blockCheck != null && CoroUtilBlock.isAir(blockCheck)) {
				return posRel;
			}
		}

		return null;
	}
	
	@SuppressWarnings("unused")
	@SideOnly(Side.CLIENT)
	public static void tryWind(World world)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();
		
		EntityPlayer player = mc.player;

		if (player == null)
		{
			return;
		}
		
		WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
			if (weatherMan == null) return;
		WindManager windMan = weatherMan.windManager;
			if (windMan == null) return;

		Random rand = new Random();
		int handleCount = 0;
		mc.mcProfiler.startSection("effectWeather");
		//Weather Effects
		for (int i = 0; i < ClientTickHandler.weatherManager.effectedParticles.size(); i++)
		{
			Particle particle = ClientTickHandler.weatherManager.effectedParticles.get(i);
			
			if (particle == null || !particle.isAlive())
			{
				ClientTickHandler.weatherManager.effectedParticles.remove(i--);
				continue;
			}
			
			if (WindReader.getWindSpeed(world, new Vec3(player.posX, player.posY, player.posZ)) >= 0.10)
			{
				handleCount++;

				if (particle instanceof EntityRotFX)
				{
					EntityRotFX entity1 = (EntityRotFX) particle;

					if (entity1 == null)
						continue;

					if ((WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(MathHelper.floor(entity1.getPosX()), 0, MathHelper.floor(entity1.getPosZ()))).getY() - 1 < (int)entity1.getPosY() + 1) || (entity1 instanceof ParticleTexFX))
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
		mc.mcProfiler.endStartSection("effectParticle");
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
	
						if ((WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(MathHelper.floor(CoroUtilEntOrParticle.getPosX(entity1)), 0, MathHelper.floor(CoroUtilEntOrParticle.getPosZ(entity1)))).getY() - 1 < (int)CoroUtilEntOrParticle.getPosY(entity1) + 1) || (entity1 instanceof ParticleTexFX))
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
		mc.mcProfiler.endSection();
	}
	
	//Thread safe functions

	@SideOnly(Side.CLIENT)
	private static Block getBlock(World parWorld, BlockPos pos)
	{
		return getBlock(parWorld, pos.getX(), pos.getY(), pos.getZ());
	}

	@SideOnly(Side.CLIENT)
	private static Block getBlock(World parWorld, int x, int y, int z)
	{
		try
		{
			if (!parWorld.isBlockLoaded(new BlockPos(x, 0, z)))
			{
				return null;
			}

			return parWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
		}
		catch (Exception ex)
		{
			return null;
		}
	}
	
	@SideOnly(Side.CLIENT)
	private static int getBlockMetadata(World parWorld, int x, int y, int z)
	{
		if (!parWorld.isBlockLoaded(new BlockPos(x, 0, z)))
		{
			return 0;
		}

		IBlockState state = parWorld.getBlockState(new BlockPos(x, y, z));
		return state.getBlock().getMetaFromState(state);
	}
	
	public static void tickTest() {
		Minecraft mc = Minecraft.getMinecraft();
		if (miniTornado == null) {
			miniTornado = new ParticleBehaviorMiniTornado(new CoroUtil.util.Vec3(mc.player.posX, mc.player.posY, mc.player.posZ));
		}
		
		//temp
		//miniTornado.coordSource = new Vec3(mc.player.posX, mc.player.posY, mc.player.posZ - 4);
		
		
		
		if (true /*|| miniTornado.particles.size() == 0*/) {
			for (int i = 0; i < 1; i++) {
				ParticleTexFX part = new ParticleTexFX(mc.world, miniTornado.coordSource.xCoord, miniTornado.coordSource.yCoord, miniTornado.coordSource.zCoord, 0, 0, 0, ParticleRegistry.squareGrey);
				miniTornado.initParticle(part);
				miniTornado.particles.add(part);
				part.spawnAsWeatherEffect();
			}
		}
		
		miniTornado.tickUpdateList();
		
		//double x = 5;
		//double z = 5;
		//double x2 = 5;
		//double z2 = 0;
		
		//double vecX = x - x2;
		//double vecZ = z - z2;
		
		//double what = Math.atan2(vecZ, vecX);
		
		//System.out.println(Math.toDegrees(what));
	}
	
	public static void tickTestFog() {
		Minecraft mc = Minecraft.getMinecraft();
		if (particleBehaviorFog == null) {
			
			particleBehaviorFog = new ParticleBehaviorFogGround(new CoroUtil.util.Vec3(mc.player.posX, mc.player.posY, mc.player.posZ));
		} else {
			particleBehaviorFog.coordSource = new CoroUtil.util.Vec3(mc.player.posX, mc.player.posY + 0.5D, mc.player.posZ);
		}
		
		if (mc.world.getTotalWorldTime() % 300 == 0) {
			if (/*true || */particleBehaviorFog.particles.size() <= 10000) {
				for (int i = 0; i < 1; i++) {
					ParticleTexFX part = new ParticleTexFX(mc.world, particleBehaviorFog.coordSource.xCoord, particleBehaviorFog.coordSource.yCoord, particleBehaviorFog.coordSource.zCoord
							, 0, 0, 0, ParticleRegistry.cloud256);
					part.setMotionX(-1);
					part.setMotionY(0.1);
					particleBehaviorFog.initParticle(part);
					//particleBehaviorFog.particles.add(part);
					part.spawnAsWeatherEffect();
					part.windWeight = 5F;
					part.debugID = 1;
					part.setMaxAge(280);
					part.setVanillaMotionDampen(false);
					ClientTickHandler.weatherManager.addEffectedParticle(part);
				}
			}
		}
		
		particleBehaviorFog.tickUpdateList();
	}
	
	public static void tickTestSandstormParticles() {
		Minecraft mc = Minecraft.getMinecraft();
		
		//vecWOP = null;
		
		if (vecWOP == null) {
			particleBehaviorFog = new ParticleBehaviorFogGround(new CoroUtil.util.Vec3(mc.player.posX, mc.player.posY, mc.player.posZ));
			vecWOP = new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ);
		}
		
		
		
		
		
		//need circumference math to keep constant distance between particles to spawn based on size of storm
		//this also needs adjusting based on the chosed particle scale (that is based on players distance to storm)
		
		//if (!isInside) {
			for (int i = 0; i < 0; i++) {
				ParticleTexFX part = new ParticleTexFX(mc.world, vecWOP.x, vecWOP.y, vecWOP.z
						, 0, 0, 0, ParticleRegistry.cloud256);
				particleBehaviorFog.initParticle(part);
				part.setFacePlayer(false);
				
				//particleBehaviorFog.particles.add(part);
				part.spawnAsWeatherEffect();
			}
		//}
			
		//particleBehaviorFog.tickUpdateList();
		
		boolean derp = false;
		if (derp) {
			IBlockState state = mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.getEntityBoundingBox().minY-1, mc.player.posZ));
			int id = Block.getStateId(state);
			id = 12520;
			double speed = 0.2D;
			Random rand = mc.world.rand;
			mc.world.spawnParticle(EnumParticleTypes.BLOCK_DUST, mc.player.posX, mc.player.posY, mc.player.posZ, 
					(rand.nextDouble() - rand.nextDouble()) * speed, (rand.nextDouble()) * speed * 2D, (rand.nextDouble() - rand.nextDouble()) * speed, id);
		}
	}
	
	/**
	 * Manages transitioning fog densities and color from current vanilla settings to our desired settings, and vice versa
	 */
	public static void tickStormFog()
	{
		if (adjustAmountTargetPocketSandOverride > 0)
			adjustAmountTargetPocketSandOverride -= 0.01F;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		World world = mc.world;
		Vec3 posPlayer = new Vec3(mc.player.posX, 0/*mc.player.posY*/, mc.player.posZ);
		SandstormObject sandstorm = ClientTickHandler.weatherManager.getClosestSandstormByIntensity(posPlayer);
		WeatherObject storm = ClientTickHandler.weatherManager.getClosestWeather(posPlayer, 1000.0F, Stage.THUNDER.getStage(), Integer.MAX_VALUE, Type.BLIZZARD, Type.SANDSTORM);
		WindManager windMan = ClientTickHandler.weatherManager.windManager;
		
		if (sandstorm != null)
		{
			if (mc.world.getTotalWorldTime() % 40 == 0)
				isPlayerOutside = WeatherUtilEntity.isEntityOutside(mc.player);

			List<CoroUtil.util.Vec3> points = sandstorm.getSandstormAsShape();
		
			boolean inStorm = CoroUtilPhysics.isInConvexShape(posPlayer.toVec3Coro(), points);
			if (inStorm)
				distToStorm = 0;
			else
				distToStorm = CoroUtilPhysics.getDistanceToShape(posPlayer.toVec3Coro(), points);
		}
		else if (storm != null && storm instanceof StormObject)
		{
			distToStorm = CoroUtilPhysics.distBetween(posPlayer.posX, posPlayer.posZ, storm.pos.posX, storm.pos.posZ);
			fogRedTarget = 0.45F;
			fogGreenTarget = 0.55F;
			fogBlueTarget = 0.65F;
			fogDensityTarget = 0.7F;
		}
		else
			distToStorm = distToStormThreshold + 10;
		
		
		//use override if needed
		if (fogMultTarget < adjustAmountTargetPocketSandOverride)
			fogMultTarget = adjustAmountTargetPocketSandOverride;
		
		//update coroutil particle renderer fog state
		EventHandler.sandstormFogAmount = fogMult;
		
		if (sandstorm != null && fogMultTarget > 0)
		{
			fogRedTarget = 0.35F;
			fogGreenTarget = 0.22F;
			fogBlueTarget = 0.10F;
			fogDensityTarget = 0.98F;
		}
		else if (storm == null)
		{
			fogRedTarget = fogRedOrig;
			fogGreenTarget = fogGreenOrig;
			fogBlueTarget = fogBlueOrig;
			fogDensityTarget = 1.0F;
		}

		//enhance the scene further with particles around player, check for sandstorm to account for pocket sand modifying adjustAmountTarget
		if (fogMult > 0.75F && sandstorm != null) {

			Vec3 windForce = windMan.getWindForce();

			Random rand = mc.world.rand;
			int spawnAreaSize = 80;

			double sandstormParticleRateDebris = ConfigParticle.sandstorm_debris_particle_rate;
			double sandstormParticleRateDust = ConfigParticle.sandstorm_dust_particle_rate;

			float adjustAmountSmooth75 = (fogMult * 8F) - 7F;

			//extra dust
			for (int i = 0; i < ((float)30 * adjustAmountSmooth75 * sandstormParticleRateDust)/*adjustAmountSmooth * 20F * ConfigMisc.Particle_Precipitation_effect_rate*/; i++) {

				BlockPos pos = new BlockPos(
						player.posX + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
						player.posY - 2 + rand.nextInt(10),
						player.posZ + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));



				if (canPrecipitateAt(world, pos)) {
					TextureAtlasSprite sprite = ParticleRegistry.cloud256;

					ParticleSandstorm part = new ParticleSandstorm(world, pos.getX(),
							pos.getY(),
							pos.getZ(),
							0, 0, 0, sprite);
					particleBehavior.initParticle(part);

					part.setMotionX(windForce.posX);
					part.setMotionZ(windForce.posZ);

					part.setFacePlayer(false);
					part.isTransparent = true;
					part.rotationYaw = (float)rand.nextInt(360);
					part.rotationPitch = (float)rand.nextInt(360);
					part.setMaxAge(40);
					part.setGravity(0.09F);
					part.setAlphaF(0F);
					float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
					part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
					part.setScale(40);
					part.aboveGroundHeight = 0.2D;

					part.setKillOnCollide(true);

					part.windWeight = 1F;

					particleBehavior.particles.add(part);
					ClientTickHandler.weatherManager.addEffectedParticle(part);
					part.spawnAsWeatherEffect();
				}
			}

			//tumbleweed
			for (int i = 0; i < ((float)1 * adjustAmountSmooth75 * sandstormParticleRateDebris); i++)
			{

				BlockPos pos = new BlockPos(
						player.posX + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
						player.posY - 2 + rand.nextInt(10),
						player.posZ + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));



				if (canPrecipitateAt(world, pos)) {
					TextureAtlasSprite sprite = ParticleRegistry.tumbleweed;

					ParticleSandstorm part = new ParticleSandstorm(world, pos.getX(),
							pos.getY(),
							pos.getZ(),
							0, 0, 0, sprite);
					particleBehavior.initParticle(part);

					part.setMotionX(windForce.posX);
					part.setMotionZ(windForce.posZ);

					part.setFacePlayer(true);
					//part.spinFast = true;
					part.isTransparent = true;
					part.rotationYaw = (float)rand.nextInt(360);
					part.rotationPitch = (float)rand.nextInt(360);
					part.setMaxAge(80);
					part.setGravity(0.3F);
					part.setAlphaF(0F);
					float brightnessMulti = 1F - (rand.nextFloat() * 0.2F);
					//part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
					part.setRBGColorF(1F * brightnessMulti, 1F * brightnessMulti, 1F * brightnessMulti);
					part.setScale(8);
					part.aboveGroundHeight = 0.5D;
					part.collisionSpeedDampen = false;
					part.bounceSpeed = 0.03D;
					part.bounceSpeedAhead = 0.03D;

					part.setKillOnCollide(false);

					part.windWeight = 1F;

					particleBehavior.particles.add(part);
					ClientTickHandler.weatherManager.addEffectedParticle(part);
					part.spawnAsWeatherEffect();


				}
			}

			//debris
			for (int i = 0; i < ((float)8 * adjustAmountSmooth75 * sandstormParticleRateDebris)/*adjustAmountSmooth * 20F * ConfigMisc.Particle_Precipitation_effect_rate*/; i++) {
				BlockPos pos = new BlockPos(
						player.posX + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
						player.posY - 2 + rand.nextInt(10),
						player.posZ + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));



				if (canPrecipitateAt(world, pos)) {
					TextureAtlasSprite sprite = null;
					int tex = rand.nextInt(3);
					if (tex == 0) {
						sprite = ParticleRegistry.debris_1;
					} else if (tex == 1) {
						sprite = ParticleRegistry.debris_2;
					} else if (tex == 2) {
						sprite = ParticleRegistry.debris_3;
					}

					ParticleSandstorm part = new ParticleSandstorm(world, pos.getX(),
							pos.getY(),
							pos.getZ(),
							0, 0, 0, sprite);
					particleBehavior.initParticle(part);

					part.setMotionX(windForce.posX);
					part.setMotionZ(windForce.posZ);

					part.setFacePlayer(false);
					part.spinFast = true;
					part.isTransparent = true;
					part.rotationYaw = (float)rand.nextInt(360);
					part.rotationPitch = (float)rand.nextInt(360);

					part.setMaxAge(80);
					part.setGravity(0.3F);
					part.setAlphaF(0F);
					float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
					//part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
					part.setRBGColorF(1F * brightnessMulti, 1F * brightnessMulti, 1F * brightnessMulti);
					part.setScale(8);
					part.aboveGroundHeight = 0.5D;
					part.collisionSpeedDampen = false;
					part.bounceSpeed = 0.03D;
					part.bounceSpeedAhead = 0.03D;

					part.setKillOnCollide(false);

					part.windWeight = 1F;

					particleBehavior.particles.add(part);
					ClientTickHandler.weatherManager.addEffectedParticle(part);
					part.spawnAsWeatherEffect();


				}
			}
		}
	}
	
	public static boolean isFogOverridding()
	{
		Minecraft mc = Minecraft.getMinecraft();
		IBlockState iblockstate = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, mc.getRenderViewEntity(), 1F);
		if (iblockstate.getMaterial().isLiquid()) return false;
		return ConfigParticle.enable_render_distance && WeatherUtilConfig.isWeatherEnabled(ClientTickHandler.weatherManager.getDimension());
	}
	
	public static void renderWorldLast(RenderWorldLastEvent event) {
		
	}

	public static void renderTick(TickEvent.RenderTickEvent event) {

		if (ConfigMisc.toaster_pc_mode) return;

		if (event.phase == TickEvent.Phase.START) {
			Minecraft mc = FMLClientHandler.instance().getClient();
			EntityPlayer entP = mc.player;
			if (entP != null) {
				float curRainStr = SceneEnhancer.getRainStrengthAndControlVisuals(entP, true);
				curRainStr = Math.abs(curRainStr);
				mc.world.setRainStrength(curRainStr);
			}
		}
	}
	
	public static void tickSounds()
	{
		Minecraft mc = Minecraft.getMinecraft();
		Vec3 pos = new Vec3(mc.player.posX, mc.player.posY, mc.player.posZ);
		List<WeatherObject> weather = ClientTickHandler.weatherManager.getWeatherObjects();
		WeatherObject wo;
		float windSpeed = WindReader.getWindSpeed(mc.world, pos);
		int size = weather.size(), success = 0;
		
		WeatherUtilSound.tick();
		
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
}
