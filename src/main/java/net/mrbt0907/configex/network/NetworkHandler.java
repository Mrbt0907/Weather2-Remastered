package net.mrbt0907.configex.network;

<<<<<<< Updated upstream
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;

public class NetworkHandler
{
	public static final SimpleChannel instance = NetworkRegistry.newSimpleChannel(new ResourceLocation(ConfigModEX.MODID), () -> "1.0.0", version -> version.equals("1.0.0"), version -> version.equals("1.0.0"));
	private static int ID = -1;
	
	public static void preInit()
	{
		register(PacketNBT.class, PacketNBT::onEncode, PacketNBT::onDecode, PacketNBT::handle);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void onClientMessage(int index, CompoundNBT nbt)
	{
		//net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
		
		switch(index)
		{
			case 0:
				ConfigManager.readNBT(nbt, 4);
				break;
			default:
				ConfigModEX.warn("Network Handler recieved an invalid packet with index of " + index + ". Skipping...");
		}
	}
	
	public static void onServerMessage(int index, CompoundNBT nbt, ServerPlayerEntity player)
	{
		switch(index)
		{
			case 0:
				ConfigManager.readNBT(nbt, player.server.isSingleplayer() ? 4 : player.server.getProfilePermissions(player.getGameProfile()));
				break;
			default:
				ConfigModEX.warn("Network Handler recieved an invalid packet with index of " + index + ". Skipping...");
		}
	}
	
	public static boolean sendClientPacket(int index, CompoundNBT nbt, Object... targets)
	{
		return sendClientPacket(new PacketNBT(index, nbt), targets);
	}
	
	@SuppressWarnings("unchecked")
	public static <MSG> boolean sendClientPacket(MSG message, Object... targets)
	{
		if (message == null) return false;
		if (targets != null && targets.length > 0)
			for (Object target : targets)
			{
				if (target instanceof RegistryKey<?>)
				{
					instance.send(PacketDistributor.DIMENSION.with((Supplier<RegistryKey<World>>) target), message);
				}
				else if (target instanceof ServerPlayerEntity)
					instance.send(PacketDistributor.PLAYER.with((Supplier<ServerPlayerEntity>) target), message);
			}
		else
			instance.send(PacketDistributor.ALL.noArg(), message);
		
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean sendServerPacket(int index, CompoundNBT nbt)
	{
		return sendServerPacket(new PacketNBT(index, nbt));
	}
	
	@OnlyIn(Dist.CLIENT)
	public static <MSG> boolean sendServerPacket(MSG message)
	{
		if (message == null) return false;
		instance.sendToServer(message);
		return true;
	}
	
	private static <MSG> void register(Class<MSG> message, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer)
	{
		instance.registerMessage(ID++, message, encoder, decoder, messageConsumer);
	}
}
=======
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.command.ClientCommandHandler;
import net.mrbt0907.configex.gui.GuiConfigEditor;

public class NetworkHandler
{
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel instance = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ConfigModEX.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void preInit()
    {
        int id = 0;
        instance.registerMessage(id++, PacketNBT.class, PacketNBT::encode, PacketNBT::decode, PacketNBT::handle);
    }

    @OnlyIn(Dist.CLIENT)
    public static void onClientMessage(int index, CompoundNBT nbt)
    {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        mc.execute(() ->
        {
            switch(index)
            {
                case 0:
                    ConfigManager.readNBT(nbt);
                    break;
                case 1: case 2: case 3:
                ClientCommandHandler.onReceiveCommand(index, nbt);
                break;
                case 5:
                    mc.setScreen(new GuiConfigEditor());
                    break;
                default:
                    ConfigModEX.warn("Network Handler received an invalid packet with index of " + index + ". Skipping...");
            }
        });
    }

    public static void onServerMessage(int index, CompoundNBT nbt, ServerPlayerEntity player)
    {
        ServerLifecycleHooks.getCurrentServer().execute(() ->
        {
            switch(index)
            {
                case 0:
                    if (ConfigManager.isRemote)
                    {
                        ConfigModEX.warn("Network Handler received a config packet, but cannot be used. Skipping...");
                        return;
                    }

                    nbt.putUUID("player", player.getUUID());
                    ConfigManager.readNBT(nbt);
                    ConfigManager.save();
                    break;
                case 1:
                    if (ConfigManager.isRemote)
                    {
                        ConfigModEX.warn("Network Handler received a resync config packet, but cannot be used. Skipping...");
                        return;
                    }
                    ConfigModEX.debug("Sending config data to " + player.getName().getString());
                    sendClientPacket(0, ConfigManager.writeNBT(new CompoundNBT()), player);
                    break;
                default:
                    ConfigModEX.warn("Network Handler received an invalid packet with index of " + index + ". Skipping...");
            }
        });
    }

    public static boolean sendClientPacket(int index, CompoundNBT nbt, Object... targets)
    {
        return sendClientPacket(new PacketNBT(index, nbt), targets);
    }

    public static boolean sendClientPacket(PacketNBT message, Object... targets)
    {
        if (message == null) return false;
        if (targets.length > 0)
            for (Object target : targets)
            {
                if (target instanceof Integer)
                    instance.send(PacketDistributor.DIMENSION.with(() -> net.minecraft.world.server.ServerWorld.OVERWORLD), message);
                else if (target instanceof ServerPlayerEntity)
                    instance.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)target), message);
                else if (target instanceof net.minecraft.command.CommandSource)
                {
                    net.minecraft.command.CommandSource source = (net.minecraft.command.CommandSource)target;
                    if (source.getEntity() instanceof ServerPlayerEntity)
                        instance.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)source.getEntity()), message);
                }
            }
        else
            instance.send(PacketDistributor.ALL.noArg(), message);

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean sendServerPacket(int index, CompoundNBT nbt)
    {
        return sendServerPacket(new PacketNBT(index, nbt));
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean sendServerPacket(PacketNBT message)
    {
        if (message == null) return false;
        instance.sendToServer(message);
        return true;
    }
}
>>>>>>> Stashed changes
