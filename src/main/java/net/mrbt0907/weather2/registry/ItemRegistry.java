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
import net.mrbt0907.weather2.item.ItemSensor;
import net.mrbt0907.weather2.item.ItemWeatherRecipe;

public class ItemRegistry
{
	private static RegistryEvent.Register<Item> registry;
	private static final List<Block> item_blocks = new ArrayList<Block>();
	
	public static final Item sensor = new ItemSensor(0);
	public static final Item thermometer = new ItemSensor(1);
	public static final Item hygrometer = new ItemSensor(2);
	public static final Item anemometer = new ItemSensor(3);
	public static final Item radio = new ItemPocketSand();
	
	public static final Item itemMotor = new Item();
	public static final Item itemSpeaker = new Item();
	public static final Item itemAntenna0 = new Item();
	public static final Item itemAntenna1 = new Item();
	public static final Item itemAntenna2 = new Item();
	public static final Item itemCPU0 = new Item();
	public static final Item itemCPU1 = new Item();
	public static final Item itemCPU2 = new Item();
	public static final Item itemBulb = new Item();
	public static final Item itemDryBulb = new Item();
	public static final Item itemWetBulb = new Item();
	public static final Item itemLCD0 = new Item();
	public static final Item itemLCD1 = new Item();
	
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
		

		add("handheld_thermometer", thermometer);
		add("handheld_hygrometer", hygrometer);
		add("handheld_anemometer", anemometer);
		
		add("motor", itemMotor);
		add("speaker", itemSpeaker);
		add("antenna_0", itemAntenna0);
		add("antenna_1", itemAntenna1);
		add("antenna_2", itemAntenna2);
		add("cpu_0", itemCPU0);
		add("cpu_1", itemCPU1);
		add("cpu_2", itemCPU2);
		add("bulb", itemBulb);
		add("bulb_dry", itemDryBulb);
		add("bulb_wet", itemWetBulb);
		add("lcd_0", itemLCD0);
		add("lcd_1", itemLCD1);
		
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
