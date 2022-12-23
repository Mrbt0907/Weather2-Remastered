package net.mrbt0907.weather2.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class PacketSound extends PacketBase
{
	public static void reset(EntityPlayerMP player)
	{
		send(15, new NBTTagCompound(), player);
	}
}
