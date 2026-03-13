package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightTexture.class)
public interface LightTextureAccessor {
    @Accessor("lightPixels")
    NativeImage getLightPixels();
}