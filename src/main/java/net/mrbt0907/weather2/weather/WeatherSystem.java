package net.mrbt0907.weather2.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import CoroUtil.util.CoroUtilPhysics;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.IWeatherLayered;
import net.mrbt0907.weather2.api.IWeatherRain;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.weather.storm.WeatherEnum;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

import CoroUtil.util.Vec3;

public class WeatherSystem
{
	public long ticks;
	protected int dim;
	protected World world;
	public WindManager windMan;
	//0 = none, 1 = usual max overcast
	public float cloudIntensity = 1F;
	protected HashSet<Long> listWeatherBlockDamageDeflector = new HashSet<>();
	
	//storms
	protected List<WeatherObject> weatherObjects = new ArrayList<WeatherObject>();
	protected List<UUID> weatherUUIDS = new ArrayList<UUID>();
	protected HashMap<Integer, ArrayList<WeatherObject>> weatherObjectsPerLayer = new HashMap<>();
	
	//volcanos
	protected List<VolcanoObject> volcanoObjects = new ArrayList<>();
	protected List<UUID> volcanoUUIDS = new ArrayList<UUID>();
	
	//ClientSide
	

	public WeatherSystem(World world)
	{
		if (world == null)
			Weather2.error("WeatherSystem recieved a null world. Game may crash");
		else
			Weather2.debug("Creating new WeatherSystem for dimension #" + world.provider.getDimension());
		this.world = world;
		dim = world.provider.getDimension();
		windMan = new WindManager(this);
		weatherObjectsPerLayer.put(0, new ArrayList<WeatherObject>());
		weatherObjectsPerLayer.put(1, new ArrayList<WeatherObject>());
		weatherObjectsPerLayer.put(2, new ArrayList<WeatherObject>());
	}
	
	public void reset()
	{
		//Remove weather objects
		weatherObjects.forEach(so -> so.reset());
		weatherObjects.clear();
		weatherUUIDS.clear();
		weatherObjectsPerLayer.forEach((layer, list) -> list.clear());
		//Remove volcanos
		volcanoObjects.forEach(vo -> vo.reset());
		volcanoObjects.clear();
		volcanoUUIDS.clear();
		//Reset wind manager
		windMan.reset();
	}
	
	public void tick()
	{
		if (world != null)
		{
			int size = weatherObjects.size();
			WeatherObject so;
			for(int i = 0; i < size; i++)
			{
				try
				{
				so = weatherObjects.get(i);
				if (so.isDead)
				{
					i--;
					size--;
					if (!getWorld().isRemote)
					{
						removeStormObject(so.getUUID());
						PacketWeatherObject.remove(dim, so);
					}
					else
					{
						Weather2.warn("Detected isDead storm object still in client side list, had to remove storm object with ID " + so.getUUID() + " from client side, wasnt properly removed via main channels");
						removeStormObject(so.getUUID());
					}
				}
				else
					so.tick();
				}
				catch (Exception e) {}
			}
			
			//tick volcanos
			volcanoObjects.forEach(vo -> vo.tick());

			//tick wind
			windMan.tick();
			ticks++;
		}
	}
	
	public List<WeatherObject> getWeatherObjects() {return weatherObjects;}
	
	public List<WeatherObject> getWeatherObjectsByLayer(int layer){return weatherObjectsPerLayer.get(layer);}

	public WeatherObject getWeatherObjectByID(UUID ID)
	{
		int size = weatherUUIDS.size();
		for (int i = 0; i < size; i++)
			if (weatherUUIDS.get(i).equals(ID))
				return weatherObjects.get(i);
		return null;
	}
	
	public void addStormObject(WeatherObject so)
	{
		if (!weatherUUIDS.contains(so.getUUID()))
		{
			weatherObjects.add(so);
			weatherUUIDS.add(so.getUUID());
			if (so instanceof IWeatherLayered)
			{
				weatherObjectsPerLayer.get(((IWeatherLayered)so).getCurrentLayer()).add(so);
			}
		}
		else
			Weather2.warn("Received new storm create for an ID that is already active! design bug or edgecase with PlayerEvent.Clone, ID: " + so.getUUID());
	}
	
	public void removeStormObject(UUID ID)
	{
		WeatherObject so = getWeatherObjectByID(ID);
		
		if (so != null)
		{
			so.setDead();
			weatherObjects.remove(so);
			weatherUUIDS.remove(ID);
			if (so instanceof IWeatherLayered)
				weatherObjectsPerLayer.get(((IWeatherLayered)so).getCurrentLayer()).remove(so);
		}
		else
			Weather2.warn("error looking up storm ID on server for removal: " + ID + " - lookup count: " + weatherUUIDS.size());
	}
	
	public List<VolcanoObject> getVolcanoObjects()
	{
		return volcanoObjects;
	}
	
	public VolcanoObject getVolcanoObjectByID(UUID ID)
	{
		int size = volcanoUUIDS.size();
		for (int i = 0; i < size; i++)
			if (volcanoUUIDS.get(i).equals(ID))
				return volcanoObjects.get(i);
		return null;
	}
	
