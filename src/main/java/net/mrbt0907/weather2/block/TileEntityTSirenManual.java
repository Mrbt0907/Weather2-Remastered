package net.mrbt0907.weather2.block;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.client.sound.MovingSoundEX;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.entity.AI.EntityAIMoveIndoorsStorm;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilSound;

public class TileEntityTSirenManual extends TileEntity implements ITickable
{
    private MovingSoundEX sound;
    
    @Override
    public void update()
    {
    	int meta = BlockRegistry.emergency_siren.getMetaFromState(this.world.getBlockState(this.getPos()));
    	
    	if (BlockSiren.isEnabled(meta))
    	{
            if (world.isRemote)
                tickClient();
            else
            	tickAlert();
    	}
    	else
    	{
    		if (world.isRemote && sound != null)
    		{
	    		sound.setDone();
	    		sound = null;
    		}
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public void tickClient()
    {
    	if (sound == null || sound.isDonePlaying() || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound))
    	{
    		Vec3 pos = new Vec3(getPos().getX(), getPos().getY(), getPos().getZ());
        	sound = WeatherUtilSound.playForcedSound(SoundRegistry.siren, SoundCategory.RECORDS, pos, (float) ConfigVolume.sirens, 1.0F, 356.0F, true, true);
    	}
    }
    
    private void tickAlert()
    {
        if (!this.world.isRemote && this.world.getTotalWorldTime() % 10L == 0L)
        {
            double d0 = 120;
            
            int posX = this.pos.getX();
            int posY = this.pos.getY();
            int posZ = this.pos.getZ();
            AxisAlignedBB axisalignedbb = (new AxisAlignedBB((double)posX, (double)posY, (double)posZ, (double)(posX + 1),  (double)(posY + 1), (double)(posZ + 1))).grow(d0);
            List<EntityVillager> list = world.<EntityVillager>getEntitiesWithinAABB(EntityVillager.class, axisalignedbb);

            for (EntityVillager entity : list)
            {
            	Iterator<EntityAITasks.EntityAITaskEntry> iter = entity.tasks.taskEntries.iterator();
            	while (iter.hasNext())
            	{
            		EntityAITasks.EntityAITaskEntry entry = iter.next();
            		EntityAIBase ai = entry.action;
            		
            		if (ai instanceof EntityAIMoveIndoorsStorm) {((EntityAIMoveIndoorsStorm) ai).isAlert = true;}
            	}
            }
        }
    }
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
    	return (oldState.getBlock() != newState.getBlock());
    }
}
