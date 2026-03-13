package net.extendedrenderer.render;

import java.nio.FloatBuffer;
import java.util.*;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.CoroUtil.config.ConfigCoroUtil;
import net.extendedrenderer.shader.MeshBufferManagerParticle;
import net.extendedrenderer.particle.ParticleRegistry;
import net.extendedrenderer.particle.ShaderManager;
import net.extendedrenderer.particle.entity.EntityRotFX;
import net.extendedrenderer.shader.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.mixins.accessor.ActiveRenderInfoAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;

import com.google.common.collect.Queues;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

@OnlyIn(Dist.CLIENT)
public class RotatingParticleManager
{
    private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");

    protected ClientWorld world;

    public final LinkedHashMap<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> fxLayers = new LinkedHashMap<>();

    private final TextureManager renderer;

    private final Queue<Particle> queueEntityFX = Queues.newArrayDeque();

    public static int debugParticleRenderCount;
    public static int lastAmountToRender;
    public static boolean useShaders;

    public static FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    public static FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);

    public static boolean forceShaderReset = false;
    private static boolean forceVBO2Update = false;

    public static void markDirtyVBO2() {
        forceVBO2Update = true;
    }

    public RotatingParticleManager(ClientWorld worldIn, TextureManager rendererIn)
    {
        this.world = worldIn;
        this.renderer = rendererIn;
    }

    public void initNewArrayData(TextureAtlasSprite sprite) {
        List<ArrayDeque<Particle>[][]> list = new ArrayList<>();

        list.add(0, new ArrayDeque[4][]);
        list.add(1, new ArrayDeque[4][]);
        list.add(2, new ArrayDeque[4][]);

        for (ArrayDeque<Particle>[][] entry : list) {
            for (int i = 0; i < 4; ++i) {
                entry[i] = new ArrayDeque[2];
                for (int j = 0; j < 2; ++j) {
                    entry[i][j] = Queues.newArrayDeque();
                }
            }
        }

        fxLayers.put(sprite, list);
    }

    public void addEffect(Particle effect)
    {
        if (effect == null) return;
        this.queueEntityFX.add(effect);
    }

    public void updateEffects()
    {
        for (int i = 0; i < 4; ++i) {
            this.updateEffectLayer(i);
        }

        if (!this.queueEntityFX.isEmpty())
        {
            RotatingParticleManager.markDirtyVBO2();

            for (Particle particle = this.queueEntityFX.poll(); particle != null; particle = this.queueEntityFX.poll())
            {
                int j = 0;
                int k = 1;
                int renderOrder = 0;

                if (particle instanceof EntityRotFX) {
                    EntityRotFX rotFX = (EntityRotFX) particle;
                    j = rotFX.getFXLayer();
                    k = rotFX.shouldDisableDepth() ? 0 : 1;
                    renderOrder = rotFX.renderOrder;
                }

                TextureAtlasSprite sprite = null;
                if (particle instanceof EntityRotFX) {
                    sprite = ((EntityRotFX) particle).getParticleTexture();
                }

                if (sprite == null) continue;

                if (!fxLayers.containsKey(sprite)) {
                    initNewArrayData(sprite);
                }

                ArrayDeque<Particle>[][] entry = fxLayers.get(sprite).get(renderOrder);

                if (entry[j][k].size() >= 16384) {
                    entry[j][k].getFirst().remove();
                    entry[j][k].removeFirst();
                }

                entry[j][k].add(particle);
            }
        }
    }

    private void updateEffectLayer(int layer)
    {
        for (int i = 0; i < 2; ++i) {
            for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {
                for (ArrayDeque<Particle>[][] entry2 : entry1.getValue()) {
                    this.tickParticleList(entry2[layer][i]);
                }
            }
        }
    }

    private void tickParticleList(Queue<Particle> queue)
    {
        if (!queue.isEmpty())
        {
            Iterator<Particle> iterator = queue.iterator();
            while (iterator.hasNext())
            {
                Particle particle = iterator.next();
                this.tickParticle(particle);
                if (!particle.isAlive())
                {
                    iterator.remove();
                    RotatingParticleManager.markDirtyVBO2();
                }
            }
        }
    }

    private void tickParticle(final Particle particle)
    {
        try
        {
            particle.tick();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking Rotating Particle");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being ticked");
            crashreportcategory.setDetail("Rotating Particle", particle::toString);
            crashreportcategory.setDetail("Particle Type", () -> {
                if (particle instanceof EntityRotFX) {
                    int i = ((EntityRotFX) particle).getFXLayer();
                    return i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i));
                }
                return "Unknown";
            });
            throw new ReportedException(crashreport);
        }
    }

    public void renderParticles(Entity entityIn, ActiveRenderInfo activeRenderInfo, float partialTicks)
    {
        boolean useParticleShaders = useShaders && ConfigCoroUtil.particleShaders;

        ActiveRenderInfoAccessor ari = (ActiveRenderInfoAccessor) activeRenderInfo;
        Vector3f left = ari.getLeft();
        Vector3f up   = ari.getUp();

        float f  = left.x();
        float f1 = left.z();
        float f2 = up.x();
        float f3 = up.y();
        float f4 = up.z();

        Vector3d camPos = activeRenderInfo.getPosition();
        EntityRotFX.interpPosX = camPos.x;
        EntityRotFX.interpPosY = camPos.y;
        EntityRotFX.interpPosZ = camPos.z;

        Minecraft mc = Minecraft.getInstance();
        debugParticleRenderCount = 0;

        if (useParticleShaders) {
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_test);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_fire);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.downfall3);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_6);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.rain_white);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.snow);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.leaf);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_1);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_2);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_3);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.tumbleweed);
        }

        Transformation transformation = null;
        Matrix4fe viewMatrix = null;

        int glCalls = 0;
        int trueRenderCount = 0;
        int particles = 0;

        if (useParticleShaders) {
            ShaderProgram shaderProgram = ShaderEngine.renderer.getShaderProgram("particle");
            transformation = ShaderEngine.renderer.transformation;
            shaderProgram.bind();


            Matrix4fe projectionMatrix = new Matrix4fe();
            {
                FloatBuffer buf = BufferUtils.createFloatBuffer(16);
                GlStateManager._getMatrix(GL11.GL_PROJECTION_MATRIX, buf);
                buf.rewind();
                Matrix4fe.get(projectionMatrix, 0, buf);
            }

            boolean distantRendering = false;
            if (distantRendering) {
                float zNear = 0.05F;
                float zFar  = (float)(mc.options.renderDistance * 16) * 4F;
                projectionMatrix.m22 = (zFar + zNear) / (zNear - zFar);
                projectionMatrix.m32 = (zFar + zFar) * zNear / (zNear - zFar);
            }

            viewMatrix = new Matrix4fe();
            {
                FloatBuffer buf2 = BufferUtils.createFloatBuffer(16);
                GlStateManager._getMatrix(GL11.GL_MODELVIEW_MATRIX, buf2);
                buf2.rewind();
                Matrix4fe.get(viewMatrix, 0, buf2);
            }

            Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);
            shaderProgram.setUniformEfficient("modelViewMatrixCamera", modelViewMatrix, viewMatrixBuffer);


            shaderProgram.setUniform("texture_sampler", 0);




            mc.gameRenderer.lightTexture().turnOnLightLayer();
            shaderProgram.setUniform("lightmap_sampler", 1);

            RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);


            int glFogMode = glGetInteger(GL_FOG_MODE);
            int modeIndex = 0;
            if      (glFogMode == GL_LINEAR) modeIndex = 0;
            else if (glFogMode == GL_EXP)    modeIndex = 1;
            else if (glFogMode == GL_EXP2)   modeIndex = 2;
            shaderProgram.setUniform("fogmode", modeIndex);

            FloatBuffer fogColorBuf = BufferUtils.createFloatBuffer(4);
            GL11.glGetFloatv(GL11.GL_FOG_COLOR, fogColorBuf);
            float fogStart   = GL11.glGetFloat(GL11.GL_FOG_START);
            float fogEnd     = GL11.glGetFloat(GL11.GL_FOG_END);
            float fogDensity = GL11.glGetFloat(GL11.GL_FOG_DENSITY);
            shaderProgram.setUniform("fogStart",   fogStart);
            shaderProgram.setUniform("fogEnd",     fogEnd);
            shaderProgram.setUniform("fogDensity", fogDensity);
            shaderProgram.setUniform("fogColor",
                    fogColorBuf.get(0), fogColorBuf.get(1),
                    fogColorBuf.get(2), fogColorBuf.get(3));
        }

        for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {

            InstancedMeshParticle mesh = null;

            if (useParticleShaders) {
                mesh = MeshBufferManagerParticle.getMesh(entry1.getKey());
                if (mesh == null) {
                    MeshBufferManagerParticle.setupMeshForParticle(entry1.getKey());
                    mesh = MeshBufferManagerParticle.getMesh(entry1.getKey());
                }
            }

            if (mesh != null || !useParticleShaders) {
                for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                    for (int i_nf = 0; i_nf < 3; ++i_nf) {
                        final int i = i_nf;

                        for (int j = 0; j < 2; ++j) {
                            if (!entry[i][j].isEmpty()) {

                                switch (j) {
                                    case 0: RenderSystem.depthMask(false); break;
                                    case 1: RenderSystem.depthMask(true);  break;
                                }

                                switch (i) {
                                    default:
                                    case 0: mc.getTextureManager().bind(PARTICLE_TEXTURES);            break;
                                    case 1: mc.getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS); break;
                                }

                                if (useParticleShaders) {
                                    mesh.initRender();
                                    mesh.initRenderVBO1();

                                    mesh.instanceDataBuffer.clear();
                                    mesh.curBufferPos = 0;
                                    particles = entry[i][j].size();

                                    for (final Particle particle : entry[i][j]) {
                                        if (particle instanceof EntityRotFX) {
                                            EntityRotFX part = (EntityRotFX) particle;
                                            part.renderParticleForShader(mesh, transformation, viewMatrix, entityIn, partialTicks, f, f4, f1, f2, f3);
                                        }
                                    }

                                    mesh.instanceDataBuffer.limit(mesh.curBufferPos * mesh.INSTANCE_SIZE_FLOATS);

                                    GL15.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO);
                                    ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);

                                    ShaderManager.glDrawElementsInstanced(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0, mesh.curBufferPos);

                                    glCalls++;
                                    trueRenderCount += mesh.curBufferPos;

                                    GL15.glBindBuffer(GL_ARRAY_BUFFER, 0);

                                    mesh.endRenderVBO1();
                                    mesh.endRender();

                                } else {

                                    mc.gameRenderer.lightTexture().turnOnLightLayer();
                                    RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);


                                    boolean fogWasEnabled = GL11.glIsEnabled(GL11.GL_FOG);
                                    if (fogWasEnabled) GL11.glDisable(GL11.GL_FOG);

                                    Tessellator tessellator = Tessellator.getInstance();
                                    BufferBuilder vertexbuffer = tessellator.getBuilder();
                                    vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE);

                                    for (final Particle particle : entry[i][j]) {
                                        particle.render(vertexbuffer, activeRenderInfo, partialTicks);
                                        debugParticleRenderCount++;
                                    }

                                    tessellator.end();


                                    if (fogWasEnabled) GL11.glEnable(GL11.GL_FOG);
                                    mc.gameRenderer.lightTexture().turnOffLightLayer();
                                    RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
                                }
                            }
                        }
                    }
                }
            }
        }

        forceVBO2Update = false;

        if (useParticleShaders) {
            ShaderEngine.renderer.getShaderProgram("particle").unbind();
            mc.gameRenderer.lightTexture().turnOffLightLayer();
        }

        if (ConfigCoroUtil.debugShaders && this.world.getGameTime() % 60 == 0) {
            System.out.println("particles: " + particles);
            System.out.println("debugParticleRenderCount: " + debugParticleRenderCount);
            System.out.println("trueRenderCount: " + trueRenderCount);
            System.out.println("glCalls: " + glCalls);
        }
    }

    public void renderLitParticles(Entity entityIn, ActiveRenderInfo activeRenderInfo, float partialTick)
    {
        float f1 = MathHelper.cos(entityIn.yRot * 0.017453292F);
        float f2 = MathHelper.sin(entityIn.yRot * 0.017453292F);
        float f3 = -f2 * MathHelper.sin(entityIn.xRot * 0.017453292F);
        float f4 =  f1 * MathHelper.sin(entityIn.xRot * 0.017453292F);
        float f5 = MathHelper.cos(entityIn.xRot * 0.017453292F);

        for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {
            for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                for (int i = 0; i < 2; ++i) {
                    Queue<Particle> queue = entry[3][i];
                    if (!queue.isEmpty()) {
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder vertexbuffer = tessellator.getBuilder();
                        vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE);

                        for (Particle particle : queue) {
                            particle.render(vertexbuffer, activeRenderInfo, partialTick);
                        }

                        tessellator.end();
                    }
                }
            }
        }
    }

    public void clearEffects(@Nullable ClientWorld worldIn)
    {
        this.world = worldIn;

        for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {
            for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                for (int i = 0; i < entry.length; i++) {
                    for (int j = 0; j < entry[i].length; j++) {
                        if (entry[i][j] != null) {
                            entry[i][j].clear();
                        }
                    }
                }
            }
        }
    }

    public String getStatistics()
    {
        int count = 0;
        return "" + count;
    }
}