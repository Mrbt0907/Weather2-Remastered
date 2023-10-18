package net.mrbt0907.weather2.server.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modconfig.ConfigMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.network.packets.PacketEZGUI;
import net.mrbt0907.weather2.server.weather.WeatherManagerServer;

public class ServerTickHandler
{
	//Main lookup method for dim to weather systems
	public static Map<Integer, WeatherManagerServer> dimensionSystems = new HashMap<Integer, WeatherManagerServer>();
	public static World lastWorld;
	public static NBTTagCompound worldNBT = new NBTTagCompound(); 
	
	public static void onTickInGame()
	{
		if (FMLCommonHandler.instance() == null || FMLCommonHandler.instance().getMinecraftServerInstance() == null)
		{
			return;
		}
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		World world = server.getWorld(0);
		
		if (world != null && lastWorld != world)
		{
			lastWorld = world;
		}
		
		//regularly save data
		if (world != null)
		{
			if (world.getTotalWorldTime() % ConfigMisc.auto_save_interval == 0)
				Weather2.writeOutData(false);
			
			Weather2.serverChunkUtil.tick();
		}
		
		World worlds[] = DimensionManager.getWorlds();
		World dim;
		List<Integer> removedManagers = new ArrayList<Integer>();
		int size = worlds.length;
		int dimension = 0;
		//add use of CSV of supported dimensions here once feature is added, for now just overworld
		
		for (int i = 0; i < size; i++)
		{
			dim = worlds[i];
			dimension = dim.provider.getDimension();
			if (!dimensionSystems.containsKey(dimension))
			{
				if (EZConfigParser.isWeatherEnabled(dimension))
					addWeatherSystem(dim);
				if (!EZConfigParser.dimNames.containsKey(dimension))
				{
					EZConfigParser.dimNames.put(dimension, dimension + ":>  " + dim.provider.getDimensionType().getName());
					EZConfigParser.nbtServerData.getCompoundTag("dimData").setString("dima_" + dimension, dimension + ":>  " + dim.provider.getDimensionType().getName());
					EZConfigParser.nbtSaveDataServer();
				}
			}
			
			if (dimensionSystems.containsKey(dimension))
			{
				if (EZConfigParser.isWeatherEnabled(dimension))
					dimensionSystems.get(dimension).tick();
				else
					removedManagers.add(dimension);
			}
		}

		for (int i : removedManagers)
			removeWeatherSystem(i);
		
		if (ConfigMisc.aesthetic_mode)
		{
			if (!ConfigMisc.overcast_mode)
			{
				ConfigMisc.overcast_mode = true;
				Weather2.debug("detected Aesthetic_Only_Mode on, setting overcast mode on");
				EZConfigParser.setOvercastModeServerSide(ConfigMisc.overcast_mode);
				ConfigMod.forceSaveAllFilesFromRuntimeSettings();
				syncServerConfigToClient();
			}
		}

		//TODO: only sync when things change? is now sent via PlayerLoggedInEvent at least
		if (world.getTotalWorldTime() % 200 == 0)
			syncServerConfigToClient();
	}
	
	//must only be used when world is active, soonest allowed is TickType.WORLDLOAD
	public static void addWeatherSystem(World world)
	{
		int dim = world.provider.getDimension();
		Weather2.debug("Registering Weather2 manager for dim: " + dim);
		WeatherManagerServer wm = new WeatherManagerServer(world);
		dimensionSystems.put(dim, wm);
		wm.readFromFile();
	}
	
	public static void removeWeatherSystem(int dim)
	{
		Weather2.debug("Weather2: Unregistering manager for dim: " + dim);
		WeatherManagerServer wm = dimensionSystems.get(dim);
		
		try
		{
			if (wm != null)
			{
				dimensionSystems.remove(dim);
				wm.writeToFile();
				wm.reset(true);
			}
		}
		catch(Exception e)
		{
			Weather2.error(e);
		}
	}

	public static void playerClientRequestsFullSync(EntityPlayerMP entP) {
		WeatherManagerServer wm = dimensionSystems.get(entP.world.provider.getDimension());
		if (wm != null) {
			wm.playerJoinedWorldSyncFull(entP);
		}
	}
	
	public static void reset() {
		Weather2.debug("Weather2: ServerTickHandler resetting");
		int size = dimensionSystems.size();
		Object[] set = dimensionSystems.keySet().toArray();
		
		for (int i = 0; i < size; i++)
				removeWeatherSystem((int) set[i]);

		//should never happen
		if (dimensionSystems.size() > 0)
		{
			Weather2.debug("Weather2: reset state failed to manually clear lists, dimensionSystems.size(): " + dimensionSystems.size() + " - forcing a full clear of lists");
			dimensionSystems.clear();
		}
	}
	
	public static WeatherManagerServer getWeatherSystemForDim(int dimID) {
		return dimensionSystems.get(dimID);
	}

	public static void syncServerConfigToClient() {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		//ClientConfigData.writeNBT(data);
		PacketEZGUI.apply(data);
	}

	public static void syncServerConfigToClientPlayer(EntityPlayerMP player) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		//ClientConfigData.writeNBT(data);
		PacketEZGUI.apply(data, player);
	}
}
