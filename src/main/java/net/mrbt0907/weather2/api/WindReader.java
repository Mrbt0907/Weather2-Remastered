package net.mrbt0907.weather2.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.mrbt0907.weather2.config.ConfigWind;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManager;

public class WindReader
{
	public static float getWindSpeed(World world, Vec3 pos)
	{
		WeatherManager manager = WeatherAPI.getManager(world);
		if (manager != null)
			return manager.windManager.getWindSpeed(pos);
		return 0.0F;
	}
	
	public static float getWindAngle(World world, Vec3 pos)
	{
		WeatherManager manager = WeatherAPI.getManager(world);
		if (manager != null)
			return manager.windManager.getWindAngle(pos);
		else
			return 0.0F;
	}
	
	public static Vec3 getWindVectors(World world, Entity entity, float maxSpeed)
	{
		return getWindVectors(world, new Vec3(entity.posX, entity.posY, entity.posZ), new Vec3(entity.motionX, entity.motionY, entity.motionZ), (float) (WeatherUtilEntity.getWeight(entity) * 0.1F * (entity instanceof EntityPlayer ? ConfigWind.windPlayerWeightMult : entity instanceof EntityLivingBase ? ConfigWind.windEntityWeightMult : 1.0F) * (entity.isInWater() ? ConfigWind.windSwimmingWeightMult : 1.0F)), 5.0F);
	}
	
	public static Vec3 getWindVectors(World world, Vec3 pos, Vec3 motion, float weight, float maxSpeed)
	{
		WeatherManager manager = WeatherAPI.getManager(world);
		if (manager != null)
			return manager.windManager.getWindVectors(pos, motion, weight, 0.05F, maxSpeed);
		else
			return motion;
	}
}
