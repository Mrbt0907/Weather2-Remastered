package net.mrbt0907.weather2.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherEnum;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import CoroUtil.util.Vec3;

public class TileRadar extends TileEntity implements ITickable
{
	
	//since client receives data every couple seconds, we need to smooth out everything for best visual
	
	public float smoothAngle = 0;
	public float smoothSpeed = 0;
	
	public float smoothAngleRotationalVel = 0;
	public float smoothAngleRotationalVelAccel = 0;
	
	public float smoothAngleAdj = 0.1F;
	public float smoothSpeedAdj = 0.1F;
	
	public WeatherObject lastTickStormObject = null;
	
	public List<WeatherObject> storms = new ArrayList<>();
	
	//public MapHandler mapHandler;
	
	@Override
    public void update()
    {
    	if (world.isRemote) {
    		if (world.getTotalWorldTime() % 200 == 0 || storms.size() == 0) {
    			
    			lastTickStormObject = ClientTickHandler.weatherManager.getClosestStorm(new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ()), 1024, WeatherEnum.Type.THUNDER);
   
    			if (ConfigMisc.debug_mode_radar) {
    				//storms.clear();
					List<WeatherObject> listAdd = new ArrayList<>();
    				for (WeatherObject wo : ClientTickHandler.weatherManager.getWeatherObjects()) {
    					//if (wo instanceof StormObject && !((StormObject) wo).isCloudlessStorm()) {
							listAdd.add(wo);
						//}
					}
					storms = listAdd;
				} else {
					storms = ClientTickHandler.weatherManager.getClosestStorms(new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ()), 1024);
				}
    		}
    	}
    }

    public NBTTagCompound writeToNBT(NBTTagCompound var1)
    {
        return super.writeToNBT(var1);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);

    }
}
