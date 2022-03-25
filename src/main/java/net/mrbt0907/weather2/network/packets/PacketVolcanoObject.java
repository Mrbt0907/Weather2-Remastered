package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.NBTTagCompound;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

public class PacketVolcanoObject extends PacketBase
{
	public static void create(Object target, VolcanoObject vo)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("volcanoObject", vo.nbtSyncForClient());
		send(4, nbt, target);
	}
	
	public static void update(Object target, VolcanoObject vo)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("volcanoObject", vo.nbtSyncForClient());
		send(5, nbt, target);
	}
}
