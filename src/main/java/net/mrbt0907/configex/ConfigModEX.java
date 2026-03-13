package net.mrbt0907.configex;

<<<<<<< Updated upstream
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.mrbt0907.configex.command.CommandConfigEX;
import net.mrbt0907.configex.config.ConfigMaster;
import net.mrbt0907.configex.event.EventsForge;
import net.mrbt0907.configex.network.NetworkHandler;

@Mod(ConfigModEX.MODID)
public class ConfigModEX
{
	public static final String MODID = "configex";
	private static final Logger LOGGER = LogManager.getLogger();
	public static boolean enableDebug = false;
	
	public ConfigModEX()
	{
	    enableDebug = true;
	    FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
		IEventBus MOD_BUS = context.getModEventBus();
		MOD_BUS.addListener(this::init);
		MOD_BUS.addListener(this::initClient);
		MOD_BUS.addListener(this::postInit);
		MOD_BUS.register(EventsForge.class);
		ConfigManager.register(new ConfigMaster());
		NetworkHandler.preInit();
		//ConfigMaster.preInit();
		MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
	}
	
	private void init(final FMLCommonSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.register(CommandConfigEX.class);
	}
	
	private void initClient(final FMLClientSetupEvent event)
	{

	}
	
	private void postInit (final FMLLoadCompleteEvent event)
	{

	}
	
	public void onServerStarting(FMLServerStartedEvent event)
	{
		NetworkHandler.sendClientPacket(0, new CompoundNBT(), (Object[])null);
	}
	
	public static void info(Object message)
	{
		LOGGER.info(message);
	}
	
	public static void debug(Object message)
	{
		boolean isDebug = enableDebug;	
		if (isDebug)
			LOGGER.info("[DEBUG] " + message);
	}
	
	public static void warn(Object message)
	{
		boolean isDebug = enableDebug;
		if (isDebug)
			LOGGER.warn(message);
	}

	public static void error(Object message)
	{
		boolean isDebug = enableDebug;
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
=======
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.command.CommandConfigEX;
import net.mrbt0907.configex.config.ConfigMaster;
import net.mrbt0907.configex.event.EventHandler;
import net.mrbt0907.configex.gui.GuiConfigEditor;
import net.mrbt0907.configex.network.NetworkHandler;

import java.io.File;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Mod(ConfigModEX.MODID)
public class ConfigModEX
{
    public static final String MODID = "configex";
    public static final String MOD = "Config Manager - Expanded";
    public static final String VERSION = "1.0";
    private static Logger log = LogManager.getLogger(MODID);
    public static final boolean enableDebug = false;

    public ConfigModEX()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(EventHandler.class);

    }

    @OnlyIn(Dist.CLIENT)
    public static Screen createConfigScreen(Screen parent) {
        return new GuiConfigEditor();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        register(new ConfigMaster());
        NetworkHandler.preInit();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        CommandConfigEX.register(event.getDispatcher());
    }

    public static IConfigEX register(IConfigEX config)
    {
        return ConfigManager.register(config);
    }

    public static String getGameFolder()
    {
        if (ServerLifecycleHooks.getCurrentServer() == null || ServerLifecycleHooks.getCurrentServer().isSingleplayer())
            return FMLPaths.GAMEDIR.get().toFile().getPath() + File.separator;
        else
            return new File(".").getAbsolutePath() + File.separator;
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
>>>>>>> Stashed changes
}