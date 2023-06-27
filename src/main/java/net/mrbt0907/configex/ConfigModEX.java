package net.mrbt0907.configex;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.command.CommandConfigEX;
import net.mrbt0907.configex.config.ConfigMaster;
import net.mrbt0907.configex.config.ConfigStorm;
import net.mrbt0907.configex.event.EventHandler;
import net.mrbt0907.configex.network.NetworkHandler;

import java.io.File;

import org.apache.logging.log4j.Logger;

@Mod(modid = ConfigModEX.MODID, name=ConfigModEX.MOD, version=ConfigModEX.VERSION, acceptedMinecraftVersions="[1.12.2]", dependencies="required-after:forge@[14.23.5.2860,);", guiFactory = "net.mrbt0907.configex.gui.AdvancedGuiFactory")
public class ConfigModEX
{
	public static final String MODID = "configex";
	public static final String MOD = "Config Manager - Expanded";
	public static final String VERSION = "1.0";
	private static Logger log;
	public static final boolean enableDebug = true;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		log = event.getModLog();
		register(new ConfigMaster());
		register(new ConfigStorm());
		MinecraftForge.EVENT_BUS.register(EventHandler.class);
		NetworkHandler.preInit();
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandConfigEX());
	}
	
	public static IConfigEX register(IConfigEX config)
	{
		return ConfigManager.register(config);
	}
	
	public static String getGameFolder()
	{
		if (FMLCommonHandler.instance().getMinecraftServerInstance() == null || FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer())
			return FMLClientHandler.instance().getClient().gameDir.getPath() + File.separator;
		else
			return FMLCommonHandler.instance().getSavesDirectory().getPath() + File.separator;
	}
	
	public static void info(Object message)
	{
		log.info(message);
	}
	
	public static void debug(Object message)
	{
		boolean isDebug = ConfigMaster.debug_mode || enableDebug;
		if (isDebug)
			log.info("[DEBUG] " + message);
	}
	
	public static void warn(Object message)
	{
		boolean isDebug = ConfigMaster.debug_mode || enableDebug;
		if (isDebug)
			log.warn(message);
	}

	public static void error(Object message)
	{
		boolean isDebug = ConfigMaster.debug_mode || enableDebug;
		if (isDebug)
		{
			Throwable exception;
			
			if (message instanceof Throwable)
				exception = (Throwable) message;
			else
				exception = new Exception(String.valueOf(message));

			exception.printStackTrace();
		}
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