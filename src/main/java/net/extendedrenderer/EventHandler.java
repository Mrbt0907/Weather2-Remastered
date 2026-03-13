package net.extendedrenderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.CoroUtil.config.ConfigCoroUtil;
import net.CoroUtil.forge.CULog;
import net.CoroUtil.util.CoroUtilBlockLightCache;
import net.extendedrenderer.particle.ParticleRegistry;
import net.extendedrenderer.particle.ShaderManager;
import net.extendedrenderer.render.RotatingParticleManager;
import net.extendedrenderer.shader.ShaderEngine;
import net.extendedrenderer.shader.ShaderListenerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import org.lwjgl.opengl.GL11;

public class EventHandler {

    public static World lastWorld;

    public static int mip_min = GL11.GL_NEAREST;
    public static int mip_mag = GL11.GL_NEAREST;

    public static float sandstormFogAmount = 0F;

    public static boolean foliageUseLast;

    public static boolean flagFoliageUpdate = false;

    public static boolean lastLightningBoltLightState = false;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void tickRenderScreen(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickShaderTest();
        }
    }

    public static void tickShaderTest() {
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void tickClient(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {

            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                if (!isPaused()) {
                    ExtendedRenderer.rotEffRenderer.updateEffects();

                    boolean lightningActive = mc.level.isThundering();

                    if (mc.level.getGameTime() % 2 == 0 || lightningActive != lastLightningBoltLightState) {
                        CoroUtilBlockLightCache.clear();
                    }

                    lastLightningBoltLightState = lightningActive;
                }
            }

            if (ConfigCoroUtil.foliageShaders != foliageUseLast) {
                foliageUseLast = ConfigCoroUtil.foliageShaders;
                flagFoliageUpdate = true;
            }

            if (flagFoliageUpdate) {
                CULog.dbg("CoroUtil detected a need to reload resource packs, initiating");
                flagFoliageUpdate = false;
                Minecraft.getInstance().reloadResourcePacks();
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void worldRender(RenderWorldLastEvent event) {
        if (!ConfigCoroUtil.useEntityRenderHookForShaders) {
            EventHandler.hookRenderShaders(event.getPartialTicks());
        }
    }

    public static boolean queryUseOfShaders() {
        RotatingParticleManager.useShaders = ShaderManager.canUseShadersInstancedRendering();

        if (ConfigCoroUtil.forceShadersOff) {
            RotatingParticleManager.useShaders = false;
        }

        return RotatingParticleManager.useShaders;
    }

    @OnlyIn(Dist.CLIENT)
    public static void hookRenderShaders(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) return;

        if (lastWorld != mc.level) {
            CULog.log("CoroUtil: resetting rotating particle renderer");
            ExtendedRenderer.rotEffRenderer.clearEffects(mc.level);
            lastWorld = mc.level;
        }

        ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getMainCamera();

        if (!ConfigCoroUtil.disableParticleRenderer) {

            mc.gameRenderer.lightTexture().turnOnLightLayer();

            mc.getProfiler().popPush("litParticles");
            ExtendedRenderer.rotEffRenderer.renderLitParticles(
                    (Entity) mc.getCameraEntity(), activeRenderInfo, partialTicks);

            RenderHelper.turnOff();

            mc.getProfiler().popPush("particles");

            queryUseOfShaders();

            if (RotatingParticleManager.forceShaderReset) {
                CULog.log("Extended Renderer: Resetting shaders");
                RotatingParticleManager.forceShaderReset = false;
                ShaderEngine.cleanup();
                ShaderListenerRegistry.postReset();
                ExtendedRenderer.foliageRenderer.foliage.clear();
                ShaderEngine.renderer = null;
                ShaderManager.resetCheck();
            }

            if (RotatingParticleManager.useShaders && ShaderEngine.renderer == null) {
                boolean simulateFail = false;
                if (!ShaderEngine.init() || simulateFail) {
                    CULog.log("Extended Renderer: Shaders failed to initialize");
                    ShaderManager.disableShaders();
                    RotatingParticleManager.useShaders = false;
                } else {
                    CULog.log("Extended Renderer: Initialized instanced rendering shaders");
                    ShaderListenerRegistry.postInit();
                }
            }

            preShaderRender((Entity) mc.getCameraEntity(), partialTicks);

            if (ConfigCoroUtil.foliageShaders) {
                ExtendedRenderer.foliageRenderer.render((Entity) mc.getCameraEntity(), partialTicks);
            }

            ExtendedRenderer.rotEffRenderer.renderParticles(
                    (Entity) mc.getCameraEntity(), activeRenderInfo, partialTicks);

            postShaderRender((Entity) mc.getCameraEntity(), partialTicks);

            mc.gameRenderer.lightTexture().turnOffLightLayer();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void preShaderRender(Entity entityIn, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager._alphaFunc(GL11.GL_GREATER, 0.004F);

        boolean fog = true;
        if (fog) {
            ActiveRenderInfo ari = mc.gameRenderer.getMainCamera();
            if (mc.level instanceof ClientWorld) {
                ClientWorld clientWorld = (ClientWorld) mc.level;
                FogRenderer.setupColor(ari, partialTicks, clientWorld,
                        mc.options.renderDistance,
                        mc.gameRenderer.getDarkenWorldAmount(partialTicks));
                FogRenderer.setupFog(ari, FogRenderer.FogType.FOG_TERRAIN,
                        Math.max(32.0F, mc.options.renderDistance * 16.0F), false);
            }
        }

        RenderSystem.disableCull();

        CoroUtilBlockLightCache.brightnessPlayer = CoroUtilBlockLightCache.getBrightnessFromLightmap(
                mc.level, (float) entityIn.getX(), (float) entityIn.getY(), (float) entityIn.getZ());

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        mip_min = 0;
        mip_mag = 0;

        if (!ConfigCoroUtil.disableMipmapFix) {
            mc.textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
            mip_min = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
            mip_mag = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void postShaderRender(Entity entityIn, float partialTicks) {
        if (!ConfigCoroUtil.disableMipmapFix && mip_min != 0 && mip_mag != 0) {
            Minecraft.getInstance().textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mip_min);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mip_mag);
        }

        RenderSystem.enableCull();

        boolean fog = true;
        if (fog) {
            RenderSystem.disableFog();
        }

        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        GlStateManager._alphaFunc(GL11.GL_GREATER, 0.1F);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isPaused() {
        return Minecraft.getInstance().isPaused();
    }

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(
            modid = ExtendedRenderer.modid,
            bus   = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerIcons(TextureStitchEvent.Pre event) {
            ParticleRegistry.init(event);
        }

        @SubscribeEvent
        public static void registerIconsPost(TextureStitchEvent.Post event) {
            ParticleRegistry.initPost(event);
        }

        @SubscribeEvent
        public static void modelBake(ModelBakeEvent event) {
            FoliageEnhancerShader.modelBakeEvent(event);
        }
    }
}