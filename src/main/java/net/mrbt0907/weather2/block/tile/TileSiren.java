package net.mrbt0907.weather2.block.tile;

import net.CoroUtil.util.CoroUtilPhysics;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.block.BlockSiren;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.sound.MovingSoundEX;
import net.mrbt0907.weather2.client.sound.SoundHandler;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigSand;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.entity.AI.EntityAITakeCover;
import net.mrbt0907.weather2.mixins.accessor.GoalSelectorAccessor;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.registry.TileEntityRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;

import java.util.ArrayList;
import java.util.List;

public class TileSiren extends TileEntity implements ITickableTileEntity
{
    MovingSoundEX sound;

    public TileSiren()
    {
        this(TileEntityRegistry.TORNADO_SIREN_TILE.get());
    }

    public TileSiren(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public void tick()
    {
        boolean isEnabled = this.level.getBlockState(this.worldPosition).getValue(BlockSiren.ENABLED);

        if (isEnabled)
        {
            if (level.isClientSide)
                tickClient();
            else
                tickAlert();
        }
        else
        {
            if (level.isClientSide && sound != null)
            {
                sound.setDone();
                sound = null;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient()
    {
        if (sound == null || sound.isStopped())
        {
            Vec3 pos = new Vec3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
            WeatherObject so = ClientTickHandler.weatherManager.getWorstWeather(pos, ConfigMisc.siren_scan_range, Stage.TORNADO.getStage(), Integer.MAX_VALUE);

            if (so != null)
                sound = SoundHandler.playMovingSound(worldPosition, SoundRegistry.siren.get(), SoundCategory.RECORDS, 2, ConfigVolume.sirens, 1.0F, 356.0D);
            else
            {
                if (!ConfigSand.disable_darude_sandstorm_plz) {
                    SandstormObject sandstorm = ClientTickHandler.weatherManager.getClosestSandstormByIntensity(pos);

                    if (sandstorm != null) {
                        List<net.CoroUtil.util.Vec3> points = sandstorm.getSandstormAsShape();

                        float distMax = 75F;


                        boolean inStorm = CoroUtilPhysics.isInConvexShape(pos.toVec3Coro(), points);
                        double dist = Math.min(distMax, CoroUtilPhysics.getDistanceToShape(pos.toVec3Coro(), points));

                        if (inStorm || dist < distMax)
                            sound = SoundHandler.playMovingSound(worldPosition, SoundRegistry.sirenDarude.get(), SoundCategory.RECORDS, 2, ConfigVolume.sirens, 1.0F, 356.0D);
                    }
                }
            }
        }
    }

    private void tickAlert()
    {
        if (!level.isClientSide && level.getGameTime() % 5L == 0L)
        {
            List<Entity> entities = new ArrayList<Entity>(level.getEntities((Entity)null, new net.minecraft.util.math.AxisAlignedBB(worldPosition).inflate(120.0D)));
            for (Entity entity : entities)
                if (entity instanceof MobEntity && entity.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 120.0D)
                    ((GoalSelectorAccessor)((MobEntity)entity).goalSelector).getAvailableGoals().forEach(goal -> {
                        if (goal.getGoal() instanceof EntityAITakeCover) ((EntityAITakeCover)goal.getGoal()).isAlert = true;
                    });
        }
    }
}