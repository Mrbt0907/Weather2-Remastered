package net.mrbt0907.weather2.api.weather;

import net.minecraft.world.World;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.weather.WeatherManager;

public abstract class AbstractManagerLogic {
    protected World world;
    protected WeatherManager manager;

    public AbstractManagerLogic(WeatherManager manager) {
        world = manager.getWorld();
        this.manager = manager;
    }

    public abstract void tickClient();

    public abstract void tickServer();

    public abstract void onLoad();

    public abstract void onSave();

    public abstract void onNetworkRecieve();

    public void spawn() {

    }

    public void despawn() {

    }

    public void despawnAll() {

    }

    public void load() {

    }

    public void save() {

    }

    public void tick() {
        if (world.isClientSide)
            tickClient();
        else
            tickServer();

        if (WeatherAPI.getWeatherLogicId() != null)
            manager.getWeatherObjects().forEach(weather ->
            {
                if (weather.weatherLogic != null)
                    weather.weatherLogic.tick();
            });
    }
}