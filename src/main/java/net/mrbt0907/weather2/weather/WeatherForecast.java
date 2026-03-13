package net.mrbt0907.weather2.weather;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class WeatherForecast
{
    public final List<Forecast> forecast = new ArrayList<>();
    
    public LinkedHashSet<Forecast> getWeather()
    {
        return new LinkedHashSet<>(forecast);
    }

    public LinkedHashSet<Forecast> getWeather(float accuracy)
    {
        LinkedHashSet<Forecast> forecast = new LinkedHashSet<>(this.forecast.size());
        
        return forecast;
    }
}