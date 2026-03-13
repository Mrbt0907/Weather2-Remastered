package net.mrbt0907.weather2.weather.storm;

import java.util.ArrayList;
import java.util.List;

public class WeatherTypes
{
	public static List<WeatherEntityConfig> weatherEntTypes;
	
	static {
		initWeatherTypes();
	}
	
	public static void initWeatherTypes()
	{
		weatherEntTypes = new ArrayList<WeatherEntityConfig>();
		WeatherEntityConfig sConf = new WeatherEntityConfig();







		sConf.tornadoInitialSpeed = 0.2F;
		sConf.tornadoPullRate = 0.05F;
		sConf.tornadoLiftRate = 0.175F;
		sConf.relTornadoSize = -10;
		sConf.tornadoBaseSize = 3;
		sConf.tornadoWidthScale = 1.0F;
		sConf.grabDist = 40D;
		sConf.tornadoTime = 4500;
		sConf.type = 0;
		sConf.grabsBlocks = false;
		weatherEntTypes.add(sConf);
		

		sConf = new WeatherEntityConfig();
		sConf.tornadoInitialSpeed = 0.2F;
		sConf.tornadoPullRate = 0.04F;
		sConf.tornadoLiftRate = 0.05F;
		sConf.relTornadoSize = -20;
		sConf.tornadoWidthScale = 1.5F;
		weatherEntTypes.add(sConf);
		

		sConf = new WeatherEntityConfig();
		sConf.tornadoInitialSpeed = 0.2F;
		sConf.tornadoPullRate = 0.04F;
		sConf.tornadoLiftRate = 0.06F;
		sConf.relTornadoSize = -30;
		sConf.tornadoWidthScale = 1.5F;
		weatherEntTypes.add(sConf);
		

		sConf = new WeatherEntityConfig();
		sConf.tornadoPullRate = 0.04F;
		sConf.tornadoLiftRate = 0.07F;
		sConf.relTornadoSize = -40;
		sConf.tornadoWidthScale = 1.9F;
		weatherEntTypes.add(sConf);
		

		sConf = new WeatherEntityConfig();
		sConf.tornadoPullRate = 0.04F;
		sConf.tornadoLiftRate = 0.08F;
		sConf.relTornadoSize = -50;
		sConf.tornadoWidthScale = 1.9F;
		weatherEntTypes.add(sConf);
		

		sConf = new WeatherEntityConfig();
		sConf.tornadoPullRate = 0.04F;
		sConf.tornadoLiftRate = 0.09F;
		sConf.relTornadoSize = -60;
		sConf.tornadoWidthScale = 2.5F;
		weatherEntTypes.add(sConf);
		

		sConf = new WeatherEntityConfig();
		sConf.tornadoPullRate = 0.06F;
		sConf.tornadoLiftRate = 0.10F;
		sConf.relTornadoSize = -75;
		sConf.tornadoWidthScale = 3.5F;
		weatherEntTypes.add(sConf);
	}
}
