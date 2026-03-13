<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/client/NewSceneEnhancer.java
package net.mrbt0907.weather2remastered.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.WindReader;
import net.mrbt0907.weather2remastered.api.weather.AbstractStormObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractWindManager;
import net.mrbt0907.weather2remastered.api.weather.WeatherEnum;
import net.mrbt0907.weather2remastered.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2remastered.config.ConfigClient;
import net.mrbt0907.weather2remastered.config.ConfigMisc;
import net.mrbt0907.weather2remastered.config.ConfigStorm;
import net.mrbt0907.weather2remastered.gui.EZConfigParser;
import net.mrbt0907.weather2remastered.particle.CloudParticle;
import net.mrbt0907.weather2remastered.util.Maths;
import net.mrbt0907.weather2remastered.util.Maths.Vec;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;
import net.mrbt0907.weather2remastered.util.WeatherUtil;
import net.mrbt0907.weather2remastered.util.WeatherUtilBlock;

public class NewSceneEnhancer implements Runnable
{
	/**The instance of the scene enhancer*/
	private static final NewSceneEnhancer INSTANCE = new NewSceneEnhancer();
	private final List<BlockPos> RANDOM_POS;
	
	//----- Internal Variables -----\\
	private volatile boolean run = true;
	private int errors = 0, errorsThreaded = 0;
	
	//----- Local Variables -----\\
	public final Minecraft MC;
	/**The cached result of a weather object if it exists*/
	public volatile AbstractWeatherObject cachedSystem;
	/**The cached result of a weather object's distance to the player*/
	public volatile double cachedSystemDistance = -1.0F;
	/**The cached result of a storm object's funnel distance to the player*/
	public volatile double cachedFunnelDistance = -1.0F;
	public volatile float cachedWindSpeed, cachedWindDirection;
	protected long ticksExisted, ticksThreadExisted;
	/**Used to detect if the client is in the world to initialize the scene enhancer*/
	protected volatile boolean inGame;
	
	//----- External Information -----\\
	/**Hopefully a thread safe list which cannot be written to if canSpawnParticle is false*/
	//public final List<BlockSESnapshot> queue = new ArrayList<BlockSESnapshot>();
	/**Unknown*/
	//public final ParticleBehaviors behavior;
	public float rain, rainTarget;
	public float overcast, overcastTarget;
	/**Used to smoothen fog transitions*/
	public float fogMult;
	/**Determines close the fog will be to the player*/
	public boolean enableFog;
	/**Determines how thick the fog will be at 1.0 fogMult*/
	public float fogDensity;
	/**Determines the red color for fog*/
	public float fogRed = -1.0F, fogRedTarget = -1.0F;
	/**Determines the green color for fog*/
	public float fogGreen = -1.0F, fogGreenTarget = -1.0F;
	/**Determines the blue color for fog*/
	public float fogBlue = -1.0F, fogBlueTarget = -1.0F;
	/**Determines how wet the current environment is*/
	public float dampness;
	/**Determines how far the sky box should be from the player*/
	public float renderDistance;

