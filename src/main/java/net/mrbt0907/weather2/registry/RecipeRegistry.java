package net.mrbt0907.weather2.registry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreIngredient;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ConfigMisc;

public class RecipeRegistry
{
	private static final ResourceLocation GROUP = new ResourceLocation(Weather2.MODID, "weather2_misc"); 
	
	public static void postInit()
	{
		Weather2.debug("Registering recipies...");
		//addShaped(ConfigMisc.disable_weather_item, new ItemStack(ItemRegistry.itemWeatherRecipe, 1), "X X,DID,X X", "ingotIron", "dustRedstone", "ingotGold");
		//addShaped(ConfigMisc.disable_sensor, new ItemStack(BlockRegistry.tornado_sensor, 1), "X X,DID,X X", "ingotIron", "dustRedstone", ItemRegistry.itemWeatherRecipe);
		//addShaped(ConfigMisc.disable_siren, new ItemStack(BlockRegistry.emergency_siren, 1), "XDX,DID,XDX", "ingotIron", "dustRedstone", BlockRegistry.tornado_sensor);
		//addShaped(ConfigMisc.disable_manual_siren, new ItemStack(BlockRegistry.emergency_siren_manual, 1), "XLX,DID,XLX", "ingotIron", Blocks.LEVER, "dustRedstone", BlockRegistry.tornado_sensor);
		addShaped(ConfigMisc.disable_wind_vane, new ItemStack(BlockRegistry.wind_vane, 1), "X X,DXD,X X", ItemRegistry.itemWeatherRecipe, "dustRedstone");
		addShaped(ConfigMisc.disable_anemometer, new ItemStack(BlockRegistry.anemometer, 1), "X X,XDX,X X", ItemRegistry.itemWeatherRecipe, "dustRedstone");
		//addShaped(ConfigMisc.disable_weather_radar, new ItemStack(BlockRegistry.weather_radar, 1), "XDX,DID,XDX", ItemRegistry.itemWeatherRecipe, "dustRedstone", Items.COMPASS);
		//addShaped(ConfigMisc.disable_weather_machine, new ItemStack(BlockRegistry.weather_constructor, 1), "XDX,DID,XDX", ItemRegistry.itemWeatherRecipe, "dustRedstone", "gemDiamond");
		//addShaped(ConfigMisc.disable_weather_deflector, new ItemStack(BlockRegistry.weather_deflector, 1), "XDX,DID,XDX", "ingotIron", "dustRedstone", ItemRegistry.itemWeatherRecipe);
		addShaped(ConfigMisc.disable_sand_layer, new ItemStack(ItemRegistry.itemSandLayer, 64), "DDD,DID,DDD", "sand", ItemRegistry.itemWeatherRecipe);
		addShaped(ConfigMisc.disable_sand, new ItemStack(Blocks.SAND, 1), "DDD,D D,DDD", ItemRegistry.itemSandLayer);
		addShaped(ConfigMisc.disable_pocket_sand, new ItemStack(ItemRegistry.itemPocketSand, 8), "DDD,DID,DDD", ItemRegistry.itemSandLayer, ItemRegistry.itemWeatherRecipe);
		addShaped(false, new ItemStack(BlockRegistry.machineCase, 1), "ABA,BCB,ABA", "ingotIron", "blockGlass", "ingotGold");
		addShaped(false, new ItemStack(ItemRegistry.itemWeatherRecipe, 1), "ABA,ACA, B ", "ingotIron", "obsidian", "gemDiamond");
		
		addShaped(ConfigMisc.disable_sensor, new ItemStack(BlockRegistry.tornado_sensor, 1), "ABA,CDE,AFA", "ingotIron", new ItemStack(ItemRegistry.itemCPU1), new ItemStack(BlockRegistry.humiditySensor), new ItemStack(BlockRegistry.machineCase), new ItemStack(BlockRegistry.temperatureSensor), new ItemStack(ItemRegistry.itemAntenna1, 1));
		addShaped(ConfigMisc.disable_siren, new ItemStack(BlockRegistry.emergency_siren, 1), "ABA,CDC,ABA", "ingotIron", new ItemStack(ItemRegistry.itemCPU1), new ItemStack(BlockRegistry.tornado_sensor, 1), new ItemStack(BlockRegistry.emergency_siren_manual, 1));
		addShaped(ConfigMisc.disable_manual_siren, new ItemStack(BlockRegistry.emergency_siren_manual, 1), "ABA,CDC,ABA", "ingotIron", "dustRedstone", new ItemStack(ItemRegistry.itemSpeaker), new ItemStack(BlockRegistry.machineCase));
		addShaped(ConfigMisc.disable_weather_machine, new ItemStack(BlockRegistry.weather_constructor, 1), "ABA,CDC,CEC", new ItemStack(ItemRegistry.itemCPU2), new ItemStack(ItemRegistry.itemAntenna2, 1), "ingotIron", new ItemStack(BlockRegistry.machineCase, 1), new ItemStack(ItemRegistry.itemWeatherRecipe, 1));
		addShaped(ConfigMisc.disable_weather_deflector, new ItemStack(BlockRegistry.weather_deflector, 1), "ABA,CDC,CEC", new ItemStack(ItemRegistry.itemAntenna2, 1), new ItemStack(ItemRegistry.itemCPU2), "ingotIron", new ItemStack(BlockRegistry.machineCase), new ItemStack(ItemRegistry.itemWeatherRecipe, 1));
		addShaped(ConfigMisc.disable_weather_radar, new ItemStack(BlockRegistry.weather_radar, 1), "ABA,CDC,AEA", "ingotIron", new ItemStack(ItemRegistry.itemLCD1, 1), new ItemStack(BlockRegistry.tornado_sensor, 1), new ItemStack(BlockRegistry.machineCase), new ItemStack(ItemRegistry.itemCPU0, 1));
		addShaped(ConfigMisc.disable_weather_radar, new ItemStack(BlockRegistry.weather_doppler_radar, 1), "AAA,BCB,DED", new ItemStack(ItemRegistry.itemLCD1, 1), new ItemStack(ItemRegistry.itemCPU1, 1), new ItemStack(BlockRegistry.weather_radar), "ingotIron", new ItemStack(ItemRegistry.itemAntenna1, 1));
		addShaped(ConfigMisc.disable_weather_radar, new ItemStack(BlockRegistry.weather_pulse_radar, 1), "AAA,BCB,DED", new ItemStack(ItemRegistry.itemLCD1, 1), new ItemStack(ItemRegistry.itemCPU2, 1), new ItemStack(BlockRegistry.weather_doppler_radar), "obsidian", new ItemStack(ItemRegistry.itemAntenna2, 1));
		
		addShaped(false, new ItemStack(BlockRegistry.stormSensor, 1), "ABA,CDC,ABA", "obsidian", new ItemStack(ItemRegistry.itemWeatherRecipe, 1), new ItemStack(ItemRegistry.itemCPU2, 1), new ItemStack(BlockRegistry.tornado_sensor, 1));
		addShaped(false, new ItemStack(BlockRegistry.humiditySensor, 1), "ABA,CDE,AFA", "ingotIron", new ItemStack(Items.BUCKET), new ItemStack(ItemRegistry.itemDryBulb, 1), new ItemStack(BlockRegistry.rainSensor, 1), new ItemStack(ItemRegistry.itemWetBulb, 1), new ItemStack(ItemRegistry.itemCPU1, 1));
		addShaped(false, new ItemStack(BlockRegistry.rainSensor, 1), "ABA,CDC,ACA", "ingotIron", new ItemStack(Items.GLASS_BOTTLE, 1), "dustRedstone", new ItemStack(BlockRegistry.machineCase, 1));
		addShaped(false, new ItemStack(BlockRegistry.temperatureSensor, 1), "ABA,CDC,AEA", "ingotIron", new ItemStack(Items.BLAZE_ROD, 1), new ItemStack(ItemRegistry.itemBulb, 1), new ItemStack(BlockRegistry.machineCase, 1), "dustRedstone");
		addShaped(false, new ItemStack(BlockRegistry.windSensor, 1), "ABA, C ,ADA", "ingotIron", new ItemStack(BlockRegistry.anemometer, 1), new ItemStack(BlockRegistry.machineCase, 1), "dustRedstone");
		
		addShaped(false, new ItemStack(ItemRegistry.itemMotor, 1), "ABA, C ,ABA", "ingotIron", "dustRedstone", "ingotGold");
		addShaped(false, new ItemStack(ItemRegistry.itemSpeaker, 2), "ABA,ACA, B ", "wool", "ingotIron", "dustRedstone");
		addShaped(false, new ItemStack(ItemRegistry.itemAntenna0, 1), " A ,BCB,DBD", "blockRedstone", "ingotIron", "dustRedstone", new ItemStack(ItemRegistry.itemMotor, 1));
		addShaped(false, new ItemStack(ItemRegistry.itemAntenna1, 1), "ABA,CDC,ABA", "ingotIron", "dustRedstone", "gemDiamond", new ItemStack(ItemRegistry.itemAntenna0, 1));
		addShaped(false, new ItemStack(ItemRegistry.itemAntenna2, 1), "ABA,CDC,ABA", "obsidian", "dustRedstone", new ItemStack(ItemRegistry.itemWeatherRecipe, 1), new ItemStack(ItemRegistry.itemAntenna1, 1));
		addShaped(false, new ItemStack(ItemRegistry.itemCPU0, 1), "ABA,CDC,ABA", "ingotIron", "dustRedstone", "paneGlass", "ingotGold");
		addShaped(false, new ItemStack(ItemRegistry.itemCPU1, 1), "ABA,BCB,ABA", "ingotIron", "ingotGold", new ItemStack(ItemRegistry.itemCPU0, 1));
		addShaped(false, new ItemStack(ItemRegistry.itemCPU2, 1), "ABA,BCB,ABA", "obsidian", "gemDiamond", new ItemStack(ItemRegistry.itemCPU1, 1));
		addShaped(false, new ItemStack(ItemRegistry.itemBulb, 6), "A A,A A,AAA", "paneGlass");
		addShaped(false, new ItemStack(ItemRegistry.itemDryBulb, 1), " A , B , C ", "ingotIron", "sand", new ItemStack(ItemRegistry.itemBulb, 1));
		addShaped(false, new ItemStack(ItemRegistry.itemWetBulb, 1), " A , B , C ", "ingotIron", "wool", new ItemStack(ItemRegistry.itemBulb, 1));
		addShaped(false, new ItemStack(ItemRegistry.itemLCD0, 3), "ABA,BCB,ABA", "paneGlass", "dustRedstone", "dustGlowstone");
		addShaped(false, new ItemStack(ItemRegistry.itemLCD1, 1), "AAA,AAA,AAA", new ItemStack(ItemRegistry.itemLCD0, 1));
		Weather2.debug("Finished registering recipies...");
	}
	
