package net.mrbt0907.weather2.client.weather.tornado;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import CoroUtil.config.ConfigCoroUtil;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.api.weather.AbstractStormRenderer;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.client.SceneEnhancer;
import net.mrbt0907.weather2.client.entity.particle.ExtendedEntityRotFX;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.util.ChunkUtils;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.StormObject.StormType;

public class NormalStormRenderer extends AbstractStormRenderer
{
	/**List of particles that make up the clouds above*/
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesCloud,
	/**List of funnel particles that a tornado makes*/
	listParticlesFunnel,
	/**List of ground particles that a storm generates*/
	listParticlesGround,
	/**List of custom rain particles that a storm generates*/
	listParticlesRain;
	
	public int particleLimitCloud, particleLimitFunnel, particleLimitGround, particleLimitRain;
	
	public NormalStormRenderer(StormObject storm)
	{
		super(storm);
		listParticlesCloud = new ArrayList<EntityRotFX>();
		listParticlesGround = new ArrayList<EntityRotFX>();
		listParticlesRain = new ArrayList<EntityRotFX>();
		listParticlesFunnel = new ArrayList<EntityRotFX>();
	}

	@Override
	public void onTick(WeatherManagerClient manager)
	{
		EntityPlayer entP = Minecraft.getMinecraft().player;
		IBlockState state = ConfigCoroUtil.optimizedCloudRendering ? Blocks.AIR.getDefaultState() : ChunkUtils.getBlockState(manager.getWorld(), (int) storm.pos_funnel_base.posX, (int) storm.pos_funnel_base.posY - 1, (int) storm.pos_funnel_base.posZ);
		Material material = state.getMaterial();
		double maxRenderDistance = SceneEnhancer.fogDistance + 64.0D;
		float sizeCloudMult = Math.min(Math.max(storm.size * 0.0016F, 0.5F), storm.getLayerHeight() * 0.04F);
		float sizeFunnelMult = Math.min(Math.max(storm.funnelSize * 0.005F, 0.3F), storm.getLayerHeight() * 0.004F);
		float sizeOtherMult = Math.min(Math.max(storm.size * 0.003F, 0.5F), storm.getLayerHeight() * 0.035F);
		float heightMult = storm.getLayerHeight() * 0.00290625F;
		float rotationMult = Math.max(heightMult * 0.45F, 1.0F);
		float r = -1.0F, g = -1.0F, b = -1.0F;

		if (ConfigParticle.enable_tornado_block_colors)
		{
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
			else if (material.equals(Material.WATER) || storm.isSpout)
			{
				r = 0.3F; g = 0.35F; b = 0.7F;
			}
			else if (material.equals(Material.LAVA))
			{
				r = 1.0F; g = 0.45F; b = 0.35F;
			}
		}
		
		int delay = Math.max(1, (int)(100F / storm.size));
		int loopSize = 1;
		int extraSpawning = 0;
		
		if (storm.isSevere())
		{
			loopSize += 4;
			extraSpawning = (int) Math.min(storm.funnelSize * 2.375F, 1000.0F);
		}
		
		Random rand = new Random();
		Vec3 playerAdjPos = new Vec3(entP.posX, storm.pos.posY, entP.posZ);
		
		//spawn clouds
		if (ConfigCoroUtil.optimizedCloudRendering)
		{
			boolean isStorm = storm.stage >= Stage.RAIN.getStage();
			int height = storm.getLayerHeight() + (isStorm ? -5 : 0);
			int size = (int) (storm.size * 1.2D);
			float finalBright = isStorm ? 0.5F : 0.75F;
			Vec3 tryPos;
			ExtendedEntityRotFX particle;
			
			for (int i = 0; i < loopSize && shouldSpawn(0); i++)
			{
				tryPos = new Vec3(storm.pos.posX + Maths.random(size), height, storm.pos.posZ + Maths.random(size));
				//position doesnt matter, set by renderer while its invisible still
				if (WeatherUtil.isAprilFoolsDay())
					particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, ParticleRegistry.chicken);
				else
					particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0);
				
				if (particle == null) break;
				//offset starting rotation for even distribution except for middle one
				if (i != 0)
				{
					double rotPos = (i - 1);
					float radStart = (float) ((360D / 8D) * rotPos);
					particle.rotationAroundCenter = radStart;
				}

				particle.setColor(finalBright, finalBright, finalBright);
				particle.setScale(750.0F * sizeCloudMult);
				particle.setMaxAge(120);
				listParticlesCloud.add(particle);
			}
		}
		else
		{
			if (manager.getWorld().getTotalWorldTime() % (delay + ConfigParticle.cloud_particle_delay) == 0) {
				for (int i = 0; i < loopSize && shouldSpawn(0); i++)
				{
					if (listParticlesCloud.size() < (storm.size + extraSpawning) / 1F) {
						double spawnRad = storm.size * 1.2D;
						Vec3 tryPos = new Vec3(storm.pos.posX + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), storm.getLayerHeight() + (rand.nextDouble() * 40.0F) + (storm.stage >= Stage.RAIN.getStage() ? 40.0F : 0.0D), storm.pos.posZ + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
						if (tryPos.distance(playerAdjPos) < maxRenderDistance) {
							if (storm.getAvoidAngleIfTerrainAtOrAheadOfPosition(storm.getAngle(), tryPos) == 0) {
								ExtendedEntityRotFX particle;
								if (WeatherUtil.isAprilFoolsDay()) {
									particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0, ParticleRegistry.chicken);
									if (particle == null) break;
									particle.setColor(1F, 1F, 1F);
								}
								else
								{
									float finalBright = Math.min(0.8F, 0.6F + (rand.nextFloat() * 0.2F)) + (storm.stage >= Stage.RAIN.getStage() ? -0.3F : 0.0F);
									particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 0);
									if (particle == null) break;
									particle.setColor(finalBright, finalBright, finalBright);
									
									if (storm.isFirenado && storm.isSevere())
									{
											particle.setParticleTexture(net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_fire);
											particle.setColor(1F, 1F, 1F);
									}
								}

								particle.rotationPitch = Maths.random(70.0F, 110.0F);
								particle.setScale(1000.0F * sizeCloudMult);
								listParticlesCloud.add(particle);
							}
						}
					}
				}
			}
		}
		
		//ground effects
		if (ConfigParticle.enable_tornado_debris && !ConfigCoroUtil.optimizedCloudRendering && storm.stormType == StormType.LAND.ordinal() && storm.stage > Stage.SEVERE.getStage() && r >= 0.0F && !material.isLiquid())
		{
			if (manager.getWorld().getTotalWorldTime() % (delay + ConfigParticle.ground_debris_particle_delay) == 0)
			{
				for (int i = 0; i < 3 && shouldSpawn(2); i++)
				{
					double spawnRad = storm.funnelSize + 150.0D;
						
					Vec3 tryPos = new Vec3(storm.pos_funnel_base.posX + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), storm.pos_funnel_base.posY, storm.pos_funnel_base.posZ + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distance(playerAdjPos) < maxRenderDistance)
					{
						int groundY = WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(), new BlockPos((int)tryPos.posX, 0, (int)tryPos.posZ)).getY();
						ExtendedEntityRotFX particle;
						if (WeatherUtil.isAprilFoolsDay())
							particle = spawnParticle(tryPos.posX, groundY + 3, tryPos.posZ, 1, ParticleRegistry.potato);
						else
							particle = spawnParticle(tryPos.posX, groundY + 3, tryPos.posZ, 1);
						if (particle == null) break;
						particle.setColor(r, g, b);
						particle.setTicksFadeInMax(40);
						particle.setTicksFadeOutMax(80);
						particle.setGravity(0.01F);
						particle.setMaxAge(100);
						particle.setScale(250.0F * sizeFunnelMult);
						particle.rotationYaw = rand.nextInt(360);
						particle.rotationPitch = rand.nextInt(80);
							
						listParticlesGround.add(particle);
					}
				}
			}
		}
		
		delay = 1;
		loopSize = 3 + (int)(storm.funnelSize / 80);
		double spawnRad = storm.funnelSize * 0.02F;
		
		if (storm.stage >= Stage.TORNADO.getStage() + 1) 
			spawnRad *= 48.25D;
		
		//spawn funnel
		if (storm.isDeadly() && storm.stormType == 0 || storm.isSpout)
		{
			if (manager.getWorld().getTotalWorldTime() % (delay + ConfigParticle.funnel_particle_delay) == 0)
			{
				for (int i = 0; i < loopSize && shouldSpawn(1); i++)
				{
					Vec3 tryPos = new Vec3(storm.pos_funnel_base.posX + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), storm.pos.posY, storm.pos_funnel_base.posZ + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));

					if (tryPos.distance(playerAdjPos) < maxRenderDistance)
					{
						ExtendedEntityRotFX particle;
						if (!storm.isFirenado)
							if (WeatherUtil.isAprilFoolsDay())
								particle = spawnParticle(tryPos.posX, storm.pos_funnel_base.posY, tryPos.posZ, listParticlesRain.size() > 100 ? 1 : 2, ParticleRegistry.potato);
							else
								particle = spawnParticle(tryPos.posX, storm.pos_funnel_base.posY, tryPos.posZ, listParticlesRain.size() > 100 ? 1 : 2, net.mrbt0907.weather2.registry.ParticleRegistry.tornado256);
						else
							particle = spawnParticle(tryPos.posX, storm.pos_funnel_base.posY, tryPos.posZ, listParticlesRain.size() > 100 ? 1 : 2, net.mrbt0907.weather2.registry.ParticleRegistry.cloud256_fire);
						if (particle == null) break;
							
							//move these to a damn profile damnit!
						particle.setMaxAge(150 + ((storm.stage-1) * 10) + rand.nextInt(100));
						particle.rotationYaw = rand.nextInt(360);
							
						float finalBright = Math.min(0.6F, 0.4F + (rand.nextFloat() * 0.2F));
							
						//highwind aka spout in this current code location
						if (storm.stage == Stage.SEVERE.getStage())
						{
							particle.setScale(250.0F * sizeFunnelMult);
							particle.setColor(finalBright, finalBright, finalBright);
						}
						else
						{
							particle.setScale(800.0F * sizeFunnelMult);
							if (r >= 0.0F)
							{
								particle.setColor(r, g, b);
								particle.setFinalColor(1.0F - storm.formingStrength, finalBright, finalBright, finalBright);
								particle.setColorFade(0.75F);
							}
							else
								particle.setColor(finalBright, finalBright, finalBright);
						}

						if (storm.isFirenado)
						{
							particle.setRBGColorF(1F, 1F, 1F);
							particle.setScale(particle.getScale() * 0.7F);
						}
							
						listParticlesFunnel.add(particle);
					}
					
				}
			}
		}
		
		if (ConfigParticle.enable_distant_downfall && storm.ticks % 20 == 0 && ConfigParticle.distant_downfall_particle_rate > 0.0F && listParticlesRain.size() < 1000 && storm.stage > Stage.THUNDER.getStage() && storm.isRaining())
		{
			int particleCount = (int)Math.ceil(storm.rain * storm.stage * ConfigParticle.distant_downfall_particle_rate * 0.005F);
			
			for (int i = 0; i < particleCount && shouldSpawn(3); i++)
			{
				double spawnRad2 = storm.size;
				Vec3 tryPos = new Vec3(storm.pos.posX + (rand.nextDouble()*spawnRad2) - (rand.nextDouble()*spawnRad2), storm.pos.posY + rand.nextInt(50), storm.pos.posZ + (rand.nextDouble()*spawnRad2) - (rand.nextDouble()*spawnRad2));
				if (tryPos.distance(playerAdjPos) < maxRenderDistance)
				{
					ExtendedEntityRotFX particle;
					float finalBright = Math.min(0.7F, 0.5F + (rand.nextFloat() * 0.2F)) + (storm.stage >= Stage.RAIN.getStage() ? -0.2F : 0.0F );
					particle = spawnParticle(tryPos.posX, tryPos.posY, tryPos.posZ, 2, net.mrbt0907.weather2.registry.ParticleRegistry.distant_downfall);
					if (particle == null) break;
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
			if (!ent.isAlive() || ent.getPosY() > storm.getLayerHeight() + 200)
			{
				ent.setExpired();
				listParticlesFunnel.remove(ent);
			}
			else
			{
				 double var16 = storm.pos.posX - ent.getPosX();
				 double var18 = storm.pos.posZ - ent.getPosZ();
				 ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
				 ent.rotationYaw += ent.getEntityId() % 90;
				 ent.rotationPitch = -30F;
				 
				 storm.spinEntity(ent);
			}
		}
		
		for (int i = 0; i < listParticlesCloud.size(); i++)
		{
			EntityRotFX ent = listParticlesCloud.get(i);
			if (!ent.isAlive() || storm.stormType == StormType.WATER.ordinal() && ent.getDistance(storm.pos.posX, ent.posY, storm.pos.posZ) < storm.funnelSize)
			{
				ent.setExpired();
				listParticlesCloud.remove(ent);
			}
			else
			{
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
				double curDist = 10D;
				
				if (storm.isSevere())
				{
					double speed = storm.spin + (rand.nextDouble() * 0.04D) * rotationMult;
					double distt = Math.max(ConfigStorm.max_storm_size, storm.funnelSize + 20.0D);//300D;
					
					if (storm.stormType == 1)
					{
						speed *= 2.0D;
						distt *= 2.0D;
					}
					
					double vecX = ent.getPosX() - storm.pos.posX;
					double vecZ = ent.getPosZ() - storm.pos.posZ;
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
						if (storm.stage >= Stage.TORNADO.getStage()) {
							if (storm.stormType == StormType.WATER.ordinal()) {
								angle += 40 + ((ent.getEntityId() % 5) * 4);
								if (curDist > 150 + ((storm.stage-Stage.TORNADO.getStage()+1) * 30)) {
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
						
						double var16 = storm.pos.posX - ent.getPosX();
						double var18 = storm.pos.posZ - ent.getPosZ();
						ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
						ent.rotationPitch = -30F - (ent.getEntityId() % 10);
					}
					else
					{
						ent.rotationPitch = (float) (90.0F - (90.0f * Math.min(ent.getPosY() / (storm.getLayerHeight() + ent.getScale() * 0.75F), 1.0F)));
						if (ConfigParticle.enable_extended_render_distance)
							ent.setScale(2000.0F * sizeCloudMult); 
					}
					
					if (curSpeed < speed * 20D)
					{
						ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
						ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
					}
				}
				else
				{
					float cloudMoveAmp = 0.1F * (1 + storm.layer);
					
					float speed = storm.getSpeed() * cloudMoveAmp;
					float angle = storm.getAngle();

					//TODO: prevent new particles spawning inside or near solid blocks

					if ((manager.getWorld().getTotalWorldTime()) % 40 == 0) {
						ent.avoidTerrainAngle = storm.getAvoidAngleIfTerrainAtOrAheadOfPosition(angle, new Vec3(ent.getPos()));
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
				
				if (storm.stormType == 1 || storm.stage > 8)
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
			
			if (ent.motionY > -heightMult)
				ent.motionY = ent.motionY - (0.1D);
			
			double speed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
			double spin = storm.spin + 0.04F;
			double vecX = ent.getPosX() - storm.pos.posX;
			double vecZ = ent.getPosZ() - storm.pos.posZ;
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
				storm.spinEntity(ent);
		}
		if (storm.strength != 100.0F)
			storm.strength = 100.0F;
	}

	@Override
	public void onParticleLimitRefresh(WeatherManagerClient manager, int newParticleLimit)
	{
		particleLimitCloud = (int) (newParticleLimit * 0.4F);
		particleLimitGround = (int) (newParticleLimit * 0.05F);
		particleLimitRain = (int) (newParticleLimit * 0.05F);
		particleLimitFunnel = (int) (newParticleLimit * 0.5F);
	}
	
	@Override
	public void cleanupRenderer()
	{
		//for (EntityRotFX particle : listParticlesCloud) particle.setExpired();
			listParticlesCloud.clear();
		//for (EntityRotFX particle : listParticlesFunnel) particle.setExpired();
			listParticlesFunnel.clear();
		//for (EntityRotFX particle : listParticlesGround) particle.setExpired();
			listParticlesGround.clear();
		//for (EntityRotFX particle : listParticlesRain) particle.setExpired();
			listParticlesRain.clear();
	}
	
	private boolean shouldSpawn(int type)
	{
		switch(type)
		{
			case 0:
				return listParticlesCloud.size() < particleLimitCloud;
			case 1:
				return listParticlesFunnel.size() < particleLimitFunnel;
			case 2:
				return listParticlesGround.size() < particleLimitGround;
			case 3:
				return listParticlesRain.size() < particleLimitRain;
			default:
				return false;
		}
	}

	@Override
	public List<String> onDebugInfo()
	{
		List<String> debugInfo = new ArrayList<String>();
		debugInfo.add("Default Renderer - Version 2.0");
		debugInfo.add("");
		debugInfo.add("Cloud Particles: " + listParticlesCloud.size() + "/" + particleLimitCloud);
		debugInfo.add("Funnel Particles: " + listParticlesFunnel.size() + "/" + particleLimitFunnel);
		debugInfo.add("Ground Particles: " + listParticlesGround.size() + "/" + particleLimitGround);
		debugInfo.add("Rain Particles: " + listParticlesRain.size() + "/" + particleLimitRain);
		return debugInfo;
	}
}
