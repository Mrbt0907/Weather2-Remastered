package net.mrbt0907.weather2.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.event.EventRegisterGrabLists;
import net.mrbt0907.weather2.api.event.EventRegisterStages;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.util.WeatherUtilConfig;
import net.mrbt0907.weather2.util.ConfigList;
import net.mrbt0907.weather2.weather.WeatherManager;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.util.Maths.Vec3;

public class WeatherAPI
{
	private static final ConfigList tornadoStageList = new ConfigList();
	private static final ConfigList hurricaneStageList = new ConfigList();
	private static final ConfigList grabList = new ConfigList();
	private static final ConfigList replaceList = new ConfigList();
	private static final ConfigList windResistanceList = new ConfigList();
	
	/**Gets the weather manager used in the world provided. There is a weather manager for each dimension.*/
	public static WeatherManager getManager(World world)
	{
		WeatherManager manager = null;
		if (world != null)
			if (world.isRemote)
				manager = getManager();//net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager;
			else
				manager = net.mrbt0907.weather2.server.event.ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
		
		return manager;
	}
	
	/**Gets the weather manager used on the client.*/
	@SideOnly(Side.CLIENT)
	public static WeatherManager getManager()
	{
		return net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager;
	}
	
	/**Gets the weather manager used in the dimension id provided. There is a weather manager for each dimension.*/
	public static WeatherManager getManager(int dimension)
	{
		return net.mrbt0907.weather2.server.event.ServerTickHandler.dimensionSystems.get(dimension);
	}
	
	/**Gets the closest weather object in the world the client is in*/
	@SideOnly(Side.CLIENT)
	public static WeatherObject getClosestWeather(Vec3 pos, double maxDist, int minStage, int maxStage, WeatherEnum.Type... excludedTypes)
	{
		return net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager != null ? net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager.getClosestWeather(pos, maxDist, minStage, maxStage, excludedTypes) : null;
	}
	
	/**Gets the closest weather object in the dimension that is selected*/
	public static WeatherObject getClosestWeather(int dimension, Vec3 pos, double maxDist, int minStage, int maxStage, WeatherEnum.Type... excludedTypes)
	{
		WeatherManager manager = getManager(dimension);
		return manager != null ? manager.getClosestWeather(pos, maxDist, minStage, maxStage, excludedTypes) : null;
	}
	
	/**
     * Check if precipitation occurring at position.
     * Use is somewhat expensive on cpu, consider caching result for frequent use
     *
     * @param world
     * @param position
     * @return
     */
	public static boolean isPrecipitatingAt(World world, BlockPos position)
	{
	    WeatherManager manager = getManager(world);
	    return manager == null ? false : manager.hasDownfall(position);
    }
	
	/**Gets the tornado stage list, which is used for rolling tornado stages*/
	public static ConfigList getTornadoStageList()
	{
		return tornadoStageList;
	}
	
	/**Gets the hurricane stage list, which is used for rolling hurricane stages*/
	public static ConfigList getHurricaneStageList()
	{
		return hurricaneStageList;
	}
	
	/**Gets the wind resistance list, which is used to get resistance values for specified blocks*/
	public static ConfigList getWRList()
	{
		return windResistanceList;
	}
	
	/**Gets the block grab list, which is used to detect what blocks can be picked up as a falling block entity. Avoid tile entities and things like buttons*/
	public static ConfigList getGrabList()
	{
		return grabList;
	}
	
	/**Gets the block replace list, which is used to specify what block turns to what when a tornado attempts to replace said block. Feel free to use any block including tile entities*/
	public static ConfigList getReplaceList()
	{
		return replaceList;
	}
	
	/**Gets the estimated wind speed based on the stage of tornado provided*/
	public static float getEFWindSpeed(int stage)
	{
		return 65.0F + 27.0F * stage;
	}
	
	/**Refreshes the dimension rules. These rules determine if weather can spawn in a dimension and whether they can create effects in a dimension.*/
	public static void refreshDimensionRules()
	{
		WeatherUtilConfig.refreshDimensionRules();
	}
	