	public void addVolcanoObject(VolcanoObject so)
	{
		if (!volcanoUUIDS.contains(so.getUUID()))
		{
			volcanoObjects.add(so);
			volcanoUUIDS.add(so.getUUID());
		}
		else
			Weather2.warn("Client received new volcano create for an ID that is already active! design bug");
	}
	
	public void removeVolcanoObject(UUID ID)
	{
		VolcanoObject vo = getVolcanoObjectByID(ID);
		if (vo != null)
		{
			vo.setDead();
			volcanoObjects.remove(vo);
			volcanoUUIDS.remove(ID);
			
			Weather2.debug("removing volcano");
		}
	}
	
	public WeatherObject getClosestStorm(Vec3 parPos, double maxDist)
	{
		return getClosestStorm(parPos, maxDist, WeatherEnum.Type.RAIN.getStage(), 25565);
	}
	
	public WeatherObject getClosestStorm(Vec3 parPos, double maxDist, WeatherEnum.Type weatherType)
	{
		return getClosestStorm(parPos, maxDist, weatherType.getStage(), 10);
	}
	
	public WeatherObject getClosestStorm(Vec3 parPos, double maxDist, int weatherTypeMin, int weatherTypeMax, WeatherEnum.Type... excludedTypes)
	{
		WeatherObject obj = null; double dist = maxDist;
		
		for (WeatherObject wo : weatherObjects)
		{
			if (!wo.isDead)
			{
				double a = wo.pos.distanceTo(parPos);
				int b = wo.type.ordinal();
				int c = wo.type.getStage();
				for (WeatherEnum.Type d : excludedTypes) if (b == d.ordinal()) continue;
				if (a < dist && a <= maxDist && c >= weatherTypeMin && c <= weatherTypeMax)
				{
					obj = wo;
					dist = a;
				}
			}
		}
		
		return obj;
	}
	
	public WeatherObject getWorstStorm(Vec3 parPos, double maxDist)
	{
		return getWorstStorm(parPos, maxDist, WeatherEnum.Type.RAIN.getStage(), 25565);
	}
	
	public WeatherObject getWorstStorm(Vec3 parPos, double maxDist, WeatherEnum.Type weatherType)
	{
		return getWorstStorm(parPos, maxDist, weatherType.getStage(), 10);
	}
	
	public WeatherObject getWorstStorm(Vec3 parPos, double maxDist, int weatherTypeMin, int weatherTypeMax, WeatherEnum.Type... excludedTypes)
	{
		WeatherObject obj = null; double dist = maxDist; int stage = 0;
		
		for (WeatherObject wo : weatherObjects)
		{
			if (!wo.isDead && wo instanceof StormObject)
			{
				double a = wo.pos.distanceTo(parPos);
				int b = wo.type.ordinal();
				int c = wo.type.getStage();
				for (WeatherEnum.Type d : excludedTypes) if (b == d.ordinal()) continue;
				if (a < dist && a <= maxDist && c >= weatherTypeMin && c <= weatherTypeMax && ((StormObject)wo).stormStage > stage)
				{
					obj = wo;
					dist = a;
					stage = ((StormObject)wo).stormStage;
				}
			}
		}
		
		return obj;
	}
	
	public WeatherObject getClosestSpecificStorm(Vec3 parPos, double maxDist)
	{
		return getClosestSpecificStorm(parPos, maxDist, WeatherEnum.Type.RAIN.ordinal(), WeatherEnum.Type.SANDSTORM.ordinal());
	}
	
	public WeatherObject getClosestSpecificStorm(Vec3 parPos, double maxDist, WeatherEnum.Type weatherType)
	{
		return getClosestSpecificStorm(parPos, maxDist, weatherType.ordinal(), weatherType.ordinal());
	}
	
	public WeatherObject getClosestSpecificStorm(Vec3 parPos, double maxDist, int weatherTypeMin, int weatherTypeMax, WeatherEnum.Type... excludedTypes)
	{
		WeatherObject obj = null; double dist = maxDist;
		
		for (WeatherObject wo : weatherObjects)
		{
			if (!wo.isDead)
			{
				double a = wo.pos.distanceTo(parPos);
				int b = wo.type.ordinal();
				for (WeatherEnum.Type c : excludedTypes) if (b == c.ordinal()) continue;
				
				if (a < dist && a <= maxDist && b >= weatherTypeMin && b <= weatherTypeMax)
				{
					obj = wo;
					dist = a;
				}
			}
		}
		
		return obj;
	}

	public boolean isPrecipitatingAt(BlockPos pos)
	{
		return isPrecipitatingAt(new Vec3(pos));
	}

	/**
	 * TODO: Heavy on the processing, consider caching the result by location for 20 ticks
	 *
	 * @param parPos
	 * @return
	 */
	public boolean isPrecipitatingAt(Vec3 parPos)
	{
		for (WeatherObject wo : weatherObjects)
			if (wo instanceof IWeatherRain && ((IWeatherRain)wo).isRaining() && wo.pos.distanceTo(parPos) < wo.size)
				return true;
		return false;
	}

