package net.mrbt0907.weather2.weather;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigWind;
import net.mrbt0907.weather2.network.packets.PacketWind;
import net.mrbt0907.weather2.server.weather.WeatherManagerServer;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import CoroUtil.util.CoroUtilEntOrParticle;

public class WindManager
{	
	public WeatherManager manager;
	
	//global
	public float windAngle = 0.0F;
	public float windSpeed = 0.0F;
	public float windAngleTarget = 0.0F;
	public float windSpeedTarget = 0.0F;
	
	//gusts
	public float windAngleGust = 0.0F;
	public float windSpeedGust = 0.0F;
	public int windTimeGust = 0;
	
	//Other
	private final Map<Vec3, WeatherObject> cache = new HashMap<Vec3, WeatherObject>();
	private long nextWindRefresh;
	

	public WindManager(WeatherManager parManager)
	{
		manager = parManager;
		windAngle = Maths.random(360);
		nextWindRefresh = 0L;
	}

	public void tick()
	{
		
		if (!manager.isClient())
		{
			if (!ConfigWind.enable)
			{
				windSpeed = 0.0F;
				windSpeedTarget = 0.0F;
				windSpeedGust = 0.0F;
				windTimeGust = 0;
			}
			else
			{
				if (manager.world.getTotalWorldTime() % 200L == 0L)
					cache.clear();
				
				if (manager.getWorld().getTotalWorldTime() >= nextWindRefresh)
				{
					nextWindRefresh = manager.getWorld().getTotalWorldTime() + Maths.random(ConfigWind.windRefreshMin, ConfigWind.windRefreshMax);
					windSpeedTarget = (float) Maths.random(ConfigWind.windSpeedMin, ConfigWind.windSpeedMax);
					windAngleTarget += (float) Maths.random(-ConfigWind.windAngleChangeMax, ConfigWind.windAngleChangeMax);

					windAngleTarget = windAngle % 360.0F;
				}

				tickWindChange();
				
				if (ConfigWind.enableWindAffectsEntities)
				{
					Entity[] entities = new Entity[0];
					Entity entity;
					entities = manager.getWorld().loadedEntityList.toArray(entities);
					int size = entities.length;
					
					for (int i = 0; i < size; i++)
					{
						entity = entities[i];
						if (!entity.isDead && entity instanceof EntityLivingBase && WeatherUtilEntity.isEntityOutside(entity, true))
						{
							Vec3 a = getWindVectors(new Vec3(entity.posX, entity.posY, entity.posZ), new Vec3(entity.motionX, entity.motionY, entity.motionZ), (float) (WeatherUtilEntity.getWeight(entity) * 8F * ConfigWind.windEntityWeightMult * (entity.isInWater() ? ConfigWind.windSwimmingWeightMult : 1.0F)), 0.05F, 5.0F);
							entity.motionX = a.posX;
							entity.motionY = a.posY;
							entity.motionZ = a.posZ;
						}
					}
				}
			}
		}
		else if (!WeatherUtil.isPaused())
			if (ConfigWind.enable)
				tickClient();
			else
			{
				windSpeed = 0.0F;
				windSpeedTarget = 0.0F;
				windSpeedGust = 0.0F;
				windTimeGust = 0;
			}
	}

	@SideOnly(Side.CLIENT)
	public void tickClient()
	{
		Minecraft mc = Minecraft.getMinecraft();
		tickWindChangeClient();
		
		if (manager.world.getTotalWorldTime() % 200L == 0L)
			cache.clear();
		
		if (ConfigWind.enableWindAffectsEntities && mc.player != null && WeatherUtilEntity.isEntityOutside(mc.player, true))
		{
			Vec3 a = getWindVectors(new Vec3(mc.player.posX, mc.player.posY, mc.player.posZ), new Vec3(mc.player.motionX, mc.player.motionY, mc.player.motionZ), (float) (WeatherUtilEntity.getWeight(mc.player) * 8.0F * ConfigWind.windPlayerWeightMult * (mc.player.isInWater() ? ConfigWind.windSwimmingWeightMult : 1.0F)), 0.05F, 5.0F);

	    	//Weather2.debug(a.toString());
			mc.player.setVelocity(a.posX, a.posY, a.posZ);
		}
	}
	
