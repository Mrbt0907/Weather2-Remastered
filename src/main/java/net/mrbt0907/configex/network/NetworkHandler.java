package net.mrbt0907.configex.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.command.ClientCommandHandler;
import net.mrbt0907.configex.gui.GuiConfigEditor;

public class NetworkHandler
{
	public static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(ConfigModEX.MODID);
	private static int ID = -1;
	
	public static void preInit()
	{
		register(PacketNBT.class);
	}
	
	@SideOnly(Side.CLIENT)
	public static void onClientMessage(int index, NBTTagCompound nbt)
	{
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
		mc.addScheduledTask(() ->
		{
			switch(index)
			{
				case 0:
					ConfigManager.readNBT(nbt);
					break;
				case 1: case 2: case 3:
					ClientCommandHandler.onRecieveCommand(index, nbt);
					break;
				case 5:
					mc.displayGuiScreen(new GuiConfigEditor());
					break;
				default:
					ConfigModEX.warn("Network Handler recieved an invalid packet with index of " + index + ". Skipping...");
			}
		});
	}
	
	public static void onServerMessage(int index, NBTTagCompound nbt, EntityPlayerMP player)
	{
		FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() ->
		{
			switch(index)
			{
				case 0:
					if (ConfigManager.isRemote)
					{
						ConfigModEX.warn("Network Handler recieved a config packet, but cannot be used. Skipping...");
						return;
					}
					ConfigModEX.debug("Sending player " + player.getUniqueID());
					nbt.setUniqueId("player", player.getUniqueID());
					ConfigManager.readNBT(nbt);
					break;
				default:
					ConfigModEX.warn("Network Handler recieved an invalid packet with index of " + index + ". Skipping...");
			}
		});
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

	@SuppressWarnings("unused")
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
