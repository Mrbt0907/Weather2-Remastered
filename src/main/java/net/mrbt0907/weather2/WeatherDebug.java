package net.mrbt0907.weather2;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WeatherDebug
{
	public static final DebugInfo stormRendering = new DebugInfo("rendering");
	public static final DebugInfo stormLogic = new DebugInfo("logic");
	
	public static void tick(World world)
	{
		if (world.isRemote)
		{
			
		}
		else
		{
			
		}
	}
	
	public static class DebugInfo
	{
		public String id;
		public long delta;
		public final String name;
		public final List<String> info = new ArrayList<String>();
		public final List<String> extraInfo = new ArrayList<String>();
		
		public DebugInfo(String name)
		{
			this.name = name;
		}
		
		public NBTTagCompound writeNBTData(NBTTagCompound nbt)
		{
			nbt.setString("name", name);
			nbt.setString("id", id);
			nbt.setLong("delta", delta);

			NBTTagCompound nbtInfo = new NBTTagCompound();
			NBTTagCompound nbtExtraInfo = new NBTTagCompound();
			
			for (int i = 0; i < info.size(); i++)
				nbtInfo.setString(i + "", info.get(i));
			for (int i = 0; i < extraInfo.size(); i++)
				nbtExtraInfo.setString(i + "", extraInfo.get(i));
			
			nbt.setTag("info", nbtInfo);
			nbt.setTag("extraInfo", nbtExtraInfo);
			
			return nbt;
		}
		
		public void readNBTData(NBTTagCompound nbt)
		{
			id = nbt.getString("id");
			delta = nbt.getLong("delta");
			
			NBTTagCompound nbtInfo = nbt.getCompoundTag("info");
			NBTTagCompound nbtExtraInfo = nbt.getCompoundTag("extraInfo");
			
			info.clear();
			extraInfo.clear();
			
			for (String key : nbtInfo.getKeySet())
				info.add(nbtInfo.getString(key));
			for (String key : nbtExtraInfo.getKeySet())
				extraInfo.add(nbtExtraInfo.getString(key));
		}
	}
}