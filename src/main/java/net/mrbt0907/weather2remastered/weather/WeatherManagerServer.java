<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/weather/WeatherManagerServer.java
package net.mrbt0907.weather2remastered.weather;

import java.io.File;
=======
package net.mrbt0907.weather2.weather;

import java.io.File;
import java.io.FileInputStream;
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/weather/WeatherManagerServer.java
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/weather/WeatherManagerServer.java
=======
import java.util.UUID;
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/weather/WeatherManagerServer.java

import org.apache.commons.io.FileUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/weather/WeatherManagerServer.java
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
=======
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/weather/WeatherManagerServer.java
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/weather/WeatherManagerServer.java
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.weather.AbstractFrontObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractStormObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherManager;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherObject;
import net.mrbt0907.weather2remastered.api.weather.IWeatherRain;
import net.mrbt0907.weather2remastered.api.weather.IWeatherStaged;
import net.mrbt0907.weather2remastered.config.ConfigClient;
import net.mrbt0907.weather2remastered.config.ConfigFront;
import net.mrbt0907.weather2remastered.config.ConfigMisc;
import net.mrbt0907.weather2remastered.config.ConfigSand;
import net.mrbt0907.weather2remastered.config.ConfigSimulation;
import net.mrbt0907.weather2remastered.config.ConfigStorm;
import net.mrbt0907.weather2remastered.gui.EZConfigParser;
import net.mrbt0907.weather2remastered.network.PacketBase;
import net.mrbt0907.weather2remastered.network.PacketFrontObject;
import net.mrbt0907.weather2remastered.network.PacketVanillaWeather;
import net.mrbt0907.weather2remastered.network.PacketWeatherObject;
import net.mrbt0907.weather2remastered.network.PacketWind;
import net.mrbt0907.weather2remastered.util.Maths;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;
import net.mrbt0907.weather2remastered.util.WeatherUtilEntity;
import net.mrbt0907.weather2remastered.util.coro.CoroFile;
import net.mrbt0907.weather2remastered.util.fartsy.FartsyUtil;

import java.io.FileInputStream;
public class WeatherManagerServer extends AbstractWeatherManager
{
	private long ticksFrontFormed = 0L;
	private long ticksSandstormFormed = 0L;
	private long ticksStormFormed = 0L;
	public static int stormChanceToday = 10;
	
	public WeatherManagerServer(ServerWorld world)
	{
		super(world);
	}
	
	@Override
	public World getWorld() {
	    return FMLEnvironment.dist.isClient() ? getClientWorldSafe() : getServerWorldSafe();
	}

	@OnlyIn(Dist.CLIENT)
	private World getClientWorldSafe() {
	    net.minecraft.world.World clientWorld = net.minecraft.client.Minecraft.getInstance().level;
	    if (clientWorld != null && clientWorld.dimension().location().toString().equals(dimension)) {
	        return clientWorld;
	    }
	    return null;
	}

