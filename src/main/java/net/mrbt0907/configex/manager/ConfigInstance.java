package net.mrbt0907.configex.manager;

<<<<<<< Updated upstream
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.api.IConfigEX.Phase;

public class ConfigInstance
{
	private final Map<String, FieldInstance> fields = new LinkedHashMap<String, FieldInstance>();
	public final IConfigEX instance;
	public final String name;
	public final String registryName;
	public final String description;
	public final String saveLocation;
	public final ForgeConfigSpec clientCFG;
	public final ForgeConfigSpec commonCFG;
	public final ForgeConfigSpec serverCFG;
	
	public ConfigInstance(IConfigEX instance)
	{
		this.instance = instance;
		name = instance.getName();
		registryName = ConfigManager.formatRegistryName(name);
		description = instance.getDescription();
		saveLocation = instance.getSaveLocation();
		clientCFG = build(Type.CLIENT);
		commonCFG = build(Type.COMMON);
		serverCFG = build(Type.SERVER);
	}
	
	public FieldInstance get(String registryName)
	{
		return fields.get(registryName);
	}
	
	public void ids(List<ResourceLocation> variables)
	{
		fields.forEach((registryName, field) -> variables.add(new ResourceLocation(this.registryName, registryName)));	
	}
	
	public void readNBT(CompoundNBT nbt, int permissionLevel)
	{
		if (nbt.contains(registryName))
		{
			int variables = 0;
			CompoundNBT nbtFields = nbt.getCompound(registryName);
			FieldInstance field;
			
			instance.onConfigChanged(Phase.START, variables);
			for (String key : nbtFields.getAllKeys())
			{
				if (fields.containsKey(key))
				{
					field = fields.get(key);
					if (field.permission > permissionLevel) continue;
					Object oldValue = field.getActualValue();
					field.readNBT(nbtFields);
					Object newValue = field.getActualValue(); 
					if (newValue != oldValue)
					{
						instance.onValueChanged(key, oldValue, newValue);
						variables++;
					}
				}
			}
			instance.onConfigChanged(Phase.END, variables);
		}
	}
	
	public void writeNBT(CompoundNBT nbt, boolean fullNBT)
	{
		CompoundNBT nbtFields = new CompoundNBT();
		for (FieldInstance field : fields.values())
		{
			if (fullNBT || field.isDirty)
			{
				if (!fullNBT)
					field.isDirty = false;
				field.writeNBT(nbtFields);
			}
		}
		nbt.put(registryName, nbtFields);
	}
	
	private ForgeConfigSpec build(Type side)
	{
		ForgeConfigSpec configuration = null;
		Builder builder = new Builder();
		Class<? extends IConfigEX> clazz = instance.getClass();
		Field[] fields = clazz.getDeclaredFields();
		boolean register = false;
		
		builder.push(name != null ? name : "main");
		for (Field field : fields)
		{
			if (field.isAnnotationPresent(Ignore.class)) continue;
			Type configType = field.isAnnotationPresent(ClientSide.class) ? Type.CLIENT : field.isAnnotationPresent(ServerSide.class) ? Type.SERVER : Type.COMMON;
			if (!configType.equals(side)) continue;
			register = true;
			FieldInstance instance = new FieldInstance(builder, configuration, this.instance, field);
			this.fields.put(instance.registryName, instance);
		}
		builder.pop();
		configuration = builder.build();
		
		if (register)
		{
			ModLoadingContext context = ModLoadingContext.get();
			switch (side)
			{
				case CLIENT: context.registerConfig(Type.CLIENT, configuration, saveLocation + "-client.toml"); break;
				case COMMON: context.registerConfig(Type.COMMON, configuration, saveLocation + "-common.toml"); break;
				case SERVER: context.registerConfig(Type.SERVER, configuration, saveLocation + "-server.toml"); break;
			}
		}
		return configuration;
	}
	
	public void load()
	{
		instance.onConfigChanged(Phase.START, 0);
		for (FieldInstance field : this.fields.values())
		{
			Object oldValue = field.getActualValue();
			field.set(field.getSavedValue(), !ConfigManager.IS_REMOTE);
			instance.onValueChanged(field.registryName, oldValue, field.getActualValue());
		}
		instance.onConfigChanged(Phase.END, this.fields.size());
	}
=======
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.ConfigAnnotations.Ignore;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.api.IConfigEX.Phase;
import net.mrbt0907.weather2.util.StringUtils;

public class ConfigInstance
{
    public final IConfigEX config;
    private final Map<String, FieldInstance> fields = new LinkedHashMap<String, FieldInstance>();
    public final String name;
    public final String registryName;
    public final String description;
    public final String saveLocation;
    public final com.electronwill.nightconfig.core.file.CommentedFileConfig configuration;
    public final File fileLocation;
    private int variablesChanged;

