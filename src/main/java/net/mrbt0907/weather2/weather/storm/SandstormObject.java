package net.mrbt0907.weather2.weather.storm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.client.entity.particle.ParticleSandstorm;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.config.ConfigSand;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.util.CachedNBTTagCompound;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.weather.WeatherManager;
import net.mrbt0907.weather2.weather.WindManager;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorSandstorm;
import extendedrenderer.particle.entity.EntityRotFX;

/**
 * spawns in sandy biomes
 * needs high wind event
 * starts small size grows up to something like 80 height
 * needs to sorda stay near sand, to be fed
 * where should position be? stay in sand biome? travel outside it?
 * - main position is moving, where the front of the storm is
 * - store original spawn position, spawn particles of increasing height from spawn to current pos
 * 
 * build up sand like snow?
 * usual crazy storm sounds
 * hurt plantlife leafyness
 * 
 * take sand and relocate it forward in direction storm is pushing, near center of where stormfront is
 * 
 * 
 * @author Corosus
 *
 */
public class SandstormObject extends WeatherObject
{

	public int height = 0;
	
	public Vec3 posSpawn = new Vec3(0, 0, 0);
	public float angle = 0.0F;
	
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesCloud;
	
	public ParticleBehaviorSandstorm particleBehavior;
	
	public int age = 0;
	private float maxSize = 100.0F;
	//public int maxAge = 20*20;
	
	public int sizePeak = 1;
	
	public int ageFadeout = 0;
	public int ageFadeoutMax = 20*60*5;
	
	//public boolean dying = false;
	public boolean isFrontGrowing = true;
	
	public Random rand = new Random();
	
	public SandstormObject(WeatherManager parManager) {
		super(parManager.getGlobalFront());
		
		this.type = WeatherEnum.Type.SANDSTORM;
		
		if (parManager.getWorld().isRemote) {
			listParticlesCloud = new ArrayList<EntityRotFX>();
			
		}
	}
	
	public void initSandstormSpawn(Vec3 pos) {
		this.pos = pos.copy();
		
		size = 1;
		sizePeak = 1;
		maxSize = 100;
		
		//temp start
		/*float angle = manager.windMan.getWindAngleForClouds();
		
		double vecX = -Maths.fastSin(Math.toRadians(angle));
		double vecZ = Maths.fastCos(Math.toRadians(angle));
		double speed = 150D;
		
		this.pos.posX -= vecX * speed;
		this.pos.posZ -= vecZ * speed;*/
		//temp end
		
		World world = manager.getWorld();
		int yy = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(pos.posX, 0, pos.posZ)).getY();
		pos.posY = yy;
		
		posGround = pos.copy();
		
		this.posSpawn = this.pos.copy();
		
