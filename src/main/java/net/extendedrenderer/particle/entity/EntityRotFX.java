package net.extendedrenderer.particle.entity;

import net.CoroUtil.api.weather.IWindHandler;
import net.CoroUtil.util.CoroUtilBlockLightCache;
import net.CoroUtil.util.Vec3;
import net.extendedrenderer.ExtendedRenderer;
import net.extendedrenderer.particle.behavior.ParticleBehaviors;
import net.extendedrenderer.render.RotatingParticleManager;
import net.extendedrenderer.shader.IShaderRenderedEntity;
import net.extendedrenderer.shader.InstancedMeshParticle;
import net.extendedrenderer.shader.Matrix4fe;
import net.extendedrenderer.shader.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.IVertexBuilder;

@OnlyIn(Dist.CLIENT)
public class EntityRotFX extends Particle implements IWindHandler, IShaderRenderedEntity
{
    public static double interpPosX;
    public static double interpPosY;
    public static double interpPosZ;

    public boolean weatherEffect = false;

    public float spawnY = -1;

    public int particleTextureIndexInt = 0;

    public float brightness = 0.7F;

    protected TextureAtlasSprite sprite = null;

    public ParticleBehaviors pb = null;

    public boolean callUpdateSuper = true;
    public boolean callUpdatePB = true;

    public float renderRange = 128F;

    public int renderOrder = 0;

    private int entityID = 0;

    public int debugID = 0;

    public float rotationYaw;
    public float rotationPitch;

    public float windWeight = 5;
    public int particleDecayExtra = 0;
    public boolean isTransparent = true;

    public boolean killOnCollide = false;

    public boolean facePlayer = false;

    public boolean facePlayerYaw = false;

    public boolean vanillaMotionDampen = true;

    public double aboveGroundHeight = 4.5D;
    public boolean checkAheadToBounce = true;
    public boolean collisionSpeedDampen = true;

    public double bounceSpeed = 0.05D;
    public double bounceSpeedMax = 0.15D;
    public double bounceSpeedAhead = 0.35D;
    public double bounceSpeedMaxAhead = 0.25D;

    public boolean spinFast = false;

    private float ticksFadeInMax = 0;
    private float ticksFadeOutMax = 0;

    private boolean dontRenderUnderTopmostBlock = false;

    private boolean killWhenUnderTopmostBlock = false;
    private int killWhenUnderTopmostBlock_ScanAheadRange = 0;

    public int killWhenUnderCameraAtLeast = 0;

    public int killWhenFarFromCameraAtLeast = 0;

    private float ticksFadeOutMaxOnDeath = -1;
    private float ticksFadeOutCurOnDeath = 0;
    protected boolean fadingOut = false;

    public float avoidTerrainAngle = 0;

    public boolean useRotationAroundCenter = false;
    public float rotationAroundCenter = 0;
    public float rotationAroundCenterPrev = 0;
    public float rotationSpeedAroundCenter = 0;
    public float rotationDistAroundCenter = 0;

    private boolean slantParticleToWind = false;

    public Quaternion rotation;
    public Quaternion rotationPrev;

    public boolean quatControl = false;

    public boolean fastLight = false;

    public float brightnessCache = 0.5F;

    public boolean rotateOrderXY = false;

    public float extraYRotation = 0;

    public boolean isCollidedHorizontally = false;
    public boolean isCollidedVerticallyDownwards = false;
    public boolean isCollidedVerticallyUpwards = false;

    public float particleScale = 1.0F;

    public Vector3f rotationAround = new Vector3f(0, 0, 0);

    public EntityRotFX(ClientWorld par1World, double par2, double par4, double par6,
                       double par8, double par10, double par12)
    {
        super(par1World, par2, par4, par6, par8, par10, par12);

        this.xd = par8;
        this.yd = par10;
        this.zd = par12;

        this.setSize(0.3F, 0.3F);
        this.entityID = par1World.random.nextInt(100000);

        rotation     = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
        rotationPrev = new Quaternion(rotation.i(), rotation.j(), rotation.k(), rotation.r());

        brightnessCache = CoroUtilBlockLightCache.getBrightnessCached(
                this.level, (float)this.x, (float)this.y, (float)this.z);
    }

