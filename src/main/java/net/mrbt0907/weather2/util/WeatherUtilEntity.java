package net.mrbt0907.weather2.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.mrbt0907.weather2.api.WeatherUtilData;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.weather.WindManager;
import CoroUtil.api.weather.IWindHandler;
import CoroUtil.util.CoroUtilEntOrParticle;
import CoroUtil.util.Vec3;
import extendedrenderer.particle.entity.EntityRotFX;

public class WeatherUtilEntity {
	//old non multiplayer friendly var, needs resdesign where this is used
	public static int playerInAirTime = 0;

	
	public static float getWeight(Object entity1) {
		return getWeight(entity1, false);
	}
	
	public static float getWeight(Object entity1, boolean forTornado)
	{
		World world = CoroUtilEntOrParticle.getWorld(entity1);

		//fixes issue #270
		if (world == null) {
			return 1F;
		}

		if (entity1 instanceof IWindHandler) {
			return ((IWindHandler) entity1).getWindWeight();
		}
		
		if (entity1 instanceof EntityMovingBlock)
		{
			return 1F + ((float)((EntityMovingBlock) entity1).age / 200);
		}

		if (entity1 instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entity1;
			if (player.onGround || player.handleWaterMovement())
			{
				playerInAirTime = 0;
			}
			else
			{
				//System.out.println(playerInAirTime);
				playerInAirTime++;
			}

			
			if (((EntityPlayer) entity1).capabilities.isCreativeMode) return 99999999F;
			
			int extraWeight = 0;
			
			if (((EntityPlayer)entity1).inventory != null && !(((EntityPlayer)entity1).inventory.armorInventory.get(2).isEmpty())
					&& ((EntityPlayer)entity1).inventory.armorInventory.get(2).getItem() == Items.IRON_CHESTPLATE)
			{
				extraWeight = 2;
			}

			if (((EntityPlayer)entity1).inventory != null && !(((EntityPlayer)entity1).inventory.armorInventory.get(2).isEmpty())
					&& ((EntityPlayer)entity1).inventory.armorInventory.get(2).getItem() == Items.DIAMOND_CHESTPLATE)
			{
				extraWeight = 4;
			}

			if (forTornado) {
				return 4.5F + extraWeight + ((float)(playerInAirTime / 400));
			} else {
				return 5.0F + extraWeight + ((float)(playerInAirTime / 400));
			}
		}

		
		if (isParticleRotServerSafe(world, entity1))
		{
			float var = WeatherUtilParticle.getParticleWeight((EntityRotFX)entity1);

			if (var != -1)
				return var;
		}

		if (entity1 instanceof EntitySquid)
			return 400F;

		if (entity1 instanceof EntityLivingBase)
		{
			EntityLivingBase livingEnt = (EntityLivingBase) entity1;
			int airTime = livingEnt.getEntityData().getInteger("timeInAir");
			if (livingEnt.onGround || livingEnt.handleWaterMovement())
				airTime = 0;
			else
				airTime++;
			
			livingEnt.getEntityData().setInteger("timeInAir", airTime);
			
		}

		if (entity1 instanceof Entity) {
			Entity ent = (Entity) entity1;
			if (WeatherUtilData.isWindWeightSet(ent) && (forTornado || WeatherUtilData.isWindAffected(ent))) {
				return WeatherUtilData.getWindWeight(ent);
			}
		}

		if (entity1 instanceof EntityLivingBase) {
			EntityLivingBase livingEnt = (EntityLivingBase) entity1;
			int airTime = livingEnt.getEntityData().getInteger("timeInAir");
			if (forTornado) {
				return 0.5F + (((float)airTime) / 800F);
			} else {
				return 500.0F + (livingEnt.onGround ? 2.0F : 0.0F) + ((airTime) / 400);
			}
		}

		if (/*entity1 instanceof EntitySurfboard || */entity1 instanceof EntityBoat || entity1 instanceof EntityItem/* || entity1 instanceof EntityTropicalFishHook*/ || entity1 instanceof EntityFishHook)
		{
			return 4000F;
		}

		if (entity1 instanceof EntityMinecart)
		{
			return 80F;
		}

		return 1F;
	}
	
