package net.mrbt0907.weather2.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class PacketEZGUI extends PacketBase
{
	public static void sync()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		send(8, nbt);
	}
	
	public static void syncResponse(NBTTagCompound nbt)
	{
		send(9, nbt);
	}
	
	public static void apply(NBTTagCompound nbt)
	{
		send(10, nbt);
	}
	
	public static void apply(NBTTagCompound nbt, EntityPlayerMP player)
	{
		send(10, nbt, player);
	}
}
