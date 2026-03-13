package net.mrbt0907.weather2.util;

import net.CoroUtil.api.weather.IWindHandler;
import net.CoroUtil.util.CoroUtilEntOrParticle;
import net.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.WeatherUtilData;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.mixins.accessor.GoalSelectorAccessor;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WindManager;

public class WeatherUtilEntity {

    public static int playerInAirTime = 0;

    
    public static float getWeight(Object obj)
    {
        World world = CoroUtilEntOrParticle.getWorld(obj);
        if (world == null)
            return -1.0F;

        if (obj instanceof IWindHandler)
            return ((IWindHandler) obj).getWindWeight();
        else if (world.isClientSide && obj instanceof Particle)
            return WeatherUtilParticle.getParticleWeight((Particle) obj);
        else if (obj instanceof EntityMovingBlock)
        {
            EntityMovingBlock block = (EntityMovingBlock) obj;
            return 12F + (block.block.isToolEffective(block.state, net.minecraftforge.common.ToolType.AXE) ? block.block.getExplosionResistance() : block.block.isToolEffective(block.state, net.minecraftforge.common.ToolType.SHOVEL) ? block.block.getExplosionResistance() * 6 : block.block.getExplosionResistance() * 13);
        }
        else if (obj instanceof SquidEntity)
            return 400F;
        else if (obj instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) obj;
            if (player.isOnGround() || player.isInWater())
                WeatherUtilEntity.playerInAirTime = 0;
            else
                WeatherUtilEntity.playerInAirTime++;

            if (player.isCreative() || player.isSpectator()) return -1.0F;

            float extraWeight = 0.0F;
            if (player.inventory != null)
                for (ItemStack stack : player.inventory.armor)
                    if (!stack.isEmpty() && stack.getMaxDamage() > 0)
                        extraWeight += stack.getMaxDamage() * 0.0025F;

            return 5.0F + extraWeight + WeatherUtilEntity.playerInAirTime * 0.0025F;
        }
        else if (obj instanceof LivingEntity)
        {
            LivingEntity livingEnt = (LivingEntity) obj;
            int airTime = livingEnt.getPersistentData().getInt("timeInAir");

            if (livingEnt.isOnGround() || livingEnt.isInWater())
                airTime = 0;
            else
                airTime++;

            livingEnt.getPersistentData().putInt("timeInAir", airTime);
            return 5.0F + airTime * 0.0025F;

        }
        else if (obj instanceof BoatEntity || obj instanceof ItemEntity || obj instanceof FishingBobberEntity)
            return 4000F;
        else if (obj instanceof AbstractMinecartEntity)
            return 80F;
        else if (obj instanceof Entity)
        {
            Entity ent = (Entity) obj;
            if (WeatherUtilData.isWindWeightSet(ent))
                return WeatherUtilData.getWindWeight(ent);
        }

