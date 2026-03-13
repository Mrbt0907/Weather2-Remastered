package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(ActiveRenderInfo.class)
public interface ActiveRenderInfoAccessor {

    @Accessor("left")
    Vector3f getLeft();

    @Accessor("up")
    Vector3f getUp();
}