package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Vector3f.class)
public interface Vector3fAccessor {
    @Accessor("x")
    float getX();

    @Accessor("y")
    float getY();

    @Accessor("z")
    float getZ();
}