    public ConfigInstance(IConfigEX config)
    {
        this.config = config;
        name = config.getName();
        registryName = StringUtils.parseID(name);
        description = config.getDescription();
        saveLocation = config.getSaveLocation();

        fileLocation = new File(ConfigModEX.getGameFolder() + "config/" + saveLocation + ".toml");


        if (!fileLocation.getParentFile().exists()) {
            fileLocation.getParentFile().mkdirs();
        }

        configuration = com.electronwill.nightconfig.core.file.CommentedFileConfig.builder(fileLocation)
                .sync()
                .autosave()
                .writingMode(com.electronwill.nightconfig.core.io.WritingMode.REPLACE)
                .build();
        initConfig();
    }

    public void initConfig()
    {
        ConfigModEX.debug("Initializing config " + registryName + "...");
        FieldInstance instance;

        fields.clear();

        for (Field field : config.getClass().getFields())
        {
            if (field.isAnnotationPresent(Ignore.class))
            {
                ConfigModEX.debug("Ignoring field " + field.getName());
                continue;
            }

            instance = new FieldInstance(config, field);
            fields.put(instance.registryName, instance);
        };


        readConfigFile();

        writeConfigFile(false);

        ConfigModEX.debug("Config " + registryName + " has initialized successfully!");
    }

    public void writeNBT(CompoundNBT nbt)
    {
        ConfigModEX.debug("Writing fields from " + name + " to nbt...");
        CompoundNBT nbtField;

        for (FieldInstance field : fields.values())
        {
            nbtField = new CompoundNBT();
            nbtField.putString("value", String.valueOf(field.getServerValue()));
            nbt.put(field.registryName, nbtField);
        }

        ConfigModEX.debug("Write complete!");
    }

    public void readNBT(CompoundNBT nbt)
    {
        ConfigModEX.debug("Reading fields from " + (ConfigManager.isRemote ? "the server" : "a client") + " to config " + name + "...");
        CompoundNBT nbtField;
        String value;
        boolean checkPerm = !ConfigManager.isRemote && nbt.hasUUID("player");
        boolean setServer = !ConfigManager.isRemote || !nbt.getBoolean("setClient");
        int permission = ConfigManager.getPermissionLevel(nbt.getUUID("player"));
        config.onConfigChanged(Phase.START, fields.size());
        variablesChanged = 0;
        for (FieldInstance field : fields.values())
            if (nbt.contains(field.registryName))
            {
                if (checkPerm && !field.hasPermission(permission))
                {
                    ConfigModEX.warn("Rejecting value for " + field.registryName + " as the player does not have permission to change the value");
                    continue;
                }
                nbtField = nbt.getCompound(field.registryName);
                value = nbtField.getString("value");
                try
                {
                    switch(field.type)
                    {
                        case 1:
                            setField(field, Integer.valueOf(value), setServer);
                            break;
                        case 2:
                            setField(field, Short.valueOf(value), setServer);
                            break;
                        case 3:
                            setField(field, Long.valueOf(value), setServer);
                            break;
                        case 4:
                            setField(field, Float.valueOf(value), setServer);
                            break;
                        case 5:
                            setField(field, Double.valueOf(value), setServer);
                            break;
                        case 6:
                            setField(field, value, setServer);
                            break;
                        case 7:
                            setField(field, Boolean.valueOf(value), setServer);
                            break;
                    }

                    if (updateField(field, false))
                        variablesChanged++;
                }
                catch(Exception e)
                {
                    ConfigModEX.warn("Rejecting value for " + field.registryName + " as the value was unable to be set");
                    continue;
                }
            }
        ConfigModEX.debug("Read complete!");
        config.onConfigChanged(Phase.END, variablesChanged);
        variablesChanged = -1;
    }

    public FieldInstance getField(String registryName)
    {
        return fields.get(registryName);
    }

    public List<FieldInstance> getFields()
    {
        return new ArrayList<FieldInstance>(fields.values());
    }

    public boolean setField(FieldInstance field, Object value, boolean changeServerVariable)
    {
        if (field == null)
            ConfigModEX.fatal(new NullPointerException("Field was null"));

        if (ConfigManager.isRemote)
        {
            if (changeServerVariable)
                return field.setServerValue(value);
            else
                return field.setClientValue(value);
        }
        else
            return field.setServerValue(value);
    }

