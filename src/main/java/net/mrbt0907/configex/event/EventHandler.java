package net.mrbt0907.configex.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.network.NetworkHandler;

public class EventHandler
{
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event)
	{
		if (event.phase.equals(Phase.START))
			ClientHandler.onTick();
	}
	
	@SubscribeEvent
	public static void playerLoggedIn(PlayerLoggedInEvent event)
	{
		if (!ConfigManager.isRemote && event.player instanceof EntityPlayerMP)
			NetworkHandler.sendClientPacket(0, ConfigManager.writeNBT(new NBTTagCompound()), event.player);
	}
}
