package net.mrbt0907.configex.manager;

import java.lang.reflect.Field;

<<<<<<< Updated upstream
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.ConfigAnnotations.ClientSide;
import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.DoubleRange;
import net.mrbt0907.configex.api.ConfigAnnotations.FloatRange;
import net.mrbt0907.configex.api.ConfigAnnotations.Hidden;
import net.mrbt0907.configex.api.ConfigAnnotations.IntegerRange;
import net.mrbt0907.configex.api.ConfigAnnotations.LongRange;
import net.mrbt0907.configex.api.ConfigAnnotations.Name;
import net.mrbt0907.configex.api.ConfigAnnotations.Permission;
import net.mrbt0907.configex.api.ConfigAnnotations.RequiresWorldReload;
import net.mrbt0907.configex.api.ConfigAnnotations.ServerSide;
import net.mrbt0907.configex.api.ConfigAnnotations.ShortRange;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2remastered.Weather2Remastered;

public class FieldInstance
{
	public final ForgeConfigSpec configuration;
	public final IConfigEX instance;
	public final Field field;
	public final String name;
	public final String registryName;
	public final String[] comment;
	public final int permission;
	public final boolean hidden;
	public final boolean requiresWorldReload;
	public final double min;
	public final double max;
	public final boolean showMin;
	public final boolean showMax;
	
	public final byte type;
	public final Object defaultValue;
	public final ConfigValue<Object> configValue;
	public final Type configType;
	public final boolean ignoreServer;
	public boolean isDirty;
	protected Object clientValue;
	protected Object serverValue;
	
	public FieldInstance(Builder builder, ForgeConfigSpec configuration, IConfigEX instance, Field field)
	{
		this.configuration = configuration;
		this.instance = instance;
		this.field = field;
		configType = field.isAnnotationPresent(ClientSide.class) ? Type.CLIENT : field.isAnnotationPresent(ServerSide.class) ? Type.SERVER : Type.COMMON;
		ignoreServer = configType.equals(Type.CLIENT);
		
		Object defaultValue = null;
		try {defaultValue = field.get(instance);} catch (Exception e) {Weather2Remastered.fatal(e);}
		type = (byte) (defaultValue instanceof Integer ? 1 : defaultValue instanceof Short ? 2 : defaultValue instanceof Long ? 3 : defaultValue instanceof Float ? 4 : defaultValue instanceof Double ? 5 : defaultValue instanceof String ? 6 : defaultValue instanceof Boolean ? 7 : 0);
		this.defaultValue = defaultValue;
		
		Name name = field.getDeclaredAnnotation(Name.class);
		this.name = name == null || name.value().length() == 0 ? field.getName() : name.value();
		registryName = ConfigManager.formatRegistryName(field.getName());
		
		Comment comment = field.getDeclaredAnnotation(Comment.class);
		String[] declaredComment = comment == null ? new String[0] : comment.value();
		this.comment = new String[declaredComment.length + 1];
		for (int i = 0; i < declaredComment.length; i++)
			this.comment[i] = declaredComment[i];
		this.comment[declaredComment.length] = "Default Value: " + defaultValue;
		
		Permission permission = field.getDeclaredAnnotation(Permission.class);
		this.permission = permission == null ? 3 : permission.value();
		
		
		hidden = field.isAnnotationPresent(Hidden.class);
		requiresWorldReload = field.isAnnotationPresent(RequiresWorldReload.class);
		
		switch(type)
		{
			case 1:
				IntegerRange rangeI = field.getAnnotation(IntegerRange.class);
				min = rangeI != null ? rangeI.min() : Integer.MIN_VALUE;
				max = rangeI != null ? rangeI.max() : Integer.MAX_VALUE;
				showMin = min != Integer.MIN_VALUE;
				showMax = max != Integer.MAX_VALUE;
				break;
			case 2:
				ShortRange rangeS = field.getAnnotation(ShortRange.class);
				min = rangeS != null ? rangeS.min() : Short.MIN_VALUE;
				max = rangeS != null ? rangeS.max() : Short.MAX_VALUE;
				showMin = min != Short.MIN_VALUE;
				showMax = max != Short.MAX_VALUE;
				break;
			case 3:
				LongRange rangeL = field.getAnnotation(LongRange.class);
				min = rangeL != null ? rangeL.min() : Long.MIN_VALUE;
				max = rangeL != null ? rangeL.max() : Long.MAX_VALUE;
				showMin = min != Long.MIN_VALUE;
				showMax = max != Long.MAX_VALUE;
				break;
			case 4:
				FloatRange rangeF = field.getAnnotation(FloatRange.class);
				min = rangeF != null ? rangeF.min() : -Float.MAX_VALUE;
				max = rangeF != null ? rangeF.max() : Float.MAX_VALUE;
				showMin = min != -Float.MAX_VALUE;
				showMax = max != Float.MAX_VALUE;
				break;
			case 5:
				DoubleRange rangeD = field.getAnnotation(DoubleRange.class);
				min = rangeD != null ? rangeD.min() : -Double.MAX_VALUE;
				max = rangeD != null ? rangeD.max() : Double.MAX_VALUE;
				showMin = min != -Double.MAX_VALUE;
				showMax = max != Double.MAX_VALUE;
				break;
			default:
				min = 0.0D;
				max = 0.0D;
				showMin = false;
				showMax = false;
		}
		
		builder.comment(this.comment);
		if (requiresWorldReload)
			builder.worldRestart();
		switch (type)
		{
			case 0: 
				configValue = null; 
				Weather2Remastered.fatal("Field " + field.getName() + " is using an unsupported type");
				break;
			default:
				configValue = builder.define(this.name, defaultValue);
		}
		
		set(defaultValue, !ConfigManager.IS_REMOTE);
	}
	
