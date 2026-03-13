package net.mrbt0907.configex.command;

<<<<<<< Updated upstream
import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.IConfigEX.Phase;
import net.mrbt0907.configex.manager.ConfigInstance;
import net.mrbt0907.configex.manager.FieldInstance;

public class CommandConfigEX
{
	@SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event)
	{
		ConfigModEX.info("Registering configEX command");
		CommandConfigEX command = new CommandConfigEX();
		List<ResourceLocation> ids = ConfigManager.ids();
		LiteralArgumentBuilder<CommandSource> cmdCfgEX = Commands.literal("configex")
		.requires(command::hasPermission)
		.then(Commands.literal("default")
			.then(Commands.argument("variable", ResourceLocationArgument.id())
				.suggests((ctx, builder) -> ISuggestionProvider.suggestResource(ids, builder))
					.executes(context -> command.executeDefault(context.getSource(), ResourceLocationArgument.getId(context, "variable")))
			)
			.executes(context -> command.onFailiure(context.getSource(), "configex", 1))
		)
		.then(Commands.literal("get")
			.then(Commands.argument("variable", ResourceLocationArgument.id())
				.suggests((ctx, builder) -> ISuggestionProvider.suggestResource(ids, builder))
					.executes(context -> command.executeGet(context.getSource(), ResourceLocationArgument.getId(context, "variable")))
			)
			.executes(context -> command.onFailiure(context.getSource(), "configex", 1))
		)
		.then(Commands.literal("set")
			.then(Commands.argument("variable", ResourceLocationArgument.id())
				.suggests((ctx, builder) -> ISuggestionProvider.suggestResource(ids, builder))
				.then(Commands.argument("value", StringArgumentType.string())
					.executes(context -> command.executeSet(context.getSource(), ResourceLocationArgument.getId(context, "variable"), StringArgumentType.escapeIfRequired(StringArgumentType.getString(context, "value"))))
				)
			)
			.executes(context -> command.onFailiure(context.getSource(), "configex", 1))
		)
		.executes(context -> command.onFailiure(context.getSource(), "configex", 0));
		event.getDispatcher().register(cmdCfgEX);
	}

	public boolean hasPermission(CommandSource source)
	{
		return source.hasPermission(2);
	}
	
	public int onFailiure(CommandSource source, String command, int index)
	{	
		switch(index)
		{
			case 0:
				source.sendSuccess(new StringTextComponent("/" + command + " <default/get/set>"), false);
				break;
			case 1:
				source.sendSuccess(new StringTextComponent("/" + command + " <default/get/set> <variable_id>"), false);
				break;
		}
		return 0;
	}
	
	public int executeDefault(CommandSource source, ResourceLocation registryName)
	{
		ConfigInstance config = ConfigManager.get(registryName.getNamespace());
		if (config == null)
		{
			source.sendFailure(new StringTextComponent("Config " + registryName.getNamespace() + " does not exist"));
			return 0;
		}
		FieldInstance field = config.get(registryName.getPath());
		if (field == null)
		{
			source.sendFailure(new StringTextComponent("Config variable " + registryName.toString() + " does not exist"));
			return 0;
		}
		
		config.instance.onConfigChanged(Phase.START, 0);
		field.set(field.defaultValue, !ConfigManager.IS_REMOTE).markDirty();
		config.instance.onConfigChanged(Phase.END, 1);
		ConfigManager.sync();
		field.save();
		source.sendSuccess(new StringTextComponent("Changed variable " + registryName.toString() + " to \"" + field.defaultValue + "\""), true);
		return 1;
	}
	
	public int executeGet(CommandSource source, ResourceLocation registryName)
	{
		ConfigInstance config = ConfigManager.get(registryName.getNamespace());
		if (config == null)
		{
			source.sendFailure(new StringTextComponent("Config " + registryName.getNamespace() + " does not exist"));
			return 0;
		}
		FieldInstance field = config.get(registryName.getPath());
		if (field == null)
		{
			source.sendFailure(new StringTextComponent("Config variable " + registryName.toString() + " does not exist"));
			return 0;
		}
		source.sendSuccess(new StringTextComponent("Variable " + registryName.toString() + " = \"" + field.getActualValue() + "\""), true);
		return 1;
	}
	
	public int executeSet(CommandSource source, ResourceLocation registryName, String value)
	{
		ConfigInstance config = ConfigManager.get(registryName.getNamespace());
		if (config == null)
		{
			source.sendFailure(new StringTextComponent("Config " + registryName.getNamespace() + " does not exist"));
			return 0;
		}
		FieldInstance field = config.get(registryName.getPath());
		if (field == null)
		{
			source.sendFailure(new StringTextComponent("Config variable " + registryName.toString() + " does not exist"));
			return 0;
		}
		
		Object oldValue = field.getActualValue();
		config.instance.onConfigChanged(Phase.START, 0);
		switch (field.type)
		{
			case 1:
				try {field.set(Integer.parseInt(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid integer")); config.instance.onConfigChanged(Phase.END, 0); return 0;}
				break;
			case 2:
				try {field.set(Short.parseShort(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid short")); config.instance.onConfigChanged(Phase.END, 0); return 0;}
				break;
			case 3:
				try {field.set(Long.parseLong(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid long")); config.instance.onConfigChanged(Phase.END, 0); return 0;}
				break;
			case 4:
				try {field.set(Float.parseFloat(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid float")); config.instance.onConfigChanged(Phase.END, 0); return 0;}
				break;
			case 5:
				try {field.set(Double.parseDouble(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid double")); config.instance.onConfigChanged(Phase.END, 0); return 0;}
				break;
			case 6:
				try {field.set(String.valueOf(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid string")); config.instance.onConfigChanged(Phase.END, 0); return 0;}
				break;
			case 7:
				try {field.set(Boolean.parseBoolean(value), !ConfigManager.IS_REMOTE);}
				catch (Exception e) {source.sendFailure(new StringTextComponent("Value \"" + value + "\" cannot be assigned to config variable " + registryName.toString() + " as it is not a valid boolean")); config.instance.onConfigChanged(Phase.END, 0); return 0;}
				break;
		}
		config.instance.onValueChanged(registryName.getPath(), oldValue, field.getActualValue());
		config.instance.onConfigChanged(Phase.END, 1);
		field.set(Boolean.parseBoolean(value), !ConfigManager.IS_REMOTE).markDirty();
		if (!ConfigManager.IS_REMOTE)
			ConfigManager.sync();
		field.save();
		source.sendSuccess(new StringTextComponent("Changed variable " + registryName.toString() + " to \"" + field.get() + "\""), true);
		return 1;
	}
}
=======
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.manager.FieldInstance;
import net.mrbt0907.configex.network.NetworkHandler;

public class CommandConfigEX
{
    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("configex")
                .executes(ctx -> {
                    say(ctx.getSource(), TextFormatting.RED + "Usage: /configex <config/default/get/set>");
                    return 1;
                })
                .then(Commands.literal("get")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests(CLIENT_SERVER_SUGGEST)
                                .then(Commands.argument("field", StringArgumentType.greedyString())
                                        .suggests(FIELD_SUGGEST)
                                        .executes(ctx -> {
                                            String mode = StringArgumentType.getString(ctx, "mode");
                                            String field = StringArgumentType.getString(ctx, "field");
                                            get(ctx.getSource(), mode.equalsIgnoreCase("server"), field);
                                            return 1;
                                        }))))
                .then(Commands.literal("set")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests(CLIENT_SERVER_SUGGEST)
                                .then(Commands.argument("field", StringArgumentType.word())
                                        .suggests(FIELD_SUGGEST)
                                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String mode = StringArgumentType.getString(ctx, "mode");
                                                    String field = StringArgumentType.getString(ctx, "field");
                                                    String value = StringArgumentType.getString(ctx, "value");
                                                    set(ctx.getSource(), mode.equalsIgnoreCase("server"), field, value);
                                                    return 1;
                                                })))))
                .then(Commands.literal("default")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests(CLIENT_SERVER_SUGGEST)
                                .then(Commands.argument("field", StringArgumentType.greedyString())
                                        .suggests(FIELD_SUGGEST)
                                        .executes(ctx -> {
                                            String mode = StringArgumentType.getString(ctx, "mode");
                                            String field = StringArgumentType.getString(ctx, "field");
                                            setDefault(ctx.getSource(), mode.equalsIgnoreCase("server"), field);
                                            return 1;
                                        }))))
                .then(Commands.literal("config")
                        .executes(ctx -> {
                            if (ctx.getSource().getEntity() instanceof ServerPlayerEntity)
                            {
                                say(ctx.getSource(), "Opening config gui...");
                                sendPacket(5, new CompoundNBT(), ctx.getSource());
                            }
                            else
                                say(ctx.getSource(), TextFormatting.RED + "You cannot run this sub command in the console");
                            return 1;
                        }));

        dispatcher.register(builder);
    }

    private static final SuggestionProvider<CommandSource> CLIENT_SERVER_SUGGEST = (ctx, builder) ->
            ISuggestionProvider.suggest(new String[] {"client", "server"}, builder);

    private static final SuggestionProvider<CommandSource> FIELD_SUGGEST = (ctx, builder) ->
            ISuggestionProvider.suggest(ConfigManager.getFieldIDs(), builder);

    private static void setDefault(CommandSource source, boolean toServer, String registryName)
    {
        int permissionLevel = getPermission(source);
        String[] registryNames = registryName.split(":");

        if (registryNames.length > 1)
        {
            if (toServer && !ConfigManager.isRemote)
            {
                FieldInstance field = ConfigManager.getFieldInstance(registryNames[0], registryName);

                if (field == null)
                    say(source, TextFormatting.RED + registryName + " does not exist");
                else
                if (field.hasPermission(permissionLevel) || !field.enforce && !field.hide)
                {
                    field.setToDefault();
                    ConfigManager.save(field.config.getName(), field.registryName);
                    say(source, field.name + " was set to default successfully! Value: " + field.getRealCachedValue());
                }
                else
                    say(source, TextFormatting.RED + field.name + " requires a higher permission level to set to default");
            }
            else
            {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString("config", registryNames[0]);
                nbt.putString("field", registryName);
                sendPacket(3, nbt, source);
            }
        }
        else
            say(source, TextFormatting.RED + registryName + " is not a valid registry name");
    }

    private static void set(CommandSource source, boolean toServer, String registryName, String value)
    {
        int permissionLevel = getPermission(source);
        String[] registryNames = registryName.split(":");

        if (registryNames.length > 1)
        {
            if (toServer)
            {
                FieldInstance field = ConfigManager.getFieldInstance(registryNames[0], registryName);

                if (field == null)
                    say(source, TextFormatting.RED + registryName + " does not exist");
                else
                if (field.hasPermission(permissionLevel) || !field.enforce && !field.hide)
                    if (field.setServerValue(value))
                    {
                        ConfigManager.save(field.config.getName(), field.registryName);
                        say(source, field.name + " was set successfully! Value: " + field.getRealCachedValue());
                    }
                    else
                        say(source, TextFormatting.RED + field.name + " was not set successfully");
                else
                    say(source, TextFormatting.RED + field.name + " requires a higher permission level to set");
            }
            else
            {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString("config", registryNames[0]);
                nbt.putString("field", registryName);
                nbt.putString("value", value);
                sendPacket(2, nbt, source);
            }
        }
        else
            say(source, TextFormatting.RED + registryName + " is not a valid registry name");
    }

    private static void get(CommandSource source, boolean fromServer, String registryName)
    {
        int permissionLevel = getPermission(source);
        String[] registryNames = registryName.split(":");

        if (registryNames.length > 1)
        {
            if (fromServer)
            {
                FieldInstance field = ConfigManager.getFieldInstance(registryNames[0], registryName);
                if (field == null)
                    say(source, TextFormatting.RED + registryName + " does not exist");
                else
                if (field.hasPermission(permissionLevel) || !field.enforce && !field.hide)
                    say(source, "Field " + field.name + "\n-----  -----\nFrom: " + field.config.getName() + "\nDefault Value: " + field.defaultValue + "\nCurrent Value: " + String.valueOf(field.getRealCachedValue()));
                else
                    say(source, TextFormatting.RED + field.name + " requires a higher permission level to access");
            }
            else
            {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString("config", registryNames[0]);
                nbt.putString("field", registryName);
                sendPacket(1, nbt, source);
            }
        }
        else
            say(source, TextFormatting.RED + registryName + " is not a valid registry name");
    }

    private static int getPermission(CommandSource source)
    {
        return source.getEntity() instanceof ServerPlayerEntity ?
                source.getServer().isSingleplayer() ? 4 :
                        source.getServer().getPlayerList().getOps().get(((ServerPlayerEntity)source.getEntity()).getGameProfile()) != null ?
                                source.getServer().getPlayerList().getOps().get(((ServerPlayerEntity)source.getEntity()).getGameProfile()).getLevel() : 0
                : 4;
    }

    private static void sendPacket(int index, CompoundNBT nbt, CommandSource source)
    {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
        {
            say(source, TextFormatting.RED + "You cannot run this sub command in the console");
            return;
        }

        nbt.putString("command", "configex");
        NetworkHandler.sendClientPacket(index, nbt, source);
    }

    private static void say(CommandSource source, String message)
    {
        source.sendSuccess(new StringTextComponent(message), false);
    }
}
>>>>>>> Stashed changes
