package net.mrbt0907.weather2.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.server.weather.WeatherSystemServer;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherEnum;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.UUID;

import CoroUtil.util.Vec3;

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
	public int weatherType = 1;
	//ya
	public int weatherSize = 50;

	//TODO: replace with ID and just lookup each time, for better serialization
	public StormObject lastTickStormObject = null;

	//for tracking between world reloads
	public UUID lastTickStormObjectID = null;

	public void cycleWeatherType(boolean reverse) {

		int maxID = 6;
		if (ConfigStorm.disable_tornados || ConfigMisc.disable_weather_machine_cyclones) {
			maxID = 4;
		}

		int minID = 0;

		if (!reverse) {

			weatherType++;

			if (weatherType > maxID) {
				weatherType = minID;
			}
		} else {
			weatherType--;

			if (weatherType < minID) {
				weatherType = maxID;
			}
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();

		killStorm();
	}

	public void killStorm() {
		WeatherSystemServer wm = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
		if (wm != null && lastTickStormObject != null)
			lastTickStormObject.isDead = true;
	}
	
	@Override
	public void update()
	{
		if (!world.isRemote)
		{
			weatherSize = 100;
			
			if (weatherType == 0)
			{
				if (lastTickStormObject != null)
					killStorm();
				return;
			}
			
			if (world.getTotalWorldTime() % 40 == 0)
			{	
				if (lastTickStormObject != null && lastTickStormObject.isDead)
					lastTickStormObject = null;
					
				WeatherSystemServer manager = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
				if (manager != null)
				{
					//for when world is reloaded, regrab instance so a duplicate isnt greated (and so old one doesnt get loose)
					if (lastTickStormObject == null && lastTickStormObjectID != null)
					{
						WeatherObject obj = manager.getWeatherObjectByID(lastTickStormObjectID);
						if (obj != null)
						{
							lastTickStormObject = (StormObject)obj;
							Weather2.debug("regrabbed old storm instance by ID " + obj.getUUID() + " for weather machine");
						}
					}
					
					if (lastTickStormObject == null && !ConfigMisc.aesthetic_mode)
					{
							StormObject so = new StormObject(manager);
							
							so.init();
							so.pos = new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ());
							so.layer = 0;
							so.player = "" + getPos().getX() + getPos().getY() + getPos().getZ();
							so.isNatural = false;
							
							
							manager.addStormObject(so);
							PacketWeatherObject.create(manager.getDimension(), so);
							lastTickStormObject = so;
							lastTickStormObjectID = so.getUUID();
					}
				}
			}
			
			if (lastTickStormObject != null && !lastTickStormObject.isDead)
			{
				lastTickStormObject.isMachineControlled = true;
				lastTickStormObject.size = weatherSize;


				lastTickStormObject.stormRain = 1000;
				lastTickStormObject.isRaining = true;
				lastTickStormObject.isDying = false;
				lastTickStormObject.stormIntensity = 0.9F;
				lastTickStormObject.stormTemperature = 40;
				
				switch(weatherType)
				{
					case 0:
						lastTickStormObject.stormTemperature = -40;
						lastTickStormObject.stormType = StormObject.Type.LAND.getInt();
						lastTickStormObject.stormStage = StormObject.Stage.NORMAL.getInt();
						lastTickStormObject.type = WeatherEnum.Type.CLOUD;
						lastTickStormObject.stormIntensity = 0.01F;
						break;
					case 1:
						lastTickStormObject.stormType = StormObject.Type.LAND.getInt();
						lastTickStormObject.stormStage = StormObject.Stage.NORMAL.getInt();
						lastTickStormObject.type = WeatherEnum.Type.RAIN;
						lastTickStormObject.stormIntensity = 0.01F;
						break;
					case 2:
						lastTickStormObject.stormType = StormObject.Type.LAND.getInt();
						lastTickStormObject.stormStage = StormObject.Stage.THUNDER.getInt();
						lastTickStormObject.type = WeatherEnum.Type.THUNDER;
						lastTickStormObject.stormIntensity = 0.99F;
						break;
					case 3:
						lastTickStormObject.stormType = StormObject.Type.LAND.getInt();
						lastTickStormObject.stormStage = StormObject.Stage.SEVERE.getInt();
						lastTickStormObject.type = WeatherEnum.Type.SUPERCELL;
						lastTickStormObject.stormIntensity = 1.99F;
						break;
					case 4:
						lastTickStormObject.stormType = StormObject.Type.LAND.getInt();
						lastTickStormObject.stormStage = StormObject.Stage.HAIL.getInt();
						lastTickStormObject.type = WeatherEnum.Type.SUPERCELL;
						lastTickStormObject.stormIntensity = 2.99F;
						break;
					case 5:
						lastTickStormObject.stormType = StormObject.Type.LAND.getInt();
						lastTickStormObject.stormStage = StormObject.Stage.STAGE1.getInt();
						lastTickStormObject.type = WeatherEnum.Type.TORNADO;
						lastTickStormObject.stormIntensity = 4.99F;
						break;
					case 6:
						lastTickStormObject.stormType = StormObject.Type.WATER.getInt();
						lastTickStormObject.stormStage = StormObject.Stage.STAGE1.getInt();
						lastTickStormObject.type = WeatherEnum.Type.HURRICANE;
						lastTickStormObject.stormIntensity = 4.99F;
						break;
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound var1)
	{
		var1.setInteger("weatherType", weatherType);
		if (lastTickStormObjectID != null)
			var1.setUniqueId("lastTickStormObjectID", lastTickStormObjectID);
 		else if (var1.hasKey("lastTickStormObjectID"))
			var1.removeTag("lastTickStormObjectID");
		return super.writeToNBT(var1);
	}

	@Override
	public void readFromNBT(NBTTagCompound var1)
	{
		super.readFromNBT(var1);
		weatherType = var1.getInteger("weatherType");
		if (var1.hasKey("lastTickStormObjectID"))
		{
			lastTickStormObjectID = var1.getUniqueId("lastTickStormObjectID");
		}
	}
}
