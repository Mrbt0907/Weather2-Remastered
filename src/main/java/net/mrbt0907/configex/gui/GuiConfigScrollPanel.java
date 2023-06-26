package net.mrbt0907.configex.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.manager.ConfigInstance;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiConfigScrollPanel extends GuiBetterSlot
{
	private GuiConfigEditor config;
	public final List<ConfigInstance> configs;
	public final List<GuiConfigEntry> options;
	private int mouseX;
	private int mouseY;
	private int selected = -1;

	public GuiConfigScrollPanel(GuiConfigEditor controls, Minecraft mc, int startX, int startY, int height, int slotSize)
	{
		super(mc, controls.width, controls.height, startX, startY, height, slotSize);
		this.config = controls;
		configs = ConfigManager.getInstances();
		options = new ArrayList<GuiConfigEntry>();
		populateData();
	}

	@Override
	protected void elementClicked(int i, boolean flag)
	{
		if (!flag)
		{
			selected = i;
			KeyBinding.resetKeyBindingArrayAndHash();
		}
	}
	
	protected void mouseClicked(int par1, int par2, int eventButton)
	{
		for (GuiConfigEntry entry : options)
			entry.textField.mouseClicked(par1, par2, eventButton);
	}

	@Override
	protected boolean isSelected(int i) {return false;}

	@Override
	protected void drawBackground(Tessellator tess) {}
	
	@Override
	protected void drawForeground(Tessellator tess)
	{
		config.drawForegroundLayer();
	}

	@Override
	public void drawScreen(int mX, int mY, float f)
	{
		mouseX = mX;
		mouseY = mY;

		if (selected != -1 && !Mouse.isButtonDown(0) && Mouse.getDWheel() == 0)
		{
			if (Mouse.next() && Mouse.getEventButtonState())
			{
				selected = -1;
				KeyBinding.resetKeyBindingArrayAndHash();
			}
		}

		try
		{
			super.drawScreen(mX, mY, f);
		}
		catch (Exception ex) {}
	}

	@Override
	protected void drawSlotPre(int index, int xPosition, int yPosition, int l, Tessellator tessellator)
	{
		if (getSize() == 0) return;
		xPosition -= 20;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		GuiConfigEntry entry = options.get(index);

		int stringWidth = mc.fontRenderer.getStringWidth(entry.name);
		config.drawString(mc.fontRenderer, entry.name, xPosition - stringWidth + 15, yPosition + 3, 0xFFFFFFFF);
		
		entry.textField.xPos = xPosition + 20;
		entry.textField.yPos = yPosition;
		entry.textField.drawTextBox();
		
		
	}

	@Override
	protected void drawSlotPost(int index, int xPosition, int yPosition, int l, Tessellator tessellator)
	{
		if (getSize() == 0) return;
		xPosition -= 20;
		GuiConfigEntry entry = options.get(index);
		int stringWidth = mc.fontRenderer.getStringWidth(entry.name);
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
			String[] lines = ConfigManager.formatCommentForGui(entry.comment, entry.defaultValue, entry.type, entry.min, entry.max).replaceAll(Configuration.NEW_LINE, "\n").split("\\n");
			for (int i = 0; i < lines.length; i++)
			{
				if (mc.fontRenderer.getStringWidth(lines[i]) > l2)
					l2 = mc.fontRenderer.getStringWidth(lines[i]);
			}
			drawGradientRect(mouseX - 3, k2 - 3, mouseX + l2 + 3, k2 + 11 + (10 * (lines.length - 1)), 0xc0000000, 0xc0000000);
			for (int i = 0; i < lines.length; i++)
				mc.fontRenderer.drawStringWithShadow(lines[i], mouseX, k2 + (i * (slotHeight / 2)), -1);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
	}

	public boolean keyTyped(char c, int i)
	{
		if (selected != -1 && options.get(selected).textField.isFocused())
		{
			if (options.get(selected).textField.textboxKeyTyped(c, i))
				options.get(selected).textField.updateChange();
			if (i == 28)
			{
				selected = -1;
				return true;
			}
			return false;
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
		options.clear();
		ConfigInstance config = configs.get(GuiConfigEditor.curIndex);
		
		if (config != null)
			config.getFields().forEach(field ->
			{
				boolean hasPermission = field.hasPermission();
				if ((!(!hasPermission && field.hide) || hasPermission) && (field.enforce && (GuiConfigEditor.serverMode || mc.isSingleplayer()) || !field.enforce))
					options.add(new GuiConfigEntry(field, GuiConfigEditor.serverMode));
			});
	}
	
	@Override
	protected int getSize()
	{
		return options.size();
	}
}
