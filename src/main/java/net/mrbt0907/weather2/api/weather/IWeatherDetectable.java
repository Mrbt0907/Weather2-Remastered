package net.mrbt0907.weather2.api.weather;

import net.mrbt0907.weather2.util.Maths.Vec3;

import java.util.UUID;

public interface IWeatherDetectable extends IWeatherWind, IWeatherStaged {
    String getName();

    String getTypeName();

    UUID getUUID();

    Vec3 getPos();

    float getAngle();

    float getSpeed();

    boolean isDying();
}
