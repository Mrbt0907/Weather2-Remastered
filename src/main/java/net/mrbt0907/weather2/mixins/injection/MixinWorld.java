package net.mrbt0907.weather2.mixins.injection;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.weather.WeatherManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {
    @Inject(method = "isRainingAt(Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At("RETURN"), cancellable = true)
    private void isRainingAt(BlockPos position, CallbackInfoReturnable<Boolean> callback) {
        World world = (World) (Object) this;
        if (!ConfigMisc.overcast_mode || !callback.getReturnValueZ()) {
            WeatherManager manager = WeatherAPI.getManager(world);
            callback.setReturnValue(manager != null && manager.hasDownfall(position));
        }
    }
}