package net.mrbt0907.weather2.server.weather;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import net.mrbt0907.weather2.api.IWeatherRain;
import net.mrbt0907.weather2.api.IWeatherStages;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.config.ConfigSand;
import net.mrbt0907.weather2.config.ConfigSimulation;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.network.packets.PacketVanillaWeather;
import net.mrbt0907.weather2.network.packets.PacketVolcanoObject;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.network.packets.PacketWind;
import net.mrbt0907.weather2.player.PlayerData;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilConfig;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.WeatherSystem;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilFile;
import CoroUtil.util.Vec3;

public class WeatherSystemServer extends WeatherSystem {

	//storm logic, syncing to client
	public int syncRange = 256;
	public long ticksSandstormFormed = 0;
	public long ticksStormFormed = 0;

	public WeatherSystemServer(World world)
	{
		super(world);
	}
	
	@Override
	public World getWorld() {
		return DimensionManager.getWorld(dim);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		if (world != null)
		{
			long ticks = world.getTotalWorldTime();
			
			tickWeatherCoverage(ticks);
			//sync storms
			weatherObjects.forEach(wo -> {if (ticks % wo.getNetRate() == 0) PacketWeatherObject.update(dim, wo);});
			
			//sync volcanos
			if (ticks % 40 == 0)
				volcanoObjects.forEach(vo -> PacketVolcanoObject.update(dim, vo));
			
			//sync wind and IMC
			if (ticks % 60 == 0)
			{
				PacketWind.update(dim, windMan);
				nbtStormsForIMC();
			}
			
			//sim box work
			if (ticks % 20 == 0)
			{
				//Remove Storms If Outside Sim Box
				
				WeatherObject wo;
				int size = weatherObjects.size();
				
				for (int i = 0; i < size; i++)
				{
					wo = weatherObjects.get(i);
					if (ConfigMisc.aesthetic_mode)
					{
						Weather2.debug("Removing storm as Aestetic Mode is active: " + wo.getUUID());
						PacketWeatherObject.remove(dim, wo);
						removeStormObject(wo.getUUID());
						i--;
						size = weatherObjects.size();
					}
					else
					{
						int count = world.playerEntities.size();
						if ((!ConfigMisc.remove_storms_if_no_players && count > 0) || true)
						{						
							EntityPlayer player = WeatherUtilEntity.getClosestPlayer(world, wo.posGround.xCoord, wo.posGround.yCoord, wo.posGround.zCoord, ConfigSimulation.max_storm_distance);
							if (player == null)
							{
								wo.ticksSinceNoNearPlayer += 20;
								//finally remove if nothing near for 30 seconds, gives multiplayer server a chance to get players in
								if (wo.ticksSinceNoNearPlayer > 600)
								{
									if (count == 0)
										Weather2.debug("Removing distant storm: " + wo.getUUID() + ", running without players");
									else
										Weather2.debug("Removing distant storm: " + wo.getUUID());
		
									PacketWeatherObject.remove(dim, wo);
									removeStormObject(wo.getUUID());
									i--;
									size = weatherObjects.size();
								}
							}
							else
								wo.ticksSinceNoNearPlayer = 0;
						}
					}
				}

				//cloud formation spawning - REFINE ME!
				if (!ConfigMisc.aesthetic_mode)
				{
					if (WeatherUtilConfig.isWeatherEnabled(dim))
						world.playerEntities.forEach(player -> weatherObjectsPerLayer.forEach((layer, weatherObjects) -> {if (ConfigStorm.isLayerValid(layer) && weatherObjects.size() < ConfigStorm.max_storms && Maths.chance(5)) spawnWeatherObject(player, layer);}));

					//if dimension can have storms, tick sandstorm spawning every 10 seconds
					if (ticks % 200 == 0 && !ConfigSand.disable_sandstorms && WeatherUtilConfig.isWeatherEnabled(dim) && windMan.isHighWindEventActive())
					{
						if (ConfigSand.sandstorm_spawn_1_in_x <= 0 || Maths.chance(ConfigSand.sandstorm_spawn_1_in_x))
						{
							if (ConfigSand.enable_global_rates_for_sandstorms)
							{
								//get a random player to try and spawn for, will recycle another if it cant spawn
								if (world.playerEntities.size() > 0 && ConfigSand.sandstorm_spawn_delay + ticksSandstormFormed < ticks)
								{
									EntityPlayer player = world.playerEntities.get(Maths.random(world.playerEntities.size()));
									spawnSandstorm(new Vec3(player.posX, player.posY, player.posZ));
								}
							}
							else
							{
								world.playerEntities.forEach(player ->
								{
									NBTTagCompound nbt = PlayerData.getPlayerNBT(CoroUtilEntity.getName(player));	
									
									if (ConfigSand.sandstorm_spawn_delay + nbt.getLong("ticksSandstormFormed") < ticks && spawnSandstorm(new Vec3(player.posX, player.posY, player.posZ)))
										nbt.setLong("ticksSandstormFormed", ticks);
								});
							}
						}
					}
				}
			}
		}
	}

