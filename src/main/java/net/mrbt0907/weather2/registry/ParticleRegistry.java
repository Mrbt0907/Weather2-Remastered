package net.mrbt0907.weather2.registry;
import extendedrenderer.render.RotatingParticleManager;
import extendedrenderer.shader.MeshBufferManagerFoliage;
import extendedrenderer.shader.MeshBufferManagerParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.mrbt0907.weather2.Weather2;

public class ParticleRegistry
{
	public static TextureAtlasSprite cloud;
	public static TextureAtlasSprite cloud_legacy;
	public static TextureAtlasSprite cloud256;
	public static TextureAtlasSprite cloud256_light;
	public static TextureAtlasSprite cloud256_meso;
	public static TextureAtlasSprite cloud256_fire;
	public static TextureAtlasSprite cloud32;
	public static TextureAtlasSprite rainLight;
	public static TextureAtlasSprite rainHeavy;
	public static TextureAtlasSprite rainSplash;
	public static TextureAtlasSprite distant_downfall;
	public static TextureAtlasSprite tornado256;
	
	public static TextureAtlasSprite radarIconReflectivityF;
	public static TextureAtlasSprite radarIconReflectivityE;
	public static TextureAtlasSprite radarIconReflectivityD;
	public static TextureAtlasSprite radarIconReflectivityC;
	public static TextureAtlasSprite radarIconReflectivityB;
	public static TextureAtlasSprite radarIconReflectivityA;
	public static TextureAtlasSprite radarIconCloud;
	public static TextureAtlasSprite radarIconRain;
	public static TextureAtlasSprite radarIconSnow;
	public static TextureAtlasSprite radarIconLightning;
	public static TextureAtlasSprite radarIconWind;
	public static TextureAtlasSprite radarIconHail;
	public static TextureAtlasSprite radarIconTornado;
	public static TextureAtlasSprite radarIconCyclone;
	public static TextureAtlasSprite radarIconSandstorm;
	public static TextureAtlasSprite radarIconWarmFront;
	public static TextureAtlasSprite radarIconColdFront;
	public static TextureAtlasSprite radarIconOccludedFront;
	public static TextureAtlasSprite radarIconStationaryFront;
	
	public static void init(TextureStitchEvent.Pre event)
	{
		MeshBufferManagerParticle.cleanup();
		MeshBufferManagerFoliage.cleanup();

		cloud256 = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":particles/cloud256"));
		cloud256_light = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":particles/cloud256_light"));
		cloud256_meso = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":particles/cloud256_meso"));
		cloud_legacy = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":particles/cloud_legacy"));
		cloud256_fire = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":particles/cloud256_fire"));
		cloud32 = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID  + ":particles/cloud32"));
		rainLight = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID  + ":particles/rain_light"));
		rainHeavy = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID  + ":particles/rain_heavy"));
		rainSplash = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID  + ":particles/rain_splash"));
		distant_downfall = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID  + ":particles/distant_downfall"));
		tornado256 = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":particles/tornado256"));
		radarIconCloud = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_cloud"));
		radarIconRain = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_rain"));
		radarIconSnow = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_snow"));
		radarIconLightning = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_lightning"));
		radarIconWind = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_wind"));
		radarIconHail = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_hail"));
		radarIconTornado = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_tornado"));
		radarIconCyclone = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_cyclone"));
		radarIconSandstorm = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_sandstorm"));
		radarIconWarmFront = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_warm_front"));
		radarIconColdFront = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_cold_front"));
		radarIconOccludedFront = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_occluded_front"));
		radarIconStationaryFront = event.getMap().registerSprite(new ResourceLocation(Weather2.OLD_MODID + ":radar/radar_icon_stationary_front"));
	}

	public static void initPost(TextureStitchEvent.Post event)
	{
		if (RotatingParticleManager.useShaders)
			RotatingParticleManager.forceShaderReset = true;
	}
}