	/**
	 * Simply compares stormfront distances, doesnt factor in tail
	 *
	 * @param parPos
	 * @param maxDist
	 * @return
	 */
	public SandstormObject getClosestSandstorm(Vec3 parPos, double maxDist) {
		
		SandstormObject closestStorm = null;
		double closestDist = 9999999;
		
		List<WeatherObject> listStorms = weatherObjects;
		
		for (int i = 0; i < listStorms.size(); i++) {
			WeatherObject wo = listStorms.get(i);
			if (wo instanceof SandstormObject) {
				SandstormObject storm = (SandstormObject) wo;
				if (storm == null || storm.isDead) continue;
				double dist = storm.pos.distanceTo(parPos);
				/*if (getWorld().isRemote) {
					System.out.println("close storm candidate: " + dist + " - " + storm.state + " - " + storm.attrib_rain);
				}*/
				if (dist < closestDist && dist <= maxDist) {
					//if ((storm.attrib_precipitation && orRain) || (severityFlagMin == -1 || storm.levelCurIntensityStage >= severityFlagMin)) {
						closestStorm = storm;
						closestDist = dist;
					//}
				}
			}
			
		}
		
		return closestStorm;
	}

	/**
	 * Gets the most intense sandstorm, used for effects and sounds
	 *
	 * @param parPos
	 * @return
	 */
	public SandstormObject getClosestSandstormByIntensity(Vec3 parPos/*, double maxDist*/) {

		SandstormObject bestStorm = null;
		double closestDist = 9999999;
		double mostIntense = 0;

		List<WeatherObject> listStorms = weatherObjects;

		for (int i = 0; i < listStorms.size(); i++) {
			WeatherObject wo = listStorms.get(i);
			if (wo instanceof SandstormObject) {
				SandstormObject sandstorm = (SandstormObject) wo;
				if (sandstorm == null || sandstorm.isDead) continue;

				List<Vec3> points = sandstorm.getSandstormAsShape();

				double scale = sandstorm.getSandstormScale();
				boolean inStorm = CoroUtilPhysics.isInConvexShape(parPos, points);
				double dist = CoroUtilPhysics.getDistanceToShape(parPos, points);
				//if best is within storm, compare intensity
				if (inStorm) {
					//System.out.println("in storm");
					closestDist = 0;
					if (scale > mostIntense) {
						mostIntense = scale;
						bestStorm = sandstorm;
					}
				//if best is not within storm, compare distance to shape
				} else if (closestDist > 0/* && dist < maxDist*/) {
					if (dist < closestDist) {
						closestDist = dist;
						bestStorm = sandstorm;
					}
				}
			}

		}

		return bestStorm;
	}

	public List<WeatherObject> getSandstormsAround(Vec3 parPos, double maxDist) {
		List<WeatherObject> storms = new ArrayList<>();

		for (int i = 0; i < weatherObjects.size(); i++) {
			WeatherObject wo = weatherObjects.get(i);
			if (wo instanceof SandstormObject) {
				SandstormObject storm = (SandstormObject) wo;
				if (storm.isDead) continue;

				if (storm.pos.distanceTo(parPos) < maxDist) {
					storms.add(storm);
				}
			}
		}

		return storms;
	}

	public List<WeatherObject> getClosestStorms(Vec3 parPos, double maxDist)
	{
		return getClosestStorms(parPos, maxDist, WeatherEnum.Type.RAIN.getStage(), 10);
	}
	
	public List<WeatherObject> getClosestStorms(Vec3 parPos, double maxDist, WeatherEnum.Type weatherType)
	{
		return getClosestStorms(parPos, maxDist, weatherType.getStage(), 10);
	}
	
	public List<WeatherObject> getClosestStorms(Vec3 parPos, double maxDist, int weatherTypeMin, int weatherTypeMax, WeatherEnum.Type... excludedTypes)
	{
		List<WeatherObject> obj = new ArrayList<WeatherObject>();
		
		for (WeatherObject wo : weatherObjects)
		{
			if (!wo.isDead)
			{
				double a = wo.pos.distanceTo(parPos);
				int b = wo.type.ordinal();
				int c = wo.type.getStage();
				for (WeatherEnum.Type d : excludedTypes) if (b == d.ordinal()) continue;
				
				if (a <= maxDist && c >= weatherTypeMin && c <= weatherTypeMax)
					obj.add(wo);
			}
		}
		
		return obj;
	}

	public HashSet<Long> getListWeatherBlockDamageDeflector()
	{
		return listWeatherBlockDamageDeflector;
	}

	public void setListWeatherBlockDamageDeflector(HashSet<Long> listWeatherBlockDamageDeflector)
	{
		this.listWeatherBlockDamageDeflector = listWeatherBlockDamageDeflector;
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public int getDimension()
	{
		return dim;
	}
	
	public boolean isClient()
	{
		return world != null && world.isRemote;
	}
}
