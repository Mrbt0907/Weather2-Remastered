package net.mrbt0907.configex.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiBetterTextField extends Gui
{
	/**
	 * Have the font renderer from GuiScreen to render the textbox text into the screen.
	 */
	public FontRenderer fontRenderer;
	public int xPos;
	public int yPos;

	/** The width of this text field. */
	public int width;
	public int height;

	/** Have the current text beign edited on the textbox. */
	public final String defaultText;
	public final String originalText;
	public String text = "";
	public int maxStringLength = 10000;
	public int cursorCounter;
	public boolean enableBackgroundDrawing = true;

	/**
	 * if true the textbox can lose focus by clicking elsewhere on the screen
	 */
	public boolean canLoseFocus = true;

	/**
	 * If this value is true along isEnabled, keyTyped will process the keys.
	 */
	public boolean isFocused = false;

	/**
	 * If this value is true along isFocused, keyTyped will process the keys.
	 */
	public boolean isEnabled = true;

	/**
	 * The current character index that should be used as start of the rendered text.
	 */
	public int lineScrollOffset = 0;
	public int cursorPosition = 0;

	/** other selection position, maybe the same as the cursor */
	public int selectionEnd = 0;
	public int enabledColor = 14737632;
	public int disabledColor = 0xBB1010;

	/** True if this textbox is visible */
	public boolean visible = true;
	public boolean hasChanged;
	
	public GuiBetterTextField(FontRenderer par1FontRenderer, int par2, int par3, int par4, int par5, String text, String defaultText)
	{
		fontRenderer = par1FontRenderer;
		xPos = par2;
		yPos = par3;
		width = par4;
		height = par5;
		this.text = originalText = text;
		this.defaultText = defaultText;
	}

	public void updateChange()
	{
		hasChanged = !text.equals(originalText);
	}
	
	/**
	 * Increments the cursor counter
	 */
	public void updateCursorCounter()
	{
		++cursorCounter;
	}

	/**
	 * Sets the text of the textbox.
	 */
	public void setText(String par1Str)
	{
		if (par1Str.length() > maxStringLength)
			text = par1Str.substring(0, maxStringLength);
		else
			text = par1Str;

		setCursorPositionEnd();
	}

	/**
	 * Returns the text beign edited on the textbox.
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * @return returns the text between the cursor and selectionEnd
	 */
	public String getSelectedtext()
	{
		int i = cursorPosition < selectionEnd ? cursorPosition : selectionEnd;
		int j = cursorPosition < selectionEnd ? selectionEnd : cursorPosition;
		return text.substring(i, j);
	}

	/**
	 * replaces selected text, or inserts text at the position on the cursor
	 */
	public void writeText(String par1Str)
	{
		String s1 = "";
		String s2 = ChatAllowedCharacters.filterAllowedCharacters(par1Str);
		int i = cursorPosition < selectionEnd ? cursorPosition : selectionEnd;
		int j = cursorPosition < selectionEnd ? selectionEnd : cursorPosition;
		int k = maxStringLength - text.length() - (i - selectionEnd);

		if (text.length() > 0)
			s1 = s1 + text.substring(0, i);

		int l;

		if (k < s2.length())
		{
			s1 = s1 + s2.substring(0, k);
			l = k;
		}
		else
		{
			s1 = s1 + s2;
			l = s2.length();
		}

		if (text.length() > 0 && j < text.length())
			s1 = s1 + text.substring(j);

		text = s1;
		moveCursorBy(i - selectionEnd + l);
	}

	/**
	 * Deletes the specified number of words starting at the cursor position. Negative numbers will delete words left of
	 * the cursor.
	 */
	public void deleteWords(int par1)
	{
		if (text.length() != 0)
		{
			if (selectionEnd != cursorPosition)
				writeText("");
			else
				deleteFromCursor(getNthWordFromCursor(par1) - cursorPosition);
		}
	}

	/**
	 * delete the selected text, otherwsie deletes characters from either side of the cursor. params: delete num
	 */
	public void deleteFromCursor(int par1)
	{
		if (text.length() != 0)
		{
			if (selectionEnd != cursorPosition)
				writeText("");
			else
			{
				boolean flag = par1 < 0;
				int j = flag ? cursorPosition + par1 : cursorPosition;
				int k = flag ? cursorPosition : cursorPosition + par1;
				String s = "";

				if (j >= 0)
					s = text.substring(0, j);

				if (k < text.length())
					s = s + text.substring(k);

				text = s;

				if (flag)
					moveCursorBy(par1);
			}
		}
	}

	/**
	 * see @getNthNextWordFromPos() params: N, position
	 */
	public int getNthWordFromCursor(int par1)
	{
		return getNthWordFromPos(par1, getCursorPosition());
	}

	/**
	 * gets the position of the nth word. N may be negative, then it looks backwards. params: N, position
	 */
	public int getNthWordFromPos(int par1, int par2)
	{
		return func_73798_a(par1, getCursorPosition(), true);
	}

	public int func_73798_a(int par1, int par2, boolean par3)
	{
		int k = par2;
		boolean flag1 = par1 < 0;
		int l = Math.abs(par1);

		for (int i1 = 0; i1 < l; ++i1)
			if (flag1)
			{
				while (par3 && k > 0 && text.charAt(k - 1) == 32)
					--k;

				while (k > 0 && text.charAt(k - 1) != 32)
					--k;
			}
			else
			{
				int j1 = text.length();
				k = text.indexOf(32, k);

				if (k == -1)
					k = j1;
				else
					while (par3 && k < j1 && text.charAt(k) == 32)
						++k;
			}

		return k;
	}

	/**
	 * Moves the text cursor by a specified number of characters and clears the selection
	 */
	public void moveCursorBy(int par1)
	{
		setCursorPosition(selectionEnd + par1);
	}

	/**
	 * sets the position of the cursor to the provided index
	 */
	public void setCursorPosition(int par1)
	{
		cursorPosition = par1;
		int j = text.length();

		if (cursorPosition < 0)
			cursorPosition = 0;

		if (cursorPosition > j)
			cursorPosition = j;

		setSelectionPos(cursorPosition);
	}

	/**
	 * sets the cursors position to the beginning
	 */
	public void setCursorPositionZero()
	{
		setCursorPosition(0);
	}

	/**
	 * sets the cursors position to after the text
	 */
	public void setCursorPositionEnd()
	{
		setCursorPosition(text.length());
	}

	/**
	 * Call this method from you GuiScreen to process the keys into textbox.
	 */
	public boolean textboxKeyTyped(char par1, int par2)
	{
		if (isEnabled && isFocused)
			switch (par1)
			{
				case 1:
					setCursorPositionEnd();
					setSelectionPos(0);
					return true;
				case 3:
					GuiScreen.setClipboardString(getSelectedtext());
					return true;
				case 22:
					writeText(GuiScreen.getClipboardString());
					return true;
				case 24:
					GuiScreen.setClipboardString(getSelectedtext());
					writeText("");
					return true;
				default:
					switch (par2)
					{
						case 14:
							if (GuiScreen.isCtrlKeyDown())
								deleteWords(-1);
							else
								deleteFromCursor(-1);

							return true;
						case 199:
							if (GuiScreen.isShiftKeyDown())
								setSelectionPos(0);
							else
								setCursorPositionZero();

							return true;
						case 203:
							if (GuiScreen.isShiftKeyDown())
								if (GuiScreen.isCtrlKeyDown())
									setSelectionPos(getNthWordFromPos(-1, getSelectionEnd()));
								else
									setSelectionPos(getSelectionEnd() - 1);
							else if (GuiScreen.isCtrlKeyDown())
								setCursorPosition(getNthWordFromCursor(-1));
							else
								moveCursorBy(-1);

							return true;
						case 205:
							if (GuiScreen.isShiftKeyDown())
								if (GuiScreen.isCtrlKeyDown())
									setSelectionPos(getNthWordFromPos(1, getSelectionEnd()));
								else
									setSelectionPos(getSelectionEnd() + 1);
							else if (GuiScreen.isCtrlKeyDown())
								setCursorPosition(getNthWordFromCursor(1));
							else
								moveCursorBy(1);

							return true;
						case 207:
							if (GuiScreen.isShiftKeyDown())
								setSelectionPos(text.length());
							else
								setCursorPositionEnd();

							return true;
						case 211:
							if (GuiScreen.isCtrlKeyDown())
								deleteWords(1);
							else
								deleteFromCursor(1);
							return true;
						default:
							if (ChatAllowedCharacters.isAllowedCharacter(par1))
							{
								writeText(Character.toString(par1));
								return true;
							}
							else
								return false;
					}
			}
		else
			return false;
	}

	/**
	 * Args: x, y, buttonClicked
	 */
	public void mouseClicked(int par1, int par2, int eventButton)
	{
		boolean flag = par1 >= xPos && par1 < xPos + width && par2 >= yPos && par2 < yPos + height;

		if (canLoseFocus)
			setFocused(isEnabled && flag);

		if (isFocused && eventButton == 0 || eventButton == 1)
		{
			int l = par1 - xPos;

			if (enableBackgroundDrawing)
				l -= 4;

			String s = fontRenderer.trimStringToWidth(text.substring(lineScrollOffset), getWidth());
			setCursorPosition(fontRenderer.trimStringToWidth(s, l).length() + lineScrollOffset);

			if (flag && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)))
			{
				if (eventButton == 0)
					if (text.equals("true"))
						text = "false";
					else if (text.equals("false"))
						text = "true";
					else
						text = originalText;
				else if (eventButton == 1)
					text = defaultText;
				
				setCursorPosition(0);
				updateChange();
			}
		}
	}

	/**
	 * Draws the textbox
	 */
	public void drawTextBox()
	{
		if (getVisible())
		{
			updateCursorCounter();
			if (getEnableBackgroundDrawing())
			{
				drawRect(xPos - 1, yPos - 1, xPos + width + 1, yPos + height + 1, isEnabled ? isFocused ? hasChanged ? 0xFFAACCFF : 0xFF88AADD : hasChanged ? 0xFF75AA75 : 0xFFAAAAAA : 0xFFAA6060);
				drawRect(xPos, yPos, xPos + width, yPos + height, 0x70000000);
			}

			int i = isEnabled ? isFocused ? hasChanged ? 0xDDDDFF : enabledColor : hasChanged ? 0x90FF90 : enabledColor : disabledColor;
			int j = cursorPosition - lineScrollOffset;
			int k = selectionEnd - lineScrollOffset;
			String s = fontRenderer.trimStringToWidth(text.substring(lineScrollOffset), getWidth());
			boolean flag = j >= 0 && j <= s.length();
			boolean flag1 = isFocused && cursorCounter / 40 % 2 == 0 && flag;
			int l = enableBackgroundDrawing ? xPos + 4 : xPos;
			int i1 = enableBackgroundDrawing ? yPos + (height - 8) / 2 : yPos;
			int j1 = l;

			if (k > s.length())
				k = s.length();

			if (s.length() > 0)
			{
				String s1 = flag ? s.substring(0, j) : s;
				j1 = fontRenderer.drawStringWithShadow(s1, l, i1, i);
			}

			boolean flag2 = cursorPosition < text.length() || text.length() >= getMaxStringLength();
			int k1 = j1;

			if (!flag)
				k1 = j > 0 ? l + width : l;
			else if (flag2)
			{
				k1 = j1 - 1;
				--j1;
			}

			if (s.length() > 0 && flag && j < s.length())
				fontRenderer.drawStringWithShadow(s.substring(j), j1, i1, i);

			if (flag1)
			{
				if (flag2)
					Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + fontRenderer.FONT_HEIGHT, -3092272);
				else
					fontRenderer.drawStringWithShadow("_", k1, i1, i);
			}

			if (k != j)
			{
				int l1 = l + fontRenderer.getStringWidth(s.substring(0, k));
				drawCursorVertical(k1, i1 - 1, l1 - 1, i1 + 1 + fontRenderer.FONT_HEIGHT);
			}
		}
	}

	/**
	 * draws the vertical line cursor in the textbox
	 */
	private void drawCursorVertical(int par1, int par2, int par3, int par4)
	{
		int i1;

		if (par1 < par3)
		{
			i1 = par1;
			par1 = par3;
			par3 = i1;
		}

		if (par2 < par4)
		{
			i1 = par2;
			par2 = par4;
			par4 = i1;
		}
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.enableColorLogic();
		GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
		vertexbuffer.pos((double)par1, (double)par4, 0.0D).endVertex();
		vertexbuffer.pos((double)par3, (double)par4, 0.0D).endVertex();
		vertexbuffer.pos((double)par3, (double)par2, 0.0D).endVertex();
		vertexbuffer.pos((double)par1, (double)par2, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.disableColorLogic();
		GlStateManager.enableTexture2D();
	}

	public void setMaxStringLength(int par1)
	{
		maxStringLength = par1;

		if (text.length() > par1)
		{
			text = text.substring(0, par1);
		}
	}

	/**
	 * returns the maximum number of character that can be contained in this textbox
	 */
	public int getMaxStringLength()
	{
		return maxStringLength;
	}

	/**
	 * returns the current position of the cursor
	 */
	public int getCursorPosition()
	{
		return cursorPosition;
	}

	/**
	 * get enable drawing background and outline
	 */
	public boolean getEnableBackgroundDrawing()
	{
		return enableBackgroundDrawing;
	}

	/**
	 * enable drawing background and outline
	 */
	public void setEnableBackgroundDrawing(boolean par1)
	{
		enableBackgroundDrawing = par1;
	}

	/**
	 * Sets the text colour for this textbox (disabled text will not use this colour)
	 */
	public void setTextColor(int par1)
	{
		enabledColor = par1;
	}

	public void setDisabledTextColour(int par1)
	{
		disabledColor = par1;
	}

	/**
	 * setter for the focused field
	 */
	public void setFocused(boolean par1)
	{
		if (par1 && !isFocused)
			cursorCounter = 0;

		isFocused = par1;
	}

	/**
	 * getter for the focused field
	 */
	public boolean isFocused()
	{
		return isFocused;
	}

	public void setEnabled(boolean par1)
	{
		isEnabled = par1;
	}

	/**
	 * the side of the selection that is not the cursor, maye be the same as the cursor
	 */
	public int getSelectionEnd()
	{
		return selectionEnd;
	}

	/**
	 * returns the width of the textbox depending on if the the box is enabled
	 */
	public int getWidth()
	{
		return getEnableBackgroundDrawing() ? width - 8 : width;
	}

	/**
	 * Sets the position of the selection anchor (i.e. position the selection was started at)
	 */
	public void setSelectionPos(int par1)
	{
		int j = text.length();

		if (par1 > j)
			par1 = j;

		if (par1 < 0)
			par1 = 0;

		selectionEnd = par1;

		if (fontRenderer != null)
		{
			if (lineScrollOffset > j)
				lineScrollOffset = j;

			int k = getWidth();
			String s = fontRenderer.trimStringToWidth(text.substring(lineScrollOffset), k);
			int l = s.length() + lineScrollOffset;

			if (par1 == lineScrollOffset)
				lineScrollOffset -= fontRenderer.trimStringToWidth(text, k, true).length();

			if (par1 > l)
				lineScrollOffset += par1 - l;
			else if (par1 <= lineScrollOffset)
				lineScrollOffset -= lineScrollOffset - par1;

			if (lineScrollOffset < 0)
				lineScrollOffset = 0;

			if (lineScrollOffset > j)
				lineScrollOffset = j;
		}
	}

	/**
	 * if true the textbox can lose focus by clicking elsewhere on the screen
	 */
	public void setCanLoseFocus(boolean par1)
	{
		canLoseFocus = par1;
	}

	/**
	 * @return {@code true} if this textbox is visible
	 */
	public boolean getVisible()
	{
		return visible;
	}

	/**
	 * Sets whether or not this textbox is visible
	 */
	public void setVisible(boolean par1)
	{
		visible = par1;
	}
}
