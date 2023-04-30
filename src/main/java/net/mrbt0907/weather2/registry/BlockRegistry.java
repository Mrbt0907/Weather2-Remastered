package net.mrbt0907.weather2.registry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.block.*;

@SuppressWarnings("unused")
public class BlockRegistry
{
	private static RegistryEvent.Register<Block> registry;
	
	public static final Block wire = new BlockSensor();
	public static final BlockMachine radio = new BlockMachine(Material.CLAY);
	public static final Block wind_chimes = new BlockNewSiren();
	public static final Block air_horn_siren = new BlockNewSiren();
	public static final Block emergency_siren_alt = new BlockNewSiren();
	public static final Block emergency_siren_alt_manual = new BlockNewSiren();
	//public static final Block wind_sock = new BlockNewSensor(0.25F, 0.0D, false, false, true, true);
	//public static final Block thermometer = new BlockNewSensor(0.25F, 250.0D, true, false, false, false);
	//public static final Block hygrometer = new BlockNewSensor(0.25F, 250.0D, false, true, false, false);
	public static final Block weather_doppler_radar = new BlockNewRadar(1);
	public static final Block weather_pulse_radar = new BlockNewRadar(2);
	public static final Block weather_humidifier = new BlockNewWeatherConstructor();
	public static final Block weather_humidifier_2 = new BlockNewWeatherConstructor();
	public static final Block weather_conditioner = new BlockNewWeatherDeflector();
	public static final Block weather_conditioner_2 = new BlockNewWeatherDeflector();
	//public static final Block ground_sensor_unit = new BlockNewSensor(1.0F, 0.0D, true, true, true, true);
	public static final Block wind_vane = new BlockWindVane();
	public static final Block anemometer = new BlockAnemometer();
	
	public static final Block machineCase = new BlockMachine(Material.CLAY);
	public static final Block stormSensor = new BlockNewSensor(Material.CLAY, 0);
	public static final Block humiditySensor = new BlockNewSensor(Material.CLAY, 1);
	public static final Block rainSensor = new BlockNewSensor(Material.CLAY, 2);
	public static final Block temperatureSensor = new BlockNewSensor(Material.CLAY, 3);
	public static final Block windSensor = new BlockNewSensor(Material.CLAY, 4);
	public static final Block barometerSensor = new BlockNewSensor(Material.CLAY, 5);
	
	public static final Block tornado_sensor = new BlockSensor();
	public static final Block emergency_siren_manual = new BlockTSirenManual();
	public static final Block emergency_siren = new BlockSiren();
	public static final Block weather_radar = new BlockNewRadar();
	public static final Block weather_constructor = new BlockWeatherConstructor();
	public static final Block weather_deflector = new BlockWeatherDeflector();
	public static final Block sand_layer = new BlockSandLayer();
	
	public static void register(RegistryEvent.Register<Block> event)
	{
		Weather2.debug("Registering blocks...");
		registry = event;
		addBlock("tornado_sensor", tornado_sensor);
		addBlock("tornado_siren", emergency_siren);
		addBlock("tornado_siren_manual", emergency_siren_manual);
		addBlock("wind_vane", wind_vane);
		addBlock("weather_forecast", weather_radar);
		addBlock("weather_forecast_2", weather_doppler_radar);
		addBlock("weather_forecast_3", weather_pulse_radar);
		addBlock("weather_machine", weather_constructor);
		addBlock("weather_deflector", weather_deflector);
		addBlock("anemometer", anemometer);
		addBlock("sand_layer", sand_layer, false);
		
		addBlock("machine_case", machineCase);
		addBlock("storm_sensor", stormSensor);
		addBlock("humidity_sensor", humiditySensor);
		addBlock("rain_sensor", rainSensor);
		addBlock("temperature_sensor", temperatureSensor);
		addBlock("wind_sensor", windSensor);
		addBlock("barometer_sensor", barometerSensor);
		
		addTileEntity("tornado_siren", TileSiren.class);
		addTileEntity("tornado_siren_manual", TileEntityTSirenManual.class);
		addTileEntity("wind_vane", TileWindVane.class);
		addTileEntity("weather_forecast", TileRadar.class);
		addTileEntity("weather_machine", TileWeatherConstructor.class);
		addTileEntity("weather_deflector", TileWeatherDeflector.class);
		addTileEntity("anemometer", TileAnemometer.class);
		addTileEntity("machine_case", TileMachine.class);
		registry = null;
		Weather2.debug("Finished registering blocks");
	}
	
	private static void addTileEntity(String registry_name, Class<? extends TileEntity> tile)
	{
		if (tile != null)
			GameRegistry.registerTileEntity(tile, new ResourceLocation(Weather2.MODID, registry_name));
	}
	
	private static void addBlock(String registry_name, Block block)
	{
		addBlock(registry_name, null, block, true);
	}
	
	
	private static void addBlock(String registry_name, Block block, boolean creative_tab)
	{
		addBlock(registry_name, null, block, creative_tab);
	}
	
	private static void addBlock(String registry_name, String ore_dict_name, Block block)
	{
		addBlock(registry_name, ore_dict_name, block, true);
	}
	
	private static void addBlock(String registry_name, String ore_dict_name, Block block, boolean creative_tab)
	{
		if (registry != null)
		{
			block.setRegistryName(new ResourceLocation(Weather2.OLD_MODID, registry_name));
			block.setUnlocalizedName(registry_name);
			
			if (ore_dict_name != null)
				OreDictionary.registerOre(ore_dict_name, block);
			if (creative_tab)
				block.setCreativeTab(Weather2.TAB);
			
			registry.getRegistry().register(block);			
			
			ItemRegistry.add(block);
			Weather2.debug("Registered block " + block.getRegistryName().getResourceDomain() +  ":" + block.getRegistryName().getResourcePath());
			return;
		}
		
		Weather2.error("Registry event returned null");
	}
}
