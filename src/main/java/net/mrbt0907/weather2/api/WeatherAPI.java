package net.mrbt0907.weather2.api;

import net.minecraftforge.common.MinecraftForge;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.event.EventRegisterGrabLists;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.util.WeatherUtilConfig;
import net.mrbt0907.weather2.util.WeatherUtilList;

public class WeatherAPI
{
	private static final WeatherUtilList grabList = new WeatherUtilList("grab_list", 0, true, true);
	private static final WeatherUtilList replaceList = new WeatherUtilList("replace_list", 1, true, true);
	private static final WeatherUtilList windResistanceList = new WeatherUtilList("wind_resistance", 3, true, true);
	
	public static WeatherUtilList getWRList()
	{
		return windResistanceList;
	}
	
	public static WeatherUtilList getGrabList()
	{
		return grabList;
	}
	
	public static WeatherUtilList getReplaceList()
	{
		return replaceList;
	}
	
	public static float getEFWindSpeed(int stage)
	{
		return 65.0F + 27.0F * stage;
	}
	
	public static void refreshDimensionRules()
	{
		WeatherUtilConfig.refreshDimensionRules();
	}
	
	public static void refreshGrabRules()
	{
		EventRegisterGrabLists event = new EventRegisterGrabLists(WeatherAPI.getGrabList(), WeatherAPI.getReplaceList(), null, WeatherAPI.getWRList());
    	event.grabList.clear();
    	event.replaceList.clear();
    	event.windResistanceList.clear();
    	
    	MinecraftForge.EVENT_BUS.post(event);
    	
    	event.grabList.parse(ConfigGrab.grab_list_entries);
    	event.replaceList.parse(ConfigGrab.replace_list_entries);
    	event.windResistanceList.parse(ConfigGrab.wind_resistance_entries);
    	
    	event.grabList.check();
    	event.replaceList.check();
    	event.windResistanceList.check();
    	Weather2.debug("Grab Rules have been updated:\n- Grab List = " + WeatherAPI.getGrabList().size() + " Entry(s)\n- Replace List = " + WeatherAPI.getReplaceList().size() + " Entry(s)\n- Wind Resistance List = " + WeatherAPI.getWRList().size() + " Entry(s)");
	}
}
