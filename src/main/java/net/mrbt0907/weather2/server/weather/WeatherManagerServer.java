package net.mrbt0907.weather2.server.weather;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.FileUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.IWeatherStaged;
import net.mrbt0907.weather2.config.ConfigFront;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.config.ConfigSand;
import net.mrbt0907.weather2.config.ConfigSimulation;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.network.packets.PacketFrontObject;
import net.mrbt0907.weather2.network.packets.PacketVanillaWeather;
import net.mrbt0907.weather2.network.packets.PacketVolcanoObject;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.network.packets.PacketWind;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilConfig;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.WeatherManager;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.TornadoHelper;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;
import CoroUtil.util.CoroUtilFile;

public class WeatherManagerServer extends WeatherManager
{
	private long ticksFrontFormed = 0L;
	private long ticksSandstormFormed = 0L;
	private long ticksStormFormed = 0L;
	
	public WeatherManagerServer(World world)
	{
		super(world);
	}
	
	@Override
	public World getWorld()
	{
		return DimensionManager.getWorld(dim);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if (world != null)
		{
			tickWeatherCoverage(ticks);

			//sync storms
			FrontObject front;
			WeatherObject system;
			
			List<FrontObject> fronts = new ArrayList<FrontObject>(this.fronts.values());
			List<WeatherObject> systems = getWeatherObjects();
			WeatherObject spawn = null;
			boolean spawned = false, spawnInFront = Maths.chance(ConfigFront.chance_to_spawn_storm_in_front * 0.01D);
			
			for (int i = 0; i < fronts.size(); i++)
			{
				front = fronts.get(i);
				if (front.isDead)
				{
					if (front.isGlobal())
					{
						front.reset();
						front.isDead = false;
					}
					else
					{
						PacketFrontObject.remove(dim, front);
						removeFront(front.getUUID());
					}
				}
				else
				{
					if(!front.equals(globalFront) && spawnInFront && canSpawnWeather(1))
					{
						spawn = front.createNaturalStorm();
						if (spawn != null)
						{
							spawned = true;
							PacketWeatherObject.create(dim, spawn);
						}
					}
					if (ticks % 40 == 0)
						PacketFrontObject.update(dim, front);
				}
			}
			
			if (spawned)
				ticksStormFormed = world.getTotalWorldTime() + ConfigStorm.storm_spawn_delay;
			
			for (int i = 0; i < systems.size(); i++)
			{
				system = systems.get(i);
				
				if (ticks % 20 == 0)
				{
					if (ConfigMisc.remove_storms_if_no_players && world.playerEntities.size() == 0 || WeatherUtilEntity.getClosestPlayer(world, system.posGround.posX, system.posGround.posY, system.posGround.posZ, ConfigSimulation.max_storm_distance) == null)
						system.ticksSinceNoNearPlayer += 20;
					else
						system.ticksSinceNoNearPlayer = 0;
				}
				
				if (system.isDead || system.ticksSinceNoNearPlayer > 600 || ConfigMisc.aesthetic_mode)
				{
					PacketWeatherObject.remove(dim, system);
					system.front.removeWeatherObject(system.getUUID());
				}
				else if (ticks % system.getNetRate() == 0)
					PacketWeatherObject.update(dim, system);
			}
			
			//sync volcanos
			if (ticks % 40 == 0)
				volcanoObjects.forEach(vo -> PacketVolcanoObject.update(dim, vo));
			
			//sync wind and IMC
			if (ticks % 60 == 0)
			{
				PacketWind.update(dim, windManager);
				nbtStormsForIMC();
			}

			//cloud formation spawning - REFINE ME!
			if (!ConfigMisc.aesthetic_mode)
			{
				if (WeatherUtilConfig.isWeatherEnabled(dim) && world.getTotalWorldTime() % ConfigStorm.spawningTickRate == 0)
				{
					List<EntityPlayer> players = world.playerEntities;
					int layer, frontCount = fronts.size() + 1;
					
					for (EntityPlayer player : players)
					{
						layer = Maths.random(2);
						if (canSpawnWeather(0) && ConfigStorm.isLayerValid(layer))
						{
							ticksFrontFormed = world.getTotalWorldTime() + ConfigStorm.storm_spawn_delay;
							PacketFrontObject.create(dim, createNaturalFront(layer, player));
							
							if (!ConfigStorm.enable_spawn_per_player)
								break;
						}
						for (int i = 0; i < frontCount; i++)
							if(!spawnInFront && canSpawnWeather(1))
							{
								spawn = globalFront.createNaturalStorm(player);
								if (spawn != null)
								{
									spawned = true;
									PacketWeatherObject.create(dim, spawn);
								}
							}
					}
					
					if (!spawnInFront && spawned)
						ticksStormFormed = world.getTotalWorldTime() + ConfigStorm.storm_spawn_delay;
					
					if (canSpawnWeather(2))
					{
						if (world.playerEntities.size() > 0)
						{
							EntityPlayer player = world.playerEntities.get(Maths.random(world.playerEntities.size() - 1));
							ticksSandstormFormed = world.getTotalWorldTime() + ConfigSand.sandstorm_spawn_delay;
							spawnSandstorm(new Vec3(player.posX, player.posY, player.posZ));
						}
					}
				}
			}
			
			world.profiler.startSection("tickProcess");
			TornadoHelper.tickProcess(world);
			world.profiler.endSection();
		}
	}

