package net.extendedrenderer.particle.entity;

import net.CoroUtil.util.CoroUtilBlockLightCache;
import net.CoroUtil.util.CoroUtilParticle;
import net.extendedrenderer.render.RotatingParticleManager;
import net.extendedrenderer.shader.InstancedMeshParticle;
import net.extendedrenderer.shader.Matrix4fe;
import net.extendedrenderer.shader.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;
import com.mojang.blaze3d.vertex.IVertexBuilder;

public class ParticleTexExtraRender extends ParticleTexFX {

    private int severityOfRainRate       = 2;
    private int extraParticlesBaseAmount = 5;
    public  boolean noExtraParticles     = false;

    public ParticleTexExtraRender(ClientWorld worldIn, double posXIn, double posYIn,
                                  double posZIn, double mX, double mY, double mZ,
                                  TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
    }

    public int  getSeverityOfRainRate()
    { return severityOfRainRate; }
    public void setSeverityOfRainRate(int v)
    { this.severityOfRainRate = v; }
    public int  getExtraParticlesBaseAmount()
    { return extraParticlesBaseAmount; }
    public void setExtraParticlesBaseAmount(int v) {
        this.extraParticlesBaseAmount = v;
    }


    @Override
    public void tickExtraRotations() {
        if (isSlantParticleToWind()) {
            rotationYaw = (float) Math.toDegrees(Math.atan2(this.zd, this.xd)) - 90;
            double motionXZ = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
            rotationPitch = -(float) Math.toDegrees(Math.atan2(motionXZ, Math.abs(this.yd)));
        }

        if (!quatControl) {
            Entity ent = Minecraft.getInstance().getCameraEntity();
            if (facePlayer) {

                updateQuaternion(ent);
            } else if (facePlayerYaw) {

                if (ent != null) {
                    this.rotationYaw = ent.yRot;
                }

                Quaternion qY = new Quaternion(new Vector3f(0, 1, 0),
                        (float) Math.toRadians(-this.rotationYaw - 180F), false);
                Quaternion qX = new Quaternion(new Vector3f(1, 0, 0),
                        (float) Math.toRadians(-this.rotationPitch), false);
                if (this.rotateOrderXY) {
                    qX.mul(qY);
                    this.rotation.set(qX.i(), qX.j(), qX.k(), qX.r());
                } else {
                    qY.mul(qX);
                    this.rotation.set(qY.i(), qY.j(), qY.k(), qY.r());
                }
            } else {
                updateQuaternion(ent);
            }
        }
    }

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        float rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ;

        if (!facePlayer) {
            rotationX  =  MathHelper.cos(this.rotationYaw   * (float) Math.PI / 180.0F);
            rotationYZ =  MathHelper.sin(this.rotationYaw   * (float) Math.PI / 180.0F);
            rotationXY = -rotationYZ * MathHelper.sin(this.rotationPitch * (float) Math.PI / 180.0F);
            rotationXZ =  rotationX  * MathHelper.sin(this.rotationPitch * (float) Math.PI / 180.0F);
            rotationZ  =  MathHelper.cos(this.rotationPitch * (float) Math.PI / 180.0F);
        } else {
            Quaternion q = renderInfo.rotation();
            rotationX  = 1.0F - 2.0F * (q.j() * q.j() + q.k() * q.k());
            rotationZ  = 1.0F - 2.0F * (q.i() * q.i() + q.j() * q.j());
            rotationYZ = 2.0F * (q.i() * q.j() - q.k() * q.r());
            rotationXY = 2.0F * (q.i() * q.k() + q.j() * q.r());
            rotationXZ = 2.0F * (q.j() * q.k() - q.i() * q.r());

            if (this.isSlantParticleToWind()) {
                rotationXZ = (float) -this.zd;
                rotationXY = (float) -this.xd;
            }
        }

