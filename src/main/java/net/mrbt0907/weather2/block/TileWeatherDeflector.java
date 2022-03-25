package net.mrbt0907.weather2.block;

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
import net.mrbt0907.weather2.server.weather.WeatherSystemServer;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import CoroUtil.util.Vec3;

public class TileWeatherDeflector extends TileEntity implements ITickable
{

	//0 = kill storms, 1 = prevent block damage
	public int mode = 0;

	public static int MODE_KILLSTORMS = 0;
	public static int MODE_NOBLOCKDAMAGE = 1;

	@Override
	public void onLoad() {
		super.onLoad();
		maintainBlockDamageDeflect();
	}

	@Override
    public void update()
    {
    	if (!world.isRemote)
    	{
    		if (world.getTotalWorldTime() % 100 == 0 && mode == MODE_KILLSTORMS)
    		{	
					WeatherSystemServer wm = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
					if (wm != null)
					{
						List<WeatherObject> storms = wm.getClosestStorms(new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ()), ConfigStorm.storm_deflector_range, ConfigStorm.storm_deflector_minimum_stage, 10);
						WeatherObject wo;
						int size = storms.size();
						
						for (int i = 0; i < size; i++)
						{
							wo = storms.get(i);
							wm.removeStormObject(wo.getUUID());
							PacketWeatherObject.remove(wm.getDimension(), wo);;
						}
					}
			}

			if (world.getTotalWorldTime() % 20 == 0)
				maintainBlockDamageDeflect();
    	}
    }

    public void maintainBlockDamageDeflect() {
		WeatherSystemServer wm = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
		if (wm != null) {
			if (mode == MODE_KILLSTORMS) {
				if (wm.getListWeatherBlockDamageDeflector().contains(getPos().toLong())) {
					wm.getListWeatherBlockDamageDeflector().remove(getPos().toLong());
				}
			} else if (mode == MODE_NOBLOCKDAMAGE) {
				if (!wm.getListWeatherBlockDamageDeflector().contains(getPos().toLong())) {
					wm.getListWeatherBlockDamageDeflector().add(getPos().toLong());
				}
			}
		}
	}

	public void rightClicked(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		cycleMode();

		String modeMsg = "";

		if (mode == MODE_KILLSTORMS) {
			modeMsg = "Kill thunderstorms and deadlier";
		} else if (mode == MODE_NOBLOCKDAMAGE) {
			modeMsg = "Prevent block damage only";
			maintainBlockDamageDeflect();
		}

		playerIn.sendMessage(new TextComponentString("Weather Deflector set to mode: " + modeMsg));
	}

	public void cycleMode() {
		mode++;

		if (mode > MODE_NOBLOCKDAMAGE) {
			mode = 0;
		}
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
	public void invalidate() {
		super.invalidate();

		if (!world.isRemote) {
			//always try to remove, incase they removed the block before the tick code could run after switching mode
			WeatherSystemServer wm = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
			wm.getListWeatherBlockDamageDeflector().remove(getPos().toLong());
		}

	}
}
