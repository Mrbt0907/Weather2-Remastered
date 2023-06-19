package net.mrbt0907.weather2.weather;

import CoroUtil.config.ConfigCoroUtil;
import extendedrenderer.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigParticle;

public class EntityRendererEX extends EntityRenderer
{
	public EntityRendererEX(Minecraft var1, IResourceManager resMan)
	{
		super(var1, resMan);
	}

	@Override
	protected void renderRainSnow(float par1)
	{
		/**
		 * why render here? because renderRainSnow provides better context, solves issues:
		 * - translucent blocks rendered after
		 * -- shaders are color adjusted when rendering on other side of
		 * --- water
		 * --- stained glass, etc
		 */
		if (ConfigCoroUtil.useEntityRenderHookForShaders)
			EventHandler.hookRenderShaders(par1);
		
		if (!ConfigMisc.proxy_render_override)
			super.renderRainSnow(par1);
		else if (!ConfigParticle.enable_precipitation) //note, the overcast effect change will effect vanilla non particle rain distance too, particle rain for life!
			super.renderRainSnow(par1);
	}
	
	/**Removes the rain splash*/
	@Override
	public void addRainParticles()
	{
		if (ConfigParticle.use_vanilla_rain_and_thunder)
			super.addRainParticles();
	}
}
