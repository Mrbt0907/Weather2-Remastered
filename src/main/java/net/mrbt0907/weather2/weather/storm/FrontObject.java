package net.mrbt0907.weather2.weather.storm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilMisc;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.IWeatherDetectable;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.config.ConfigFront;
import net.mrbt0907.weather2.config.ConfigSimulation;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigWind;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.weather.WeatherManager;

public class FrontObject implements IWeatherDetectable
{
	protected final Map<UUID, WeatherObject> systems = new HashMap<UUID, WeatherObject>();
	private UUID uuid = UUID.randomUUID();
	protected World world;
	private WeatherManager manager;
	public NBTTagCompound nbt;
	public Vec3 pos;
	public Vec3 motion;
	public float angle;
	public float size;
	public boolean isDying;
	public boolean isDead;
	private boolean isGlobal;
	public float temperature;
	public float humidity;
	public float pressure;
	public float frontMultiplier;
	/**Type of front<p>0 - Stationary Front<br>1 - Cold Front<br>2 - Warm Front<br>3 - Occluded Front*/
	public int type;
	public int layer;
	public int maxStorms;
	public int storms;
	public int activeStorms;
	
	public FrontObject(WeatherManager manager, Vec3 pos, int layer)
	{
		this.manager = manager;
		world = manager.getWorld();
		this.pos = pos;
		this.layer = layer;
		size = Maths.random(ConfigStorm.min_storm_size, ConfigFront.max_front_size);
		angle = CoroUtilMisc.adjVal(Maths.random(360.0F), manager.windManager.windAngle, 30.0F);
		float vecX = (float) -Math.sin(Math.toRadians(angle));
		float vecZ = (float) Math.cos(Math.toRadians(angle));
		float speed = (manager.windManager.windSpeed * 0.2F) + 0.02F;
		motion = new Vec3(vecX * speed, 0.0D, vecZ * speed);
		
		if (pos == null)
		{
			maxStorms = -1;
			isGlobal = true;
		}
		else
		{
			maxStorms = Maths.random(1, 35);
			temperature = WeatherUtil.getTemperature(world, pos.toBlockPos());
			humidity = WeatherUtil.getTemperature(world, pos.toBlockPos());
			pressure = WeatherUtil.getPressure(world, pos.toBlockPos());
			if (temperature > 0.5)
				type = 1;
			else
				type = 2;
		}
		
		frontMultiplier = Maths.random(0.5F, 2.0F);
		nbt = new NBTTagCompound();
	}
	
	public void tick()
	{
		if (maxStorms > -1 && storms >= maxStorms)
		{
			isDying = true;
			
			if (systems.size() == 0 || activeStorms == 0)
			{
				isDead = true;
				return;
			}
		}
		
		tickMovement();
		
		if (manager.getWorld().isRemote)
		{
			
		}
		else
		{
			
			if (ConfigSimulation.simulation_enable)
				tickProgressionSimulation();
			else
				tickProgressionNormal();
			
		}
		
		systems.forEach((uuid, system) -> {if (!system.isDead) {system.tick();}});
	}
	
	public void tickMovement()
	{
		if (pos != null)
			if (world.isRemote)
			{
				pos.posX += motion.posX;
				pos.posZ += motion.posZ;
			}
			else
			{
				float mult = (type == 0 ? 0.25F : type == 1 ? 1.25F : 1.0F) * frontMultiplier;
				angle = CoroUtilMisc.adjVal(angle, manager.windManager.windAngle, 0.001F * (float)ConfigFront.angle_change_mult * mult);
				
				float vecX = (float) -Math.sin(Math.toRadians(angle));
				float vecZ = (float) Math.cos(Math.toRadians(angle));
				float cloudSpeed = 0.2F;
				float speed = (manager.windManager.windSpeed * cloudSpeed) + (type == 1 ? 0.2F : 0.02F);
				motion.posX = CoroUtilMisc.adjVal((float)motion.posX, vecX * speed, (float)ConfigFront.speed_change_mult * mult);
				motion.posZ = CoroUtilMisc.adjVal((float)motion.posZ, vecZ * speed, (float)ConfigFront.speed_change_mult * mult);
		
				pos.posX += motion.posX;
				pos.posZ += motion.posZ;
			}
	}
	
	public void tickProgressionSimulation()
	{
		
	}
	
