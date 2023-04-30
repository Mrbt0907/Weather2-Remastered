package net.mrbt0907.configex.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.configex.ConfigEX;
import net.mrbt0907.configex.ConfigProcessor;

public class NetworkHandler
{
	public static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(ConfigEX.MODID);
	private static int ID = -1;
	
	public static void preInit()
	{
		register(PacketNBT.class);
	}
	
	@SideOnly(Side.CLIENT)
	public static void onClientMessage(int index, NBTTagCompound nbt)
	{
		switch(index)
		{
			case 0:
				ConfigProcessor.applyServerValues(nbt);
			default:
				ConfigEX.error("Client recieved an invalid network message (type:" + index + ")");
		}
	}
	
	public static void onServerMessage(int index, NBTTagCompound nbt)
	{
		switch(index)
		{
			default:
				ConfigEX.error("Server recieved an invalid network message (type:" + index + ")");
		}
	}
	
	public static boolean sendClientPacket(int index, NBTTagCompound nbt, Object... targets)
	{
		return sendClientPacket(new PacketNBT(index, nbt), targets);
	}
	
	public static boolean sendClientPacket(IMessage message, Object... targets)
	{
		if (message == null) return false;
		if (targets.length > 0)
			for (Object target : targets)
			{
				if (target instanceof Integer)
					instance.sendToDimension(message, (int)targets[0]);
				else if (target instanceof EntityPlayerMP)
					instance.sendTo(message, (EntityPlayerMP)targets[0]);
			}
		else
			instance.sendToAll(message);
		
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean sendServerPacket(int index, NBTTagCompound nbt)
	{
		return sendServerPacket(new PacketNBT(index, nbt));
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean sendServerPacket(IMessage message)
	{
		if (message == null) return false;
		instance.sendToServer(message);
		return true;
	}

	private static <T extends IMessage & IMessageHandler<T, IMessage>> void register(Class<T> packetXHandler)
	{
		register(packetXHandler, packetXHandler, (Side)null);
	}

	private static <T extends IMessage & IMessageHandler<T, IMessage>> void register(Class<T> packetXHandler, Side... side)
	{
		register(packetXHandler, packetXHandler, side);
	}
	
	private static <T extends IMessage> void register(Class<? extends IMessageHandler<T, IMessage>> handler, Class<T> packet, Side... side)
	{
		if (side[0] == null)
		{
			instance.registerMessage(handler, packet, ID++, Side.CLIENT);
			instance.registerMessage(handler, packet, ID++, Side.SERVER);
			return;
		}

		instance.registerMessage(handler, packet, ID++, side[0]);
	}
}
