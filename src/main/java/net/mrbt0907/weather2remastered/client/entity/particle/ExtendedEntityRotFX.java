package net.mrbt0907.weather2.client.entity.particle;

import javax.annotation.Nonnull;
import net.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.util.Maths;
import com.mojang.blaze3d.vertex.IVertexBuilder;

public class ExtendedEntityRotFX extends EntityRotFX
{
    protected boolean useVolumetrics;
    protected int ticksExisted;
    private float invMax;
    protected float startRed, startGreen, startBlue, startMult, finalRed, finalBlue, finalGreen, finalAdj, finalMult;

    public ExtendedEntityRotFX(ClientWorld world, double xCoordIn, double yCoordIn, double zCoordIn,
                               double xSpeedIn, double ySpeedIn, double zSpeedIn, TextureAtlasSprite texture)
    {

        super(world, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);

        if (texture != null)
            setSprite(texture);

        this.gravity       = 1.0F;
        this.particleScale = 1.0F;

        setMaxAge(100);
        setCanCollide(false);

        finalMult = 1.0F;
        startMult = 1.0F;
        invMax    = 1.0F / (getMaxAge() * startMult);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (finalAdj < 1.0F)
        {
            finalAdj = ticksExisted * invMax + finalMult;
            if (finalAdj > 1.0F) finalAdj = 1.0F;
            float f = 1.0F - finalAdj;
            setColor(startRed * f + finalRed * finalAdj,
                    startGreen * f + finalGreen * finalAdj,
                    startBlue * f + finalBlue * finalAdj);
        }

        ticksExisted++;
    }

    @Override
    protected void renderBillboard(@Nonnull IVertexBuilder buffer,
                                   @Nonnull ActiveRenderInfo renderInfo,
                                   float partialTicks,
                                   float rotationX, float rotationZ,
                                   float rotationYZ, float rotationXY, float rotationXZ)
    {
        if (!ConfigClient.enable_volumetrics || !useVolumetrics)
            super.renderBillboard(buffer, renderInfo, partialTicks,
                    rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    public boolean isVolumetric() { return useVolumetrics; }
    public void    setVolumetric() { useVolumetrics = true; }

    @Override
    public void setColor(float r, float g, float b)
    {
        startRed   = r;
        startGreen = g;
        startBlue  = b;
        finalRed   = r;
        finalGreen = g;
        finalBlue  = b;
        super.setColor(r, g, b);
    }

    public void setFinalColor(float percent, float r, float g, float b)
    {
        finalRed   = r;
        finalGreen = g;
        finalBlue  = b;
        finalAdj   = 0.0F;
        finalMult  = percent;
    }

    public void setColorFade(float percent)
    {
        startMult = Maths.clamp(percent, 0.0F, 1.0F);
        invMax    = 1.0F / (getMaxAge() * startMult);
    }

    @Override
    public void setMaxAge(int particleLifeTime)
    {
        super.setMaxAge(particleLifeTime);
        invMax = 1.0F / (getMaxAge() * startMult);
    }

    @Override
    public int getFXLayer() { return 1; }

    @Override
    public int getLightColor(float partialTick)
    {
        double px = this.x;
        double py = Math.max(0.0D, Math.min(255.0D, this.y));
        double pz = this.z;

        BlockPos pos = new BlockPos(px, py, pz);

        if (!this.level.isLoaded(pos))
            return this.level.dimensionType().hasSkyLight() ? (15 << 20) : 0;

        int skyLight   = this.level.getBrightness(LightType.SKY,   pos);
        int blockLight = this.level.getBrightness(LightType.BLOCK, pos);
        return (skyLight << 20) | (blockLight << 4);
    }
}