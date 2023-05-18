package net.mrbt0907.weather2.event;

import extendedrenderer.render.FoliageRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.api.WeatherUtilData;
import net.mrbt0907.weather2.api.event.EventRegisterGrabLists;
import net.mrbt0907.weather2.api.event.EventRegisterParticleRenderer;
import net.mrbt0907.weather2.client.SceneEnhancer;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.client.weather.tornado.LegacyStormRenderer;
import net.mrbt0907.weather2.config.ConfigFoliage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigParticle;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.entity.AI.EntityAIMoveIndoorsStorm;
import net.mrbt0907.weather2.registry.ParticleRegistry;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.util.ReflectionHelper;
import net.mrbt0907.weather2.util.UtilEntityBuffsMini;
import net.mrbt0907.weather2.weather.WindManager;

public class EventHandlerForge
{

	@SubscribeEvent
	public void onParticleRendererRegister(EventRegisterParticleRenderer event)
	{
		event.register(new ResourceLocation(Weather2.MODID, "legacy"), LegacyStormRenderer.class);
	}
	
	@SubscribeEvent
	public void onGrabListRefresh(EventRegisterGrabLists event)
	{
		event.windResistanceList.add("minecraft:glass", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:grass_path", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:farmland", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:stained_glass", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:glass_pane", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:stained_glass_pane", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:leaves", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:leaves2", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:sapling", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:web", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:tallgrass", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:deadbush", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:yellow_flower", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:red_flower", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:double_plant", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:brown_mushroom", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:red_mushroom", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:brown_mushroom_block", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:red_mushroom_block", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:melon_block", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:pumpkin", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:skull", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:flower_pot", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:torch", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:cactus", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:vine", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:waterlily", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:chorus_plant", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:chorus_flower", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:hay_block", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:crop", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:string", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:redstone", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:repeater", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:comparator", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:mycelium", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:grass", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:gravel", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:sand", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:soul_sand", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:wool", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:carpet", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:netherrack", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:ladder", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:planks", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:oak_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:birch_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:spruce_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:dark_oak_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:jungle_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:acacia_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:wooden_slab", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:oak_stairs", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:birch_stairs", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:dark_oak_stairs", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:jungle_stairs", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:acacia_stairs", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:crafting_table", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:bookshelf", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:dirt", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:piston", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:sticky_piston", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:glowstone", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:redstone_lamp", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:observer", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:daylight_sensor", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:redstone_block", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:wooden_door", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:birch_door", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:spruce_door", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:acacia_door", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:jungle_door", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:dark_oak_door", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:lapis_block", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:clay", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("weather2:tornado_sensor", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:tornado_siren", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:tornado_siren_manual", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:wind_vane", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:weather_forecast", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:weather_machine", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("weather2:weather_deflector", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("weather2:anemometer", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:stained_hardened_clay", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:white_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:orange_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:magenta_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:light_blue_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:yellow_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:lime_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:pink_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:gray_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:silver_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:cyan_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:purple_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:blue_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:brown_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:green_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:red_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:black_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:red_sandstone_stairs", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:log", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:log2", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:sandstone_stairs", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:stone_stairs", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:cobblestone", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:cobblestone_wall", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:monster_egg", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:trapdoor", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:concrete_powder", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:quartz_stairs", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:quartz_block", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:stone_slab2", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:stone_slab", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:concrete", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:iron_door", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:iron_trapdoor", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:brick_block", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:brick_stairs", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:stonebrick", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:stone_brick_stairs", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:iron_block", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:diamond_block", WeatherAPI.getEFWindSpeed(7));
		event.windResistanceList.add("minecraft:gold_block", WeatherAPI.getEFWindSpeed(6));
		event.windResistanceList.add("minecraft:stone", WeatherAPI.getEFWindSpeed(6));
		
		event.grabList.add("minecraft:glass");
		event.grabList.add("minecraft:stained_glass");
		event.grabList.add("minecraft:glass_pane");
		event.grabList.add("minecraft:stained_glass_pane");
		event.grabList.add("minecraft:leaves");
		event.grabList.add("minecraft:leaves2");
		event.grabList.add("minecraft:web");
		event.grabList.add("minecraft:flower_pot");
		event.grabList.add("minecraft:hay_block");
		event.grabList.add("minecraft:string");
		event.grabList.add("minecraft:redstone");
		event.grabList.add("minecraft:repeater");
		event.grabList.add("minecraft:comparator");
		event.grabList.add("minecraft:mycelium");
		event.grabList.add("minecraft:wool");
		event.grabList.add("minecraft:carpet");
		event.grabList.add("minecraft:ladder");
		event.grabList.add("minecraft:planks");
		event.grabList.add("minecraft:oak_fence");
		event.grabList.add("minecraft:birch_fence");
		event.grabList.add("minecraft:spruce_fence");
		event.grabList.add("minecraft:dark_oak_fence");
		event.grabList.add("minecraft:jungle_fence");
		event.grabList.add("minecraft:acacia_fence");
		event.grabList.add("minecraft:wooden_slab");
		event.grabList.add("minecraft:oak_stairs");
		event.grabList.add("minecraft:birch_stairs");
		event.grabList.add("minecraft:dark_oak_stairs");
		event.grabList.add("minecraft:jungle_stairs");
		event.grabList.add("minecraft:acacia_stairs");
		event.grabList.add("minecraft:crafting_table");
		event.grabList.add("minecraft:bookshelf");
		event.grabList.add("minecraft:piston");
		event.grabList.add("minecraft:sticky_piston");
		event.grabList.add("minecraft:observer");
		event.grabList.add("minecraft:daylight_sensor");
		event.grabList.add("minecraft:redstone_block");
		event.grabList.add("minecraft:wooden_door");
		event.grabList.add("minecraft:birch_door");
		event.grabList.add("minecraft:spruce_door");
		event.grabList.add("minecraft:acacia_door");
		event.grabList.add("minecraft:jungle_door");
		event.grabList.add("minecraft:dark_oak_door");
		event.grabList.add("minecraft:lapis_block");
		event.grabList.add("minecraft:clay");
		event.grabList.add("weather2:tornado_sensor");
		event.grabList.add("weather2:tornado_siren");
		event.grabList.add("weather2:tornado_siren_manual");
		event.grabList.add("weather2:wind_vane");
		event.grabList.add("weather2:weather_forecast");
		event.grabList.add("weather2:weather_machine");
		event.grabList.add("weather2:weather_deflector");
		event.grabList.add("weather2:anemometer");
		event.grabList.add("minecraft:white_glazed_terracotta");
		event.grabList.add("minecraft:orange_glazed_terracotta");
		event.grabList.add("minecraft:magenta_glazed_terracotta");
		event.grabList.add("minecraft:light_blue_glazed_terracotta");
		event.grabList.add("minecraft:yellow_glazed_terracotta");
		event.grabList.add("minecraft:lime_glazed_terracotta");
		event.grabList.add("minecraft:pink_glazed_terracotta");
		event.grabList.add("minecraft:gray_glazed_terracotta");
		event.grabList.add("minecraft:silver_glazed_terracotta");
		event.grabList.add("minecraft:cyan_glazed_terracotta");
		event.grabList.add("minecraft:purple_glazed_terracotta");
		event.grabList.add("minecraft:blue_glazed_terracotta");
		event.grabList.add("minecraft:brown_glazed_terracotta");
		event.grabList.add("minecraft:green_glazed_terracotta");
		event.grabList.add("minecraft:red_glazed_terracotta");
		event.grabList.add("minecraft:black_glazed_terracotta");
		event.grabList.add("minecraft:red_sandstone_stairs");
		event.grabList.add("minecraft:log");
		event.grabList.add("minecraft:log2");
		event.grabList.add("minecraft:sandstone_stairs");
		event.grabList.add("minecraft:stone_stairs");
		event.grabList.add("minecraft:cobblestone");
		event.grabList.add("minecraft:cobblestone_wall");
		event.grabList.add("minecraft:monster_egg");
		event.grabList.add("minecraft:trapdoor");
		event.grabList.add("minecraft:concrete_powder");
		event.grabList.add("minecraft:quartz_stairs");
		event.grabList.add("minecraft:quartz_block");
		event.grabList.add("minecraft:stone_slab2");
		event.grabList.add("minecraft:stone_slab");
		event.grabList.add("minecraft:concrete");
		event.grabList.add("minecraft:iron_door");
		event.grabList.add("minecraft:iron_trapdoor");
		event.grabList.add("minecraft:brick_block");
		event.grabList.add("minecraft:brick_stairs");
		event.grabList.add("minecraft:stonebrick");
		event.grabList.add("minecraft:stone_brick_stairs");
		event.grabList.add("minecraft:iron_block");
		event.grabList.add("minecraft:diamond_block");
		event.grabList.add("minecraft:gold_block");
		
		event.replaceList.add("minecraft:farmland", "minecraft:dirt");
		event.grabList.add("minecraft:grass_path", "minecraft:dirt");
		event.replaceList.add("minecraft:torch", "minecraft:air");
		event.replaceList.add("minecraft:redstone_torch", "minecraft:air");
		event.replaceList.add("minecraft:glowstone", "minecraft:air");
		event.replaceList.add("minecraft:redstone_lamp", "minecraft:air");
	}
	
	@SubscribeEvent
	public void worldSave(Save event) {
		Weather2.writeOutData(false);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {

		if (ConfigMisc.toaster_pc_mode) return;

		ClientTickHandler.checkClientWeather();
		ClientTickHandler.weatherManager.tickRender(event.getPartialTicks());
		SceneEnhancer.renderWorldLast(event);

		FoliageRenderer.radialRange = ConfigFoliage.shader_range;
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent.Pre event) {
		
		//optifine breaks (removes) forge added method setTextureEntry, dont use it
		ParticleRegistry.init(event);
		
		
	}

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerIconsPost(TextureStitchEvent.Post event)
    {
        ParticleRegistry.initPost(event);
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void onFogColors(FogColors event)
	{
		if (ConfigMisc.toaster_pc_mode) return;
		
        if (SceneEnhancer.isFogOverridding())
        {
			//backup original fog colors that are actively being adjusted based on time of day
			SceneEnhancer.fogRedOrig = event.getRed();
			SceneEnhancer.fogGreenOrig = event.getGreen();
			SceneEnhancer.fogBlueOrig = event.getBlue();
        	event.setRed(SceneEnhancer.fogRed);
        	event.setGreen(SceneEnhancer.fogGreen);
        	event.setBlue(SceneEnhancer.fogBlue);
			GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
        }
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onFogRender(RenderFogEvent event)
	{
		if (ConfigMisc.toaster_pc_mode) return;

		if (SceneEnhancer.isFogOverridding())
		{
			try
			{
				event.getRenderer().farPlaneDistance = (float) ConfigParticle.render_distance;
			}
			catch (Exception e)
			{
				if ((float) ReflectionHelper.get(EntityRenderer.class, event.getRenderer(), "farPlaneDistance", "field_78530_s") != ConfigParticle.render_distance);
					ReflectionHelper.set(EntityRenderer.class, event.getRenderer(), (float)ConfigParticle.render_distance, "farPlaneDistance", "field_78530_s");
			}
        }
		
		try
		{
			SceneEnhancer.fogDistance = event.getRenderer().farPlaneDistance;
		}
		catch (Exception e)
		{
			SceneEnhancer.fogDistance = (float) ReflectionHelper.get(EntityRenderer.class, event.getRenderer(), "farPlaneDistance", "field_78530_s");
		}

		
		//TODO: make use of this, density only works with EXP or EXP 2 mode
		GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
		GlStateManager.setFogStart(SceneEnhancer.fogStart);
        GlStateManager.setFogEnd(SceneEnhancer.fogEnd);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		SceneEnhancer.renderTick(event);
	}

	@SubscribeEvent
	public void onEntityCreatedOrLoaded(EntityJoinWorldEvent event)
	{
		if (event.getEntity().world.isRemote) return;

		if (ConfigStorm.enable_villagers_take_cover)
		{
			if (event.getEntity() instanceof EntityVillager)
			{
				EntityVillager ent = (EntityVillager) event.getEntity();
				UtilEntityBuffsMini.replaceTaskIfMissing(ent, EntityAIMoveIndoors.class, EntityAIMoveIndoorsStorm.class, 2);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent.Post event)
	{
		FoliageEnhancerShader.setupReplacers();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void modelBake(ModelBakeEvent event)
	{
		FoliageEnhancerShader.modelBakeEvent(event);
	}

	@SubscribeEvent
	public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event)
	{
		Entity ent = event.getEntity();
		if (!ent.world.isRemote)
		{
			if (WeatherUtilData.isWindAffected(ent))
			{
				WindManager windMan = ServerTickHandler.getWeatherSystemForDim(ent.world.provider.getDimension()).windManager;
				windMan.getEntityWindVectors(ent, 1F / 20F, 0.5F);
			}
		}
	}
}