	private static void addShaped(boolean disabled, ItemStack output, String pattern, Object... inputs)
	{
		if (output != null && pattern != null && pattern.length() > 0 && inputs.length > 0)
		{
			if (disabled)
			{
				Weather2.debug("Recipe " + output.getItem().getRegistryName().getNamespace() + ":" + output.getItem().getRegistryName().getPath() + " is disabled. Skipping...");
				return;
			}
			
			Object[] args = null;
			List<Character> count = new ArrayList<Character>();
			int in = 0; int index = 0; Character c; String[] s = {"", null, null};
			
			for (int i = 0; i < pattern.length(); i++)
			{
				c = pattern.charAt(i);
				
				if (c.equals(','))
				{
					index++;
					in = 0;
					s[index] = "";
				}
				else if (in < 3)
				{
					s[index] += c;
					if (!c.equals(' ') && !count.contains(c))
						count.add(c);
					in++;
				}
			}
			
			if (count.size() <= 0)
			{
				Weather2.error("Pattern returned null for recipe " + output.getItem().getRegistryName().getNamespace() + ":" + output.getItem().getRegistryName().getPath() + ".  Skipping recipe...");
				return;
			}
			
			in = 0;
			for (String str : s)
				if (str != null)
					in++;
			
			args = new Object[(count.size() * 2) + in];
			for (int i = 0; i < 3; i++)
				if (s[i] != null)
					args[i] = s[i];
			
			for (int i = 0; i < inputs.length && i < count.size(); i++)
			{
				args[in] = count.get(i);
				if (inputs[i] instanceof String)
					args[in + 1] = new OreIngredient(inputs[i].toString());
				else
					args[in + 1] = inputs[i];
				in += 2;
			}
			
			Weather2.debug("Adding shaped recipe for "  + output.getItem().getRegistryName().getNamespace() + ":" + output.getItem().getRegistryName().getPath());
			GameRegistry.addShapedRecipe(output.getItem().getRegistryName(), GROUP, output, args);
		}
		else
			Weather2.error("Recipe is missing parameters");
	}
}
