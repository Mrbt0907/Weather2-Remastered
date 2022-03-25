package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.NBTTagCompound;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class PacketWeatherObject extends PacketBase
{
	public static void create(Object target, WeatherObject wo)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		wo.nbt.setUpdateForced(true);
		wo.writeNBT();
		wo.nbt.setUpdateForced(false);
		nbt.setTag("weatherObject", wo.nbt.getNewNBT());
		send(1, nbt, target);
	}
	
	public static void update(Object target, WeatherObject wo)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		wo.nbt.setNewNBT(new NBTTagCompound());
		wo.writeNBT();
		nbt.setTag("weatherObject", wo.nbt.getNewNBT());
		send(2, nbt, target);
	}
	
	public static void remove(int dimension, WeatherObject wo)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setUniqueId("uuid", wo.getUUID());
		send(3, nbt, dimension);
	}
}
