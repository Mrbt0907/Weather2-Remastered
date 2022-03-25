package net.mrbt0907.weather2.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.item.ItemPocketSand;
import net.mrbt0907.weather2.item.ItemSandLayer;
import net.mrbt0907.weather2.item.ItemWeatherRecipe;

public class ItemRegistry
{
	private static RegistryEvent.Register<Item> registry;
	private static final List<Block> item_blocks = new ArrayList<Block>();
	
	public static final Item sensor = new ItemPocketSand();
	public static final Item thermometer = new ItemPocketSand();
	public static final Item hygrometer = new ItemPocketSand();
	public static final Item anemometer = new ItemPocketSand();
	public static final Item radio = new ItemPocketSand();
	
	public static final Item itemSandLayer = new ItemSandLayer(BlockRegistry.sand_layer);
	public static final Item itemWeatherRecipe = new ItemWeatherRecipe();
	public static final Item itemPocketSand = new ItemPocketSand();
	
	public static void add(Block block)
	{
		item_blocks.add(block);
	}
	
	public static void register(RegistryEvent.Register<Item> event)
	{
		Weather2.debug("Registering items...");
		registry = event;
		add("sand_layer_placeable", itemSandLayer);
		add("weather_item", itemWeatherRecipe);
		add("pocket_sand", itemPocketSand);
		for (Block block : item_blocks)
			add(block.getRegistryName().getResourcePath(), new ItemBlock(block));
		
		registry = null;
		Weather2.debug("Finished registering items");
	}
	
	public static void add(String name, Item item)
	{
		add(name, null, item);
	}
	
	public static void add(String name, String ore_dict_name, Item item)
	{
		if (registry != null)
		{
			item.setRegistryName(name);
			item.setUnlocalizedName(name);
			
			if (ore_dict_name != null)
				OreDictionary.registerOre(ore_dict_name, item);
			item.setCreativeTab(Weather2.TAB);
			registry.getRegistry().register(item);
			
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
			
			Weather2.debug("Registered item " + item.getRegistryName().getResourceDomain() +  ":" + item.getRegistryName().getResourcePath());
			return;
		}
		Weather2.error("Registry event returned null");
	}
}
