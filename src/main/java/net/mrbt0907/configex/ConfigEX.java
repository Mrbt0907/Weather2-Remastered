package net.mrbt0907.configex;

import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.mrbt0907.configex.api.ConfigAPI;
import net.mrbt0907.configex.config.ConfigMain;
import net.mrbt0907.configex.server.CommandConfigEX;

@Mod(modid = ConfigEX.MODID, name=ConfigEX.MOD, version=ConfigEX.VERSION, acceptedMinecraftVersions="[1.12.2]", dependencies="required-after:forge@[14.23.5.2847,);")
public class ConfigEX
{
	public static final String MOD = "Mrbt0907's Config System";
	public static final String MODID = "configex";
	public static final String VERSION = "1.0";
	public static Logger log;
	public static boolean debug = true;
	public static ConfigMain config;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		log = event.getModLog();
		config = new ConfigMain();
		ConfigAPI.addConfig(config);
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandConfigEX());
		
	}
	
	public static void info(Object message)
	{
		log.info(message);
	}
	
	public static void debug(Object message)
	{
		if (debug)
			log.info("[DEBUG] " + message);
	}
	
	public static void warn(Object message)
	{
		if (debug)
			log.warn(message);
	}

	public static void error(Object message)
	{
		if (debug)
			new Exception(message.toString()).printStackTrace();
	}
	
	public static void fatal(Object message)
	{
		throw new Error(message.toString());
	}
}