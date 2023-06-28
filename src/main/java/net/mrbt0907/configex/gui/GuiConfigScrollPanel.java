package net.mrbt0907.configex.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.manager.ConfigInstance;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiConfigScrollPanel extends GuiScrollPanel
{
	private GuiConfigEditor config;
	public final List<ConfigInstance> configs;
	public final List<GuiConfigEntry> options;
	private int mouseX;
	private int mouseY;
	protected int mouseYStart = -1;
	
	public GuiConfigScrollPanel(GuiConfigEditor controls, Minecraft mc, int scrollBarX, int width, int height, int scrollBarSize, int slotHeight)
	{
		super(mc, controls.xStart, controls.yStart, scrollBarX, width, height, scrollBarSize, slotHeight, 7, 8);
		this.config = controls;
		configs = ConfigManager.getInstances();
		options = new ArrayList<GuiConfigEntry>();
		populateData();
	}

	@Override
	protected void onSlotClicked(int i, boolean flag)
	{
		if (!flag)
		{
			selected = i;
			KeyBinding.resetKeyBindingArrayAndHash();
		}
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int eventButton)
	{
		for (GuiConfigEntry entry : options)
			entry.textField.mouseClicked(mouseX, mouseY, eventButton);
	}

	@Override
	protected boolean isSelected(int i) {return false;}

	@Override
	protected void drawBackground(Tessellator tess, int mouseX, int mouseY, float partialTicks)
	{
		config.drawBackgroundLayer();
	}
	
	@Override
	protected void drawForeground(Tessellator tess, int mouseX, int mouseY, float partialTicks)
	{
		config.drawForegroundLayer();
		config.drawButtons(mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawScreen(int mX, int mY, float f)
	{
		mouseX = mX;
		mouseY = mY;
		xStart = config.xStart + 169;
		yStart = config.yStart + 40;
		xScrollBar = xStart + 133;
		if (selected != -1 && !Mouse.isButtonDown(0) && Mouse.getDWheel() == 0)
			if (Mouse.next() && Mouse.getEventButtonState())
			{
				selected = -1;
				KeyBinding.resetKeyBindingArrayAndHash();
			}
		
		super.drawScreen(mX, mY, f);
		if (Mouse.isButtonDown(0))
		{
			if (mouseX > xScrollBar && mouseX < xScrollBar + scrollBarSize && mouseY > yStart && mouseY < yStart + ySize)
			{
				int yScrollMax = ySize - scrollBarSize;
				int yScrollExtra = getScrollHeight() - ySize;
				if (yScrollExtra > 0)
				{
					float percExtra = Math.min((float)yScrollExtra / (float)yScrollMax, 1.0F);
					int yStartMax = (int)(scrollBarSize * 0.5F + (yScrollMax * (1.0F - percExtra) * 0.5F));
					int yEndMax = (int)(yScrollMax * Math.min(percExtra, 1.0F));
					setScrollPosPerc(MathHelper.clamp((float) ((mouseY - yStart) - yStartMax) / (float) yEndMax, 0.0F, 1.0F));
				}
			}
		}
	}

	@Override
	protected void drawScrollBar(Tessellator tessellator, int mouseX, int mouseY, float partialTicks)
	{
		int yScrollMax = ySize - scrollBarSize;
		int yScrollExtra = getScrollHeight() - ySize;
		if (yScrollExtra > 0)
		{
			float percScrolled = scrollPos / yScrollExtra;
			float percExtra = MathHelper.clamp((float)yScrollExtra / (float)yScrollMax, 0.0F, 1.0F);
			int yStartMax = (int)(yScrollMax * percExtra * percScrolled);
			int yEndMax = (int)(yScrollMax * percExtra * (1.0F - percScrolled));
			drawGradientRect(xScrollBar, yStart + yStartMax, xScrollBar + scrollBarSize, yStart + scrollBarSize + yScrollMax - yEndMax, 0x55000000, 0x55000000);
			drawGradientRect(xScrollBar, yStart, xScrollBar + scrollBarSize, yStart + ySize, 0x55000000, 0x55000000);
		}
	}

	@Override
	protected void drawSlotPre(Tessellator tessellatorint, int xPosition, int yPosition, int slot)
	{
		if (getSize() == 0) return;
		xPosition -= 20;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		GuiConfigEntry entry = options.get(slot);
		String name = entry.name;
		if (mc.fontRenderer.getStringWidth(name) > xSize + 18)
			name = mc.fontRenderer.trimStringToWidth(name, xSize + 18) + "...";
		int stringWidth = mc.fontRenderer.getStringWidth(name);
		config.drawString(mc.fontRenderer, name, xPosition - stringWidth + 15, yPosition + 3, 0xFFFFFFFF);
		
		entry.textField.xPos = xPosition + 20;
		entry.textField.yPos = yPosition;
		entry.textField.drawTextBox();
		
		
	}

	@Override
	protected void drawSlotPost(Tessellator tessellatorint, int xPosition, int yPosition, int slot)
	{
		if (getSize() == 0) return;
		xPosition -= 20;
		GuiConfigEntry entry = options.get(slot);

		String name = entry.name;
		if (mc.fontRenderer.getStringWidth(name) > xSize + 18)
			name = mc.fontRenderer.trimStringToWidth(name, xSize + 18) + "...";
		int stringWidth = mc.fontRenderer.getStringWidth(name);
		int hover_x_min = xPosition - stringWidth + 15;
		int hover_y_min = yPosition;
		int hover_x_max = xPosition + 15;
		int hover_y_max = yPosition + slotHeight;

		boolean hover_string = mouseX >= hover_x_min && mouseY >= hover_y_min && mouseX < hover_x_max && mouseY < hover_y_max;

		if (hover_string)
		{
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int l2 = 0;
			int k2 = hover_y_min - 10;
			String[] lines = (entry.name + Configuration.NEW_LINE + Configuration.NEW_LINE + ConfigManager.formatCommentForGui(entry.comment, entry.defaultValue, entry.type, entry.min, entry.max) + (entry.hasPermission ? "" : Configuration.NEW_LINE + TextFormatting.RED + "" + TextFormatting.BOLD + "Higher permission level required")).replaceAll(Configuration.NEW_LINE, "\n").split("\\n");
			for (int i = 0; i < lines.length; i++)
				if (mc.fontRenderer.getStringWidth(lines[i]) > l2)
					l2 = mc.fontRenderer.getStringWidth(lines[i]);
			drawGradientRect(mouseX - 3, k2 - 3, mouseX + l2 + 3, k2 + 11 + (10 * (lines.length - 1)), 0xc0000000, 0xc0000000);
			for (int i = 0; i < lines.length; i++)
				mc.fontRenderer.drawStringWithShadow(lines[i], mouseX, k2 + (i * (slotHeight / 2)), -1);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
	}

	public boolean keyTyped(char c, int i)
	{
		if (selected != -1)
		{
			GuiConfigEntry entry = options.get(selected); 
			if (entry.textField.isFocused())
			{
				entry.textField.textboxKeyTyped(c, i);
				entry.textField.updateChange();
				if (i == Keyboard.KEY_RETURN)
				{
					selected = -1;
					entry.textField.isFocused = false;
					return true;
				}
				else if (i == Keyboard.KEY_ESCAPE)
				{
					selected = -1;
					entry.textField.isFocused = false;
				}
			}
			return false;
		}
		else
		{
			int height = getScrollHeight();
			if (i == Keyboard.KEY_UP)
				adjustScrollPos(-height / getSize());
			else if (i == Keyboard.KEY_DOWN)
				adjustScrollPos(height / getSize());
			else if (i == Keyboard.KEY_NEXT)
				adjustScrollPos(height / getSize() + slotHeight * 8);
			else if (i == Keyboard.KEY_PRIOR)
				adjustScrollPos(-height / getSize() - slotHeight * 8);
			else if (i == Keyboard.KEY_HOME)
				adjustScrollPos(-(getScrollHeight() - ySize));
			else if (i == Keyboard.KEY_END)
				adjustScrollPos(getScrollHeight() - ySize);
		}
		
		return true;
	}

	/**
	 * Draws a rectangle with a vertical gradient between the specified colors.
	 */
	protected void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6)
	{
		float f = (float)(par5 >> 24 & 255) / 255.0F;
		float f1 = (float)(par5 >> 16 & 255) / 255.0F;
		float f2 = (float)(par5 >> 8 & 255) / 255.0F;
		float f3 = (float)(par5 & 255) / 255.0F;
		float f4 = (float)(par6 >> 24 & 255) / 255.0F;
		float f5 = (float)(par6 >> 16 & 255) / 255.0F;
		float f6 = (float)(par6 >> 8 & 255) / 255.0F;
		float f7 = (float)(par6 & 255) / 255.0F;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
			
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		vertexbuffer.pos((double)par3, (double)par2, (double)0).color(f1, f2, f3, f).endVertex();
		vertexbuffer.pos((double)par1, (double)par2, (double)0).color(f1, f2, f3, f).endVertex();
		vertexbuffer.pos((double)par1, (double)par4, (double)0).color(f5, f6, f7, f4).endVertex();
		vertexbuffer.pos((double)par3, (double)par4, (double)0).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();
		
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public void populateData()
	{
		scrollPos = 0.0F;
		options.clear();
		ConfigInstance config = configs.get(GuiConfigEditor.curIndex);
		
		if (config != null)
			config.getFields().forEach(field ->
			{
				boolean hasPermission = field.hasPermission();
				if ((!(!hasPermission && field.hide) || hasPermission) && (field.enforce && (GuiConfigEditor.serverMode || ConfigManager.isSinglePlayer()) || !field.enforce))
					options.add(new GuiConfigEntry(field, GuiConfigEditor.serverMode));
			});
	}
	
	@Override
	protected int getSize()
	{
		return options.size();
	}
}
