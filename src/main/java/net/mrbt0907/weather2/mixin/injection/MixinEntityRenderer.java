package net.mrbt0907.weather2.mixin.injection;

import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.mixin.MixinWorldReciever;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.storm.StormObject;

@Pseudo
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer
{
	private static final Minecraft MC = Minecraft.getMinecraft();
	/** Overriding this pushes the skybox further back when Extended Render Distance is enabled with Optifine installed */
    private float clipDistance;
    /** Injecting into setupCameraTransform gives us the perfect spot to override clipDistance */
	@Inject(method = "setupCameraTransform(FI)V", at = @At("RETURN"), cancellable=true)
	private void setupCameraTransform(float partialTicks, int pass, CallbackInfo callback)
	{
		NewSceneEnhancer scene = NewSceneEnhancer.instance();
		/**clipDistance = Hard cutoff for anything rendering**/
		clipDistance = scene.renderDistance * 1.5F;
	}
	
	/** Extending the view happens here instead of on farPlaneDistance, because vanilla ends its fog
	 *  at that field and moving it left the last chunk unfogged against the sky. Scaling the far plane
	 *  the call already computed keeps the sky pass (x2) and the terrain pass (x sqrt 2) at the ratio
	 *  vanilla chose. */
	@Redirect(method = "renderWorldPass(IFJ)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"), remap = true)
	private void extendFarPlane(float fov, float aspect, float near, float far)
	{
		float vanillaFar = MC.gameSettings.renderDistanceChunks * 16.0F;
		float scale = vanillaFar > 0.0F ? NewSceneEnhancer.instance().renderDistance * 1.25F / vanillaFar : 1.0F;
		Project.gluPerspective(fov, aspect, near, Math.max(far, far * scale));
	}
	
	/** Injecting into orientCamera gives us the perfect spot to shake the perspective */
	@Inject(method = "orientCamera(F)V", at = @At("RETURN"))
	private void orientCamera(float partialTicks, CallbackInfo callback)
    {
		if (!MC.isGamePaused())
		{
			if (ConfigClient.camera_shake_mult > 0.0D)
			{
				NewSceneEnhancer scene = NewSceneEnhancer.instance();
				float tornadoStrength = 0.0F;
				float windStrength = WeatherUtilEntity.isEntityOutside(MC.player, true) ?  0.1F * Maths.clamp((scene.cachedWindSpeed - 4.0F) * 0.2F, 0.0F, 1.0F) : 0.0F;
				float strength;
				
				if (scene.cachedSystem != null && scene.cachedSystem instanceof StormObject)
				{
					StormObject storm = (StormObject) scene.cachedSystem;
					if (storm.type.equals(Type.TORNADO))
						tornadoStrength = (1.0F - (float) Math.min(((scene.cachedFunnelDistance - storm.funnelSize) / (storm.funnelSize + 64.0F)), 1.0F)) * Math.min(storm.stage * 0.1F, 1.0F);
				}
				
				strength = (tornadoStrength + windStrength) * 0.025F * ConfigClient.camera_shake_mult;
				if (strength > 0.0F)
				{
					GlStateManager.translate(Maths.random(-strength, strength), Maths.random(-strength, strength), Maths.random(-strength, strength));
				}
			}
		}
    }
	
	/** Injecting into renderRainSnow removes the need to create a different EntityRenderer as we can directly hook into the method.<br><br>
	 * When Proxy Render Override is enabled, it solves:<br>
	 * - Translucent blocks not rendering correctly<br>
	 * - Shaders not color adjusting to Water, Stained Glass, etc.
	 */
	@Inject(method = "renderRainSnow(F)V", at = @At("HEAD"), cancellable=true)
	private void renderRain(float partialTicks, CallbackInfo callback)
	{
		MixinWorldReciever.renderRain(partialTicks, callback);
	}
	
	/** Injecting into addRainParticles allows us to disable rain splashes and sounds in favor of our own effects */
	@Inject(method = "addRainParticles()V", at = @At("HEAD"), cancellable=true)
	private void renderSplash(CallbackInfo callback)
	{
		if (ConfigMisc.proxy_render_override && !ConfigClient.enable_vanilla_rain)
			callback.cancel();
	}

}