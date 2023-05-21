package net.mrbt0907.weather2.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.common.MinecraftForge;
import net.mrbt0907.weather2.api.event.EventRegisterEZGuiOption;
import net.mrbt0907.weather2.util.TriMapEx;

public class EZGuiAPI
{
	/**0: button.on
	 * <br>1: button.off
	 * <br>2: button.both
	 * <br>3: button.none 
	 * <br>4: button.exit 
	 * <br>5: button.advanced 
	 * <br>6: button.reset 
	 * <br>7: button.next 
	 * <br>8: button.previous 
	 * <br>9: button.highest 
	 * <br>10: button.veryhigh 
	 * <br>11: button.high 
	 * <br>12: button.medium 
	 * <br>13: button.low 
	 * <br>14: button.verylow 
	 * <br>15: button.lowest 
	 * <br>16: button.highest.alt
	 * <br>17: button.lowest.alt 
	 * <br>18: button.mostcommon 
	 * <br>19: button.verycommon 
	 * <br>20: button.common 
	 * <br>21: button.normal 
	 * <br>22: button.rare 
	 * <br>23: button.veryrare 
	 * <br>24: button.realistic
	 * <br>25: graphics
	 * <br>26: system
	 * <br>27: storm
	 * <br>28: dimensions*/
	public static final String[] BUTTON_LIST = {"button.on", "button.off", "button.both", "button.none", "button.exit", "button.advanced", "button.reset", "button.next", "button.previous", "button.highest", "button.veryhigh", "button.high", "button.medium", "button.low", "button.verylow", "button.lowest", "button.highest.alt", "button.lowest.alt", "button.mostcommon", "button.verycommon", "button.common", "button.normal", "button.rare", "button.veryrare", "button.realistic", "graphics", "system", "storms", "dimensions"};
	/**Button Toggle Switch For Weather<br>0 - Off<br>1 - On*/
	public static final List<String> BL_WTOGGLE = new ArrayList<String>(Arrays.asList("button.off.w", "button.on.w"));
	/**Button Toggle Switch For Effects<br>0 - Off<br>1 - On*/
	public static final List<String> BL_ETOGGLE = new ArrayList<String>(Arrays.asList("button.off.e", "button.on.e"));
	/**Button Switch For Shader Compatibilities*/
	public static final List<String> BL_SHADERS = new ArrayList<String>(Arrays.asList(BUTTON_LIST[1], "button.shader.1", "button.shader.2", "button.shader.3", "button.shader.4", "button.shader.5", "button.shader.6", "button.shader.7", "button.shader.8", "button.shader.9", "button.shader.10", "button.shader.11"));
	/**Button Toggle Switch<br>0 - Off<br>1 - On*/
	public static final List<String> BL_TOGGLE = new ArrayList<String>(Arrays.asList(BUTTON_LIST[1], BUTTON_LIST[0]));
	/**Button Scale<br>0 - Lowest<br>1 - Very Low<br>2 - Low<br>3 - Medium<br>4 - High<br>5 - Very High<br>6 - Highest*/
	public static final List<String> BL_STR = new ArrayList<String>(Arrays.asList(BUTTON_LIST[17], BUTTON_LIST[14], BUTTON_LIST[13], BUTTON_LIST[12], BUTTON_LIST[11], BUTTON_LIST[10], BUTTON_LIST[16]));
	/**Button Scale With None<br>0 - None<br>1 - Lowest<br>2 - Very Low<br>3 - Low<br>4 - Medium<br>5 - High<br>6 - Very High<br>7 - Highest*/
	public static final List<String> BL_STR_ALT = new ArrayList<String>(Arrays.asList(BUTTON_LIST[3], BUTTON_LIST[15], BUTTON_LIST[14], BUTTON_LIST[13], BUTTON_LIST[12], BUTTON_LIST[11], BUTTON_LIST[10], BUTTON_LIST[9]));
	/**Button Frequency<br>0 - Rarest<br>1 - Very Rare<br>2 - Rare<br>3 - Normal<br>4 - Common<br>5 - Very Common<br>1 - Most Common*/
	public static final List<String> BL_RARE = new ArrayList<String>(Arrays.asList(BUTTON_LIST[24], BUTTON_LIST[23], BUTTON_LIST[22], BUTTON_LIST[21], BUTTON_LIST[20], BUTTON_LIST[19], BUTTON_LIST[18]));
	/**Button Frequency With None<br>0 - None<br>1 - Rarest<br>2 - Very Rare<br>3 - Rare<br>4 - Normal<br>5 - Common<br>6 - Very Common<br>7 - Most Common*/
	public static final List<String> BL_RARE_ALT = new ArrayList<String>(Arrays.asList(BUTTON_LIST[3], BUTTON_LIST[24], BUTTON_LIST[23], BUTTON_LIST[22], BUTTON_LIST[21], BUTTON_LIST[20], BUTTON_LIST[19], BUTTON_LIST[18]));
	
	
	//Sub Buttons
	public static final int BUTTON_MIN = 8;
	public static final String BA_CLOUD = "a_cloud";
	public static final String BA_FUNNEL = "a_funnel";
	public static final String BA_PRECIPITATION = "a_precipitation";
	public static final String BA_EFFECT = "a_effect";
	public static final String BA_EF = "a_ef";
	public static final String BA_SHADER = "a_shader";
	public static final String BA_FOLIAGE = "a_foliage";
	public static final String BA_RENDER_DISTANCE = "a_render_distance";
	public static final String BB_GLOBAL = "b_global";
	public static final String BB_RADAR = "b_radar";
	public static final String BC_ENABLE_TORNADO = "c_tornado";
	public static final String BC_ENABLE_CYCLONE = "c_cyclone";
	public static final String BC_ENABLE_SANDSTORM = "c_sandstorm";
	public static final String BC_FREQUENCY = "c_frequency";
	public static final String BC_GRAB_BLOCK = "c_grab_block";
	public static final String BC_GRAB_ITEM = "c_grab_item";
	public static final String BC_GRAB_MOB = "c_grab_mob";
	public static final String BC_GRAB_PLAYER = "c_grab_player";
	public static final String BC_STORM_PER_PLAYER = "c_storm_per_player";
	
