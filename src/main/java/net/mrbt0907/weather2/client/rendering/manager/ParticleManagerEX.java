package net.mrbt0907.weather2.client.rendering.manager;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import net.extendedrenderer.particle.ShaderManager;
import net.extendedrenderer.particle.entity.EntityRotFX;
import net.extendedrenderer.render.RotatingParticleManager;
import net.extendedrenderer.shader.InstancedMeshParticle;
import net.extendedrenderer.shader.Matrix4fe;
import net.extendedrenderer.shader.MeshBufferManagerParticle;
import net.extendedrenderer.shader.ShaderEngine;
import net.extendedrenderer.shader.ShaderProgram;
import net.extendedrenderer.shader.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.mrbt0907.weather2.client.rendering.shaders.VolumetricRenderer;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.mixins.accessor.ActiveRenderInfoAccessor;
import net.mrbt0907.weather2.mixins.accessor.ParticleAccessor;
import net.mrbt0907.weather2.util.Maths;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class ParticleManagerEX extends RotatingParticleManager
{
    protected static final Minecraft MC = Minecraft.getInstance();
    protected static final Tessellator TESSELLATOR = Tessellator.getInstance();
    protected static final ResourceLocation PARTICLE_TEXTURES =
            new ResourceLocation("textures/particle/particles.png");

    protected final TextureManager renderer;

    private FloatBuffer projectionBuffer;
    private FloatBuffer modelViewBuffer;

    private final Map<Particle, Double> distanceCache = new HashMap<>(10000);
    public final Comparator<? super Particle> COMPARE_DISTANCE =
            (a, b) -> Double.compare(distanceCache.get(b), distanceCache.get(a));

    private final List<Particle> layerA = new ArrayList<>(10000);
    private final List<Particle> layerB = new ArrayList<>(10000);
    private final List<Particle> layerC = new ArrayList<>(10000);
    private final List<Particle> layerD = new ArrayList<>(10000);
    private final Map<Particle, InstancedMeshParticle> layerMesh = new HashMap<>();

    public ParticleManagerEX(ClientWorld world, TextureManager renderer)
    {
        super(world, renderer);
        this.renderer = renderer;
    }

    @Override
    public void renderParticles(Entity entityIn, ActiveRenderInfo activeRenderInfo, float partialTicks)
    {
        if (ConfigClient.enable_legacy_rendering)
        {
            super.renderParticles(entityIn, activeRenderInfo, partialTicks);
            return;
        }

        Vector3d camPos = activeRenderInfo.getPosition();
        EntityRotFX.interpPosX = camPos.x;
        EntityRotFX.interpPosY = camPos.y;
        EntityRotFX.interpPosZ = camPos.z;

        RotatingParticleManager.debugParticleRenderCount = 0;

        boolean useParticleShaders = false;

        Matrix4fe viewMatrix     = null;
        Transformation transformation = null;
        ShaderProgram shaderProgram   = null;

        if (useParticleShaders)
        {
            shaderProgram  = ShaderEngine.renderer.getShaderProgram("particle");
            transformation = ShaderEngine.renderer.transformation;
            shaderProgram.bind();

            if (projectionBuffer == null) {
                projectionBuffer = BufferUtils.createFloatBuffer(16);
                modelViewBuffer  = BufferUtils.createFloatBuffer(16);
            }

            projectionBuffer.clear();
            GlStateManager._getMatrix(GL11.GL_PROJECTION_MATRIX, projectionBuffer);
            projectionBuffer.rewind();
            Matrix4fe projectionMatrix = new Matrix4fe();
            Matrix4fe.get(projectionMatrix, 0, projectionBuffer);

            viewMatrix = new Matrix4fe();
            modelViewBuffer.clear();
            GlStateManager._getMatrix(GL11.GL_MODELVIEW_MATRIX, modelViewBuffer);
            modelViewBuffer.rewind();
            Matrix4fe.get(viewMatrix, 0, modelViewBuffer);

            Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);
            shaderProgram.setUniformEfficient("modelViewMatrixCamera", modelViewMatrix,
                    RotatingParticleManager.viewMatrixBuffer);
            shaderProgram.setUniform("texture_sampler", 0);

            int glFogMode = GL11.glGetInteger(GL11.GL_FOG_MODE);
            int modeIndex = (glFogMode == GL11.GL_EXP2) ? 2 : (glFogMode == GL11.GL_EXP ? 1 : 0);
            shaderProgram.setUniform("fogmode", modeIndex);
        }

        ActiveRenderInfoAccessor ari = (ActiveRenderInfoAccessor) activeRenderInfo;
        Vector3f left = ari.getLeft();
        Vector3f up   = ari.getUp();

        layerA.clear();
        layerB.clear();
        layerC.clear();
        layerD.clear();
        layerMesh.clear();

        for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet())
        {
            TextureAtlasSprite key = entry1.getKey();
            if (key == null) continue;

            InstancedMeshParticle mesh = useParticleShaders
                    ? MeshBufferManagerParticle.getMesh(key) : null;

            if (useParticleShaders && mesh == null) {
                MeshBufferManagerParticle.setupMeshForParticle(key);
                mesh = MeshBufferManagerParticle.getMesh(key);
            }

            for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 2; j++) {
                        if (entry[i][j].isEmpty()) continue;
                        for (Particle particle : entry[i][j]) {
                            if (i != 1) {
                                if (j == 1) layerA.add(particle);
                                else        layerC.add(particle);
                            } else {
                                if (j == 1) layerB.add(particle);
                                else        layerD.add(particle);
                            }
                            if (mesh != null) layerMesh.put(particle, mesh);
                        }
                    }
                }
            }
        }

        computeDistanceCache(entityIn, layerA, layerB, layerC, layerD);

        RenderSystem.pushMatrix();
        renderLayer(entityIn, activeRenderInfo, partialTicks, viewMatrix, transformation,
                layerA, PARTICLE_TEXTURES,            true,  useParticleShaders);
        renderLayer(entityIn, activeRenderInfo, partialTicks, viewMatrix, transformation,
                layerB, AtlasTexture.LOCATION_BLOCKS, true,  useParticleShaders);
        renderLayer(entityIn, activeRenderInfo, partialTicks, viewMatrix, transformation,
                layerC, PARTICLE_TEXTURES,            false, useParticleShaders);
        renderLayer(entityIn, activeRenderInfo, partialTicks, viewMatrix, transformation,
                layerD, AtlasTexture.LOCATION_BLOCKS, false, useParticleShaders);
        RenderSystem.popMatrix();

        if (useParticleShaders && shaderProgram != null)
            shaderProgram.unbind();
    }

    @SuppressWarnings("unchecked")
    private void computeDistanceCache(Entity player, List<Particle>... layers)
    {
        distanceCache.clear();
        double px = player.getX(), py = player.getY(), pz = player.getZ();
        for (List<Particle> list : layers) {
            for (Particle p : list) {
                ParticleAccessor pa = (ParticleAccessor)(Object) p;
                distanceCache.put(p, Maths.distance(px, py, pz,
                        pa.getX(), pa.getY(), pa.getZ()));
            }
        }
    }

    private void renderLayer(Entity entity, ActiveRenderInfo activeRenderInfo, float partialTicks,
                             Matrix4fe viewMatrix, Transformation transformation,
                             List<Particle> particles, ResourceLocation texture,
                             boolean depthMask, boolean useParticleShaders)
    {
        if (particles.isEmpty()) return;

        RenderSystem.depthMask(depthMask);
        renderer.bind(texture);

        particles.sort(COMPARE_DISTANCE);

        if (ConfigClient.enable_volumetrics)
            VolumetricRenderer.render(entity, new ArrayList<>(particles), partialTicks);

        if (useParticleShaders)
        {
            ActiveRenderInfoAccessor ari = (ActiveRenderInfoAccessor) activeRenderInfo;
            Vector3f left = ari.getLeft();
            Vector3f up   = ari.getUp();
            float rotationX  = left.x();
            float rotationZ  = left.z();
            float rotationYZ = up.x();
            float rotationXY = up.y();
            float rotationXZ = up.z();

            Map<InstancedMeshParticle, List<Particle>> grouped = new HashMap<>();
            for (Particle p : particles) {
                InstancedMeshParticle mesh = layerMesh.get(p);
                if (mesh == null) continue;
                grouped.computeIfAbsent(mesh, k -> new ArrayList<>()).add(p);
            }

            for (Map.Entry<InstancedMeshParticle, List<Particle>> e : grouped.entrySet())
            {
                InstancedMeshParticle mesh = e.getKey();
                mesh.initRender();
                mesh.initRenderVBO1();
                mesh.instanceDataBuffer.clear();
                mesh.curBufferPos = 0;

                for (Particle p : e.getValue()) {
                    if (p instanceof EntityRotFX)
                        ((EntityRotFX) p).renderParticleForShader(
                                mesh, transformation, viewMatrix, entity, partialTicks,
                                rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
                }

                mesh.instanceDataBuffer.limit(mesh.curBufferPos * InstancedMeshParticle.INSTANCE_SIZE_FLOATS);

                GL15.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO);
                ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);
                ShaderManager.glDrawElementsInstanced(GL_TRIANGLES, mesh.getVertexCount(),
                        GL_UNSIGNED_INT, 0, mesh.curBufferPos);

                GL15.glBindBuffer(GL_ARRAY_BUFFER, 0);
                mesh.endRenderVBO1();
                mesh.endRender();
            }
        }
        else
        {
            BufferBuilder vertexbuffer = TESSELLATOR.getBuilder();
            vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE);

            for (Particle p : particles)
            {
                p.render(vertexbuffer, activeRenderInfo, partialTicks);
                RotatingParticleManager.debugParticleRenderCount++;
            }

            TESSELLATOR.end();
        }

        particles.clear();
    }
}