		/*height = 0;
		size = 0;
		
		maxSize = 300;
		maxHeight = 100;*/
	}
	
	public float getSandstormScale() {
		if (isFrontGrowing) {
			return (float)size / (float)maxSize;
		} else {
			return 1F - ((float)ageFadeout / (float)ageFadeoutMax);
		}
	}
	
	public static boolean isDesert(Biome biome) {
		return isDesert(biome, false);
	}
	
	/**
	 * prevent rivers from killing sandstorm if its just passing over from desert to more desert
	 * 
	 * @param biome
	 * @param forSpawn
	 * @return
	 */
	public static boolean isDesert(Biome biome, boolean forSpawn) {
		return biome == Biomes.DESERT || biome == Biomes.DESERT_HILLS || (!forSpawn && biome == Biomes.RIVER) || biome.biomeName.contains("desert") && biome.getDefaultTemperature() >= 2.0F;
	}
	
	/**
	 * 
	 * - size of storm determined by how long it was in desert
	 * - front of storm dies down once it exits desert
	 * - stops moving once fully dies down
	 * 
	 * - storm continues for minutes even after front has exited desert
	 * 
	 * 
	 * 
	 */
	public void tickProgressionAndMovement() {
		
		World world = manager.getWorld();
		WindManager windMan = manager.windManager;
		
		angle = windMan.windAngle;
		float speedWind = windMan.windSpeed;
		
		/**
		 * Progression
		 */
		
		if (!world.isRemote) {
			age++;

			//boolean isGrowing = true;
			
			BlockPos posBlock = pos.toBlockPos();
			
			//only grow if in loaded area and in desert, also prevent it from growing again for some reason if it started dying already
			if (isFrontGrowing && world.isBlockLoaded(posBlock)) {
				Biome biomeIn = world.getBiomeForCoordsBody(posBlock);

				if (isDesert(biomeIn)) {
					isFrontGrowing = true;
				} else {
					//System.out.println("sandstorm fadeout started");
					isFrontGrowing = false;
				}
			} else {
				isFrontGrowing = false;
			}
			
			int sizeAdjRate = 10;
			
			if (isFrontGrowing) {
				if (world.getTotalWorldTime() % sizeAdjRate == 0) {
					if (size < maxSize) {
						size++;
						//System.out.println("size: " + size);
					}
				}
			} else {
				if (world.getTotalWorldTime() % sizeAdjRate == 0) {
					if (size > 0) {
						size--;
						//System.out.println("size: " + size);
					}
				}
				
				//fadeout till death
				if (ageFadeout < ageFadeoutMax) {
					ageFadeout++;
				} else {
					//System.out.println("sandstorm died");
					this.setDead();
				}
			}
			
			if (size > sizePeak) {
				sizePeak = size;
			}
			
		}
		
		/**
		 * Movement
		 */
		
		//clouds move at 0.2 amp of actual wind speed
		
		double vecX = -Maths.fastSin(Math.toRadians(angle));
		double vecZ = Maths.fastCos(Math.toRadians(angle));
		double speed = speedWind * 0.3D;//0.2D;
		
		//prevent it from moving if its died down to nothing
		if (size > 0) {
			this.pos.posX += vecX * speed;
			this.pos.posZ += vecZ * speed;
		}
		
		//wind movement
		//this.motion = windMan.applyWindForceImpl(this.motion, 5F, 1F/20F, 0.5F);
		
		/*this.pos.posX += this.motion.posX;
		this.pos.posY += this.motion.posY;
		this.pos.posZ += this.motion.posZ;*/
		
		int yy = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(pos.posX, 0, pos.posZ)).getY();
		
		this.pos.posY = yy + 1;
	}
	
	@SuppressWarnings("unused")
	public void tickBlockSandBuildup() {

		World world = manager.getWorld();
		WindManager windMan = manager.windManager;
		
		float angle = windMan.windAngle;
		
		//keep it set to do a lot of work only occasionally, prevents chunk render update spam for client which kills fps 
		int delay = ConfigSand.buildup_tick_delay;
		int loop = (int)((float)ConfigSand.max_buildup_loop_ammount * getSandstormScale());
		
		int count = 0;
		
		//sand block buildup
		if (!world.isRemote) {
			if (world.getTotalWorldTime() % delay == 0) {
				
		    	for (int i = 0; i < loop; i++) {
		    		
		    		//rate of placement based on storm intensity
		    		if (rand.nextDouble() >= getSandstormScale()) continue;

					Vec3 vecPos = getRandomPosInSandstorm();

					int y = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(vecPos.posX, 0, vecPos.posZ)).getY();
					vecPos.posY = y;

					//avoid unloaded areas
					if (!world.isBlockLoaded(vecPos.toBlockPos())) continue;

					Biome biomeIn = world.getBiomeForCoordsBody(vecPos.toBlockPos());

					if (ConfigSand.enable_buildup_outside_desert || isDesert(biomeIn)) {
						WeatherUtilBlock.fillAgainstWallSmoothly(world, vecPos, angle/* + angleRand*/, 15, 2, BlockRegistry.sand_layer);
					}

					count++;

			    	
		    	}
				
				//System.out.println("count: " + count);
			}
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (manager == null) {
			System.out.println("WeatherManager is null for " + this + ", why!!!");
			return;
		}
		
		World world = manager.getWorld();
		//WindManager windMan = manager.windMan;
		
		if (world == null) {
			System.out.println("world is null for " + this + ", why!!!");
			return;
		}
		
		if (WeatherUtil.isPausedSideSafe(world)) return;
		
		
		
		tickProgressionAndMovement();
		
		int yy = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(pos.posX, 0, pos.posZ)).getY();
		
		
		
		
		
		if (world.isRemote) {
			tickClient();
		}
		
		//if (size >= 2) {
		if (getSandstormScale() > 0.2D) {
			tickBlockSandBuildup();
		}
		
		this.posGround.posX = pos.posX;
		this.posGround.posY = yy;
		this.posGround.posZ = pos.posZ;
		
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient()
	{
		Minecraft mc = Minecraft.getMinecraft();
		World world = manager.getWorld();
		WindManager windMan = manager.windManager;
		
		if (particleBehavior == null) {
			particleBehavior = new ParticleBehaviorSandstorm(pos.toVec3Coro());
		}
		
    	double distBetweenParticles = 3;
    	
    	/**
    	 * if circ is 10, 10 / 3 size = 3 particles
    	 * if 30 circ / 3 size = 10 particles
    	 * if 200 circ / 3 size = 66 particles
    	 * 
    	 * how many degrees do we need to jump, 
    	 * 360 / 3 part = 120
    	 * 360 / 10 part = 36
    	 * 360 / 66 part = 5.4
    	 * 
    	 */
    	
    	Random rand = mc.world.rand;
    	
    	this.height = this.size / 4;
    	int heightLayers = Math.max(1, this.height / (int) distBetweenParticles);
    	
    	
    	double distFromSpawn = this.posSpawn.distanceSq(this.pos);
    	
    	double xVec = this.posSpawn.posX - this.pos.posX;
    	double zVec = this.posSpawn.posZ - this.pos.posZ;
    	
    	double directionAngle = Maths.fastATan2(zVec, xVec);
    	
    	/**
    	 * 
    	 * ideas: 
    	 * - pull particle distance inwards as its y reduces
    	 * -- factor in initial height spawn, first push out, then in, for a circularly shaped effect vertically
    	 * - base needs to be bigger than upper area
    	 * -- account for size change in the degRate value calculations for less particle spam
    	 * - needs more independant particle motion, its too unified atm
    	 * - irl sandstorms last between hours and days, adjust time for mc using speed and scale and lifetime
    	 */
    	
    	double directionAngleDeg = Math.toDegrees(directionAngle);
    	
    	int spawnedThisTick = 0;
    	
    	/**
    	 * stormfront wall
    	 */
    	float sandstormScale = getSandstormScale();

		double sandstormParticleRateDust = ConfigParticle.sandstorm_dust_particle_rate;
    	if (size > 0/*isFrontGrowing || sandstormScale > 0.5F*/) {
	    	for (int heightLayer = 0; heightLayer < heightLayers && spawnedThisTick < 500; heightLayer++) {
	    		//youd think this should be angle - 90 to angle + 90, but minecraft / bad math
			    //for (double i = directionAngleDeg; i < directionAngleDeg + (180); i += degRate) {
	    			double i = directionAngleDeg + (rand.nextDouble() * 180D);
			    	if ((mc.world.getTotalWorldTime()) % 2 == 0) {

						if (rand.nextDouble() >= sandstormParticleRateDust) continue;

			    		double sizeSub = heightLayer * 2D;
			    		double sizeDyn = size - sizeSub;
			    		double inwardsAdj = rand.nextDouble() * 5D;//(sizeDyn * 0.75D);
			    		
			    		double sizeRand = (sizeDyn + /*rand.nextDouble() * 30D*/ - inwardsAdj/*30D*/)/* / (double)heightLayer*/;
			    		double x = pos.posX + (-Maths.fastSin(Math.toRadians(i)) * (sizeRand));
			    		double z = pos.posZ + (Maths.fastCos(Math.toRadians(i)) * (sizeRand));
			    		double y = pos.posY + (heightLayer * distBetweenParticles * 2);
			    		
			    		TextureAtlasSprite sprite = ParticleRegistry.cloud256;
						if (WeatherUtil.isAprilFoolsDay()) {
							sprite = ParticleRegistry.chicken;
						}
			    		
			    		ParticleSandstorm part = new ParticleSandstorm(mc.world, x, y, z
			    				, 0, 0, 0, sprite);
			    		particleBehavior.initParticle(part);
			    		
			    		part.angleToStorm = i;
			    		part.distAdj = sizeRand;
			    		part.heightLayer = heightLayer;
			    		part.lockPosition = true;
			    		
			    		part.setFacePlayer(false);
			    		part.isTransparent = true;
			    		part.rotationYaw = (float) i + rand.nextInt(20) - 10;//Math.toDegrees(Maths.fastCos(Math.toRadians(i)) * 2D);
			    		part.rotationPitch = 0;
			    		part.setMaxAge(300);
			    		part.setGravity(0.09F);
			    		part.setAlphaF(1F);
			    		float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
			    		part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
			    		part.setScale(100);
			    		
			    		//part.windWeight = 5F;
			    		
			    		part.setKillOnCollide(true);
			    		part.renderOrder = 0;
			    		particleBehavior.particles.add(part);
			    		part.spawnAsWeatherEffect();
			    		
			    		spawnedThisTick++;
			    		
			    		//only need for non managed particles
			    		//ClientTickHandler.weatherManager.addWeatheredParticle(part);
			    		
			    		//mc.effectRenderer.addEffect(part);
			    		
			    		
			    	}
		    	//}
	    	}
    	}
    	
    	
    	if (spawnedThisTick > 0) {
    		//System.out.println("spawnedThisTickv1: " + spawnedThisTick);
    		spawnedThisTick = 0;
    	}
    	
    	if ((mc.world.getTotalWorldTime()) % 20 == 0) {
    		//System.out.println("sandstormScale: " + sandstormScale + " - size: " + size);
    	}
    	
    	//half of the angle (?)
    	double spawnAngle = Maths.fastATan2((double)this.sizePeak/*this.size*//* / 2D*/, distFromSpawn);
    	
    	//tweaking for visual due to it moving, etc
    	spawnAngle *= 1.2D;
    	
    	double spawnDistInc = 10;
    	
    	double extraDistSpawnIntoWall = sizePeak / 2D;
    	
    	/**
    	 * Spawn particles between spawn pos and current pos, cone shaped
    	 */
    	if ((mc.world.getTotalWorldTime()) % 3 == 0) {
    		
    		//System.out.println(this.particleBehavior.particles.size());
    		
	    	for (double spawnDistTick = 0; spawnDistTick < distFromSpawn + (extraDistSpawnIntoWall) && spawnedThisTick < 500; spawnDistTick += spawnDistInc) {
	    		
	    		//rate of spawn based on storm intensity
	    		if (rand.nextDouble() >= sandstormScale) continue;

				if (rand.nextDouble() >= sandstormParticleRateDust) continue;
	    		
	    		//add 1/4 PI for some reason, converting math to mc I guess
	    		double randAngle = directionAngle + (Math.PI / 2D) - (spawnAngle) + (rand.nextDouble() * spawnAngle * 2D);

	    		double randHeight = (spawnDistTick / distFromSpawn) * height * 1.2D * rand.nextDouble();
	    		
	    		//project out from spawn point, towards a point within acceptable angle
	    		double x = posSpawn.posX + (-Maths.fastSin(/*Math.toRadians(*/randAngle/*)*/) * (spawnDistTick));
	    		double z = posSpawn.posZ + (Maths.fastCos(/*Math.toRadians(*/randAngle/*)*/) * (spawnDistTick));
	    		
	    		//attempt to widen start, might mess with spawn positions further towards front
	    		x += (rand.nextDouble() - rand.nextDouble()) * 30D;
	    		z += (rand.nextDouble() - rand.nextDouble()) * 30D;
	    		
	    		int yy = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(x, 0, z)).getY();
	    		double y = yy/*posSpawn.posY*/ + 2 + randHeight;
	    		
	    		TextureAtlasSprite sprite = ParticleRegistry.cloud256;
	    		if (WeatherUtil.isAprilFoolsDay()) {
	    			sprite = ParticleRegistry.chicken;
				}
	    		
	    		ParticleSandstorm part = new ParticleSandstorm(mc.world, x, y, z
	    				, 0, 0, 0, sprite);
	    		particleBehavior.initParticle(part);
	    		
	    		part.setFacePlayer(false);
	    		part.isTransparent = true;
	    		part.rotationYaw = (float)rand.nextInt(360);
	    		part.rotationPitch = (float)rand.nextInt(360);
	    		part.setMaxAge(100);
	    		part.setGravity(0.09F);
	    		part.setAlphaF(1F);
	    		float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
	    		part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
	    		part.setScale(100);part.renderOrder = 0;
	    		
	    		part.setKillOnCollide(true);
	    		
	    		part.windWeight = 1F;
	    		
	    		particleBehavior.particles.add(part);
	    		//ClientTickHandler.weatherManager.addWeatheredParticle(part);
	    		part.spawnAsWeatherEffect();
	    		
	    		spawnedThisTick++;
	    	}
	    	
	    	//System.out.println("age: " + age + " - SCALE: " + getSandstormScale());
    	}
    	
    	if (spawnedThisTick > 0) {
    		//System.out.println("spawnedThisTickv2: " + spawnedThisTick);
    		
    	}

	    float angle = windMan.windAngle;
	    float speedWind = windMan.windSpeed;
		
		double vecX = -Maths.fastSin(Math.toRadians(angle));
		double vecZ = Maths.fastCos(Math.toRadians(angle));
		double speed = 0.8D;
		
		
	    
		particleBehavior.coordSource = pos.toVec3Coro();
	    particleBehavior.tickUpdateList();
	    
	    //System.out.println("client side size: " + size);
	    
	    /**
	     * keep sandstorm front in position
	     */
	    for (int i = 0; i < particleBehavior.particles.size(); i++) {
	    	ParticleSandstorm particle = (ParticleSandstorm) particleBehavior.particles.get(i);
	    	
	    	/**
	    	 * lock to position while sandstorm is in first size using phase, otherwise just let them fly without lock
	    	 */
	    	if (particle.lockPosition) {
	    		if (size > 0) {
			    	double x = pos.posX + (-Maths.fastSin(Math.toRadians(particle.angleToStorm)) * (particle.distAdj));
		    		double z = pos.posZ + (Maths.fastCos(Math.toRadians(particle.angleToStorm)) * (particle.distAdj));
		    		double y = pos.posY + (particle.heightLayer * distBetweenParticles);
		    		
		    		moveToPosition(particle, x, y, z, 0.01D);
	    		} else {
	    			//should be same formula actual storm object uses for speed
	    			particle.setMotionX((vecX * speedWind * 0.3F));
			    	particle.setMotionZ((vecZ * speedWind * 0.3F));
	    		}
	    	} else {
	    		particle.setMotionX(/*particle.getMotionX() + */(vecX * speed));
		    	particle.setMotionZ(/*particle.getMotionZ() + */(vecZ * speed));
	    	}
    		//windMan.applyWindForceNew(particle);
    		
	    }
	    //System.out.println("spawn particles at: " + pos);
	}
	
	public Vec3 getRandomPosInSandstorm() {
		
		double extraDistSpawnIntoWall = sizePeak / 2D;
		double distFromSpawn = this.posSpawn.distanceSq(this.pos);
		
		double randDist = rand.nextDouble() * (distFromSpawn + extraDistSpawnIntoWall);
		
		double xVec = this.posSpawn.posX - this.pos.posX;
    	double zVec = this.posSpawn.posZ - this.pos.posZ;
    	
    	double spawnAngle = Maths.fastATan2((double)this.sizePeak, distFromSpawn);
    	
    	//tweaking for visual due to it moving, etc
    	//spawnAngle *= 1.2D;
    	
    	double directionAngle = Maths.fastATan2(zVec, xVec);
		
		double randAngle = directionAngle + (Math.PI / 2D) - (spawnAngle) + (rand.nextDouble() * spawnAngle * 2D);
		
		double x = posSpawn.posX + (-Maths.fastSin(/*Math.toRadians(*/randAngle/*)*/) * (randDist));
		double z = posSpawn.posZ + (Maths.fastCos(/*Math.toRadians(*/randAngle/*)*/) * (randDist));
		
		return new Vec3(x, 0, z);
	}
	
	public List<CoroUtil.util.Vec3> getSandstormAsShape() {
		List<CoroUtil.util.Vec3> listPoints = new ArrayList<>();
		
		double extraDistSpawnIntoWall = sizePeak / 2D;
		double distFromSpawn = this.posSpawn.distanceSq(this.pos);

		//for triangle shape
		listPoints.add(new CoroUtil.util.Vec3(this.posSpawn.posX, 0, this.posSpawn.posZ));
		
		double xVec = this.posSpawn.posX - this.pos.posX;
    	double zVec = this.posSpawn.posZ - this.pos.posZ;
    	
    	double spawnAngle = Maths.fastATan2((double)this.sizePeak, distFromSpawn);
    	
    	double directionAngle = Maths.fastATan2(zVec, xVec);
    	
    	double angleLeft = directionAngle + (Math.PI / 2D) - (spawnAngle);
    	double angleRight = directionAngle + (Math.PI / 2D) - (spawnAngle) + (/*rand.nextDouble() * */spawnAngle * 2D);

    	double xLeft = posSpawn.posX + (-Maths.fastSin(/*Math.toRadians(*/angleLeft/*)*/) * (distFromSpawn + extraDistSpawnIntoWall));
		double zLeft = posSpawn.posZ + (Maths.fastCos(/*Math.toRadians(*/angleLeft/*)*/) * (distFromSpawn + extraDistSpawnIntoWall));
		
		double xRight = posSpawn.posX + (-Maths.fastSin(/*Math.toRadians(*/angleRight/*)*/) * (distFromSpawn + extraDistSpawnIntoWall));
		double zRight = posSpawn.posZ + (Maths.fastCos(/*Math.toRadians(*/angleRight/*)*/) * (distFromSpawn + extraDistSpawnIntoWall));
		
		listPoints.add(new CoroUtil.util.Vec3(xLeft, 0, zLeft));
		listPoints.add(new CoroUtil.util.Vec3(xRight, 0, zRight));
		
		return listPoints;
	}
	
	public void moveToPosition(ParticleSandstorm particle, double x, double y, double z, double maxSpeed) {
		if (particle.getPosX() > x) {
			particle.setMotionX(particle.getMotionX() + -maxSpeed);
		} else {
			particle.setMotionX(particle.getMotionX() + maxSpeed);
		}
		
		if (particle.getPosZ() > z) {
			particle.setMotionZ(particle.getMotionZ() + -maxSpeed);
		} else {
			particle.setMotionZ(particle.getMotionZ() + maxSpeed);
		}
		
		
		double distXZ = Math.sqrt((particle.getPosX() - x) * 2 + (particle.getPosZ() - z) * 2);
		if (distXZ < 5D) {
			particle.setMotionX(particle.getMotionX() * 0.8D);
			particle.setMotionZ(particle.getMotionZ() * 0.8D);
		}
	}
	
	@Override
	public int getNetRate()
	{
		return 1;
	}
		

	@Override
	public void readFromNBT()
	{
		super.readFromNBT();
		posSpawn = new Vec3(nbt.getDouble("posSpawnX"), nbt.getDouble("posSpawnY"), nbt.getDouble("posSpawnZ"));
				
				this.ageFadeout = nbt.getInteger("ageFadeout");
				this.ageFadeoutMax = nbt.getInteger("ageFadeoutMax");
				
				this.sizePeak = nbt.getInteger("sizePeak");
				this.age = nbt.getInteger("age");
				
				this.isFrontGrowing = nbt.getBoolean("isFrontGrowing");
			

		motion = new Vec3(nbt.getDouble("vecX"), nbt.getDouble("vecY"), nbt.getDouble("vecZ"));
	}

	@Override
	public CachedNBTTagCompound writeToNBT()
	{
		super.writeToNBT();
		nbt.setDouble("posSpawnX", posSpawn.posX);
		nbt.setDouble("posSpawnY", posSpawn.posY);
		nbt.setDouble("posSpawnZ", posSpawn.posZ);
		nbt.setInteger("ageFadeout", this.ageFadeout);
		nbt.setInteger("ageFadeoutMax", this.ageFadeoutMax);
		nbt.setInteger("sizePeak", sizePeak);
		nbt.setInteger("age", age);
		nbt.setBoolean("isFrontGrowing", isFrontGrowing);
		nbt.setDouble("vecX", motion.posX);
		nbt.setDouble("vecY", motion.posY);
		nbt.setDouble("vecZ", motion.posZ);
		return nbt;
	}

	@Override
	public void cleanup() {
		super.cleanup();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void cleanupClient(boolean wipe)
	{
		listParticlesCloud.forEach(particle -> particle.setExpired());
		listParticlesCloud.clear();
		if (particleBehavior != null) particleBehavior.particles.clear();
		particleBehavior = null;
	}

	@Override
	public float getWindSpeed()
	{
		return 7.0F;
	}

	@Override
	public int getStage()
	{
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void setStage(int stage) {}

	@Override
	public String getName()
	{
		return "Sandstorm";
	}

	@Override
	public String getTypeName()
	{
		return "SS";
	}

	@Override
	public float getAngle()
	{
		return angle;
	}

	@Override
	public float getSpeed()
	{
		return (float) motion.speedSq();
	}

	public int getParticleCount()
	{
		return particleBehavior == null ? 0 : particleBehavior.particles.size();
	}

	public boolean canSpawnParticle()
	{
		return ConfigParticle.max_particles < 0 || ClientTickHandler.weatherManager.getParticleCount() < ConfigParticle.max_particles;
	}

}