	/**Refreshes every stage list, which each is used for rolling storm stages*/
	public static void refreshStages()
	{
		EventRegisterStages event = new EventRegisterStages(tornadoStageList, hurricaneStageList);
		event.tornadoStageList.clear();
		event.hurricaneStageList.clear();
		
		MinecraftForge.EVENT_BUS.post(event);
		
		String stagesA = ConfigStorm.chances_for_tornados.replaceAll("[^\\d\\.\\s\\,\\=]*", ""), stagesB = ConfigStorm.chances_for_hurricanes.replaceAll("[^\\d\\.\\s\\,\\=]*", "");
		
		event.tornadoStageList.parse(stagesA);
		event.hurricaneStageList.parse(stagesB);
		
		Weather2.debug("Cyclonic stages have been updated:\n- Tornado Stage List = " + tornadoStageList.size() + "\nHurricane Stage List = " + hurricaneStageList.size());
	}
	
	/**Refreshes every grab list at once*/
	public static void refreshGrabRules()
	{
		EventRegisterGrabLists event = new EventRegisterGrabLists(grabList, replaceList, null, windResistanceList);
    	event.grabList.clear();
    	event.replaceList.clear();
    	event.windResistanceList.clear();
    	
    	MinecraftForge.EVENT_BUS.post(event);
    	
    	event.grabList.parse(ConfigGrab.grab_list_entries);
    	event.replaceList.parse(ConfigGrab.replace_list_entries);
    	event.windResistanceList.parse(ConfigGrab.wind_resistance_entries);
    	
    	Set<ResourceLocation> entries =  Block.REGISTRY.getKeys();
    	
    	ConfigList list = processGrabList(entries, event.grabList, ConfigGrab.grab_list_partial_matches, 0);
    	event.grabList.clear();
    	event.grabList.addAll(list);
    	
    	list = processGrabList(entries, event.replaceList, ConfigGrab.replace_list_partial_matches, 1);
    	event.replaceList.clear();
    	event.replaceList.addAll(list);
    	
    	list = processGrabList(entries, event.windResistanceList, ConfigGrab.wind_resistance_partial_matches, 2);
    	event.windResistanceList.clear();
    	event.windResistanceList.addAll(list);
    	Weather2.debug("Grab Rules have been updated:\n- Grab List = " + WeatherAPI.getGrabList().size() + " Entry(s)\n- Replace List = " + WeatherAPI.getReplaceList().size() + " Entry(s)\n- Wind Resistance List = " + WeatherAPI.getWRList().size() + " Entry(s)");
	}
	
	private static ConfigList processGrabList(Set<ResourceLocation> entries, ConfigList cfg, boolean partialMatches, int type)
	{
		ConfigList list = new ConfigList();
    	String keyA, keyB, keyC;
    	List<String> keys;
    	List<Object> values;
    	boolean usePartialMatch = false;
    	
    	
    	for (Entry<String, Object[]> entry : cfg.toMap().entrySet())
    	{
    		if (type > 0 && entry.getValue().length == 0) continue;
    		keyA = entry.getKey();
    		if (keyA.contains(":"))
    			keyB = keyA;
    		else
    		{
    			keyB = "minecraft:" + keyA;
    			usePartialMatch = partialMatches;
    		}
    		keys = new ArrayList<String>();
    		values = new ArrayList<Object>();
    		
    		for(ResourceLocation block : entries)
    		{
    			keyC = block.toString();
    			
    			if (keyC.equals(keyB) || usePartialMatch && keyC.toLowerCase().contains(keyA.toLowerCase()))
    			 keys.add(keyC);
    		}

    		for(Object str : entry.getValue())
    		{
    			switch (type)
    			{
    				case 1:
    					if (str instanceof String)
    	    			{
    	    				usePartialMatch = false;
    		    			keyA = (String) str;
    		    			
    		    			if (keyA.contains(":"))
    		    	    		keyB = keyA;
    		    	    	else
    		    	    	{
    		    	    		keyB = "minecraft:" + keyA;
    		    	    		usePartialMatch = partialMatches;
    		    	    	}
    		    				
    		    			for(ResourceLocation block : entries)
    		    	    	{
    		    	    		keyC = block.toString();
    		    	    			
    		    	    		if (keyC.equals(keyB) || usePartialMatch && keyC.toLowerCase().contains(keyA.toLowerCase()))
    		    	    			values.add(keyC);
    		    	    	}
    	    			}
    					break;
    				case 2:
    					if (str instanceof Float)
    						values.add(str);
    					else if (str instanceof String)
    					{
    						try
    						{
    							float a = Float.parseFloat((String) str);
    							values.add(a);
    						}
    						catch (Exception e)
    						{
    							
    						}
    					}
    					break;
    				default:
    					values.add(str);
    			}
    		}
    		
    		if (type > 0 && values.size() == 0) continue;
    		
    		for (String key : keys)
    			list.add(key, values.toArray());
    	}
    	
    	return list;
	}
}