    public boolean isSlantParticleToWind() {
        return slantParticleToWind;
    }

    public void setSlantParticleToWind(boolean slantParticleToWind) {
        this.slantParticleToWind = slantParticleToWind;
    }

    public float getTicksFadeOutMaxOnDeath() {
        return ticksFadeOutMaxOnDeath;
    }

    public void setTicksFadeOutMaxOnDeath(float ticksFadeOutMaxOnDeath) {
        this.ticksFadeOutMaxOnDeath = ticksFadeOutMaxOnDeath;
    }

    public boolean isKillWhenUnderTopmostBlock() {
        return killWhenUnderTopmostBlock;
    }

    public void setKillWhenUnderTopmostBlock(boolean killWhenUnderTopmostBlock) {
        this.killWhenUnderTopmostBlock = killWhenUnderTopmostBlock;
    }

    public boolean isDontRenderUnderTopmostBlock() {
        return dontRenderUnderTopmostBlock;
    }

    public void setDontRenderUnderTopmostBlock(boolean dontRenderUnderTopmostBlock) {
        this.dontRenderUnderTopmostBlock = dontRenderUnderTopmostBlock;
    }

    public float getTicksFadeInMax() {
        return ticksFadeInMax;
    }

    public void setTicksFadeInMax(float ticksFadeInMax) {
        this.ticksFadeInMax = ticksFadeInMax;
    }

    public float getTicksFadeOutMax() {
        return ticksFadeOutMax;
    }

    public void setTicksFadeOutMax(float ticksFadeOutMax) {
        this.ticksFadeOutMax = ticksFadeOutMax;
    }

    public int getParticleTextureIndex()
    {
        return this.particleTextureIndexInt;
    }

    public void setMaxAge(int par) {
        this.lifetime = par;
    }

    public float getRedColorF()   { return this.rCol; }
    public float getGreenColorF() { return this.gCol; }
    public float getBlueColorF()  { return this.bCol; }

    public float getAlphaF()
    {
        return this.alpha;
    }

    public void setAlphaF(float alpha) {
        this.setAlpha(alpha);
    }

    @Override
    public void remove() {
        if (pb != null) pb.particles.remove(this);
        super.remove();
    }

