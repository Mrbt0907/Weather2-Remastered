package net.CoroUtil.util;

import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

public class CoroUtilMisc {

    public static void sendCommandSenderMsg(ICommandSource entP, String msg) {
        entP.sendMessage(new StringTextComponent(msg), null);
    }

    public static float adjVal(float source, float target, float adj) {
        if (source < target) {
            source += adj;

            if (source > target) {
                source = target;
            }
        } else if (source > target) {
            source -= adj;

            if (source < target) {
                source = target;
            }
        }
        return source;
    }

}