	/**TODO: Make occluded fronts  merge only when cold front meets warm front from behind, make stationary fronts merge when colliding from any other direction*/
	public void tickProgressionNormal()
	{
		if (world.getTotalWorldTime() % Math.max(ConfigFront.tick_rate, 1L) == 0L)
		{
			if (pos != null)
			{
				BlockPos pos = this.pos.toBlockPos();
				float temperature = WeatherUtil.getTemperature(world, pos);
				float humidity = WeatherUtil.getTemperature(world, pos);
				float pressure = WeatherUtil.getPressure(world, pos);
				
				this.temperature = CoroUtilMisc.adjVal(this.temperature, temperature, 0.0001F * (float)ConfigFront.environment_change_mult);
				this.humidity = CoroUtilMisc.adjVal(this.humidity, humidity, 0.0005F * (float)ConfigFront.environment_change_mult);
				this.pressure = CoroUtilMisc.adjVal(this.pressure, pressure, 0.0002F * (float)ConfigFront.environment_change_mult);
				
				if (type == 1)
					for (FrontObject front : manager.getFronts())
					{
						if (!front.equals(this) && front.type == 2 && front.pos.distance(this.pos) - (front.size * 0.25F) <= 0.0F)
						{
							activeStorms += front.activeStorms;
							maxStorms += front.maxStorms;
							storms += front.storms;
							
							for (WeatherObject weather : front.getWeatherObjects())
							{
								weather.front = this;
								front.systems.remove(weather.getUUID());
							}
							
							front.isDead = true;
							type = 0;
							
							motion = new Vec3(0.0D, 0.0D, 0.0D);
						}
					}
				
				if (type < 3)
				{
					if (motion.speed() / ConfigWind.windSpeedMax <= 0.05F)
						type = 0;
					else if (this.temperature < 0.35)
						type = 1;
					else
						type = 2;
				}
			}
		}
	}
	
	public StormObject createStorm(double posX, double posZ, int stage, Map<String, Boolean> flags)
	{
		if(isDying) return null;
		StormObject storm = new StormObject(this);
		storm.layer = layer;
		storm.isNatural = false;
		storm.stormTemperature = 0.1F;
		storm.pos = new Vec3(posX, storm.getLayerHeight(), posZ);
		storm.stormHumidity = stage * 50.0F;
		storm.stormIntensity = stage;
		storm.stormSizeRate = 1.0F;
		storm.stormStage = stage;
		storm.stormStageMax = storm.stormStage;
		
		for (Entry<String, Boolean> flag : flags.entrySet())
		{
			switch(flag.getKey().toLowerCase())
			{
			case "alwaysprogress":
				storm.alwaysProgresses = flag.getValue();
			case "neverDissipate":
				storm.neverDissipate = flag.getValue();
			case "isFirenado":
				storm.isFirenado = flag.getValue();
			case "shouldConvert":
				storm.shouldConvert = flag.getValue();
			case "isViolent":
				storm.isViolent = flag.getValue();
			case "shouldBuildHumidity":
				storm.shouldBuildHumidity = flag.getValue();
			}
		}
		addWeatherObject(storm);
		return storm;
	}
	
	public StormObject createNaturalStorm()
	{
		if (ConfigStorm.isLayerValid(layer) && !isDying)
		{
			StormObject storm = new StormObject(this);
			storm.layer = layer;
			storm.isNatural = true;
			storm.pos = new Vec3(pos.posX + Maths.random(-size, size), storm.getLayerHeight(), pos.posZ + Maths.random(-size, size));
			if (Maths.chance(ConfigStorm.chance_for_supercell * 0.01D))
				storm.initRealStorm();
			addWeatherObject(storm);
			return storm;
		}
		else
			return null;
	}
	
	/**Creates a weather object in this front*/
	public WeatherObject createWeatherObject(Class<? extends WeatherObject> clazz)
	{
		if (clazz != null && !isDying)
		{
			try
			{
				WeatherObject system = clazz.getConstructor(WeatherManager.class).newInstance(manager);
				addWeatherObject(system);
				return system;
			}
			catch (Exception e)
			{
				Weather2.error(e.getMessage());
			}
		}
		
		Weather2.error("Cannot create a weather object; clazz returned null");
		return null;
	}
	
	public void removeWeatherObject(UUID uuid)
	{
		WeatherObject system = systems.get(uuid);
		manager.removeWeatherObject(uuid);
		
		if (system != null)
		{
			Weather2.debug("Weather " + uuid + " was removed from front " + this.uuid);

			if (!system.type.equals(Type.CLOUD))
				activeStorms--;
			
			system.reset();
			systems.remove(uuid);
		}
		else
			Weather2.error("Front " + this.uuid.toString() + " tried to remove a non-existent weather object with uuid " + uuid);
	}
	
	@SideOnly(Side.CLIENT)
	public void cleanupClient(boolean wipe)
	{
		systems.forEach((uuid, system) -> system.cleanupClient(wipe));
	}
	
