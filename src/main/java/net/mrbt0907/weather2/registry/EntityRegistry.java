package net.mrbt0907.weather2.registry;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.entity.EntityIceBall;
import net.mrbt0907.weather2.entity.EntityLightningBolt;
import net.mrbt0907.weather2.entity.EntityLightningBoltCustom;
import net.mrbt0907.weather2.entity.EntityMovingBlock;

public class EntityRegistry
{
	public static void init()
	{
		Weather2.debug("Registering Entities...");
		addMapping(EntityIceBall.class, "weather_hail", 0, 128, 5, true);
		addMapping(EntityMovingBlock.class, "moving_block", 1, 128, 5, true);
		addMapping(EntityLightningBolt.class, "weather2_lightning_bolt", 2, 512, 5, true);
		addMapping(EntityLightningBoltCustom.class, "weather2_lightning_bolt_custom", 2, 512, 5, true);
		Weather2.debug("Finished registering entities...");
	}
	
	public static void addMapping(Class<? extends Entity> entity, String name, int entityId, int distSync, int tickRateSync, boolean syncMotion)
	{
		net.minecraftforge.fml.common.registry.EntityRegistry.registerModEntity(new ResourceLocation(Weather2.MODID, name), entity, name, entityId, Weather2.instance, distSync, tickRateSync, syncMotion);
		Weather2.debug("Registered entity " + Weather2.MODID + ":" + name);
	}
}
