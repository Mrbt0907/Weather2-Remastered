package net.mrbt0907.weather2.network.packets;

import CoroUtil.packet.PacketHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.mrbt0907.weather2.Weather2;

public class PacketBase
{
	protected static void send(int command, NBTTagCompound nbt, Object... target)
	{
		if (nbt == null)
		{
			Weather2.error("Network command #" + command + " returned null nbt data");
			return;
		}
		
		nbt.setInteger("command", command);
		
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			Weather2.event_channel.sendToServer(PacketHelper.getNBTPacket(nbt, Weather2.MODID));
		else
		{
			if (target.length == 0)
				Weather2.event_channel.sendToAll(PacketHelper.getNBTPacket(nbt, Weather2.MODID));
			else if (target[0] instanceof Integer)
				Weather2.event_channel.sendToDimension(PacketHelper.getNBTPacket(nbt, Weather2.MODID), (int) target[0]);
			else if (target[0] instanceof EntityPlayerMP)
				Weather2.event_channel.sendTo(PacketHelper.getNBTPacket(nbt, Weather2.MODID), (EntityPlayerMP) target[0]);
			else
				Weather2.error("Network packet #" + command + " returned an invalid target");
		}
	}
}