	public void tickWeatherCoverage(long ticks)
	{
		WorldInfo worldInfo = world.getWorldInfo();
		boolean isRaining = worldInfo.isRaining();
		boolean isThundering = worldInfo.isThundering();
		if (WeatherUtilConfig.isWeatherEnabled(dim))
		{
			if (!ConfigMisc.overcast_mode && ConfigMisc.server_weather_mode != -1)
			{
				worldInfo.setRaining(ConfigMisc.server_weather_mode == 1);
				worldInfo.setThundering(ConfigMisc.server_weather_mode == 1);
			}
				
			if (isThundering && ConfigStorm.prevent_vanilla_thunderstorms)
				worldInfo.setThundering(false);
		}
			
		if (ticks % 40 == 0)
			PacketVanillaWeather.send(dim, isRaining ? isThundering ? 2 : 1 : 0, getWorld().getWorldInfo().getRainTime());

		//tick partial cloud cover variation0
		if (ticks % 200 == 0)
			cloudIntensity = ConfigMisc.overcast_mode && isRaining ? 1.0F : cloudIntensity + (float) MathHelper.clamp(Maths.random(ConfigParticle.cloud_coverage_change_amount) - Maths.random(ConfigParticle.cloud_coverage_change_amount), ConfigParticle.min_cloud_coverage_perc * 0.01D, ConfigParticle.max_cloud_coverage_perc * 0.01D);
				//force full cloudIntensity if server side raining
				//note: storms also revert to clouded storms for same condition
	}

	public void writeToFile()
	{
		Weather2.debug("Saving weather2 data...");
		NBTTagCompound mainNBT = new NBTTagCompound();
		NBTTagCompound volcanoesNBT = new NBTTagCompound();
		volcanoObjects.forEach(vo -> {
			NBTTagCompound nbt = new NBTTagCompound();
			vo.writeToNBT(nbt);
			volcanoesNBT.setTag("volcano_" + vo.ID, nbt);
		});
		mainNBT.setTag("volcanoData", volcanoesNBT);
		
		NBTTagCompound frontNBT = new NBTTagCompound();
		NBTTagCompound weatherNBT = new NBTTagCompound();
		frontNBT.setTag("front_global", globalFront.writeNBT());
		globalFront.getWeatherObjects().forEach(weatherObject ->
			{
				weatherObject.nbt.setUpdateForced(true);
				weatherObject.nbt.setUUID("frontUUID", globalFront.getUUID());
				weatherNBT.setTag("storm_" + weatherObject.getUUID().toString(), weatherObject.writeToNBT().getNewNBT());
				weatherObject.nbt.setUpdateForced(false);
				Weather2.debug("Saved storm_" + weatherObject.getUUID().toString());
			}
		);
		Weather2.debug("Saved front_global");
		
		fronts.forEach((uuid, front) ->
			{
				if (!front.equals(globalFront))
				{
					frontNBT.setTag("front_" + uuid.toString(), front.writeNBT());
					front.getWeatherObjects().forEach(weatherObject ->
						{
							weatherObject.nbt.setUpdateForced(true);
							weatherObject.nbt.setUUID("frontUUID", uuid);
							weatherNBT.setTag("storm_" + weatherObject.getUUID().toString(), weatherObject.writeToNBT().getNewNBT());
							weatherObject.nbt.setUpdateForced(false);
							Weather2.debug("Saved storm_" + weatherObject.getUUID().toString());
						}
					);
					Weather2.debug("Saved front_" + uuid.toString());
				}
			}
		);
		
		mainNBT.setTag("frontData", frontNBT);
		mainNBT.setTag("stormData", weatherNBT);
		mainNBT.setFloat("cloudIntensity", cloudIntensity);
		mainNBT.setFloat("ticksFrontFormed", ticksFrontFormed);
		mainNBT.setFloat("ticksSandstormFormed", ticksSandstormFormed);
		mainNBT.setFloat("ticksStormFormed", ticksStormFormed);
		mainNBT.setTag("windMan", windManager.writeToNBT(new NBTTagCompound()));
		
		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "weather2" + File.separator;
		
		try
		{
			//Write out to file
			if (!(new File(saveFolder).exists())) new File(saveFolder).mkdirs();
			FileOutputStream fos = new FileOutputStream(saveFolder + "WeatherData_" + dim + ".dat");
			CompressedStreamTools.writeCompressed(mainNBT, fos);
			fos.close();
			Weather2.debug("Save successful!");
		}
		catch (Exception ex)
		{
			Weather2.debug("Save failed.");
			ex.printStackTrace();
		}
	}
	
