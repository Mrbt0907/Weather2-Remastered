package net.mrbt0907.configex.server;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.mrbt0907.configex.Test;
import net.mrbt0907.configex.api.ConfigAPI;

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
		return "command.configex.usage";
	}
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}
	
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
	{
		switch(args.length - 1)
		{
			default:
				return getListOfStringsMatchingLastWord(args, new String[] {"save", "test"});
		}
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
	{
		int size = args.length;
		String[] list = new String[] {"a", "b", "c", "d", "e", "f"};
		
		if (size > 0)
			switch(args[0].toLowerCase())
			{
				case "save":
					ConfigAPI.saveConfigChanges();
					break;
				case "test":
					Test.double_example = Math.random() * 200.0D;
					Test.float_example = (float) Math.random() * 200.0F;
					Test.long_example = (long) (Math.random() * 200.0D);
					Test.short_example = (short) (Math.random() * 200.0D);
					Test.int_example = (int)(Math.random() * 200.0D);
					Test.string_example = Math.random() + " + " + Math.random();
					Test.list_integer_example = Arrays.asList(list[(int)(Math.random() * list.length)], list[(int)(Math.random() * list.length)], list[(int)(Math.random() * list.length)], list[(int)(Math.random() * list.length)], list[(int)(Math.random() * list.length)], list[(int)(Math.random() * list.length)], list[(int)(Math.random() * list.length)], list[(int)(Math.random() * list.length)], list[(int)(Math.random() * list.length)], list[(int)(Math.random() * list.length)]);
					break;
				case "load":
					if (sender instanceof EntityPlayerMP)
					{
						EntityPlayerMP player = (EntityPlayerMP) sender;
						player.sendMessage(new TextComponentString("double: " + Test.double_example));
						player.sendMessage(new TextComponentString("float: " + Test.float_example));
						player.sendMessage(new TextComponentString("long: " + Test.long_example));
						player.sendMessage(new TextComponentString("short: " + Test.short_example));
						player.sendMessage(new TextComponentString("int: " + Test.int_example));
						player.sendMessage(new TextComponentString("string: " + Test.string_example));
						player.sendMessage(new TextComponentString("list: " + Test.list_integer_example));
						player.sendMessage(new TextComponentString("map: " + Test.map_example));
					}
					break;
				case "default":
					ConfigAPI.resetToDefault(ConfigAPI.getConfig(new ResourceLocation("test")));
					break;
			}
		else
			say(sender, "usage");
	}
		
	private void say(ICommandSender sender, String localizationID, Object... args)
	{
		notifyCommandListener(sender, this, "command." + getName() + "." + localizationID, args);
	}
}
