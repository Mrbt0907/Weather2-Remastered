package net.mrbt0907.weather2.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.server.weather.WeatherManagerServer;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class BlockNewSensor extends BlockMachine
{
	public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
	
	/**Determines what the sensor will scan<br>0 - Scan Stage<br>1 - Scan Humidity<br>2 - Scan Rain<br>3 - Scan Temperature<br>4 - Scan Wind*/
	private int scanType;
	
    public BlockNewSensor(Material material, int scanType)
    {
    	super(material);
        setCreativeTab(Weather2.TAB);      
        this.scanType = scanType;
        setDefaultState(blockState.getBaseState().withProperty(POWER, Integer.valueOf(0)));
        setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
    	
    	if (world.isRemote) return;
    	int power = 0;
    	WeatherManagerServer manager = ServerTickHandler.dimensionSystems.get(world.provider.getDimension());
    	
    	if (manager != null) 
    	{
    		WeatherObject so = manager.getWorstWeather(new Maths.Vec3(pos.getX(), pos.getY(), pos.getZ()), ConfigMisc.sensor_scan_range, Stage.RAIN.getStage(), Integer.MAX_VALUE);
    		
    		switch(scanType)
			{
				case 0:
					if (so != null) 
		    		{
		    			if (so instanceof StormObject)
		    				power = MathHelper.clamp(((StormObject)so).stage, 0, 15);
		    			else if (so instanceof SandstormObject)
		    				power = 5;
		    			
		    			
		    		}
	    			break;
				case 1:
					power = (int) (WeatherUtil.getHumidity(world, pos) * 15.0F);
					break;
				case 2:
						List<WeatherObject> wos = manager.getWeatherObjects();
						power = 0;
						
						for (WeatherObject wo : wos)
						{
							if (wo instanceof StormObject && ((StormObject) wo).hasDownfall() && wo.pos.distance(new Maths.Vec3(pos)) < wo.size)
							{
								power = 15;
								break;
							}
						}
					break;
				case 3:
					power = (int) MathHelper.clamp(WeatherUtil.getTemperature(world, pos) * 15.0F * 0.7F, 0.0F, 15.0F);
					break;
				case 4:
					power = (int)Math.min(((manager.windManager.windSpeed > manager.windManager.windSpeedGust ? manager.windManager.windSpeed : manager.windManager.windSpeedGust) * 0.072F) * 15.0F, 15.0F);
					break;
				case 5:
					power = (int) MathHelper.clamp((WeatherUtil.getPressure(world, pos) - 900.0F) * 0.12F, 0.0F, 15.0F);
					break;
			}
            world.setBlockState(pos, state.withProperty(POWER, MathHelper.clamp(power, 0, 15)), 3);
    	}
        
        world.scheduleBlockUpdate(pos, this, 100, 1);
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos,
    		EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
    		EntityLivingBase placer) {
    	worldIn.scheduleBlockUpdate(pos, this, 10, 1);
    	return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return ((Integer)blockState.getValue(POWER)).intValue();
    }
    
    @Override
    public boolean canProvidePower(IBlockState state)
    {
        return true;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(POWER, Integer.valueOf(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return ((Integer)state.getValue(POWER)).intValue();
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {POWER});
    }
}
