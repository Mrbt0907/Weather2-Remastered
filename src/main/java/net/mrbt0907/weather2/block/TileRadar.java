package net.mrbt0907.weather2.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.api.weather.IWeatherDetectable;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.client.gui.elements.GuiRadarObject;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class TileRadar extends TileEntity implements ITickable
{
	//Radar Settings
	private int tier = 0;
	private int pingLength = 60;
	private int pingMaxLength = 60;
	
	public int pingRate = 200;
	public double pingRange = 1024.0D;
	public int showType = 0;
	public boolean showRating = false;
	public boolean showEF = false;
	public boolean showDirection = false;
	public boolean showWindSpeed = false;
	public boolean liveRadar = false;
	
	//Render Variables
	public float fadeRate = 0.05F;
	public float renderAlpha = 1.0F;
	public float renderRange = 2.0F;
	
	//Other Variables
	public float smoothAngle = 0;
	public float smoothSpeed = 0;
	public float smoothAngleRotationalVel = 0;
	public float smoothAngleRotationalVelAccel = 0;
	public float smoothAngleAdj = 0.1F;
	public float smoothSpeedAdj = 0.1F;
	
	public IWeatherDetectable system = null;
	public List<GuiRadarObject> systems = new ArrayList<>();
	
	public TileRadar() {}
	
	public TileRadar(int tier)
	{
		this.tier = tier;
		refresh();
	}
	
	@Override
    public void update()
    {
    	if (world.isRemote)
    		tickClient();
    }

	public int getTier()
	{
		return tier;
	}
	
	private void refresh()
	{
		showEF = ConfigStorm.enable_ef_scale ? true : tier > 1;
		
		switch(tier)
		{
			case 1:
				pingRate = 150;
				pingRange = ConfigMisc.doppler_radar_range;
				renderRange = 2.0F;
				liveRadar = true;
				showRating = true;
				showDirection = true;
				showWindSpeed = true;
				liveRadar = true;
				break;
			case 2:
				pingRate = 100;
				pingRange = ConfigMisc.pulse_doppler_radar_range;
				renderRange = 3.0F;
				liveRadar = true;
				showRating = true;
				showDirection = false;
				showWindSpeed = false;
				liveRadar = false;
				break;
			default:
				pingRate = 200;
				pingRange = ConfigMisc.radar_range;
				renderRange = 1.0F;
				liveRadar = false;
				showType = 0;
				showRating = false;
				showDirection = false;
				showWindSpeed = false;
				liveRadar = false;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient()
	{
		if (world.getTotalWorldTime() % pingRate == 0)
		{
			if (net.mrbt0907.weather2.client.event.ClientTickHandler.clientConfigData.debug_mode_radar)
			{
				WeatherObject system = net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager.getClosestWeather(new Vec3(getPos().getX(), getPos().getY(), getPos().getZ()), pingRange);
				this.system = (system == null || system.isDead ? null : system);
			}
			else
				system = null;
				
			
			if (net.mrbt0907.weather2.client.event.ClientTickHandler.clientConfigData.debug_mode_radar)
			{
				systems.clear();
				for (FrontObject front : net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager.getFronts())
					if (!front.isGlobal())
					{
						if (Maths.distanceSq(getPos().getX(), getPos().getY(), getPos().getZ(), front.pos.posX, getPos().getY(), front.pos.posZ) <= pingRange)
						{
							systems.add(new GuiRadarObject(front));
							front.getWeatherObjects().forEach(so -> {if (Maths.distanceSq(getPos().getX(), getPos().getY(), getPos().getZ(), so.pos.posX, getPos().getY(), so.pos.posZ) <= pingRange) systems.add(new GuiRadarObject(so));});
						}
					}
					else
						front.getWeatherObjects().forEach(so -> {if (Maths.distanceSq(getPos().getX(), getPos().getY(), getPos().getZ(), so.pos.posX, getPos().getY(), so.pos.posZ) <= pingRange) systems.add(new GuiRadarObject(so));});
			}
			else
			{
				systems.clear();
				for (FrontObject front : net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager.getFronts())
					if (!front.isGlobal())
					{
						if (Maths.distanceSq(getPos().getX(), getPos().getY(), getPos().getZ(), front.pos.posX, getPos().getY(), front.pos.posZ) <= pingRange)
						{
							systems.add(new GuiRadarObject(front));
							front.getWeatherObjects().forEach(so -> {if (!so.type.equals(Type.CLOUD) && Maths.distanceSq(getPos().getX(), getPos().getY(), getPos().getZ(), so.pos.posX, getPos().getY(), so.pos.posZ) <= pingRange) systems.add(new GuiRadarObject(so));});
						}
					}
					else
						front.getWeatherObjects().forEach(so -> {if (!so.type.equals(Type.CLOUD) && Maths.distanceSq(getPos().getX(), getPos().getY(), getPos().getZ(), so.pos.posX, getPos().getY(), so.pos.posZ) <= pingRange) systems.add(new GuiRadarObject(so));});
			}
			
			pingLength = pingMaxLength;
			renderAlpha = 1.0F;
		}

		if (pingLength == 0 && renderAlpha > 0.1F)
			renderAlpha -= fadeRate;
		
		if (pingLength > 0)
			pingLength--;
	}
	
	@SideOnly(Side.CLIENT)
	public double getPingRange(int tier)
	{
		return tier == 1 ? net.mrbt0907.weather2.client.event.ClientTickHandler.clientConfigData.doppler_radar_range : tier == 2 ? net.mrbt0907.weather2.client.event.ClientTickHandler.clientConfigData.pulse_doppler_radar_range : net.mrbt0907.weather2.client.event.ClientTickHandler.clientConfigData.radar_range;
	}
	
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
    	tag = super.writeToNBT(tag);
    	tag.setInteger("tier", tier);
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        if (tag.hasKey("tier"))
        {
        	tier = tag.getInteger("tier");
        	fadeRate = tier == 0 ? 0.0035F : tier == 1 ? 0.001F : 0.0005F;
        }
        refresh();
    }
}
