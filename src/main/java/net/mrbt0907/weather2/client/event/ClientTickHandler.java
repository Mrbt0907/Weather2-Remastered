package net.mrbt0907.weather2.client.event;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import extendedrenderer.ExtendedRenderer;
import extendedrenderer.render.FoliageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WindReader;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.client.gui.GuiEZConfig;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ClientConfigData;
import net.mrbt0907.weather2.config.ConfigFoliage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.network.packets.PacketData;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilSound;
import net.mrbt0907.weather2.weather.EntityRendererEX;

import org.lwjgl.input.Mouse;

public class ClientTickHandler
{
	public static World lastWorld;
	public static WeatherManagerClient weatherManager;
	public static FoliageEnhancerShader foliageEnhancer;
	public static ClientConfigData clientConfigData;

	public boolean hasOpenedConfig = false;
	public GuiButton configButton;
	//storing old reference to help retain any modifications done by other mods (dynamic surroundings asm)
	public EntityRenderer oldRenderer;
	public float smoothAngle = 0;
	public float smoothAngleRotationalVelAccel = 0;
	public float smoothAngleAdj = 0.1F;
	public int prevDir = 0;
	public boolean extraGrassLast = ConfigFoliage.enable_extra_grass;
	public boolean op = false;
	
	public ClientTickHandler()
	{
		//this constructor gets called multiple times when created from proxy, this prevents multiple inits
		new Thread(NewSceneEnhancer.instance(), "Weather2 New Scene Enhancer").start();
		
		if (foliageEnhancer == null)
		{
			foliageEnhancer = new FoliageEnhancerShader();
			(new Thread(foliageEnhancer, "Weather2 Foliage Enhancer")).start();
		}
		
		clientConfigData = new ClientConfigData();
    	op = ConfigManager.getPermissionLevel() > 3;
	}

    public void onRenderScreenTick()
    {
    	Minecraft mc = FMLClientHandler.instance().getClient();
    	if (mc.currentScreen instanceof GuiIngameMenu)
    	{
    		ScaledResolution scaledresolution = new ScaledResolution(mc);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
    		int k = Mouse.getX() * i / mc.displayWidth;
            int l = j - Mouse.getY() * j / mc.displayHeight - 1;
    		configButton = new GuiButton(0, (i/2)-100, 0, 200, 20, "Weather2 EZ Config");
    		configButton.drawButton(mc, k, l, 1F);
    		
    		if (k >= configButton.x && l >= configButton.y && k < configButton.x + 200 && l < configButton.y + 20 && Mouse.isButtonDown(0))
    			mc.displayGuiScreen(new GuiEZConfig());
    	}
    }

    public void onTickInGUI(GuiScreen guiscreen)
    {
        
    }
    
