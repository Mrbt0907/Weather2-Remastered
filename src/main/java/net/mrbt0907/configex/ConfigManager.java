package net.mrbt0907.configex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.event.ClientHandler;
import net.mrbt0907.configex.manager.ConfigInstance;
import net.mrbt0907.configex.manager.FieldInstance;
import net.mrbt0907.configex.network.NetworkHandler;

public class ConfigManager
{
	private static final Map<String, ConfigInstance> configs = new LinkedHashMap<String, ConfigInstance>();
	public static final boolean isRemote;
	
	static
	{
		isRemote = FMLCommonHandler.instance().getSide().equals(Side.CLIENT);
	}
	
	public static void readNBT(NBTTagCompound nbt)
	{
		ConfigModEX.debug("Reading provided config nbt data...");
		NBTTagCompound nbtManager = nbt.getCompoundTag("manager");
		nbtManager.getKeySet().forEach(key ->
		{
			ConfigInstance instance = configs.get(key);
			if (instance == null)
				ConfigModEX.error(new NullPointerException("Recieved a non-existant nbt entry for configs. Skipping..."));
			else
			{
				NBTTagCompound nbtConfig = nbtManager.getCompoundTag(key);
				if (!isRemote)
				{
					if (nbt.hasUniqueId("player"))
						nbtConfig.setUniqueId("player", nbt.getUniqueId("player"));
				}
				else
					nbtConfig.setBoolean("setClient", nbt.getBoolean("setClient"));
				instance.readNBT(nbtConfig);
			}
		});
		ConfigModEX.debug("Finished reading config nbt data");
		if (!ConfigManager.isRemote)
		{
			ConfigModEX.debug("Sending nbt data back to all clients");
			NetworkHandler.sendClientPacket(0, ConfigManager.writeNBT(new NBTTagCompound()));
		}
	}
	
	public static NBTTagCompound writeNBT(NBTTagCompound nbt)
	{
		ConfigModEX.debug("Writing config nbt data for processing...");
		NBTTagCompound nbtManager = new NBTTagCompound();
		configs.forEach((name, config) -> 
		{
			NBTTagCompound nbtConfig = new NBTTagCompound();
			config.writeNBT(nbtConfig);
			nbtManager.setTag(name, nbtConfig);
		});
		ConfigModEX.debug("Finished writing config nbt data");
		nbt.setTag("manager", nbtManager);
		return nbt;
	}
	
	public static FieldInstance getFieldInstance(String configName, String fieldName)
	{
		configName = formatRegistryName(configName);
		fieldName = formatRegistryName(fieldName);
		
		ConfigInstance instance = configs.get(configName);
		if (instance == null)
		{
			ConfigModEX.error(new NullPointerException("Config instance was null. Skipping..."));
			return null;
		}
		return instance.getField(fieldName);
	}
	
	public static List<FieldInstance> getFieldInstances(String configName)
	{
		configName = formatRegistryName(configName);
		ConfigInstance instance = configs.get(configName);
		
		if (instance == null)
		{
			ConfigModEX.error(new NullPointerException("Config instance was null. Skipping..."));
			return null;
		}
		
		return instance.getFields();
	}
	
	public static ConfigInstance getInstance(IConfigEX config)
	{
		return getInstance(config.getName());
	}
	
	public static ConfigInstance getInstance(String configName)
	{
		return configs.get(formatRegistryName(configName));
	}
	
	public static List<ConfigInstance> getInstances()
	{
		return new ArrayList<ConfigInstance>(configs.values());
	}
	
	public static void sync(IConfigEX config)
	{
		sync(config.getName());
	}
	
	public static void sync(String configName)
	{
		configName = formatRegistryName(configName);
		ConfigModEX.debug("Requested sync for config " + configName);
		ConfigInstance instance = configs.get(configName);
		
		if (instance == null)
		{
			ConfigModEX.error(new NullPointerException("Config instance was null. Skipping..."));
			return;
		}
		
		instance.updateAllFields(true);
		instance.writeConfigFile(false);
		
		if (!isRemote)
		{
			ConfigModEX.debug("Sending information to all clients...");
			NetworkHandler.sendClientPacket(0, ConfigManager.writeNBT(new NBTTagCompound()));
		}
	}
	
	public static void sync(IConfigEX config, String variableName)
	{
		sync(config.getName(), variableName);
	}
	
