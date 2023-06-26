package net.mrbt0907.weather2.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class PacketRefresh extends PacketBase
{
	public static void resetSounds(EntityPlayerMP player)
	{
		send(15, new NBTTagCompound(), player);
	}
	public static void resetSceneEnhancer(EntityPlayerMP player)
	{
		send(17, new NBTTagCompound(), player);
	}
}