	public static boolean isParticleRotServerSafe(World world, Object obj)
	{
		if (!world.isRemote) return false;
		return isParticleRotClientCheck(obj);
	}
	
	public static boolean isParticleRotClientCheck(Object obj)
	{
		return obj instanceof EntityRotFX;
	}
	
	public static boolean canPushEntity(Entity ent)
	{
		//weather2: shouldnt be needed since its particles only now, ish
		//if (!WeatherUtil.canUseWindOn(ent)) return false;
		
		WindManager windMan = ClientTickHandler.weatherManager.windMan;
		
		double speed = 10.0D;
		int startX = (int)(ent.posX - speed * (double)(-MathHelper.sin(windMan.getWindAngleForPriority(null) / 180.0F * (float)Math.PI) * MathHelper.cos(0F/*weatherMan.wind.yDirection*/ / 180.0F * (float)Math.PI)));
		int startZ = (int)(ent.posZ - speed * (double)(MathHelper.cos(windMan.getWindAngleForPriority(null) / 180.0F * (float)Math.PI) * MathHelper.cos(0F/*weatherMan.wind.yDirection*/ / 180.0F * (float)Math.PI)));

		return ent.world.rayTraceBlocks((new Vec3(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ)).toMCVec(), (new Vec3(startX, ent.posY + (double)ent.getEyeHeight(), startZ)).toMCVec()) == null;
		//return true;
	}
	
	public static boolean isEntityOutside(Entity parEnt) {
		return isEntityOutside(parEnt, false);
	}
	
	public static boolean isEntityOutside(Entity parEnt, boolean cheapCheck) {
		return isPosOutside(parEnt.world, new Vec3(parEnt.posX, parEnt.posY, parEnt.posZ), cheapCheck);
	}
	
	public static boolean isPosOutside(World parWorld, Vec3 parPos) {
		return isPosOutside(parWorld, parPos, false);
	}
	
	public static boolean isPosOutside(World parWorld, Vec3 parPos, boolean cheapCheck)
	{
		int rangeCheck = 5;
		int yOffset = 1;
		
		if (WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(MathHelper.floor(parPos.xCoord), 0, MathHelper.floor(parPos.zCoord))).getY() < parPos.yCoord+1) return true;
		
		if (cheapCheck) return false;
		
		Vec3 vecTry = new Vec3(parPos.xCoord + EnumFacing.NORTH.getFrontOffsetX()*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + EnumFacing.NORTH.getFrontOffsetZ()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.xCoord + EnumFacing.SOUTH.getFrontOffsetX()*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + EnumFacing.SOUTH.getFrontOffsetZ()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.xCoord + EnumFacing.EAST.getFrontOffsetX()*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + EnumFacing.EAST.getFrontOffsetZ()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.xCoord + EnumFacing.WEST.getFrontOffsetX()*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + EnumFacing.WEST.getFrontOffsetZ()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		return false;
	}
	
	public static boolean checkVecOutside(World parWorld, Vec3 parPos, Vec3 parCheckPos)
	{
		return parWorld.rayTraceBlocks(parPos.toMCVec(), parCheckPos.toMCVec()) == null && WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(MathHelper.floor(parCheckPos.xCoord), 0, MathHelper.floor(parCheckPos.zCoord))).getY() < parCheckPos.yCoord;
	}

	public static EntityPlayer getClosestPlayer(World world, double posX, double posY, double posZ, double distance)
	{
		double r = 0.0D;
		EntityPlayer player = null;

		for (EntityPlayer entity : world.playerEntities)
		{
			double player_distance = entity.getDistanceSq(posX, posY, posZ);
			if (player_distance < distance * distance && (player_distance < r || player == null))
			{
				r = player_distance;
				player = entity;
			}
		}

		return player;
	}
}
