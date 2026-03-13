package net.CoroUtil.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.mrbt0907.weather2.mixins.accessor.ParticleAccessor;

public class CoroUtilEntOrParticle {

    public static double getPosX(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity)obj).getX();
        } else {
            return getPosXParticle(obj);
        }
    }

    private static double getPosXParticle(Object obj) {
        return ((ParticleAccessor)obj).getX();
    }

    public static double getPosY(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity)obj).getY();
        } else {
            return getPosYParticle(obj);
        }
    }

    private static double getPosYParticle(Object obj) {
        return ((ParticleAccessor)obj).getY();
    }

    public static double getPosZ(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity)obj).getZ();
        } else {
            return getPosZParticle(obj);
        }
    }

    private static double getPosZParticle(Object obj) {
        return ((ParticleAccessor)obj).getZ();
    }

    public static double getMotionX(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity)obj).getDeltaMovement().x;
        } else {
            return getMotionXParticle(obj);
        }
    }

    private static double getMotionXParticle(Object obj) {
        return ((ParticleAccessor)obj).getXd();
    }

    public static double getMotionY(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity)obj).getDeltaMovement().y;
        } else {
            return getMotionYParticle(obj);
        }
    }

    private static double getMotionYParticle(Object obj) {
        return ((ParticleAccessor)obj).getYd();
    }

    public static double getMotionZ(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity)obj).getDeltaMovement().z;
        } else {
            return getMotionZParticle(obj);
        }
    }

    private static double getMotionZParticle(Object obj) {
        return ((ParticleAccessor)obj).getZd();
    }

    public static void setMotionX(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity)obj;
            Vector3d motion = ent.getDeltaMovement();
            ent.setDeltaMovement(val, motion.y, motion.z);
        } else {
            setMotionXParticle(obj, val);
        }
    }

    private static void setMotionXParticle(Object obj, double val) {
        ((ParticleAccessor)obj).setXd(val);
    }

    public static void setMotionY(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity)obj;
            Vector3d motion = ent.getDeltaMovement();
            ent.setDeltaMovement(motion.x, val, motion.z);
        } else {
            setMotionYParticle(obj, val);
        }
    }

    private static void setMotionYParticle(Object obj, double val) {
        ((ParticleAccessor)obj).setYd(val);
    }

    public static void setMotionZ(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity)obj;
            Vector3d motion = ent.getDeltaMovement();
            ent.setDeltaMovement(motion.x, motion.y, val);
        } else {
            setMotionZParticle(obj, val);
        }
    }

    private static void setMotionZParticle(Object obj, double val) {
        ((ParticleAccessor)obj).setZd(val);
    }

    public static World getWorld(Object obj) {
        if (obj instanceof Entity) {
            return ((Entity)obj).level;
        } else {
            return getWorldParticle(obj);
        }
    }

    private static World getWorldParticle(Object obj) {
        return ((ParticleAccessor)obj).getLevel();
    }

    public static double getDistance(Object obj, double x, double y, double z)
    {
        double d0 = getPosX(obj) - x;
        double d1 = getPosY(obj) - y;
        double d2 = getPosZ(obj) - z;
        return (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static void setPosX(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity)obj;
            ent.setPos(val, ent.getY(), ent.getZ());
        } else {
            setPosXParticle(obj, val);
        }
    }

    private static void setPosXParticle(Object obj, double val) {
        ((ParticleAccessor)obj).setX(val);
    }

    public static void setPosY(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity)obj;
            ent.setPos(ent.getX(), val, ent.getZ());
        } else {
            setPosYParticle(obj, val);
        }
    }

    private static void setPosYParticle(Object obj, double val) {
        ((ParticleAccessor)obj).setY(val);
    }

    public static void setPosZ(Object obj, double val) {
        if (obj instanceof Entity) {
            Entity ent = (Entity)obj;
            ent.setPos(ent.getX(), ent.getY(), val);
        } else {
            setPosZParticle(obj, val);
        }
    }

    private static void setPosZParticle(Object obj, double val) {
        ((ParticleAccessor)obj).setZ(val);
    }

}