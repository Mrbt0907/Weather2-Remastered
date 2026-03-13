package net.extendedrenderer.particle.entity;

import net.extendedrenderer.shader.InstancedMeshParticle;
import net.extendedrenderer.shader.Matrix4fe;
import net.extendedrenderer.shader.Transformation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3f;

public class ParticleCustomMatrix extends ParticleTexFX {

    public float angleX;
    public float angleY;
    public float angleZ;

    public float yy;

    public ParticleCustomMatrix(ClientWorld worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
    }

    @Override
    public void renderParticleForShader(InstancedMeshParticle mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (mesh.curBufferPos >= mesh.numInstances) return;

        float posX = (float) (this.xo + (this.x - this.xo) * (double) partialTicks - mesh.interpPosX);
        float posY = (float) (this.yo + (this.y - this.yo) * (double) partialTicks - mesh.interpPosY);
        float posZ = (float) (this.zo + (this.z - this.zo) * (double) partialTicks - mesh.interpPosZ);
        Vector3f pos = new Vector3f(posX, posY, posZ);

        Matrix4fe matrixFunnel = new Matrix4fe();
        matrixFunnel.rotateY(angleY);
        matrixFunnel.rotateX(angleX);
        matrixFunnel.translate(new Vector3f(0, yy, 0));


        if (rotationAroundCenter < rotationAroundCenterPrev) {
            rotationAroundCenterPrev -= 360;
        }
        float deltaRot = rotationAroundCenterPrev + (rotationAroundCenter - rotationAroundCenterPrev) * partialTicks;
        matrixFunnel.translate(new Vector3f(
                (float) Math.sin(Math.toRadians(deltaRot)) * rotationDistAroundCenter,
                0,
                (float) Math.cos(Math.toRadians(deltaRot)) * rotationDistAroundCenter));
        Vector3f posExtraRot = matrixFunnel.getTranslation();

        pos.set(pos.x() + posExtraRot.x(), pos.y() + posExtraRot.y(), pos.z() + posExtraRot.z());

        Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos, partialTicks);

        modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos), mesh.instanceDataBuffer);

        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS, brightnessCache);

        int rgbaIndex = 0;
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.rCol);
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.gCol);
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.bCol);
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.alpha);

        mesh.curBufferPos++;
    }

    public static float lerpDegrees(float start, float end, float amount)
    {
        float difference = Math.abs(end - start);
        if (difference > 180)
        {
            if (end > start)
            {
                start += 360;
            }
            else
            {
                end += 360;
            }
        }

        float value = (start + ((end - start) * amount));

        float rangeZero = 360;

        if (value >= 0 && value <= 360)
            return value;

        return (value % rangeZero);
    }
}