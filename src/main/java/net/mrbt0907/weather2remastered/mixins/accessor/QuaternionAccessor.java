package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.util.math.vector.Quaternion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Quaternion.class)
public interface QuaternionAccessor {
    @Accessor("i")
    float getI();

    @Accessor("j")
    float getJ();

    @Accessor("k")
    float getK();

    @Accessor("r")
    float getR();
}