        renderMulti(buffer, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    private void renderMulti(IVertexBuilder buffer, float partialTicks,
                             float rotationX,  float rotationZ,
                             float rotationYZ, float rotationXY, float rotationXZ) {

        if (this.sprite == null) return;

        float scale = 0.1F * this.getScale();

        float u0 = this.sprite.getU0();
        float u1 = this.sprite.getU1();
        float v0 = this.sprite.getV0();
        float v1 = this.sprite.getV1();

        float fixY = 0;
        {
            float part      = 16F / 3F;
            float posBottom = (float)(this.y - 10D);
            int precipHeight = this.level.getHeightmapPos(
                    Heightmap.Type.MOTION_BLOCKING,
                    new BlockPos(this.x, this.y, this.z)).getY();

            if (posBottom < precipHeight) {
                float diff = precipHeight - posBottom;
                if (diff > part) diff = part;
                fixY = 0;
            }
        }

        int renderAmount = noExtraParticles
                ? 1
                : Math.min(extraParticlesBaseAmount + (Math.max(0, severityOfRainRate - 1) * 5),
                CoroUtilParticle.maxRainDrops);

        int light = 15728640;

        int r = (int)(this.rCol  * 255);
        int g = (int)(this.gCol  * 255);
        int b = (int)(this.bCol  * 255);
        int a = (int)(this.alpha * 255);

        try {
            for (int ii = 0; ii < renderAmount; ii++) {
                float f5 = (float)(this.xo + (this.x - this.xo) * partialTicks - interpPosX);
                float f6 = (float)(this.yo + (this.y - this.yo) * partialTicks - interpPosY) + fixY;
                float f7 = (float)(this.zo + (this.z - this.zo) * partialTicks - interpPosZ);

                double ox = 0, oy = 0, oz = 0;
                if (ii != 0) {
                    ox = CoroUtilParticle.rainPositions[ii].xCoord;
                    oy = CoroUtilParticle.rainPositions[ii].yCoord;
                    oz = CoroUtilParticle.rainPositions[ii].zCoord;
                    f5 += ox;  f6 += oy;  f7 += oz;
                }

                if (this.isDontRenderUnderTopmostBlock()) {
                    int h = this.level.getHeightmapPos(
                            Heightmap.Type.MOTION_BLOCKING,
                            new BlockPos(this.x + ox, this.y, this.z + oz)).getY();
                    if (this.y + oy <= h) continue;
                }

                if (ii != 0) {
                    RotatingParticleManager.debugParticleRenderCount++;
                }

                Vector3d c0 = new Vector3d(-rotationX * scale - rotationXY * scale,
                        -rotationZ * scale,
                        -rotationYZ * scale - rotationXZ * scale);
                Vector3d c1 = new Vector3d(-rotationX * scale + rotationXY * scale,
                        rotationZ * scale,
                        -rotationYZ * scale + rotationXZ * scale);
                Vector3d c2 = new Vector3d( rotationX * scale + rotationXY * scale,
                        rotationZ * scale,
                        rotationYZ * scale + rotationXZ * scale);
                Vector3d c3 = new Vector3d( rotationX * scale - rotationXY * scale,
                        -rotationZ * scale,
                        rotationYZ * scale - rotationXZ * scale);

                buffer.vertex(f5 + c0.x, f6 + c0.y, f7 + c0.z).uv(u1, v1).color(r, g, b, a).uv2(light).endVertex();
                buffer.vertex(f5 + c1.x, f6 + c1.y, f7 + c1.z).uv(u1, v0).color(r, g, b, a).uv2(light).endVertex();
                buffer.vertex(f5 + c2.x, f6 + c2.y, f7 + c2.z).uv(u0, v0).color(r, g, b, a).uv2(light).endVertex();
                buffer.vertex(f5 + c3.x, f6 + c3.y, f7 + c3.z).uv(u0, v1).color(r, g, b, a).uv2(light).endVertex();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void renderParticleForShader(InstancedMeshParticle mesh, Transformation transformation,
                                        Matrix4fe viewMatrix, Entity entityIn, float partialTicks,
                                        float rotationX, float rotationZ,
                                        float rotationYZ, float rotationXY, float rotationXZ) {

        float baseX = (float)(this.xo + (this.x - this.xo) * partialTicks);
        float baseY = (float)(this.yo + (this.y - this.yo) * partialTicks);
        float baseZ = (float)(this.zo + (this.z - this.zo) * partialTicks);

        int renderAmount = noExtraParticles
                ? 1
                : Math.min(extraParticlesBaseAmount + (Math.max(0, severityOfRainRate - 1) * 5),
                CoroUtilParticle.maxRainDrops);

        for (int iii = 0; iii < renderAmount; iii++) {
            if (mesh.curBufferPos >= mesh.numInstances) return;

            Vector3f pos;
            if (iii != 0) {
                pos = new Vector3f(baseX + (float) CoroUtilParticle.rainPositions[iii].xCoord,
                        baseY + (float) CoroUtilParticle.rainPositions[iii].yCoord,
                        baseZ + (float) CoroUtilParticle.rainPositions[iii].zCoord);
            } else {
                pos = new Vector3f(baseX, baseY, baseZ);
            }

            if (this.isDontRenderUnderTopmostBlock()) {
                int h = this.level.getHeightmapPos(
                        Heightmap.Type.MOTION_BLOCKING,
                        new BlockPos(pos.x(), pos.y(), pos.z())).getY();
                if (pos.y() <= h) continue;
            }

            pos.set(pos.x() - (float) interpPosX,
                    pos.y() - (float) interpPosY,
                    pos.z() - (float) interpPosZ);

            Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos, partialTicks);
            modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos, mesh.instanceDataBuffer);

            float brightness = fastLight
                    ? CoroUtilBlockLightCache.brightnessPlayer
                    : CoroUtilBlockLightCache.getBrightnessCached(this.level, (float)this.x, (float)this.y, (float)this.z);
            mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos
                    + mesh.MATRIX_SIZE_FLOATS, brightness);

            int rgbaIndex = 0;
            mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getRedColorF());
            mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getGreenColorF());
            mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getBlueColorF());
            mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getAlphaF());

            mesh.curBufferPos++;
        }
    }

    @Override
    public void updateQuaternion(Entity camera) {
        if (camera != null) {
            if (this.facePlayer) {
                this.rotationYaw   = camera.yRot;
                this.rotationPitch = camera.xRot;
            } else if (facePlayerYaw) {
                this.rotationYaw = camera.yRot;
            }
        }

        Quaternion qY = new Quaternion(new Vector3f(0, 1, 0),
                (float) Math.toRadians(-this.rotationYaw - 180F), false);
        Quaternion qX = new Quaternion(new Vector3f(1, 0, 0),
                (float) Math.toRadians(-this.rotationPitch), false);

        if (this.rotateOrderXY) {
            qX.mul(qY);
            this.rotation.set(qX.i(), qX.j(), qX.k(), qX.r());
        } else {
            qY.mul(qX);
            this.rotation.set(qY.i(), qY.j(), qY.k(), qY.r());

            if (extraYRotation != 0) {
                Quaternion qYExtra = new Quaternion(new Vector3f(0, 1, 0), extraYRotation, false);
                this.rotation.mul(qYExtra);
            }
        }
    }
}