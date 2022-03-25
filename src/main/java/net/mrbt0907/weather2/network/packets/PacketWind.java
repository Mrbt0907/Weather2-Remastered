package net.mrbt0907.weather2.network.packets;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.weather.WindManager;

public class PacketWind extends PacketBase
{
	public static void update(int dimension, WindManager wm)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("manager", wm.nbtSyncForClient());
		send(6, nbt, dimension);
		FMLInterModComms.sendRuntimeMessage(Weather2.instance, Weather2.MODID, "weather.wind", nbt);
	}
}