	private World getServerWorldSafe() {
	    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
	    if (server != null) {
	        return server.getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("minecraft:overworld")));
	    }
	    return null;
	}
	
	@Override
	public void tick(boolean isClientTick)
	{
		super.tick(false);
		if (getWorld() != null)
		{
			tickWeatherCoverage(ticks);

			//sync storms
			AbstractFrontObject front;
			AbstractWeatherObject system;
			
			//Get storm chance for today
			if(getWorld().getDayTime() % 24000 == 1) stormChanceToday = Maths.random(ConfigStorm.storm_spawn_chance_min, ConfigStorm.storm_spawn_chance_max);
			List<AbstractFrontObject> fronts = new ArrayList<AbstractFrontObject>(this.fronts.values());
			List<AbstractWeatherObject> systems = getWeatherObjects();
			AbstractWeatherObject spawn = null;
			boolean spawned = false, spawnInFront = Maths.chance(ConfigFront.chance_to_spawn_storm_in_front * 0.01D);
			
			for (int i = 0; i < fronts.size(); i++)
			{
				front = fronts.get(i);
				
				if (front.isDead)
				{
					
					if (front.isGlobal())
					{
						front.reset();
						systems.forEach(weather -> PacketWeatherObject.remove(dimension, weather));
						systems.clear();
						front.isDead = false;
					}
					else
					{
						PacketFrontObject.remove(dimension, front);
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
							PacketWeatherObject.create(dimension, spawn);
						}
					}
					if (ticks % 40 == 0)
						PacketFrontObject.update(dimension, front);
				}
			}
			
			if (spawned)
				ticksStormFormed = getWorld().getGameTime() + ConfigStorm.storm_spawn_delay;
			for (int i = 0; i < systems.size(); i++)
			{
				
				system = systems.get(i);
				
				if (ticks % 20 == 0)
				{
					if (ConfigMisc.remove_storms_if_no_players && getWorld().players().size() == 0 || WeatherUtilEntity.getClosestPlayer(getWorld(), system.posGround.posX, system.posGround.posY, system.posGround.posZ, ConfigSimulation.max_storm_distance) == null) {
						system.ticksSinceNoNearPlayer += 20;
						//System.out.println((ConfigMisc.remove_storms_if_no_players && getWorld().players().size() == 0) + " || " + (WeatherUtilEntity.getClosestPlayer(getWorld(), system.posGround.posX, system.posGround.posY, system.posGround.posZ, ConfigSimulation.max_storm_distance) == null));
					}
					
					else
						system.ticksSinceNoNearPlayer = 0;
				}
				
				if (system.isDead || system.ticksSinceNoNearPlayer > 600 || ConfigMisc.aesthetic_mode)
				{
					System.out.println("Removing weather object " + system.getUUID() + " because isDead: " + system.isDead + " or noNearPlayerTicks " + system.ticksSinceNoNearPlayer + " > 600");
					PacketWeatherObject.remove(dimension, system);
					system.front.removeWeatherObject(system.getUUID());
				}
				else if (ticks % system.getNetRate() == 0)
					PacketWeatherObject.update(dimension, system);
			}
			
			//sync volcanos
			//if (ticks % 40 == 0)
			//	volcanoObjects.forEach(vo -> PacketVolcanoObject.update(dim, vo));
			
			//sync wind and IMC
			if (ticks % 60 == 0)
			{
				PacketWind.update(dimension, windManager);
				nbtStormsForIMC();
			}

			//cloud formation spawning - REFINE ME!
			if (!ConfigMisc.aesthetic_mode)
			{
				if (EZConfigParser.isWeatherEnabled(dimension) && getWorld().getGameTime() % ConfigStorm.spawningTickRate == 0)
				{
					List<ServerPlayerEntity> players = ((ServerWorld) world).players(); //Is this okay considering we can only have multiple players if it's a server???
					int layer, frontCount = fronts.size() + 1;
					
					for (ServerPlayerEntity player : players)
					{
						layer = Maths.random(2);
						if (canSpawnWeather(0) && ConfigStorm.isLayerValid(layer))
						{
							ticksFrontFormed = getWorld().getGameTime() + ConfigStorm.storm_spawn_delay;
							PacketFrontObject.create(dimension, createNaturalFront(layer, player));
							
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
									PacketWeatherObject.create(dimension, spawn);
								}
							}
					}
					
					if (!spawnInFront && spawned)
						ticksStormFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;
					
					if (canSpawnWeather(2))
					{
						if (world.players().size() > 0)
						{
							PlayerEntity player = world.players().get(Maths.random(world.players().size() - 1));
							ticksSandstormFormed = world.getGameTime() + ConfigSand.sandstorm_spawn_delay;
							spawnSandstorm(new Vec3(player.blockPosition()));
						}
					}
				}
			}
		}
	}

	public void tickWeatherCoverage(long ticks)
	{
		boolean isRaining = world.isRaining();
		boolean isThundering = world.isThundering();
		if (EZConfigParser.isWeatherEnabled(dimension))
		{
			if (!ConfigMisc.overcast_mode && ConfigMisc.server_weather_mode != -1)
			{
				((ServerWorld)world).getLevelData().setRaining(ConfigMisc.server_weather_mode == 1);
				CompoundNBT thunder = new CompoundNBT();
				thunder.putFloat("setThunderLevel", 1.0F);
				PacketBase.send(20, thunder);
			}
				
			if (isThundering && ConfigStorm.prevent_vanilla_thunderstorms) {
				CompoundNBT thunder = new CompoundNBT();
				thunder.putFloat("setThunderLevel", 0.0F);
				PacketBase.send(20, thunder);
			}
		}
			
		if (ticks % 40 == 0) {
			int rainTime = ((IServerWorldInfo)world.getLevelData()).getRainTime();
			PacketVanillaWeather.send(dimension, isRaining ? isThundering ? 2 : 1 : 0, rainTime);
		}
		//tick partial cloud cover variation0
		if (ticks % 200 == 0)
			cloudIntensity = ConfigMisc.overcast_mode && isRaining ? 1.0F : cloudIntensity + (float) Maths.clamp(Maths.random(ConfigClient.cloud_coverage_change_amount) - Maths.random(ConfigClient.cloud_coverage_change_amount), ConfigClient.min_cloud_coverage_perc * 0.01D, ConfigClient.max_cloud_coverage_perc * 0.01D);
				//force full cloudIntensity if server side raining
				//note: storms also revert to clouded storms for same condition
	}

	public void writeToFile()
	{
		Weather2Remastered.debug("Saving weather2 data...");
		CompoundNBT mainNBT = new CompoundNBT();
		/*NBTTagCompound volcanoesNBT = new NBTTagCompound();
		volcanoObjects.forEach(vo -> {
			NBTTagCompound nbt = new NBTTagCompound();
			vo.writeToNBT(nbt);
			volcanoesNBT.setTag("volcano_" + vo.ID, nbt);
		});
		mainNBT.setTag("volcanoData", volcanoesNBT);
		*/
		CompoundNBT frontNBT = new CompoundNBT();
		CompoundNBT weatherNBT = new CompoundNBT();
		frontNBT.put("front_global", globalFront.writeNBT());
		globalFront.getWeatherObjects().forEach(weatherObject ->
			{
				weatherObject.nbt.setUpdateForced(true);
				weatherObject.nbt.setUUID("frontUUID", globalFront.getUUID());
				weatherNBT.put("storm_" + weatherObject.getUUID().toString(), weatherObject.writeToNBT().getNewNBT());
				weatherObject.nbt.setUpdateForced(false);
				Weather2Remastered.debug("Saved storm_" + weatherObject.getUUID().toString());
			}
		);
		Weather2Remastered.debug("Saved front_global");
		
		fronts.forEach((uuid, front) ->
			{
				if (!front.equals(globalFront))
				{
					frontNBT.put("front_" + uuid.toString(), front.writeNBT());
					front.getWeatherObjects().forEach(weatherObject ->
						{
							weatherObject.nbt.setUpdateForced(true);
							weatherObject.nbt.setUUID("frontUUID", uuid);
							weatherNBT.put("storm_" + weatherObject.getUUID().toString(), weatherObject.writeToNBT().getNewNBT());
							weatherObject.nbt.setUpdateForced(false);
							Weather2Remastered.debug("Saved storm_" + weatherObject.getUUID().toString());
						}
					);
					Weather2Remastered.debug("Saved front_" + uuid.toString());
				}
			}
		);
		
		mainNBT.put("frontData", frontNBT);
		mainNBT.put("stormData", weatherNBT);
		mainNBT.putFloat("cloudIntensity", cloudIntensity);
		mainNBT.putFloat("ticksFrontFormed", ticksFrontFormed);
		mainNBT.putFloat("ticksSandstormFormed", ticksSandstormFormed);
		mainNBT.putFloat("ticksStormFormed", ticksStormFormed);
		mainNBT.put("windMan", windManager.writeToNBT(new CompoundNBT()));
		mainNBT.putInt("stormChanceToday", stormChanceToday);

		String saveFolder = CoroFile.getWorldSaveFolderPath() + CoroFile.getWorldFolderName(world) + "weather2" + File.separator;
		
		try
		{
			//Write out to file
			if (!(new File(saveFolder).exists())) new File(saveFolder).mkdirs();
			FileOutputStream fos = new FileOutputStream(saveFolder + "WeatherData_" + dimension.replace(":", "_") + ".dat");
			CompressedStreamTools.writeCompressed(mainNBT, fos);
			fos.close();
			Weather2Remastered.debug("Save successful!");
		}
		catch (Exception ex)
		{
			Weather2Remastered.debug("Save failed.");
			ex.printStackTrace();
		}
	}
	
	public void readFromFile()
	{
		Weather2Remastered.debug("Loading weather2 data...");
		CompoundNBT mainNBT = new CompoundNBT();
		String saveFolder = CoroFile.getWorldSaveFolderPath() + CoroFile.getWorldFolderName(world) + "weather2" + File.separator;

		try
		{
			if (new File(saveFolder + "WeatherData_" + dimension.replace(":", "_") + ".dat").exists())
			{
				mainNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "WeatherData_" + dimension.replace(":", "_") + ".dat"));
				File tmp = (new File(saveFolder + "WeatherData_" + dimension.replace(":", "_") + "_BACKUP0.dat"));
				if (tmp.exists()) FileUtils.copyFile(tmp, (new File(saveFolder + "WeatherData_" + dimension.replace(":", "_") + "_BACKUP1.dat")));
				if ((new File(saveFolder + "WeatherData_" + dimension.replace(":", "_") + ".dat").exists())) FileUtils.copyFile((new File(saveFolder + "WeatherData_" + dimension.replace(":", "_") + ".dat")), (new File(saveFolder + "WeatherData_" + dimension.replace(":", "_") + "_BACKUP0.dat")));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Weather2Remastered.warn("Weather2 File: WeatherData.dat failed to load, automatically restoring to backup from previous game run");
			try
			{
				if ((new File(saveFolder + "WeatherData_" + dimension.replace(":", "_") + "_BACKUP0.dat")).exists())
					mainNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "WeatherData_" + dimension.replace(":", "_") + "_BACKUP0.dat"));
				else
					Weather2Remastered.warn("Failed to find backup file WeatherData_BACKUP0.dat, nothing loaded");
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Weather2Remastered.warn("Completely failed to find backup file WeatherData_BACKUP0.dat, nothing loaded");
			}
		}

		//prevent setting to 0 for worlds updating to new weather version
		if (mainNBT.contains("cloudIntensity"))
			cloudIntensity = mainNBT.getFloat("cloudIntensity");
		if (mainNBT.contains("ticksFrontFormed"))
			ticksFrontFormed = mainNBT.getLong("ticksFrontFormed");
		if (mainNBT.contains("ticksSandstormFormed"))
			ticksSandstormFormed = mainNBT.getLong("ticksSandstormFormed");
		if (mainNBT.contains("ticksStormFormed"))
			ticksStormFormed = mainNBT.getLong("ticksStormFormed");
		if(mainNBT.contains("stormChanceToday"))
			stormChanceToday = mainNBT.getInt("stormChanceToday");

		windManager.readFromNBT(mainNBT.getCompound("windMan"));
		/*CompoundNBT volcanosNBT = mainNBT.getCompound("volcanoData");
		
		volcanosNBT.getAllKeys().forEach(name -> {
			CompoundNBT nbt = volcanosNBT.getCompound(name);
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
		});*/
		
		CompoundNBT frontNBT = mainNBT.getCompound("frontData");
		frontNBT.getAllKeys().forEach(name ->
		{
			CompoundNBT nbt = frontNBT.getCompound(name);
			Weather2Remastered.debug("Front name: " + name);
			if (name.equals("front_global"))
			{
				globalFront = new AbstractFrontObject(this, null, 0);
				globalFront.readNBT(nbt);
				fronts.put(globalFront.getUUID(), globalFront);

				Weather2Remastered.debug("Loaded global front " + globalFront.getUUID().toString());
				PacketFrontObject.create(world.dimension().location().toString(), globalFront);
			}
			else
			{
				AbstractFrontObject front = new AbstractFrontObject(this, new Vec3(nbt.getInt("layer"), nbt.getDouble("posX"), nbt.getDouble("posZ")), nbt.getInt("layer"));
				front.readNBT(nbt);
				fronts.put(front.getUUID(), front);
				Weather2Remastered.debug("Loaded front " + front.getUUID().toString());
				PacketFrontObject.create(world.dimension().location().toString(), front);
			}
		});
		
		if (globalFront == null)
		{
			globalFront = new AbstractFrontObject(this, null, 0);
			fronts.put(globalFront.getUUID(), globalFront);	
			Weather2Remastered.debug("Created a new global front " + globalFront.getUUID().toString());
			PacketFrontObject.create(world.dimension().location().toString(), globalFront);
		}
		
		CompoundNBT weatherNBT = mainNBT.getCompound("stormData");
		weatherNBT.getAllKeys().forEach(name ->
		{
			CompoundNBT nbt = weatherNBT.getCompound(name);
			AbstractFrontObject front = getFront(nbt.getUUID("frontUUID"));
			AbstractWeatherObject wo = null;
			
			if (front == null) front = globalFront;
			switch (nbt.getInt("weatherObjectType"))
			{
				case 0: //Cloud Type
					wo = new AbstractStormObject(front);
					break;
				case 1: //Sand Type
					//wo = new SandstormObject(this);
					Weather2Remastered.error("Can't make a new SandstormObject. Fartsy said no...");
					break;
				default:
					Weather2Remastered.warn("Non-existant type attempted to load into the weather system. Skipping...");
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
				
				Weather2Remastered.debug("Loaded storm " + wo.getUUID().toString());
				PacketWeatherObject.create(world.dimension(), wo);
			}
		});
		Weather2Remastered.debug("Loading Weather2 data successful!");
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
		double dirX = -Maths.fastSin(Math.toRadians(angle));
		double dirZ = Maths.fastCos(Math.toRadians(angle));
		double vecX = dirX * searchRadius/2 * -1;
		double vecZ = dirZ * searchRadius/2 * -1;
		
		Random rand = new Random();
		
		BlockPos foundPos = null;
		
		int findTriesMax = 30;
		for (int i = 0; i < findTriesMax; i++) {
			
			int x = MathHelper.floor(posIn.posX + vecX + rand.nextInt(searchRadius * 2) - searchRadius);
			int z = MathHelper.floor(posIn.posZ + vecZ + rand.nextInt(searchRadius * 2) - searchRadius);
			
			BlockPos pos = new BlockPos(x, 0, z);
			
			if (!world.isLoaded(pos)) continue;
			Biome biomeIn = world.getBiome(pos);
			
			/*if (SandstormObject.isDesert(biomeIn, true))
			{
				//found
				foundPos = pos;
				//break;
				
				//check left and right about 20 blocks, if its not still desert, force retry
				double dirXLeft = -Maths.fastSin(Math.toRadians(angle-90));
				double dirZLeft = Maths.fastCos(Math.toRadians(angle-90));
				double dirXRight = -Maths.fastSin(Math.toRadians(angle+90));
				double dirZRight = Maths.fastCos(Math.toRadians(angle+90));
				
				double distLeftRight = 20;
				BlockPos posLeft = new BlockPos(foundPos.getX() + (dirXLeft * distLeftRight), 0, foundPos.getZ() + (dirZLeft * distLeftRight));
				if (!world.isLoaded(posLeft)) continue;
				if (!SandstormObject.isDesert(world.getBiomeForCoordsBody(posLeft))) continue;
				
				BlockPos posRight = new BlockPos(foundPos.getX() + (dirXRight * distLeftRight), 0, foundPos.getZ() + (dirZRight * distLeftRight));
				if (!world.isLoaded(posRight)) continue;
				if (!SandstormObject.isDesert(world.getBiomeForCoordsBody(posRight))) continue;
				
				//go as far upwind as possible until no desert / unloaded area
				
				BlockPos posFind = new BlockPos(foundPos);
				BlockPos posFindLastGoodUpwind = new BlockPos(foundPos);
				BlockPos posFindLastGoodDownwind = new BlockPos(foundPos);
				double tickDist = 10;
				
				while (world.isLoaded(posFind) && SandstormObject.isDesert(world.getBiomeForCoordsBody(posFind))) {
					//update last good
					posFindLastGoodUpwind = new BlockPos(posFind);
					
					//scan against wind (upwind)
					int xx = MathHelper.floor(posFind.getX() + (dirX * -1D * tickDist));
					int zz = MathHelper.floor(posFind.getZ() + (dirZ * -1D * tickDist));
					
					posFind = new BlockPos(xx, 0, zz);
				}
				
				//reset for downwind scan
				posFind = new BlockPos(foundPos);
				
				while (world.isLoaded(posFind) && SandstormObject.isDesert(world.getBiomeForCoordsBody(posFind))) {
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
			}*/
		}

		Weather2Remastered.debug("couldnt spawn sandstorm");
		return false;
	}
	
	public void playerJoinedWorldSyncFull(ServerPlayerEntity entP)
	{
	//	Weather2Remastered.debug((entP == null ? "An unknown player " : "Player " + entP.getDisplayName().toString() + "'s client requested for a full sync"));
		//sync storms
		fronts.forEach((uuid, front) -> {PacketFrontObject.create(entP, front);
		front.getWeatherObjects().forEach(wo -> PacketWeatherObject.create(entP, wo));});
					
		//sync volcanos
		//volcanoObjects.forEach(vo -> PacketVolcanoObject.create(entP, vo));
		//Weather2Remastered.error("Can't sync volcanoobject because fartsy left them in the dust.");
	}
	
	//populate data with rain storms and deadly storms
	public void nbtStormsForIMC()
	{
		CompoundNBT nbt = new CompoundNBT();
		List<AbstractWeatherObject> list = getWeatherObjects();
		for (AbstractWeatherObject wo : list)
			if (wo instanceof IWeatherRain && ((IWeatherRain)wo).hasDownfall() || wo instanceof IWeatherStaged && ((IWeatherStaged)wo).getStage() > 0)
			{
				wo.writeToNBT();
				nbt.put("storm_" + wo.getUUID().toString(), wo.nbt.getNewNBT());
			}
		
		if (!nbt.isEmpty())
		{
			//PacketBase.send(20, nbt);
			InterModComms.sendTo(Weather2Remastered.MODID, "weather.storms", () -> nbt);
			//Weather2Remastered.debug("Attempting to send IMC weather.storms");
		}
	}
	
	protected boolean canSpawnWeather(int type)
	{
		//System.out.println("I am ticking and can spawn weather? " + !EZConfigParser.isWeatherEnabled(getWorld().dimension().location().toString()));
		if (!EZConfigParser.isWeatherEnabled(getWorld().dimension().location().toString())) return false;
		long ticks;
		switch(type)
		{
			case 0:
				ticks = ticksFrontFormed - getWorld().getGameTime();
				if (ticks > ConfigStorm.storm_spawn_delay)
					ticksFrontFormed = getWorld().getGameTime() + ConfigStorm.storm_spawn_delay;
				return (!ConfigStorm.disable_tornados || !ConfigStorm.disable_cyclones) && ticksFrontFormed < getWorld().getGameTime() && fronts.size() - 1 < ConfigFront.max_front_objects;
			case 1:
				ticks = ticksStormFormed - getWorld().getGameTime();
				if (ticks > ConfigStorm.storm_spawn_delay)
					ticksStormFormed = getWorld().getGameTime() + ConfigStorm.storm_spawn_delay;
				return (!ConfigStorm.disable_tornados || !ConfigStorm.disable_cyclones) && ticksStormFormed < getWorld().getGameTime() && systems.size() < ConfigStorm.max_weather_objects;
			case 2:
				/*ticks = ticksSandstormFormed - world.getTotalWorldTime();
				if (ticks > ConfigSand.sandstorm_spawn_delay)
					ticksSandstormFormed = world.getTotalWorldTime() + ConfigSand.sandstorm_spawn_delay;
				return !ConfigSand.disable_sandstorms && ticksSandstormFormed < world.getTotalWorldTime() && systems.size() < ConfigStorm.max_weather_objects;*/
				//Weather2Remastered.error("sandstorm dont exist, no darude for you -Fartsy");
				return false;
			default:
				return false;
		}
	}
}
=======
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.IWeatherStaged;
import net.mrbt0907.weather2.config.ConfigFront;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigSand;
import net.mrbt0907.weather2.config.ConfigSimulation;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.network.packets.PacketFrontObject;
import net.mrbt0907.weather2.network.packets.PacketVanillaWeather;
import net.mrbt0907.weather2.network.packets.PacketVolcanoObject;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.network.packets.PacketWind;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;
import net.CoroUtil.util.CoroUtilFile;