	private Vec3 playerPos = new Vec3(0, 0, 0);
	NewSceneEnhancer()
	{
		MC = Minecraft.getInstance().getSelf();
		//behavior = new ParticleBehaviors(null);
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
	@SuppressWarnings("static-access")
	protected void tickThread()
	{
		if (MC.level != null && MC.player != null && EZConfigParser.isEffectsEnabled(MC.level.dimension().location().toString()))
		{
			playerPos.posX = MC.player.getX();
			playerPos.posY = MC.player.getY();
			playerPos.posZ = MC.player.getZ();
			Vec playerPos2D = new Vec(MC.player.getX(), MC.player.getZ());

			if (ticksThreadExisted % 2L == 0L && ClientTickHandler.weatherManager != null)
			{
				cachedSystem = ClientTickHandler.weatherManager != null ? ClientTickHandler.weatherManager.getClosestWeather(playerPos, renderDistance, 0, Integer.MAX_VALUE, WeatherEnum.Type.CLOUD) : null;				
			}
			
			if (cachedSystem != null)
			{
				cachedSystemDistance = cachedSystem.pos.distanceSq(playerPos2D);
				
				if (cachedSystem instanceof AbstractStormObject)
					cachedFunnelDistance = ((AbstractStormObject)cachedSystem).pos_funnel_base.distanceSq(playerPos);
			}
			else
			{
				if (cachedSystemDistance >= 0.0D)
					cachedSystemDistance = -1.0D;
				
				if (cachedFunnelDistance >= 0.0D)
					cachedFunnelDistance = -1.0D;
			}
			cachedWindDirection = WindReader.getWindAngle(MC.level, playerPos);
			cachedWindSpeed = WindReader.getWindSpeed(MC.level, playerPos);
			
			tickQueuePrecipitation();
			tickQueueFog();
			//tickQueueParticles();
			//tickQueueSounds();
		}
	}
	
	/**Finds if fog needs to be rendered and sets the target fog if needed*/
	protected void tickQueueFog()
	{
		if (cachedSystem != null)
		{
			float max = 0.29F;
//			Weather2Remastered.info("fogdensity " + fogDensity + " Was calculated from Maths.clamp (Math.max((Math.abs(" + rain + " 0.125F)) /" + " 0.69F, 0.0F) * " + max + " * " + (float) ConfigClient.fog_mult + "0.0F, " + max);
/*			if (cachedSystem instanceof SandstormObject)
			{
				fogDensity = (float) ((1.0D - Math.min(cachedSystemDistance / 300.0D, 1.0D)) * max * ConfigClient.fog_mult);
				fogRedTarget = 0.35F;
				fogGreenTarget = 0.22F;
				fogBlueTarget = 0.10F;
				return;
			}
			else*/ 
			if (rainTarget != 0.0F)
			{
				fogDensity = Maths.clamp(Math.max((Math.abs(rain + 0.125F)) / 0.69F, 0.0F) * max * (float) ConfigClient.fog_mult, 0.0F, max);
				return;
			}
		}
		fogDensity = 0.0F;
	}

	/**Finds block positions of where particles can spawn and caches the results*/
	protected void tickQueueParticles()
	{
		/*if (ticksThreadExisted % 10L == 0L)
	    {
	        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
	        IBlockState state;
	        Block block;
	        Material material;
	        int areaWidth = 20, areaHeight = (int) (areaWidth * 0.5F);
	        int posX = (int) MC.player.posX, posY = (int) MC.player.posY, posZ = (int) MC.player.posZ;
	        int meta;
	        List<BlockSESnapshot> snapshots = new ArrayList<>();

	        if (ConfigClient.enable_falling_leaves || ConfigClient.enable_waterfall_splash || ConfigClient.enable_fire_particle)
	        {
	            for (int x = posX - areaWidth; x < posX + areaWidth; x++) {
	                for (int y = posY - areaHeight; y < posY + areaHeight; y++) {
	                    for (int z = posZ - areaWidth; z < posZ + areaWidth; z++) {
	                        pos.setPos(x, y, z);
	                        state = getBlockState(pos.getX(), pos.getY(), pos.getZ());
	                        block = state.getBlock();
	                        if (block.equals(Blocks.AIR)) continue;

	                        BlockPos neighborImmutable = getRandomNeighbor(pos);
	                        boolean hasNeighbor = false;
	                        if (neighborImmutable != null) {
	                            neighborPos.setPos(neighborImmutable);
	                            hasNeighbor = true;
	                        }

	                        material = state.getMaterial();
	                        meta = block.getMetaFromState(state);

	                        if (ConfigClient.enable_falling_leaves &&
	                            (material.equals(Material.LEAVES) || material.equals(Material.VINE) || material.equals(Material.PLANTS))
	                            && hasNeighbor) {
	                            snapshots.add(new BlockSESnapshot(state, pos.toImmutable(), neighborPos.toImmutable(), 0));
	                        } else if (ConfigClient.enable_waterfall_splash && material.equals(Material.WATER)) {
	                            if ((meta & 8) != 0) {
	                                IBlockState state2 = getBlockState(x, y - 1, z);
	                                IBlockState state3 = getBlockState(x, y + 10, z);
	                                int meta2 = state2.getBlock().getMetaFromState(state2);

	                                if (((state2 == null || !state2.getMaterial().equals(Material.WATER)) || (meta2 & 8) == 0) &&
	                                    (state3 != null && state3.getMaterial() == Material.WATER)) {
	                                    snapshots.add(new BlockSESnapshot(state, pos.toImmutable(), null, 1));
	                                }
	                            }
	                        } else if (ConfigClient.enable_fire_particle && block == Blocks.FIRE) {
	                            snapshots.add(new BlockSESnapshot(state, pos.toImmutable(), null, 2));
	                        }
	                    }
	                }
	            }

	            queue.clear();
	            queue.addAll(snapshots);
	        } else if (!queue.isEmpty()) {
	            queue.clear();
	        }
	    }*/
	}

	/**Finds if precipitation needs to be rendered and sets the target rain if needed*/
	protected void tickQueuePrecipitation()
	{
		if (ClientTickHandler.weatherManager != null)
		{
			Vec3 pos = new Vec3(MC.player.position());
			rainTarget = ClientTickHandler.weatherManager.getRainTarget(pos, renderDistance + 512F);
			overcastTarget = ClientTickHandler.weatherManager.getOvercastTarget(pos, renderDistance + 512F);

			if (ConfigMisc.overcast_mode && ClientTickHandler.weatherManager.weatherID >= 1)
			{
				rainTarget = Math.max(rainTarget, ConfigStorm.min_overcast_rain);
				overcastTarget = Math.max(overcastTarget, ConfigStorm.min_overcast_rain);
			}

			if (WeatherUtil.getTemperature(MC.level, MC.player.blockPosition()) < 0.0F)
				rainTarget = -rainTarget;

			MC.level.getLevelData().setRaining(rainTarget != 0.0F);
			MC.level.setThunderLevel(overcast * 1.25F);
		}
        else reset();
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
						Thread.sleep(ConfigClient.scene_enhancer_thread_delay);
					}
					catch (Throwable e)
					{
						if (errorsThreaded < 5)
							Weather2Remastered.warn("Scene Enhancer tickThread encountered an error. Attempting " + (5 - errorsThreaded) + " more time(s)...");
						else
						{
							Weather2Remastered.warn("Scene Enhancer tickThread has failed to run successfuly. Disaling scene enhancer...");
							if (MC.player != null)
								MC.player.sendMessage(new StringTextComponent("Scene Enhancer has crashed on the scene thread! Disabling scene enhancer..."), MC.player.getUUID());
							run = false;
							reset();
						}
						
						Weather2Remastered.error(e);
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
						Weather2Remastered.warn("Scene Enhancer tickNonThread encountered an error. Attempting " + (5 - errors) + " more time(s)...");
					else
					{
						Weather2Remastered.warn("Scene Enhancer tickNonThread has failed to run successfuly. Disaling scene enhancer...");
						if (MC.player != null)
							MC.player.sendMessage(new StringTextComponent("Scene Enhancer has crashed on the client thread! Disabling scene enhancer..."), MC.player.getUUID());
						run = false;
						reset();
					}
					
					Weather2Remastered.error(e);
					errors++;
				}
		}
		/**Ran every game tick to update values based on given variables*/
		protected void tickNonThread()
		{
			if (inGame && MC.level == null)
			{
				inGame = false;
				reset();
			}
			else if (!inGame && MC.level != null && ClientTickHandler.weatherManager != null)
			{
				inGame = true;
				Weather2Remastered.debug("Scene Enhancer is online!");
			}
			
			if (inGame)
			{
				if (!MC.isPaused())
				{
					tickFog();
					tickPrecipitation();
					/*tickAmbiance();
					tickParticles();
					tickSounds();
					if (ConfigCoroUtil.foliageShaders && EventHandler.queryUseOfShaders())
					{
						if (!FoliageEnhancerShader.useThread)
							if (MC.world.getTotalWorldTime() % 40 == 0)
								FoliageEnhancerShader.tickClientThreaded();

						if (MC.world.getTotalWorldTime() % 5 == 0)
							FoliageEnhancerShader.tickClientCloseToPlayer();
					}
					*/
					//Weather2Remastered.error("Trying to tick with nothing to tick!");
				}
			}
			
		}

