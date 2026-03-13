package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteTexturedParticle.class)
public interface SpriteTexturedParticleAccessor {

    @Accessor("sprite")
    TextureAtlasSprite getSprite();

    @Accessor("sprite")
    void setSprite(TextureAtlasSprite sprite);
}