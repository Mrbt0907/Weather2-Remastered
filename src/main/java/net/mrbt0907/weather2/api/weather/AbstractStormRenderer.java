package net.mrbt0907.weather2.api.weather;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import CoroUtil.config.ConfigCoroUtil;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.behavior.ParticleBehaviorFog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.client.entity.particle.ExtendedEntityRotFX;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.weather.storm.StormObject;

@SideOnly(Side.CLIENT)
public abstract class AbstractStormRenderer
{
	protected ParticleBehaviorFog particleBehaviorFog;
	public StormObject storm;
	public ResourceLocation id;
	public int particlesLeft;
	private final List<Particle> particles = new ArrayList<Particle>();
	private int particleLimit = 1;
	private static long delta, worldDelta;
	public static final List<String> renderDebugInfo = new ArrayList<String>();
	
	public AbstractStormRenderer(StormObject storm)
	{
		refreshParticleLimit();
		this.storm = storm;
	}
	
	public final void tick()
	{
		int attempts = 0;
		
		if (particleBehaviorFog == null)
			particleBehaviorFog = new ParticleBehaviorFog(storm.pos.toVec3Coro());
		else if (!Minecraft.getMinecraft().isSingleplayer() || !(Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu))
				particleBehaviorFog.tickUpdateList();
		
		Iterator<Particle> particles = this.particles.iterator();
		Particle particle;		
		while (particles.hasNext())
		{
			particle = particles.next();
			
			if (!particle.isAlive())
				particles.remove();
		}
		particles = null;
		
		particlesLeft = particleLimit - this.particles.size();
		
		if (storm != null)
		{
			delta = System.nanoTime();
			while (attempts > -1)
				try
				{
					onTick(ClientTickHandler.weatherManager);
					attempts = -1;
				}
				catch(Exception e)
				{
					attempts++;
					
					if (attempts < 3)
					{
						Weather2.warn("Particle renderer's onTick() has encountered an error. Retrying...");
						e.printStackTrace();
					}
					else
					{
						Weather2.warn("Particle renderer's onTick() has failed to run correctly. Disabling particle renderer...");
						e.printStackTrace();
						ConfigParticle.particle_renderer = "-1";
						WeatherAPI.refreshRenders(false);
						attempts = -1;
					}
				}
			delta = (long)((System.nanoTime() - delta) * 0.001F);
			
			if (worldDelta != ClientTickHandler.weatherManager.getWorld().getTotalWorldTime())
			{
				worldDelta = ClientTickHandler.weatherManager.getWorld().getTotalWorldTime();
				renderDebugInfo.clear();
				renderDebugInfo.add("Renderer: " + String.valueOf(WeatherAPI.getParticleRendererId()));
				renderDebugInfo.add("Delta: " + delta + "si");
				if (ConfigParticle.max_particles > 0)
				{
					renderDebugInfo.add("Particle Count: " + this.particles.size() + "/" + particleLimit);
					renderDebugInfo.add("Global Particle Count: " + ClientTickHandler.weatherManager.getParticleCount() + "/" + ConfigParticle.max_particles);
				}
				else
				{
					renderDebugInfo.add("Particle Count: " + this.particles.size());
					renderDebugInfo.add("Global Particle Count: " + ClientTickHandler.weatherManager.getParticleCount());
				}
				List<String> extraDebugInfo = null;
				
				attempts = 0;
				while (attempts > -1)
					try
					{
						extraDebugInfo = onDebugInfo();
						attempts = -1;
					}
					catch(Exception e)
					{
						attempts++;
						
						if (attempts < 3)
						{
							Weather2.warn("Particle renderer's onDebugInfo() has encountered an error. Retrying...");
							e.printStackTrace();
						}
						else
						{
							Weather2.warn("Particle renderer's onTick() has failed to run correctly. Disabling particle renderer...");
							e.printStackTrace();
							ConfigParticle.particle_renderer = "-1";
							WeatherAPI.refreshRenders(false);
							attempts = -1;
						}
					}
				
				if (extraDebugInfo != null)
				{
					renderDebugInfo.add("-------   --------");
					renderDebugInfo.addAll(extraDebugInfo);
				}
			}
		}
		
	}
	
