package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.NBTTagCompound;
import net.mrbt0907.weather2.weather.storm.FrontObject;

public class PacketFrontObject extends PacketBase
{
	public static void create(Object target, FrontObject front)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("frontObject", front.writeNBT());
		nbt.setUniqueId("uuid", front.getUUID());
		send(11, nbt, target);
	}
	
	public static void update(Object target, FrontObject front)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("frontObject", front.writeNBT());
		send(12, nbt, target);
	}
	
	public static void remove(int dimension, FrontObject front)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setUniqueId("frontUUID", front.getUUID());
		send(13, nbt, dimension);
	}
}
