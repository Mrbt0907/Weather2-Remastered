package net.mrbt0907.weather2;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.client.weather.StormNames;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.registry.EntityRegistry;
import net.mrbt0907.weather2.registry.ItemRegistry;
import net.mrbt0907.weather2.registry.RecipeRegistry;
import net.mrbt0907.weather2.registry.SoundRegistry;

@Mod.EventBusSubscriber(modid = Weather2.MODID)
public class CommonProxy
{
	public void preInit()
	{
		StormNames.init();
	}
	
	public void init()
	{
		SoundRegistry.init();
		EntityRegistry.init();
	}
	
	public void postInit()
	{
		WeatherAPI.refreshGrabRules();
		RecipeRegistry.postInit();
	}

	public void postPostInit()
	{
		
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
