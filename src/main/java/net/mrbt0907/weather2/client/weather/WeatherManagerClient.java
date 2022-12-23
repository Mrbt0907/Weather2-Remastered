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
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.entity.EntityLightningBolt;
import net.mrbt0907.weather2.entity.EntityLightningBoltCustom;
import net.mrbt0907.weather2.weather.WeatherManager;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

@SideOnly(Side.CLIENT)
public class WeatherManagerClient extends WeatherManager
{
	//data for client, stormfronts synced from server
	//new for 1.10.2, replaces world.weatherEffects use
	public List<Particle> effectedParticles = new ArrayList<Particle>();
	public List<Particle> weatherParticles = new ArrayList<Particle>();
	public static StormObject closestStormCached;
	public int weatherID = 0;
	public int weatherRainTime = 0;

	public WeatherManagerClient(World world)
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
		
		Particle particle;
		for (int i = 0; i < weatherParticles.size(); i++)
		{
			particle = weatherParticles.get(i);
			
			if (!particle.isAlive())
			{
				weatherParticles.remove(i);
				i--;
			}
		}
	}
	
	public void tickRender(float partialTick)
	{
		if (world != null)
			getWeatherObjects().forEach(wo -> wo.tickRender(partialTick));
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
				FrontObject front = getFront(nbt.getUniqueId("frontUUID"));
				UUID uuid = nbt.getUniqueId("ID");
				
				Type weatherObjectType = Type.get(nbt.getInteger("weatherType"));
				
				WeatherObject wo = null;
				if (weatherObjectType.ordinal() < Type.SANDSTORM.ordinal())
				{
					Weather2.debug("Creating a new storm: " + uuid.toString());
					wo = new StormObject(front);
				}
				else
				{
					Weather2.debug("Creating a new sandstorm: " + uuid.toString());
					wo = new SandstormObject(this);
				}
				
				//StormObject so
				wo.nbt.setNewNBT(nbt);
				wo.nbt.updateCacheFromNew();
				wo.readNBT();
				
				front.addWeatherObject(wo);
				break;
			}
			case 2:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("weatherObject");
				FrontObject front = getFront(nbt.getUniqueId("frontUUID"));
				if(front == null) break;
					
				WeatherObject wo = front.getWeatherObject(nbt.getUniqueId("ID"));
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
				UUID uuidA = mainNBT.getUniqueId("weatherObject"), uuidB = mainNBT.getUniqueId("frontObject");
				FrontObject front = getFront(uuidB);
				
				if (front != null)
					front.removeWeatherObject(uuidA);
				else
					removeWeatherObject(uuidA);
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
				windManager.nbtSyncFromServer(nbt);
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
			case 11:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("frontObject");
				UUID uuid = mainNBT.getUniqueId("uuid");
				if (nbt.hasKey("posX"))
				{
					Weather2.debug("Creating a new front: " + uuid.toString());
					FrontObject front = createFront(nbt.getInteger("layer"), nbt.getDouble("posX"), nbt.getDouble("posZ"));
					front.readNBT(nbt);
					fronts.put(uuid, front);
				}
				else
				{
					Weather2.debug("Creating a new global front: " + uuid.toString());
					globalFront = new FrontObject(this, null, 0);
					globalFront.readNBT(nbt);
					fronts.put(uuid, globalFront);
				}
				break;
			}
			case 12:
			{
				NBTTagCompound nbt = mainNBT.getCompoundTag("frontObject");
				FrontObject front = getFront(nbt.getUniqueId("uuid"));
				if(front != null)
					front.readNBT(nbt);
				break;
			}
			case 13:
			{
				UUID uuid = mainNBT.getUniqueId("frontUUID");
				FrontObject front = getFront(uuid);
				if (front != null)
					if (globalFront.equals(front))
						globalFront.reset();
					else
					{
						front.reset();
						removeFront(front);
					}
				else
					Weather2.error("error removing front, cant find by ID: " + uuid.toString());
				break;
			}
			case 14:
			{
				fronts.forEach((uuid, front) -> front.cleanupClient(false));
				weatherParticles.forEach(particle -> particle.setExpired());
				weatherParticles.clear();
				Weather2.debug("Cleaned up client particles");
				break;
			}
			default:
				Weather2.error("Server sent an invalid network packet");
		}
	}
	
	public void addWeatherParticle(Particle particle)
	{
		weatherParticles.add(particle);
	}
	
	public void addEffectedParticle(Particle particle)
	{
		effectedParticles.add(particle);
	}

	public int getParticleCount()
	{
		return weatherParticles.size();
	}
	
	@Override
	public void reset()
	{
		super.reset();
		effectedParticles.clear();
		closestStormCached = null;
	}
}
