package net.mrbt0907.weather2.registry;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.block.BlockAnemometer;
import net.mrbt0907.weather2.block.BlockSandLayer;
import net.mrbt0907.weather2.block.BlockSensor;
import net.mrbt0907.weather2.block.BlockSiren;
import net.mrbt0907.weather2.block.BlockTSirenManual;
import net.mrbt0907.weather2.block.BlockWeatherDeflector;
import net.mrbt0907.weather2.block.BlockRadar;
import net.mrbt0907.weather2.block.BlockWeatherConstructor;
import net.mrbt0907.weather2.block.BlockWindVane;
import net.mrbt0907.weather2.block.TileAnemometer;
import net.mrbt0907.weather2.block.TileSiren;
import net.mrbt0907.weather2.block.TileEntityTSirenManual;
import net.mrbt0907.weather2.block.TileWeatherDeflector;
import net.mrbt0907.weather2.block.TileRadar;
import net.mrbt0907.weather2.block.TileWeatherConstructor;
import net.mrbt0907.weather2.block.TileWindVane;

@SuppressWarnings("unused")
public class BlockRegistry
{
	private static RegistryEvent.Register<Block> registry;
	
	public static final Block wire = new BlockSensor();
	public static final Block radio = new BlockSensor();
	public static final Block wind_chimes = new BlockSensor();
	public static final Block air_horn_siren = new BlockSensor();
	public static final Block emergency_siren_alt = new BlockSensor();
	public static final Block emergency_siren_alt_manual = new BlockSensor();
	public static final Block wind_sock = new BlockSensor();
	public static final Block thermometer = new BlockSensor();
	public static final Block hygrometer = new BlockSensor();
	public static final Block weather_doppler_radar = new BlockSensor();
	public static final Block weather_pulse_radar = new BlockSensor();
	public static final Block weather_humidifier = new BlockSensor();
	public static final Block weather_humidifier_2 = new BlockSensor();
	public static final Block weather_conditioner = new BlockSensor();
	public static final Block weather_conditioner_2 = new BlockSensor();
	
	public static final Block tornado_sensor = new BlockSensor();
	public static final Block emergency_siren_manual = new BlockTSirenManual();
	public static final Block emergency_siren = new BlockSiren();
	public static final Block wind_vane = new BlockWindVane();
	public static final Block anemometer = new BlockAnemometer();
	public static final Block weather_radar = new BlockRadar();
	public static final Block weather_constructor = new BlockWeatherConstructor();
	public static final Block weather_deflector = new BlockWeatherDeflector();
	public static final Block sand_layer = new BlockSandLayer();
	
	public static void register(RegistryEvent.Register<Block> event)
	{
		Weather2.debug("Registering blocks...");
		registry = event;
		addBlock("tornado_sensor", tornado_sensor);
		addBlock("tornado_siren", emergency_siren, TileSiren.class);
		addBlock("tornado_siren_manual", emergency_siren_manual, TileEntityTSirenManual.class);
		addBlock("wind_vane", wind_vane, TileWindVane.class);
		addBlock("weather_forecast", weather_radar, TileRadar.class);
		addBlock("weather_machine", weather_constructor, TileWeatherConstructor.class);
		addBlock("weather_deflector", weather_deflector, TileWeatherDeflector.class);
		addBlock("anemometer", anemometer, TileAnemometer.class);
		addBlock("sand_layer", sand_layer, false);
		registry = null;
		Weather2.debug("Finished registering blocks");
	}
	
	private static void addBlock(String registry_name, Block block)
	{
		addBlock(registry_name, null, block, null, true);
	}
	
	private static void addBlock(String registry_name, Block block, boolean creative_tab)
	{
		addBlock(registry_name, null, block, null, creative_tab);
	}
	
	private static void addBlock(String registry_name, String ore_dict_name, Block block)
	{
		addBlock(registry_name, ore_dict_name, block, null, true);
	}
	
	private static void addBlock(String registry_name, Block block, Class<? extends TileEntity> tile)
	{
		addBlock(registry_name, null, block, tile, true);
	}
	
	
	private static void addBlock(String registry_name, Block block, Class<? extends TileEntity> tile, boolean creative_tab)
	{
		addBlock(registry_name, null, block, tile, creative_tab);
	}
	
	private static void addBlock(String registry_name, String ore_dict_name, Block block, Class<? extends TileEntity> tile)
	{
		addBlock(registry_name, ore_dict_name, block, tile, true);
	}
	
	private static void addBlock(String registry_name, String ore_dict_name, Block block, Class<? extends TileEntity> tile, boolean creative_tab)
	{
		if (registry != null)
		{
			block.setRegistryName(registry_name);
			block.setUnlocalizedName(registry_name);
			
			if (ore_dict_name != null)
				OreDictionary.registerOre(ore_dict_name, block);
			if (creative_tab)
				block.setCreativeTab(Weather2.TAB);
			
			registry.getRegistry().register(block);			
			if (tile != null)
				GameRegistry.registerTileEntity(tile, block.getRegistryName());
			
			ItemRegistry.add(block);
			Weather2.debug("Registered block " + block.getRegistryName().getResourceDomain() +  ":" + block.getRegistryName().getResourcePath());
			return;
		}
		
		Weather2.error("Registry event returned null");
	}
}
