package net.mrbt0907.weather2.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.mrbt0907.weather2.ClientProxy;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.EZGuiAPI;
import net.mrbt0907.weather2.client.gui.elements.GuiButtonBoolean;
import net.mrbt0907.weather2.client.gui.elements.GuiButtonCycle;
import net.mrbt0907.weather2.network.packets.PacketEZGUI;
import net.mrbt0907.weather2.util.TriMapEx;
import net.mrbt0907.weather2.util.WeatherUtilConfig;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import modconfig.gui.GuiConfigEditor;

public class GuiEZConfig extends GuiScreen
{
	public int xCenter;
	public int yCenter;
	public int xStart;
	public int yStart;
	/** The X size of the inventory window in pixels. */
	protected int xSize = 256;
	/** The Y size of the inventory window in pixels. */
	protected int ySize = 256;
	
	public ResourceLocation backgroundA = new ResourceLocation(Weather2.OLD_MODID + ":textures/gui/ez_gui_2.png");
	public ResourceLocation[] backgroundB = new ResourceLocation[0];
	public int page = 0;
	public int subPage = 0;
	public int maxSubPages = 0;
	public int maxEntries = 6;
	private boolean send = false;
	
	//Locals
	private static final String[] PAGEL = {"graphics", "system", "storms", "dimensions"};
	@SuppressWarnings("unused")
	private static final String[] FLAGL = {"flag.op","flag.reload"};
	
	//Other Things
	public static final String PREFIX = "btn_";
	
	//Main Buttons
	public static final String B_EXIT = "m_exit";
	public static final String B_ADVANCED = "m_advanced";
	public static final String B_NEXT = "m_next";
	public static final String B_PREVIOUS = "m_previous";
	public static final String B_GRAPHICS = "m_graphics";
	public static final String B_SYSTEM = "m_system";
	public static final String B_STORM = "m_storm";
	public static final String B_DIMENSION = "m_dimension";

	List<String> settings = new ArrayList<String>();
	
	//public static final String BD_MIN = "d_min";
	
	//Elements
	//public HashMap<Integer, List<GuiButton>> pages = new HashMap<Integer, List<GuiButton>>();
	public HashMap<Integer, String> buttons = new HashMap<Integer, String>();
	public NBTTagCompound nbtSendCache;
	
