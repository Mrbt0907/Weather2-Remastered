package net.mrbt0907.weather2;

import modconfig.ConfigMod;
import modconfig.IConfigCategory;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.config.*;
import net.mrbt0907.weather2.event.EventHandlerFML;
import net.mrbt0907.weather2.event.EventHandlerForge;
import net.mrbt0907.weather2.event.EventHandlerPacket;
import net.mrbt0907.weather2.player.PlayerData;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.server.command.CommandWeather2;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.server.weather.WeatherManagerServer;
import net.mrbt0907.weather2.util.WeatherUtilConfig;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

@Mod(modid = Weather2.MODID, name=Weather2.MOD, version=Weather2.VERSION, acceptedMinecraftVersions="[1.12.2]", dependencies="required-after:coroutil@[1.12.1-1.2.37,);required-after:forge@[14.23.5.2847,);")
public class Weather2
{
	public static final String MOD = "Weather 2 - Remastered";
	public static final String MODID = "weather2";
	public static final String VERSION = "2.8.2-indev-b";
	public static final FMLEventChannel event_channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MODID);
	public static final CreativeTabs TAB = new CreativeTabs(MODID) {@Override public ItemStack getTabIconItem() {return new ItemStack(BlockRegistry.tornado_sensor);}};
	@Mod.Instance( value = Weather2.MODID )
	public static Weather2 instance;
	public static Logger log;
	public static List<IConfigCategory> configs = new ArrayList<>();
	public static boolean initProperNeededForWorld = true;
	
	
	@SidedProxy(clientSide = "net.mrbt0907.weather2.ClientProxy", serverSide = "net.mrbt0907.weather2.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		log = event.getModLog();
		event_channel.register(new EventHandlerPacket());
		MinecraftForge.EVENT_BUS.register(new EventHandlerFML());
		MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
		ConfigMod.addConfigFile(event, addConfig(new ConfigMisc()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigVolume()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigParticle()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigFront()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigStorm()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigGrab()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigSimulation()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigWind()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigSand()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigSnow()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigFoliage()));
		WeatherUtilConfig.loadNBT();
		info("Starting Weather2 - Remastered...");
		debug("Running preInit...");
		proxy.preInit();
		debug("Finished preInit");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		debug("Running init...");
		proxy.init();
		debug("Finished init");
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		debug("Running postInit...");
		proxy.postInit();
		//setting last state to track after configs load, but before ticking that uses it
		EventHandlerFML.extraGrassLast = ConfigFoliage.enable_extra_grass;
		debug("Finished postInit");
		info("Weather2 - Remastered is online!");
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandWeather2());
		WeatherAPI.refreshDimensionRules();
	}
	
	@Mod.EventHandler
	public void serverStart(FMLServerStartedEvent event) {}
	
	@Mod.EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		writeOutData(true);
		resetStates();
		
		initProperNeededForWorld = true;
	}
	
	
	
	/**
	 * To work around the need to force a configmod refresh on these when EZ GUI changes values
	 *
	 * @param config
	 * @return
	 */
	public static IConfigCategory addConfig(IConfigCategory config) {
		configs.add(config);
		return config;
	}
	
	public static void resetStates()
	{
		ServerTickHandler.reset();
	}
	
	public static void writeOutData(boolean unloadInstances)
	{
		//write out overworld only, because only dim with volcanos planned
		try {
			WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(0);
			if (wm != null) {
				wm.writeToFile();
			}
			PlayerData.writeAllPlayerNBT(unloadInstances);
			//doesnt cover all needs, client connected to server needs this called from gui close too
			//maybe dont call this from here so client connected to server doesnt override what a client wants his 'server' settings to be in his singleplayer world
			//factoring in we dont do per world settings for this
			//WeatherUtilConfig.nbtSaveDataAll();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Triggered when communicating with other mods
	 * @param event
	 */
	@Mod.EventHandler
	public void handleIMCMessages(FMLInterModComms.IMCEvent event) {}
	
	public static void info(Object message)
	{
		log.info(message);
	}
	
	public static void debug(Object message)
	{
		if (ConfigMisc.debug_mode)
			log.info("[DEBUG] " + message);
	}
	
	public static void warn(Object message)
	{
		if (ConfigMisc.debug_mode)
			log.warn(message);
	}

	public static void error(Object message)
	{
		if (ConfigMisc.debug_mode)
			log.error(new Exception(message.toString()));
	}
	
	public static void fatal(Object message) throws Exception
	{
		throw new Exception(message.toString());
	}
}