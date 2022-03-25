package net.mrbt0907.weather2.api;

import net.minecraft.entity.Entity;

public class WeatherUtilData {

    public static String weather2_WindWeight = "weather2_WindWeight";
    public static String weather2_WindAffected = "weather2_WindAffected";

    /**
     * Use this method in addition to setWindWeight flag you want wind to push around your entity, use setWindWeight to set by how much.
     * If this method is not used, only tornadoes will pull your entity around.
     *
     * @param ent
     */
    public static void setWindAffected(Entity ent) {
        ent.getEntityData().setBoolean(weather2_WindAffected, true);
    }

    /**
     * Used to set wind weight on an entity in a way that weather2 will recognize
     *
     * @param ent
     * @param weight
     */
    public static void setWindWeight(Entity ent, float weight) {
        ent.getEntityData().setFloat(weather2_WindWeight, weight);
    }

    public static boolean isWindAffected(Entity ent) {
        return ent.getEntityData().getBoolean(weather2_WindAffected);
    }

    public static float getWindWeight(Entity ent) {
        return ent.getEntityData().getFloat(weather2_WindWeight);
    }

    public static boolean isWindWeightSet(Entity ent) {
        return ent.getEntityData().hasKey(weather2_WindWeight);
    }

}
