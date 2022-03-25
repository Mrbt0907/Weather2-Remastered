package net.mrbt0907.weather2.client.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.entity.EntityLightningBolt;
import net.mrbt0907.weather2.entity.EntityLightningBoltCustom;
import net.mrbt0907.weather2.weather.WeatherSystem;
import net.mrbt0907.weather2.weather.storm.EnumWeatherObjectType;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

@SideOnly(Side.CLIENT)
public class WeatherSystemClient extends WeatherSystem {

	//data for client, stormfronts synced from server
	
	//new for 1.10.2, replaces world.weatherEffects use
	public List<Particle> listWeatherEffectedParticles = new ArrayList<Particle>();
	public static StormObject closestStormCached;
	public int weatherID = 0;
	public int weatherRainTime = 0;

	public WeatherSystemClient(World world)
	{
		super(world);
	}
	
	@Override
	public World getWorld()
	{
		return FMLClientHandler.instance().getClient().world;
	}
	
	@Override
	public void tick()
	{
		super.tick();
	}
	
	public void tickRender(float partialTick)
	{
		if (world != null)
		{
			//tick storms
			//There are scenarios where getStormObjects().get(i) returns a null storm, uncertain why, for now try to catch it and move on
			weatherObjects.forEach(wo -> wo.tickRender(partialTick));
		}
	}
	
	/**Gets called when the server sends a network packet<p><b>Network Command List</b><br>
	 *0 - Update Vanilla Weather<br>
	 *1 - Create Weather Object<br>
	 *2 - Update Weather Object<br>
	 *3 - Remove Weather Object<br>
	 *4 - Create Volcano Object<br>
	 *5 - Update Volcano Object<br>
	 *6 - Update Wind Manager<br>
	 *7 - Create Lightning Bolt*/
	public void nbtSyncFromServer(NBTTagCompound mainNBT)
	{
		int command = mainNBT.getInteger("command");
		switch(command)
		{
			case 0:
				weatherID = mainNBT.getInteger("weatherID");
				weatherRainTime = mainNBT.getInteger("weatherRainTime");
				break;
			case 1:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("weatherObject");
				UUID uuid = nbt.getUniqueId("ID");
				Weather2.debug("Creating a new storm: " + uuid.toString());
				EnumWeatherObjectType weatherObjectType = EnumWeatherObjectType.get(nbt.getInteger("weatherObjectType"));
				
				WeatherObject wo = null;
				if (weatherObjectType == EnumWeatherObjectType.CLOUD)
					wo = new StormObject(this);
				else if (weatherObjectType == EnumWeatherObjectType.SAND)
					wo = new SandstormObject(this);
				
				//StormObject so
				wo.nbt.setNewNBT(nbt);
				wo.nbt.updateCacheFromNew();
				wo.readNBT();
				
				addStormObject(wo);
				break;
			}
			case 2:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("weatherObject");
				WeatherObject wo = getWeatherObjectByID(nbt.getUniqueId("ID"));
				if (wo != null)
				{
					wo.nbt.setNewNBT(nbt);
					wo.readNBT();
					wo.nbt.updateCacheFromNew();
				}
				break;
			}
			case 3:
			{
				UUID uuid = mainNBT.getUniqueId("uuid");
				if (weatherUUIDS.contains(uuid))
					removeStormObject(uuid);
				else
					Weather2.debug("error removing storm, cant find by ID: " + uuid.toString());
				break;
			}
			case 4:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("volcanoObject");
				VolcanoObject vo = new VolcanoObject(this);
				Weather2.debug("Creating a new volcano: " + nbt.getUniqueId("ID"));
				vo.nbtSyncFromServer(nbt);
				addVolcanoObject(vo);
				break;
			}
			case 5:
			{
				NBTTagCompound stormNBT = mainNBT.getCompoundTag("volcanoObject");
				UUID uuid = stormNBT.getUniqueId("ID");
				
				if (volcanoUUIDS.contains(uuid))
					getVolcanoObjectByID(uuid).nbtSyncFromServer(stormNBT);
				break;
			}
			case 6:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("manager");
				windMan.nbtSyncFromServer(nbt);
				break;
			}
			case 7:
			{
				int posXS = mainNBT.getInteger("posX");
				int posYS = mainNBT.getInteger("posY");
				int posZS = mainNBT.getInteger("posZ");
				
				boolean custom = mainNBT.getBoolean("useCustomLightning");
				
				//Weather.dbg("uhhh " + parNBT);
				
				double posX = (double)posXS;
				double posY = (double)posYS;
				double posZ = (double)posZS;
				Entity ent = null;
				if (!custom)
					ent = new EntityLightningBolt(getWorld(), posX, posY, posZ);
				else
					ent = new EntityLightningBoltCustom(getWorld(), posX, posY, posZ);
				ent.serverPosX = posXS;
				ent.serverPosY = posYS;
				ent.serverPosZ = posZS;
				ent.rotationYaw = 0.0F;
				ent.rotationPitch = 0.0F;
				ent.setEntityId(mainNBT.getInteger("entityID"));
				getWorld().addWeatherEffect(ent);
				break;
			}
			default:
				Weather2.error("Server sent an invalid network packet");
		}
		/*if (command.equals("syncVolcanoRemove")) {
			Weather2.debug("removing client side volcano");
			NBTTagCompound stormNBT = parNBT.getCompoundTag("data");
			long ID = stormNBT.getLong("ID");
			
			VolcanoObject so = volcanoUUIDS.get(ID);
			if (so != null) {
				removeVolcanoObject(ID);
			}
		} */
	}
	
	public void addWeatheredParticle(Particle particle)
	{
		listWeatherEffectedParticles.add(particle);
	}

	@Override
	public void reset()
	{
		super.reset();
		listWeatherEffectedParticles.clear();
		closestStormCached = null;
	}
}
