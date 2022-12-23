package net.mrbt0907.weather2.api.weather;

import java.util.UUID;

import net.mrbt0907.weather2.util.Maths.Vec3;

public interface IWeatherDetectable extends IWeatherWind, IWeatherStaged
{
	public String getName();
	public String getTypeName();
	public UUID getUUID();
	public Vec3 getPos();
	public float getAngle();
	public float getSpeed();
	public boolean isDying();
}