	public void readFromFile()
	{
		Weather2.debug("Loading weather2 data...");
		NBTTagCompound mainNBT = new NBTTagCompound();
		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "weather2" + File.separator;

		try
		{
			if (new File(saveFolder + "WeatherData_" + dim + ".dat").exists())
			{
				mainNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "WeatherData_" + dim + ".dat"));
				File tmp = (new File(saveFolder + "WeatherData_" + dim + "_BACKUP0.dat"));
				if (tmp.exists()) FileUtils.copyFile(tmp, (new File(saveFolder + "WeatherData_" + dim + "_BACKUP1.dat")));
				if ((new File(saveFolder + "WeatherData_" + dim + ".dat").exists())) FileUtils.copyFile((new File(saveFolder + "WeatherData_" + dim + ".dat")), (new File(saveFolder + "WeatherData_" + dim + "_BACKUP0.dat")));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Weather2.warn("Weather2 File: WeatherData.dat failed to load, automatically restoring to backup from previous game run");
			try
			{
				if ((new File(saveFolder + "WeatherData_" + dim + "_BACKUP0.dat")).exists())
					mainNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "WeatherData_" + dim + "_BACKUP0.dat"));
				else
					Weather2.warn("Failed to find backup file WeatherData_BACKUP0.dat, nothing loaded");
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Weather2.warn("Completely failed to find backup file WeatherData_BACKUP0.dat, nothing loaded");
			}
		}

		//prevent setting to 0 for worlds updating to new weather version
		if (mainNBT.hasKey("cloudIntensity"))
			cloudIntensity = mainNBT.getFloat("cloudIntensity");
		if (mainNBT.hasKey("ticksFrontFormed"))
			ticksFrontFormed = mainNBT.getLong("ticksFrontFormed");
		if (mainNBT.hasKey("ticksSandstormFormed"))
			ticksSandstormFormed = mainNBT.getLong("ticksSandstormFormed");
		if (mainNBT.hasKey("ticksStormFormed"))
			ticksStormFormed = mainNBT.getLong("ticksStormFormed");
		
		windManager.readFromNBT(mainNBT.getCompoundTag("windMan"));
		NBTTagCompound volcanosNBT = mainNBT.getCompoundTag("volcanoData");
		
		volcanosNBT.getKeySet().forEach(name -> {
			NBTTagCompound nbt = volcanosNBT.getCompoundTag(name);
			VolcanoObject vo = new VolcanoObject(this);
			
			try
			{
				vo.readFromNBT(nbt);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			
			addVolcanoObject(vo);
			
			//THIS LINE NEEDS REFINING FOR PLAYERS WHO JOIN AFTER THE FACT!!!
			PacketVolcanoObject.create(dim, vo);
		});
		
		NBTTagCompound frontNBT = mainNBT.getCompoundTag("frontData");
		frontNBT.getKeySet().forEach(name ->
		{
			NBTTagCompound nbt = frontNBT.getCompoundTag(name);
			Weather2.debug("Front name: " + name);
			if (name.equals("front_global"))
			{
				globalFront = new FrontObject(this, null, 0);
				globalFront.readNBT(nbt);
				fronts.put(globalFront.getUUID(), globalFront);

				Weather2.debug("Loaded global front " + globalFront.getUUID().toString());
			}
			else
			{
				FrontObject front = new FrontObject(this, new Vec3(nbt.getInteger("layer"), nbt.getDouble("posX"), nbt.getDouble("posZ")), nbt.getInteger("layer"));
				front.readNBT(nbt);
				fronts.put(front.getUUID(), front);
				Weather2.debug("Loaded front " + front.getUUID().toString());
			}
		});
		
		if (globalFront == null)
		{
			globalFront = new FrontObject(this, null, 0);
			fronts.put(globalFront.getUUID(), globalFront);	
			Weather2.debug("Created a new global front " + globalFront.getUUID().toString());
		}
		
		NBTTagCompound weatherNBT = mainNBT.getCompoundTag("stormData");
		weatherNBT.getKeySet().forEach(name ->
		{
			NBTTagCompound nbt = weatherNBT.getCompoundTag(name);
			FrontObject front = getFront(nbt.getUniqueId("frontUUID"));
			WeatherObject wo = null;
			
			if (front == null) front = globalFront;
			switch (nbt.getInteger("weatherObjectType"))
			{
				case 0: //Cloud Type
					wo = new StormObject(front);
					break;
				case 1: //Sand Type
					wo = new SandstormObject(this);
					break;
				default:
					Weather2.warn("Non-existant type attempted to load into the weather system. Skipping...");
			}
			
			if (wo != null)
			{
				try
				{
					wo.nbt.setNewNBT(nbt);
					wo.nbt.updateCacheFromNew();
					wo.readFromNBT();
					front.addWeatherObject(wo);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				Weather2.debug("Loaded storm " + wo.getUUID().toString());
			}
		});
		Weather2.debug("Loading Weather2 data successful!");
	}
	
	public boolean spawnSandstorm(Vec3 posIn)
	{
		/**
		 * 1. Start upwind
		 * 2. Find random spot near there loaded and in desert
		 * 3. scan upwind and downwind, require a good stretch of sand for a storm
		 */
		int searchRadius = 512;
		double angle = windManager.windAngle;
		
		//-1 for upwind
		double dirX = -Math.sin(Math.toRadians(angle));
		double dirZ = Math.cos(Math.toRadians(angle));
		double vecX = dirX * searchRadius/2 * -1;
		double vecZ = dirZ * searchRadius/2 * -1;
		
		Random rand = new Random();
		
		BlockPos foundPos = null;
		
		int findTriesMax = 30;
		for (int i = 0; i < findTriesMax; i++) {
			
			int x = MathHelper.floor(posIn.posX + vecX + rand.nextInt(searchRadius * 2) - searchRadius);
			int z = MathHelper.floor(posIn.posZ + vecZ + rand.nextInt(searchRadius * 2) - searchRadius);
			
			BlockPos pos = new BlockPos(x, 0, z);
			
			if (!world.isBlockLoaded(pos)) continue;
			Biome biomeIn = world.getBiomeForCoordsBody(pos);
			
			if (SandstormObject.isDesert(biomeIn, true))
			{
				//found
				foundPos = pos;
				//break;
				
				//check left and right about 20 blocks, if its not still desert, force retry
				double dirXLeft = -Math.sin(Math.toRadians(angle-90));
				double dirZLeft = Math.cos(Math.toRadians(angle-90));
				double dirXRight = -Math.sin(Math.toRadians(angle+90));
				double dirZRight = Math.cos(Math.toRadians(angle+90));
				
				double distLeftRight = 20;
				BlockPos posLeft = new BlockPos(foundPos.getX() + (dirXLeft * distLeftRight), 0, foundPos.getZ() + (dirZLeft * distLeftRight));
				if (!world.isBlockLoaded(posLeft)) continue;
				if (!SandstormObject.isDesert(world.getBiomeForCoordsBody(posLeft))) continue;
				
				BlockPos posRight = new BlockPos(foundPos.getX() + (dirXRight * distLeftRight), 0, foundPos.getZ() + (dirZRight * distLeftRight));
				if (!world.isBlockLoaded(posRight)) continue;
				if (!SandstormObject.isDesert(world.getBiomeForCoordsBody(posRight))) continue;
				
				//go as far upwind as possible until no desert / unloaded area
				
				BlockPos posFind = new BlockPos(foundPos);
				BlockPos posFindLastGoodUpwind = new BlockPos(foundPos);
				BlockPos posFindLastGoodDownwind = new BlockPos(foundPos);
				double tickDist = 10;
				
				while (world.isBlockLoaded(posFind) && SandstormObject.isDesert(world.getBiomeForCoordsBody(posFind))) {
					//update last good
					posFindLastGoodUpwind = new BlockPos(posFind);
					
					//scan against wind (upwind)
					int xx = MathHelper.floor(posFind.getX() + (dirX * -1D * tickDist));
					int zz = MathHelper.floor(posFind.getZ() + (dirZ * -1D * tickDist));
					
					posFind = new BlockPos(xx, 0, zz);
				}
				
				//reset for downwind scan
				posFind = new BlockPos(foundPos);
				
				while (world.isBlockLoaded(posFind) && SandstormObject.isDesert(world.getBiomeForCoordsBody(posFind))) {
					//update last good
					posFindLastGoodDownwind = new BlockPos(posFind);
					
					//scan with wind (downwind)
					int xx = MathHelper.floor(posFind.getX() + (dirX * 1D * tickDist));
					int zz = MathHelper.floor(posFind.getZ() + (dirZ * 1D * tickDist));
					
					posFind = new BlockPos(xx, 0, zz);
				}
				
				int minDistanceOfDesertStretchNeeded = 200;
				double dist = posFindLastGoodUpwind.getDistance(posFindLastGoodDownwind.getX(), posFindLastGoodDownwind.getY(), posFindLastGoodDownwind.getZ());
				
				if (dist >= minDistanceOfDesertStretchNeeded) {
					
					SandstormObject sandstorm = new SandstormObject(this);
					
					sandstorm.init();
					BlockPos posSpawn = new BlockPos(WeatherUtilBlock.getPrecipitationHeightSafe(world, posFindLastGoodUpwind)).add(0, 1, 0);
					sandstorm.initSandstormSpawn(new Vec3(posSpawn));
					globalFront.addWeatherObject(sandstorm);
					PacketWeatherObject.create(dim, sandstorm);
					
					Weather2.debug("found decent spot and stretch for sandstorm, stretch: " + dist);
					return true;
				}
			}
		}

		Weather2.debug("couldnt spawn sandstorm");
		return false;
	}
	
	public void playerJoinedWorldSyncFull(EntityPlayerMP entP)
	{
		Weather2.debug("Weather2: playerJoinedWorldSyncFull for dim: " + dim);
		
		//sync storms
		fronts.forEach((uuid, front) -> {PacketFrontObject.create(entP, front); front.getWeatherObjects().forEach(wo -> PacketWeatherObject.create(entP, wo));});
					
		//sync volcanos
		volcanoObjects.forEach(vo -> PacketVolcanoObject.create(entP, vo));
	}
	
	//populate data with rain storms and deadly storms
	public void nbtStormsForIMC()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		List<WeatherObject> list = getWeatherObjects();
		for (WeatherObject wo : list)
			if (wo instanceof IWeatherRain && ((IWeatherRain)wo).hasDownfall() || wo instanceof IWeatherStaged && ((IWeatherStaged)wo).getStage() > 0)
			{
				wo.writeToNBT();
				nbt.setTag("storm_" + wo.getUUID().toString(), wo.nbt.getNewNBT());
			}
		
		if (!nbt.isEmpty())
			FMLInterModComms.sendRuntimeMessage(Weather2.instance, Weather2.MODID, "weather.storms", nbt);
	}
	
	protected boolean canSpawnWeather(int type)
	{
		if (!WeatherUtilConfig.isWeatherEnabled(world.provider.getDimension())) return false;
		long ticks;
		
		switch(type)
		{
			case 0:
				ticks = ticksFrontFormed - world.getTotalWorldTime();
				if (ticks > ConfigStorm.storm_spawn_delay)
					ticksFrontFormed = world.getTotalWorldTime() + ConfigStorm.storm_spawn_delay;
				return (!ConfigStorm.disable_tornados || !ConfigStorm.disable_cyclones) && ticksFrontFormed < world.getTotalWorldTime() && fronts.size() - 1 < ConfigFront.max_front_objects;
			case 1:
				ticks = ticksStormFormed - world.getTotalWorldTime();
				if (ticks > ConfigStorm.storm_spawn_delay)
					ticksStormFormed = world.getTotalWorldTime() + ConfigStorm.storm_spawn_delay;
				return (!ConfigStorm.disable_tornados || !ConfigStorm.disable_cyclones) && ticksStormFormed < world.getTotalWorldTime() && systems.size() < ConfigStorm.max_weather_objects;
			case 2:
				ticks = ticksSandstormFormed - world.getTotalWorldTime();
				if (ticks > ConfigSand.sandstorm_spawn_delay)
					ticksSandstormFormed = world.getTotalWorldTime() + ConfigSand.sandstorm_spawn_delay;
				return !ConfigSand.disable_sandstorms && ticksSandstormFormed < world.getTotalWorldTime() && systems.size() < ConfigStorm.max_weather_objects;
			default:
				return false;
		}
	}
}