    public void onTickInGame()
    {

		if (ConfigMisc.toaster_pc_mode) return;
		
        Minecraft mc = FMLClientHandler.instance().getClient();
        World world = mc.world;
        if (ConfigMisc.proxy_render_override)
        {
        	if (!(mc.entityRenderer instanceof EntityRendererEX))
        	{
				oldRenderer = mc.entityRenderer;
        		EntityRendererEX temp = new EntityRendererEX(mc, mc.getResourceManager());
		        mc.entityRenderer = temp;
        	}
    	}
        else
        {
    		if ((mc.entityRenderer instanceof EntityRendererEX))
    		{
    			if (oldRenderer != null)
    				mc.entityRenderer = oldRenderer;
    			else
					mc.entityRenderer = new EntityRenderer(mc, mc.getResourceManager());
    		}
    	}

		if (world != null)
		{
			checkClientWeather();
			weatherManager.tick();
			Weather2.clientChunkUtil.tick();
			
			if (!ConfigMisc.aesthetic_mode && ConfigMisc.enable_forced_clouds_off && world.provider.getDimension() == 0)
				mc.gameSettings.clouds = 0;
			
			if (EZConfigParser.isEffectsEnabled(world.provider.getDimension()))
				NewSceneEnhancer.instance().tick();
			
			if (!EZConfigParser.isWeatherEnabled(world.provider.getDimension()) && weatherManager.getFronts().size() > 1)
			{
				Weather2.debug("Removing all storms as the dimension weather is disabled");
				weatherManager.reset(false);
			}
			
			//TODO: evaluate if best here
			
			Vec3 pos = mc.player == null ? null : new Vec3(mc.player.getPosition());
			float windDir = WindReader.getWindAngle(world, pos);
			float windSpeed = WindReader.getWindSpeed(world, pos) * 0.25F;

			float diff = Math.abs(windDir - smoothAngle)/* - 180*/;

			if (diff > 10)
			{

				if (smoothAngle > 180) smoothAngle -= 360;
				if (smoothAngle < -180) smoothAngle += 360;

				float bestMove = Maths.wrapDegrees(windDir - smoothAngle);

				smoothAngleAdj = windSpeed;//0.2F;

				if (Math.abs(bestMove) < 180/* - (angleAdjust * 2)*/) {
					float realAdj = smoothAngleAdj;//Math.max(smoothAngleAdj, Math.abs(bestMove));

					if (realAdj * 2 > windSpeed) {
						if (bestMove > 0) {
							smoothAngleRotationalVelAccel -= realAdj;
							if (prevDir < 0) {
								smoothAngleRotationalVelAccel = 0;
							}
							prevDir = 1;
						} else if (bestMove < 0) {
							smoothAngleRotationalVelAccel += realAdj;
							if (prevDir > 0) {
								smoothAngleRotationalVelAccel = 0;
							}
							prevDir = -1;
						}
					}

					if (smoothAngleRotationalVelAccel > 0.3 || smoothAngleRotationalVelAccel < -0.3) {
						smoothAngle += smoothAngleRotationalVelAccel * 0.3F;
					} else {
					}

					smoothAngleRotationalVelAccel *= 0.80F;
				}
			}
			if (!Minecraft.getMinecraft().isGamePaused()) {

				ExtendedRenderer.foliageRenderer.windDir = smoothAngle;

				float rate = 0.005F;

				if (ExtendedRenderer.foliageRenderer.windSpeedSmooth != windSpeed) {
					if (ExtendedRenderer.foliageRenderer.windSpeedSmooth < windSpeed) {
						if (ExtendedRenderer.foliageRenderer.windSpeedSmooth + rate > windSpeed) {
							ExtendedRenderer.foliageRenderer.windSpeedSmooth = windSpeed;
						} else {
							ExtendedRenderer.foliageRenderer.windSpeedSmooth += rate;
						}
					} else {
						if (ExtendedRenderer.foliageRenderer.windSpeedSmooth - rate < windSpeed) {
							ExtendedRenderer.foliageRenderer.windSpeedSmooth = windSpeed;
						} else {
							ExtendedRenderer.foliageRenderer.windSpeedSmooth -= rate;
						}
					}
				}

				float baseTimeChangeRate = 60F;


				FoliageRenderer.windTime += 0 + (baseTimeChangeRate * ExtendedRenderer.foliageRenderer.windSpeedSmooth);
			}
		}
		else
			resetClientWeather();
    }

    public static void resetClientWeather() {
		if (weatherManager != null) {
			Weather2.debug("Weather2: Detected old WeatherManagerClient with unloaded world, clearing its data");
			WeatherUtilSound.reset();
			Weather2.clientChunkUtil.clearCache();
			weatherManager.reset(true);
			weatherManager = null;
		}
	}
	
    public static void checkClientWeather()
    {
    	try
    	{
			World world = FMLClientHandler.instance().getClient().world;
    		if (weatherManager == null || world != lastWorld)
    			init(world);
    	} 
    	catch (Exception ex)
    	{
    		Weather2.debug("Weather2: Warning, client received packet before it was ready to use, and failed to init client weather due to null world");
    	}
    }
    
    public static void init(World world)
    {
		//this is generally triggered when they teleport to another dimension
		if (weatherManager != null)
		{
			Weather2.debug("Weather2: Detected old WeatherManagerClient with active world, clearing its data");
			weatherManager.reset(true);
		}

		Weather2.debug("Weather2: Initializing WeatherManagerClient for client world and requesting full sync");

    	lastWorld = world;
    	weatherManager = new WeatherManagerClient(world);
		//request a full sync from server
    	PacketData.sync();
    }

    static void getField(Field field, Object newValue) throws Exception
    {
        field.setAccessible(true);
        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}