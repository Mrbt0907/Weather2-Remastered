package net.mrbt0907.weather2.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.Gui;

public class WeatherUtilGui extends Gui
{
		protected String parseFloat(float value)
		{
			String input;
			if (value >= 1000000000000000000000000000000000000.0F)
			input = "Infinity (" + (int)Math.floor((value / Float.MAX_VALUE) * 100) + "%)";
			else if (value >= 1000000000000000000000000000000000.0F)
			input = Math.floor(value * 0.00000000000000000000000000000001F) * 0.1F + " Decillion";
			else if (value >= 1000000000000000000000000000000.0F)
			input = Math.floor(value * 0.00000000000000000000000000001F) / 10 + " Nonillion";
			else if (value >= 1000000000000000000000000000.0F)
			input = Math.floor(value * 0.00000000000000000000000001F) / 10 + " Octillion";
			else if (value >= 1000000000000000000000000.0F)
			input = Math.floor(value * 0.00000000000000000000001F) / 10 + " Septillion";
			else if (value >= 1000000000000000000000.0F)
			input = Math.floor(value * 0.00000000000000000001F) / 10F + " Sextillion";
			else if (value >= 1000000000000000000.0F)
			input = Math.floor(value * 0.00000000000000001F) / 10 + " Quintillion";
			else if (value >= 1000000000000000.0F)
			input = Math.floor(value * 0.00000000000001F) / 10 + " Quadrillion";
			else if (value >= 1000000000000.0F)
			input = Math.floor(value * 0.00000000001F) / 10 + " Trillion";
			else if (value >= 1000000000.0F)
			input = Math.floor(value * 0.00000001F) / 10 + " Billion";
			else if (value >= 1000000.0F)
			input =  Math.floor(value * 0.00001F) / 10 + " Million";
			else
			input = (int)Math.floor(value) + "";
			return input;
		}

		protected void drawBar(int x, int y, int u, int v, int width, int height, int cu, int cv, float amount)
		{
			amount = Maths.clamp(amount, 0.0F, 1.0F);
			drawModalRectWithCustomSizedTexture(x, y, u, v, (int)(width * amount), height, cu, cv);
		}

		protected void drawMirroredBar(int x, int y, int u, int v, int width, int height, int cu, int cv, float amount)
		{
			amount = Maths.clamp(amount, 0.0F, 1.0F);
			drawModalRectWithCustomSizedTexture(x + (int)(width - (width * amount)), y, u + (int)(width - (width * amount)), v, (int)(width * amount), height, cu, cv);
			drawModalRectWithCustomSizedTexture(x + width, y, width, v, (int)(width * amount), height, cu, cv);
		}
		
		protected void drawBar(int x, int y, int u, int v, int width, int height, float amount)
		{
			amount = Maths.clamp(amount, 0.0F, 1.0F);
			drawTexturedModalRect(x, y, u, v, (int)(width * amount), height);
		}

		protected void drawMirroredBar(int x, int y, int u, int v, int width, int height, float amount)
		{
			amount = Maths.clamp(amount, 0.0F, 1.0F);
			drawTexturedModalRect(x + (int)(width - (width * amount)), y, u + (int)(width - (width * amount)), v, (int)(width * amount), height);
			drawTexturedModalRect(x + width, y, width, v, (int)(width * amount), height);
		}

		protected void color(float... values)
		{
			int index = 0;
			float[] rgba = {255.0F, 255.0F, 255.0F, 255.0F};
			for (int i = 0; i < values.length; i++)
			if (index < 4)
			{
				rgba[index] = values[i];
				index ++;
			}

			else
			break;
			GL11.glColor4f(Maths.clamp(rgba[0] / 255.0F, 0.0F, 1.0F), Maths.clamp(rgba[1] / 255.0F, 0.0F, 1.0F), Maths.clamp(rgba[2] / 255.0F, 0.0F, 1.0F), Maths.clamp(rgba[3] / 255.0F, 0.0F, 1.0F));
		}
}
