package net.mrbt0907.configex.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class GuiBetterSlot
{
	/** button id of the button used to scroll up */
	public static final int scrollUpButtonID = 7;
	/** the buttonID of the button used to scroll down */
	public static final int scrollDownButtonID = 8;
	public final Minecraft mc;
	/**The width of the GuiScreen. Affects the container rendering, but not the overlays.*/
	public int width;
	/**The height of the GuiScreen. Affects the container rendering, but not the overlays or the scrolling.*/
	public int height;
	/** The top of the slot container. Affects the overlays and scrolling. */
	public int top;
	/** The bottom of the slot container. Affects the overlays and scrolling. */
	public int bottom;
	public int right;
	public int left;
	/** The height of a slot. */
	public final int slotHeight;
	/** X axis position of the mouse */
	public int mouseX;
	/** Y axis position of the mouse */
	public int mouseY;
	/** where the mouse was in the window when you first clicked to scroll */
	public float initialClickY = -2.0F;
	/**
	 * what to multiply the amount you moved your mouse by(used for slowing down scrolling when over the items and no on
	 * scroll bar)
	 */
	public float scrollMultiplier;
	/** how far down this slot has been scrolled */
	public float amountScrolled;
	/** the element in the list that was selected */
	public int selectedElement = -1;
	/** the time when this button was last clicked. */
	public long lastClicked = 0L;
	
	//public String BACKGROUND_IMAGE = "/gui/background.png";
	public ResourceLocation resBG = new ResourceLocation("/gui/background.png");

	public GuiBetterSlot(Minecraft mc, int width, int height, int left, int top, int bottom, int slotHeight)
	{
		this.mc = mc;
		this.width = width + left;
		this.height = height;
		this.left = 0;
		this.top = top;
		this.bottom = bottom;
		this.slotHeight = slotHeight;
		right = width;
	}

	/**
	 * Gets the size of the current slot list.
	 */
	protected abstract int getSize();

	/**
	 * the element in the slot that was clicked, boolean for wether it was double clicked or not
	 */
	protected abstract void elementClicked(int i, boolean flag);

	/**
	 * returns true if the element passed in is currently selected
	 */
	protected abstract boolean isSelected(int i);

	/**
	 * return the height of the content being scrolled
	 */
	protected int getContentHeight()
	{
		return getSize() * slotHeight;
	}

	protected abstract void drawBackground(Tessellator tessellator);
	protected abstract void drawForeground(Tessellator tessellator);

	protected abstract void drawSlotPre(int i, int j, int k, int l, Tessellator tessellator);
	protected abstract void drawSlotPost(int i, int j, int k, int l, Tessellator tessellator);

	/**
	 * stop the thing from scrolling out of bounds
	 */
	private void bindAmountScrolled()
	{
		int i = func_77209_d();

		if (i < 0)
			i /= 2;

		if (amountScrolled < 0.0F)
			amountScrolled = 0.0F;

		if (amountScrolled > (float)i)
			amountScrolled = (float)i;
	}

	public int func_77209_d()
	{
		return getContentHeight() - (bottom - top - 4);
	}

	public void actionPerformed(GuiButton par1GuiButton)
	{
		if (par1GuiButton.enabled)
		{
			if (par1GuiButton.id == scrollUpButtonID)
			{
				amountScrolled -= (float)(slotHeight * 2 / 3);
				initialClickY = -2.0F;
				bindAmountScrolled();
			}
			else if (par1GuiButton.id == scrollDownButtonID)
			{
				amountScrolled += (float)(slotHeight * 2 / 3);
				initialClickY = -2.0F;
				bindAmountScrolled();
			}
		}
	}

	/**
	 * draws the slot to the screen, pass in mouse's current x and y and partial ticks
	 */
	public void drawScreen(int par1, int par2, float par3)
	{
		mouseX = par1;
		mouseY = par2;
		int k = getSize();
		int l = getScrollBarX();
		int i1 = l + 6;
		int j1;
		int k1;
		int l1;
		int i2;
		int j2;

		if (Mouse.isButtonDown(0))
		{
			if (initialClickY == -1.0F)
			{
				boolean flag = true;

				if (par2 >= top && par2 <= bottom)
				{
					int k2 = width / 2 - 110;
					j1 = width / 2 + 110;
					k1 = par2 - top - (int)amountScrolled - 4;
					l1 = k1 / slotHeight;

					if (par1 >= k2 && par1 <= j1 && l1 >= 0 && k1 >= 0 && l1 < k)
					{
						boolean flag1 = l1 == selectedElement && Minecraft.getSystemTime() - lastClicked < 250L;
						elementClicked(l1, flag1);
						selectedElement = l1;
						lastClicked = Minecraft.getSystemTime();
					}
					else if (par1 >= k2 && par1 <= j1 && k1 < 0)
						flag = false;

					if (par1 >= l && par1 <= i1)
					{
						scrollMultiplier = -1.0F;
						j2 = func_77209_d();

						if (j2 < 1)
							j2 = 1;

						i2 = (int)((float)((bottom - top) * (bottom - top)) / (float)getContentHeight());

						if (i2 < 32)
							i2 = 32;

						if (i2 > bottom - top - 8)
							i2 = bottom - top - 8;

						scrollMultiplier /= (float)(bottom - top - i2) / (float)j2;
					}
					else
						scrollMultiplier = 1.0F;

					if (flag)
						initialClickY = (float)par2;
					else
						initialClickY = -2.0F;
				}
				else
					initialClickY = -2.0F;
			}
			else if (initialClickY >= 0.0F)
			{
				amountScrolled -= ((float)par2 - initialClickY) * scrollMultiplier;
				initialClickY = (float)par2;
			}
		}
		else
		{
			while (!mc.gameSettings.touchscreen && Mouse.next())
			{
				int l2 = Mouse.getEventDWheel();

				if (l2 != 0)
				{
					if (l2 > 0)
						l2 = -1;
					else if (l2 < 0)
						l2 = 1;

					amountScrolled += (float)(l2 * slotHeight / 2);
				}
			}

			initialClickY = -1.0F;
		}

		bindAmountScrolled();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		drawBackground(tessellator);
		j1 = width / 2 - 16;
		k1 = top + 4 - (int)amountScrolled;

		int i3;

		for (l1 = 0; l1 < k; ++l1)
		{
			j2 = k1 + l1 * slotHeight;
			i2 = slotHeight - 4;

			if (j2 <= bottom && j2 + i2 >= top)
				drawSlotPre(l1, j1, j2, i2, tessellator);
		}

		drawForeground(tessellator);
		
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		j2 = func_77209_d();

		if (j2 > 0)
		{
			i2 = (bottom - top) * (bottom - top) / getContentHeight();

			if (i2 < 32)
				i2 = 32;

			if (i2 > bottom - top - 8)
				i2 = bottom - top - 8;

			i3 = (int)amountScrolled * (bottom - top - i2) / j2 + top;
			if (i3 < top)
				i3 = top;
			
			
			
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexbuffer.pos((double)l, (double)bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 100).endVertex();
			vertexbuffer.pos((double)i1, (double)bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 100).endVertex();
			vertexbuffer.pos((double)i1, (double)top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 100).endVertex();
			vertexbuffer.pos((double)l, (double)top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 100).endVertex();
			tessellator.draw();
			
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexbuffer.pos((double)l, (double)(i3 + i2), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos((double)i1, (double)(i3 + i2), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos((double)i1, (double)i3, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
			vertexbuffer.pos((double)l, (double)i3, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
			tessellator.draw();
			
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexbuffer.pos((double)l, (double)(i3 + i2 - 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos((double)(i1 - 1), (double)(i3 + i2 - 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos((double)(i1 - 1), (double)i3, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos((double)l, (double)i3, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
			tessellator.draw();
		}
		for (l1 = 0; l1 < k; ++l1)
		{
			j2 = k1 + l1 * slotHeight;
			i2 = slotHeight - 4;

			if (j2 <= bottom && j2 + i2 >= top)
				drawSlotPost(l1, j1, j2, i2, tessellator);
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	protected int getScrollBarX()
	{
		return width / 2 + 124;
	}
}