	public void tickWindChange()
	{
		//Wind angle
		if (windAngle != windAngleTarget)
		{
			float difference = windAngle + -(windAngle > 180 && windAngleTarget <= 180 ? windAngleTarget + 360.0F : windAngle <= 180 && windAngleTarget > 180 ? windAngleTarget + -360.0F : windAngleTarget);
			float change = (float) (1.95F * ConfigWind.windChangeMult);
			if (Math.abs(difference) > change)
				if (difference > 0.0F)
					windAngle -= change;
				else
					windAngle += change;
			else
				windAngle = windAngleTarget;
			
			windAngle = windAngle % 360.0F;
		}
		
		//Wind Speed
		if (windSpeed != windSpeedTarget)
		{
			float difference = windSpeed - windSpeedTarget;
			float change = (float) (0.015F * ConfigWind.windChangeMult);
			if (Math.abs(difference) > change)
				if (windSpeed > windSpeedTarget)
					windSpeed -= change;
				else
					windSpeed += change;
			else
				windSpeed = windSpeedTarget;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void tickWindChangeClient()
	{
		//Wind angle
		if (windAngle != windAngleTarget)
		{
			float difference = windAngle + -(windAngle > 180 && windAngleTarget <= 180 ? windAngleTarget + 360.0F : windAngle <= 180 && windAngleTarget > 180 ? windAngleTarget + -360.0F : windAngleTarget);
			float change = (float) (1.95F * ConfigWind.windChangeMult);
			if (Math.abs(difference) > change)
				if (difference > 0.0F)
					windAngle -= change;
				else
					windAngle += change;
			else
				windAngle = windAngleTarget;
			
			windAngle = windAngle % 360.0F;
		}
		
		//Wind Speed
		if (windSpeed != windSpeedTarget)
		{
			float difference = windSpeed - windSpeedTarget;
			float change = (float) (0.015F * ConfigWind.windChangeMult);
			if (Math.abs(difference) > change)
				if (windSpeed > windSpeedTarget)
					windSpeed -= change;
				else
					windSpeed += change;
			else
				windSpeed = windSpeedTarget;
		}
	}
	
	public NBTTagCompound nbtSyncForClient() {
		NBTTagCompound data = new NBTTagCompound();
		
		data.setFloat("windSpeedTarget", windSpeedTarget);
		data.setFloat("windAngleTarget", windAngleTarget);
		data.setFloat("windSpeedGust", windSpeedGust);
		data.setFloat("windAngleGust", windAngleGust);
		data.setInteger("windTimeGust", windTimeGust);
		
		return data;
	}
	
	public void nbtSyncFromServer(NBTTagCompound parNBT) {
		
		windSpeedTarget = parNBT.getFloat("windSpeedTarget");
		windAngleTarget = parNBT.getFloat("windAngleTarget");
		windSpeedGust = parNBT.getFloat("windSpeedGust");
		windAngleGust = parNBT.getFloat("windAngleGust");
		windTimeGust = parNBT.getInteger("windTimeGust");
	}
	
	public void syncData()
	{
		if (manager instanceof WeatherManagerServer)
			PacketWind.update(manager.dim, this);
	}
	
	public void reset() {
		manager = null;
	}
	
	public void getEntityWindVectors(Object ent)
	{
		getEntityWindVectors(ent, 0.1F, 0.5F);
	}
	
	/**
	 * 
	 * To solve the problem of speed going overkill due to bad formulas
	 * 
	 * end goal: make object move at speed of wind
	 * - object has a weight that slows that adjustment
	 * - conservation of momentum
	 * 
	 * calculate force based on wind speed vs objects speed
	 * - use that force to apply to weight of object
	 * - profit
	 * 
	 * 
	 * @param ent
	 */
	public void getEntityWindVectors(Object ent, float multiplier, float maxSpeed) {

		Vec3 pos = manager.world.isRemote ? new Vec3(FMLClientHandler.instance().getClientPlayerEntity().getPosition()) : new Vec3(CoroUtilEntOrParticle.getPosX(ent), CoroUtilEntOrParticle.getPosY(ent), CoroUtilEntOrParticle.getPosZ(ent));
		Vec3 motion = getWindVectors(pos, new Vec3(CoroUtilEntOrParticle.getMotionX(ent), CoroUtilEntOrParticle.getMotionY(ent), CoroUtilEntOrParticle.getMotionZ(ent)), WeatherUtilEntity.getWeight(ent), multiplier, maxSpeed);
		
		CoroUtilEntOrParticle.setMotionX(ent, motion.posX);
    	CoroUtilEntOrParticle.setMotionZ(ent, motion.posZ);
	}
	
	public Vec3 applyWindForceImpl(Vec3 pos, Vec3 motion, float weight) {
		return getWindVectors(pos, motion, weight, 1F/20F, 0.5F);
	}
	
	/**
	 * Handle generic uses of wind force, for stuff like weather objects that arent entities or paticles
	 * 
	 * @param motion
	 * @param weight
	 * @param multiplier
	 * @param maxSpeed
	 * @return
	 */
	public Vec3 getWindVectors(Vec3 pos, Vec3 motion, float weight, float multiplier, float maxSpeed)
	{
		float windAngle = getWindAngle(pos);
		float windSpeed = getWindSpeed(pos);
		
    	float windX = (float) -Maths.fastSin(Math.toRadians(windAngle)) * windSpeed;
    	float windZ = (float) Maths.fastCos(Math.toRadians(windAngle)) * windSpeed;
    	
    	float objX = (float) motion.posX;
    	float objZ = (float) motion.posZ;
		
    	float windWeight = 1F;
    	float objWeight = weight;
    	
    	
    	//divide by zero protection
    	if (objWeight == 0.0F)
    		objWeight = 0.001F;
    	else if (objWeight < 0.0F)
    		return motion;
    	
    	//TEMP
    	//objWeight = 1F;
    	
    	float weightDiff = windWeight / objWeight;
    	
    	float vecX = (objX - windX) * weightDiff;
    	float vecZ = (objZ - windZ) * weightDiff;
    	
    	vecX *= multiplier;
    	vecZ *= multiplier;
    	
    	//copy over existing motion data
    	Vec3 newMotion = motion.copy();
        newMotion.posX = Maths.clamp(objX - vecX, -maxSpeed, maxSpeed);
        newMotion.posZ = Maths.clamp(objZ - vecZ, -maxSpeed, maxSpeed);
        return newMotion;
	}
	
	public float getWindSpeed(Vec3 pos)
	{
		if (pos == null) return manager.windManager.windSpeed;
		
        WeatherObject wo = getWeatherObject(pos);
        
        if (wo != null)
        {
        	float size = (wo.size * 0.90F);
			return Math.max(manager.windManager.windSpeed, (float)((wo instanceof SandstormObject ? 7.5F : ((StormObject)wo).windSpeed) * Math.min((size - wo.pos.distanceSq(pos) + (wo instanceof SandstormObject ? size : ((StormObject)wo).funnelSize)) / size, 1.0F)));
        }
        else
        	return manager.windManager.windSpeed;
	}
	
	public float getWindAngle(Vec3 pos)
	{
		if (pos == null) return manager.windManager.windAngle;
		
        WeatherObject wo = getWeatherObject(pos);
    	if (wo != null)
    	{
            float yaw = (-((float)Maths.fastATan2(wo.posGround.posX - pos.posX, wo.posGround.posZ - pos.posZ)) * 180.0F / (float)Math.PI) + 360.0F;
			return yaw % 360.0F;
    	}
    	else
    		return manager.windManager.windAngle;
	}
	
	public Vec3 getWindForce()
	{
		float windX = (float) -Maths.fastSin(Math.toRadians(windAngle)) * windSpeed;
		float windZ = (float) Maths.fastCos(Math.toRadians(windAngle)) * windSpeed;
		return new Vec3(windX, 0, windZ);
	}

	private WeatherObject getWeatherObject(Vec3 pos)
	{
		for (Entry<Vec3, WeatherObject> entry : cache.entrySet())
		{
			if (pos.distanceSq(entry.getKey()) < 300.0D)
				return entry.getValue();
		}
		
		WeatherObject wo = manager.getClosestWeather(pos, ConfigStorm.max_storm_size + 50.0D, 0, Integer.MAX_VALUE, Type.CLOUD);
		cache.put(pos, wo);
		return wo;
	}
	
    public void readFromNBT(NBTTagCompound data) {
    	windSpeedTarget = data.getFloat("windSpeedTarget");
    	windAngleTarget = data.getFloat("windAngleTarget");
        windSpeedGust = data.getFloat("windSpeedGust");
        windAngleGust = data.getFloat("windAngleGust");
        windTimeGust = data.getInteger("windTimeGust");

    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setFloat("windSpeedTarget", windSpeedTarget);
        data.setFloat("windAngleTarget", windAngleTarget);

        data.setFloat("windSpeedGust", windSpeedGust);
        data.setFloat("windAngleGust", windAngleGust);
        data.setInteger("windTimeGust", windTimeGust);

        return data;
    }
}
