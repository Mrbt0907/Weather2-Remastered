package net.mrbt0907.weather2.api.weather;

import net.minecraft.util.math.BlockPos;
import net.mrbt0907.weather2.util.Maths.Vec3;

public interface IWeatherRain
{
	public static final int MINIMUM_DRIZZLE = 100, MINIMUM_RAIN = 200, MINIMUM_HEAVY_RAIN = 300;
	
	public float getDownfall();
	public float getDownfall(Vec3 pos);
	public float getDownfall(BlockPos pos);
	public boolean hasDownfall();
	public boolean hasDownfall(Vec3 pos);
	public boolean hasDownfall(BlockPos pos);
}