	public void readNBT(CompoundNBT nbt)
	{
		if (ConfigManager.IS_REMOTE ? !ignoreServer : true && nbt.contains(registryName))
			switch(type)
			{
				case 1: set(nbt.getInt(registryName), ConfigManager.IS_REMOTE); break;
				case 2: set(nbt.getShort(registryName), ConfigManager.IS_REMOTE); break;
				case 3: set(nbt.getLong(registryName), ConfigManager.IS_REMOTE); break;
				case 4: set(nbt.getFloat(registryName), ConfigManager.IS_REMOTE); break;
				case 5: set(nbt.getDouble(registryName), ConfigManager.IS_REMOTE); break;
				case 6: set(nbt.getString(registryName), ConfigManager.IS_REMOTE); break;
				case 7: set(nbt.getBoolean(registryName), ConfigManager.IS_REMOTE); break;
			}
	}
	
	public void writeNBT(CompoundNBT nbt)
	{
		if (ConfigManager.IS_REMOTE ? !ignoreServer : true)
			switch(type)
			{
				case 1: nbt.putInt(registryName, (int) get()); break;
				case 2: nbt.putShort(registryName, (short) get()); break;
				case 3: nbt.putLong(registryName, (long) get()); break;
				case 4: nbt.putFloat(registryName, (float) get()); break;
				case 5: nbt.putDouble(registryName, (double) get()); break;
				case 6: nbt.putString(registryName, (String) get()); break;
				case 7: nbt.putBoolean(registryName, (boolean) get()); break;
			}
	}
	
	public Object getSavedValue()
	{
		return configValue.get();
	}
	
	public Object getActualValue()
	{
		try
		{
			return field.get(instance);
		}
		catch (Exception e)
		{
			ConfigModEX.fatal(e);
			return null;
		}
	}
	
	public Object get()
	{
		return serverValue == null ? clientValue : serverValue;
	}
	