	public GuiEZConfig()
	{
		super();
		//only sync request on initial gui open
		PacketEZGUI.sync();
		
		//Initialize send cache.
		nbtSendCache = new NBTTagCompound();
		PacketEZGUI.isOp();
		EZGuiAPI.refreshOptions();
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return true;
	}
	
	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
	}
	
	public void resetGuiElements()
	{
		buttons.clear();
		buttonList.clear();
	}


	@Override
	public void drawBackground(int backgroundID)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(backgroundA);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		String title = format("title") + " - " + format(PAGEL[page]);
		
		drawScaledCustomSizeModalRect(x, y, 0.0F, 0.0F, 1, 1, 256, 256, 1.0F, 1.0F);
		drawString(this.fontRenderer, title, (int) ((xStart + 128) - this.fontRenderer.getStringWidth(title) * 0.5), yStart+15, 16777215);
	}
	
	public void drawElements()
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int buttonRowBX = 14;
		int buttonRowBY = 85;
		int buttonHeight = 25;
		int size = buttons.size();
		
		if (page != 3)
			for (int i = 0; i < size; i++)
				drawString(this.fontRenderer, format("button." + buttons.get(i + EZGuiAPI.BUTTON_MIN + (maxEntries * subPage)) + ".tooltip"), xStart + buttonRowBX, yStart + buttonRowBY + (buttonHeight * i), 16777215);
		else
			for (int i = 0; i < size; i++)
				drawString(this.fontRenderer, WordUtils.capitalize(String.valueOf(buttons.get(EZGuiAPI.BUTTON_MIN + i * 2)).replaceAll("\\_", " ")), xStart + buttonRowBX, yStart + buttonRowBY + (buttonHeight * i), 16777215);
		
		if (maxSubPages > 0)
			drawString(this.fontRenderer, format("misc.page", subPage + 1, maxSubPages + 1), xStart+46, yStart+238, 16777215);
	}
	
	protected <T extends GuiButton> T addButton(T button, String localization)
	{
		buttons.put(button.id, localization);
		buttonList.add(button);
		return button;
	}
	
	@Override
	protected <T extends GuiButton> T addButton(T button)
    {
		buttonList.add(button);
		return button;
    }
	
	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		drawBackground(-1);
		drawElements();
		super.drawScreen(par1, par2, par3);
	}
	
	@Override
	protected void keyTyped(char par1, int par2)
	{
		try
		{
			super.keyTyped(par1, par2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		try
		{
			super.mouseClicked(par1, par2, par3);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		if (send)
			send();
		resetGuiElements();
		ScaledResolution sr = new ScaledResolution(mc);
		int scaledWidth = sr.getScaledWidth();
		int scaledHeight = sr.getScaledHeight();
		int buttonWidth = 56;
		int buttonHeight = 20;
		int buttonRowAY = 48;
		int buttonRowBX = 187;
		int buttonRowBY = 80;
		int buttonRowCY = 232;
		
		xSize = 256;
		ySize = 256;
		xCenter = (int) (scaledWidth * 0.5F);
		yCenter = (int) (scaledHeight * 0.5F);
		xStart = (int) (xCenter - xSize * 0.5F);
		yStart = (int) (yCenter - ySize * 0.5F);
		
		addButton(new GuiButton(0, xStart + 193, yStart + buttonRowCY, buttonWidth, buttonHeight, format(EZGuiAPI.BUTTON_LIST[4])));
		if (ClientProxy.clientTickHandler.op) addButton(new GuiButton(1, xStart + 110, yStart + buttonRowCY, buttonWidth + 20, buttonHeight, format(EZGuiAPI.BUTTON_LIST[5])));
		addButton(new GuiButton(2, xStart + 8, yStart + buttonRowAY, buttonWidth, buttonHeight, format(EZGuiAPI.BUTTON_LIST[25])));
		addButton(new GuiButton(3, xStart + 69, yStart + buttonRowAY, buttonWidth, buttonHeight, format(EZGuiAPI.BUTTON_LIST[26])));
		addButton(new GuiButton(4, xStart + 130, yStart + buttonRowAY, buttonWidth, buttonHeight, format(EZGuiAPI.BUTTON_LIST[27])));
		addButton(new GuiButton(5, xStart + 191, yStart + buttonRowAY, buttonWidth, buttonHeight, format(EZGuiAPI.BUTTON_LIST[28])));
		
		int size = 0;
		int startingIndex = maxEntries * subPage;
		
		try
		{
			switch(page)
			{
				case 3:
					size = WeatherUtilConfig.dimNames.size();
					maxSubPages = size / (this.maxEntries);
					Object[] keys = WeatherUtilConfig.dimNames.keySet().toArray();
					Object[] values = WeatherUtilConfig.dimNames.values().toArray();
					int ii = 0, iii = 0;
					
					if (ClientProxy.clientTickHandler.op)
						for(int i = 0; i < size && i - startingIndex < this.maxEntries; i++)
						{
							ii = i + iii + EZGuiAPI.BUTTON_MIN;
							addButton(new GuiButtonCycle(ii, xStart + buttonRowBX - (buttonWidth + 5), yStart + buttonRowBY + (buttonHeight + 5) * i, buttonWidth, buttonHeight, EZGuiAPI.BL_WTOGGLE, WeatherUtilConfig.isWeatherEnabled((int) keys[i]) ? 1 : 0), (String) values[i]);
							addButton(new GuiButtonCycle(ii + 1, xStart + buttonRowBX, yStart + buttonRowBY + (buttonHeight + 5) * i, buttonWidth, buttonHeight, EZGuiAPI.BL_ETOGGLE, WeatherUtilConfig.isEffectsEnabled((int) keys[i]) ? 1 : 0));
							iii++;
						}
					
					break;
				default:
					TriMapEx<String, List<String>, Integer> options = EZGuiAPI.getOptions();
					Map<String, Integer> categories = EZGuiAPI.getOptionCategories();
					settings.clear();
					String id;
					
					for(Entry<String, Integer> entry : categories.entrySet())
						if (entry.getValue() == page)
							settings.add(entry.getKey());
					
					size = settings.size();
					maxSubPages = ((size - 1) / this.maxEntries);
					
					for(int i = 0; i < size && i - startingIndex < this.maxEntries; i++)
					{
						if (i >= startingIndex)
						{
							id = settings.get(i);
							addButton(new GuiButtonCycle((i) + EZGuiAPI.BUTTON_MIN, xStart + buttonRowBX, yStart + buttonRowBY + (buttonHeight + 5) * (i - startingIndex), buttonWidth, buttonHeight, options.getA(id), options.getB(id)), id);
						}
					}
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (maxSubPages > 0)
		{
			if (subPage < maxSubPages)
				addButton(new GuiButton(6, xStart + 67, yStart + buttonRowCY, buttonWidth - 20, buttonHeight, format(EZGuiAPI.BUTTON_LIST[7])));
			if (subPage > 0)
				addButton(new GuiButton(7, xStart + 7, yStart + buttonRowCY, buttonWidth - 20, buttonHeight, format(EZGuiAPI.BUTTON_LIST[8])));
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		int index = button.id - EZGuiAPI.BUTTON_MIN;
		if (button instanceof GuiButtonBoolean)
		{
			((GuiButtonBoolean)button).setBooleanToggle();
			switch(page)
			{
				case 0:
					if (!nbtSendCache.hasKey("client"))
						nbtSendCache.setTag("client", new NBTTagCompound());
					nbtSendCache.getCompoundTag("client").setInteger(PREFIX + settings.get(index), ((GuiButtonBoolean)button).enabled ? 1 : 0);
					break;
				case 3:
					Object[] keys = WeatherUtilConfig.dimNames.keySet().toArray();
					if (!nbtSendCache.hasKey("server"))
						nbtSendCache.setTag("server", new NBTTagCompound());
					if (!nbtSendCache.getCompoundTag("server").hasKey("dimData"))
						nbtSendCache.getCompoundTag("server").setTag("dimData", new NBTTagCompound());
					nbtSendCache.getCompoundTag("server").getCompoundTag("dimData").setInteger(((button.id - EZGuiAPI.BUTTON_MIN) % 2 == 0 ? "dimb_" + keys[(int)((button.id - EZGuiAPI.BUTTON_MIN) * 0.5F)] : "dimc_" + keys[(int)((button.id - EZGuiAPI.BUTTON_MIN - 1) * 0.5F)]), ((GuiButtonBoolean)button).enabled ? 1 : 0);
					break;
				default:
					if (!nbtSendCache.hasKey("server"))
						nbtSendCache.setTag("server", new NBTTagCompound());
					nbtSendCache.getCompoundTag("server").setInteger(PREFIX + settings.get(index), ((GuiButtonBoolean)button).enabled ? 1 : 0);
			}
		}
		else if (button instanceof GuiButtonCycle)
		{
			((GuiButtonCycle)button).cycleIndex();
			switch(page)
			{
				case 0:
					if (!nbtSendCache.hasKey("client"))
						nbtSendCache.setTag("client", new NBTTagCompound());
					nbtSendCache.getCompoundTag("client").setInteger(PREFIX + settings.get(index), ((GuiButtonCycle)button).index);
					break;
				case 3:
					Object[] keys = WeatherUtilConfig.dimNames.keySet().toArray();
					if (!nbtSendCache.hasKey("server"))
						nbtSendCache.setTag("server", new NBTTagCompound());
					if (!nbtSendCache.getCompoundTag("server").hasKey("dimData"))
						nbtSendCache.getCompoundTag("server").setTag("dimData", new NBTTagCompound());
					nbtSendCache.getCompoundTag("server").getCompoundTag("dimData").setInteger(((button.id - EZGuiAPI.BUTTON_MIN) % 2 == 0 ? "dimb_" + keys[(int)((button.id - EZGuiAPI.BUTTON_MIN) * 0.5F)] : "dimc_" + keys[(int)((button.id - EZGuiAPI.BUTTON_MIN - 1) * 0.5F)]), ((GuiButtonCycle)button).index);
					break;
				default:
					if (!nbtSendCache.hasKey("server"))
						nbtSendCache.setTag("server", new NBTTagCompound());
					nbtSendCache.getCompoundTag("server").setInteger(PREFIX + settings.get(index), ((GuiButtonCycle)button).index);
			}
		}
		
		switch(button.id)
		{
			case 0:
				mc.player.closeScreen();
				if (send)
					send();
				break;
			case 1:
				if (send)
					send();
				if (ClientProxy.clientTickHandler.op) Minecraft.getMinecraft().displayGuiScreen(new GuiConfigEditor());
				break;
			case 2: case 3: case 4: case 5:
				page = button.id == 2 ? 0 : button.id == 3 ? 1 : button.id == 4 ? 2 : 3; 
				subPage = 0;
				initGui();
				break;
			case 6:
				if (subPage < maxSubPages)
					subPage++;
				initGui();
				break;
			case 7:
				if (subPage > 0)
					subPage--;
				initGui();
				break;
			default:
				if (button.id > -1)
					send = true;
				else
					Weather2.warn("Unknown GUI button was pressed (ID:" + button.id + ")");
		}
	}
	
	public int sanitize(int val)
	{
		return sanitize(val, 0, 9999);
	}
	
	public int sanitize(int val, int min, int max)
	{
		if (val > max) val = max;
		if (val < min) val = min;
		return val;
	}
	
	@Override
	public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
	{
		float f = 0.00390625F / 2F;
		float f1 = 0.00390625F / 2F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos((double)(x + 0), (double)(y + height), (double)this.zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + height) * f1)).endVertex();
		worldrenderer.pos((double)(x + width), (double)(y + height), (double)this.zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + height) * f1)).endVertex();
		worldrenderer.pos((double)(x + width), (double)(y + 0), (double)this.zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + 0) * f1)).endVertex();
		worldrenderer.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + 0) * f1)).endVertex();
		tessellator.draw();
	}
	
	private void send()
	{
		Weather2.debug("Preparing to send packets... " + nbtSendCache);
		PacketEZGUI.isOp();
		if (nbtSendCache.hasKey("client"))
		{
			Weather2.debug("Sending config packet to client");
			nbtSendCache.getCompoundTag("client").setInteger("server", 0);
			WeatherUtilConfig.nbtReceiveClient(nbtSendCache.getCompoundTag("client"));
			nbtSendCache.removeTag("client");
		}
		if (nbtSendCache.hasKey("server") && ClientProxy.clientTickHandler.op)
		{
			Weather2.debug("Sending config packet to server");
			PacketEZGUI.apply(nbtSendCache.getCompoundTag("server"));
			nbtSendCache.removeTag("server");
		}
		send = false;
		PacketEZGUI.sync();
	}
	
	private String format(String local, Object... args)
	{
		return new TextComponentTranslation("config.ezgui." + local, args).getFormattedText();
	}
}