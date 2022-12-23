package net.mrbt0907.weather2.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.mrbt0907.weather2.Weather2;
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
	protected int xSize = 372;
	/** The Y size of the inventory window in pixels. */
	protected int ySize = 287;
	
	public ResourceLocation backgroundA = new ResourceLocation(Weather2.MODID + ":textures/gui/ez_gui_2.png");
	public ResourceLocation[] backgroundB = new ResourceLocation[0];
	public int page = 0;
	public int subPage = 0;
	public int maxSubPages = 0;
	public int maxEntries = 7;
	private boolean send = false;
	
	//Locals
	private static final String[] PAGEL = {"graphics", "system", "storms", "dimensions"};
	@SuppressWarnings("unused")
	private static final String[] FLAGL = {"flag.op","flag.reload"};
	/**0: button.on
	 * <br>1: button.off
	 * <br>2: button.both
	 * <br>3: button.none 
	 * <br>4: button.exit 
	 * <br>5: button.advanced 
	 * <br>6: button.reset 
	 * <br>7: button.next 
	 * <br>8: button.previous 
	 * <br>9: button.highest 
	 * <br>10: button.veryhigh 
	 * <br>11: button.high 
	 * <br>12: button.medium 
	 * <br>13: button.low 
	 * <br>14: button.verylow 
	 * <br>15: button.lowest 
	 * <br>16: button.highest.alt
	 * <br>17: button.lowest.alt 
	 * <br>18: button.mostcommon 
	 * <br>19: button.verycommon 
	 * <br>20: button.common 
	 * <br>21: button.normal 
	 * <br>22: button.rare 
	 * <br>23: button.veryrare 
	 * <br>24: button.realistic
	 * <br>25: graphics
	 * <br>26: system
	 * <br>27: storm
	 * <br>28: dimensions*/
	private static final String[] BUTTONL = {"button.on", "button.off", "button.both", "button.none", "button.exit", "button.advanced", "button.reset", "button.next", "button.previous", "button.highest", "button.veryhigh", "button.high", "button.medium", "button.low", "button.verylow", "button.lowest", "button.highest.alt", "button.lowest.alt", "button.mostcommon", "button.verycommon", "button.common", "button.normal", "button.rare", "button.veryrare", "button.realistic", "graphics", "system", "storms", "dimensions"};
	/**Button Toggle Switch For Weather<br>0 - Off<br>1 - On*/
	private static final List<String> BL_WTOGGLE = new ArrayList<String>(Arrays.asList("button.off.w", "button.on.w"));
	/**Button Toggle Switch For Effects<br>0 - Off<br>1 - On*/
	private static final List<String> BL_ETOGGLE = new ArrayList<String>(Arrays.asList("button.off.e", "button.on.e"));
	/**Button Toggle Switch For Effects<br>0 - Off<br>1 - On*/
	private static final List<String> BL_SHADERS = new ArrayList<String>(Arrays.asList(BUTTONL[1], "button.shader.1", "button.shader.2", "button.shader.3", "button.shader.4", "button.shader.5", "button.shader.6", "button.shader.7", "button.shader.8", "button.shader.9", "button.shader.10", "button.shader.11"));
	/**Button Toggle Switch<br>0 - Off<br>1 - On*/
	private static final List<String> BL_TOGGLE = new ArrayList<String>(Arrays.asList(BUTTONL[1], BUTTONL[0]));
	/**Button Scale<br>0 - Lowest<br>1 - Very Low<br>2 - Low<br>3 - Medium<br>4 - High<br>5 - Very High<br>6 - Highest*/
	private static final List<String> BL_STR = new ArrayList<String>(Arrays.asList(BUTTONL[17], BUTTONL[14], BUTTONL[13], BUTTONL[12], BUTTONL[11], BUTTONL[10], BUTTONL[16]));
	/**Button Scale With None<br>0 - None<br>1 - Lowest<br>2 - Very Low<br>3 - Low<br>4 - Medium<br>5 - High<br>6 - Very High<br>7 - Highest*/
	private static final List<String> BL_STR_ALT = new ArrayList<String>(Arrays.asList(BUTTONL[3], BUTTONL[15], BUTTONL[14], BUTTONL[13], BUTTONL[12], BUTTONL[11], BUTTONL[10], BUTTONL[9]));
	/**Button Frequency<br>0 - Rarest<br>1 - Very Rare<br>2 - Rare<br>3 - Normal<br>4 - Common<br>5 - Very Common<br>1 - Most Common*/
	private static final List<String> BL_RARE = new ArrayList<String>(Arrays.asList(BUTTONL[24], BUTTONL[23], BUTTONL[22], BUTTONL[21], BUTTONL[20], BUTTONL[19], BUTTONL[18]));
	/**Button Frequency With None<br>0 - None<br>1 - Rarest<br>2 - Very Rare<br>3 - Rare<br>4 - Normal<br>5 - Common<br>6 - Very Common<br>7 - Most Common*/
	@SuppressWarnings("unused")
	private static final List<String> BL_RARE_ALT = new ArrayList<String>(Arrays.asList(BUTTONL[3], BUTTONL[24], BUTTONL[23], BUTTONL[22], BUTTONL[21], BUTTONL[20], BUTTONL[19], BUTTONL[18]));
	private static final TriMapEx<Integer, List<String>, Integer> BA_List = new TriMapEx<Integer, List<String>, Integer>();
	private static final TriMapEx<Integer, List<String>, Integer> BB_List = new TriMapEx<Integer, List<String>, Integer>();
	private static final TriMapEx<Integer, List<String>, Integer> BC_List = new TriMapEx<Integer, List<String>, Integer>();
	private static final Map<Integer, String> TA_List = new HashMap<Integer, String>();
	private static final Map<Integer, String> TB_List = new HashMap<Integer, String>();
	private static final Map<Integer, String> TC_List = new HashMap<Integer, String>();
	
	
	//Other Things
	public static final String prefix = "btn_";
	
	//Main Buttons
	public static final int B_EXIT = 0;
	public static final int B_ADVANCED = 1;
	public static final int B_NEXT = 2;
	public static final int B_PREVIOUS = 3;
	public static final int B_GRAPHICS = 4;
	public static final int B_SYSTEM = 5;
	public static final int B_STORM = 6;
	public static final int B_DIMENSION = 7;
	
	//Sub Buttons
	public static final int BA_CLOUD = 8;
	public static final int BA_FUNNEL = 9;
	public static final int BA_PRECIPITATION = 10;
	public static final int BA_EFFECT = 11;
	public static final int BA_EF = 12;
	public static final int BA_RADAR = 13;
	public static final int BA_SHADER = 14;
	public static final int BA_FOLIAGE = 15;
	public static final int BB_GLOBAL = 16;
	public static final int BC_ENABLE_TORNADO = 17;
	public static final int BC_ENABLE_CYCLONE = 18;
	public static final int BC_ENABLE_SANDSTORM = 19;
	public static final int BC_FREQUENCY = 20;
	public static final int BC_GRAB_BLOCK = 21;
	public static final int BC_GRAB_ITEM = 22;
	public static final int BC_GRAB_MOB = 23;
	public static final int BC_GRAB_PLAYER = 24;
	public static final int BC_STORM_PER_PLAYER = 25;
	public static final int BD_MIN = 26;
	
	//Elements
	//public HashMap<Integer, List<GuiButton>> pages = new HashMap<Integer, List<GuiButton>>();
	public HashMap<Integer, String> buttons = new HashMap<Integer, String>();
	public NBTTagCompound nbtSendCache;
	
	//Permissions
	public boolean op = false;
	
	public GuiEZConfig()
	{
		super();
		
		op = FMLCommonHandler.instance().getMinecraftServerInstance() != null && FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer();
		//only sync request on initial gui open
		PacketEZGUI.sync();
		
		//Initialize button lists
		TA_List.put(BA_CLOUD, "cloud");
		TA_List.put(BA_FUNNEL, "funnel");
		TA_List.put(BA_PRECIPITATION, "precipitation");
		TA_List.put(BA_EFFECT, "effect");
		TA_List.put(BA_EF, "ef");
		TA_List.put(BA_RADAR, "radar");
		TA_List.put(BA_SHADER, "shader");
		TA_List.put(BA_FOLIAGE, "foliage");
		TB_List.put(BB_GLOBAL, "global");
		TC_List.put(BC_ENABLE_TORNADO, "tornado");
		TC_List.put(BC_ENABLE_CYCLONE, "cyclone");
		TC_List.put(BC_ENABLE_SANDSTORM, "sandstorm");
		TC_List.put(BC_FREQUENCY, "frequency");
		TC_List.put(BC_GRAB_BLOCK, "grab.block");
		TC_List.put(BC_GRAB_ITEM, "grab.item");
		TC_List.put(BC_GRAB_MOB, "grab.mob");
		TC_List.put(BC_GRAB_PLAYER, "grab.player");
		TC_List.put(BC_STORM_PER_PLAYER, "stormperplayer");
		
		//Initialize send cache.
		nbtSendCache = new NBTTagCompound();
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
		//a fix for container using gui opening on client side that doesnt need slot manip - might not have been needed, below was doing initGui on main gui close
		//Minecraft.getMinecraft().player.openContainer = Minecraft.getMinecraft().player.inventoryContainer;
	}
	
	public <T extends GuiButton> T addButton(T button, String local)
	{
		buttons.put(buttons.size(), local);
		return addButton(button);
	}
	
	@Override
	public <T extends GuiButton> T addButton(T button)
	{
		buttonList.add(button);
		return button;
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
		int y = (height - ySize) / 4;
		String title = format("title") + " - " + format(PAGEL[page]);
		
		drawTexturedModalRect(x, y, 0, 0, 372, 287);
		drawString(this.fontRenderer, title, (int) ((xStart + 186) - this.fontRenderer.getStringWidth(title) * 0.5), yStart+15, 16777215);
	}
	
	public void drawElements()
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int buttonRowBX = 14;
		int buttonRowBY = 87;
		int buttonHeight = 25;
		int size = buttons.size();
		
		if (page != 3)
			for (int i = 0; i < size; i++)
				drawString(this.fontRenderer, format("button." + buttons.get(i) + ".tooltip"), xStart + buttonRowBX, yStart + buttonRowBY + (buttonHeight * i), 16777215);
		else
			for (int i = 0; i < size; i++)
				
				drawString(this.fontRenderer, WordUtils.capitalize(buttons.get(i).replaceAll("\\_", " ")), xStart + buttonRowBX, yStart + buttonRowBY + (buttonHeight * i), 16777215);
		
		if (maxSubPages > 0)
			drawString(this.fontRenderer, format("misc.page", subPage + 1, maxSubPages + 1), xStart+73, yStart+270, 16777215);
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
	
	public void updateGuiElements()
	{
		
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
		int buttonWidth = 80;
		int buttonHeight = 20;
		int buttonRowAY = 48;
		int buttonRowBX = 253;
		int buttonRowBY = 82;
		int buttonRowCY = 263;
		
		xSize = 372;
		ySize = 250;
		xCenter = (int) (scaledWidth * 0.5F);
		yCenter = (int) (scaledHeight * 0.25F);
		xStart = (int) (xCenter - xSize * 0.5F);
		yStart = (int) (yCenter - ySize * 0.25F);
		
		addButton(new GuiButton(B_EXIT, xStart + 285, yStart + buttonRowCY, buttonWidth, buttonHeight, format(BUTTONL[4])));
		addButton(new GuiButton(B_ADVANCED, xStart + 180, yStart + buttonRowCY, buttonWidth + 20, buttonHeight, format(BUTTONL[5])));
		addButton(new GuiButton(B_GRAPHICS, xStart + 7, yStart + buttonRowAY, buttonWidth, buttonHeight, format(BUTTONL[25])));
		addButton(new GuiButton(B_SYSTEM, xStart + 100, yStart + buttonRowAY, buttonWidth, buttonHeight, format(BUTTONL[26])));
		addButton(new GuiButton(B_STORM, xStart + 192, yStart + buttonRowAY, buttonWidth, buttonHeight, format(BUTTONL[27])));
		addButton(new GuiButton(B_DIMENSION, xStart + 285, yStart + buttonRowAY, buttonWidth, buttonHeight, format(BUTTONL[28])));
		
		BA_List.put(BA_CLOUD, BL_STR, WeatherUtilConfig.getConfigValue(BA_CLOUD));
		BA_List.put(BA_FUNNEL, BL_STR, WeatherUtilConfig.getConfigValue(BA_FUNNEL));
		BA_List.put(BA_PRECIPITATION, BL_STR_ALT, WeatherUtilConfig.getConfigValue(BA_PRECIPITATION));
		BA_List.put(BA_EFFECT, BL_STR_ALT, WeatherUtilConfig.getConfigValue(BA_EFFECT));
		BA_List.put(BA_EF, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BA_EF));
		BA_List.put(BA_RADAR, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BA_RADAR));
		BA_List.put(BA_SHADER, BL_SHADERS, WeatherUtilConfig.getConfigValue(BA_SHADER));
		BA_List.put(BA_FOLIAGE, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BA_FOLIAGE));
		BB_List.put(BB_GLOBAL, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BB_GLOBAL));
		BC_List.put(BC_ENABLE_TORNADO, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BC_ENABLE_TORNADO));
		BC_List.put(BC_ENABLE_CYCLONE, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BC_ENABLE_CYCLONE));
		BC_List.put(BC_ENABLE_SANDSTORM, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BC_ENABLE_SANDSTORM));
		BC_List.put(BC_FREQUENCY, BL_RARE, WeatherUtilConfig.getConfigValue(BC_FREQUENCY));
		BC_List.put(BC_GRAB_BLOCK, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BC_GRAB_BLOCK));
		BC_List.put(BC_GRAB_ITEM, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BC_GRAB_ITEM));
		BC_List.put(BC_GRAB_MOB, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BC_GRAB_MOB));
		BC_List.put(BC_GRAB_PLAYER, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BC_GRAB_PLAYER));
		BC_List.put(BC_STORM_PER_PLAYER, BL_TOGGLE, WeatherUtilConfig.getConfigValue(BC_STORM_PER_PLAYER));
		
		int size = 0;
		int offsetA = 0;
		
		try
		{
			switch(page)
			{
				case 0:
					size = BA_List.size();
					maxSubPages = (size / maxEntries);
					offsetA = (maxEntries * subPage) + BA_CLOUD; 
					
					for(int i = 0; i < size - (maxEntries * subPage) && i < maxEntries; i++)
						addButton(new GuiButtonCycle(i + offsetA, xStart + buttonRowBX, yStart + buttonRowBY + (buttonHeight + 5) * i, buttonWidth, buttonHeight, BA_List.getA(i + offsetA), BA_List.getB(i + offsetA)), TA_List.get(i + offsetA));
					break;
				case 1:
					size = BB_List.size();
					maxSubPages = (size / maxEntries);
					offsetA = (maxEntries * subPage) + BB_GLOBAL; 
					
					for(int i = 0; i < size - (maxEntries * subPage) && i < maxEntries; i++)
						addButton(new GuiButtonCycle(i + offsetA, xStart + buttonRowBX, yStart + buttonRowBY + (buttonHeight + 5) * i, buttonWidth, buttonHeight, BB_List.getA(i + offsetA), BB_List.getB(i + offsetA)), TB_List.get(i + offsetA));
					break;
				case 2:
					size = BC_List.size();
					maxSubPages = (size / maxEntries);
					offsetA = (maxEntries * subPage) + BC_ENABLE_TORNADO; 
					
					for(int i = 0; i < size - (maxEntries * subPage) && i < maxEntries; i++)
						addButton(new GuiButtonCycle(i + offsetA, xStart + buttonRowBX, yStart + buttonRowBY + (buttonHeight + 5) * i, buttonWidth, buttonHeight, BC_List.getA(i + offsetA), BC_List.getB(i + offsetA)), TC_List.get(i + offsetA));
					break;
				case 3:
					size = WeatherUtilConfig.dimNames.size();
					int maxEntries = this.maxEntries * 2;
					int offsetB = (this.maxEntries * subPage);
					maxSubPages = size / (this.maxEntries);
					offsetA = (maxEntries * subPage) + BD_MIN;
					Object[] keys = WeatherUtilConfig.dimNames.keySet().toArray();
					Object[] values = WeatherUtilConfig.dimNames.values().toArray();
					int ii = 0;
					for(int i = 0; i < size - offsetB && i < this.maxEntries; i++)
					{
						addButton(new GuiButtonCycle(i + offsetA + ii, xStart + buttonRowBX - (buttonWidth + 5), yStart + buttonRowBY + (buttonHeight + 5) * i, buttonWidth, buttonHeight, BL_WTOGGLE, WeatherUtilConfig.isWeatherEnabled((int) keys[i + offsetB]) ? 1 : 0), (String) values[i + offsetB]);
						addButton(new GuiButtonCycle(i + offsetA + 1 + ii, xStart + buttonRowBX, yStart + buttonRowBY + (buttonHeight + 5) * i, buttonWidth, buttonHeight, BL_ETOGGLE, WeatherUtilConfig.isEffectsEnabled((int) keys[i + offsetB]) ? 1 : 0));
						ii++;
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
				addButton(new GuiButton(B_NEXT, xStart + 97, yStart + buttonRowCY, buttonWidth - 20, buttonHeight, format(BUTTONL[7])));
			if (subPage > 0)
				addButton(new GuiButton(B_PREVIOUS, xStart + 7, yStart + buttonRowCY, buttonWidth - 20, buttonHeight, format(BUTTONL[8])));
		}
		updateGuiElements();
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (button instanceof GuiButtonBoolean)
		{
			((GuiButtonBoolean)button).setBooleanToggle();
			if (button.id < BB_GLOBAL)
			{
				if (!nbtSendCache.hasKey("client"))
					nbtSendCache.setTag("client", new NBTTagCompound());
				nbtSendCache.getCompoundTag("client").setInteger(prefix + button.id, ((GuiButtonBoolean)button).enabled ? 1 : 0);
			}
			else if (button.id < BD_MIN)
			{
				if (!nbtSendCache.hasKey("server"))
					nbtSendCache.setTag("server", new NBTTagCompound());
				nbtSendCache.getCompoundTag("server").setInteger(prefix + button.id, ((GuiButtonBoolean)button).enabled ? 1 : 0);
			}
			else
			{
				Object[] keys = WeatherUtilConfig.dimNames.keySet().toArray();
				if (!nbtSendCache.hasKey("server"))
					nbtSendCache.setTag("server", new NBTTagCompound());
				if (!nbtSendCache.getCompoundTag("server").hasKey("dimData"))
					nbtSendCache.getCompoundTag("server").setTag("dimData", new NBTTagCompound());
				nbtSendCache.getCompoundTag("server").getCompoundTag("dimData").setInteger(((button.id - BD_MIN) % 2 == 0 ? "dimb_" + keys[(int)((button.id - BD_MIN) * 0.5F)] : "dimc_" + keys[(int)((button.id - BD_MIN - 1) * 0.5F)]), ((GuiButtonBoolean)button).enabled ? 1 : 0);
			}
		}
		else if (button instanceof GuiButtonCycle)
		{
			((GuiButtonCycle)button).cycleIndex();
			if (button.id < BB_GLOBAL)
			{
				if (!nbtSendCache.hasKey("client"))
					nbtSendCache.setTag("client", new NBTTagCompound());
				nbtSendCache.getCompoundTag("client").setInteger(prefix + button.id, ((GuiButtonCycle)button).index);
			}
			else if (button.id < BD_MIN)
			{
				if (!nbtSendCache.hasKey("server"))
					nbtSendCache.setTag("server", new NBTTagCompound());
				nbtSendCache.getCompoundTag("server").setInteger(prefix + button.id, ((GuiButtonCycle)button).index);
			}
			else
			{
				Object[] keys = WeatherUtilConfig.dimNames.keySet().toArray();
				if (!nbtSendCache.hasKey("server"))
					nbtSendCache.setTag("server", new NBTTagCompound());
				if (!nbtSendCache.getCompoundTag("server").hasKey("dimData"))
					nbtSendCache.getCompoundTag("server").setTag("dimData", new NBTTagCompound());
				nbtSendCache.getCompoundTag("server").getCompoundTag("dimData").setInteger(((button.id - BD_MIN) % 2 == 0 ? "dimb_" + keys[(int)((button.id - BD_MIN) * 0.5F)] : "dimc_" + keys[(int)((button.id - BD_MIN - 1) * 0.5F)]), ((GuiButtonCycle)button).index);
			}
		}
			
		switch(button.id)
		{
			case B_EXIT:
				mc.player.closeScreen();
				if (send)
					send();
				break;
			case B_ADVANCED:
				if (send)
					send();
				Minecraft.getMinecraft().displayGuiScreen(new GuiConfigEditor());
				break;
			case B_GRAPHICS: case B_SYSTEM: case B_STORM: case B_DIMENSION:
				page = button.id == B_GRAPHICS ? 0 : button.id == B_SYSTEM ? 1 : button.id == B_STORM ? 2 : 3; 
				subPage = 0;
				initGui();
				break;
			case B_NEXT:
				if (subPage < maxSubPages)
					subPage++;
				initGui();
				break;
			case B_PREVIOUS:
				if (subPage > 0)
					subPage--;
				initGui();
				break;
			default:
				if (button.id > -1)
				{
					send = true;
				}
				else
				{
					Weather2.warn("Unknown GUI button was pressed (ID:" + button.id + ")");
				}
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
		if (nbtSendCache.hasKey("client"))
		{
			Weather2.debug("Sending config packet to client");
			nbtSendCache.getCompoundTag("client").setInteger("server", 0);
			WeatherUtilConfig.nbtReceiveClient(nbtSendCache.getCompoundTag("client"));
			nbtSendCache.removeTag("client");
		}
		if (nbtSendCache.hasKey("server"))
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