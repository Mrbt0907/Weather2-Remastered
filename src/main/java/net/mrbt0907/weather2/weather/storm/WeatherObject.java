package net.mrbt0907.weather2.weather.storm;

import java.util.UUID;

import CoroUtil.util.Vec3;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.util.CachedNBTTagCompound;
import net.mrbt0907.weather2.weather.WeatherSystem;

public class WeatherObject
{
	private UUID id;
	public WeatherSystem manager;
	public CachedNBTTagCompound nbt;
	public WeatherEnum.Type type = WeatherEnum.Type.CLOUD;
	public EnumWeatherObjectType weatherObjectType = EnumWeatherObjectType.CLOUD;
	public Vec3 pos = new Vec3(0, 0, 0);
	public Vec3 posGround = new Vec3(0, 0, 0);
	public Vec3 motion = new Vec3(0, 0, 0);
	public boolean isDying = false;
	public boolean isDead = false;
	public long ticks = 0L;
	public int size = 50;

	/**
	 * used to count up to a threshold to finally remove weather objects,
	 * solves issue of simbox cutoff removing storms for first few ticks as player is joining in singleplayer
	 * helps with multiplayer, requiring 30 seconds of no players near before removal
	 */
	public int ticksSinceNoNearPlayer = 0;

	public WeatherObject(WeatherSystem manager)
	{
		this.manager = manager;
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
			cleanupClient();
		
		cleanup();
	}
	
	public void reset() {setDead();}
	public void cleanup() {manager = null;}
	
	@SideOnly(Side.CLIENT)
	public void cleanupClient() {}
	
	public void readFromNBT() {}
	public void writeToNBT() {}
	public void readNBT()
	{	
		id = nbt.getUUID("ID");
		pos = new Vec3(nbt.getDouble("posX"), nbt.getDouble("posY"), nbt.getDouble("posZ"));
		motion = new Vec3(nbt.getDouble("vecX"), nbt.getDouble("vecY"), nbt.getDouble("vecZ"));
		size = nbt.getInteger("size");
		type = WeatherEnum.Type.get(nbt.getInteger("weatherType"));
		weatherObjectType = EnumWeatherObjectType.get(nbt.getInteger("weatherObjectType"));
	}
	
	public void writeNBT()
	{
		nbt.setDouble("posX", pos.xCoord);
		nbt.setDouble("posY", pos.yCoord);
		nbt.setDouble("posZ", pos.zCoord);
		nbt.setDouble("vecX", motion.xCoord);
		nbt.setDouble("vecY", motion.yCoord);
		nbt.setDouble("vecZ", motion.zCoord);
		nbt.setUUID("ID", id);
		//just blind set ID into non cached data so client always has it, no need to check for forced state and restore orig state
		nbt.getNewNBT().setUniqueId("ID", id);
		nbt.setInteger("size", size);
		nbt.setInteger("weatherType", type.ordinal());
		nbt.setInteger("weatherObjectType", weatherObjectType.ordinal());
	}
	
	public int getNetRate() {return 40;}
	public boolean isDangerous() {return type.isDangerous();}
	public UUID getUUID() {return id;}
}
