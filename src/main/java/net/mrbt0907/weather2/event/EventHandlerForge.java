package net.mrbt0907.weather2.event;

import extendedrenderer.render.FoliageRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
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
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.client.rendering.April24StormRenderer;
import net.mrbt0907.weather2.client.rendering.LegacyStormRenderer;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigFoliage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.entity.AI.EntityAITakeCover;
import net.mrbt0907.weather2.registry.ParticleRegistry;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.WindManager;

public class EventHandlerForge
{
	@SubscribeEvent
	public void onParticleRendererRegister(EventRegisterParticleRenderer event)
	{
		event.register(new ResourceLocation(Weather2.MODID, "legacy"), LegacyStormRenderer.class);
		event.register(new ResourceLocation(Weather2.MODID, "april24"), April24StormRenderer.class);
	}
	
	@SubscribeEvent
	public void onGrabListRefresh(EventRegisterGrabLists event)
	{
		event.windResistanceList.add("minecraft:acacia_door", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:acacia_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:acacia_fence_gate", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:acacia_stairs", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:activator_rail", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:anvil", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:banner", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:beacon", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:bed", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:birch_door", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:birch_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:birch_fence_gate", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:birch_stairs", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:black_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:black_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:blue_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:blue_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:bone_block", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:bookshelf", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:brewing_stand", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:brick_block", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:brick_stairs", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:brown_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:brown_mushroom", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:brown_mushroom_block", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:brown_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:cactus", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:cake", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:carpet", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:cauldron", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:chest", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:chorus_flower", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:chorus_plant", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:clay", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:coal_block", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:coal_ore", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:cobblestone", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:cobblestone_wall", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:comparator", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:concrete", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:concrete_powder", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:crafting_table", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:cyan_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:cyan_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:dark_oak_door", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:dark_oak_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:dark_oak_fence_gate", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:dark_oak_stairs", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:daylight_detector", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:deadbush", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:detector_rail", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:diamond_block", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:diamond_ore", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:dirt", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:dispenser", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:double_plant", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:dragon_egg", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:dropper", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:emerald_block", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:emerald_ore", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:enchanting_table", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:end_bricks", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:end_stone", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:ender_chest", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:farmland", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:fence_gate", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:flower_pot", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:furnace", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:glass", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:glass_pane", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:glowstone", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:gold_block", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:gold_ore", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:golden_rail", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:grass", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:grass_path", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:gravel", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:gray_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:gray_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:green_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:green_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:hardened_clay", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:hay_block", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:heavy_weighted_pressure_plate", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:hopper", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:ice", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:iron_bars", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:iron_block", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:iron_door", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:iron_ore", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:iron_trapdoor", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:jukebox", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:jungle_door", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:jungle_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:jungle_fence_gate", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:jungle_stairs", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:ladder", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:lapis_block", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:lapis_ore", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:leaves", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:leaves2", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:lever", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:light_blue_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:light_blue_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:light_weighted_pressure_plate", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:lime_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:lime_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:lit_pumpkin", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:log", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:log2", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:magenta_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:magenta_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:magma", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:melon_block", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:mossy_cobblestone", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:mycelium", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:nether_brick", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:nether_brick_fence", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:nether_brick_stairs", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:nether_wart_block", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:netherrack", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:noteblock", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:oak_stairs", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:observer", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:obsidian", WeatherAPI.getEFWindSpeed(6));
		event.windResistanceList.add("minecraft:orange_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:orange_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:packed_ice", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:pink_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:pink_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:piston", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:planks", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:prismarine", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:pumpkin", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:purple_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:purple_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:purpur_block", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:purpur_pillar", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:purpur_slab", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:purpur_stairs", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:quartz_block", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:quartz_ore", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("minecraft:quartz_stairs", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:rail", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:red_flower", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:red_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:red_mushroom", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:red_mushroom_block", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:red_nether_brick", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:red_sandstone", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:red_sandstone_stairs", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:red_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:redstone", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:redstone_block", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:redstone_lamp", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:redstone_ore", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("minecraft:redstone_torch", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:reeds", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:repeater", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:sand", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:sandstone", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:sandstone_stairs", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:sapling", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:sea_lantern", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:sign", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:silver_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:silver_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:skull", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:slime", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:snow", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:snow_layer", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:soul_sand", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:sponge", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:spruce_door", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:spruce_fence", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:spruce_fence_gate", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:spruce_stairs", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:stained_glass", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:stained_hardened_clay", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:sticky_piston", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:stone", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:stone_brick_stairs", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:stone_button", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:stone_pressure_plate", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:stone_slab", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:stone_slab2", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:stone_stairs", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:stonebrick", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:string", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:tallgrass", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:tnt", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:torch", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:trapdoor", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:trapped_chest", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:tripwire_hook", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:vine", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:waterlily", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:web", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:white_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:white_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:wooden_button", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:wooden_door", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:wooden_pressure_plate", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:wooden_slab", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("minecraft:wool", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("minecraft:yellow_flower", WeatherAPI.getEFWindSpeed(0));
		event.windResistanceList.add("minecraft:yellow_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("minecraft:yellow_shulker_box", WeatherAPI.getEFWindSpeed(1));
		event.windResistanceList.add("weather2:anemometer", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:barometer_sensor", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:humidity_sensor", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:machine_case", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:radio_transmitter", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:rain_sensor", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:storm_sensor", WeatherAPI.getEFWindSpeed(4));
		event.windResistanceList.add("weather2:temperature_sensor", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:tornado_sensor", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:tornado_siren", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:tornado_siren_manual", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:weather_deflector", WeatherAPI.getEFWindSpeed(5));
		event.windResistanceList.add("weather2:weather_forecast", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("weather2:weather_forecast_2", WeatherAPI.getEFWindSpeed(2));
		event.windResistanceList.add("weather2:weather_forecast_3", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:weather_machine", WeatherAPI.getEFWindSpeed(6));
		event.windResistanceList.add("weather2:wind_sensor", WeatherAPI.getEFWindSpeed(3));
		event.windResistanceList.add("weather2:wind_vane", WeatherAPI.getEFWindSpeed(3));
		
		event.grabList.add("minecraft:acacia_door");
		event.grabList.add("minecraft:acacia_fence");
		event.grabList.add("minecraft:acacia_fence_gate");
		event.grabList.add("minecraft:acacia_stairs");
		event.grabList.add("minecraft:activator_rail");
		event.grabList.add("minecraft:anvil");
		event.grabList.add("minecraft:banner");
		event.grabList.add("minecraft:beacon");
		event.grabList.add("minecraft:bed");
		event.grabList.add("minecraft:birch_door");
		event.grabList.add("minecraft:birch_fence");
		event.grabList.add("minecraft:birch_fence_gate");
		event.grabList.add("minecraft:birch_stairs");
		event.grabList.add("minecraft:black_glazed_terracotta");
		event.grabList.add("minecraft:black_shulker_box");
		event.grabList.add("minecraft:blue_glazed_terracotta");
		event.grabList.add("minecraft:blue_shulker_box");
		event.grabList.add("minecraft:bone_block");
		event.grabList.add("minecraft:bookshelf");
		event.grabList.add("minecraft:brewing_stand");
		event.grabList.add("minecraft:brick_block");
		event.grabList.add("minecraft:brick_stairs");
		event.grabList.add("minecraft:brown_glazed_terracotta");
		event.grabList.add("minecraft:brown_mushroom_block");
		event.grabList.add("minecraft:brown_shulker_box");
		event.grabList.add("minecraft:cactus");
		event.grabList.add("minecraft:cake");
		event.grabList.add("minecraft:carpet");
		event.grabList.add("minecraft:cauldron");
		event.grabList.add("minecraft:chest");
		event.grabList.add("minecraft:chorus_flower");
		event.grabList.add("minecraft:chorus_plant");
		event.grabList.add("minecraft:clay");
		event.grabList.add("minecraft:coal_block");
		event.grabList.add("minecraft:coal_ore");
		//event.grabList.add("minecraft:cobblestone");
		event.grabList.add("minecraft:cobblestone_wall");
		event.grabList.add("minecraft:comparator");
		event.grabList.add("minecraft:concrete");
		event.grabList.add("minecraft:concrete_powder");
		event.grabList.add("minecraft:crafting_table");
		event.grabList.add("minecraft:cyan_glazed_terracotta");
		event.grabList.add("minecraft:cyan_shulker_box");
		event.grabList.add("minecraft:dark_oak_door");
		event.grabList.add("minecraft:dark_oak_fence");
		event.grabList.add("minecraft:dark_oak_fence_gate");
		event.grabList.add("minecraft:dark_oak_stairs");
		event.grabList.add("minecraft:daylight_detector");
		event.grabList.add("minecraft:detector_rail");
		event.grabList.add("minecraft:diamond_block");
		event.grabList.add("minecraft:diamond_ore");
		event.grabList.add("minecraft:dispenser");
		event.grabList.add("minecraft:dragon_egg");
		event.grabList.add("minecraft:dropper");
		event.grabList.add("minecraft:emerald_block");
		event.grabList.add("minecraft:emerald_ore");
		event.grabList.add("minecraft:enchanting_table");
		event.grabList.add("minecraft:end_bricks");
		event.grabList.add("minecraft:end_stone");
		event.grabList.add("minecraft:ender_chest");
		event.grabList.add("minecraft:fence");
		event.grabList.add("minecraft:fence_gate");
		event.grabList.add("minecraft:flower_pot");
		event.grabList.add("minecraft:furnace");
		event.grabList.add("minecraft:gold_block");
		event.grabList.add("minecraft:gold_ore");
		event.grabList.add("minecraft:golden_rail");
		event.grabList.add("minecraft:gravel");
		event.grabList.add("minecraft:gray_glazed_terracotta");
		event.grabList.add("minecraft:gray_shulker_box");
		event.grabList.add("minecraft:green_glazed_terracotta");
		event.grabList.add("minecraft:green_shulker_box");
		event.grabList.add("minecraft:hardened_clay");
		event.grabList.add("minecraft:hay_block");
		event.grabList.add("minecraft:heavy_weighted_pressure_plate");
		event.grabList.add("minecraft:hopper");
		event.grabList.add("minecraft:iron_bars");
		event.grabList.add("minecraft:iron_block");
		event.grabList.add("minecraft:iron_door");
		event.grabList.add("minecraft:iron_ore");
		event.grabList.add("minecraft:iron_trapdoor");
		event.grabList.add("minecraft:jukebox");
		event.grabList.add("minecraft:jungle_door");
		event.grabList.add("minecraft:jungle_fence");
		event.grabList.add("minecraft:jungle_fence_gate");
		event.grabList.add("minecraft:jungle_stairs");
		event.grabList.add("minecraft:ladder");
		event.grabList.add("minecraft:lapis_block");
		event.grabList.add("minecraft:lapis_ore");
		//event.grabList.add("minecraft:leaves");
		event.grabList.add("minecraft:leaves2");
		event.grabList.add("minecraft:lever");
		event.grabList.add("minecraft:light_blue_glazed_terracotta");
		event.grabList.add("minecraft:light_blue_shulker_box");
		event.grabList.add("minecraft:light_weighted_pressure_plate");
		event.grabList.add("minecraft:lime_glazed_terracotta");
		event.grabList.add("minecraft:lime_shulker_box");
		event.grabList.add("minecraft:lit_pumpkin");
		event.grabList.add("minecraft:log");
		event.grabList.add("minecraft:log2");
		event.grabList.add("minecraft:magenta_glazed_terracotta");
		event.grabList.add("minecraft:magenta_shulker_box");
		event.grabList.add("minecraft:magma");
		event.grabList.add("minecraft:melon_block");
		event.grabList.add("minecraft:mossy_cobblestone");
		event.grabList.add("minecraft:mycelium");
		event.grabList.add("minecraft:nether_brick");
		event.grabList.add("minecraft:nether_brick_fence");
		event.grabList.add("minecraft:nether_brick_stairs");
		event.grabList.add("minecraft:nether_wart_block");
		event.grabList.add("minecraft:netherrack");
		event.grabList.add("minecraft:noteblock");
		event.grabList.add("minecraft:oak_stairs");
		event.grabList.add("minecraft:observer");
		event.grabList.add("minecraft:obsidian");
		event.grabList.add("minecraft:orange_glazed_terracotta");
		event.grabList.add("minecraft:orange_shulker_box");
		event.grabList.add("minecraft:pink_glazed_terracotta");
		event.grabList.add("minecraft:pink_shulker_box");
		event.grabList.add("minecraft:piston");
		//event.grabList.add("minecraft:planks");
		event.grabList.add("minecraft:prismarine");
		event.grabList.add("minecraft:pumpkin");
		event.grabList.add("minecraft:purple_glazed_terracotta");
		event.grabList.add("minecraft:purple_shulker_box");
		event.grabList.add("minecraft:purpur_block");
		event.grabList.add("minecraft:purpur_pillar");
		event.grabList.add("minecraft:purpur_slab");
		event.grabList.add("minecraft:purpur_stairs");
		event.grabList.add("minecraft:quartz_block");
		event.grabList.add("minecraft:quartz_ore");
		event.grabList.add("minecraft:quartz_stairs");
		event.grabList.add("minecraft:rail");
		event.grabList.add("minecraft:red_glazed_terracotta");
		event.grabList.add("minecraft:red_mushroom_block");
		event.grabList.add("minecraft:red_nether_brick");
		event.grabList.add("minecraft:red_sandstone");
		event.grabList.add("minecraft:red_sandstone_stairs");
		event.grabList.add("minecraft:red_shulker_box");
		event.grabList.add("minecraft:redstone_block");
		event.grabList.add("minecraft:redstone_ore");
		event.grabList.add("minecraft:redstone_torch");
		event.grabList.add("minecraft:repeater");
		event.grabList.add("minecraft:sand");
		event.grabList.add("minecraft:sandstone");
		event.grabList.add("minecraft:sandstone_stairs");
		event.grabList.add("minecraft:sapling");
		event.grabList.add("minecraft:sea_lantern");
		event.grabList.add("minecraft:sign");
		event.grabList.add("minecraft:silver_glazed_terracotta");
		event.grabList.add("minecraft:silver_shulker_box");
		event.grabList.add("minecraft:skull");
		event.grabList.add("minecraft:slime");
		event.grabList.add("minecraft:soul_sand");
		event.grabList.add("minecraft:sponge");
		event.grabList.add("minecraft:spruce_door");
		event.grabList.add("minecraft:spruce_fence");
		event.grabList.add("minecraft:spruce_fence_gate");
		event.grabList.add("minecraft:spruce_stairs");
		event.grabList.add("minecraft:stained_hardened_clay");
		event.grabList.add("minecraft:sticky_piston");
		event.grabList.add("minecraft:stone_brick_stairs");
		event.grabList.add("minecraft:stone_button");
		event.grabList.add("minecraft:stone_pressure_plate");
		event.grabList.add("minecraft:stone_slab");
		event.grabList.add("minecraft:stone_slab2");
		event.grabList.add("minecraft:stone_stairs");
		event.grabList.add("minecraft:stonebrick");
		event.grabList.add("minecraft:tnt");
		event.grabList.add("minecraft:trapdoor");
		event.grabList.add("minecraft:trapped_chest");
		event.grabList.add("minecraft:tripwire_hook");
		event.grabList.add("minecraft:white_glazed_terracotta");
		event.grabList.add("minecraft:white_shulker_box");
		event.grabList.add("minecraft:wooden_door");
		event.grabList.add("minecraft:wooden_slab");
		event.grabList.add("minecraft:wool");
		event.grabList.add("minecraft:yellow_flower");
		event.grabList.add("minecraft:yellow_glazed_terracotta");
		event.grabList.add("minecraft:yellow_shulker_box");
		event.grabList.add("weather2:anemometer");
		event.grabList.add("weather2:barometer_sensor");
		event.grabList.add("weather2:humidity_sensor");
		event.grabList.add("weather2:machine_case");
		event.grabList.add("weather2:radio_transmitter");
		event.grabList.add("weather2:rain_sensor");
		event.grabList.add("weather2:storm_sensor");
		event.grabList.add("weather2:temperature_sensor");
		event.grabList.add("weather2:tornado_sensor");
		event.grabList.add("weather2:tornado_siren");
		event.grabList.add("weather2:tornado_siren_manual");
		event.grabList.add("weather2:weather_deflector");
		event.grabList.add("weather2:weather_forecast");
		event.grabList.add("weather2:weather_forecast_2");
		event.grabList.add("weather2:weather_forecast_3");
		event.grabList.add("weather2:weather_machine");
		event.grabList.add("weather2:wind_sensor");
		event.grabList.add("weather2:wind_vane");

		//event.replaceList.add("minecraft:leaves", "minecraft:air");
		//event.replaceList.add("minecraft:leaves2", "minecraft:air");
		event.replaceList.add("minecraft:glass", "minecraft:air");
		event.replaceList.add("minecraft:glass_pane", "minecraft:air");
		event.replaceList.add("minecraft:glowstone", "minecraft:air");
		event.replaceList.add("minecraft:redstone_lamp", "minecraft:air");
		event.replaceList.add("minecraft:stained_glass", "minecraft:air");
		event.replaceList.add("minecraft:stained_glass_pane", "minecraft:air");
		event.replaceList.add("minecraft:brown_mushroom", "minecraft:air");
		event.replaceList.add("minecraft:deadbush", "minecraft:air");
		event.replaceList.add("minecraft:dirt", "minecraft:air");
		//event.replaceList.add("minecraft:double_plant", "minecraft:air");
		event.replaceList.add("minecraft:farmland", "minecraft:dirt");
		//event.replaceList.add("minecraft:grass", "minecraft:dirt");
		event.replaceList.add("minecraft:grass_path", "minecraft:dirt");
		event.replaceList.add("minecraft:ice", "minecraft:water");
		event.replaceList.add("minecraft:packed_ice", "minecraft:water");
		//event.replaceList.add("minecraft:red_flower", "minecraft:air");
		event.replaceList.add("minecraft:red_mushroom", "minecraft:air");
		event.replaceList.add("minecraft:redstone", "minecraft:air");
		event.replaceList.add("minecraft:reeds", "minecraft:air");
		event.replaceList.add("minecraft:snow", "minecraft:air");
		event.replaceList.add("minecraft:snow_layer", "minecraft:air");
		event.replaceList.add("minecraft:stone", "minecraft:cobblestone");
		event.replaceList.add("minecraft:string", "minecraft:air");
		//event.replaceList.add("minecraft:tallgrass", "minecraft:air");
		event.replaceList.add("minecraft:torch", "minecraft:air");
		event.replaceList.add("minecraft:vine", "minecraft:air");
		event.replaceList.add("minecraft:waterlily", "minecraft:air");
		event.replaceList.add("minecraft:web", "minecraft:air");
		event.replaceList.add("minecraft:wooden_button", "minecraft:air");
		event.replaceList.add("minecraft:wooden_pressure_plate", "minecraft:air");
	}
	
	@SubscribeEvent
	public void worldSave(Save event)
	{
		Weather2.writeOutData(false);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {
		if (ConfigMisc.toaster_pc_mode) return;

		ClientTickHandler.checkClientWeather();
		ClientTickHandler.weatherManager.tickRender(event.getPartialTicks());

		FoliageRenderer.radialRange = ConfigFoliage.shader_range;
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent.Pre event)
	{
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
		NewSceneEnhancer scene = NewSceneEnhancer.instance();
		
        if (scene.shouldChangeFogColor())
        {
			//backup original fog colors that are actively being adjusted based on time of day
        	float red = event.getRed(), green = event.getGreen(), blue = event.getBlue();
        	
        	if (scene.seesWeatherObject() && scene.fogMult > 0.0025F)
        	{
        		if (scene.fogRed < 0.0F)
            		scene.fogRed = red;
            	if (scene.fogGreen < 0.0F)
            		scene.fogGreen = green;
            	if (scene.fogBlue < 0.0F)
            		scene.fogBlue = blue;
        	}
        	else
        	{
        		if (scene.fogRedTarget >= 0.0F && scene.fogRedTarget != red)
            		scene.fogRedTarget = red;
            	if (scene.fogGreenTarget >= 0.0F && scene.fogGreenTarget != green)
            		scene.fogGreenTarget = green;
            	if (scene.fogBlueTarget >= 0.0F && scene.fogBlueTarget != blue)
            		scene.fogBlueTarget = blue;
            	
            	if (scene.fogRed != -1.0F && scene.fogRed == red)
            		scene.fogRed = -1.0F;
				if (scene.fogGreen != -1.0F && scene.fogGreen == green)
					scene.fogGreen = -1.0F;
				if (scene.fogBlue != -1.0F && scene.fogBlue == blue)
					scene.fogBlue = -1.0F;
        	}
        	
        	if (scene.fogRed >= 0.0F && scene.fogGreen >= 0.0F && scene.fogBlue >= 0.0F)
        	{
	        	event.setRed(scene.fogRed);
	        	event.setGreen(scene.fogGreen);
	        	event.setBlue(scene.fogBlue);
        	}
        }
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onFogRender(RenderFogEvent event)
	{
		if (ConfigMisc.toaster_pc_mode || ConfigClient.enable_custom_fog != true) return;
		NewSceneEnhancer scene = NewSceneEnhancer.instance();
		GlStateManager.setFog(GlStateManager.FogMode.EXP);
		GlStateManager.setFogStart(0.0F);
		GlStateManager.setFogEnd(scene.renderDistance);
		GlStateManager.setFogDensity(scene.fogMult);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		NewSceneEnhancer.instance().tickRender(event);	
	}

	@SubscribeEvent
	public void onEntityJoined(EntityJoinWorldEvent event)
	{
		Entity entity = event.getEntity();
		if (entity.world.isRemote) return;

		if (ConfigStorm.enable_villagers_take_cover && entity instanceof EntityCreature)
		{
			EntityCreature creature = (EntityCreature) entity;
			
			if (!WeatherUtilEntity.hasAITask(creature, EntityAITakeCover.class) && WeatherUtilEntity.hasAITask(creature, EntityAIMoveIndoors.class))
				creature.tasks.addTask(1, new EntityAITakeCover(creature));
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
