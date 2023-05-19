package net.mrbt0907.weather2.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.server.weather.WeatherManagerServer;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.UUID;

public class TileWeatherConstructor extends TileEntity implements ITickable
{
	
	//gui ideas
	
	/* Activity Mode: Locked on / Locked off / Time cycle
	 * Weather Type: Snow / Rain / Deadly Storm
	 * if activity mode on delay, otherwise just track last storm the tile entity made and wait for it to be dead or really far?:
	 * Weather Rate: 5 / 10 / 20 / 30 / 1 hr / 2 hr
	 * Size: ya
	 * 
	 * 
	 */
	
	/**Type of weather that the weather constructor will spawn.<p><b>Types</b>- 0: None<br>- 1: Rainstorm<br>- 2: Thunderstorm<br>- 3: Supercell<br>- 4: Hailing Supercell<br>- 5: EF1 Tornado<br>- 6: Category 1 Hurricane*/
	public int stage = 1;
	public int maxStage = 7;

	//TODO: replace with ID and just lookup each time, for better serialization
	public StormObject lastTickStormObject = null;

	//for tracking between world reloads
	public UUID lastTickStormObjectID = null;

	public void cycleWeatherType(boolean reverse)
	{
		maxStage = ConfigStorm.disable_tornados || ConfigMisc.disable_weather_machine_cyclones ? 5 : 7;
		stage = (stage + (reverse ? -1 : 1)) % (maxStage + 1);
		
		if (stage < 0)
			stage = maxStage;
		
		setStormSettings();
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		killStorm();
	}


	public void setStormSettings()
	{
		if (lastTickStormObject != null)
			switch(stage)
			{
				case 1:
					lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
					lastTickStormObject.stage = Stage.NORMAL.getStage();
					lastTickStormObject.intensity = 0.01F;
					lastTickStormObject.updateType();
					break;
				case 2:
					lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
					lastTickStormObject.stage = Stage.RAIN.getStage();
					lastTickStormObject.intensity = 0.99F;
					lastTickStormObject.updateType();
					break;
				case 3:
					lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
					lastTickStormObject.stage = Stage.THUNDER.getStage();
					lastTickStormObject.intensity = 1.99F;
					lastTickStormObject.updateType();
					break;
				case 4:
					lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
					lastTickStormObject.stage = Stage.SEVERE.getStage();
					lastTickStormObject.intensity = 2.99F;
					lastTickStormObject.hail = 0.0F;
					lastTickStormObject.updateType();
					break;
				case 5:
					lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
					lastTickStormObject.stage = Stage.SEVERE.getStage();
					lastTickStormObject.intensity = 2.99F;
					lastTickStormObject.hail = 150.0F;
					lastTickStormObject.updateType();
					break;
				case 6:
					lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
					lastTickStormObject.stage = Stage.TORNADO.getStage() + 1;
					lastTickStormObject.intensity = 4.99F;
					lastTickStormObject.updateType();
					break;
				case 7:
					lastTickStormObject.stormType = StormObject.StormType.WATER.ordinal();
					lastTickStormObject.stage = Stage.HURRICANE.getStage();
					lastTickStormObject.intensity = 4.99F;
					lastTickStormObject.updateType();
					break;
				default:
					if (lastTickStormObject != null)
						killStorm();
			}
	}
	
	public void createStorm()
	{
		if (lastTickStormObject == null)
		{
			if (!ConfigMisc.aesthetic_mode)
			{
				WeatherManagerServer manager = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
				if (manager != null)
				{
					StormObject so  = manager.getGlobalFront().createStorm(getPos().getX(), getPos().getZ(), 1, null);
					so.isNatural = false;
					so.canProgress = false;
					so.overrideMotion = true;
					so.overrideAngle = true;
					so.shouldConvert = false;
					so.shouldBuildHumidity = false;
					so.rain = 150;
					so.temperature = 40;
					so.stageMax = 5;
					
					PacketWeatherObject.create(manager.getDimension(), so);
					lastTickStormObject = so;
					lastTickStormObjectID = so.getUUID();
					setStormSettings();
				}
			}
		}
	}
	
	public void killStorm()
	{
		if (lastTickStormObject != null)
		{
			lastTickStormObject.setDead();
			lastTickStormObject = null;
		}
	}
	
	@Override
	public void update()
	{
		if (!world.isRemote)
		{
			if (stage > 0 && world.getTotalWorldTime() % 40 == 0)
			{
				WeatherManagerServer manager = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
				if (manager != null)
					if (lastTickStormObject == null)
					{
						if (lastTickStormObjectID != null)
						{
							WeatherObject system = manager.getGlobalFront().getWeatherObject(lastTickStormObjectID);
							if (system != null)
							{
								lastTickStormObject = (StormObject)system;
								Weather2.debug("Weather machine reobtained storm " + system.getUUID());
							}
							else
							{
								createStorm();
								Weather2.debug("Weather machine created storm " + lastTickStormObjectID + " because old storm no longer exists");
							}
						}
						else
						{
							createStorm();
							Weather2.debug("Weather machine created storm " + lastTickStormObjectID);
						}
					}
					else if (lastTickStormObject.isDead)
					{
						lastTickStormObject = null;
						lastTickStormObjectID = null;
						createStorm();
						Weather2.debug("Weather machine created storm " + lastTickStormObjectID + " because old storm has died");
					}
			}
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound var1)
	{
		super.writeToNBT(var1);
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("weatherType", stage);
		if (lastTickStormObjectID != null)
			data.setUniqueId("lastTickStormObjectID", lastTickStormObjectID);
		else if (var1.hasKey("lastTickStormObjectID"))
			data.removeTag("lastTickStormObjectID");
		
		var1.setTag("weatherMachine", data);
		return var1;
	}

	public void readFromNBT(NBTTagCompound var1)
	{	
		if (var1.hasKey("weatherMachine"))
		{
			NBTTagCompound data = var1.getCompoundTag("weatherMachine");
			stage = data.getInteger("weatherType");
			if (data.hasUniqueId("lastTickStormObjectID"))
				lastTickStormObjectID = data.getUniqueId("lastTickStormObjectID");
		}
		super.readFromNBT(var1);
	}
}
