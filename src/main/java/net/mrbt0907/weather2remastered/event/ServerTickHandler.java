<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/event/ServerTickHandler.java
package net.mrbt0907.weather2remastered.event;
=======
package net.mrbt0907.weather2.event;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.modconfig.ConfigMod;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.network.packets.PacketEZGUI;
import net.mrbt0907.weather2.weather.WeatherManagerServer;
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/event/ServerTickHandler.java

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/event/ServerTickHandler.java
import java.util.stream.StreamSupport;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.config.ConfigMisc;
import net.mrbt0907.weather2remastered.gui.EZConfigParser;
import net.mrbt0907.weather2remastered.network.PacketEZGUI;
import net.mrbt0907.weather2remastered.weather.WeatherManagerServer;

public class ServerTickHandler
{
	//Main lookup method for dim to weather systems
	public static Map<String, WeatherManagerServer> dimensionSystems = new HashMap<String, WeatherManagerServer>();
	public static World lastWorld;
	public static CompoundNBT worldNBT = new CompoundNBT(); 
	
	public static void onTickInGame()
	{
		MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
		World world = server.getLevel(World.OVERWORLD);
		
		if (world != null && lastWorld != world)
		{
			lastWorld = world;
		}
		
		//regularly save data
		//if (world != null) if (world.getGameTime() % ConfigMisc.auto_save_interval == 0) Weather2Remastered.writeOutData(false);
		
		ServerWorld[] worlds = StreamSupport.stream(server.getAllLevels().spliterator(), false).toArray(ServerWorld[]::new);
		ServerWorld dim;
		List<String> removedManagers = new ArrayList<String>();
		int size = worlds.length;
		//add use of CSV of supported dimensions here once feature is added, for now just overworld
		
		for (int i = 0; i < size; i++)
		{
			dim = worlds[i].getLevel();
			if (worlds[i] == null) return;
			String dimension = dim.dimension().location().toString();

			if (!dimensionSystems.containsKey(dimension))
			{
				if (EZConfigParser.isWeatherEnabled(dimension)){
					addWeatherSystem(dim);
				}
				if (!EZConfigParser.dimNames.containsValue(dimension))
				{
					EZConfigParser.dimNames.put(i, dim.dimension().location().toString());
					EZConfigParser.nbtServerData.getCompound("dimData").putString("dima_" + i, dim.dimension().location().toString());
					EZConfigParser.nbtSaveDataServer();
				}
			}
			
			if (dimensionSystems.containsKey(dimension))
			{
				
				if (EZConfigParser.isWeatherEnabled(dimension))
					dimensionSystems.get(dimension).tick(false);
				else
					removedManagers.add(dimension);
			}
		}

		for (String i : removedManagers)
			removeWeatherSystem(i);
		
		if (ConfigMisc.aesthetic_mode)
		{
			if (!ConfigMisc.overcast_mode)
			{
				ConfigMisc.overcast_mode = true;
				Weather2Remastered.debug("detected Aesthetic_Only_Mode on, setting overcast mode on");
				EZConfigParser.setOvercastModeServerSide(ConfigMisc.overcast_mode);
				Weather2Remastered.error("Can't force save all files from runtime settings...");
				//ConfigMod.forceSaveAllFilesFromRuntimeSettings();
				syncServerConfigToClient();
			}
		}

		//TODO: only sync when things change? is now sent via PlayerLoggedInEvent at least
		if (world.getGameTime() % 200 == 0)
			syncServerConfigToClient();
	}
	
	//must only be used when world is active, soonest allowed is TickType.WORLDLOAD
	public static void addWeatherSystem(ServerWorld world)
	{
		String dim = world.dimension().location().toString();
		Weather2Remastered.debug("Registering Weather2 manager for dim: " + dim);
		WeatherManagerServer wm = new WeatherManagerServer(world);
		dimensionSystems.put(dim, wm);
		wm.readFromFile();
	}
	
	public static void removeWeatherSystem(String i)
	{
		Weather2Remastered.debug("Unregistering manager for dim: " + i);
		WeatherManagerServer wm = dimensionSystems.get(i);
		
		try
		{
			if (wm != null)
			{
				System.out.println("wm null");
				dimensionSystems.remove(i);
				wm.writeToFile();
				wm.reset(true);
			}
		}
		catch(Exception e)
		{
			Weather2Remastered.error(e);
		}
	}

	public static void playerClientRequestsFullSync(ServerPlayerEntity entP) {
		WeatherManagerServer wm = dimensionSystems.get(entP.level.dimension().location().toString());
		if (wm != null) {
			//System.out.println("Player joined world, syncing data to them!");
			wm.playerJoinedWorldSyncFull(entP);
		}
	}
	
	public static void reset() {
		Weather2Remastered.debug("Weather2: ServerTickHandler resetting");
		for (String key : dimensionSystems.keySet())
				removeWeatherSystem(key);
		//should never happen
		if (dimensionSystems.size() > 0)
		{
			Weather2Remastered.debug("Weather2: reset state failed to manually clear lists, dimensionSystems.size(): " + dimensionSystems.size() + " - forcing a full clear of lists");
			dimensionSystems.clear();
		}
	}
	
	public static WeatherManagerServer getWeatherSystemForDim(String dimID) {
		return dimensionSystems.get(dimID);
	}

	public static void syncServerConfigToClient() {
		//packets
		CompoundNBT data = new CompoundNBT();
		//ClientConfigData.writeNBT(data);
		PacketEZGUI.apply(data);
	}

