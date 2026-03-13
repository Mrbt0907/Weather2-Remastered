package net.mrbt0907.weather2.api.weather;

import net.minecraft.util.math.BlockPos;
import net.mrbt0907.weather2.util.Maths.Vec3;

public interface IWeatherRain {
    int MINIMUM_DRIZZLE = 50, MINIMUM_RAIN = 200, MINIMUM_HEAVY_RAIN = 300;

    float getDownfall();

    float getDownfall(Vec3 pos);

    float getDownfall(BlockPos pos);

    boolean hasDownfall();

    boolean hasDownfall(Vec3 pos);

    boolean hasDownfall(BlockPos pos);
}