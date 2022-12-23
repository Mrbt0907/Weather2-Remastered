package net.mrbt0907.weather2.weather.storm;

import java.util.UUID;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.api.weather.IWeatherDetectable;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.util.CachedNBTTagCompound;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManager;

public abstract class WeatherObject implements IWeatherDetectable
{
	private UUID id;
	public FrontObject front;
	public WeatherManager manager;
	public CachedNBTTagCompound nbt;
	public WeatherEnum.Type type = WeatherEnum.Type.CLOUD;
	public Vec3 pos = new Vec3(0, 0, 0);
	public Vec3 posGround = new Vec3(0, 0, 0);
	public Vec3 motion = new Vec3(0, 0, 0);
	public boolean isDying = false;
	public boolean isDead = false;
	public long ticks = 0L;
	public int size = ConfigStorm.min_storm_size;
	
	/**
	 * used to count up to a threshold to finally remove weather objects,
	 * solves issue of simbox cutoff removing storms for first few ticks as player is joining in singleplayer
	 * helps with multiplayer, requiring 30 seconds of no players near before removal
	 */
	public int ticksSinceNoNearPlayer = 0;

	public WeatherObject(FrontObject front)
	{
		this.front = front;
		manager = front.getWeatherManager();
		nbt = new CachedNBTTagCompound();
		init();
	}
	
	public void init()
	{
		id = UUID.randomUUID();
	}
	
	public void tick()
	{
		ticks++;
		
		if (ticks < 0)
			ticks = 0;
	}
	
	@SideOnly(Side.CLIENT)
	public void tickRender(float partialTick) {}
	
	public void setDead()
	{
		isDead = true;
		
		//cleanup memory
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) 
			cleanupClient(true);
		
		cleanup();
	}
	
	public void reset() {setDead();}
	public void cleanup() {manager = null;}
	
	@SideOnly(Side.CLIENT)
	public void cleanupClient(boolean wipe) {}
	public void readFromNBT() {}
	public void writeToNBT() {}
	public void readNBT()
	{	
		id = nbt.getUUID("ID");
		pos = new Vec3(nbt.getDouble("posX"), nbt.getDouble("posY"), nbt.getDouble("posZ"));
		motion = new Vec3(nbt.getDouble("vecX"), nbt.getDouble("vecY"), nbt.getDouble("vecZ"));
		size = nbt.getInteger("size");
		type = WeatherEnum.Type.get(nbt.getInteger("weatherType"));
		isDying = nbt.getBoolean("isDying");
		isDead = nbt.getBoolean("isDead");
	}
	
	public CachedNBTTagCompound writeNBT()
	{
		nbt.setDouble("posX", pos.posX);
		nbt.setDouble("posY", pos.posY);
		nbt.setDouble("posZ", pos.posZ);
		nbt.setDouble("vecX", motion.posX);
		nbt.setDouble("vecY", motion.posY);
		nbt.setDouble("vecZ", motion.posZ);
		nbt.setUUID("ID", id);
		nbt.setUUID("frontUUID", front.getUUID());
		//just blind set ID into non cached data so client always has it, no need to check for forced state and restore orig state
		nbt.getNewNBT().setUniqueId("ID", id);
		nbt.setInteger("size", size);
		nbt.setInteger("weatherType", type.ordinal());
		nbt.setBoolean("isDying", isDying);
		nbt.setBoolean("isDead", isDead);
		return nbt;
	}
	
	public int getNetRate() {return 40;}
	public boolean isDangerous() {return type.isDangerous();}
	public UUID getUUID() {return id;}
	

	@SideOnly(Side.CLIENT)
	public abstract int getParticleCount();
	
	@SideOnly(Side.CLIENT)
	public abstract boolean canSpawnParticle();

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
}