	public FieldInstance set(Object value, boolean setServerValue)
	{
		Object oldValue;
		if (setServerValue)
		{
			oldValue = serverValue;
			serverValue = value;
		}
		else
		{
			oldValue = clientValue;
			clientValue = value;
		}
		
		try
		{
			field.set(instance, serverValue == null ? clientValue : serverValue);
		}
		catch (Exception e)
		{
			Weather2Remastered.error(e);
			if (setServerValue)
			{
				serverValue = oldValue;
			}
			else
			{
				clientValue = oldValue;
			}
		}
		return this;
	}
	
	public FieldInstance markDirty()
	{
		isDirty = true;
		return this;
	}
	
	public FieldInstance reset()
	{
		if (ConfigManager.IS_REMOTE)
			set(null, true);
		return this;
	}
	
	public void save()
	{
		if (ConfigManager.IS_REMOTE)
			configValue.set(clientValue);
		else
			configValue.set(serverValue);
		configValue.save();
	}
=======
import org.apache.commons.lang3.text.WordUtils;

import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.StringUtils;

public class FieldInstance
{
    protected final Field field;
    public final IConfigEX config;
    public final String name;
    public final String registryName;
    public final String displayName;
    public final String comment;
    public final boolean enforce;
    public final boolean hide;
    public final boolean requiresRestart;
    public final boolean requiresWorldRestart;
    public final int permission;

    public final byte type;
    public final double min;
    public final double max;
    public final boolean showMin;
    public final boolean showMax;
    public final Object defaultValue;
    private Object cachedValue;
    private Object clientValue;
    private Object serverValue;
    private String configPath;
    public boolean hasChanged;

    public FieldInstance(IConfigEX instance, Field field)
    {
        this.config = instance;
        this.field = field;
        name = field.getName();
        registryName = StringUtils.parseID(instance.getName() + ":" + name);
        Name nameAnnotation = field.getAnnotation(Name.class);
        displayName = nameAnnotation == null || nameAnnotation.value().trim().isEmpty() ? WordUtils.capitalize(name.replace('_', ' ')) : nameAnnotation.value().trim();
        Comment commentAnnotation = field.getAnnotation(Comment.class);
        comment = commentAnnotation == null ? "" : ConfigManager.formatComment(commentAnnotation.value());
        enforce = field.isAnnotationPresent(Enforce.class);
        hide = field.isAnnotationPresent(Hidden.class);
        requiresWorldRestart = field.isAnnotationPresent(RequiresWorldReload.class);
        requiresRestart = field.isAnnotationPresent(RequiresRestart.class);
        Permission permissionAnnotation = field.getAnnotation(Permission.class);
        permission = permissionAnnotation == null ? 3 : permissionAnnotation.value();
        if(permission < 0 || permission > 4)
            ConfigModEX.fatal(new IndexOutOfBoundsException("Permission level " + permission + " does not exist"));
        defaultValue = cachedValue = getRealValue();
        type = (byte) (defaultValue instanceof Integer ? 1 : defaultValue instanceof Short ? 2 : defaultValue instanceof Long ? 3 : defaultValue instanceof Float ? 4 : defaultValue instanceof Double ? 5 : defaultValue instanceof String ? 6 : defaultValue instanceof Boolean ? 7 : 0);

        switch(type)
        {
            case 1:
                IntegerRange rangeI = field.getAnnotation(IntegerRange.class);
                min = rangeI != null ? rangeI.min() : Integer.MIN_VALUE;
                max = rangeI != null ? rangeI.max() : Integer.MAX_VALUE;
                showMin = min != Integer.MIN_VALUE;
                showMax = max != Integer.MAX_VALUE;
                break;
            case 2:
                ShortRange rangeS = field.getAnnotation(ShortRange.class);
                min = rangeS != null ? rangeS.min() : Short.MIN_VALUE;
                max = rangeS != null ? rangeS.max() : Short.MAX_VALUE;
                showMin = min != Short.MIN_VALUE;
                showMax = max != Short.MAX_VALUE;
                break;
            case 3:
                LongRange rangeL = field.getAnnotation(LongRange.class);
                min = rangeL != null ? rangeL.min() : Long.MIN_VALUE;
                max = rangeL != null ? rangeL.max() : Long.MAX_VALUE;
                showMin = min != Long.MIN_VALUE;
                showMax = max != Long.MAX_VALUE;
                break;
            case 4:
                FloatRange rangeF = field.getAnnotation(FloatRange.class);
                min = rangeF != null ? rangeF.min() : -Float.MAX_VALUE;
                max = rangeF != null ? rangeF.max() : Float.MAX_VALUE;
                showMin = min != -Float.MAX_VALUE;
                showMax = max != Float.MAX_VALUE;
                break;
            case 5:
                DoubleRange rangeD = field.getAnnotation(DoubleRange.class);
                min = rangeD != null ? rangeD.min() : -Double.MAX_VALUE;
                max = rangeD != null ? rangeD.max() : Double.MAX_VALUE;
                showMin = min != -Double.MAX_VALUE;
                showMax = max != Double.MAX_VALUE;
                break;
            default:
                min = 0.0D;
                max = 0.0D;
                showMin = false;
                showMax = false;
        }

        if (min > max)
            ConfigModEX.fatal(new IndexOutOfBoundsException("Minimum bound was higher than max bound in Range annotation"));

        if (ConfigManager.isRemote)
            clientValue = defaultValue;
        else
            serverValue = defaultValue;
        hasChanged = true;
        ConfigModEX.debug("Successfully created a field instance for variable " + registryName + ": " + this.toString());
    }

