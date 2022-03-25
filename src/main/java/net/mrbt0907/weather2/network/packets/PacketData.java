package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.NBTTagCompound;

public class PacketData extends PacketBase
{
	public static void sync()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		send(11, nbt);
	}
}
