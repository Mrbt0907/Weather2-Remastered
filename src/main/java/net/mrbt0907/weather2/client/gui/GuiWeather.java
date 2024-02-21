package net.mrbt0907.weather2.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mrbt0907.weather2.api.WindReader;
import net.mrbt0907.weather2.api.weather.AbstractWeatherRenderer;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.item.ItemSensor;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilGui;

public class GuiWeather extends WeatherUtilGui
{
	private final Minecraft mc = Minecraft.getMinecraft();
	
	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Pre event)
	{
		if (!event.getType().equals(ElementType.HOTBAR) || mc.world == null) return;
		GL11.glPushMatrix();
		GL11.glEnable(2977);
		GL11.glBlendFunc(770, 771);
		if (mc.player != null)
		{
			WeatherManagerClient manager = ClientTickHandler.weatherManager;
			if (manager != null)
			{
				ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
				if (ConfigParticle.enable_debug_renderer)
					for (int i = 0; i < AbstractWeatherRenderer.renderDebugInfo.size(); i++)
						drawString(mc.fontRenderer, AbstractWeatherRenderer.renderDebugInfo.get(i), 0, 2 + 10 * i, 0xFFFFFF00);
				
				if (stack.getItem() instanceof ItemSensor)
				{
					ItemSensor item = (ItemSensor) stack.getItem();
					NBTTagCompound nbt = stack.getTagCompound();
					boolean enabled = nbt == null ? false : nbt.getBoolean("enabled");
					
					if (enabled)
					{
						Maths.Vec3 pos = new Maths.Vec3(mc.player.posX, mc.player.posY, mc.player.posZ);
						BlockPos bPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
							
				    	switch(item.getType())
						{
							case 1:
								this.drawString(mc.fontRenderer, String.format("Temperature: %.2f°F,  %.02f°C", WeatherUtil.toFahrenheit(WeatherUtil.getTemperature((World) mc.world, bPos)), WeatherUtil.toCelsius(WeatherUtil.getTemperature((World) mc.world, bPos))), 0, 2, 0xFFFFFFFF);
								break;
							case 2:
									this.drawString(mc.fontRenderer, String.format("Humidity: %.02f%%", (WeatherUtil.getHumidity((World) mc.world, bPos) * 100.0F)), 0, 2, 0xFFFFFFFF);
								break;
							case 3:
								float windAngle = WindReader.getWindAngle((World) mc.world, pos);
								float windSpeed = WindReader.getWindSpeed((World) mc.world, pos);
								this.drawString(mc.fontRenderer, String.format("Wind Speed: %.2f Mph, %.2f Kph, %.2f M/s  (%.2f) (%s)", WeatherUtil.toMph(windSpeed), WeatherUtil.toKph(windSpeed), WeatherUtil.toMps(windSpeed), windAngle, (windAngle >= 315 ? "South" : windAngle >= 225 ? "East" : windAngle >= 135 ? "North" : windAngle >= 45 ? "West" : "South")), 0, 2, 0xFFFFFFFF);
								break;
						}
					}
				}
			}
		}
		color();
		//GL11.glDisable(3042);
		GL11.glPopMatrix();
	}
}
