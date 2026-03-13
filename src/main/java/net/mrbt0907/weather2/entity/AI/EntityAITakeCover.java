package net.mrbt0907.weather2.entity.AI;

import net.CoroUtil.ai.ITaskInitializer;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.event.ServerTickHandler;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.WeatherManager;

import java.util.EnumSet;
import java.util.Optional;

public class EntityAITakeCover extends Goal implements ITaskInitializer {
    public boolean isAlert = false;
    protected PathNavigator navigator;
    private CreatureEntity entity;
    private BlockPos doorPos;
    private int insidePosX = -1;
    private int insidePosZ = -1;

    public EntityAITakeCover(CreatureEntity entity) {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        setEntity(entity);
    }

    @Override
    public boolean canUse() {
        WeatherManager weatherManager = ServerTickHandler.getWeatherSystemForDim(entity.level.dimension());
        if (weatherManager == null) return false;

        BlockPos blockpos = entity.blockPosition();
        Vec3 pos = new Vec3(blockpos);
        boolean runInside = isAlert || weatherManager.getWorstWeather(pos, ConfigStorm.villager_detection_range, Stage.SEVERE.getStage(), Integer.MAX_VALUE, WeatherEnum.Type.CLOUD) != null;

        if (runInside) {
            if (insidePosX != -1 && entity.distanceToSqr(insidePosX, entity.getY(), insidePosZ) < 4.0D)
                return false;
            else {
                if (entity.level instanceof ServerWorld) {
                    ServerWorld serverWorld = (ServerWorld) entity.level;
                    PointOfInterestManager poiManager = serverWorld.getPoiManager();

                    Optional<BlockPos> nearestPOI = poiManager.findClosest(
                            poi -> poi == PointOfInterestType.HOME,
                            blockpos,
                            14,
                            PointOfInterestManager.Status.ANY
                    );

                    if (nearestPOI.isPresent()) {
                        doorPos = nearestPOI.get();
                        return true;
                    }

                    return false;
                }
                return false;
            }
        } else
            return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !navigator.isDone();
    }

    @Override
    public void start() {
        insidePosX = -1;

        if (doorPos == null)
            return;

        int i = doorPos.getX();
        int j = doorPos.getY();
        int k = doorPos.getZ();

        if (entity.distanceToSqr(doorPos.getX(), doorPos.getY(), doorPos.getZ()) > 256.0D) {
            Vector3d vec3d = RandomPositionGenerator.getLandPosTowards(
                    this.entity,
                    14,
                    3,
                    new Vector3d((double) i + 0.5D, j, (double) k + 0.5D)
            );

            if (vec3d != null)
                navigator.moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0D);
        } else
            navigator.moveTo((double) i + 0.5D, j, (double) k + 0.5D, 1.0D);
    }

    @Override
    public void stop() {
        if (doorPos != null) {
            insidePosX = doorPos.getX();
            insidePosZ = doorPos.getZ();
        }
        doorPos = null;
        isAlert = false;
    }

    @Override
    public void setEntity(CreatureEntity entity) {
        this.entity = entity;
        navigator = entity.getNavigation();
    }
}