	public void reset()
	{
		systems.forEach((uuid, system) -> system.reset());
	}
	
	public void aimStormAtPlayer(EntityPlayer entP)
	{
		if (entP == null)
			entP = manager.getWorld().getClosestPlayer(pos.posX, pos.posY, pos.posZ, -1, false);
		
		if (entP != null)
		{
			float yaw = -(float)(Math.atan2(entP.posX - pos.posX, entP.posZ - pos.posZ) * 180.0D / Math.PI);
			int size = ConfigStorm.storm_aim_accuracy_in_angle;
			if (size > 0)
				yaw += Maths.random(size) - (size / 2);
			
			angle = yaw;
			
			Weather2.debug("Front " + uuid + " was aimed at player " + CoroUtilEntity.getName(entP));
		}
	}
	
	public void readNBT(NBTTagCompound nbt)
	{
		uuid = nbt.getUniqueId("uuid");
		if (pos != null)
		{
			pos.posX = nbt.getDouble("posX");
			pos.posY = nbt.getDouble("posY");
			pos.posZ = nbt.getDouble("posZ");
			motion.posX = nbt.getDouble("motionX");
			motion.posY = nbt.getDouble("motionY");
			motion.posZ = nbt.getDouble("motionZ");
			
			temperature = nbt.getFloat("temperature");
			humidity = nbt.getFloat("humidity");
			pressure = nbt.getFloat("pressure");

			type = nbt.getInteger("type");
			frontMultiplier = nbt.getFloat("frontMultiplier");
		}
		angle = nbt.getFloat("angle");
		layer = nbt.getInteger("layer");
		maxStorms = nbt.getInteger("maxStorms");
		storms = nbt.getInteger("storms");
		size = nbt.getFloat("size");
	}
	
	public NBTTagCompound writeNBT()
	{
		nbt.setUniqueId("uuid", uuid);
		if (pos != null)
		{
			nbt.setDouble("posX", pos.posX);
			nbt.setDouble("posY", pos.posY);
			nbt.setDouble("posZ", pos.posZ);
			nbt.setDouble("motionX", motion.posX);
			nbt.setDouble("motionY", motion.posY);
			nbt.setDouble("motionZ", motion.posZ);
			
			nbt.setFloat("temperature", temperature);
			nbt.setFloat("humidity", humidity);
			nbt.setFloat("pressure", pressure);

			nbt.setInteger("type", type);
			nbt.setFloat("frontMultiplier", frontMultiplier);
		}
		nbt.setFloat("angle", angle);
		nbt.setInteger("layer", layer);
		nbt.setInteger("maxStorms", maxStorms);
		nbt.setInteger("storms", storms);
		nbt.setFloat("size", size);

		return nbt;
	}
	
	public WeatherManager getWeatherManager()
	{
		return manager;
	}
	
	public WeatherObject getWeatherObject(UUID uuid)
	{
		return systems.get(uuid);
	}
	
	public List<WeatherObject> getWeatherObjects()
	{
		return new ArrayList<WeatherObject>(systems.values());
	}
	
	public World getWorld()
	{
		return manager.getWorld();
	}
	
	public int size()
	{
		return systems.size();
	}
	
	public UUID getUUID()
	{
		return uuid;
	}

	public boolean isGlobal()
	{
		return isGlobal;
	}

	public void addWeatherObject(WeatherObject weather)
	{
		if (weather != null && weather.front.equals(this))
		{
			storms++;
			
			if (!weather.type.equals(Type.CLOUD))
				activeStorms++;
				
			systems.put(weather.getUUID(), weather);
			manager.addWeatherObject(weather);
		}
	}

	public boolean contains(UUID uuid)
	{
		return systems.containsKey(uuid);
	}

	@Override
	public float getWindSpeed() {return 0.0F;}

	@Override
	public int getStage() {return 0;}

	@Override
	public void setStage(int stage) {}

	@Override
	public Vec3 getPos()
	{
		return pos;
	}

	@Override
	public boolean isDying()
	{
		return isDying;
	}

	@Override
	public String getName()
	{
		switch(type)
		{
			case 1:
				return "Cold Front";
			case 2:
				return "Warm Front";
			case 3:
				return "Occluded Front";
			default:
				return "Stationary Front";
		}
	}

	@Override
	public String getTypeName()
	{
		switch(type)
		{
			case 1:
				return "CF";
			case 2:
				return "WF";
			case 3:
				return "OF";
			default:
				return "SF";
		}
	}

	@Override
	public float getAngle()
	{
		return angle;
	}

	@Override
	public float getSpeed()
	{
		return (float) motion.speed();
	}
}
