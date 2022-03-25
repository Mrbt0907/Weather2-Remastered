package net.mrbt0907.weather2.block;

import CoroUtil.util.CoroUtilPhysics;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigSand;
import net.mrbt0907.weather2.entity.AI.EntityAIMoveIndoorsStorm;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.util.WeatherUtilSound;
import net.mrbt0907.weather2.weather.storm.WeatherEnum;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import CoroUtil.util.Vec3;

import java.util.Iterator;
import java.util.List;

public class TileSiren extends TileEntity implements ITickable
{
    public long lastPlayTime = 0L;

    @Override
    public void update()
    {
    	int meta = BlockRegistry.emergency_siren.getMetaFromState(this.world.getBlockState(this.getPos()));
    	
    	if (BlockSiren.isEnabled(meta))    
            if (world.isRemote)
                tickClient();
            else
            	tickAlert();
    }
    
    @SideOnly(Side.CLIENT)
    public void tickClient() {
    	
    	if (this.lastPlayTime < System.currentTimeMillis())
        {
            Vec3 pos = new Vec3(getPos().getX(), getPos().getY(), getPos().getZ());

    		WeatherObject so = ClientTickHandler.weatherManager.getWorstStorm(pos, ConfigMisc.siren_scan_range, WeatherEnum.Type.TORNADO);
    		
            if (so != null)
            {
                this.lastPlayTime = System.currentTimeMillis() + 13000L;
                WeatherUtilSound.playNonMovingSound(pos, "streaming.siren", 1.0F, 1.0F, 120);
            } else {
                if (!ConfigSand.disable_darude_sandstorm_plz) {
                    SandstormObject sandstorm = ClientTickHandler.weatherManager.getClosestSandstormByIntensity(pos);

                    if (sandstorm != null) {
                        List<Vec3> points = sandstorm.getSandstormAsShape();

                        float distMax = 75F;

                        //double scale = sandstorm.getSandstormScale();
                        boolean inStorm = CoroUtilPhysics.isInConvexShape(pos, points);
                        double dist = Math.min(distMax, CoroUtilPhysics.getDistanceToShape(pos, points));

                        if (inStorm || dist < distMax) {
                            String soundToPlay = "siren_sandstorm_5_extra";
                            if (getWorld().rand.nextBoolean()) {
                                soundToPlay = "siren_sandstorm_6_extra";
                            }

                            float distScale = Math.max(0.1F, 1F - (float) ((dist) / distMax));
                            if (inStorm) distScale = 1F;

                            this.lastPlayTime = System.currentTimeMillis() + 15000L;//WeatherUtilSound.soundToLength.get(soundToPlay) - 500L;
                            WeatherUtilSound.playNonMovingSound(pos, "streaming." + soundToPlay, 1F, distScale, distMax);
                        }
                    }
                }
            }
        }
    }
    
    private void tickAlert()
    {
        if (!this.world.isRemote && this.world.getTotalWorldTime() % 20 == 0)
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
}
