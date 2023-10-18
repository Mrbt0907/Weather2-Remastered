package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.NBTTagCompound;
import net.mrbt0907.weather2.WeatherDebug.DebugInfo;

public class PacketDebug extends PacketBase
{
	public static void sendDebugToClients(DebugInfo info)
	{
		send(18, info.writeNBTData(new NBTTagCompound()));
	}
}
