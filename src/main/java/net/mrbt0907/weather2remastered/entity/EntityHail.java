package net.mrbt0907.weather2.entity;

import net.CoroUtil.api.weather.IWindHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.mrbt0907.weather2.api.WeatherDamageSource;
import net.mrbt0907.weather2.registry.EntityRegistry;
import net.mrbt0907.weather2.util.Maths;

import javax.annotation.Nonnull;
import java.util.Optional;

public class EntityHail extends Entity implements IWindHandler {
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState ICE = Blocks.ICE.defaultBlockState();
    public double size;

    public EntityHail(EntityType<?> type, World world) {
        super(type, world);
    }

    public EntityHail(World world) {
        this(EntityRegistry.WEATHER_HAIL.get(), world);
        setSize(0.3F, 0.3F);
    }

    public EntityHail(World world, float size) {
        this(EntityRegistry.WEATHER_HAIL.get(), world);
        setSize(size, size);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundNBT nbt) {
        if (nbt.contains("size"))
            size = nbt.getDouble("size");
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundNBT nbt) {
        nbt.putDouble("size", size);
    }

    @Override
    public boolean save(@Nonnull CompoundNBT compound) {
        return false;
    }

    @Override
    public void tick() {
        Vector3d motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;

        if (motionY > -3.0D)
            motionY -= 0.1D;

        motionX = Maths.clamp(motionX + Maths.random(-0.05D, 0.05D), -3.0D, 3.0D);
        motionZ = Maths.clamp(motionZ + Maths.random(-0.05D, 0.05D), -3.0D, 3.0D);

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        this.setPos(this.getX() + motionX, this.getY() + motionY, this.getZ() + motionZ);

        this.setDeltaMovement(motionX, motionY, motionZ);

        if (this.getY() < -64.0D)
            outOfWorld();

        if (!level.isClientSide) {
            if (isInWater()) {
                this.remove();
                return;
            }


            Vector3d start_point = new Vector3d(this.getX(), this.getY(), this.getZ());
            Vector3d end_point = new Vector3d(this.getX() + motionX * 1.3D, this.getY() + motionY * 1.3D, this.getZ() + motionZ * 1.3D);

            double speed = Maths.speedSq(motionX, motionY, motionZ);
            if (speed > 0.2F) {
                if (tickCount % 5 == 0 && EntityMovingBlock.loadedEntities.containsKey(level.dimension()))
                    for (Entity entity : EntityMovingBlock.loadedEntities.get(level.dimension())) {
                        AxisAlignedBB expandedBox = entity.getBoundingBox().inflate(this.getBbWidth());

                        Optional<Vector3d> hitPoint = expandedBox.clip(start_point, end_point);

                        if (hitPoint.isPresent()) {
                            try {
                                entity.hurt(WeatherDamageSource.HAIL, (float) speed * getWeight());
                            } catch (Exception e) {
                            }

                            level.playSound(null, new BlockPos(this.getX(), this.getY(), this.getZ()),
                                    SoundEvents.GLASS_HIT, SoundCategory.AMBIENT, 1F, 5F - this.getBbWidth() * 5.0F);
                            this.remove();
                            return;
                        }
                    }
            }

            end_point = new Vector3d(this.getX() + motionX * 1.3D, this.getY() + motionY * 1.3D, this.getZ() + motionZ * 1.3D);
            BlockRayTraceResult raytrace = level.clip(new RayTraceContext(
                    new Vector3d(this.getX(), this.getY(), this.getZ()),
                    end_point,
                    RayTraceContext.BlockMode.COLLIDER,
                    RayTraceContext.FluidMode.NONE,
                    this
            ));

            if (raytrace != null) {
                end_point = new Vector3d(raytrace.getLocation().x, raytrace.getLocation().y, raytrace.getLocation().z);

                if (RayTraceResult.Type.BLOCK.equals(raytrace.getType())) {
                    double dampening;
                    BlockPos target_pos = raytrace.getBlockPos();
                    BlockState target = level.getBlockState(target_pos);
                    Block target_block = target.getBlock();

                    net.minecraftforge.common.ToolType target_tool = target.getHarvestTool();
                    net.minecraftforge.common.ToolType ice_tool = EntityHail.ICE.getHarvestTool();
                    float speed_penalty = (target_tool != null && target_tool.equals(ice_tool)) ? 0.20F : 0.06F;

                    float hardness = target.getDestroySpeed(level, target_pos);

                    if (hardness >= 0.0F && hardness < getWeight() * speed * speed_penalty) {
                        dampening = 1.0F / (hardness + 1.0F);
                        dampening = dampening > 1.0F ? 1.0F : dampening;
                        motionX *= dampening;
                        motionY *= dampening;
                        motionZ *= dampening;
                        this.setDeltaMovement(motionX, motionY, motionZ);

                        speed = (float) Maths.speedSq(motionX, motionY, motionZ);
                        if (speed < 0.01F) {
                            if (Maths.chance())
                                level.playSound(null, new BlockPos(this.getX(), this.getY(), this.getZ()), SoundEvents.STONE_STEP, SoundCategory.AMBIENT, 1F, 5F - this.getBbWidth() * 5.0F);
                            this.remove();
                        } else {
                            level.setBlock(target_pos, EntityHail.AIR, 2 | 16);
                            level.levelEvent(2001, target_pos, Block.getId(target));
                        }
                    } else {
                        if (Maths.chance())
                            level.playSound(null, new BlockPos(this.getX(), this.getY(), this.getZ()), SoundEvents.STONE_STEP, SoundCategory.AMBIENT, 1F, 5F - this.getBbWidth() * 5.0F);
                        this.remove();
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        level.playSound(null, new BlockPos(this.getX(), this.getY(), this.getZ()), SoundEvents.GLASS_BREAK, SoundCategory.AMBIENT, 1F, 5F - this.getBbWidth() * 5.0F);
        this.remove();
        return false;
    }

    public float getWeight() {
        return this.getBbHeight() + this.getBbWidth();
    }

    protected void setSize(float width, float height) {
        size = width;
        this.setBoundingBox(new AxisAlignedBB(
                this.getX() - width / 2, this.getY(), this.getZ() - width / 2,
                this.getX() + width / 2, this.getY() + height, this.getZ() + width / 2
        ));
    }

    @Override
    public float getWindWeight() {
        return 4.0F + getWeight();
    }

    @Override
    public int getParticleDecayExtra() {
        return 0;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}