	public static void syncServerConfigToClientPlayer(ServerPlayerEntity player) {
		//packets
		CompoundNBT data = new CompoundNBT();
		//ClientConfigData.writeNBT(data);
		PacketEZGUI.apply(data, player);
	}
}
=======

public class ServerTickHandler {
    public static Map<RegistryKey<World>, WeatherManagerServer> dimensionSystems = new HashMap<>();
    public static World lastWorld;
    public static CompoundNBT worldNBT = new CompoundNBT();

    public static void onTickInGame() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;

        ServerWorld world = server.getLevel(World.OVERWORLD);

        if (world != null && ServerTickHandler.lastWorld != world)
            ServerTickHandler.lastWorld = world;

        if (world != null)
            if (world.getGameTime() % ConfigMisc.auto_save_interval == 0)
                Weather2.writeOutData(false);

        Iterable<ServerWorld> worlds = server.getAllLevels();
        List<RegistryKey<World>> removedManagers = new ArrayList<>();

        for (ServerWorld dim : worlds) {
            RegistryKey<World> dimension = dim.dimension();
            ResourceLocation dimLocation = dimension.location();
            String dimPath = dimLocation.getPath();
            String dimFullPath = dimLocation.toString();

            EntityMovingBlock.updateEntities(dim);

            if (!ServerTickHandler.dimensionSystems.containsKey(dimension)) {
                if (EZConfigParser.isWeatherEnabled(dimFullPath)) {
                    ServerTickHandler.addWeatherSystem(dim);
                }

                if (!EZConfigParser.dimNames.containsKey(dimFullPath)) {
                    String dimName = dimLocation.toString();
                    EZConfigParser.dimNames.put(dimFullPath, dimFullPath + ":>  " + dimName);

                    CompoundNBT dimData = EZConfigParser.nbtServerData.getCompound("dimData");
                    dimData.putString("dima_" + dimFullPath, dimFullPath + ":>  " + dimName);
                    EZConfigParser.nbtSaveDataServer();
                }
            }

            if (ServerTickHandler.dimensionSystems.containsKey(dimension)) {
                if (EZConfigParser.isWeatherEnabled(dimFullPath))
                    ServerTickHandler.dimensionSystems.get(dimension).tick();
                else
                    removedManagers.add(dimension);
            }
        }

        for (RegistryKey<World> key : removedManagers)
            ServerTickHandler.removeWeatherSystem(key);

        if (ConfigMisc.aesthetic_mode) {
            if (!ConfigMisc.overcast_mode) {
                ConfigMisc.overcast_mode = true;
                Weather2.debug("detected Aesthetic_Only_Mode on, setting overcast mode on");
                EZConfigParser.setOvercastModeServerSide(ConfigMisc.overcast_mode);
                ConfigMod.forceSaveAllFilesFromRuntimeSettings();
                ServerTickHandler.syncServerConfigToClient();
            }
        }

        if (world != null && world.getGameTime() % 200 == 0)
            ServerTickHandler.syncServerConfigToClient();
    }

    public static void addWeatherSystem(ServerWorld world) {
        RegistryKey<World> dim = world.dimension();
        Weather2.debug("Registering Weather2 manager for dim: " + dim.location());
        WeatherManagerServer wm = new WeatherManagerServer(world);
        ServerTickHandler.dimensionSystems.put(dim, wm);
        wm.readFromFile();
    }

    public static void removeWeatherSystem(RegistryKey<World> dim) {
        Weather2.debug("Weather2: Unregistering manager for dim: " + dim.location());
        WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(dim);

        try {
            if (wm != null) {
                ServerTickHandler.dimensionSystems.remove(dim);
                wm.writeToFile();
                wm.reset(true);
            }
        } catch (Exception e) {
            Weather2.error(e);
        }
    }

    public static void playerClientRequestsFullSync(ServerPlayerEntity entP) {
        WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(entP.level.dimension());
        if (wm != null) {
            wm.playerJoinedWorldSyncFull(entP);
        }
    }

    public static void reset() {
        Weather2.debug("Weather2: ServerTickHandler resetting");
        int size = ServerTickHandler.dimensionSystems.size();
        Object[] set = ServerTickHandler.dimensionSystems.keySet().toArray();

        for (int i = 0; i < size; i++)
            ServerTickHandler.removeWeatherSystem((RegistryKey<World>) set[i]);

        EntityMovingBlock.resetEntities();

        if (ServerTickHandler.dimensionSystems.size() > 0) {
            Weather2.debug("Weather2: reset state failed to manually clear lists, dimensionSystems.size(): " + ServerTickHandler.dimensionSystems.size() + " - forcing a full clear of lists");
            ServerTickHandler.dimensionSystems.clear();
        }
    }

    public static WeatherManagerServer getWeatherSystemForDim(RegistryKey<World> dimKey) {
        return ServerTickHandler.dimensionSystems.get(dimKey);
    }

    public static WeatherManagerServer getWeatherSystemForDim(ResourceLocation dimLocation) {
        for (Map.Entry<RegistryKey<World>, WeatherManagerServer> entry : dimensionSystems.entrySet()) {
            if (entry.getKey().location().equals(dimLocation)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static void syncServerConfigToClient() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            syncServerConfigToClientPlayer(player);
        }
    }

    public static void syncServerConfigToClientPlayer(ServerPlayerEntity player) {
        CompoundNBT data = new CompoundNBT();
        PacketEZGUI.apply(data, player);
    }
}
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/event/ServerTickHandler.java