	private static TriMapEx<String, List<String>, Integer> options = new TriMapEx<String, List<String>, Integer>();
	private static Map<String, Integer> optionCategories = new LinkedHashMap<String,Integer>();
	
	/**Gets all of the EZ Gui options in all 3 categories<br>
	 * Type String: Option ID<br>
	 * Type List: Available Options<br>
	 * Type Integer: Default Value*/
	public static TriMapEx<String, List<String>, Integer> getOptions()
	{
		return options;
	}
	
	/**Gets all of the EZ Gui options with their respective catergory indexes<br>
	 * Type String: Option ID<br>
	 * Type Integer: Category Index (0 = Graphics, 1 = Systems, 2 = Storms)*/
	public static Map<String, Integer> getOptionCategories()
	{
		return optionCategories;
	}
	
	/**Refreshes all EZ Gui Options*/
	public static void refreshOptions()
	{
		options.clear();
		optionCategories.clear();
		options.put(BA_CLOUD, BL_STR, 3);
		options.put(BA_FUNNEL, BL_STR, 3);
		options.put(BA_PRECIPITATION, BL_STR_ALT, 3);
		options.put(BA_EFFECT, BL_STR_ALT, 3);
		options.put(BA_EF, BL_TOGGLE, 0);
		options.put(BA_SHADER, BL_SHADERS, 0);
		options.put(BA_FOLIAGE, BL_TOGGLE, 0);
		options.put(BA_RENDER_DISTANCE, BL_STR_ALT, 0);
		options.put(BB_GLOBAL, BL_TOGGLE, 0);
		options.put(BB_RADAR, BL_TOGGLE, 0);
		options.put(BC_ENABLE_TORNADO, BL_TOGGLE, 1);
		options.put(BC_ENABLE_CYCLONE, BL_TOGGLE, 1);
		options.put(BC_ENABLE_SANDSTORM, BL_TOGGLE, 1);
		options.put(BC_FREQUENCY, BL_RARE, 3);
		options.put(BC_GRAB_BLOCK, BL_TOGGLE, 1);
		options.put(BC_GRAB_ITEM, BL_TOGGLE, 0);
		options.put(BC_GRAB_MOB, BL_TOGGLE, 1);
		options.put(BC_GRAB_PLAYER, BL_TOGGLE, 1);
		options.put(BC_STORM_PER_PLAYER, BL_TOGGLE, 0);
		
		for(String key : options.keys())
		{
			if (key.matches("^a_.*"))
				optionCategories.put(key, 0);
			else if (key.matches("^b_.*"))
				optionCategories.put(key, 1);
			else if (key.matches("^c_.*"))
				optionCategories.put(key, 2);
		}
		EventRegisterEZGuiOption event = new EventRegisterEZGuiOption(options, optionCategories);
		MinecraftForge.EVENT_BUS.post(event);
		options = event.getOptions();
		optionCategories = event.getOptionCategories();
	}
}
