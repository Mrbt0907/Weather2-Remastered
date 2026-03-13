package net.mrbt0907.weather2.client.entity.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.extendedrenderer.particle.entity.ParticleTexFX;

public class ParticleSandstorm extends ParticleTexFX {

    public double angleToStorm = 0;
    public int heightLayer = 0;
    public double distAdj = 0;
    public boolean lockPosition = false;

    public ParticleSandstorm(ClientWorld worldIn, double posXIn, double posYIn,
                             double posZIn, double mX, double mY, double mZ,
                             TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
    }

    

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        super.render(buffer, renderInfo, partialTicks);
    }
}