	public void tickWeatherCoverage(long ticks)
	{
		WorldInfo worldInfo = world.getWorldInfo();
		boolean isRaining = worldInfo.isRaining();
		boolean isThundering = worldInfo.isThundering();
		if (!ConfigMisc.overcast_mode && ConfigMisc.server_weather_mode != -1)
		{
			worldInfo.setRaining(ConfigMisc.server_weather_mode == 1);
			worldInfo.setThundering(ConfigMisc.server_weather_mode == 1);
		}
			
		if (isThundering && ConfigStorm.prevent_vanilla_thunderstorms)
			worldInfo.setThundering(false);

			
		if (ticks % 40 == 0)
			PacketVanillaWeather.send(dim, isRaining ? isThundering ? 2 : 1 : 0, getWorld().getWorldInfo().getRainTime());

		//tick partial cloud cover variation0
		if (ticks % 200 == 0)
			cloudIntensity = ConfigMisc.overcast_mode && isRaining ? 1.0F : cloudIntensity + (float) MathHelper.clamp(Maths.random(ConfigParticle.cloud_coverage_change_amount) - Maths.random(ConfigParticle.cloud_coverage_change_amount), ConfigParticle.min_cloud_coverage_perc * 0.01D, ConfigParticle.max_cloud_coverage_perc * 0.01D);
				//force full cloudIntensity if server side raining
				//note: storms also revert to clouded storms for same condition
	}

	public void writeToFile() {
		Weather2.debug("Saving weather2 data...");
		NBTTagCompound mainNBT = new NBTTagCompound();
		NBTTagCompound volcanoesNBT = new NBTTagCompound();
		volcanoObjects.forEach(vo -> {
			NBTTagCompound nbt = new NBTTagCompound();
			vo.writeToNBT(nbt);
			volcanoesNBT.setTag("volcano_" + vo.ID, nbt);
		});
		mainNBT.setTag("volcanoData", volcanoesNBT);
		
		NBTTagCompound weatherNBT = new NBTTagCompound();
		weatherObjects.forEach(wo -> {
			wo.nbt.setUpdateForced(true);
			wo.writeToNBT();
			wo.nbt.setUpdateForced(false);
			weatherNBT.setTag("storm_" + wo.getUUID().toString(), wo.nbt.getNewNBT());
			Weather2.debug("Saving Storm to storm_" + wo.getUUID().toString() + "...");
		});
		mainNBT.setTag("stormData", weatherNBT);
		mainNBT.setFloat("cloudIntensity", this.cloudIntensity);
		mainNBT.setTag("windMan", windMan.writeToNBT(new NBTTagCompound()));
		
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
		
		windMan.readFromNBT(mainNBT.getCompoundTag("windMan"));
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
		
		NBTTagCompound nbtStorms = mainNBT.getCompoundTag("stormData");
		
		nbtStorms.getKeySet().forEach(name -> {
			NBTTagCompound nbt = nbtStorms.getCompoundTag(name);
			WeatherObject wo = null;
			switch (nbt.getInteger("weatherObjectType"))
			{
				case 0: //Cloud Type
					wo = new StormObject(this/*-1, -1, null*/);
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
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				addStormObject(wo);
				
				//TODO: possibly unneeded/redundant/bug inducing, packets will be sent upon request from client
				PacketWeatherObject.create(dim, wo);
				Weather2.debug("Loaded Storm " + wo.getUUID().toString());
			}
		});
		Weather2.debug("Loading weather2 data successful!");
	}
	
