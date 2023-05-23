package net.mrbt0907.weather2.server.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import CoroUtil.forge.CULog;
import modconfig.ConfigMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ClientConfigData;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.entity.EntityLightningBoltCustom;
import net.mrbt0907.weather2.network.packets.PacketEZGUI;
import net.mrbt0907.weather2.network.packets.PacketLightning;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.server.weather.WeatherManagerServer;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilConfig;

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
		if (world != null) {
			if (world.getTotalWorldTime() % ConfigMisc.auto_save_interval == 0) {
				Weather2.writeOutData(false);
			}
		}
		
		World worlds[] = DimensionManager.getWorlds();
		int size = worlds.length;
		int dimension = 0;
		//add use of CSV of supported dimensions here once feature is added, for now just overworld
		
		for (int i = 0; i < size; i++)
		{
			dimension = worlds[i].provider.getDimension();
			if (!dimensionSystems.containsKey(dimension))
			{
				if (WeatherUtilConfig.isWeatherEnabled(dimension))
					addWeatherSystem(world);
				WeatherUtilConfig.dimNames.put(dimension, dimension + ":>  " + worlds[i].provider.getDimensionType().getName());
				WeatherUtilConfig.nbtServerData.getCompoundTag("dimData").setString("dima_" + dimension, dimension + ":>  " + worlds[i].provider.getDimensionType().getName());
				WeatherUtilConfig.nbtSaveDataServer();
			}
			
			if (dimensionSystems.containsKey(dimension))
				dimensionSystems.get(i).tick();
		}

		if (ConfigMisc.aesthetic_mode) {
			if (!ConfigMisc.overcast_mode) {
				ConfigMisc.overcast_mode = true;
				CULog.dbg("detected Aesthetic_Only_Mode on, setting overcast mode on");
				WeatherUtilConfig.setOvercastModeServerSide(ConfigMisc.overcast_mode);
				ConfigMod.forceSaveAllFilesFromRuntimeSettings();
				syncServerConfigToClient();
			}
		}

		//TODO: only sync when things change? is now sent via PlayerLoggedInEvent at least
		if (world.getTotalWorldTime() % 200 == 0) {
			syncServerConfigToClient();
		}
		
		boolean testRainRequest = false;
		if (testRainRequest) {
			
			List<IMCMessage> listMsgs = new ArrayList<IMCMessage>();
			listMsgs = FMLInterModComms.fetchRuntimeMessages(Weather2.MODID);
			for (int i = 0; i < listMsgs.size(); i++) {
				
				//System.out.println("Weather2 side: " + listMsgs.get(i).key + " - modID: " + listMsgs.get(i).getSender() + " - source: " + listMsgs.get(i).toString() + " - " + listMsgs.get(i).getNBTValue());
				
				if (listMsgs.get(i).key.equals("weather.raining")) {

					NBTTagCompound nbt = listMsgs.get(i).getNBTValue();
					
					String replyMod = nbt.getString("replymod");
					nbt.setBoolean("isRaining", true);
					
					FMLInterModComms.sendRuntimeMessage(replyMod, replyMod, "weather.raining", nbt);
					
				}
			}
			
		}
		
		
		boolean debugIMC = false;
		if (debugIMC) {
			try {
				List<IMCMessage> listMsgs = new ArrayList<IMCMessage>();
				listMsgs = FMLInterModComms.fetchRuntimeMessages(Weather2.MODID);
				for (int i = 0; i < listMsgs.size(); i++) {
					
					//System.out.println(listMsgs.get(i).key + " - modID: " + listMsgs.get(i).getSender() + " - source: " + listMsgs.get(i).toString() + " - " + listMsgs.get(i).getNBTValue());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		boolean testCustomLightning = false;
		if (testCustomLightning) {
			if (world.getTotalWorldTime() % 20 == 0) {
				EntityPlayer player = world.getClosestPlayer(0, 0, 0, -1, false);
				if (player != null) {
					EntityLightningBoltCustom lightning = new EntityLightningBoltCustom(world, player.posX, player.posY, player.posZ);
					world.addWeatherEffect(lightning);
					PacketLightning.spawnLightning(world.provider.getDimension(), lightning, true);
				}
			}
		}

		boolean derp = false;
		if (derp) {
			if (world.getTotalWorldTime() % 2 == 0) {
				EntityPlayer player = world.getClosestPlayer(0, 0, 0, -1, false);
				if (player != null) {
					ItemStack is = player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
					if (is != null && is.getItem() instanceof ItemSpade) {
						int y = world.getHeight(new BlockPos(player.posX, 0, player.posZ)).getY();
						System.out.println("y " + y);
						//BlockPos airAtPlayer = new BlockPos(player.posX, y, player.posZ);
						//IBlockState state = world.getBlockState(new BlockPos(player.posX, player.getEntityBoundingBox().minY-1, player.posZ));
						//if (state.getBlock() != Blocks.SAND) {
							//WeatherUtilBlock.floodAreaWithLayerableBlock(player.world, new Vec3(player.posX, player.posY, player.posZ), player.rotationYawHead, 15, 5, 2, CommonProxy.blockSandLayer, 4);
							WeatherUtilBlock.fillAgainstWallSmoothly(player.world, new Vec3(player.posX, y + 0.5D, player.posZ/*player.posX, player.posY, player.posZ*/), player.rotationYawHead, 15, 2, BlockRegistry.sand_layer);
						//}
					}
				}
			}
		}
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
	
	public static void removeWeatherSystem(int dim) {
		Weather2.debug("Weather2: Unregistering manager for dim: " + dim);
		WeatherManagerServer wm = dimensionSystems.get(dim);
		
		if (wm != null)
			dimensionSystems.remove(dim);
		
		//wm.readFromFile();
		wm.writeToFile();
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
		ClientConfigData.writeNBT(data);
		PacketEZGUI.apply(data);
	}

	public static void syncServerConfigToClientPlayer(EntityPlayerMP player) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		ClientConfigData.writeNBT(data);
		PacketEZGUI.apply(data, player);
	}
}
