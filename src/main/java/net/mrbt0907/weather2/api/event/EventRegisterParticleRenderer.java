package net.mrbt0907.weather2.api.event;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.AbstractWeatherRenderer;

@Cancelable
public class EventRegisterParticleRenderer extends Event
{
	private final Map<ResourceLocation, Class<?>> registry = new LinkedHashMap<ResourceLocation, Class<?>>();
	
	/**Event used to register custom particle spawning renderers into the game.*/
	public EventRegisterParticleRenderer() {}
	
	/**Gets a copy of the current registry for particle renderers.*/
	public Map<ResourceLocation, Class<?>> getRegistry()
	{
		return new LinkedHashMap<ResourceLocation, Class<?>>(registry);
	}
	
	/**Registers a particle renderer to the game.*/
	public void register(ResourceLocation id, Class<?> particleRenderer)
	{
		if (id == null)
			Weather2.debug("Failed to register a particle renderer as the id was null. Skipping...");
		else if (id.toString().equals(Weather2.MODID + ":normal") || registry.containsKey(id))
			Weather2.debug("Failed to register a particle renderer as the id is already taken. Skipping...");
		else if (particleRenderer == null)
			Weather2.debug("Failed to register particle renderer " + id.toString() + " as the renderer was null. Skipping...");
		else if (!AbstractWeatherRenderer.class.isAssignableFrom(particleRenderer))
			Weather2.debug("Failed to register particle renderer " + id.toString() + " as the renderer does not extend from AbstractStormRenderer. Skipping...");
		else
		{
			registry.put(id, particleRenderer);
			Weather2.debug("Registered particle renderer " + id.toString());
		}
	}
}