public class WeatherManagerServer extends WeatherManager
{
    private long ticksFrontFormed = 0L;
    private long ticksSandstormFormed = 0L;
    private long ticksStormFormed = 0L;
    public static int stormChanceToday = 10;

    public WeatherManagerServer(World world)
    {
        super(world);
    }

    @Override
    public World getWorld()
    {
        return ServerLifecycleHooks.getCurrentServer().getLevel(dim);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (world != null)
        {
            tickWeatherCoverage(ticks);


            FrontObject front;
            WeatherObject system;


            if(world.getDayTime() % 24000 == 1)
                stormChanceToday = Maths.random(ConfigStorm.storm_spawn_chance_min, ConfigStorm.storm_spawn_chance_max);

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
                        systems.forEach(weather -> PacketWeatherObject.remove(dim, weather));
                        systems.clear();
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
                ticksStormFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;

            for (int i = 0; i < systems.size(); i++)
            {
                system = systems.get(i);

                if (ticks % 20 == 0)
                {
                    if (ConfigMisc.remove_storms_if_no_players && world.players().size() == 0 || WeatherUtilEntity.getClosestPlayer(world, system.posGround.posX, system.posGround.posY, system.posGround.posZ, ConfigSimulation.max_storm_distance) == null)
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


            if (ticks % 40 == 0)
                volcanoObjects.forEach(vo -> PacketVolcanoObject.update(dim, vo));


            if (ticks % 60 == 0)
            {
                PacketWind.update(dim, windManager);
                nbtStormsForIMC();
            }


            if (!ConfigMisc.aesthetic_mode)
            {
                if (EZConfigParser.isWeatherEnabled(world.dimension().location().toString()) && world.getGameTime() % ConfigStorm.spawningTickRate == 0)
                {
                    List<PlayerEntity> players = new ArrayList<>(world.players());
                    int layer, frontCount = fronts.size() + 1;

                    for (PlayerEntity player : players)
                    {
                        layer = Maths.random(2);
                        if (canSpawnWeather(0) && ConfigStorm.isLayerValid(layer))
                        {
                            ticksFrontFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;
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
                        ticksStormFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;

                    if (canSpawnWeather(2))
                    {
                        if (world.players().size() > 0)
                        {
                            PlayerEntity player = world.players().get(Maths.random(world.players().size() - 1));
                            ticksSandstormFormed = world.getGameTime() + ConfigSand.sandstorm_spawn_delay;
                            spawnSandstorm(new Vec3(player.getX(), player.getY(), player.getZ()));
                        }
                    }
                }
            }
        }
    }

    public void tickWeatherCoverage(long ticks)
    {
        IServerWorldInfo worldInfo = (IServerWorldInfo) world.getLevelData();
        boolean isRaining = worldInfo.isRaining();
        boolean isThundering = worldInfo.isThundering();

        if (EZConfigParser.isWeatherEnabled(world.dimension().location().toString()))
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
            PacketVanillaWeather.send(dim, isRaining ? isThundering ? 2 : 1 : 0, worldInfo.getRainTime());


        if (ticks % 200 == 0)
            cloudIntensity = ConfigMisc.overcast_mode && isRaining ? 1.0F : cloudIntensity + (float) Maths.clamp(Maths.random(ConfigClient.cloud_coverage_change_amount) - Maths.random(ConfigClient.cloud_coverage_change_amount), ConfigClient.min_cloud_coverage_perc * 0.01D, ConfigClient.max_cloud_coverage_perc * 0.01D);


    }

    public void writeToFile()
    {
        Weather2.debug("Saving weather2 data...");
        CompoundNBT mainNBT = new CompoundNBT();
        CompoundNBT volcanoesNBT = new CompoundNBT();
        volcanoObjects.forEach(vo -> {
            CompoundNBT nbt = new CompoundNBT();
            vo.writeToNBT(nbt);
            volcanoesNBT.put("volcano_" + vo.ID, nbt);
        });
        mainNBT.put("volcanoData", volcanoesNBT);

        CompoundNBT frontNBT = new CompoundNBT();
        CompoundNBT weatherNBT = new CompoundNBT();
        frontNBT.put("front_global", globalFront.writeNBT());
        globalFront.getWeatherObjects().forEach(weatherObject ->
                {
                    weatherObject.nbt.setUpdateForced(true);
                    weatherObject.nbt.setUUID("frontUUID", globalFront.getUUID());
                    weatherNBT.put("storm_" + weatherObject.getUUID().toString(), weatherObject.writeToNBT().getNewNBT());
                    weatherObject.nbt.setUpdateForced(false);
                    Weather2.debug("Saved storm_" + weatherObject.getUUID().toString());
                }
        );
        Weather2.debug("Saved front_global");

        fronts.forEach((uuid, front) ->
                {
                    if (!front.equals(globalFront))
                    {
                        frontNBT.put("front_" + uuid.toString(), front.writeNBT());
                        front.getWeatherObjects().forEach(weatherObject ->
                                {
                                    weatherObject.nbt.setUpdateForced(true);
                                    weatherObject.nbt.setUUID("frontUUID", uuid);
                                    weatherNBT.put("storm_" + weatherObject.getUUID().toString(), weatherObject.writeToNBT().getNewNBT());
                                    weatherObject.nbt.setUpdateForced(false);
                                    Weather2.debug("Saved storm_" + weatherObject.getUUID().toString());
                                }
                        );
                        Weather2.debug("Saved front_" + uuid.toString());
                    }
                }
        );

        mainNBT.put("frontData", frontNBT);
        mainNBT.put("stormData", weatherNBT);
        mainNBT.putFloat("cloudIntensity", cloudIntensity);
        mainNBT.putLong("ticksFrontFormed", ticksFrontFormed);
        mainNBT.putLong("ticksSandstormFormed", ticksSandstormFormed);
        mainNBT.putLong("ticksStormFormed", ticksStormFormed);
        mainNBT.put("windMan", windManager.writeToNBT(new CompoundNBT()));
        mainNBT.putInt("stormChanceToday", stormChanceToday);

        String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "weather2" + File.separator;

        try
        {

            if (!(new File(saveFolder).exists())) new File(saveFolder).mkdirs();
            FileOutputStream fos = new FileOutputStream(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + ".dat");
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
        CompoundNBT mainNBT = new CompoundNBT();
        String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "weather2" + File.separator;

        try
        {
            if (new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + ".dat").exists())
            {
                mainNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + ".dat"));
                File tmp = (new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + "_BACKUP0.dat"));
                if (tmp.exists())
                    FileUtils.copyFile(tmp, (new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + "_BACKUP1.dat")));
                if ((new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + ".dat").exists()))
                    FileUtils.copyFile((new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + ".dat")), (new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + "_BACKUP0.dat")));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Weather2.warn("Weather2 File: WeatherData.dat failed to load, automatically restoring to backup from previous game run");
            try
            {
                if ((new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + "_BACKUP0.dat")).exists())
                    mainNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + "_BACKUP0.dat"));
                else
                    Weather2.warn("Failed to find backup file WeatherData_BACKUP0.dat, nothing loaded");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Weather2.warn("Completely failed to find backup file WeatherData_BACKUP0.dat, nothing loaded");
            }
        }


        if (mainNBT.contains("cloudIntensity"))
            cloudIntensity = mainNBT.getFloat("cloudIntensity");
        if (mainNBT.contains("ticksFrontFormed"))
            ticksFrontFormed = mainNBT.getLong("ticksFrontFormed");
        if (mainNBT.contains("ticksSandstormFormed"))
            ticksSandstormFormed = mainNBT.getLong("ticksSandstormFormed");
        if (mainNBT.contains("ticksStormFormed"))
            ticksStormFormed = mainNBT.getLong("ticksStormFormed");
        if (mainNBT.contains("stormChanceToday"))
            stormChanceToday = mainNBT.getInt("stormChanceToday");

        windManager.readFromNBT(mainNBT.getCompound("windMan"));
        CompoundNBT volcanosNBT = mainNBT.getCompound("volcanoData");

        volcanosNBT.getAllKeys().forEach(name -> {
            CompoundNBT nbt = volcanosNBT.getCompound(name);
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


            PacketVolcanoObject.create(dim, vo);
        });

        CompoundNBT frontNBT = mainNBT.getCompound("frontData");
        frontNBT.getAllKeys().forEach(name ->
        {
            CompoundNBT nbt = frontNBT.getCompound(name);
            Weather2.debug("Front name: " + name);
            if (name.equals("front_global"))
            {
                globalFront = new FrontObject(this, null, 0);
                globalFront.readNBT(nbt);
                fronts.put(globalFront.getUUID(), globalFront);

                Weather2.debug("Loaded global front " + globalFront.getUUID().toString());
                PacketFrontObject.create(world.dimension(), globalFront);
            }
            else
            {
                FrontObject front = new FrontObject(this, new Vec3(nbt.getInt("layer"), nbt.getDouble("posX"), nbt.getDouble("posZ")), nbt.getInt("layer"));
                front.readNBT(nbt);
                fronts.put(front.getUUID(), front);
                Weather2.debug("Loaded front " + front.getUUID().toString());
                PacketFrontObject.create(world.dimension(), front);
            }
        });

        if (globalFront == null)
        {
            globalFront = new FrontObject(this, null, 0);
            fronts.put(globalFront.getUUID(), globalFront);
            Weather2.debug("Created a new global front " + globalFront.getUUID().toString());
            PacketFrontObject.create(world.dimension(), globalFront);
        }

        CompoundNBT weatherNBT = mainNBT.getCompound("stormData");
        weatherNBT.getAllKeys().forEach(name ->
        {
            try
            {
                CompoundNBT nbt = weatherNBT.getCompound(name);


                FrontObject front = null;
                if (nbt.hasUUID("frontUUID"))
                {
                    UUID frontUUID = nbt.getUUID("frontUUID");
                    if (frontUUID != null)
                    {
                        front = getFront(frontUUID);
                    }
                }


                if (front == null)
                    front = globalFront;

                WeatherObject wo = null;

                if (!nbt.contains("weatherObjectType"))
                {
                    Weather2.warn("Storm " + name + " missing weatherObjectType, skipping...");
                    return;
                }

                switch (nbt.getInt("weatherObjectType"))
                {
                    case 0:
                        wo = new StormObject(front);
                        break;
                    case 1:
                        wo = new SandstormObject(this);
                        break;
                    default:
                        Weather2.warn("Non-existent type attempted to load into the weather system. Skipping...");
                        return;
                }

                if (wo != null)
                {
                    try
                    {
                        wo.nbt.setNewNBT(nbt);
                        wo.nbt.updateCacheFromNew();
                        wo.readFromNBT();
                        front.addWeatherObject(wo);
                        Weather2.debug("Loaded storm " + wo.getUUID().toString());
                        PacketWeatherObject.create(world.dimension(), wo);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        });
        Weather2.debug("Loading Weather2 data successful!");
    }

    public boolean spawnSandstorm(Vec3 posIn)
    {
        
        int searchRadius = 512;
        double angle = windManager.windAngle;


        double dirX = -Maths.fastSin(Math.toRadians(angle));
        double dirZ = Maths.fastCos(Math.toRadians(angle));
        double vecX = dirX * searchRadius/2 * -1;
        double vecZ = dirZ * searchRadius/2 * -1;

        Random rand = new Random();

        BlockPos foundPos = null;

        int findTriesMax = 30;
        for (int i = 0; i < findTriesMax; i++)
        {
            int x = MathHelper.floor(posIn.posX + vecX + rand.nextInt(searchRadius * 2) - searchRadius);
            int z = MathHelper.floor(posIn.posZ + vecZ + rand.nextInt(searchRadius * 2) - searchRadius);

            BlockPos pos = new BlockPos(x, 0, z);

            if (!world.isLoaded(pos)) continue;
            Biome biomeIn = world.getBiome(pos);

            if (SandstormObject.isDesert(biomeIn, true))
            {

                foundPos = pos;



                double dirXLeft = -Maths.fastSin(Math.toRadians(angle-90));
                double dirZLeft = Maths.fastCos(Math.toRadians(angle-90));
                double dirXRight = -Maths.fastSin(Math.toRadians(angle+90));
                double dirZRight = Maths.fastCos(Math.toRadians(angle+90));

                double distLeftRight = 20;
                BlockPos posLeft = new BlockPos(foundPos.getX() + (dirXLeft * distLeftRight), 0, foundPos.getZ() + (dirZLeft * distLeftRight));
                if (!world.isLoaded(posLeft)) continue;
                if (!SandstormObject.isDesert(world.getBiome(posLeft))) continue;

                BlockPos posRight = new BlockPos(foundPos.getX() + (dirXRight * distLeftRight), 0, foundPos.getZ() + (dirZRight * distLeftRight));
                if (!world.isLoaded(posRight)) continue;
                if (!SandstormObject.isDesert(world.getBiome(posRight))) continue;



                BlockPos posFind = new BlockPos(foundPos);
                BlockPos posFindLastGoodUpwind = new BlockPos(foundPos);
                BlockPos posFindLastGoodDownwind = new BlockPos(foundPos);
                double tickDist = 10;

                while (world.isLoaded(posFind) && SandstormObject.isDesert(world.getBiome(posFind)))
                {

                    posFindLastGoodUpwind = new BlockPos(posFind);


                    int xx = MathHelper.floor(posFind.getX() + (dirX * -1D * tickDist));
                    int zz = MathHelper.floor(posFind.getZ() + (dirZ * -1D * tickDist));

                    posFind = new BlockPos(xx, 0, zz);
                }


                posFind = new BlockPos(foundPos);

                while (world.isLoaded(posFind) && SandstormObject.isDesert(world.getBiome(posFind)))
                {

                    posFindLastGoodDownwind = new BlockPos(posFind);


                    int xx = MathHelper.floor(posFind.getX() + (dirX * 1D * tickDist));
                    int zz = MathHelper.floor(posFind.getZ() + (dirZ * 1D * tickDist));

                    posFind = new BlockPos(xx, 0, zz);
                }

                int minDistanceOfDesertStretchNeeded = 200;
                double dist = Math.sqrt(posFindLastGoodUpwind.distSqr(posFindLastGoodDownwind));

                if (dist >= minDistanceOfDesertStretchNeeded)
                {
                    SandstormObject sandstorm = new SandstormObject(this);

                    sandstorm.init();
                    BlockPos posSpawn = new BlockPos(WeatherUtilBlock.getPrecipitationHeightSafe(world, posFindLastGoodUpwind)).above(1);
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

    public void playerJoinedWorldSyncFull(ServerPlayerEntity entP)
    {
        Weather2.debug((entP == null ? "An unknown player " : "Player " + entP.getDisplayName().getString() + "'s client requested for a full sync"));


        fronts.forEach((uuid, front) -> {PacketFrontObject.create(entP, front); front.getWeatherObjects().forEach(wo -> PacketWeatherObject.create(entP, wo));});


        volcanoObjects.forEach(vo -> PacketVolcanoObject.create(entP, vo));
    }


    public void nbtStormsForIMC()
    {
        CompoundNBT nbt = new CompoundNBT();
        List<WeatherObject> list = getWeatherObjects();
        for (WeatherObject wo : list)
            if (wo instanceof IWeatherRain && ((IWeatherRain)wo).hasDownfall() || wo instanceof IWeatherStaged && ((IWeatherStaged)wo).getStage() > 0)
            {
                wo.writeToNBT();
                nbt.put("storm_" + wo.getUUID().toString(), wo.nbt.getNewNBT());
            }

        if (!nbt.isEmpty())
            InterModComms.sendTo(Weather2.MODID, "weather.storms", () -> nbt);
    }

    protected boolean canSpawnWeather(int type)
    {
        if (!EZConfigParser.isWeatherEnabled(world.dimension().location().toString()))
            return false;
        long ticks;

        switch(type)
        {
            case 0:
                ticks = ticksFrontFormed - world.getGameTime();
                if (ticks > ConfigStorm.storm_spawn_delay)
                    ticksFrontFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;
                return (!ConfigStorm.disable_tornados || !ConfigStorm.disable_cyclones) && ticksFrontFormed < world.getGameTime() && fronts.size() - 1 < ConfigFront.max_front_objects;
            case 1:
                ticks = ticksStormFormed - world.getGameTime();
                if (ticks > ConfigStorm.storm_spawn_delay)
                    ticksStormFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;
                return (!ConfigStorm.disable_tornados || !ConfigStorm.disable_cyclones) && ticksStormFormed < world.getGameTime() && systems.size() < ConfigStorm.max_weather_objects;
            case 2:
                
                return false;
            default:
                return false;
        }
    }
}
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/weather/WeatherManagerServer.java
