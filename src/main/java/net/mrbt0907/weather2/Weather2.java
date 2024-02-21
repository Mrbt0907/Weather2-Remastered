package net.mrbt0907.weather2;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.mrbt0907.configex.ConfigModEX;
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
import net.mrbt0907.weather2.util.ChunkUtils;

import org.apache.logging.log4j.Logger;

@Mod(modid = Weather2.MODID, name=Weather2.MOD, version=Weather2.VERSION, acceptedMinecraftVersions="[1.12.2]", dependencies="required-after:coroutil@[1.12.1-1.2.37,);required-after:configex@[1.0,);required-after:forge@[14.23.5.2860,);", guiFactory = "net.mrbt0907.configex.gui.AdvancedGuiFactory")
public class Weather2
{
	public static final String MOD = "Weather 2 - Remastered";
	public static final String MODID = "weather2remaster";
	public static final String OLD_MODID = "weather2";
	public static final String VERSION = "2.8.15";
	public static final FMLEventChannel event_channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MODID);
	public static final CreativeTabs TAB = new CreativeTabs(MODID) {@Override public ItemStack createIcon() {return new ItemStack(BlockRegistry.tornado_sensor);}};
	@Mod.Instance( value = Weather2.MODID )
	public static Weather2 instance;
	public static Logger log;
	public static boolean initProperNeededForWorld = true;
	public static ChunkUtils clientChunkUtil;
	public static ChunkUtils serverChunkUtil;
	@SidedProxy(modId = Weather2.MODID, clientSide = "net.mrbt0907.weather2.ClientProxy", serverSide = "net.mrbt0907.weather2.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		log = event.getModLog();
		event_channel.register(new EventHandlerPacket());
		MinecraftForge.EVENT_BUS.register(new EventHandlerFML());
		MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
		ConfigModEX.register(new ConfigMisc());
		ConfigModEX.register(new ConfigVolume());
		ConfigModEX.register(new ConfigParticle());
		ConfigModEX.register(new ConfigFront());
		ConfigModEX.register(new ConfigStorm());
		ConfigModEX.register(new ConfigGrab());
		ConfigModEX.register(new ConfigSimulation());
		ConfigModEX.register(new ConfigWind());
		ConfigModEX.register(new ConfigSand());
		ConfigModEX.register(new ConfigSnow());
		ConfigModEX.register(new ConfigFoliage());
		EZConfigParser.loadNBT();
		info("Starting Weather2 - Remastered...");
		if (FMLCommonHandler.instance().getSide().equals(Side.CLIENT))
			clientChunkUtil = new ChunkUtils();
		else
			serverChunkUtil = new ChunkUtils();
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
	public void postPostInit(FMLLoadCompleteEvent event)
	{
		proxy.postPostInit();	
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandWeather2());
		WeatherAPI.refreshDimensionRules();
		if (serverChunkUtil == null)
			serverChunkUtil = new ChunkUtils();
	}
	
	@Mod.EventHandler
	public void serverStart(FMLServerStartedEvent event) {}
	
	@Mod.EventHandler
	public void serverStop(FMLServerStoppedEvent event)
	{
		writeOutData(true);
		ServerTickHandler.reset();
		initProperNeededForWorld = true;
		serverChunkUtil.clearCache();
		serverChunkUtil = null;
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

	public static ChunkUtils getChunkUtil(World world)
	{
		return world.isRemote ? clientChunkUtil : serverChunkUtil;
	}
	
	public static void info(Object message)
	{
		log.info(message);
	}
	
	public static void debug(Object message)
	{
		boolean isDebug = ConfigMisc.debug_mode;
		if (isDebug)
			log.info("[DEBUG] " + message);
	}
	
	public static void warn(Object message)
	{
		boolean isDebug = ConfigMisc.debug_mode;
		if (isDebug)
			log.warn(message);
	}

	public static void error(Object message)
	{
			Throwable exception;
			
			if (message instanceof Throwable)
				exception = (Throwable) message;
			else
				exception = new Exception(String.valueOf(message));

			exception.printStackTrace();
	}
	
	public static void fatal(Object message)
	{
		Error error;
		
		if (message instanceof Error)
			error = (Error) message;
		else
			error = new Error(String.valueOf(message));
		
		throw error;
	}
}