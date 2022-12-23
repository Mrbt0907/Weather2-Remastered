package net.mrbt0907.weather2.event;

import CoroUtil.forge.CULog;
import extendedrenderer.EventHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.*;
import net.mrbt0907.weather2.ClientProxy;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.config.ConfigFoliage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.server.event.ServerTickHandler;

public class EventHandlerFML {

	public static boolean sleepFlag = false;
	public static boolean wasRain = false;
	public static int rainTime = 0;
	public static boolean wasThunder = false;
	public static int thunderTime = 0;

	//initialized at post init after configs loaded in
	public static boolean extraGrassLast;

	@SubscribeEvent
	public void tickServer(ServerTickEvent event)
	{

		if (event.phase == Phase.START) {
			ServerTickHandler.onTickInGame();
		}

		if (ConfigMisc.disable_rain_reset_upon_sleep) {
			WorldServer world = DimensionManager.getWorld(0);
			if (world != null) {
				if (event.phase == Phase.START) {
					if (world.areAllPlayersAsleep()) {
						sleepFlag = true;
						wasRain = world.getWorldInfo().isRaining();
						wasThunder = world.getWorldInfo().isThundering();
						rainTime = world.getWorldInfo().getRainTime();
						thunderTime = world.getWorldInfo().getThunderTime();
					} else {
						sleepFlag = false;
					}
				} else {
					if (sleepFlag) {
						world.getWorldInfo().setRaining(wasRain);
						world.getWorldInfo().setRainTime(rainTime);
						world.getWorldInfo().setThundering(wasThunder);
						world.getWorldInfo().setThunderTime(thunderTime);
					}
				}
			}
		}

	}

	@SubscribeEvent
	public void tickClient(ClientTickEvent event)
	{
		if (event.phase == Phase.START)
		{
			try
			{
				ClientProxy.clientTickHandler.onTickInGame();

				if (extraGrassLast != ConfigFoliage.enable_extra_grass)
					extraGrassLast = ConfigFoliage.enable_extra_grass;

				boolean hackyLiveReplace = false;

				if (hackyLiveReplace && EventHandler.flagFoliageUpdate) {
					CULog.dbg("CoroUtil detected a need to reload resource packs, initiating");
					EventHandler.flagFoliageUpdate = false;
					FoliageEnhancerShader.liveReloadModels();
				}

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public void tickRenderScreen(RenderTickEvent event)
	{
		if (event.phase == Phase.END)
			ClientProxy.clientTickHandler.onRenderScreenTick();
	}

	@SubscribeEvent
	public void playerLoggedIn(PlayerLoggedInEvent event)
	{
		if (event.player instanceof EntityPlayerMP)
			ServerTickHandler.syncServerConfigToClientPlayer((EntityPlayerMP) event.player);
	}
}