        return 1F;
    }

    public static boolean isParticleRotServerSafe(World world, Object obj)
    {
        return world.isClientSide && WeatherUtilEntity.isParticleRotClientCheck(obj);
    }

    public static boolean isParticleRotClientCheck(Object obj)
    {
        return obj instanceof EntityRotFX;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean canPushEntity(Entity ent)
    {
        WindManager windMan = ClientTickHandler.weatherManager.windManager;

        double speed = 10.0D;
        int startX = (int)(ent.getX() - speed * (double)(-Maths.fastSin(windMan.windAngle / 180.0F * (float)Math.PI) * Maths.fastCos(0F / 180.0F * (float)Math.PI)));
        int startZ = (int)(ent.getZ() - speed * (double)(Maths.fastCos(windMan.windAngle / 180.0F * (float)Math.PI) * Maths.fastCos(0F / 180.0F * (float)Math.PI)));

        Vector3d start = new Vector3d(ent.getX(), ent.getY() + (double)ent.getEyeHeight(), ent.getZ());
        Vector3d end = new Vector3d(startX, ent.getY() + (double)ent.getEyeHeight(), startZ);

        BlockRayTraceResult result = ent.level.clip(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, ent));
        return result.getType() == RayTraceResult.Type.MISS;
    }

    public static boolean isEntityOutside(Entity parEnt) {
        return WeatherUtilEntity.isEntityOutside(parEnt, false);
    }

    public static boolean isEntityOutside(Entity parEnt, boolean cheapCheck) {
        return WeatherUtilEntity.isPosOutside(parEnt.level, new Vec3(parEnt.getX(), parEnt.getY(), parEnt.getZ()), cheapCheck);
    }

    public static boolean isPosOutside(World parWorld, Vec3 parPos) {
        return WeatherUtilEntity.isPosOutside(parWorld, parPos, false);
    }

    public static boolean isPosOutside(World parWorld, Vec3 parPos, boolean cheapCheck)
    {
        int rangeCheck = 5;
        int yOffset = 1;

        if (WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(MathHelper.floor(parPos.posX), 0, MathHelper.floor(parPos.posZ))).getY() < parPos.posY+1) return true;

        if (cheapCheck) return false;

        Vec3 vecTry = new Vec3(parPos.posX + Direction.NORTH.getStepX()*rangeCheck, parPos.posY+yOffset, parPos.posZ + Direction.NORTH.getStepZ()*rangeCheck);
        if (WeatherUtilEntity.checkVecOutside(parWorld, parPos, vecTry)) return true;

        vecTry = new Vec3(parPos.posX + Direction.SOUTH.getStepX()*rangeCheck, parPos.posY+yOffset, parPos.posZ + Direction.SOUTH.getStepZ()*rangeCheck);
        if (WeatherUtilEntity.checkVecOutside(parWorld, parPos, vecTry)) return true;

        vecTry = new Vec3(parPos.posX + Direction.EAST.getStepX()*rangeCheck, parPos.posY+yOffset, parPos.posZ + Direction.EAST.getStepZ()*rangeCheck);
        if (WeatherUtilEntity.checkVecOutside(parWorld, parPos, vecTry)) return true;

        vecTry = new Vec3(parPos.posX + Direction.WEST.getStepX()*rangeCheck, parPos.posY+yOffset, parPos.posZ + Direction.WEST.getStepZ()*rangeCheck);
        if (WeatherUtilEntity.checkVecOutside(parWorld, parPos, vecTry)) return true;

        return false;
    }

    public static boolean checkVecOutside(World parWorld, Vec3 parPos, Vec3 parCheckPos)
    {
        Vector3d start = new Vector3d(parPos.posX, parPos.posY, parPos.posZ);
        Vector3d end = new Vector3d(parCheckPos.posX, parCheckPos.posY, parCheckPos.posZ);

        BlockRayTraceResult result = parWorld.clip(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));
        return result.getType() == RayTraceResult.Type.MISS && WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(MathHelper.floor(parCheckPos.posX), 0, MathHelper.floor(parCheckPos.posZ))).getY() < parCheckPos.posY;
    }

    
    public static PlayerEntity getClosestPlayer(World world, double posX, double posY, double posZ, double radius)
    {
        double min_radius = 9999;
        PlayerEntity player = null;

        for (PlayerEntity entity : world.players())
        {
            double player_distance = FartsyUtil.sqrtf((float) entity.distanceToSqr(posX, posY, posZ));

            if (player_distance <= radius && (player_distance < min_radius || player == null))
            {
                player = entity;
                min_radius = player_distance;

            }
        }

        return player;
    }

    public static boolean hasAITask(CreatureEntity creature, Class<? extends Goal> clazz)
    {
        GoalSelectorAccessor accessor = (GoalSelectorAccessor) creature.goalSelector;
        for (PrioritizedGoal entry : accessor.getAvailableGoals())
            if (clazz.isAssignableFrom(entry.getGoal().getClass()))
                return true;
        return false;
    }
}