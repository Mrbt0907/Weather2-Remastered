package net.mrbt0907.weather2;

import extendedrenderer.shader.IShaderListener;
import extendedrenderer.shader.ShaderListenerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.block.TileAnemometer;
import net.mrbt0907.weather2.block.TileSiren;
import net.mrbt0907.weather2.block.TileWeatherDeflector;
import net.mrbt0907.weather2.block.TileRadar;
import net.mrbt0907.weather2.block.TileWeatherConstructor;
import net.mrbt0907.weather2.block.TileWindVane;
import net.mrbt0907.weather2.client.block.RenderAnemometer;
import net.mrbt0907.weather2.client.block.RenderSiren;
import net.mrbt0907.weather2.client.block.RenderWeatherDeflector;
import net.mrbt0907.weather2.client.block.RenderRadar;
import net.mrbt0907.weather2.client.block.RenderWeatherConstructor;
import net.mrbt0907.weather2.client.block.RenderWindVane;
import net.mrbt0907.weather2.client.entity.RenderFlyingBlock;
import net.mrbt0907.weather2.client.entity.RenderLightningBolt;
import net.mrbt0907.weather2.client.entity.RenderLightningBoltCustom;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.entity.EntityIceBall;
import net.mrbt0907.weather2.entity.EntityLightningBolt;
import net.mrbt0907.weather2.entity.EntityLightningBoltCustom;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.util.WeatherUtilSound;
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

	public static TextureAtlasSprite radarIconRain;
	public static TextureAtlasSprite radarIconLightning;
	public static TextureAtlasSprite radarIconWind;
	public static TextureAtlasSprite radarIconHail;
	public static TextureAtlasSprite radarIconTornado;
	public static TextureAtlasSprite radarIconCyclone;
	public static TextureAtlasSprite radarIconSandstorm;
	public static ClientTickHandler clientTickHandler;
	
	public ClientProxy()
	{
			clientTickHandler = new ClientTickHandler();
	}

	@Override
	public void init()
	{
		super.init();
		WeatherUtilSound.init();
		
		addMapping(EntityIceBall.class, new RenderFlyingBlock(Minecraft.getMinecraft().getRenderManager(), Blocks.ICE));
		addMapping(EntityMovingBlock.class, new RenderFlyingBlock(Minecraft.getMinecraft().getRenderManager(), null));
		addMapping(EntityLightningBolt.class, new RenderLightningBolt(Minecraft.getMinecraft().getRenderManager()));
		addMapping(EntityLightningBoltCustom.class, new RenderLightningBoltCustom(Minecraft.getMinecraft().getRenderManager()));
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileSiren.class, new RenderSiren());
		ClientRegistry.bindTileEntitySpecialRenderer(TileWindVane.class, new RenderWindVane());
		ClientRegistry.bindTileEntitySpecialRenderer(TileRadar.class, new RenderRadar());
		ClientRegistry.bindTileEntitySpecialRenderer(TileWeatherConstructor.class, new RenderWeatherConstructor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileWeatherDeflector.class, new RenderWeatherDeflector());
		ClientRegistry.bindTileEntitySpecialRenderer(TileAnemometer.class, new RenderAnemometer());
	}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {

	}
	
	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	private static void addMapping(Class<? extends Entity> entityClass, Render render) {
		RenderingRegistry.registerEntityRenderingHandler(entityClass, render);
	}
	
	@Override
	public void preInit()
	{
		super.preInit();
		ShaderListenerRegistry.addListener(new IShaderListener()
		{
			@Override
			public void init() {
				FoliageEnhancerShader.shadersInit();
			}

			@Override
			public void reset() {
				FoliageEnhancerShader.shadersReset();
			}
		});
	}
	
	@Override
	public void postInit()
	{
		super.postInit();
	}
}
