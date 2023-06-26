package net.mrbt0907.weather2.client.entity.particle;

import extendedrenderer.particle.entity.ParticleTexLeafColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.util.Maths;

/**
 * Renders only when near player
 * kills itself when too far
 * spawn/render only on grass blocks
 * animation of waving grass
 * - distorts mesh shape? might not be possible for instanced rendered mesh
 * - need base of particle to stay flush with ground, so i cant outright simply modify pitch or roll to simulate wind
 * -- or i could if it was just a blade of grass but i want to use the tallgrass texture
 */
public class ParticleTallGrass extends ParticleTexLeafColor {

    public int height = 0;

    public ParticleTallGrass(World worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        float windSpeed = ClientTickHandler.weatherManager.windManager.windSpeed;
        this.rotationPitch = windSpeed * 60F;
        this.rotationPitch = (float)Math.toDegrees(Maths.fastSin(this.getAge() * 0.1F) * 0.2F);
    }
}
