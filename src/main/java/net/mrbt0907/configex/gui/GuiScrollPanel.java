package net.mrbt0907.configex.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class GuiScrollPanel
{
	/** button id of the button used to scroll up */
	public static final int scrollUpButtonID = 7;
	/** the buttonID of the button used to scroll down */
	public static final int scrollDownButtonID = 8;
	public final Minecraft mc;
	public final int slotHeight;
	public final int scrollUpID;
	public final int scrollDownID;
	public int xStart;
	public int yStart;
	public int xScrollBar;
	public int xSize;
	public int ySize;
	public int scrollBarSize;
	
	public int selected = -1;
	protected float scrollPos;
	/**The mouse position in which the user started to drag the scroll bar*/
	protected float initialMouseY;
	
	//TODO: Figure out how to get the scroll menu to stay in one place and size correctly, then figure out how to get it to scroll again with a custom renderer
	/**Creates a gui element that can be scrolled with a custom vertical scroll bar
	 * @param mc  The instance of the Minecraft client
	 * @param xStart  How far right will the panel be
	 * @param xStart  How far down will the panel be
	 * @param xScrollBar  The x position of the scroll bar offset by xStart
	 * @param xSize  The width of the panel moving right
	 * @param ySize  The height of the panel moving down
	 * @param slotHeight  The height of each slot in the panel
	 * @param scrollUpID  The button id this panel will use to detect scroll up movement
	 * @param scrollDownID  The button id this panel will use to detect scroll down movement*/
	public GuiScrollPanel(Minecraft mc, int xStart, int yStart, int xScrollBar, int xSize, int ySize, int scrollBarSize, int slotHeight, int scrollUpID, int scrollDownID)
	{
		this.mc = mc;
		this.xStart = xStart;
		this.yStart = yStart;
		this.xScrollBar = xScrollBar;
		this.xSize = xSize;
		this.ySize = ySize;
		this.scrollBarSize = scrollBarSize;
		this.slotHeight = slotHeight;
		this.scrollUpID = scrollUpID;
		this.scrollDownID = scrollDownID;
	}

	/**Executes when a slot is clicked upon
	 * @param slot  The slot id that was clicked on
	 * @param doubleClicked  Was the slot double clicked upon?*/
	protected abstract void onSlotClicked(int slot, boolean doubleClicked);
	/**Return true if the slot should be selected*/
	protected abstract boolean isSelected(int slot);
	/**Draws a background behind all slots*/
	protected abstract void drawBackground(Tessellator tessellator, int mouseX, int mouseY, float partialTicks);
	/**Draws a scroll bar behind the foreground*/
	protected abstract void drawScrollBar(Tessellator tessellator, int mouseX, int mouseY, float partialTicks);
	/**Draws a foreground in front of all slots*/
	protected abstract void drawForeground(Tessellator tessellator, int mouseX, int mouseY, float partialTicks);
	/**Draws one of the slots within the panel*/
	protected abstract void drawSlotPre(Tessellator tessellatorint, int xPos, int yPos, int slot);
	/**Draws one of the slots within the panel in front of the foreground*/
	protected abstract void drawSlotPost(Tessellator tessellatorint, int xPos, int yPos, int slot);
	/**Returns the current length of the entries this panel will render*/
	protected abstract int getSize();
	/**Returns the total height of the slot list*/
	protected int getScrollHeight()
	{
		return getSize() * slotHeight;
	}
	
	/**Moves the scroll position by the adjustment value; This is in actual height, not percentage*/
	protected void setScrollPos(float scrollPos)
	{
		this.scrollPos = MathHelper.clamp(scrollPos, 0.0F, getScrollHeight() - ySize);
	}
	
	/**Moves the scroll position by the adjustment value; This is in actual height, not percentage*/
	protected void adjustScrollPos(float adjustment)
	{
		setScrollPos(scrollPos + adjustment);
	}
	

	/**Moves the scroll position by the adjustment value; This is in actual height, not percentage*/
	protected void setScrollPosPerc(float perc)
	{
		setScrollPos((getScrollHeight() - ySize) * perc);
	}

	public void actionPerformed(GuiButton button)
	{
		if (button.enabled && getScrollHeight() - ySize > 0)
			if (button.id == scrollUpID)
				adjustScrollPos(-slotHeight / getSize() * 0.75F);
			else if (button.id == scrollDownID)
				adjustScrollPos(slotHeight / getSize() * 0.75F);
	}

	/**Prepares each slot position and renders each slot and renders the scroll bar
	 * @param mouseX  The current mouse position on the x axis
	 * @param mouseY  The current mouse position on the x axis
	 * @param partialTicks  Used to smoothen gui transitions*/
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		int size = getSize();
		int height = getScrollHeight();
		int slot = 0;
		int slotHeight = 0;
		
		if (Mouse.isButtonDown(0))
		{
			int realY = yStart + ySize;
			slot = (int)((scrollPos + (mouseY - yStart)) / this.slotHeight);
			if (slot < size && mouseX > xStart && mouseX < xStart + xSize && mouseY > yStart && mouseY < realY)
				onSlotClicked(slot, false);
			else
				selected = -1;
		}
		else
		{
			while (!mc.gameSettings.touchscreen && getScrollHeight() - ySize > 0 && Mouse.next())
			{
				int l2 = Mouse.getEventDWheel();
				if (l2 > 0)
					adjustScrollPos(-height / getSize() * 0.75F);
				else if (l2 < 0)
					adjustScrollPos(height / getSize() * 0.75F);
			}
		}

		slot = (int)(scrollPos / this.slotHeight);
		slotHeight = slot * this.slotHeight;
		GlStateManager.pushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		Tessellator tessellator = Tessellator.getInstance();
		drawBackground(tessellator, mouseX, mouseY, partialTicks);
		for (int i = slot; i < size && slotHeight - scrollPos < ySize; i++, slotHeight += this.slotHeight)
			drawSlotPre(tessellator, xStart, (int)(yStart + slotHeight - scrollPos), i);
		drawScrollBar(tessellator, mouseX, mouseY, partialTicks);
		drawForeground(tessellator, mouseX, mouseY, partialTicks);

		slotHeight = slot * this.slotHeight;
		for (int i = slot; i < size && slotHeight - scrollPos < ySize; i++, slotHeight += this.slotHeight)
			drawSlotPost(tessellator, xStart, (int)(yStart + slotHeight - scrollPos), i);
		GlStateManager.popMatrix();
		/*
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

				if (mouseY >= yStart && mouseY <= bottom)
				{
					int k2 = xSize / 2 - 110;
					j1 = xSize / 2 + 110;
					k1 = mouseY - yStart - (int)amountScrolled - 4;
					l1 = k1 / slotHeight;

					if (mouseX >= k2 && mouseX <= j1 && l1 >= 0 && k1 >= 0 && l1 < k)
					{
						boolean flag1 = l1 == selectedElement && Minecraft.getSystemTime() - lastClicked < 250L;
						elementClicked(l1, flag1);
						selectedElement = l1;
						lastClicked = Minecraft.getSystemTime();
					}
					else if (mouseX >= k2 && mouseX <= j1 && k1 < 0)
						flag = false;

					if (mouseX >= l && mouseX <= i1)
					{
						scrollMultiplier = -1.0F;
						j2 = func_77209_d();

						if (j2 < 1)
							j2 = 1;

						i2 = (int)((float)((bottom - yStart) * (bottom - yStart)) / (float)getScrollHeight());

						if (i2 < 32)
							i2 = 32;

						if (i2 > bottom - yStart - 8)
							i2 = bottom - yStart - 8;

						scrollMultiplier /= (float)(bottom - yStart - i2) / (float)j2;
					}
					else
						scrollMultiplier = 1.0F;

					if (flag)
						initialClickY = (float)mouseY;
					else
						initialClickY = -2.0F;
				}
				else
					initialClickY = -2.0F;
			}
			else if (initialClickY >= 0.0F)
			{
				amountScrolled -= ((float)mouseY - initialClickY) * scrollMultiplier;
				initialClickY = (float)mouseY;
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
		j1 = xSize / 2 - 16;
		k1 = yStart + 4 - (int)amountScrolled;

		int i3;

		for (l1 = 0; l1 < k; ++l1)
		{
			j2 = k1 + l1 * slotHeight;
			i2 = slotHeight - 4;

			if (j2 <= bottom && j2 + i2 >= yStart)
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
			i2 = (bottom - yStart) * (bottom - yStart) / getScrollHeight();

			if (i2 < 32)
				i2 = 32;

			if (i2 > bottom - yStart - 8)
				i2 = bottom - yStart - 8;

			i3 = (int)amountScrolled * (bottom - yStart - i2) / j2 + yStart;
			if (i3 < yStart)
				i3 = yStart;
			
			
			
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexbuffer.pos((double)l, (double)bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 100).endVertex();
			vertexbuffer.pos((double)i1, (double)bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 100).endVertex();
			vertexbuffer.pos((double)i1, (double)yStart, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 100).endVertex();
			vertexbuffer.pos((double)l, (double)yStart, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 100).endVertex();
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

			if (j2 <= bottom && j2 + i2 >= yStart)
				drawSlotPost(l1, j1, j2, i2, tessellator);
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);*/
	}
}
