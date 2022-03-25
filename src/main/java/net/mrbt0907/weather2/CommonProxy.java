package net.mrbt0907.weather2;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.registry.EntityRegistry;
import net.mrbt0907.weather2.registry.ItemRegistry;
import net.mrbt0907.weather2.registry.RecipeRegistry;
import net.mrbt0907.weather2.registry.SoundRegistry;

@Mod.EventBusSubscriber
public class CommonProxy implements IGuiHandler
{
	public void preInit() {}
	
	public void init()
	{
		//WeatherUtilConfig.processLists();
		SoundRegistry.init();
		EntityRegistry.init();
	}
	
	public void postInit()
	{
		WeatherAPI.refreshGrabRules();
		RecipeRegistry.postInit();
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		ItemRegistry.register(event);
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		BlockRegistry.register(event);
	}
}
