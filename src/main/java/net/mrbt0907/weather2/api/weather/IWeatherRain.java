package net.mrbt0907.weather2.api.weather;

import net.minecraft.util.math.BlockPos;
import net.mrbt0907.weather2.util.Maths.Vec3;

public interface IWeatherRain
{
	public float getDownfall();
	public float getDownfall(Vec3 pos);
	public float getDownfall(BlockPos pos);
	public boolean hasDownfall();
	public boolean hasDownfall(Vec3 pos);
	public boolean hasDownfall(BlockPos pos);
}