    @Override
    public void tick() {
        super.tick();

        Entity ent = Minecraft.getInstance().getCameraEntity();

        if (!isVanillaMotionDampen()) {
            this.xd /= 0.9800000190734863D;
            this.yd /= 0.9800000190734863D;
            this.zd /= 0.9800000190734863D;
        }

        if (!this.removed && !fadingOut) {
            if (killOnCollide) {
                if (this.isCollided()) {
                    startDeath();
                }
            }

            if (killWhenUnderTopmostBlock) {
                int height = this.level.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING, new BlockPos(this.x, this.y, this.z)).getY();
                if (this.y - killWhenUnderTopmostBlock_ScanAheadRange <= height) {
                    startDeath();
                }
            }

            if (killWhenUnderCameraAtLeast != 0) {
                if (this.y < ent.getY() - killWhenUnderCameraAtLeast) {
                    startDeath();
                }
            }

            if (killWhenFarFromCameraAtLeast != 0) {
                if (getAge() > 20 && getAge() % 5 == 0) {
                    double d0 = this.x - ent.getX();
                    double d1 = this.y - ent.getY();
                    double d2 = this.z - ent.getZ();
                    if (Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) > killWhenFarFromCameraAtLeast) {
                        startDeath();
                    }
                }
            }
        }

        if (!collisionSpeedDampen) {
            if (this.onGround) {
                this.xd /= 0.699999988079071D;
                this.zd /= 0.699999988079071D;
            }
        }

        if (spinFast) {
            this.rotationPitch += this.entityID % 2 == 0 ? 10 : -10;
            this.rotationYaw   += this.entityID % 2 == 0 ? -10 : 10;
        }

        if (!fadingOut) {
            if (ticksFadeInMax > 0 && this.getAge() < ticksFadeInMax) {
                this.setAlphaF((float)this.getAge() / ticksFadeInMax);
            } else if (ticksFadeOutMax > 0 && this.getAge() > this.getMaxAge() - ticksFadeOutMax) {
                float count = this.getAge() - (this.getMaxAge() - ticksFadeOutMax);
                float val = (ticksFadeOutMax - count) / ticksFadeOutMax;
                this.setAlphaF(val);
            } else if (ticksFadeInMax > 0 || ticksFadeOutMax > 0) {
                this.setAlphaF(1F);
            }
        } else {
            if (ticksFadeOutCurOnDeath < ticksFadeOutMaxOnDeath) {
                ticksFadeOutCurOnDeath++;
            } else {
                this.remove();
            }
            float val = 1F - (ticksFadeOutCurOnDeath / ticksFadeOutMaxOnDeath);
            this.setAlphaF(val);
        }

        if (this.level.getGameTime() % 5 == 0) {
            brightnessCache = CoroUtilBlockLightCache.getBrightnessCached(this.level, (float)this.x, (float)this.y, (float)this.z);
        }

        rotationAroundCenter += rotationSpeedAroundCenter;
        rotationAroundCenter %= 360;

        tickExtraRotations();
    }

    public void tickExtraRotations() {
        if (slantParticleToWind) {
            double motionXZ = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
            rotationPitch = (float)Math.atan2(this.yd, motionXZ);
        }

        if (!quatControl) {
            rotationPrev = new Quaternion(rotation.i(), rotation.j(), rotation.k(), rotation.r());
            Entity ent = Minecraft.getInstance().getCameraEntity();
            updateQuaternion(ent);
        }
    }

    public void startDeath() {
        if (ticksFadeOutMaxOnDeath > 0) {
            ticksFadeOutCurOnDeath = 0;
            fadingOut = true;
        } else {
            this.remove();
        }
    }

    public void setParticleTextureIndex(int par1)
    {
        this.particleTextureIndexInt = par1;
    }

    public int getFXLayer()
    {
        return 5;
    }

    public void spawnAsWeatherEffect()
    {
        weatherEffect = true;
        ExtendedRenderer.rotEffRenderer.addEffect(this);
    }

    public int getAge()
    {
        return this.age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

    public int getMaxAge()
    {
        return this.lifetime;
    }

    @Override
    public void setSize(float par1, float par2)
    {
        super.setSize(par1, par2);
        this.setPos(this.x, this.y, this.z);
    }

    public void setGravity(float par) {
        this.gravity = par;
    }

    public float maxRenderRange() {
        return renderRange;
    }

    public void setScale(float parScale) {
        this.particleScale = parScale;
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f((float)this.x, (float)this.y, (float)this.z);
    }

    @Override
    public Quaternion getQuaternion() {
        return this.rotation;
    }

    @Override
    public Quaternion getQuaternionPrev() {
        return this.rotationPrev;
    }

    @Override
    public float getScale() {
        return this.particleScale;
    }

    public Vec3 getPos() {
        return new Vec3(this.x, this.y, this.z);
    }

    public double getPosX() { return this.x; }
    public void setPosX(double posX) { this.x = posX; }

    public double getPosY() { return this.y; }
    public void setPosY(double posY) { this.y = posY; }

    public double getPosZ() { return this.z; }
    public void setPosZ(double posZ) { this.z = posZ; }

    public double getMotionX() { return this.xd; }
    public void setMotionX(double v) { this.xd = v; }

    public double getMotionY() { return this.yd; }
    public void setMotionY(double v) { this.yd = v; }

    public double getMotionZ() { return this.zd; }
    public void setMotionZ(double v) { this.zd = v; }

    public double getPrevPosX() { return this.xo; }
    public void setPrevPosX(double v) { this.xo = v; }

    public double getPrevPosY() { return this.yo; }
    public void setPrevPosY(double v) { this.yo = v; }

    public double getPrevPosZ() { return this.zo; }
    public void setPrevPosZ(double v) { this.zo = v; }

    public int getEntityId() { return entityID; }

    public ClientWorld getWorld() { return this.level; }

    public void setCanCollide(boolean val) { this.hasPhysics = val; }
    public boolean getCanCollide() { return this.hasPhysics; }

    public boolean isCollided() { return this.onGround; }

    public double getDistance(double x, double y, double z)
    {
        double d0 = this.x - x;
        double d1 = this.y - y;
        double d2 = this.z - z;
        return MathHelper.sqrt((float)(d0 * d0 + d1 * d1 + d2 * d2));
    }

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        float rotationX;
        float rotationZ;
        float rotationYZ;
        float rotationXY;
        float rotationXZ;

        if (!facePlayer) {
            rotationX  =  MathHelper.cos(this.rotationYaw   * (float)Math.PI / 180.0F);
            rotationYZ =  MathHelper.sin(this.rotationYaw   * (float)Math.PI / 180.0F);
            rotationXY = -rotationYZ * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
            rotationXZ =  rotationX  * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
            rotationZ  =  MathHelper.cos(this.rotationPitch * (float)Math.PI / 180.0F);
        } else {
            Quaternion q = renderInfo.rotation();
            rotationX  =  1.0F - 2.0F * (q.j() * q.j() + q.k() * q.k());
            rotationZ  =  1.0F - 2.0F * (q.i() * q.i() + q.j() * q.j());
            rotationYZ =  2.0F * (q.i() * q.j() - q.k() * q.r());
            rotationXY =  2.0F * (q.i() * q.k() + q.j() * q.r());
            rotationXZ =  2.0F * (q.j() * q.k() - q.i() * q.r());
        }

        renderBillboard(buffer, renderInfo, partialTicks,
                rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    protected void renderBillboard(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks,
                                   float rotationX, float rotationZ,
                                   float rotationYZ, float rotationXY, float rotationXZ) {
        if (this.sprite == null) return;

        float interpX = (float)(this.xo + (this.x - this.xo) * partialTicks - interpPosX);
        float interpY = (float)(this.yo + (this.y - this.yo) * partialTicks - interpPosY);
        float interpZ = (float)(this.zo + (this.z - this.zo) * partialTicks - interpPosZ);

        float s = this.particleScale * 0.1F;

        float u0 = this.sprite.getU0();
        float u1 = this.sprite.getU1();
        float v0 = this.sprite.getV0();
        float v1 = this.sprite.getV1();

        float bright = Math.max(0.0f, Math.min(1.0f, brightnessCache));
        int r = (int)(this.rCol * bright * 255);
        int g = (int)(this.gCol * bright * 255);
        int b = (int)(this.bCol * bright * 255);
        int a = (int)(this.alpha * 255);

        int light = this.getLightColor(partialTicks);

        buffer.vertex(interpX + s * (-rotationX - rotationXY),
                        interpY + s * (-rotationZ),
                        interpZ + s * (-rotationYZ - rotationXZ))
                .uv(u1, v1).color(r, g, b, a).uv2(light).endVertex();

        buffer.vertex(interpX + s * (-rotationX + rotationXY),
                        interpY + s * ( rotationZ),
                        interpZ + s * (-rotationYZ + rotationXZ))
                .uv(u1, v0).color(r, g, b, a).uv2(light).endVertex();

        buffer.vertex(interpX + s * ( rotationX + rotationXY),
                        interpY + s * ( rotationZ),
                        interpZ + s * ( rotationYZ + rotationXZ))
                .uv(u0, v0).color(r, g, b, a).uv2(light).endVertex();

        buffer.vertex(interpX + s * ( rotationX - rotationXY),
                        interpY + s * (-rotationZ),
                        interpZ + s * ( rotationYZ - rotationXZ))
                .uv(u0, v1).color(r, g, b, a).uv2(light).endVertex();
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.NO_RENDER;
    }

    public void renderParticleForShader(InstancedMeshParticle mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
                                        float partialTicks, float rotationX, float rotationZ,
                                        float rotationYZ, float rotationXY, float rotationXZ) {

        if (mesh.curBufferPos >= mesh.numInstances) return;

        float posX = (float)(this.xo + (this.x - this.xo) * (double) partialTicks - EntityRotFX.interpPosX);
        float posY = (float)(this.yo + (this.y - this.yo) * (double) partialTicks - EntityRotFX.interpPosY);
        float posZ = (float)(this.zo + (this.z - this.zo) * (double) partialTicks - EntityRotFX.interpPosZ);
        Vector3f pos = new Vector3f(posX, posY, posZ);

        Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos, partialTicks);
        modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos, mesh.instanceDataBuffer);

        float brightness = brightnessCache;
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + mesh.MATRIX_SIZE_FLOATS, brightness);

        int rgbaIndex = 0;
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.rCol);
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.gCol);
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.bCol);
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.alpha);

        mesh.curBufferPos++;
    }

    @Override
    public float getWindWeight() { return windWeight; }

    @Override
    public int getParticleDecayExtra() { return particleDecayExtra; }

    public boolean shouldDisableDepth() {
        return isTransparent;
    }

    public void setKillOnCollide(boolean val) { this.killOnCollide = val; }

    @Override
    public void move(double x, double y, double z)
    {
        double yy = y, xx = x, zz = z;

        if (this.hasPhysics) {
            net.minecraft.util.math.vector.Vector3d movement = net.minecraft.entity.Entity.collideBoundingBoxHeuristically(
                    null,
                    new net.minecraft.util.math.vector.Vector3d(x, y, z),
                    this.getBoundingBox(),
                    this.level,
                    net.minecraft.util.math.shapes.ISelectionContext.empty(),
                    new net.minecraft.util.ReuseableStream<>(java.util.stream.Stream.empty())
            );
            x = movement.x;
            y = movement.y;
            z = movement.z;
        }

        if (x != 0.0D || y != 0.0D || z != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().move(x, y, z));
            this.setLocationFromBoundingbox();
        }


        this.onGround = (yy != y && yy < 0.0D);
        this.isCollidedHorizontally = (xx != x || zz != z);
        this.isCollidedVerticallyDownwards = (yy < y);
        this.isCollidedVerticallyUpwards = (yy > y);

        if (xx != x) this.xd = 0.0D;
        if (zz != z) this.zd = 0.0D;
    }

    public void setFacePlayer(boolean val) { this.facePlayer = val; }

    public TextureAtlasSprite getParticleTexture() { return this.sprite; }

    public void setSprite(TextureAtlasSprite sprite) { this.sprite = sprite; }

    public boolean isVanillaMotionDampen() { return vanillaMotionDampen; }

    public void setVanillaMotionDampen(boolean v) { this.vanillaMotionDampen = v; }

    @Override
    public void setColor(float r, float g, float b) {
        super.setColor(r, g, b);
        RotatingParticleManager.markDirtyVBO2();
    }

    @Override
    protected void setAlpha(float alpha) {
        super.setAlpha(alpha);
        RotatingParticleManager.markDirtyVBO2();
    }

    public void updateQuaternion(Entity camera) {
        if (camera != null) {
            if (this.facePlayer) {
                this.rotationYaw   = camera.yRot;
                this.rotationPitch = camera.xRot;
            } else if (facePlayerYaw) {
                this.rotationYaw = camera.yRot;
            }
        }

        Quaternion qY = new Quaternion(new Vector3f(0, 1, 0), (float)Math.toRadians(-this.rotationYaw - 180F), false);
        Quaternion qX = new Quaternion(new Vector3f(1, 0, 0), (float)Math.toRadians(-this.rotationPitch), false);

        if (this.rotateOrderXY) {
            qX.mul(qY);
            this.rotation.set(qX.i(), qX.j(), qX.k(), qX.r());
        } else {
            qY.mul(qX);
            this.rotation.set(qY.i(), qY.j(), qY.k(), qY.r());
        }
    }

    public int getKillWhenUnderTopmostBlock_ScanAheadRange() {
        return killWhenUnderTopmostBlock_ScanAheadRange;
    }

    public void setKillWhenUnderTopmostBlock_ScanAheadRange(int v) {
        this.killWhenUnderTopmostBlock_ScanAheadRange = v;
    }

    public boolean isCollidedVertically() {
        return isCollidedVerticallyDownwards || isCollidedVerticallyUpwards;
    }
}