package net.mrbt0907.weather2.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.server.weather.WeatherManagerServer;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class TileWeatherDeflector extends TileEntity implements ITickable
{

	//0 = kill storms, 1 = prevent block damage
	public int mode = 0;
	
	public static final int MODE_KILLSTORMS = 0;
	public static final int MODE_NOBLOCKDAMAGE = 1;

	@Override
	public void onLoad()
	{
		super.onLoad();
		maintainBlockDamageDeflect();
	}

	@Override
    public void update()
    {
    	if (!world.isRemote)
    	{
    		if (mode == MODE_KILLSTORMS && world.getTotalWorldTime() % 100L == 0L)
    		{
				WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
				if (wm != null)
				{
					List<WeatherObject> storms = new ArrayList<WeatherObject>(wm.getWeatherSystems(new Vec3(getPos().getX(), getPos().getY(), getPos().getZ()), ConfigStorm.storm_deflector_range, ConfigStorm.storm_deflector_minimum_stage, Integer.MAX_VALUE).keySet());
					WeatherObject wo;
					int size = storms.size();
						
					for (int i = 0; i < size; i++)
					{
						wo = storms.get(i);
						wo.front.removeWeatherObject(wo.getUUID());
						PacketWeatherObject.remove(wm.getDimension(), wo);;
					}
					storms.clear();
				}
			}

			if (world.getTotalWorldTime() % 20 == 0)
				maintainBlockDamageDeflect();
    	}
    }
	
    public void maintainBlockDamageDeflect()
    {
		WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
		
		if (wm != null)
		{
			long pos = getPos().toLong();
			switch(mode)
			{
				case 1:
					if (wm.getListWeatherBlockDamageDeflector().contains(pos))
						wm.getListWeatherBlockDamageDeflector().remove(pos);
					break;
				default:
					if (!wm.getListWeatherBlockDamageDeflector().contains(pos))
						wm.getListWeatherBlockDamageDeflector().add(pos);
			}
		}
	}

	public void rightClicked(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		cycleMode();

		switch (mode)
		{
			case MODE_NOBLOCKDAMAGE:
				playerIn.sendMessage(new TextComponentString("Deflection Mode: Protect Blocks"));
				maintainBlockDamageDeflect();
				break;
			default:
				playerIn.sendMessage(new TextComponentString("Deflection Mode: Destroy Storms"));
		}
	}

	public void cycleMode()
	{
		mode = (mode + 1) % 2;
	}

    public NBTTagCompound writeToNBT(NBTTagCompound var1)
    {
    	var1.setInteger("mode", mode);
        return super.writeToNBT(var1);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
		mode = var1.getInteger("mode");
    }

	@Override
	public void invalidate()
	{
		super.invalidate();

		if (!world.isRemote)
		{
			//always try to remove, incase they removed the block before the tick code could run after switching mode
			WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
			wm.getListWeatherBlockDamageDeflector().remove(getPos().toLong());
		}
	}
}
