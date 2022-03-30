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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.mrbt0907.weather2.ClientProxy;
import net.mrbt0907.weather2.api.IWeatherStages;
import net.mrbt0907.weather2.block.TileRadar;
import net.mrbt0907.weather2.client.SceneEnhancer;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.weather.WeatherSystemClient;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigSimulation;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.WeatherEnum;

import org.lwjgl.opengl.GL11;

import CoroUtil.util.Vec3;

public class RenderRadar extends TileEntitySpecialRenderer<TileEntity>
{
	@Override
	public void render(TileEntity tile, double x, double y, double z, float var8, int destroyStage, float alpha)
	{
		TileRadar radar = (TileRadar) tile;
		WeatherObject lastWO = radar.lastTickStormObject;
		
		double sizeSimBoxDiameter = ConfigMisc.radar_range;
		float sizeRenderBoxDiameter = 2;
		
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
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();

		float playerViewY = Minecraft.getMinecraft().getRenderManager().playerViewY;
		
		renderLivingLabel("\u00A7" + '6' + "|", x, y + 1.2F, z, 1, 10, 10, playerViewY);

		if (ConfigMisc.debug_mode_radar)
		{
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (player != null)
			{
				WeatherSystemClient wm = ClientTickHandler.weatherManager;
				if (ConfigSimulation.simulation_enable)
				{
					if (lastWO != null && lastWO instanceof IWeatherStages)
					{
						BlockPos ground = new BlockPos(lastWO.posGround.xCoord, lastWO.posGround.yCoord, lastWO.posGround.zCoord);
						float groundHumidity = WeatherUtil.getHumidity(wm.getWorld(), ground);
						float groundTemp = WeatherUtil.getTemperature(wm.getWorld(), ground);
						String stormType = lastWO.type.toString();
						int stormStage = ((IWeatherStages)lastWO).getStage();
						
						if (stormStage > -1)
							stormType += " " + (lastWO.type.equals(WeatherEnum.Type.TORNADO) ? "EF" : "C") + stormStage;
						
						renderLivingLabel("\u00A7" + String.format(" Ground Humidity: %01.01f%%  (%01.04f)", groundHumidity * 100.0F, groundHumidity), x, y + 1.7F, z, 1, 10, 10, playerViewY);
						renderLivingLabel("\u00A7" + String.format(" Ground Temperature: %01.01f°F or %01.01f°C  (%01.04f)", WeatherUtil.toFahrenheit(groundTemp), WeatherUtil.toCelsius(groundTemp), groundTemp), x, y + 1.6F, z, 1, 10, 10, playerViewY);
						//renderLivingLabel("\u00A7" + String.format(" Forming Strength: %01.01f%%  (%01.04f)", wo.stormFormingStrength * 100.0F, wo.stormFormingStrength), x, y + 2.3F, z, 1, 10, 10, playerViewY);
						//renderLivingLabel("\u00A7" + String.format(" Instability: %01.04f", wo.stormIntensity), x, y + 2.2F, z, 1, 10, 10, playerViewY);
						//renderLivingLabel("\u00A7" + String.format(" Wind Speed: %01.04f", wo.stormWind), x, y + 2.1F, z, 1, 10, 10, playerViewY);
						//renderLivingLabel("\u00A7" + String.format(" Humidity: %01.01f%%  (%01.04f)", wo.stormRain * 100.0F, wo.stormRain), x, y + 2.0F, z, 1, 10, 10, playerViewY);
						//renderLivingLabel("\u00A7" + String.format(" Temperature: %01.01f°F or %01.01f°C  (%01.04f)", WeatherUtil.toFahrenheit(wo.stormTemperature), WeatherUtil.toCelsius(wo.stormTemperature), wo.stormTemperature), x, y + 1.9F, z, 1, 10, 10, playerViewY);
						renderLivingLabel("\u00A7" + String.format(" %s (X:%01.01f, Y:%01.01f, Z:%01.01f)", stormType, lastWO.pos.xCoord, lastWO.pos.yCoord, lastWO.pos.zCoord), x, y + 2.4F, z, 1, 10, 10, playerViewY);
					}
				}
				else
				{
					float precipStr = Math.abs(SceneEnhancer.getRainStrengthAndControlVisuals(player, true));
					boolean clientWeather2Rain = precipStr > 0;
	
					String rainThunder = player.world.rainingStrength + " / " + player.world.thunderingStrength;
					renderLivingLabel("\u00A7" + " Vanilla Weather Time: " + wm.weatherRainTime, x, y + 1.9F, z, 1, 10, 10, playerViewY);
					renderLivingLabel("\u00A7" + " Client Weather: " + (player.world.isThundering() ? "Thundering" : player.world.isRaining() ? "Heavy Rain" : clientWeather2Rain ? "Light Rain" : "Clear"), x, y + 2.0F, z, 1, 10, 10, playerViewY);
					renderLivingLabel("\u00A7" + " Server Weather: " + (wm.weatherID == 2 ? "Thunder" : wm.weatherID == 1 ? "Rain" : "Clear"), x, y + 2.1F, z, 1, 10, 10, playerViewY);
					renderLivingLabel("\u00A7" + " Precipitation Strength: " + SceneEnhancer.getRainStrengthAndControlVisuals(player), x, y + 2.2F, z, 1, 10, 10, playerViewY);
					renderLivingLabel("\u00A7" + " Vanilla Rain/Thunder Strength: " + rainThunder, x, y + 2.3F, z, 1, 10, 10, playerViewY);
					renderLivingLabel("\u00A7" + " -------------------------", x, y + 2.4F, z, 1, 10, 10, playerViewY);
					if (lastWO != null && lastWO instanceof StormObject)
					{
						renderLivingLabel("\u00A7" + " Stage Complete: " + ((((StormObject)lastWO).stormIntensity - (((StormObject)lastWO).stormStage - 1)) * 100.0F) + "%", x, y + 2.5F, z, 1, 10, 10, playerViewY);
						renderLivingLabel("\u00A7" + " Current Funnel Wind Speed: " + (long)((StormObject)lastWO).stormWind + " MPH", x, y + 2.6F, z, 1, 10, 10, playerViewY);
						renderLivingLabel("\u00A7" + " Current Funnel Size: " + (long)((StormObject)lastWO).funnel_size + " Blocks", x, y + 2.7F, z, 1, 10, 10, playerViewY);
						renderLivingLabel("\u00A7" + " Current Stage/MaxStage: " + ((StormObject)lastWO).getStage() + "/" + ((StormObject)lastWO).stormStageMax, x, y + 2.8F, z, 1, 10, 10, playerViewY);
						renderLivingLabel("\u00A7" + " Growth Percentage: " + (((StormObject)lastWO).stormSizeRate * 100) + "%", x, y + 2.9F, z, 1, 10, 10, playerViewY);
						renderLivingLabel("\u00A7" + " Is Violent: " + ((StormObject)lastWO).isViolent, x, y + 3.0F, z, 1, 10, 10, playerViewY);
						renderLivingLabel("\u00A7" + " UUID: " + lastWO.getUUID().toString(), x, y + 3.1F, z, 1, 10, 10, playerViewY);
						String stage = "Unknown Storm";
						switch(lastWO.type.getStage())
						{
						case 0:
							stage = "Cloud";
							break;
						case 1:
							stage = "Rainstorm";
							break;
						case 2:
							stage = "Thunderstorm";
							break;
						case 3:
							if (((StormObject)lastWO).stormStage == 2)
								stage = "Supercell";
							else
								stage = "Hailing Supercell";
							break;
						case 4:
							if(((StormObject)lastWO).stormType == 1)
								stage = "Tropical Storm";
							else
								if (ConfigStorm.enable_ef_scale)
									stage = "Tornado - EF" + (((StormObject)lastWO).stormStage - 4);
								else
									stage = "Tornado - F" + (int)MathHelper.clamp(Math.floor(((StormObject)lastWO).funnel_size * 0.0206611570247933884297520661157F), 0, ((StormObject)lastWO).stormStageMax - 4);
							break;
						case 5:
							stage = "Hurricane - Category " + (((StormObject)lastWO).stormStage - 4);
							break;
						}
						if (((StormObject)lastWO).isDying)
							stage += "  (Dying)";
						renderLivingLabel("\u00A7" + " " + TextFormatting.BOLD + stage, x, y + 3.2F, z, 1, 10, 10, playerViewY);
					}
				}
			}
		}
		
		radar.storms.forEach(wo ->
		{
			GlStateManager.pushMatrix();
			
			Vec3 posRenderOffset = new Vec3(wo.pos.xCoord - radar.getPos().getX(), 0, wo.pos.zCoord - radar.getPos().getZ());
			posRenderOffset.xCoord /= sizeSimBoxDiameter;
			posRenderOffset.zCoord /= sizeSimBoxDiameter;
			
			posRenderOffset.xCoord *= sizeRenderBoxDiameter - 0.5D;
			posRenderOffset.zCoord *= sizeRenderBoxDiameter - 0.5D;
			

			GlStateManager.translate(posRenderOffset.xCoord, 0, posRenderOffset.zCoord);

			if (wo instanceof StormObject) {
				StormObject storm = (StormObject)wo;
				if (storm.stormStage > StormObject.Stage.HAIL.getInt())
				{
					
					if (storm.stormType == StormObject.Type.WATER.getInt())
					{
						renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconCyclone);
						renderLivingLabel("C" + (storm.stormStage - 4), x, y + 1.5F, z, 1, 15, 5, playerViewY);
					}
					else
					{
						renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconTornado);
						if (ConfigStorm.enable_ef_scale)
							renderLivingLabel("EF" + (storm.stormStage - 4), x, y + 1.5F, z, 1, 12, 5, playerViewY);
						else
							renderLivingLabel("F" + (int)MathHelper.clamp(Math.floor(storm.funnel_size * 0.0206611570247933884297520661157F), 0, storm.stormStageMax - 4), x, y + 1.5F, z, 1, 12, 5, playerViewY);
					}
				}
				else
					switch(storm.stormStage)
					{
						case 1:
						{
							renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconLightning);
							break;
						}
						case 2:
						{
							renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconLightning);
							renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconWind);
							break;
						}
						case 3:
						{
							renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconHail);
							renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconWind);
							break;
						}
						default:
						{
							if (storm.isRaining)
							{
								renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconRain);;
							}
							break;
						}
					}

				String charCode = "|";
				if (ConfigMisc.debug_mode_radar) {
					if (storm.stormTemperature > 0) {
						charCode = TextFormatting.DARK_RED.toString();
					} else {
						charCode = TextFormatting.BLUE.toString();
					}
				}

				if (storm.stormStage > StormObject.Stage.NORMAL.getInt()) {
					if (ConfigMisc.debug_mode_radar && storm.equals(lastWO)) {
						renderLivingLabel(TextFormatting.GOLD + "" +  TextFormatting.BOLD + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
					}
					else
					{
						if (storm.isDying)
							renderLivingLabel("\u00A7" + '4' + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
						else
							renderLivingLabel("\u00A7" + '2' + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
					}
				} else {
					if (ConfigMisc.debug_mode_radar) {
						if (storm.isCloudless) {
							renderLivingLabel(TextFormatting.BLACK + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
						} else {
							renderLivingLabel(charCode + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
							//renderLivingLabel("\u00A7" + 'f' + charCode, x, y + 1.1F, z, 1, 5, 5, playerViewY);
						}
					} else {
						renderLivingLabel(TextFormatting.WHITE + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
					}
				}
			} else if (wo instanceof SandstormObject) {
				renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconSandstorm);
				if (((SandstormObject)wo).isFrontGrowing) {
					renderLivingLabel("\u00A7" + '2' + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
				} else {
					renderLivingLabel("\u00A7" + '4' + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
				}
			}
			GlStateManager.translate(-posRenderOffset.xCoord, 0, -posRenderOffset.zCoord);
			GlStateManager.popMatrix();
		});
	}
	
	protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, float angle)
	{
		renderLivingLabel(par2Str, par3, par5, par7, par9, 200, 80, angle);
	}
	
	protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, int width, int height, float angle)
	{

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
		var11.drawString(par2Str, -width/2+borderSize, 0, 0xFFFFFF);
		GlStateManager.enableLighting();
		//GL11.glEnable(GL11.GL_LIGHTING);
		GlStateManager.enableBlend();
		//GL11.glDisable(GL11.GL_BLEND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		//GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
		//GL11.glPopMatrix();

		GlStateManager.enableCull();
		//GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	public void renderIconNew(double x, double y, double z, int width, int height, float angle, TextureAtlasSprite parIcon) {
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
		GlStateManager.rotate(-angle, 0.0F, 1.0F, 0.0F);
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
		.color(r, g, b, 1.0F).endVertex();
		
		worldrenderer
		.pos((double)(-width / 2 - borderSize), (double)(height), 0.0D)
		.tex(f6, f8)
		.color(r, g, b, 1.0F).endVertex();
		
		worldrenderer
		.pos((double)(width / 2 + borderSize), (double)(height), 0.0D)
		.tex(f7, f8)
		.color(r, g, b, 1.0F).endVertex();
		
		worldrenderer
		.pos((double)(width / 2 + borderSize), (double)(-borderSize), 0.0D)
		.tex(f7, f9)
		.color(r, g, b, 1.0F).endVertex();
		
		tessellator.draw();

		GlStateManager.popMatrix();
		//GL11.glPopMatrix();
	}
}
