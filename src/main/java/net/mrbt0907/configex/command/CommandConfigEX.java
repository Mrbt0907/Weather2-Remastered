package net.mrbt0907.configex.command;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.manager.FieldInstance;
import net.mrbt0907.configex.network.NetworkHandler;
import scala.actors.threadpool.Arrays;

public class CommandConfigEX extends CommandBase
{

	@Override
	public String getName()
	{
		return "configex";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "command." + getName() + ".usage";
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
		return true;
    }
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
	{
		switch(args.length)
		{
			case 1:
				return getListOfStringsMatchingLastWord(args, new String[] {"get", "set", "default", "refresh", "config", "version"});
			case 2:
				switch(args[0])
				{
					case "get": case "set": case "default": case "refresh":
						return getListOfStringsMatchingLastWord(args, new String[] {"client", "server"});
				}
			case 3:
				switch(args[0])
				{
					case "get": case "set": case "default":
						return getListOfStringsMatchingLastWord(args, ConfigManager.getFieldIDs());
				}
			case 4:
				switch(args[0])
				{
					case "set":
						return getListOfStringsMatchingLastWord(args, new String[] {"<value>"});
				}
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length > 0)
			switch(args[0].toLowerCase())
			{
				case "get":
					if (args.length > 2)
						get(server, sender, args[1].equalsIgnoreCase("server"), args[2]);
					else
						say(sender, TextFormatting.RED + "Usage: /" + getName() + " get <client/server> <ConfigID:VariableID>");
					break;
				case "set":
					if (args.length > 3)
						set(server, sender, args[1].equalsIgnoreCase("server"), args[2], String.join(" ", (String[]) Arrays.copyOfRange(args, 3, args.length)));
					else
						say(sender, TextFormatting.RED + "Usage: /" + getName() + " set <client/server> <ConfigID:VariableID> <New Value>");
					break;
				case "default":
					if (args.length > 2)
						setDefault(server, sender, args[1].equalsIgnoreCase("server"), args[2]);
					else
						say(sender, TextFormatting.RED + "Usage: /" + getName() + " default <client/server> <ConfigID:VariableID>");
					break;
				case "config":
					if (sender instanceof EntityPlayerMP)
					{
						say(sender, "Opening config gui...");
						sendPacket(5, new NBTTagCompound(), sender);
					}
					else
						say(sender, TextFormatting.RED + "You cannot run this sub command in the console");
					break;
				case "refresh":
					break;
				case "version":
					break;
				default:
					say(sender, TextFormatting.RED + "Usage: /" + getName() + " <config/default/get/set/refresh/version>");
			}
		else
			say(sender, TextFormatting.RED + "Usage: /" + getName() + " <config/default/get/set/refresh/version>");
	}
	
	private void setDefault(MinecraftServer server, ICommandSender sender, boolean toServer, String registryName)
	{
		int permissionLevel = getPermission(server, sender);
		String[] registryNames = registryName.split(":");
		
		if (registryNames.length > 1)
		{
			if (toServer && !ConfigManager.isRemote)
			{
				FieldInstance field = ConfigManager.getFieldInstance(registryNames[0], registryNames[1]);
				
				if (field == null)
					say(sender, TextFormatting.RED + registryName + " does not exist");
				else
					if (field.hasPermission(permissionLevel))
					{
						field.setToDefault();
						ConfigManager.sync(field.config.getName(), field.registryName);
						say(sender, field.name + " was set to default successfully! Value: " + field.getRealCachedValue());
					}
					else
						say(sender, TextFormatting.RED + field.name + " requires a higher permission level to set");
			}
			else
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("config", registryNames[0]);
				nbt.setString("field", registryNames[1]);
				sendPacket(3, nbt, sender);
			}
		}
		else
			say(sender, TextFormatting.RED + registryName + " is not a valid registry name");
	}
	
	
	private void set(MinecraftServer server, ICommandSender sender, boolean toServer, String registryName, String value)
	{
		int permissionLevel = getPermission(server, sender);
		String[] registryNames = registryName.split(":");
		
		if (registryNames.length > 1)
		{
			if (toServer)
			{
				FieldInstance field = ConfigManager.getFieldInstance(registryNames[0], registryNames[1]);
				
				if (field == null)
					say(sender, TextFormatting.RED + registryName + " does not exist");
				else
					if (field.hasPermission(permissionLevel))
						if (field.setServerValue(value))
						{
							ConfigManager.sync(field.config.getName(), field.registryName);
							say(sender, field.name + " was set successfully! Value: " + field.getRealCachedValue());
						}
						else
							say(sender, TextFormatting.RED + field.name + " was not set successfully");
					else
						say(sender, TextFormatting.RED + field.name + " requires a higher permission level to set");
			}
			else
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("config", registryNames[0]);
				nbt.setString("field", registryNames[1]);
				nbt.setString("value", value);
				sendPacket(2, nbt, sender);
			}
		}
		else
			say(sender, TextFormatting.RED + registryName + " is not a valid registry name");
	}
	
	private void get(MinecraftServer server, ICommandSender sender, boolean fromServer, String registryName)
	{
		int permissionLevel = getPermission(server, sender);
		String[] registryNames = registryName.split(":");
		
		if (registryNames.length > 1)
		{
			if (fromServer)
			{
				FieldInstance field = ConfigManager.getFieldInstance(registryNames[0], registryNames[1]);
				if (field == null)
					say(sender,TextFormatting.RED + registryName + " does not exist");
				else
					if (field.hasPermission(permissionLevel))
						say(sender, "Field " + field.name + "\n-----  -----\nFrom: " + field.config.getName() + "\nDefault Value: " + field.defaultValue + "\nCurrent Value: " + String.valueOf(field.getRealCachedValue()));
					else
						say(sender, TextFormatting.RED + field.name + " requires a higher permission level to access");
			}
			else
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("config", registryNames[0]);
				nbt.setString("field", registryNames[1]);
				sendPacket(1, nbt, sender);
			}
		}
		else
			say(sender, TextFormatting.RED + registryName + " is not a valid registry name");
	}
	
	private int getPermission(MinecraftServer server, ICommandSender sender)
	{
		return sender instanceof EntityPlayerMP ? server.isSinglePlayer() ? 4 : server.getPlayerList().getOppedPlayers().getPermissionLevel(((EntityPlayerMP)sender).getGameProfile()) : 4;
	}
	
	private void sendPacket(int index, NBTTagCompound nbt, ICommandSender sender)
	{
		if (!(sender instanceof EntityPlayerMP))
		{
			say(sender, TextFormatting.RED + "You cannot run this sub command in the console");
			return;
		}
		
		nbt.setString("command", getName());
		NetworkHandler.sendClientPacket(index, nbt, sender);
	}
	
	private void say(ICommandSender sender, String message)
	{
		notifyCommandListener(sender, this, message);
	}
}
