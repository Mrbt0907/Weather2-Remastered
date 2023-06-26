package net.mrbt0907.weather2.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.api.WindReader;
import net.mrbt0907.weather2.util.WeatherUtilEntity;

public class TileWindVane extends TileEntity implements ITickable
{
	
	//since client receives data every couple seconds, we need to smooth out everything for best visual
	
	public float smoothAngle = 0;
	public float smoothSpeed = 0;
	
	public float smoothAngleRotationalVel = 0;
	public float smoothAngleRotationalVelAccel = 0;
	
	public float smoothAngleAdj = 0.1F;
	public float smoothSpeedAdj = 0.1F;
	
	public boolean isOutsideCached = false;

	@Override
    public void update()
    {
    	if (world.isRemote) {
    		
    		if (world.getTotalWorldTime() % 40 == 0) {
    			isOutsideCached = WeatherUtilEntity.isPosOutside(world, new Vec3(getPos().getX()+0.5F, getPos().getY()+0.5F, getPos().getZ()+0.5F));
    		}
    		
    		if (isOutsideCached)
    		{	
	    		float targetAngle = WindReader.getWindAngle(world, new Vec3(getPos().getX(), getPos().getY(), getPos().getZ()));
	    		float windSpeed = WindReader.getWindSpeed(world, new Vec3(getPos().getX(), getPos().getY(), getPos().getZ()));
	    		
	    		if (smoothAngle > 180) smoothAngle-=360;
	    		if (smoothAngle < -180) smoothAngle+=360;
	    		
	    		float bestMove = Maths.wrapDegrees(targetAngle - smoothAngle);
	    		
	    		//float diff = ((targetAngle + 360 + 180) - (smoothAngle + 360 + 180));
	    		
	    		smoothAngleAdj = windSpeed;//0.2F;
	    		
	    		if (Math.abs(bestMove) < 180/* - (angleAdjust * 2)*/) {
	    			float realAdj = smoothAngleAdj;//Math.max(smoothAngleAdj, Math.abs(bestMove));
	    			
	    			if (realAdj * 2 > windSpeed) {
		    			if (bestMove > 0) smoothAngleRotationalVelAccel -= realAdj;
		    			if (bestMove < 0) smoothAngleRotationalVelAccel += realAdj;
	    			}
	    			
	    			if (smoothAngleRotationalVelAccel > 0.3 || smoothAngleRotationalVelAccel < -0.3) {
	    				smoothAngle += smoothAngleRotationalVelAccel;
	    			} else {
	    				//smoothAngleRotationalVelAccel *= 0.9F;
	    			}
	    			
	    			//smoothAngle += smoothAngleRotationalVelAccel;
	    			
	    			smoothAngleRotationalVelAccel *= 0.80F;
	    			
	    			//System.out.println("diff: " + diff);
	    			
	    			//System.out.println("smoothAngle: " + smoothAngle + " - smoothAngleRotationalVel: " + smoothAngleRotationalVel + " - smoothAngleRotationalVelAccel: " + smoothAngleRotationalVelAccel);
	    		}
    		}
    	}
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
    	return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 3, getPos().getZ() + 1);
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
