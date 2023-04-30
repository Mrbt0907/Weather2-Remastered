package net.mrbt0907.configex.network;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
public class PacketNBT implements IMessage, IMessageHandler<PacketNBT, IMessage>
{
	private int index;
	private NBTTagCompound nbt;
	
	public PacketNBT() {}

	public PacketNBT(int index, NBTTagCompound nbt)
	{
		this.index = index;
		this.nbt = nbt;
	}
	
	@Override
	public void fromBytes(ByteBuf buffer)
	{
		index = buffer.readInt();
		nbt = ByteBufUtils.readTag(buffer);
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeInt(index);
		ByteBufUtils.writeTag(buffer, nbt);
	}

	@Override
	public PacketNBT onMessage(PacketNBT message, MessageContext ctx)
	{
		if (ctx.side.isClient())
			return onClientMessage(message, ctx);
		else
			return onServerMessage(message, ctx);
	}

	@SideOnly(Side.CLIENT)
	protected PacketNBT onClientMessage(PacketNBT message, MessageContext ctx)
	{
		NetworkHandler.onClientMessage(message.index, message.nbt);
		return null;
	}

	protected PacketNBT onServerMessage(PacketNBT message, MessageContext ctx)
	{
		NetworkHandler.onServerMessage(message.index, message.nbt);
		return null;
	}
}