		protected void tickFog()
		{
			float mult = (float) ConfigClient.fog_change_rate;
			fogMult = Maths.adjust(fogMult, fogDensity, (fogMult < 0.1F ? 0.00005F : 0.001F) * mult);
				if (fogRed >= 0.0F && fogRed != fogRedTarget)
					fogRed = Maths.adjust(fogRed, fogRedTarget, 0.001F * mult);
				if (fogGreen >= 0.0F && fogGreen != fogGreenTarget)
					fogGreen = Maths.adjust(fogGreen, fogGreenTarget, 0.001F * mult);
				if (fogBlue >= 0.0F && fogBlue != fogBlueTarget)
					fogBlue = Maths.adjust(fogBlue, fogBlueTarget, 0.001F * mult);
		}

		/**Smoothly adjusts precipitation values based on the rain target*/
		protected void tickPrecipitation()
		{
			float rate = 0.0005F * Math.abs((float) ConfigClient.rain_change_mult);
			
			if (rainTarget < 0.0F && rain > 0.0F || rainTarget >= 0.0F && rain < 0.0F)
				rain = -rain;
			
			if (rain != rainTarget)
				rain = Maths.adjust(rain, rainTarget, rate);
			
			if (overcast != overcastTarget)
				overcast = Maths.adjust(overcast, overcastTarget, rate);
		}

		/**Processes all spawned particles and adds motion to each one*/
		protected void tickParticles()
		{
			WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
				if (weatherMan == null) return;
			AbstractWindManager windMan = weatherMan.windManager;
				if (windMan == null) return;

			Random rand = MC.level.random;
			//Weather Effects
			for (int i = 0; i < ClientTickHandler.weatherManager.effectedParticles.size(); i++)
			{
				Particle particle = ClientTickHandler.weatherManager.effectedParticles.get(i);
				
				if (particle == null || !particle.isAlive())
				{
					ClientTickHandler.weatherManager.effectedParticles.remove(i--);
					continue;
				}
					
				if (WindReader.getWindSpeed(MC.level, new Vec3(MC.player.getX(), MC.player.getY(), MC.player.getZ())) > 0.0)
				{
					if (particle instanceof CloudParticle)
					{
						CloudParticle entity1 = (CloudParticle) particle;
		
						if ((WeatherUtilBlock.getPrecipitationHeightSafe(MC.level, new BlockPos(MathHelper.floor(entity1.getX()), 0, MathHelper.floor(entity1.getZ()))).getY() - 1 < (int)entity1.getY() + 1)) //|| (entity1 instanceof ParticleTexFX))
						{
							/*if (entity1 instanceof IWindHandler)
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
		
							}*/
						}
		
						windMan.getEntityWindVectors(entity1, 0.05F, 5.0F);
					}
				}
			}
			//if (WeatherUtilParticle.fxLayers == null)
				//WeatherUtilParticle.getFXLayers();
			
			//Particles
		/*	for (int layer = 0; layer < WeatherUtilParticle.fxLayers.length; layer++)
			{
				for (int i = 0; i < WeatherUtilParticle.fxLayers[layer].length; i++)
				{
					for (Particle entity1 : WeatherUtilParticle.fxLayers[layer][i])
					{
						String className = entity1.getClass().getName();
						if (className.equals("net.minecraft.client.particle.Barrier") || ConfigClient.enable_vanilla_rain && className.equals("net.minecraft.client.particle.ParticleRain"))
							continue;
		
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
			}*/
		}
		public synchronized void reset()
		{
			cachedSystem = null;
			errors = 0;
			errorsThreaded = 0;
			rain = rainTarget = overcast = overcastTarget = fogDensity = fogMult = 0.0F;
			fogRed = fogRedTarget = fogGreen = fogGreenTarget = fogBlue = fogBlueTarget = -1.0F;
		//	if (WeatherUtilParticle.fxLayers == null)
			//	WeatherUtilParticle.getFXLayers();
			Weather2Remastered.debug("Scene Enhancer has been reset but WeatherUtilParticle is missing at the moment.");
		}
		
		public synchronized void enable()
		{
			if (!run)
			{
				run = true;
				reset();
				Weather2Remastered.debug("Scene Enhancer has been re-enabled");
			}
			else
				Weather2Remastered.warn("Scene Enhancer is already running, skipping enable...");
		}

		//----- Non Threaded Methods -----\\
		public void tickRender(RenderTickEvent event)
		{
			if (event.phase.equals(Phase.START) && MC.level != null)
			{
				MC.level.setRainLevel(Math.abs(rain));
				MC.level.setThunderLevel(overcast);
				//System.out.println("Overcast "+ overcast + " Rain " + rainTarget);
			}
		}
		public boolean shouldChangeFogColor()
		{
			return fogRedTarget >= 0.0F || fogGreenTarget >= 0.0F || fogBlueTarget >= 0.0F || fogRed >= 0.0F || fogGreen >= 0.0F || fogBlue >= 0.0F;
		}

		public boolean seesWeatherObject()
		{
			return cachedSystem != null;
		}
}
=======
package net.mrbt0907.weather2.client;

import java.util.*;

