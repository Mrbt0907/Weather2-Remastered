<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/client/weather/WeatherManagerClient.java
package net.mrbt0907.weather2remastered.client.weather;
=======
package net.mrbt0907.weather2.client.weather;
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/client/weather/WeatherManagerClient.java

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/client/weather/WeatherManagerClient.java

=======
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/client/weather/WeatherManagerClient.java
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/client/weather/WeatherManagerClient.java
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.weather.AbstractFrontObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractStormObject;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherManager;
import net.mrbt0907.weather2remastered.api.weather.AbstractWeatherObject;
import net.mrbt0907.weather2remastered.api.weather.IWeatherRain;
import net.mrbt0907.weather2remastered.api.weather.IWeatherStaged;
import net.mrbt0907.weather2remastered.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2remastered.client.NewSceneEnhancer;
import net.mrbt0907.weather2remastered.config.ConfigClient;
import net.mrbt0907.weather2remastered.config.ConfigStorm;
import net.mrbt0907.weather2remastered.config.ConfigVolume;
import net.mrbt0907.weather2remastered.registry.SoundRegistry;
import net.mrbt0907.weather2remastered.util.Maths;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;
import net.mrbt0907.weather2remastered.util.fartsy.FartsyUtil;

@OnlyIn(Dist.CLIENT)
public class WeatherManagerClient extends AbstractWeatherManager
{
	private static final Minecraft MC = Minecraft.getInstance(); 
	//data for client, stormfronts synced from server
	//new for 1.10.2, replaces world.weatherEffects use
	public List<Particle> effectedParticles = new ArrayList<Particle>();
	public List<Particle> weatherParticles = new ArrayList<Particle>();
	public static AbstractStormObject closestStormCached;
	public int weatherID = 0;
	public int weatherRainTime = 0;
	private int particleLimit = 0;

	public WeatherManagerClient(ClientWorld world)
	{
		super(world);
	}
	
	@Override
	public World getWorld()
	{
		return Minecraft.getInstance().level;
	}
	
