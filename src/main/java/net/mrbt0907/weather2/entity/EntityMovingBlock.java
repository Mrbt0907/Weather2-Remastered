package net.mrbt0907.weather2.entity;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.weather.storm.StormObject;

public class EntityMovingBlock extends Entity implements IEntityAdditionalSpawnData
{	
	public Block block;
	public IBlockState state;
	public TileEntity tileentity;
	public Material material;
	public int metadata;
	//mode 0 = use gravity
	public int mode;
	public int age;
	public boolean noCollision;
	public boolean collideFalling = false;
	public double vecX;
	public double vecY;
	public double vecZ;
	public int gravityDelay;
	
	public EntityMovingBlock(World var1)
	{
		super(var1);
		mode = 1;
		age = 0;
		state = Blocks.STONE.getDefaultState();
		block = state.getBlock();
		noCollision = true;
		gravityDelay = 60;
	}

	public EntityMovingBlock(World world, int x, int y, int z, IBlockState state, StormObject storm)
	{
		super(world);
		mode = 1;
		age = 0;
		noCollision = false;
		gravityDelay = 60;
		setSize(0.9F, 0.9F);
		setPosition(x + 0.5D, y + 0.5D, z + 0.5D);
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		prevPosX = (x + 0.5F);
		prevPosY = (y + 0.5F);
		prevPosZ = (double)(z + 0.5F);
		metadata = state.getBlock().getMetaFromState(state);
		material = state.getMaterial();
		this.state = state;
		block = state.getBlock();
	}

	@Override
	public boolean isInRangeToRenderDist(double var1)
	{
		return var1 < 256D * 256D;
	}

	@Override
	public boolean canTriggerWalking() {return false;}

	@Override
	public void entityInit() {}

	@Override
	public boolean canBePushed(){return !this.isDead;}
	
