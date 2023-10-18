package net.mrbt0907.configex.command;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.manager.FieldInstance;

public class ClientCommandHandler
{
	private static final Minecraft MC = Minecraft.getMinecraft();
	
	public static void onRecieveCommand(int index, NBTTagCompound nbt)
	{
		EntityPlayerSP player = MC.player;
		String[] args;
		FieldInstance field;
		
		if (player != null)
			switch (index)
			{
				case 1:
					args = new String[] {nbt.getString("config"), nbt.getString("field")};
					field = ConfigManager.getFieldInstance(args[0], args[1]);
					
					if (field == null)
						say(TextFormatting.RED + args[0] + ":" + args[1] + " does not exist");
					else
						if (field.hasPermission() || !field.enforce && !field.hide)
							say("Field " + field.name + "\n-----  -----\nFrom: " + field.config.getName() + "\nDefault Value: " + field.defaultValue + "\nCurrent Value: " + String.valueOf(field.getRealCachedValue()));
						else
							say(TextFormatting.RED + field.name + " requires a higher permission level to access");
					break;
				case 2:
					args = new String[] {nbt.getString("config"), nbt.getString("field")};
					field = ConfigManager.getFieldInstance(args[0], args[1]);
					String value = nbt.getString("value");
					if (field == null)
						say(TextFormatting.RED + args[0] + ":" + args[1] + " does not exist");
					else
						if (field.hasPermission() || !field.enforce && !field.hide)
							if (!(field.enforce && !ConfigManager.isSinglePlayer()))
								if (field.setClientValue(value))
								{
									ConfigManager.save(field.config.getName(), field.registryName);
									say(field.name + " was set successfully! Client Value: " + field.getRealClientValue() + ", Server Value: " + field.getRealCachedValue());
								}
								else
									say(TextFormatting.RED + field.name + " was not set successfully");
							else
								say(TextFormatting.RED + field.name + " cannot be set client side while in the server");
						else
							say(TextFormatting.RED + field.name + " requires a higher permission level to set");
					break;
				case 3:
					args = new String[] {nbt.getString("config"), nbt.getString("field")};
					field = ConfigManager.getFieldInstance(args[0], args[1]);
					if (field == null)
						say(TextFormatting.RED + args[0] + ":" + args[1] + " does not exist");
					else
						if (field.hasPermission() || !field.enforce && !field.hide)
							if (!(field.enforce && !MC.isSingleplayer()))
							{
								field.setToDefault();
								ConfigManager.save(field.config.getName(), field.registryName);
								say(field.name + " was set to default successfully! Client Value: " + field.getRealClientValue() + ", Server Value: " + field.getRealCachedValue());
							}
							else
								say(TextFormatting.RED + field.name + " cannot be set to default client side while in the server");
						else
							say(TextFormatting.RED + field.name + " requires a higher permission level to set to default");
					break;
				default:
					ConfigModEX.warn("Client Command Handler recieved an invalid packet with index of " + index + ". Skipping...");
			}
		else
			ConfigModEX.warn("Packet " + index + " was rejected by the client command handler because player was null");
	}
	
	private static void say(String message)
	{
		if (MC.player == null) return;
		MC.player.sendMessage(new TextComponentString(message));
	}
}