	public static void sync(String configName, String variableName)
	{
		configName = formatRegistryName(configName);
		configName = formatRegistryName(configName);
		ConfigModEX.debug("Requested sync for variable " + variableName + " in config " + configName);
		ConfigInstance instance = configs.get(configName);
		
		if (instance == null)
		{
			ConfigModEX.error(new NullPointerException("Config instance was null. Skipping..."));
			return;
		}
		
		FieldInstance field = instance.getField(variableName);
		
		if (field == null)
		{
			ConfigModEX.error(new NullPointerException("Field instance was null. Skipping..."));
			return;
		}
		
		instance.updateField(field, false);
		instance.writeConfigFile(false);
		
		if (!isRemote)
		{
			ConfigModEX.debug("Sending information to all clients...");
			NetworkHandler.sendClientPacket(0, ConfigManager.writeNBT(new NBTTagCompound()));
		}
	}
	
	/**Registers the config instance to the registry, and */
	public static IConfigEX register(IConfigEX config)
	{
		if (config == null)
			ConfigModEX.fatal(new NullPointerException("Config instance was null"));
		else if (config.getName() == null)
			ConfigModEX.fatal(new NullPointerException("Config instance has a null name"));
		else if (config.getSaveLocation() == null)
			ConfigModEX.fatal(new NullPointerException("Config instance has a null save location"));
		else if (configs.containsKey(formatRegistryName(config.getName())))
		{
			ConfigModEX.error(new IllegalArgumentException("Config instance was already registered. Skipping..."));
			return null;
		}	
		ConfigInstance instance = new ConfigInstance(config); 
		configs.put(instance.registryName, instance);
		ConfigModEX.info("Registered config " + config.getName());
		
		return config;
	}
	
	public static int size()
	{
		return configs.size();
	}
	
	public static int sizeFields()
	{
		int size = 0;
		for (ConfigInstance config : configs.values())
			size += config.size();
		return size;
	}
	
	public static void reset()
	{
		if (isRemote && !ClientHandler.inGame())
		{
			ConfigModEX.debug("Resetting all config values to orignal values...");
			configs.forEach((name, config) -> config.reset());
			return;
		}
		ConfigModEX.error("Cannot reset variables server side. Skipping call...");
	}
	
	public static int getPermissionLevel()
	{
		return getPermissionLevel(null);
	}
	
	public static int getPermissionLevel(UUID uuid)
	{
		if (isRemote)
		{
			FMLClientHandler handler = FMLClientHandler.instance();
			return handler.getClientPlayerEntity() == null ? 4 : handler.getClientPlayerEntity().getPermissionLevel();
		}
		else
		{
			if (uuid == null) return 4;
			PlayerList players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
			EntityPlayerMP player = players.getPlayerByUUID(uuid);
			return player == null ? 0 : players.getOppedPlayers().getPermissionLevel(player.getGameProfile());
		} 
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isSinglePlayer()
	{
		Minecraft mc = Minecraft.getMinecraft();
		return mc.isSingleplayer() || mc.world == null;
	}
	
	public static String[] getFieldIDs()
	{
		List<String> ids = new ArrayList<String>();
		String[] output;
		for (ConfigInstance config : configs.values())
			for(FieldInstance field : config.getFields())
				ids.add(field.registryName);
		output = new String[ids.size()];
		output = ids.toArray(output);
		return output;
	}
	
	public static String formatRegistryName(String name)
	{
		return name.trim().toLowerCase().replaceAll(" ", "_").replaceAll("[^a-zA-Z0-9_\\-:]", "");
	}
	
	public static String formatComment(String comment)
	{
		return formatComment(new String[] {comment});
	}
	
	public static String formatComment(String[] comment)
	{
		String output = "";
		
		for (String line : comment)
		{
			if (output.isEmpty())
				output = line;
			else
				output += Configuration.NEW_LINE + line;
		}
		
		return output.isEmpty() ? null : output;
	}

	public static String formatCommentForCFG(FieldInstance field)
	{
		if (field.comment == null) return null;
		return (field.comment.isEmpty() ? "" : field.comment + Configuration.NEW_LINE) + (field.type < 6 ? "Minimum: \"" + (field.type < 4 ? String.format("%d", (long) field.min) : field.min) + "\",  Maximum: \"" + (field.type < 4 ? String.format("%d", (long) field.max) : field.max) + "\""  + Configuration.NEW_LINE : "") + "Default: \"" + field.defaultValue + "\"";
	}
	

	public static String formatCommentForGui(String comment, String defaultValue, int type, boolean showMin, boolean showMax, double min, double max)
	{
		if (comment == null) return null;
		return (comment.isEmpty() ? "" : WordUtils.wrap(comment, 40) + Configuration.NEW_LINE) + (type < 6 ? (showMin ? TextFormatting.YELLOW + "Minimum: " + (type < 4 ? String.format("%d", (long) min) : min) + (showMax ? ",  " : Configuration.NEW_LINE) : "") + (showMax ? TextFormatting.YELLOW + "Maximum: " + (type < 4 ? String.format("%d", (long) max) : max) + ""  + Configuration.NEW_LINE : "") : "") + TextFormatting.GOLD + "Default: " + defaultValue;
	}
}
