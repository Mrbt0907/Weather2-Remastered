package net.mrbt0907.weather2.api.weather;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public abstract class AbstractWeatherLogic {
    protected final World world;
    protected final WeatherObject weather;
    public String weatherID;

    public AbstractWeatherLogic(WeatherObject weather) {
        world = weather.manager.getWorld();
        this.weather = weather;
    }

    public abstract String[] getWeatherIds();

    public abstract String[] getWeatherFlags();

    public abstract void readNBT(CompoundNBT nbt);

    public abstract void writeNBT(CompoundNBT nbt);

    public abstract void tickWeather();

    public abstract boolean onSpawnCommand(String weatherID);

    public abstract boolean onSpawn();

    public abstract void onDespawn();

    public abstract boolean canSpawn();

    public abstract String getName();

    public abstract String getDisplayName();

    public void tick() {
        tickWeather();
    }
}