package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerEntity.class)
public interface ClientPlayerEntityAccessor {
    @Invoker("getPermissionLevel")
    int invokeGetPermissionLevel();
}