	@Override
	public void tick(boolean isClientTick)
	{
		super.tick(true);
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
	public void nbtSyncFromServer(CompoundNBT mainNBT)
	{
		//System.out.println("SYNCING FROM SERVER " + FartsyUtil.prettyPrintNBT(mainNBT));
		int command = mainNBT.getInt("command");
		switch(command)
		{
			case 0:
				weatherID = mainNBT.getInt("weatherID");
				weatherRainTime = mainNBT.getInt("weatherRainTime");
				break;
			case 1:
			{
				CompoundNBT nbt = mainNBT.getCompound("weatherObject");
				if (!nbt.contains("frontUUID")) {
					Weather2Remastered.debug("nbt didn't contain frontUUID...?: " + FartsyUtil.prettyPrintNBT(nbt));
					break;
				}
				AbstractFrontObject front = getFront(nbt.getUUID("frontUUID"));
				UUID uuid = nbt.getUUID("ID");
				
				Type weatherObjectType = Type.get(nbt.getInt("weatherType"));
				
				AbstractWeatherObject wo = null;
				if (weatherObjectType.ordinal() < Type.SANDSTORM.ordinal())
				{
					System.out.println(nbt.getUUID("frontUUID"));
					if (front == null) {
						Weather2Remastered.debug("FRONT NULL, is it a global front maybe...?" + front);
						return;
					}
					Weather2Remastered.debug("Creating a new storm: " + uuid.toString());
					wo = new AbstractStormObject(front);
				}
				else
				{
					Weather2Remastered.debug("NOT Creating a new sandstorm: " + uuid.toString());
					//wo = new SandstormObject(this);
				}
				
				//StormObject so
				wo.nbt.setNewNBT(nbt);
				wo.nbt.updateCacheFromNew();
				wo.readFromNBT();
				
				front.addWeatherObject(wo);
				refreshParticleLimit();
				break;
			}
			case 2:
			{
				CompoundNBT nbt = mainNBT.getCompound("weatherObject");
				AbstractFrontObject front = getFront(nbt.getUUID("frontUUID"));
				if(front == null) break;
					
				AbstractWeatherObject wo = front.getWeatherObject(nbt.getUUID("ID"));
				if (wo != null)
				{
					wo.nbt.setNewNBT(nbt);
					wo.readFromNBT();
					wo.nbt.updateCacheFromNew();
				}
				break;
			}
			case 3:
			{
				UUID uuidA = mainNBT.getUUID("weatherObject"), uuidB = mainNBT.getUUID("frontObject");
				AbstractFrontObject front = getFront(uuidB);
				AbstractWeatherObject system = systems.get(uuidA);

				if (front != null)
					front.removeWeatherObject(uuidA);
				else if (system != null)
					removeWeatherObject(uuidA);
				refreshParticleLimit();
				break;
			}
			case 4:
			{
				CompoundNBT nbt = mainNBT.getCompound("volcanoObject");
				//VolcanoObject vo = new VolcanoObject(this);
				Weather2Remastered.debug("error Creating a new volcano: " + nbt.getUUID("ID"));
				//vo.nbtSyncFromServer(nbt);
				//addVolcanoObject(vo);
				break;
			}
			case 5:
			{
				//CompoundNBT stormNBT = mainNBT.getCompound("volcanoObject");
				//UUID uuid = stormNBT.getUUID("ID");
				
				//if (volcanoUUIDS.contains(uuid))
				//	getVolcanoObjectByID(uuid).nbtSyncFromServer(stormNBT);
				break;
			}
			case 6:
			{
				CompoundNBT nbt = mainNBT.getCompound("manager");
				windManager.nbtSyncFromServer(nbt);
				break;
			}
			case 7:
			{
				int posXS = mainNBT.getInt("posX");
				int posYS = mainNBT.getInt("posY");
				int posZS = mainNBT.getInt("posZ");
				ServerWorld dim = ServerLifecycleHooks.getCurrentServer().getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mainNBT.getString("dimension"))));
				if (mainNBT.getBoolean("withBolt") == true) {
					//System.out.println("Creating lightning in dimension " + mainNBT.getString("dimension") + " ServerWorld: " + dim);
					LightningBoltEntity lightning = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(dim);
					if (lightning == null) {
						System.out.println("WHAT");
					}
				    if (lightning != null) {
				        lightning.moveTo(posXS + 0.5, posYS, posZS + 0.5);
				        lightning.setVisualOnly(false);
				        dim.addFreshEntity(lightning);
				        System.out.println("SPAWNED AT " + posXS + " " + posYS + " " + posZS);
				    }
				}
				else
				{
					int x = mainNBT.getInt("posX"), y = mainNBT.getInt("posY"), z = mainNBT.getInt("posZ");
					double distance = Maths.distanceSq(MC.player.getX(), MC.player.getY(), MC.player.getZ(), x, y, z);
					if (MC.player != null)
					{
						if (distance < ConfigStorm.max_lightning_bolt_distance)
						{
							if (ConfigClient.enable_sky_lightning)
								world.setSkyFlashTime(4);
							world.playSound(null, new BlockPos(x, y, z), SoundRegistry.THUNDER_NEAR.get(), SoundCategory.WEATHER, 10000.0F * (float)ConfigVolume.lightning, Maths.random(0.65F, 0.75F));
						}
						else if (distance < ConfigStorm.max_lightning_bolt_distance * 1.5D)
							world.playSound(null, new BlockPos(x, y, z), SoundRegistry.THUNDER_FAR.get(), SoundCategory.WEATHER, 10000.0F * (float)ConfigVolume.lightning, Maths.random(0.65F, 0.75F));
					}
				}
				break;
			}
			case 11:
			{
				CompoundNBT nbt = mainNBT.getCompound("frontObject");

				UUID uuid = nbt.getUUID("uuid");
				if (nbt.contains("posX"))
				{
					Weather2Remastered.debug("Creating a new front: " + uuid.toString() + "" + nbt.getDouble("posX"));
					AbstractFrontObject front = createFront(nbt.getInt("layer"), nbt.getDouble("posX"), nbt.getDouble("posZ"));
					front.readNBT(nbt);
					fronts.put(uuid, front);
				}
				else
				{
					Weather2Remastered.debug("Creating a new global front: " + uuid.toString());
					globalFront = new AbstractFrontObject(this, null, 0);
					globalFront.readNBT(nbt);
					fronts.put(uuid, globalFront);
				}
				break;
			}
			case 12:
			{
				CompoundNBT nbt = mainNBT.getCompound("frontObject");
				AbstractFrontObject front = getFront(nbt.getUUID("uuid"));
				//System.out.println(nbt.getUUID("uuid"));
				if(front != null) front.readNBT(nbt);
				break;
			}
			case 13:
			{
				UUID uuid = mainNBT.getUUID("frontUUID");
				AbstractFrontObject front = getFront(uuid);
				if (front != null)
					if (globalFront.equals(front))
						globalFront.reset();
					else
					{
						front.reset();
						removeFront(front);
					}
				else
					Weather2Remastered.debug("error removing front, cant find by ID: " + uuid.toString());
				break;
			}
			case 14:
			{
				fronts.forEach((uuid, front) -> front.cleanupClient(false));
				weatherParticles.forEach(particle -> particle.remove());
				weatherParticles.clear();
				Weather2Remastered.debug("Cleaned up client particles");
				break;
			}
			case 20: 
			{
				//if (net.minecraft.client.Minecraft.getInstance().level != null) {
					//net.minecraft.client.world.ClientWorld world = net.minecraft.client.Minecraft.getInstance().level;
					//world.setThunderLevel(mainNBT.getFloat("setThunderLevel"));
//					System.out.println("Changing thunder level in world! Now: " + mainNBT.getFloat("setThunderLevel"));
				//}
				break;
			}
			default:
				Weather2Remastered.error("Server sent an invalid network packet");
		}
	}
	
	public float getRainTargetValue(Vec3 position)
	{
		float rainTarget = -Float.MAX_VALUE, rain;
		List<AbstractWeatherObject> systems = new ArrayList<AbstractWeatherObject>(this.systems.values());
		
		for (AbstractWeatherObject system : systems)
		{
			if (system instanceof IWeatherRain)
			{
				rain = ((IWeatherRain)system).getDownfall(position) - IWeatherRain.MINIMUM_DRIZZLE;
				if (rain > rainTarget)
					rainTarget = rain;
//				System.out.println(Math.abs(rainTarget) > 0.10 ? Maths.clamp(Math.abs(rainTarget) / IWeatherRain.MINIMUM_HEAVY_RAIN, 0.0F, 1.0F) : 0.0F);
			}
		}
		return Math.abs(rainTarget) > 0.10 ? Maths.clamp(Math.abs(rainTarget) / IWeatherRain.MINIMUM_HEAVY_RAIN, 0.0F, 1.0F) : 0.0F;
	}
	
	public float getOvercastTargetValue(Vec3 position)
	{
		float overcastTarget = -1.0F, overcast;
		List<AbstractWeatherObject> systems = new ArrayList<AbstractWeatherObject>(this.systems.values());
		
		for (AbstractWeatherObject system : systems)
		{
			if (system instanceof IWeatherStaged)
			{
				float distance = (float) Maths.distanceSq(position.posX, position.posZ, system.pos.posX, system.pos.posZ);
				float distanceMult = 1.0F - Math.min(Math.max(distance - system.size, 0.0F) / (system.size * 0.25F), 1.0F);
				float stageMult = 0.25F + Math.min(((IWeatherStaged)system).getStage() * 0.25F, 0.5F) + (system instanceof AbstractStormObject && ((AbstractStormObject)system).isViolent ? 0.25F : 0.0F);
				overcast = stageMult * distanceMult;
				if (overcast > overcastTarget)
					overcastTarget = overcast;
				//System.out.println(overcast);
			}
		}
		return Maths.clamp(overcastTarget, 0.0F, 1.0F);
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
	
	public void refreshParticleLimit()
	{
		int systems = 0;
		final ClientPlayerEntity player = MC.player;
		if (player == null) return;
		for (AbstractWeatherObject system : this.systems.values())
			if (system.pos.distanceSq(player.getX(), system.pos.posY, player.getZ()) < NewSceneEnhancer.instance().renderDistance)
				systems++;
		
		if (systems == 0) systems = 1;
		
		particleLimit = ConfigClient.max_particles > 0 ? ConfigClient.max_particles / systems : Integer.MAX_VALUE;
		this.systems.forEach((uuid, system) ->
		{
			if (system instanceof AbstractStormObject && ((AbstractStormObject)system).particleRenderer != null)
				((AbstractStormObject)system).particleRenderer.refreshParticleLimit();
		});
	}
	
	public int getParticleLimit()
	{
		return particleLimit;
	}
	
	@Override
	public void reset(boolean fullReset)
	{
		super.reset(fullReset);
		effectedParticles.clear();
		closestStormCached = null;
	}

	/**Gets the target rain value based on the closest raining storm in range**/
	public float getRainTarget(Vec3 pos, float maxDistSq) {
		float rainTarget = 0.0F;
		AbstractStormObject storm = getStrongestClosestStormWithRain(pos, maxDistSq);
		if (storm != null) {
			//System.out.println(storm.getUUID() + " has humidity of " + storm.rain + " at " + storm.pos.toBlockPos().toString());
			float rain = ((IWeatherRain)storm).getDownfall(pos) - IWeatherRain.MINIMUM_DRIZZLE;
			if (rain > rainTarget) rainTarget = rain;
		}
		//if (rainTarget != 0.0F)System.out.println("GetRainTarget returned " + ((rainTarget > ConfigStorm.min_overcast_rain) ? Maths.clamp(rainTarget / IWeatherRain.MINIMUM_HEAVY_RAIN, 0.0F, 10.0F) : 0.0F));
		return rainTarget > ConfigStorm.min_overcast_rain ? Maths.clamp(rainTarget / IWeatherRain.MINIMUM_HEAVY_RAIN, 0.0F, 10.0F) : 0.0F;
	}

	/**Gets the target overcast value based on the closest and worst storm in range**/
	public float getOvercastTarget(Vec3 pos, float maxDistSq) {
		float overcastTarget = 0.0F;
		AbstractStormObject storm = getStrongestClosestStorm(pos, maxDistSq);
		if (storm != null) {
			float distance = (float) Maths.distanceSq(pos.posX, pos.posZ, storm.pos.posX, storm.pos.posZ);
			float distanceMult = 1.0F - Math.min(Math.max(distance - storm.size, 0.0F) / (storm.size * 0.25F), 1.0F);
			float stageMult = 0.25F + Math.min(((IWeatherStaged)storm).getStage() * 0.25F, 0.5F) + (storm.isViolent ? 0.5F : 0.0F);
			float overcast = stageMult * distanceMult;
			if (overcast > overcastTarget) overcastTarget = overcast;
		}
		//if (overcastTarget != 0.0F)System.out.println("getOvercast returned: " + overcastTarget);
		return Maths.clamp(overcastTarget, 0.0F, 1.0F);
	}
=======
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.IWeatherStaged;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.entity.EntityLightningEX;
import net.mrbt0907.weather2.mixins.accessor.ClientWorldAccessor;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManager;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;

@OnlyIn(Dist.CLIENT)
public class WeatherManagerClient extends WeatherManager
{
    private static final Minecraft MC = Minecraft.getInstance();


    public List<Particle> effectedParticles = new ArrayList<Particle>();
    public List<Particle> weatherParticles = new ArrayList<Particle>();
    public static StormObject closestStormCached;
    public int weatherID = 0;
    public int weatherRainTime = 0;
    private int particleLimit = 0;

    public WeatherManagerClient(World world)
    {
        super(world);
    }

    public ClientWorld getWorld() {
        return (ClientWorld) Minecraft.getInstance().level;
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


    public void nbtSyncFromServer(CompoundNBT mainNBT)
    {
        int command = mainNBT.getInt("command");
        switch(command)
        {
            case 0:
                weatherID = mainNBT.getInt("weatherID");
                weatherRainTime = mainNBT.getInt("weatherRainTime");
                break;
            case 1:
            {
                CompoundNBT nbt = mainNBT.getCompound("weatherObject");
                FrontObject front = getFront(nbt.getUUID("frontUUID"));
                UUID uuid = nbt.getUUID("ID");

                Type weatherObjectType = Type.get(nbt.getInt("weatherType"));

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


                wo.nbt.setNewNBT(nbt);
                wo.nbt.updateCacheFromNew();
                wo.readFromNBT();

                front.addWeatherObject(wo);
                refreshParticleLimit();
                break;
            }
            case 2:
            {
                CompoundNBT nbt = mainNBT.getCompound("weatherObject");
                FrontObject front = getFront(nbt.getUUID("frontUUID"));
                if(front == null) break;

                WeatherObject wo = front.getWeatherObject(nbt.getUUID("ID"));
                if (wo != null)
                {
                    wo.nbt.setNewNBT(nbt);
                    wo.readFromNBT();
                    wo.nbt.updateCacheFromNew();
                }
                break;
            }
            case 3:
            {
                UUID uuidA = mainNBT.getUUID("weatherObject"), uuidB = mainNBT.getUUID("frontObject");
                FrontObject front = getFront(uuidB);
                WeatherObject system = systems.get(uuidA);

                if (front != null)
                    front.removeWeatherObject(uuidA);
                else if (system != null)
                    removeWeatherObject(uuidA);

                refreshParticleLimit();
                break;
            }
            case 4:
            {
                CompoundNBT nbt = mainNBT.getCompound("volcanoObject");
                VolcanoObject vo = new VolcanoObject(this);
                Weather2.debug("Creating a new volcano: " + nbt.getUUID("ID"));
                vo.nbtSyncFromServer(nbt);
                addVolcanoObject(vo);
                break;
            }
            case 5:
            {
                CompoundNBT stormNBT = mainNBT.getCompound("volcanoObject");
                UUID uuid = stormNBT.getUUID("ID");

                if (volcanoUUIDS.contains(uuid))
                    getVolcanoObjectByID(uuid).nbtSyncFromServer(stormNBT);
                break;
            }
            case 6:
            {
                CompoundNBT nbt = mainNBT.getCompound("manager");
                windManager.nbtSyncFromServer(nbt);
                break;
            }
            case 7:
            {
                int posXS = mainNBT.getInt("posX");
                int posYS = mainNBT.getInt("posY");
                int posZS = mainNBT.getInt("posZ");
                if (mainNBT.contains("entityID"))
                {
                    double posX = (double)posXS;
                    double posY = (double)posYS;
                    double posZ = (double)posZS;
                    Entity ent = new EntityLightningEX(world, posX, posY, posZ);
                    ent.setId(mainNBT.getInt("entityID"));
                    world.addFreshEntity(ent);
                }
                else
                {
                    int x = mainNBT.getInt("posX"), y = mainNBT.getInt("posY"), z = mainNBT.getInt("posZ");
                    double distance = Maths.distanceSq(MC.player.getX(), MC.player.getY(), MC.player.getZ(), x, y, z);
                    if (MC.player != null)
                    {
                        if (distance < ConfigStorm.max_lightning_bolt_distance)
                        {
                            if (ConfigClient.enable_sky_lightning && world instanceof ClientWorld) {
                                ((ClientWorldAccessor) world).setSkyFlashTime(2);
                            }

                            world.playSound(MC.player, x, y, z, SoundRegistry.thunderNear.get(), SoundCategory.WEATHER,
                                    10000.0F * (float)ConfigVolume.lightning, Maths.random(0.65F, 0.75F));
                        }
                        else if (distance < ConfigStorm.max_lightning_bolt_distance * 1.5D)
                        {
                            world.playSound(MC.player, x, y, z, SoundRegistry.thunderFar.get(), SoundCategory.WEATHER,
                                    10000.0F * (float)ConfigVolume.lightning, Maths.random(0.65F, 0.75F));
                        }
                    }
                }
                break;
            }
            case 11:
            {
                CompoundNBT nbt = mainNBT.getCompound("frontObject");
                UUID uuid = mainNBT.getUUID("uuid");
                if (nbt.contains("posX"))
                {
                    Weather2.debug("Creating a new front: " + uuid.toString());
                    FrontObject front = createFront(nbt.getInt("layer"), nbt.getDouble("posX"), nbt.getDouble("posZ"));
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
                CompoundNBT nbt = mainNBT.getCompound("frontObject");
                FrontObject front = getFront(nbt.getUUID("uuid"));
                if(front != null)
                    front.readNBT(nbt);
                break;
            }
            case 13:
            {
                UUID uuid = mainNBT.getUUID("frontUUID");
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
                weatherParticles.forEach(particle -> particle.remove());
                weatherParticles.clear();
                Weather2.debug("Cleaned up client particles");
                break;
            }
            default:
                Weather2.error("Server sent an invalid network packet");
        }
    }

    public float getRainTargetValue(Vec3 position)
    {
        float rainTarget = -Float.MAX_VALUE, rain;
        List<WeatherObject> systems = new ArrayList<WeatherObject>(this.systems.values());

        for (WeatherObject system : systems)
        {
            if (system instanceof IWeatherRain)
            {
                rain = ((IWeatherRain)system).getDownfall(position) - IWeatherRain.MINIMUM_DRIZZLE;
                if (rain > rainTarget)
                    rainTarget = rain;
            }
        }
        return Maths.clamp(rainTarget / IWeatherRain.MINIMUM_HEAVY_RAIN, 0.0F, 1.0F);
    }

    public float getOvercastTargetValue(Vec3 position)
    {
        float overcastTarget = -Float.MAX_VALUE, overcast;
        List<WeatherObject> systems = new ArrayList<WeatherObject>(this.systems.values());

        for (WeatherObject system : systems)
        {
            if (system instanceof IWeatherStaged)
            {
                float distance = (float) Maths.distanceSq(position.posX, position.posZ, system.pos.posX, system.pos.posZ);
                float distanceMult = 1.0F - Math.min(Math.max(distance - system.size, 0.0F) / (system.size * 0.25F), 1.0F);
                float stageMult = 0.25F + Math.min(((IWeatherStaged)system).getStage() * 0.25F, 0.5F) + (system instanceof StormObject && ((StormObject)system).isViolent ? 0.25F : 0.0F);
                overcast = stageMult * distanceMult;
                if (overcast > overcastTarget)
                    overcastTarget = overcast;
            }
        }

        return Maths.clamp(overcastTarget, 0.0F, 1.0F);
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

    public void refreshParticleLimit()
    {
        int systems = 0;
        final ClientPlayerEntity player = MC.player;
        if (player == null) return;
        for (WeatherObject system : this.systems.values())
            if (system.pos.distanceSq(player.getX(), system.pos.posY, player.getZ()) < NewSceneEnhancer.instance().renderDistance)
                systems++;

        if (systems == 0) systems = 1;

        particleLimit = ConfigClient.max_particles > 0 ? ConfigClient.max_particles / systems : Integer.MAX_VALUE;
        this.systems.forEach((uuid, system) ->
        {
            if (system instanceof StormObject && ((StormObject)system).particleRenderer != null)
                ((StormObject)system).particleRenderer.refreshParticleLimit();
        });
    }

    public int getParticleLimit()
    {
        return particleLimit;
    }

    @Override
    public void reset(boolean fullReset)
    {
        super.reset(fullReset);
        effectedParticles.clear();
        closestStormCached = null;
    }


    public float getRainTarget(Vec3 pos, float maxDistSq) {
        float rainTarget = 0.0F;
        StormObject storm = getStrongestClosestStormWithRain(pos, maxDistSq);
        if (storm != null) {
            float rain = ((IWeatherRain)storm).getDownfall(pos) - IWeatherRain.MINIMUM_DRIZZLE;
            if (rain > rainTarget) rainTarget = rain;
        }
        return Maths.clamp(rainTarget / IWeatherRain.MINIMUM_HEAVY_RAIN, 0.0F, 1.0F);
    }


    public float getOvercastTarget(Vec3 pos, float maxDistSq) {
        float overcastTarget = 0.0F;
        StormObject storm = getStrongestClosestStorm(pos, maxDistSq);
        if (storm != null) {
            float distance = (float) Maths.distanceSq(pos.posX, pos.posZ, storm.pos.posX, storm.pos.posZ);
            float distanceMult = 1.0F - Math.min(Math.max(distance - storm.size, 0.0F) / (storm.size * 0.25F), 1.0F);
            float stageMult = 0.25F + Math.min(((IWeatherStaged)storm).getStage() * 0.25F, 0.5F) + (storm.isViolent ? 0.25F : 0.0F);
            float overcast = stageMult * distanceMult;
            if (overcast > overcastTarget) overcastTarget = overcast;
        }
        return overcastTarget;
    }
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/client/weather/WeatherManagerClient.java
}