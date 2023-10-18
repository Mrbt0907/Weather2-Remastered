package net.mrbt0907.weather2.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.network.packets.PacketEZGUI;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.util.WeatherUtilSound;
import CoroUtil.packet.PacketHelper;

public class EventHandlerPacket {
	
	//if im going to load nbt, i probably should package it at the VERY end of the packet so it loads properly
	//does .payload continue from where i last read or is it whole thing?
	//maybe i should just do nbt only
	
	//changes from 1.6.4 to 1.7.2:
	//all nbt now:
	//- inv writes stack to nbt, dont use buffer
	//- any sending code needs a full reverification that it matches up with how its received in this class
	//- READ ABOVE ^
	//- CoroAI_Inv could be factored out and replaced with CoroAI_Ent, epoch entities use it this way
	
	@SubscribeEvent
	public void onPacketFromServer(FMLNetworkEvent.ClientCustomPacketEvent event)
	{
		try
		{
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.getPacket().payload());
			int command = nbt.getInteger("command");
			
			Minecraft.getMinecraft().addScheduledTask(() ->
			{
				switch(command)
				{
					case 0: case 1: case 2: case 3:case 4: case 5: case 6: case 7:
						ClientTickHandler.checkClientWeather();
						//this line still gets NPE's despite it checking if its null right before it, wtf
						//Fixed it, your welcome
						ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
						break;
					case 9:
						EZConfigParser.nbtReceiveClient(nbt);
						break;
					//case 9:
						//ItemPocketSand.particulateFromServer(nbt.getString("playerName"));
						//break;
					case 10:
						//ClientTickHandler.clientConfigData.readNBT(nbt);
						break;
					case 11: case 12: case 13: case 14:
						ClientTickHandler.checkClientWeather();
						ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
						break;
					case 15:
						WeatherUtilSound.reset();
						Weather2.debug("Refreshed weather2 sound system");
						break;
					case 17:
						NewSceneEnhancer.instance().reset();
						NewSceneEnhancer.instance().enable();
						break;
					case 18:
						
						break;
					default:
						Weather2.error("Recieved an invalid network packet from the server");
				}
			});
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}		
	}
	
	@SubscribeEvent
	public void onPacketFromClient(FMLNetworkEvent.ServerCustomPacketEvent event) {
		final EntityPlayerMP entP = ((NetHandlerPlayServer)event.getHandler()).player;
		
		try
		{
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.getPacket().payload());
			int command = nbt.getInteger("command");

			entP.server.addScheduledTask(() ->
			{
				switch(command)
				{
					case 8:
						NBTTagCompound sendNBT = EZConfigParser.nbtServerData;
						sendNBT.setInteger("command", 9);
						sendNBT.setInteger("server", 1);
						sendNBT.setBoolean("op", FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() || FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(entP.getGameProfile()));
						PacketEZGUI.syncResponse(sendNBT);
						break;
					case 11:
						ServerTickHandler.playerClientRequestsFullSync(entP);
						break;
					case 10:
						if (FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() || FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(entP.getGameProfile()))
							EZConfigParser.nbtReceiveServer(nbt);
						break;
				}
			});
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}