    public boolean hasPermission()
    {
        return hasPermission(ConfigManager.getPermissionLevel());
    }

    public boolean hasPermission(int permission)
    {
        return this.permission == 0 || permission >= this.permission;
    }

    public Object getRealValue()
    {
        try
        {
            return field.get(config);
        }
        catch (Exception e)
        {
            ConfigModEX.warn("ConfigModEX has failed to get the value from field " + name);
            ConfigModEX.fatal(e);
            return null;
        }
    }

    public Object getRealCachedValue()
    {
        return cachedValue;
    }

    public FieldInstance setRealValue()
    {
        if (ConfigManager.isRemote)
            setRealValue(getClientValue());
        else
            setRealValue(serverValue);

        return this;
    }

    private FieldInstance setRealValue(Object value)
    {
        try
        {
            field.set(config, value);
            cachedValue = value;
            hasChanged = false;
        }
        catch (Exception e)
        {
            ConfigModEX.warn("ConfigModEX has failed to set the value to field " + name);
            ConfigModEX.fatal(e);
        }
        return this;
    }

    public Object getClientValue()
    {
        return (enforce || !hasPermission()) && serverValue != null ? serverValue : clientValue;
    }

    public Object getRealClientValue()
    {
        return clientValue;
    }

    public Object getServerValue()
    {
        return serverValue;
    }

    public boolean hasServerValue()
    {
        return serverValue != null;
    }

    private boolean setClient(Object value)
    {
        if (!hasPermission()) return false;

        try
        {
            switch(type)
            {
                case 1:
                    clientValue = (int) Maths.clamp(Integer.valueOf(String.valueOf(value)), min, max);
                    break;
                case 2:
                    clientValue = (short) Maths.clamp(Short.valueOf(String.valueOf(value)), min, max);
                    break;
                case 3:
                    clientValue = (long) Maths.clamp(Long.valueOf(String.valueOf(value)), min, max);
                    break;
                case 4:
                    clientValue = (float) Maths.clamp(Float.valueOf(String.valueOf(value)), min, max);
                    break;
                case 5:
                    clientValue = (double) Maths.clamp(Double.valueOf(String.valueOf(value)), min, max);
                    break;
                case 7:
                    clientValue = (boolean) Boolean.valueOf(String.valueOf(value));
                    break;
                default:
                    clientValue = String.valueOf(value);
            }
            hasChanged = hasChanged || cachedValue != clientValue;
        }
        catch(Exception e)
        {
            return false;
        }

        return cachedValue != clientValue;
    }

