package net.mrbt0907.weather2.client.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.block.TileRadar;
import net.mrbt0907.weather2.client.SceneEnhancer;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigSimulation;
import net.mrbt0907.weather2.registry.ParticleRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.weather.storm.StormObject;

import org.lwjgl.opengl.GL11;

public class RenderRadar extends TileEntitySpecialRenderer<TileEntity>
{
	@Override
	public void render(TileEntity tile, double x, double y, double z, float var8, int destroyStage, float alpha)
	{
		TileRadar radar = (TileRadar) tile;
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y+1.1F, (float)z + 0.5F);
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.depthMask(false);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		GlStateManager.disableTexture2D();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos((double)-(radar.renderRange - 0.5D), 0, -(double)(radar.renderRange - 0.5D)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
	    worldrenderer.pos((double)-(radar.renderRange - 0.5D), 0, (double)(radar.renderRange - 0.5D)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
	    worldrenderer.pos((double)(radar.renderRange - 0.5D), 0, (double)(radar.renderRange - 0.5D)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
	    worldrenderer.pos((double)(radar.renderRange - 0.5D), 0, -(double)(radar.renderRange - 0.5D)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();

		float playerViewY = Minecraft.getMinecraft().getRenderManager().playerViewY;
		renderLivingLabel("\u00A7" + '6' + "|", x, y + 1.2F, z, 1, 10, 10, playerViewY, 1.0F);

		if (ConfigMisc.debug_mode_radar)
		{
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (player != null)
			{
				WeatherManagerClient wm = ClientTickHandler.weatherManager;
				if (ConfigSimulation.simulation_enable)
				{
					
				}
				else
				{
					float precipStr = Math.abs(SceneEnhancer.curPrecipStr);
					String rainThunder = Math.round(player.world.rainingStrength * 100.0F) + "% / " + Math.round(player.world.thunderingStrength * 100.0F) + "%";
					renderLivingLabel("\u00A7" + " Vanilla Weather Time: " + wm.weatherRainTime, x, y + 1.9F, z, 1, 10, 10, playerViewY, 1.0F);
					renderLivingLabel("\u00A7" + " Client Weather: " + (player.world.isThundering() ? "Thundering" : precipStr >= 0.5F ? "Heavy Rain" : precipStr >= 0.15F ? "Light Rain" : precipStr > 0.01F ? "Drizzle" : "Clear"), x, y + 2.0F, z, 1, 10, 10, playerViewY, 1.0F);
					renderLivingLabel("\u00A7" + " Server Weather: " + (wm.weatherID == 2 ? "Thunder" : wm.weatherID == 1 ? "Rain" : "Clear"), x, y + 2.1F, z, 1, 10, 10, playerViewY, 1.0F);
					renderLivingLabel("\u00A7" + " Precipitation Strength: " + Math.round(precipStr * 100.0F) + "%", x, y + 2.2F, z, 1, 10, 10, playerViewY, 1.0F);
					renderLivingLabel("\u00A7" + " Vanilla Rain/Thunder Strength: " + rainThunder, x, y + 2.3F, z, 1, 10, 10, playerViewY, 1.0F);
					renderLivingLabel("\u00A7" + " -------------------------", x, y + 2.4F, z, 1, 10, 10, playerViewY, 1.0F);
					if (radar.system != null && radar.system instanceof StormObject)
					{
						StormObject system = (StormObject) radar.system;
						renderLivingLabel("\u00A7" + " Stage Complete: " + (((system.stormIntensity - system.stormStage + 1)) * 100.0F) + "%", x, y + 2.5F, z, 1, 10, 10, playerViewY, 1.0F);
						renderLivingLabel("\u00A7" + " Current Funnel Wind Speed: " + (long)WeatherUtil.toMph(system.stormWind) + " MPH", x, y + 2.6F, z, 1, 10, 10, playerViewY, 1.0F);
						renderLivingLabel("\u00A7" + " Current Funnel Size: " + (long)system.funnel_size + " Blocks", x, y + 2.7F, z, 1, 10, 10, playerViewY, 1.0F);
						renderLivingLabel("\u00A7" + " Current Stage/MaxStage: " + system.stormStage + "/" + system.stormStageMax, x, y + 2.8F, z, 1, 10, 10, playerViewY, 1.0F);
						renderLivingLabel("\u00A7" + " Growth Percentage: " + (system.stormSizeRate * 100) + "%", x, y + 2.9F, z, 1, 10, 10, playerViewY, 1.0F);
						renderLivingLabel("\u00A7" + " Is Violent: " + system.isViolent, x, y + 3.0F, z, 1, 10, 10, playerViewY, 1.0F);
						renderLivingLabel("\u00A7" + " UUID: " + system.getUUID(), x, y + 3.1F, z, 1, 10, 10, playerViewY, 1.0F);
						String stage = radar.system.getName();
						
						if (system.isDying)
							stage += "  (Dying)";
						renderLivingLabel("\u00A7" + " " + TextFormatting.BOLD + stage, x, y + 3.2F, z, 1, 10, 10, playerViewY, 1.0F);
					}

					renderLivingLabel("\u00A7" + " Radar Tier " + radar.getTier() + " (R:" + radar.pingRange + ")", x, y + 3.3F, z, 1, 10, 10, playerViewY, 1.0F);
				}
			}
		}
		
		BlockPos pos = radar.getPos();
		radar.systems.forEach(so ->
		{
			GlStateManager.pushMatrix();
			
			Vec3 posRenderOffset = so.pos.copy();
			posRenderOffset.posX -= pos.getX();
			posRenderOffset.posZ -= pos.getZ();	
			posRenderOffset.posX /= radar.pingRange;
			posRenderOffset.posZ /= radar.pingRange;
			posRenderOffset.posX *= radar.renderRange - 0.5D;
			posRenderOffset.posZ *= radar.renderRange - 0.5D;
			

			GlStateManager.translate(posRenderOffset.posX, 0, posRenderOffset.posZ);
			if (radar.showRating)
			{
				FontRenderer font = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
				renderLivingLabel(so.typeName, x, y + (so.type == 0 ? 1.54F : 1.5F), z, 1, font.getStringWidth(so.typeName), 5, playerViewY, radar.renderAlpha);
			}
			
			if (so.type == 1 || so.type == 2)
			{
				//float offset = Float.parseFloat(so.toString().replaceAll("\\D", ""));
				switch(so.stage)
				{
					case 0:
						renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha * 0.5F, ParticleRegistry.radarIconCloud);
						//renderIconNew(x, y + 1.01F, z, 32, 32, 90.0F, 0.0F, offset, radar.renderAlpha, ParticleRegistry.radarIconReflectivityA);
						break;
					case 1:
						if (so.isRaining)
							if (so.name.toLowerCase().contains("snowstorm"))
								renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconSnow);
							else
								renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconRain);
						else
							renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconCloud);
						//renderIconNew(x, y + 1.01F, z, 32, 32, 90.0F, 0.0F, offset, radar.renderAlpha, ParticleRegistry.radarIconReflectivityB);
						break;
					case 2:
						renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconLightning);
						//renderIconNew(x, y + 1.01F, z, 32, 32, 90.0F, 0.0F, offset, radar.renderAlpha, ParticleRegistry.radarIconReflectivityC);
						break;
					case 3:
						renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconLightning);
						renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconWind);
						///renderIconNew(x, y + 1.01F, z, 32, 32, 90.0F, 0.0F, offset, radar.renderAlpha, ParticleRegistry.radarIconReflectivityD);
						break;
					default:
						if (so.type == 1)
						{
							renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconTornado);
							//renderIconNew(x, y + 1.01F, z, 32, 32, 90.0F, 0.0F, offset, radar.renderAlpha, ParticleRegistry.radarIconReflectivityE);
						}
						else
						{
							renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconCyclone);
							//renderIconNew(x, y + 1.01F, z, 32, 32, 90.0F, 0.0F, offset, radar.renderAlpha, ParticleRegistry.radarIconReflectivityF);
						}
				}
				
				if (so.isHailing)
					renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconHail);
				
				if (ConfigMisc.debug_mode_radar && radar.system != null && so.uuid.equals(radar.system.getUUID()))
					renderLivingLabel(TextFormatting.GOLD + "" +  TextFormatting.BOLD + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY, radar.renderAlpha);
				else
				{
					if (so.stage == Stage.NORMAL.getStage())
						renderLivingLabel(TextFormatting.GRAY + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY, radar.renderAlpha * 0.35F);
					else if (so.isDying)
						renderLivingLabel(TextFormatting.RED + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY, radar.renderAlpha);
					else
						renderLivingLabel(TextFormatting.GREEN + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY, radar.renderAlpha);
				}
				
			}
			else if (so.type == 0)
			{
				int type = so.name.toLowerCase().contains("stationary") ? 0 : so.name.toLowerCase().contains("warm") ? 2 : so.name.toLowerCase().contains("cold") ? 1 : 3;
				renderIconNew(x, y + 1.12F, z, (int)(64 * radar.renderRange), (int)(64 * radar.renderRange), 90.0F, 0.0F, so.angle, radar.renderAlpha, type == 0 ? ParticleRegistry.radarIconStationaryFront : type == 1 ? ParticleRegistry.radarIconColdFront : type == 2 ? ParticleRegistry.radarIconWarmFront : ParticleRegistry.radarIconOccludedFront);
				if (!so.isDying)
					renderLivingLabel(TextFormatting.BOLD + "" + TextFormatting.DARK_GREEN + "|", x, y + 1.22F, z, 1, 5, 5, playerViewY, radar.renderAlpha);
				else
					renderLivingLabel(TextFormatting.BOLD + "" + TextFormatting.DARK_RED + "|", x, y + 1.22F, z, 1, 5, 5, playerViewY, radar.renderAlpha);
			}
			else
			{
				renderIconNew(x, y + 1.4F, z, 16, 16, 0.0F, playerViewY, 0.0F, radar.renderAlpha, ParticleRegistry.radarIconSandstorm);
				if (!so.isDying)
					renderLivingLabel("\u00A7" + '2' + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY, radar.renderAlpha);
				else
					renderLivingLabel("\u00A7" + '4' + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY, radar.renderAlpha);
			}
			
			GlStateManager.translate(-posRenderOffset.posX, 0, -posRenderOffset.posZ);
			GlStateManager.popMatrix();
		});
	}
	
	protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, float angle, float alpha)
	{
		renderLivingLabel(par2Str, par3, par5, par7, par9, 200, 80, angle, alpha);
	}
	
	protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, int width, int height, float angle, float alpha)
	{
		int hexAlpha = (int) (255 * alpha) << 24, c1 = 0xFFFFFF;
		c1 += hexAlpha;
		int borderSize = 2;

		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();
		
		FontRenderer var11 = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
		float var12 = 0.6F;
		float var13 = 0.016666668F * var12;
		GlStateManager.pushMatrix();
		//GL11.glPushMatrix();
		GlStateManager.translate(par3 + 0.5F, par5, par7 + 0.5F);
		//GL11.glTranslatef((float)par3 + 0.5F, (float)par5, (float)par7 + 0.5F);
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		//GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-angle, 0.0F, 1.0F, 0.0F);
		//GL11.glRotatef(-angle, 0.0F, 1.0F, 0.0F);
		GlStateManager.scale(-var13, -var13, var13);
		//GL11.glScalef(-var13, -var13, var13);
		GlStateManager.disableLighting();
		//GL11.glDisable(GL11.GL_LIGHTING);
		
		if (par9 == 0) {
			//GL11.glDepthMask(false);
			//GL11.glDisable(GL11.GL_DEPTH_TEST);
			GlStateManager.enableBlend();
			//GL11.glEnable(GL11.GL_BLEND);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			Tessellator var14 = Tessellator.getInstance();
			BufferBuilder worldrenderer = var14.getBuffer();
			byte var15 = 0;
			
			worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			
			worldrenderer
			.color(0.0F, 0.0F, 0.0F, 0.25F)
			.pos((double)(-width / 2 - borderSize), (double)(-borderSize + var15), 0.0D)
			
			.endVertex();
			
			worldrenderer
			.color(0.0F, 0.0F, 0.0F, 0.25F)
			.pos((double)(-width / 2 - borderSize), (double)(height + var15), 0.0D)
			
			.endVertex();
			
			worldrenderer
			.color(0.0F, 0.0F, 0.0F, 0.25F)
			.pos((double)(width / 2 + borderSize), (double)(height + var15), 0.0D)
			
			.endVertex();
			
			worldrenderer
			.color(0.0F, 0.0F, 0.0F, 0.25F)
			.pos((double)(width / 2 + borderSize), (double)(-borderSize + var15), 0.0D)
			
			.endVertex();
			
			var14.draw();
		}
		GlStateManager.enableTexture2D();
		//GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glEnable(GL11.GL_DEPTH_TEST);
		//GL11.glDepthMask(true);
		var11.drawString(par2Str, -width/2+borderSize, 0, c1);
		GlStateManager.enableLighting();
		//GL11.glEnable(GL11.GL_LIGHTING);
		GlStateManager.enableBlend();
		//GL11.glDisable(GL11.GL_BLEND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		//GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
		//GL11.glPopMatrix();

		GlStateManager.enableCull();
		//GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	public void renderIconNew(double x, double y, double z, int width, int height, float angleX, float angleY, float angleZ, float alpha, TextureAtlasSprite parIcon) {
		float f6 = parIcon.getMinU();
		float f7 = parIcon.getMaxU();
		float f9 = parIcon.getMinV();
		float f8 = parIcon.getMaxV();
		
		float var12 = 0.6F;
		float var13 = 0.016666668F * var12;
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y, (float)z + 0.5F);
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		//GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(angleX, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(-angleY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(angleZ, 0.0F, 0.0F, 1.0F);
		GlStateManager.scale(-var13, -var13, var13);
		
		int borderSize = 2;
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		
		GlStateManager.disableFog();
		
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		float r = 1F;
		float g = 1F;
		float b = 1F;
		
		worldrenderer
		.pos((double)(-width / 2 - borderSize), (double)(-borderSize), 0.0D)
		.tex(f6, f9)
		.color(r, g, b, alpha).endVertex();
		
		worldrenderer
		.pos((double)(-width / 2 - borderSize), (double)(height), 0.0D)
		.tex(f6, f8)
		.color(r, g, b, alpha).endVertex();
		
		worldrenderer
		.pos((double)(width / 2 + borderSize), (double)(height), 0.0D)
		.tex(f7, f8)
		.color(r, g, b, alpha).endVertex();
		
		worldrenderer
		.pos((double)(width / 2 + borderSize), (double)(-borderSize), 0.0D)
		.tex(f7, f9)
		.color(r, g, b, alpha).endVertex();
		
		tessellator.draw();

		GlStateManager.popMatrix();
	}
}