    public void defaultField(FieldInstance field)
    {
        if (field == null)
            ConfigModEX.fatal(new NullPointerException("Field was null"));

        field.setToDefault();
    }

    public boolean updateField(FieldInstance field, boolean fullSync)
    {
        if (field == null)
            ConfigModEX.fatal(new NullPointerException("Field was null"));

        if (fullSync)
            if (ConfigManager.isRemote)
                field.setClientValue(field.getRealValue());
            else
                field.setServerValue(field.getRealValue());

        Object value = field.getRealCachedValue();

        if (field.hasChanged)
        {
            field.setRealValue();
            if (variablesChanged < 0)
            {
                config.onConfigChanged(Phase.START, 1);
                config.onValueChanged(field.name, value, ConfigManager.isRemote ? field.getClientValue() : field.getServerValue());
                config.onConfigChanged(Phase.END, 1);
            }
            else
                config.onValueChanged(field.name, value, ConfigManager.isRemote ? field.getClientValue() : field.getServerValue());
            return true;
        }
        return false;
    }

    public void updateAllFields(boolean fullSync)
    {
        if (fullSync)
        {
            config.onConfigChanged(Phase.START, fields.size());
            variablesChanged = 0;
        }

        for (FieldInstance field : fields.values())
        {
            if (updateField(field, fullSync) && fullSync)
                variablesChanged++;
        }

        if (fullSync)
        {
            config.onConfigChanged(Phase.END, variablesChanged);
            variablesChanged = -1;
        }
    }

    public void writeConfigFile(boolean wipeFile)
    {
        if (wipeFile && fileLocation.exists()) fileLocation.delete();
        ConfigModEX.debug("Config " + registryName + " is writing to file " + ConfigModEX.getGameFolder() + "config/" + saveLocation + ".toml" + "...");
        fields.forEach((name, field) ->
        {
            if (field.hasConfigValue())
                field.updateConfigValue();
            else
                ConfigModEX.warn("Field " + name + " had not initialized in the config. Skipping write...");
        });
        configuration.save();
        ConfigModEX.debug("Saved config " + registryName + " successfully!");
    }

    public void readConfigFile()
    {
        ConfigModEX.debug("Config " + registryName + " is reading from file " + ConfigModEX.getGameFolder() + "config/" + saveLocation + ".toml" + "...");
        configuration.load();
        config.onConfigChanged(Phase.START, fields.size());
        variablesChanged = 0;

        fields.forEach((name, field) ->
        {

            String configPath = registryName + "." + field.displayName;

            switch(field.type)
            {
                case 1:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, (int) field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.getIntOrElse(configPath, (int) field.defaultValue), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 2:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, (short) field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, (short) configuration.getIntOrElse(configPath, (short) field.defaultValue), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 3:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, (long) field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.getLongOrElse(configPath, (long) field.defaultValue), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 4:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, (float) field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, (float) configuration.get(configPath), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 5:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, (double) field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.get(configPath), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 6:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, (String) field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.getOrElse(configPath, (String) field.defaultValue), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 7:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, (boolean) field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.getOrElse(configPath, (boolean) field.defaultValue), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                default:
                {
                    ConfigModEX.warn("Field " + field.registryName + " is using an unsupported type. Skipping...");
                    return;
                }
            }
        });
        configuration.save();
        ConfigModEX.debug("Loaded config " + registryName + " successfully!");
        config.onConfigChanged(Phase.END, variablesChanged);
        variablesChanged = -1;
    }

    public int size()
    {
        return fields.size();
    }

    public void setToDefault()
    {
        fields.forEach((registryName, field) -> field.setToDefault());
    }

    public void reset(boolean fullReset)
    {
        config.onConfigChanged(Phase.START, fields.size());
        variablesChanged = 0;
        fields.forEach((name, field) ->
        {
            Object value = field.getRealCachedValue();
            if (fullReset)
                field.reset();
            else
                field.setRealValue();
            if (value != field.getRealCachedValue() || !fullReset)
            {
                config.onValueChanged(field.name, value, field.getRealCachedValue());
                variablesChanged++;
            }
        });
        config.onConfigChanged(Phase.END, variablesChanged);
        variablesChanged = -1;
        ConfigModEX.debug("Reset all fields for config " + name);
    }
>>>>>>> Stashed changes
}