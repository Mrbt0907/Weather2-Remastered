package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.NBTTagCompound;

public class PacketVanillaWeather extends PacketBase
{
	public static void send(int dimension, int weatherID, int weatherRainTime)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("weatherID", weatherID);
		nbt.setInteger("weatherRainTime", weatherRainTime);
		send(0, nbt, dimension);
	}
}