	public abstract void onTick(WeatherManagerClient manager);
	public abstract void onParticleLimitRefresh(WeatherManagerClient manager, int newParticleLimit);
	public abstract List<String> onDebugInfo();
	
	public abstract void cleanupRenderer();
	
	public final void cleanup()
	{
		particles.clear();
		cleanupRenderer();
		if (particleBehaviorFog != null)
		{
			if (particleBehaviorFog.particles != null)
				particleBehaviorFog.particles.clear();
			
			particleBehaviorFog = null;
		}
	}
	
	public final ExtendedEntityRotFX spawnParticle(double x, double y, double z, int parRenderOrder)
	{
		return spawnParticle(x, y, z, parRenderOrder, ConfigCoroUtil.optimizedCloudRendering ? net.mrbt0907.weather2.registry.ParticleRegistry.cloud32 : net.mrbt0907.weather2.registry.ParticleRegistry.cloud256);
	}
	
	public final ExtendedEntityRotFX spawnParticle(double x, double y, double z, int parRenderOrder, TextureAtlasSprite tex)
	{
		if (!canSpawnParticle()) return null;
		
		double speed = 0D;
		Random rand = new Random();
		ExtendedEntityRotFX entityfx = new ExtendedEntityRotFX(ClientTickHandler.weatherManager.getWorld(), x, y, z, (rand.nextDouble() - rand.nextDouble()) * speed, 0.0D, (rand.nextDouble() - rand.nextDouble()) * speed, tex);
		entityfx.pb = particleBehaviorFog;
		entityfx.renderOrder = parRenderOrder;
		particleBehaviorFog.initParticle(entityfx);
		
		entityfx.setCanCollide(false);
		entityfx.callUpdatePB = false;
		
		if (storm.stage == Stage.NORMAL.getStage())
			entityfx.setMaxAge(300 + rand.nextInt(100));
		else
			entityfx.setMaxAge((storm.size/2) + rand.nextInt(100));
		
		//pieces that move down with funnel need render order shift, also only for relevant storm formations
		if (entityfx.getEntityId() % 20 < 5 && storm.isSevere())
		{
			entityfx.renderOrder = 1;
			entityfx.setMaxAge((storm.size) + rand.nextInt(100));
		}

		//temp?
		if (ConfigCoroUtil.optimizedCloudRendering)
			entityfx.setMaxAge(400);
		
		ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
		particleBehaviorFog.particles.add(entityfx);
		particles.add(entityfx);
		if (ClientTickHandler.weatherManager != null)
			ClientTickHandler.weatherManager.addWeatherParticle(entityfx);
		
		particlesLeft--;
		return entityfx;
	}
	
	public final void refreshParticleLimit()
	{
		particleLimit = ClientTickHandler.weatherManager.getParticleLimit();
		int attempts = 0;
		while (attempts > -1)
			try
			{
				onParticleLimitRefresh(ClientTickHandler.weatherManager, particleLimit);
				attempts = -1;
			}
			catch(Exception e)
			{
				attempts++;
				
				if (attempts < 3)
				{
					Weather2.warn("Particle renderer's onParticleLimitRefresh() has encountered an error. Retrying...");
					e.printStackTrace();
				}
				else
				{
					Weather2.warn("Particle renderer's onParticleLimitRefresh() has failed to run correctly. Disabling particle renderer...");
					e.printStackTrace();
					ConfigParticle.particle_renderer = "-1";
					WeatherAPI.refreshRenders(false);
					attempts = -1;
				}
			}
		
	}
	
	public final boolean canSpawnParticle()
	{
		return particleLimit >= particles.size();
	}
}