import net.CoroUtil.api.weather.IWindHandler;
import net.CoroUtil.config.ConfigCoroUtil;
import net.CoroUtil.util.CoroUtilEntOrParticle;
import net.extendedrenderer.EventHandler;
import net.extendedrenderer.particle.ParticleRegistry;
import net.extendedrenderer.particle.behavior.ParticleBehaviors;
import net.extendedrenderer.particle.entity.EntityRotFX;
import net.extendedrenderer.particle.entity.ParticleTexExtraRender;
import net.extendedrenderer.particle.entity.ParticleTexFX;
import net.extendedrenderer.particle.entity.ParticleTexLeafColor;
import net.extendedrenderer.render.RotatingParticleManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.block.Blocks;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WindReader;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.client.entity.particle.EntityWaterfallFX;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.client.sound.MovingSoundEX;
import net.mrbt0907.weather2.client.sound.SoundHandler;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.mixins.accessor.ParticleAccessor;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.BlockSESnapshot;
import net.mrbt0907.weather2.util.ChunkUtils;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilParticle;
import net.mrbt0907.weather2.util.Maths.Vec;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WindManager;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class NewSceneEnhancer implements Runnable
{
    private static final NewSceneEnhancer INSTANCE = new NewSceneEnhancer();
    private final List<BlockPos> RANDOM_POS;

    private volatile boolean run = true;
    private int errors = 0, errorsThreaded = 0;

    public final Minecraft MC;
    public volatile WeatherObject cachedSystem;
    public volatile double cachedSystemDistance = -1.0F;
    public volatile double cachedFunnelDistance = -1.0F;
    public volatile float cachedWindSpeed, cachedWindDirection;
    protected long ticksExisted, ticksThreadExisted;
    protected volatile boolean inGame;

    public final List<BlockSESnapshot> queue = new ArrayList<BlockSESnapshot>();
    public final ParticleBehaviors behavior;
    public float rain, rainTarget;
    public float overcast, overcastTarget;
    public float fogMult;
    public boolean enableFog;
    public float fogDensity;
    public float fogRed = -1.0F, fogRedTarget = -1.0F;
    public float fogGreen = -1.0F, fogGreenTarget = -1.0F;
    public float fogBlue = -1.0F, fogBlueTarget = -1.0F;
    public float dampness;
    public float renderDistance;

    private Vec3 playerPos = new Vec3(0, 0, 0);

    NewSceneEnhancer()
    {
        MC = Minecraft.getInstance();
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

    protected void tickThread()
    {
        if (MC.level != null && MC.player != null && EZConfigParser.isEffectsEnabled(MC.level.dimension().location()))
        {
            playerPos.posX = MC.player.getX();
            playerPos.posY = MC.player.getY();
            playerPos.posZ = MC.player.getZ();
            Vec playerPos2D = new Vec(MC.player.getX(), MC.player.getZ());

            if (ticksThreadExisted % 2L == 0L)
                cachedSystem = ClientTickHandler.weatherManager != null ? ClientTickHandler.weatherManager.getClosestWeather(playerPos, renderDistance, 0, Integer.MAX_VALUE, WeatherEnum.Type.CLOUD) : null;

            if (cachedSystem != null)
            {
                cachedSystemDistance = cachedSystem.pos.distanceSq(playerPos2D);

                if (cachedSystem instanceof StormObject)
                    cachedFunnelDistance = ((StormObject)cachedSystem).pos_funnel_base.distanceSq(playerPos);
            }
            else
            {
                if (cachedSystemDistance >= 0.0D)
                    cachedSystemDistance = -1.0D;

                if (cachedFunnelDistance >= 0.0D)
                    cachedFunnelDistance = -1.0D;
            }
            cachedWindDirection = WindReader.getWindAngle(MC.level, playerPos);
            cachedWindSpeed = WindReader.getWindSpeed(MC.level, playerPos);

            tickQueuePrecipitation();
            tickQueueFog();
            tickQueueParticles();
            tickQueueSounds();
        }
    }

    protected void tickQueueFog()
    {
        if (cachedSystem != null)
        {
            float max = 0.29F * (ConfigClient.enable_vanilla_fog ? (float) ConfigClient.fog_mult : 0.0F);
            if (cachedSystem instanceof SandstormObject)
            {
                fogDensity = (float) ((1.0D - Math.min(cachedSystemDistance / 300.0D, 1.0D)) * max * ConfigClient.fog_mult);
                fogRedTarget = 0.35F;
                fogGreenTarget = 0.22F;
                fogBlueTarget = 0.10F;
                return;
            }
            else if (rainTarget != 0.0F)
            {
                fogDensity = Maths.clamp(Math.max((Math.abs(rain + 0.125F)) / 0.69F, 0.0F) * max * (float) ConfigClient.fog_mult, 0.0F, max);
                return;
            }
        }
        fogDensity = 0.0F;
    }

    protected void tickQueuePrecipitation()
    {
        if (ClientTickHandler.weatherManager != null)
        {
            Vec3 pos = new Vec3(MC.player.blockPosition());
            rainTarget = ClientTickHandler.weatherManager.getRainTarget(pos, renderDistance + 64F);
            overcastTarget = ClientTickHandler.weatherManager.getOvercastTarget(pos, renderDistance + 64F);
            if (ConfigMisc.overcast_mode && ClientTickHandler.weatherManager.weatherID >= 1)
            {
                rainTarget = Math.max(rainTarget, ConfigStorm.min_overcast_rain);
                overcastTarget = Math.max(overcastTarget, ConfigStorm.min_overcast_rain);
            }

            if (WeatherUtil.getTemperature(MC.level, MC.player.blockPosition()) < 0.0F)
                rainTarget = -rainTarget;

            MC.level.getLevelData().setRaining(rainTarget != 0.0F);
        }
        else reset();
    }

    protected void tickQueueParticles()
    {
        if (ticksThreadExisted % 10L == 0L)
        {
            BlockPos.Mutable pos = new BlockPos.Mutable();
            BlockPos.Mutable neighborPos = new BlockPos.Mutable();
            BlockState state;
            Block block;
            Material material;
            int areaWidth = 20, areaHeight = (int) (areaWidth * 0.5F);
            int posX = (int) MC.player.getX(), posY = (int) MC.player.getY(), posZ = (int) MC.player.getZ();
            List<BlockSESnapshot> snapshots = new ArrayList<>();

            if (ConfigClient.enable_falling_leaves || ConfigClient.enable_waterfall_splash || ConfigClient.enable_fire_particle)
            {
                for (int x = posX - areaWidth; x < posX + areaWidth; x++) {
                    for (int y = posY - areaHeight; y < posY + areaHeight; y++) {
                        for (int z = posZ - areaWidth; z < posZ + areaWidth; z++) {
                            pos.set(x, y, z);
                            state = getBlockState(pos.getX(), pos.getY(), pos.getZ());

                            if (state == null) continue;

                            block = state.getBlock();
                            if (block.equals(Blocks.AIR)) continue;

                            BlockPos neighborImmutable = getRandomNeighbor(pos);
                            boolean hasNeighbor = false;
                            if (neighborImmutable != null) {
                                neighborPos.set(neighborImmutable);
                                hasNeighbor = true;
                            }

                            material = state.getMaterial();

                            if (ConfigClient.enable_falling_leaves &&
                                    (material.equals(Material.LEAVES) ||
                                            material.equals(Material.REPLACEABLE_PLANT) ||
                                            material.equals(Material.PLANT))
                                    && hasNeighbor) {
                                snapshots.add(new BlockSESnapshot(state, pos.immutable(), neighborPos.immutable(), 0));
                            } else if (ConfigClient.enable_waterfall_splash && material.equals(Material.WATER)) {
                                if (!state.getFluidState().isEmpty() && !state.getFluidState().isSource()) {
                                    BlockState state2 = getBlockState(x, y - 1, z);
                                    BlockState state3 = getBlockState(x, y + 10, z);

                                    if ((state2 == null || !state2.getMaterial().equals(Material.WATER)) &&
                                            (state3 != null && state3.getMaterial().equals(Material.WATER))) {
                                        snapshots.add(new BlockSESnapshot(state, pos.immutable(), null, 1));
                                    }
                                }
                            } else if (ConfigClient.enable_fire_particle && block == Blocks.FIRE) {
                                snapshots.add(new BlockSESnapshot(state, pos.immutable(), null, 2));
                            }
                        }
                    }
                }

                queue.clear();
                queue.addAll(snapshots);
            } else if (!queue.isEmpty()) {
                queue.clear();
            }
        }
    }

    protected void tickQueueSounds()
    {

    }

    public void tickRender(RenderTickEvent event)
    {
        if (event.phase.equals(Phase.START) && MC.level != null)
        {
            MC.level.setRainLevel(Math.abs(overcast));
            MC.level.setThunderLevel(overcast);
        }
    }

    protected void tickNonThread()
    {
        if (inGame && MC.level == null)
        {
            inGame = false;
            reset();
        }
        else if (!inGame && MC.level != null && ClientTickHandler.weatherManager != null)
        {
            inGame = true;
        }

        if (inGame)
        {
            if (!MC.isPaused())
            {
                tickFog();
                tickPrecipitation();
                tickAmbiance();
                tickParticles();
                tickSounds();
                SoundHandler.tick();
                renderDistance = ConfigClient.enable_extended_render_distance ? (float) ConfigClient.extended_render_distance : MC.options.renderDistance * 4;
                if (ConfigCoroUtil.foliageShaders && EventHandler.queryUseOfShaders())
                {
                    if (!FoliageEnhancerShader.useThread)
                        if (MC.level.getGameTime() % 40 == 0)
                            FoliageEnhancerShader.tickClientThreaded();

                    if (MC.level.getGameTime() % 5 == 0)
                        FoliageEnhancerShader.tickClientCloseToPlayer();
                }
            }
        }
    }

    protected void tickPrecipitation()
    {
        float rate = 0.0005F * Math.abs((float) ConfigClient.rain_change_mult);

        if (rainTarget < 0.0F && rain > 0.0F || rainTarget >= 0.0F && rain < 0.0F)
            rain = -rain;

        if (rain != rainTarget)
            rain = Maths.adjust(rain, rainTarget, rate);

        if (overcast != overcastTarget)
            overcast = Maths.adjust(overcast, overcastTarget, rate);

        if (!ConfigClient.enable_vanilla_rain && ConfigClient.precipitation_particle_rate > 0.0D && rain != 0.0F)
        {
            ClientWorld clientWorld = MC.level instanceof ClientWorld ? (ClientWorld) MC.level : null;
            if (clientWorld == null) return;

            ParticleTexFX particle;
            BlockPos pos, posPrecip;
            boolean snowing = rain < 0.0F;
            int particleCount = (int) Math.abs(rain * 15.0F * ConfigClient.precipitation_particle_rate),
                    particleCountSplash, particleCountSheet;
            int spawnArea = 20;

            if (particleCount > 200)
                particleCount = 200;

            particleCount += 5;

            particleCountSheet = ConfigClient.enable_heavy_precipitation && rain > 0.5F ? (int) (particleCount * 0.2F) : 0;
            particleCountSplash = ConfigClient.enable_precipitation_splash ? (int)(particleCount * 4) : 0;

            if (snowing)
            {
                spawnArea = 50;
                for (int i = 0; i < particleCount; i++)
                {
                    pos = new BlockPos(MC.player.getX() + MC.level.random.nextInt(spawnArea) - MC.level.random.nextInt(spawnArea), MC.player.getY() - 5 + MC.level.random.nextInt(25), MC.player.getZ() + MC.level.random.nextInt(spawnArea) - MC.level.random.nextInt(spawnArea));
                    posPrecip = MC.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos);
                    if (posPrecip.getY() <= pos.getY())
                    {
                        ParticleTexExtraRender snow = new ParticleTexExtraRender(clientWorld, pos.getX(), pos.getY(), pos.getZ(),
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
                        snow.windWeight = 2.5F;
                        snow.setMaxAge(40);
                        snow.setFacePlayer(false);
                        snow.setTicksFadeInMax(5);
                        snow.setAlphaF(0);
                        snow.setTicksFadeOutMax(5);
                        snow.rotationYaw = snow.getWorld().random.nextInt(360) - 180F;
                        spawnParticle(snow, false);
                    }
                }
            }
            else
            {
                int type = rain > 0.6F ? 2 : rain > 0.3F ? 1 : 0;
                for (int i = 0; i < particleCount; i++)
                {
                    pos = new BlockPos(MC.player.getX() + MC.level.random.nextInt(spawnArea) - MC.level.random.nextInt(spawnArea), MC.player.getY() - 5 + MC.level.random.nextInt(25), MC.player.getZ() + MC.level.random.nextInt(spawnArea) - MC.level.random.nextInt(spawnArea));
                    posPrecip = MC.level.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING, pos);
                    if (posPrecip.getY() <= pos.getY())
                    {
                        particle = new ParticleTexExtraRender(clientWorld, pos.getX(), pos.getY(), pos.getZ(), 0D, 0D, 0D, type == 2 ? net.mrbt0907.weather2.registry.ParticleRegistry.rainHeavy : type == 1 ? ParticleRegistry.rain_white : net.mrbt0907.weather2.registry.ParticleRegistry.rainLight);
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
                            particle.extraYRotation = MC.level.random.nextInt(360) - 180F;
                        }

                        particle.setScale(type == 2 ? 6F : 2.0F);
                        particle.isTransparent = true;
                        particle.setGravity(2.5F);
                        particle.setMaxAge(50);
                        particle.setTicksFadeInMax(5);
                        particle.setAlphaF(0);
                        particle.rotationYaw = MC.level.random.nextInt(360) - 180F;
                        particle.setMotionY(-0.5D);
                        particle.renderOrder = 2;
                        spawnParticle(particle, false);
                    }
                }

                spawnArea = 50;
                for (int i = 0; i < particleCountSplash; i++)
                {
                    pos = new BlockPos(MC.player.getX() + MC.level.random.nextInt(spawnArea) - (spawnArea * 0.5F), MC.player.getY() - 5 + MC.level.random.nextInt(15), MC.player.getZ() + MC.level.random.nextInt(spawnArea) - (spawnArea * 0.5F));
                    pos = MC.level.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING, pos).below();
                    BlockState state = MC.level.getBlockState(pos);

                    VoxelShape shape = state.getShape(MC.level, pos);
                    if (shape.isEmpty()) continue;

                    AxisAlignedBB axisalignedbb = shape.bounds();

                    if (MC.level.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING, pos).getY() <= pos.above().getY())
                    {
                        particle = new ParticleTexFX(clientWorld, pos.getX() + MC.level.random.nextFloat(), pos.getY() + 0.01D + axisalignedbb.maxY, pos.getZ() + MC.level.random.nextFloat(), 0D, 0D, 0D, net.mrbt0907.weather2.registry.ParticleRegistry.rainSplash);
                        particle.setKillWhenUnderTopmostBlock(true);
                        particle.setCanCollide(false);
                        particle.killWhenUnderCameraAtLeast = 5;
                        boolean upward = MC.level.random.nextBoolean();

                        particle.windWeight = 20F;
                        particle.setFacePlayer(upward);
                        particle.setScale(1F + MC.level.random.nextFloat());
                        particle.setMaxAge(15);
                        particle.setGravity(-0.0F);
                        particle.setTicksFadeInMax(0);
                        particle.setAlphaF(0);
                        particle.setTicksFadeOutMax(4);
                        particle.rotationYaw = MC.level.random.nextInt(360) - 180F;
                        particle.rotationPitch = 90;
                        particle.setMotionY(0D);
                        particle.setMotionX((MC.level.random.nextFloat() - 0.5F) * 0.01F);
                        particle.setMotionZ((MC.level.random.nextFloat() - 0.5F) * 0.01F);
                        spawnParticle(particle, false);
                    }
                }

                spawnArea = 60;
                for (int i = 0; i < particleCountSheet; i++)
                {
                    pos = new BlockPos(MC.player.getX() + MC.level.random.nextInt(spawnArea) - (spawnArea * 0.5F), MC.player.getY() - 5 + MC.level.random.nextInt(35), MC.player.getZ() + MC.level.random.nextInt(spawnArea) - (spawnArea * 0.5F));
                    posPrecip = MC.level.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING, pos);
                    if (posPrecip.getY() <= pos.getY())
                    {
                        particle = new ParticleTexExtraRender(clientWorld,
                                pos.getX() + MC.level.random.nextFloat(),
                                pos.getY() - 1 + 0.01D,
                                pos.getZ() + MC.level.random.nextFloat(),
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
                        particle.setScale(200F + (MC.level.random.nextFloat() * 3F));
                        particle.setMaxAge(60);
                        particle.setGravity(0.35F);
                        particle.setTicksFadeInMax(20);
                        particle.setAlphaF(0);
                        particle.setTicksFadeOutMax(20);
                        particle.rotationYaw = MC.level.random.nextInt(360) - 180F;
                        particle.rotationPitch = 90;
                        particle.setMotionY(-0.3D);
                        particle.setMotionX((MC.level.random.nextFloat() - 0.5F) * 0.01F);
                        particle.setMotionZ((MC.level.random.nextFloat() - 0.5F) * 0.01F);
                        spawnParticle(particle, false);
                    }
                }
            }
        }
    }

    protected void tickFog()
    {
        float mult = (float) ConfigClient.fog_change_rate;
        fogMult = Maths.adjust(fogMult, fogDensity, (fogMult < 0.1F ? 0.00005F : 0.001F) * mult);
        if (fogRed >= 0.0F && fogRed != fogRedTarget)
            fogRed = Maths.adjust(fogRed, fogRedTarget, 0.001F * mult);
        if (fogGreen >= 0.0F && fogGreen != fogGreenTarget)
            fogGreen = Maths.adjust(fogGreen, fogGreenTarget, 0.001F * mult);
        if (fogBlue >= 0.0F && fogBlue != fogBlueTarget)
            fogBlue = Maths.adjust(fogBlue, fogBlueTarget, 0.001F * mult);
    }

    protected void tickAmbiance()
    {
        ClientWorld clientWorld = MC.level instanceof ClientWorld ? (ClientWorld) MC.level : null;
        if (clientWorld == null) return;

        List<BlockSESnapshot> snapshots = new ArrayList<BlockSESnapshot>(queue);
        int particleCount = (int) (160.0D / Maths.clamp(ConfigClient.ambient_particle_rate, 0.0001D, 159.0D));

        for (BlockSESnapshot snapshot : snapshots)
        {
            if (MC.level.random.nextInt(particleCount) != 0) continue;
            EntityRotFX particle;
            switch (snapshot.type)
            {
                case 0:
                    particle = new ParticleTexLeafColor(clientWorld, snapshot.x, snapshot.y, snapshot.z, 0D, 0D, 0D, ParticleRegistry.leaf);
                    particle.setPos(snapshot.x + snapshot.rX * (0.04D + Maths.random(0.75D)), snapshot.y + snapshot.rY * (0.04D + Maths.random(0.75D)), snapshot.z + snapshot.rZ * (0.04D + Maths.random(0.75D)));
                    particle.setPrevPosX(particle.getPosX());
                    particle.setPrevPosY(particle.getPosY());
                    particle.setPrevPosZ(particle.getPosZ());
                    particle.setMotionX(0);
                    particle.setMotionY(0);
                    particle.setMotionZ(0);
                    particle.setGravity(0.05F);
                    particle.setCanCollide(true);
                    particle.setKillOnCollide(false);
                    particle.windWeight = 10.0F;
                    particle.collisionSpeedDampen = false;
                    particle.killWhenUnderCameraAtLeast = 20;
                    particle.killWhenFarFromCameraAtLeast = 20;
                    particle.isTransparent = false;
                    particle.rotationYaw = MC.level.random.nextInt(360);
                    particle.rotationPitch = MC.level.random.nextInt(360);
                    particle.updateQuaternion(null);
                    spawnParticle(particle, false);
                    break;
                case 1:
                    for (int i = 0; i < 10; i++)
                    {
                        particle = new EntityWaterfallFX(clientWorld,
                                (double)snapshot.x + 0.5F + ((MC.level.random.nextFloat() * 2F) - (1)),
                                (double)snapshot.y + 0.7F + ((MC.level.random.nextFloat() * 2F) - (1)),
                                (double)snapshot.z + 0.5F + ((MC.level.random.nextFloat() * 2F) - (1)),
                                ((MC.level.random.nextFloat() * 0.2F) - (0.2F/2)),
                                ((MC.level.random.nextFloat() * 0.2F) - (0.2F/2)),
                                ((MC.level.random.nextFloat() * 0.2F) - (0.2F/2)),
                                2D, 3);
                        particle.setMotionY(4.5F);
                        spawnParticle(particle, true);
                    }
                    break;
                case 2:
                    double speed = 0.15D;
                    particle = new ParticleTexFX(clientWorld, snapshot.x + MC.level.random.nextDouble(), snapshot.y + 0.2D + MC.level.random.nextDouble() * 0.2D, snapshot.z + MC.level.random.nextDouble(), (MC.level.random.nextDouble() - MC.level.random.nextDouble()) * speed, 0.03D, (MC.level.random.nextDouble() - MC.level.random.nextDouble()) * speed, ParticleRegistry.smoke);
                    ParticleBehaviors.setParticleRandoms(particle, true, true);
                    ParticleBehaviors.setParticleFire(particle);
                    particle.setMaxAge(100 + MC.level.random.nextInt(300));
                    spawnParticle(particle, true);
                    break;
            }
        }

        snapshots = null;
    }

    protected void tickParticles()
    {
        WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
        if (weatherMan == null) return;
        WindManager windMan = weatherMan.windManager;
        if (windMan == null) return;

        Random rand = MC.level.random;
        for (int i = 0; i < ClientTickHandler.weatherManager.effectedParticles.size(); i++)
        {
            Particle particle = ClientTickHandler.weatherManager.effectedParticles.get(i);

            if (particle == null || !particle.isAlive())
            {
                ClientTickHandler.weatherManager.effectedParticles.remove(i--);
                continue;
            }

            if (WindReader.getWindSpeed(MC.level, new Vec3(MC.player.getX(), MC.player.getY(), MC.player.getZ())) > 0.0)
            {
                if (particle instanceof EntityRotFX)
                {
                    EntityRotFX entity1 = (EntityRotFX) particle;

                    if ((WeatherUtilBlock.getPrecipitationHeightSafe(MC.level, new BlockPos(MathHelper.floor(entity1.getPosX()), 0, MathHelper.floor(entity1.getPosZ()))).getY() - 1 < (int)entity1.getPosY() + 1) || (entity1 instanceof ParticleTexFX))
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
                                entity1.setMotionY(entity1.getMotionY() + rand.nextDouble() * 0.02 * ((ParticleTexFX) entity1).getGravity());
                            entity1.setMotionY(entity1.getMotionY() - 0.01F * ((ParticleTexFX) entity1).getGravity());
                        }
                    }

                    windMan.getEntityWindVectors(entity1, 0.05F, 5.0F);
                }
            }
        }

        if (WeatherUtilParticle.particles == null)
            WeatherUtilParticle.getFXLayers();

        if (WeatherUtilParticle.particles != null)
        {
            for (Queue<Particle> queue : WeatherUtilParticle.particles.values())
            {
                for (Particle entity1 : new ArrayList<>(queue))
                {
                    String className = entity1.getClass().getName();
                    if (className.equals("net.minecraft.client.particle.BarrierParticle") ||
                            ConfigClient.enable_vanilla_rain && className.equals("net.minecraft.client.particle.RainParticle"))
                        continue;

                    if ((WeatherUtilBlock.getPrecipitationHeightSafe(MC.level, new BlockPos(MathHelper.floor(CoroUtilEntOrParticle.getPosX(entity1)), 0, MathHelper.floor(CoroUtilEntOrParticle.getPosZ(entity1)))).getY() - 1 < (int)CoroUtilEntOrParticle.getPosY(entity1) + 1) || (entity1 instanceof ParticleTexFX))
                    {
                        if ((entity1 instanceof FlameParticle))
                        {
                            if (windMan.windSpeed >= 0.20) {
                                ((ParticleAccessor) entity1).setAge(((ParticleAccessor) entity1).getAge() + 1);
                            }
                        }
                        else if (entity1 instanceof IWindHandler)
                        {
                            if (((IWindHandler)entity1).getParticleDecayExtra() > 0 && WeatherUtilParticle.getParticleAge(entity1) % 2 == 0)
                            {
                                WeatherUtilParticle.setParticleAge(entity1, WeatherUtilParticle.getParticleAge(entity1) + ((IWindHandler)entity1).getParticleDecayExtra());
                            }
                        }
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

    protected void tickSounds()
    {
        if (ClientTickHandler.weatherManager == null) return;

        SoundHandler.pruneStaleSounds();

        Vec3 pos = new Vec3(MC.player.getX(), MC.player.getY(), MC.player.getZ());
        List<WeatherObject> weather = ClientTickHandler.weatherManager.getWeatherObjects();
        float windSpeed = WindReader.getWindSpeed(MC.level, pos);

        if (!ConfigClient.enable_vanilla_rain)
        {
            if (rain > 0.0F)
            {
                int type = rain > 0.6F ? 2 : rain > 0.3F ? 1 : 0;
                SoundEvent rainSound = type == 0 ? SoundRegistry.rainLight.get() : SoundEvents.WEATHER_RAIN;
                float rainVol = type == 0 ? 1.0F : 0.1F + (rain * (float) ConfigVolume.rain);
                float rainPitch = 1.0F - (rain * 0.1F);

                ISound active = SoundHandler.getSound(rainSound, 1);
                if (active instanceof MovingSoundEX)
                    ((MovingSoundEX) active).adjustVolume(rainVol);
                else
                    SoundHandler.playStaticSound(rainSound, SoundCategory.WEATHER, 1, rainVol, rainPitch);
            }
            else
            {
                SoundHandler.stopSound(SoundRegistry.rainLight.get(), 1);
                SoundHandler.stopSound(SoundEvents.WEATHER_RAIN, 1);
            }
        }

        int success = 0;
        for (int i = 0; i < weather.size() && success < 4; i++)
        {
            WeatherObject wo = weather.get(i);

            if (wo instanceof StormObject && success < 3)
            {
                StormObject storm = (StormObject) wo;
                if (storm.isDeadly())
                {
                    boolean violent = storm.isViolent || storm.stage > Stage.TORNADO.getStage() + 2;

                    if (!violent && cachedFunnelDistance < 224.0D)
                    {
                        SoundHandler.playMovingSound(storm.pos_funnel_base, SoundRegistry.windFast.get(),
                                SoundCategory.WEATHER, 2, (float) ConfigVolume.cyclone,
                                storm.isViolent ? 0.7F : 0.8F, storm.funnelSize + 350.0F);

                        if (wo.type.equals(Type.TORNADO))
                            SoundHandler.playMovingSound(storm.pos_funnel_base, SoundRegistry.debris.get(),
                                    SoundCategory.WEATHER, 2, (float) ConfigVolume.debris,
                                    1.0F, storm.funnelSize + 150.0F);

                        success += 2;
                    }
                    else if (violent && cachedFunnelDistance < 1280.0D)
                    {
                        SoundHandler.playMovingSound(storm.pos_funnel_base, SoundRegistry.storm.get(),
                                SoundCategory.WEATHER, 1,
                                (float) Maths.clamp(ConfigVolume.cyclone * ((ConfigStorm.max_storm_size / 3.0) / (cachedFunnelDistance + 0.1)), 0.05, 1.35),
                                storm.isViolent ? 0.9F : 1.0F, wo.size * 2.0F);
                        success++;
                    }
                }
            }
            else if (wo instanceof SandstormObject && wo.pos.distanceSq(pos) - wo.size + 100.0D <= 0.0D)
            {
                SoundHandler.playMovingSound(wo, SoundRegistry.sandstorm.get(),
                        SoundCategory.WEATHER, 2, (float) ConfigVolume.cyclone, 1.0F, wo.size + 100.0F);
                success++;
            }
        }

        if (success == 0)
        {
            SoundHandler.stopSound(SoundRegistry.windFast.get(), 2);
            SoundHandler.stopSound(SoundRegistry.debris.get(), 2);
            SoundHandler.stopSound(SoundRegistry.storm.get(), 1);
            SoundHandler.stopSound(SoundRegistry.sandstorm.get(), 2);
        }

        if (windSpeed > 6.5F)
        {
            SoundHandler.stopSound(SoundRegistry.wind.get(), 0);
            if (cachedFunnelDistance < 320.0D)
                SoundHandler.playStaticSound(SoundRegistry.windFast.get(), SoundCategory.WEATHER, 0, (float) ConfigVolume.wind, 1.0F);
            else if (cachedFunnelDistance < 690.0D)
                SoundHandler.playStaticSound(SoundRegistry.windFast.get(), SoundCategory.WEATHER, 0, (float) ConfigVolume.wind * 0.35F, 1.0F);
            else
                SoundHandler.stopSound(SoundRegistry.windFast.get(), 0);
        }
        else if (windSpeed > 1.4F)
        {
            SoundHandler.stopSound(SoundRegistry.windFast.get(), 0);
            SoundHandler.stopSound(SoundRegistry.storm.get(), 0);
            SoundHandler.playStaticSound(SoundRegistry.wind.get(), SoundCategory.WEATHER, 0, (float) ConfigVolume.wind, 1.0F);
        }
        else
        {
            SoundHandler.stopSound(SoundRegistry.wind.get(), 0);
            SoundHandler.stopSound(SoundRegistry.windFast.get(), 0);
        }
    }

    public void spawnParticle(Particle particle, boolean isNormalEffect)
    {
        if (isNormalEffect)
            MC.particleEngine.add(particle);
        else
        {
            if (particle instanceof EntityRotFX)
                ((EntityRotFX) particle).spawnAsWeatherEffect();
            ClientTickHandler.weatherManager.addEffectedParticle(particle);
        }
    }

    public boolean seesWeatherObject()
    {
        return cachedSystem != null;
    }

    public boolean shouldChangeFogColor()
    {
        return fogRedTarget >= 0.0F || fogGreenTarget >= 0.0F || fogBlueTarget >= 0.0F || fogRed >= 0.0F || fogGreen >= 0.0F || fogBlue >= 0.0F;
    }

    public boolean shouldChangeFog()
    {
        return fogMult > 0.0F;
    }

    public WeatherObject getWeatherObject()
    {
        return cachedSystem;
    }

    private BlockState getBlockState(int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x, y, z);

        if (MC.level.isLoaded(pos))
        {
            return ChunkUtils.getBlockState(MC.level, pos);
        }

        return null;
    }

    private BlockPos getRandomNeighbor(BlockPos pos)
    {
        BlockPos neighborPos;
        BlockState state;
        int x, y, z;

        Collections.shuffle(RANDOM_POS);
        for (BlockPos randPos : RANDOM_POS)
        {
            neighborPos = new BlockPos(
                    pos.getX() + randPos.getX(),
                    pos.getY() + randPos.getY(),
                    pos.getZ() + randPos.getZ()
            );
            x = neighborPos.getX();
            y = neighborPos.getY();
            z = neighborPos.getZ();
            state = getBlockState(x, y, z);

            if (state != null && state.getBlock().equals(Blocks.AIR))
                return randPos;
        }

        return null;
    }

    @Override
    public void run()
    {
        while (true)
        {
            if (run)
                try
                {
                    tickThread();
                    ticksThreadExisted++;
                    errorsThreaded = 0;
                    Thread.sleep(ConfigClient.scene_enhancer_thread_delay);
                }
                catch (Throwable e)
                {
                    if (errorsThreaded < 5)
                        Weather2.warn("Scene Enhancer tickThread encountered an error. Attempting " + (5 - errorsThreaded) + " more time(s)...");
                    else
                    {
                        Weather2.warn("Scene Enhancer tickThread has failed to run successfuly. Disaling scene enhancer...");
                        if (MC.player != null)
                            MC.player.sendMessage(new StringTextComponent("Scene Enhancer has crashed on the scene thread! Disabling scene enhancer..."), MC.player.getUUID());
                        run = false;
                        reset();
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
                    if (MC.player != null)
                        MC.player.sendMessage(new StringTextComponent("Scene Enhancer has crashed on the client thread! Disabling scene enhancer..."), MC.player.getUUID());
                    run = false;
                    reset();
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
        fogRed = fogRedTarget = fogGreen = fogGreenTarget = fogBlue = fogBlueTarget = -1.0F;
        if (WeatherUtilParticle.particles == null)
            WeatherUtilParticle.getFXLayers();
    }

    public synchronized void enable()
    {
        if (!run)
        {
            run = true;
            reset();
        }
        else
            Weather2.warn("Scene Enhancer is already running, skipping enable...");
    }
}
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/client/NewSceneEnhancer.java