	@Override
	public boolean canBeCollidedWith()
	{
		return !this.isDead && !this.noCollision;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		//new kill off when distant method
		if (!world.isRemote)
			if (world.getClosestPlayer(this.posX, 50, this.posZ, 512, false) == null)
				setDead();
		
		if (block.equals(Blocks.AIR))
			setDead();
		else
		{
			++age;

			if (age > gravityDelay)
			{
				mode = 0;

				if (tileentity == null && ConfigGrab.Storm_Tornado_rarityOfDisintegrate != -1 && rand.nextInt((ConfigGrab.Storm_Tornado_rarityOfDisintegrate + 1) * 20) == 0)
					setDead();
			}

			vecX++;
			vecY++;
			vecZ++;
			
			if (mode == 1)
			{
				fallDistance = 0.0F;
				collidedHorizontally = false;
			}

			Vec3d var1 = new Vec3d(posX, posY, posZ);
			Vec3d var2 = new Vec3d(posX + motionX * 1.3D, posY + motionY * 1.3D, posZ + motionZ * 1.3D);
			RayTraceResult var3 = world.rayTraceBlocks(var1, var2);
			var2 = new Vec3d(posX + motionX * 1.3D, posY + motionY * 1.3D, posZ + motionZ * 1.3D);

			if (var3 != null)
				var2 = new Vec3d(var3.hitVec.x, var3.hitVec.y, var3.hitVec.z);

			Entity var4 = null;
			List<Entity> var5 = new ArrayList<Entity>(), entities = new ArrayList<Entity>();

			if (age > gravityDelay / 4)
				for (Entity entity : entities)
					if (!entity.equals(this) && entity.getDistance(posX, posY, posZ) < height * 2.0F)
						var5.add(entity);

			double var6 = 0.0D;
			int var8;
			int var9;
			int var11;

			for (Entity var10 : var5)
			{
				if (!(var10 instanceof EntityMovingBlock) && var10.canBeCollidedWith() && this.canEntityBeSeen(var10))
				{
					if (!(var10 instanceof EntityPlayer) || !((EntityPlayer)var10).capabilities.isCreativeMode) {
						var10.motionX = this.motionX / 1.5D;
						var10.motionY = this.motionY / 1.5D;
						var10.motionZ = this.motionZ / 1.5D;
					}
					
					if (ConfigGrab.grabbed_blocks_hurt && Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) > 0.4F) {
						//System.out.println("damaging with block: " + var10);
						
						DamageSource ds = DamageSource.causeThrownDamage(this, this);
						ds.damageType = "wm.movingblock";
						try
						{
							var10.attackEntityFrom(ds, 4);
						}
						catch(Exception e)
						{
							Weather2.error(e);
						}
					}
				}

				if (var10.canBeCollidedWith() && !this.noCollision)
				{
					if (var10.canBePushed())
					{
						var10.getDistanceSq(this);

						if (this.isBurning())
						{
							var10.setFire(15);
						}

						if (block == Blocks.CACTUS)
						{
							try
							{
								var10.attackEntityFrom(DamageSource.causeThrownDamage(this, this), 1);
							}
							catch(Exception e)
							{
								Weather2.error(e);
							}
							
						}
						else if (this.material == Material.LAVA)
						{
							var10.setFire(15);
						}
						else
						{
							var9 = MathHelper.floor(this.posX);
							var11 = MathHelper.floor(this.posY);
							int var12 = MathHelper.floor(this.posZ);
							BlockPos pos = new BlockPos(var9, var11, var12);
							IBlockState state = world.getBlockState(pos);
							block.onEntityCollision(this.world, pos, state, var10);
						}
					}

					float var16 = 0.3F;
					AxisAlignedBB var19 = var10.getEntityBoundingBox().grow((double)var16, (double)var16, (double)var16);
					RayTraceResult var13 = var19.calculateIntercept(var1, var2);

					if (var13 != null)
					{
						double var14 = var1.distanceTo(var13.hitVec);

						if (var14 < var6 || var6 == 0.0D)
						{
							var4 = var10;
							var6 = var14;
						}
					}
				}
			}

			if (var4 != null)
			{
				var3 = new RayTraceResult(var4);
			}

			if (var3 != null && var3.entityHit == null && this.mode == 0)
			{
				var8 = var3.getBlockPos().getX();
				int var17 = var3.getBlockPos().getY();
				var9 = var3.getBlockPos().getZ();

				//0
				if (var3.sideHit == EnumFacing.DOWN)
				{
					--var17;
				}

				//1
				if (var3.sideHit == EnumFacing.UP)
				{
					++var17;
				}

				//2
				if (var3.sideHit == EnumFacing.SOUTH)
				{
					--var9;
				}

				//3
				if (var3.sideHit == EnumFacing.NORTH)
				{
					++var9;
				}

				//4
				if (var3.sideHit == EnumFacing.WEST)
				{
					--var8;
				}

				//5
				if (var3.sideHit == EnumFacing.EAST)
				{
					++var8;
				}

					if (var3.sideHit != EnumFacing.DOWN && !this.collideFalling)
					{
						if (!this.collideFalling)
						{
							this.collideFalling = true;
							this.posX = MathHelper.floor(posX);
							this.posZ = MathHelper.floor(posZ);
							//this.posZ = (double)((int)(this.posZ + 0.0D));
							this.setPosition(this.posX, this.posY, this.posZ);
							this.motionX = 0.0D;
							this.motionZ = 0.0D;
						}
					}
					else
					{
						this.blockify(var8, var17, var9, var3.sideHit);
					}


				return;
			}

			float var18 = 0.98F;

				this.motionY -= 0.05000000074505806D;

			this.motionX *= (double)var18;
			this.motionY *= (double)var18;
			this.motionZ *= (double)var18;
			var11 = (int)(this.posX + this.motionX * 5.0D);
			byte var20 = 50;
			int var21 = (int)(this.posZ + this.motionZ * 5.0D);

			if (!this.world.isBlockLoaded(new BlockPos(var11, var20, var21)))
				this.setDead();
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;

			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	public boolean canEntityBeSeen(Entity par1Entity)
	{
		return this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ), new Vec3d(par1Entity.posX, par1Entity.posY + (double)par1Entity.getEyeHeight(), par1Entity.posZ)) == null;
	}

	private void blockify(int x, int y, int z, EnumFacing var4)
	{
		//TODO: this was the only thing killing off moving blocks on client side, syncing is broken server to client?
		if (world.isRemote) return;
			setDead();

		try
		{		
			BlockPos pos = new BlockPos(x, y, z);
			IBlockState state = Weather2.getChunkUtil(world).getBlockState(world, pos);
	
			if (tileentity != null || ConfigGrab.Storm_Tornado_rarityOfBreakOnFall > 0 && rand.nextInt(ConfigGrab.Storm_Tornado_rarityOfBreakOnFall + 1) != 0)
				if (!state.getMaterial().isLiquid() && Weather2.getChunkUtil(world).isValidPos(world, y))
					Weather2.getChunkUtil(world).setBlockState(world, x, y, z, this.state);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		return false;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt)
	{
		nbt.setString("Tile", Block.REGISTRY.getNameForObject(block).toString());
		nbt.setByte("Metadata", (byte)metadata);
		NBTTagCompound var2 = new NBTTagCompound();

		if (tileentity != null)
			tileentity.writeToNBT(var2);

		nbt.setTag("TileEntity", var2);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt)
	{
		block = (Block)Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("Tile")));
		metadata = nbt.getByte("Metadata") & 15;
		state = block.getStateFromMeta(metadata);
		tileentity = null;

		if (block instanceof BlockContainer)
		{
			tileentity = ((BlockContainer)this.block).createNewTileEntity(world, metadata);
			NBTTagCompound var2 = nbt.getCompoundTag("TileEntity");
			tileentity.readFromNBT(var2);
		}
	}

	@Override
	public void writeSpawnData(ByteBuf data)
	{
		String str = "blank";
		if (block != null && Block.REGISTRY.getNameForObject(block) != null)
			str = Block.REGISTRY.getNameForObject(block).toString();
		ByteBufUtils.writeUTF8String(data, str);
		data.writeInt(metadata);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void readSpawnData(ByteBuf data)
	{
		String str = ByteBufUtils.readUTF8String(data);
		if (!str.equals("blank"))
		{
			block = Block.REGISTRY.getObject(new ResourceLocation(str));
			metadata = data.readInt();
		}
		else
		{
			block = Blocks.STONE;
			metadata = 0;
		}

		state = block.getStateFromMeta(metadata);
		material = state.getMaterial();
	}
}
