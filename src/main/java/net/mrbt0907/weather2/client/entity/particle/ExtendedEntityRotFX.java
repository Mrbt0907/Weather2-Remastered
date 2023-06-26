package net.mrbt0907.weather2.client.entity.particle;

import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.mrbt0907.weather2.util.Maths;

public class ExtendedEntityRotFX extends EntityRotFX
{
	protected int ticksExisted;
	protected float startRed, startGreen, startBlue, startMult, finalRed, finalBlue, finalGreen, finalAdj, finalMult;
	
	public ExtendedEntityRotFX(World world, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, TextureAtlasSprite texture)
	{
		super(world, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn - 0.5D, zSpeedIn);
        setParticleTexture(texture);
        particleGravity = 1F;
        particleScale = 1F;
        setMaxAge(100);
        setCanCollide(false);
		finalMult = 1.0F;
		startMult = 1.0F;
	}
	
	public void onUpdate()
	{
		super.onUpdate();
		
		if (finalAdj < 1.0F)
		{
			finalAdj = Math.min(((float) ticksExisted / (particleMaxAge * startMult)) + finalMult, 1.0F);
			float f = finalAdj, f1 = 1.0F - finalAdj;
			setRBGColorF(startRed * f1 + finalRed * f, startGreen * f1 + finalGreen * f, startBlue * f1 + finalBlue * f);
		}
		
		ticksExisted++;
	}
	
	public void setColor(float r, float g, float b)
	{
		startRed = r;
		startGreen = g;
		startBlue = b;
		finalRed = r;
		finalGreen = g;
		finalBlue = b;
	}
	
	public void setFinalColor(float percent, float r, float g, float b)
	{
		finalRed = r;
		finalGreen = g;
		finalBlue = b;
		finalAdj = 0.0F;
		finalMult = percent;
	}

	public void setColorFade(float percent)
	{
		startMult = Maths.clamp(percent, 0.0F, 1.0F);
	}
	
    @Override
    public int getFXLayer()
    {
        return 1;
    }
}
