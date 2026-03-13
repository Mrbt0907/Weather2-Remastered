package net.mrbt0907.weather2.api;

import net.minecraft.entity.Entity;

public class WeatherUtilData {

    public static String weather2_WindWeight = "weather2_WindWeight";
    public static String weather2_WindAffected = "weather2_WindAffected";

    public static void setWindAffected(Entity ent) {
        ent.getPersistentData().putBoolean(weather2_WindAffected, true);
    }

    public static void setWindWeight(Entity ent, float weight) {
        ent.getPersistentData().putFloat(weather2_WindWeight, weight);
    }

    public static boolean isWindAffected(Entity ent) {
        return ent.getPersistentData().getBoolean(weather2_WindAffected);
    }

    public static float getWindWeight(Entity ent) {
        return ent.getPersistentData().getFloat(weather2_WindWeight);
    }

    public static boolean isWindWeightSet(Entity ent) {
        return ent.getPersistentData().contains(weather2_WindWeight);
    }

}