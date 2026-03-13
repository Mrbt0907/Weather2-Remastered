package net.mrbt0907.weather2.block.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.block.BlockSiren;
import net.mrbt0907.weather2.client.sound.MovingSoundEX;
import net.mrbt0907.weather2.client.sound.SoundHandler;
import net.mrbt0907.weather2.config.ConfigVolume;
import net.mrbt0907.weather2.entity.AI.EntityAITakeCover;
import net.mrbt0907.weather2.mixins.accessor.GoalSelectorAccessor;
import net.mrbt0907.weather2.registry.SoundRegistry;
import net.mrbt0907.weather2.registry.TileEntityRegistry;

public class TileEntityTSirenManual extends TileEntity implements ITickableTileEntity
{
    private MovingSoundEX sound;

    public TileEntityTSirenManual()
    {
        this(TileEntityRegistry.TORNADO_SIREN_MANUAL_TILE.get());
    }

    public TileEntityTSirenManual(TileEntityType<?> tileEntityTypeIn)
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
        if (sound == null || sound.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(sound))
            sound = SoundHandler.playMovingSound(
                    worldPosition,
                    SoundRegistry.siren.get(),
                    SoundCategory.RECORDS,
                    2,
                    ConfigVolume.sirens,
                    1.0F,
                    356.0D
            );
    }

    private void tickAlert()
    {
        if (!level.isClientSide && level.getGameTime() % 5L == 0L)
        {
            List<Entity> entities = new ArrayList<Entity>(level.getEntities((Entity)null, new net.minecraft.util.math.AxisAlignedBB(worldPosition).inflate(120.0D)));
            for (Entity entity : entities)
                if (entity instanceof MobEntity && entity.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 120.0D)
                    ((GoalSelectorAccessor)((MobEntity)entity).goalSelector).getAvailableGoals().forEach(goal -> {
                        if (goal.getGoal() instanceof EntityAITakeCover)
                            ((EntityAITakeCover)goal.getGoal()).isAlert = true;
                    });
        }
    }

    @Override
    public boolean onlyOpCanSetNbt()
    {
        return false;
    }
}