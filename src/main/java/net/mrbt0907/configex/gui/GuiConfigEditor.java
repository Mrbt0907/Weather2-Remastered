package net.mrbt0907.configex.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.network.NetworkHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiConfigEditor extends GuiScreen
{
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(ConfigModEX.MODID, "textures/gui/advanced_gui.png");
	public static final ResourceLocation GUI_BORDER_TEXTURE = new ResourceLocation(ConfigModEX.MODID, "textures/gui/advanced_gui_border.png");
	public static int G_RESET = 0;
	public static int G_SAVE = 1;
	public static int G_MODNEXT = 2;
	public static int G_MODPREV = 3;
	public static int G_CONFIGMODE = 4;
	public static int G_CLOSE = 9;
	public static int curIndex = 0;
	public static boolean serverMode = false;
	
	public GuiConfigScrollPanel scrollPane;
	public int xCenter;
	public int yCenter;
	public int xStart;
	public int yStart;
	public int xSize = 320;
	public int ySize = 246;
	public boolean changed;
	
	public GuiConfigEditor()
	{
		super();
		mc = Minecraft.getMinecraft();
		scrollPane = new GuiConfigScrollPanel(this, mc, xStart + 460, yStart + 36, yStart + ySize - 33, 20);
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
	}

	@Override
	public void drawScreen(int var1, int var2, float var3)
	{
		try
		{
			drawBackgroundLayer();
			scrollPane.drawScreen(var1, var2, var3);
			//drawForegroundLayer();
		}
		catch (Exception ex) {}
		
		super.drawScreen(var1, var2, var3);
	}
	
	protected void drawForegroundLayer()
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(GUI_BORDER_TEXTURE);
		drawTexturedModalRect(xStart, yStart, 0, 0, 512, 512);

		String title = TextFormatting.BOLD + "" + scrollPane.configs.get(curIndex).name;
		String subtitle = format("title." + (serverMode ? "server" : "client"));
		drawString(fontRenderer, subtitle, xStart + 160 - fontRenderer.getStringWidth(subtitle) / 2, yStart + 23, 16777215);
		drawString(fontRenderer, title, xStart + 160 - fontRenderer.getStringWidth(title) / 2, yStart + 10, 16777215);
		drawString(fontRenderer, (curIndex + 1) + "/" + scrollPane.configs.size(), xStart + 40, yStart + 226, 16777215);
	}

	protected void drawBackgroundLayer()
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawWorldBackground(0);
		mc.getTextureManager().bindTexture(GUI_TEXTURE);
		drawTexturedModalRect(xStart, yStart, 0, 0, 512, 512);
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return true;
	}

	@Override
	public void initGui()
	{
		ScaledResolution sr = new ScaledResolution(mc);
		int scaledWidth = sr.getScaledWidth();
		int scaledHeight = sr.getScaledHeight();
		int buttonWidth = 30;
		int buttonHeight = 20;
		int buttonBottomY = 220;
		xCenter = (int) (scaledWidth * 0.5F);
		yCenter = (int) (scaledHeight * 0.5F);
		xStart = (int) (xCenter - xSize * 0.5F);
		yStart = (int) (yCenter - ySize * 0.5F);
		buttonList.clear();
		buttonList.add(new GuiButton(G_MODNEXT, xStart + 74, yStart + buttonBottomY, buttonWidth, buttonHeight, format("next")));
		buttonList.add(new GuiButton(G_MODPREV, xStart + 7, yStart + buttonBottomY, buttonWidth, buttonHeight, format("previous")));
		buttonList.add(new GuiButton(G_SAVE, xStart + 252, yStart + buttonBottomY, buttonWidth + 32, buttonHeight, format("exit")));
		
		if (mc.isSingleplayer())
			serverMode = false;
		else
			buttonList.add(new GuiButton(G_CONFIGMODE, xStart + 162, yStart + buttonBottomY, buttonWidth + 56, buttonHeight, serverMode ? format("clientmode") : format("servermode")));
	}
	
	@Override
	protected void keyTyped(char par1, int par2)
	{
		try
		{
			if (scrollPane.keyTyped(par1, par2))
			{
				if (par2 == 1)
				{
					if (mc.player != null)
						mc.player.sendMessage(new TextComponentTranslation("config.gui." + (changed ? "save" : "nosave")));
					this.mc.displayGuiScreen(null);
					if (mc.currentScreen == null)
						mc.setIngameFocus();
				}
			}
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
			scrollPane.mouseClicked(par1, par2, par3);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void actionPerformed(GuiButton var1)
	{
		checkForUpdates();
		if (var1.id == G_RESET)
		{

			checkForUpdates();
		}
		else if (var1.id == G_MODPREV)
		{
			curIndex--;
			if (curIndex < 0)
				curIndex = scrollPane.configs.size() - 1;
			scrollPane.populateData();
		}
		else if (var1.id == G_MODNEXT)
		{
			curIndex++;
			if (curIndex >= scrollPane.configs.size())
				curIndex = 0;
			scrollPane.populateData();
		}
		else if (var1.id == G_CONFIGMODE)
		{
			serverMode = !serverMode;
			scrollPane.populateData();
		}
		else if (var1.id == G_SAVE) 
		{
			if (mc.player != null && changed)
				mc.player.sendMessage(new TextComponentTranslation("config.gui.save"));
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
		initGui();
	}
	
	public void checkForUpdates()
	{
		Map<String, NBTTagCompound> tags = new HashMap<String, NBTTagCompound>();
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagCompound nbtManager = new NBTTagCompound();
		NBTTagCompound nbtField;
		for(GuiConfigEntry option : scrollPane.options)
		{
			if (!tags.containsKey(option.categoryName))
				tags.put(option.categoryName, new NBTTagCompound());
			
			if (option.textField.hasChanged)
			{
				changed = true;
				nbtField = new NBTTagCompound();
				nbtField.setString("value", option.textField.text);
				tags.get(option.categoryName).setTag(option.registryName, nbtField);
			}
		}
		
		for (Entry<String, NBTTagCompound> tag : tags.entrySet())
			if (!tag.getValue().isEmpty())
				nbtManager.setTag(tag.getKey(), tag.getValue());
		
		nbt.setTag("manager", nbtManager);
		
		if (!nbtManager.isEmpty())
		{
			if (serverMode)
			{
				ConfigModEX.debug("Sending advanced config data to server...");
				NetworkHandler.sendServerPacket(0, nbt);
			}
			else
			{
				ConfigModEX.debug("Applying client changes...");
				nbt.setBoolean("setClient", true);
				ConfigManager.readNBT(nbt);
			}
		}
	}
	
	@Override
	public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
	{
		float f = 0.00390625F / 2F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		vertexbuffer.pos((double)(x + 0), (double)(y + height), (double)zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + height) * f)).endVertex();
		vertexbuffer.pos((double)(x + width), (double)(y + height), (double)zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + height) * f)).endVertex();
		vertexbuffer.pos((double)(x + width), (double)(y + 0), (double)zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + 0) * f)).endVertex();
		vertexbuffer.pos((double)(x + 0), (double)(y + 0), (double)zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + 0) * f)).endVertex();
		tessellator.draw();
	}
	
	private String format(String local, Object... args)
	{
		return I18n.format("config.gui." + local, args);
	}
}
