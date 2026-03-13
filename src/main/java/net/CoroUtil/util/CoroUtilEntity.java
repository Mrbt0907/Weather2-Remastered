package net.CoroUtil.util;

import net.minecraft.entity.*;

public class CoroUtilEntity {

    public static String getName(Entity ent) {
        return ent != null ? ent.getName().getString() : "nullObject";
    }

}