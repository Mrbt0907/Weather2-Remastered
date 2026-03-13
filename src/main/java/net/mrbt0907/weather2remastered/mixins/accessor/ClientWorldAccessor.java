package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientWorld.class)
public interface ClientWorldAccessor {
    @Accessor("skyFlashTime")
    int getSkyFlashTime();

    @Accessor("skyFlashTime")
    void setSkyFlashTime(int time);
}