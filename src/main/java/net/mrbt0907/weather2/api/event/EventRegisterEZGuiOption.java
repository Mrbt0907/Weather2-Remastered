package net.mrbt0907.weather2.api.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.util.TriMapEx;

public class EventRegisterEZGuiOption extends Event
{
	private TriMapEx<String, List<String>, Integer> options;
	private Map<String, Integer> optionCategory;
	
	public EventRegisterEZGuiOption(TriMapEx<String, List<String>, Integer> options, Map<String, Integer> categories)
	{
		this.options = options;
		this.optionCategory = categories;
	}
	
	public void register(String id, EnumEZCategory category, int defaultOption, String... options)
	{
		List<String> settings = new ArrayList<String>();
		
		if (options.length > 0)
			for (String option : options)
				settings.add(option);
		
		register(id, category, defaultOption, settings);
	}
		
	public void register(String id, EnumEZCategory category, int defaultOption, List<String> options)
	{
		if (id == null)
			Weather2.debug("Failed to register EZ Gui option as the id was null. Skipping...");
		else if (this.options.contains(id))
			Weather2.debug("Failed to register EZ Gui option " + id + " as the id was already used. Skipping...");
		else if (category == null)
			Weather2.debug("Failed to register EZ Gui option " + id + " as the category was invalid. Skipping...");
		else if (options.size() < 2)
			Weather2.debug("Failed to register EZ Gui option " + id + " as there was not enough options. Skipping...");
		else if (defaultOption > options.size() || defaultOption < 0)
			Weather2.debug("Failed to register EZ Gui option " + id + " as the default option is out of range. Skipping...");
		else
		{
			List<String> settings = new ArrayList<String>();
			for (String option : options)
				settings.add(option);
			this.options.put(id, settings, defaultOption);
			optionCategory.put(id, category.ordinal());
		}
	}
	
	public TriMapEx<String, List<String>, Integer> getOptions()
	{
		return options;
	}
	
	public Map<String, Integer> getOptionCategories()
	{
		return optionCategory;
	}
	
	public static enum EnumEZCategory
	{
		GRAPHICS, SYSTEM, STORMS;
	}
}