	public boolean spawnSandstorm(Vec3 posIn) {
		/**
		 * 1. Start upwind
		 * 2. Find random spot near there loaded and in desert
		 * 3. scan upwind and downwind, require a good stretch of sand for a storm
		 */
		
		int searchRadius = 512;
		
		double angle = windMan.getWindAngleForClouds();
		//-1 for upwind
		double dirX = -Math.sin(Math.toRadians(angle));
		double dirZ = Math.cos(Math.toRadians(angle));
		double vecX = dirX * searchRadius/2 * -1;
		double vecZ = dirZ * searchRadius/2 * -1;
		
		Random rand = new Random();
		
		BlockPos foundPos = null;
		
		int findTriesMax = 30;
		for (int i = 0; i < findTriesMax; i++) {
			
			int x = MathHelper.floor(posIn.xCoord + vecX + rand.nextInt(searchRadius * 2) - searchRadius);
			int z = MathHelper.floor(posIn.zCoord + vecZ + rand.nextInt(searchRadius * 2) - searchRadius);
			
			BlockPos pos = new BlockPos(x, 0, z);
			
			if (!world.isBlockLoaded(pos)) continue;
			Biome biomeIn = world.getBiomeForCoordsBody(pos);
			
			if (SandstormObject.isDesert(biomeIn, true)) {
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
					addStormObject(sandstorm);
					PacketWeatherObject.create(dim, sandstorm);
					ticksSandstormFormed = world.getTotalWorldTime();
					
					Weather2.debug("found decent spot and stretch for sandstorm, stretch: " + dist);
					return true;
				}
			}
		}

		Weather2.debug("couldnt spawn sandstorm");
		return false;
	}
	
	public void spawnWeatherObject(EntityPlayer player, int layer)
	{
		int tryCountMax = 10;
		int tryCountCur = 0;
		int spawnX = -1;
		int spawnZ = -1;
		Vec3 tryPos = null;
		WeatherObject soClose = null;
		EntityPlayer playerClose = null;
		int closestToPlayer = 128;
		
		//use 256 or the cutoff val if its configured small
		float windOffsetDist = Math.min(256, ConfigSimulation.max_storm_distance / 4 * 3);
		double angle = windMan.getWindAngleForClouds();
		double vecX = -Math.sin(Math.toRadians(angle)) * windOffsetDist;
		double vecZ = Math.cos(Math.toRadians(angle)) * windOffsetDist;
		
		while (tryCountCur++ == 0 || (tryCountCur < tryCountMax && (soClose != null || playerClose != null))) {
			spawnX = (int) (player.posX - vecX + Maths.random(ConfigSimulation.max_storm_spawning_distance) - Maths.random(ConfigSimulation.max_storm_spawning_distance));
			spawnZ = (int) (player.posZ - vecZ + Maths.random(ConfigSimulation.max_storm_spawning_distance) - Maths.random(ConfigSimulation.max_storm_spawning_distance));
			tryPos = new Vec3(spawnX, StormObject.layers.get(layer), spawnZ);
			soClose = getClosestSpecificStorm(tryPos, ConfigParticle.min_cloud_distance);
			playerClose = player.world.getClosestPlayer(spawnX, 50, spawnZ, closestToPlayer, false);
		}
		
		if (soClose == null)
		{
			StormObject so = new StormObject(this);
			so.pos = tryPos;
			so.layer = layer;
			//make only layer 0 produce deadly storms
			if (layer != 0)
				so.canBeDeadly = false;
			so.player = CoroUtilEntity.getName(player);
			if (Maths.random(1.0F) >= cloudIntensity)
				so.isCloudless = true;
			addStormObject(so);
			PacketWeatherObject.create(dim, so);
		}
	}
	
	public void playerJoinedWorldSyncFull(EntityPlayerMP entP) {
		Weather2.debug("Weather2: playerJoinedWorldSyncFull for dim: " + dim);
		//sync storms
		weatherObjects.forEach(wo -> PacketWeatherObject.create(entP, wo));
					
		//sync volcanos
		volcanoObjects.forEach(vo -> PacketVolcanoObject.create(entP, vo));
	}
	
	//populate data with rain storms and deadly storms
	public void nbtStormsForIMC() {
		NBTTagCompound nbt = new NBTTagCompound();
		for (WeatherObject wo : weatherObjects)
			if (wo instanceof IWeatherRain && ((IWeatherRain)wo).isRaining() || wo instanceof IWeatherStages && ((IWeatherStages)wo).getStage() > 0)
			{
				wo.writeNBT();
				nbt.setTag("storm_" + wo.getUUID().toString(), wo.nbt.getNewNBT());
			}
		
		if (!nbt.hasNoTags())
			FMLInterModComms.sendRuntimeMessage(Weather2.instance, Weather2.MODID, "weather.storms", nbt);
	}
}