    private boolean setServer(Object value)
    {
        try
        {
            if (value == null)
                serverValue = null;
            else
                switch(type)
                {
                    case 1:
                        serverValue = (int) Maths.clamp(Integer.valueOf(String.valueOf(value)), min, max);
                        break;
                    case 2:
                        serverValue = (short) Maths.clamp(Short.valueOf(String.valueOf(value)), min, max);
                        break;
                    case 3:
                        serverValue = (long) Maths.clamp(Long.valueOf(String.valueOf(value)), min, max);
                        break;
                    case 4:
                        serverValue = (float) Maths.clamp(Float.valueOf(String.valueOf(value)), min, max);
                        break;
                    case 5:
                        serverValue = (double) Maths.clamp(Double.valueOf(String.valueOf(value)), min, max);
                        break;
                    case 7:
                        serverValue = Boolean.valueOf(String.valueOf(value));
                        break;
                    default:
                        serverValue = String.valueOf(value);
                }

            hasChanged = hasChanged || (!ConfigManager.isRemote || enforce || !hasPermission()) && cachedValue != serverValue;
        }
        catch(Exception e)
        {
            return false;
        }

        return (!ConfigManager.isRemote || enforce || !hasPermission()) && cachedValue != serverValue;
    }

    public boolean setClientValue(Object value)
    {
        if (ConfigManager.isRemote)
        {
            if (value == null)
                ConfigModEX.fatal(new NullPointerException("Client value cannot be null clientside"));
            return setClient(value);
        }
        else
            ConfigModEX.fatal(new IllegalArgumentException("Client value cannot be set serverside"));
        return false;
    }

    public boolean setServerValue(Object value)
    {
        if (!ConfigManager.isRemote && value == null)
            ConfigModEX.fatal(new NullPointerException("Server value cannot be null serverside"));
        return setServer(value);
    }

    public FieldInstance setToDefault()
    {
        if (ConfigManager.isRemote)
            clientValue = defaultValue;
        else
            serverValue = defaultValue;
        return this;
    }

    public boolean hasConfigValue()
    {
        return configPath != null;
    }

    public FieldInstance setConfigValue(com.electronwill.nightconfig.core.file.CommentedFileConfig config, Object defaultValue, String comment)
    {
        if (hasConfigValue())
            ConfigModEX.fatal(new IllegalArgumentException("Config value was already set"));
        this.configPath = StringUtils.parseID(this.config.getName()) + "." + displayName;
        config.set(configPath, defaultValue);
        if (comment != null)
            config.setComment(configPath, comment);
        return this;
    }

    public FieldInstance updateConfigValue()
    {
        if (configPath == null)
        {
            ConfigModEX.error("Config path was not set. Skipping...");
            return this;
        }

        ConfigInstance configInstance = ConfigManager.getInstance(this.config);
        if (configInstance == null)
        {
            ConfigModEX.error("Config instance is null for " + this.config.getName() + ". Skipping...");
            return this;
        }

        if (configInstance.configuration == null)
        {
            ConfigModEX.error("Configuration is null for " + this.config.getName() + ". Skipping...");
            return this;
        }

        configInstance.configuration.set(configPath, String.valueOf(ConfigManager.isRemote ? clientValue : serverValue));
        return this;
    }

    public void reset()
    {
        if (ConfigManager.isRemote)
        {
            setServerValue(null);
            setRealValue();
        }
    }

    public String toString()
    {
        return String.format("{instance=" + config.getName() + ", registryName=" + registryName + ", enforced=" + enforce + ", hide=" + hide + ", requiresRestart=" + requiresRestart + ", requiresWorldRestart=" + requiresWorldRestart + ", permission=" + permission + ", defaultValue=" + defaultValue + "}");
    }
>>>>>>> Stashed changes
}