package net.mrbt0907.weather2.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.mrbt0907.weather2.util.Maths;
import CoroUtil.api.weather.IWindHandler;
import CoroUtil.entity.EntityThrowableUsefull;
import CoroUtil.util.Vec3;

public class EntityIceBall extends EntityThrowableUsefull implements IWindHandler
{
	public int ticksInAir;
	

	public EntityIceBall(World world)
	{
		super(world);
		ticksMaxAlive = Integer.MAX_VALUE;
		float size = Maths.random(0.08F, 0.3F);
		setSize(size, size);
	}

	public EntityIceBall(World world, EntityLivingBase entityliving)
	{
		super(world, entityliving);
		float f = 0.4F;
		float size = Maths.random(0.1F, 0.6F);
		setSize(size, size);
        this.motionX = (double)(-Maths.fastSin(-this.rotationYaw / 180.0F * (float)Math.PI) * Maths.fastCos(-this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionZ = (double)(Maths.fastCos(-this.rotationYaw / 180.0F * (float)Math.PI) * Maths.fastCos(-this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionY = (double)(-Maths.fastSin((-this.rotationPitch + this.func_70183_g()) / 180.0F * (float)Math.PI) * f);
		ticksMaxAlive = Integer.MAX_VALUE;
		
	}

	public EntityIceBall(World world, double d, double d1, double d2)
	{
		super(world, d, d1, d2);
		ticksMaxAlive = Integer.MAX_VALUE;
	}
	
	@Override
	public void onUpdate()
    {
		super.onUpdate();
		//gravity
		if (motionY > -3.0D)
			motionY -= 0.1D;
		
		if (!this.world.isRemote)
        {
			if (collided || isInWater())
				setDead();
        }
    }
	
	@Override
	protected float getGravityVelocity()
	{
		return 0F;
	}
	
	@Override
	public RayTraceResult tickEntityCollision(Vec3 vec3, Vec3 vec31)
	{
		RayTraceResult movingobjectposition = null;		
        Entity entity = null, target = null;
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().grow(motionX, motionY, motionZ).grow(0.5D, 1D, 0.5D));
        EntityLivingBase owner = getThrower();
        
        if (ticksInAir >= 4)
	        for (int j = 0; j < list.size(); ++j)
	        {
	            target = list.get(j);
	
	            if (target.canBeCollidedWith() && target != owner)
	            {
	            	entity = target;
	                break;
	            }
	        }

        if (entity != null)
            movingobjectposition = new RayTraceResult(entity);
        
        return movingobjectposition;
	}

	@Override
	protected void onImpact(RayTraceResult movingobjectposition)
	{
		if (!world.isRemote)
		{
			if (movingobjectposition.entityHit != null)
			{
				float damage = width * 10.0F * (float) (-motionY / 3.0F);
				movingobjectposition.entityHit.attackEntityFrom(DamageSource.FALLING_BLOCK, damage);
			}
			
			world.playSound(null, new BlockPos(posX, posY, posZ), SoundEvents.BLOCK_STONE_STEP, SoundCategory.AMBIENT, 3F, 5F - width * 5.0F);//0.2F + world.rand.nextFloat() * 0.1F);
			setDead();
		}
	}
	
	@Override
	public void setDead()
	{
		super.setDead();
	}
	

	@Override
	public float getWindWeight()
	{
		return 4;
	}

	@Override
	public int getParticleDecayExtra()
	{
		return 0;
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {}
}
