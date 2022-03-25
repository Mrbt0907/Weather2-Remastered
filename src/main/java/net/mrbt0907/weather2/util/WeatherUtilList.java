package net.mrbt0907.weather2.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.mrbt0907.weather2.config.ConfigGrab;

public class WeatherUtilList
{
	/**The name of the list*/
	private String name;
	/**Provides the check method a way to verify the integrity of entries.
	 * <br>- 0: Grab Block List
	 * <br>- 1: Replace Block List
	 * <br>- 2: Grab Entity List
	 * <br>- 3: Wind Resistance List
	 * <br>- Others: Unchecked List*/
	private int id;
	/**Whether the list should verify it's entries when the registry calls it.*/
	private boolean enableChecks;
	private boolean enableClear;
	//Formatting should be !minecraft:grass#1
	private final Map<String,List<String>> list = new HashMap<String,List<String>>();
	private final Map<String,Float> listN = new HashMap<String,Float>();
	
	/**Initializes a list used mainly in configurations. Parameters are as follows:
	 * <br>- <b>"name"</b>: The name of this list.
	 * <br>- <b>"id"</b>: The id of this list. Ids determine how this list will funcion. Ids are below:
	 * <br>-- 0: Grab Block List
	 * <br>-- 1: Replace Block List
	 * <br>-- 2: Grab Entity List
	 * <br>-- 3: Wind Resistance List
	 * <br>-- Others: Unchecked List
	 * <br>- <b>"enableChecks"</b>: Whether the list should remove invalid entries when the check phase happens.
	 * <br>- <b>"enableClear"</b>: Whether this list can be cleared or not.*/
	public WeatherUtilList(String name, int i, boolean enableChecks, boolean enableClear)
	{
		this.name = name;
		this.id = i;
		this.enableChecks = enableChecks;
		this.enableClear = enableClear;
	}
	
	/**Verifies the integrity of this list and removes invalid entries. Set <b>enableChecks</b> to false if you want the list to remain unchecked.*/
	public void check()
	{
		if (!enableChecks) return;
		Map<String,List<String>> list = new HashMap<String,List<String>>();
		Map<String,Float> listN = new HashMap<String,Float>();
		
		switch(id)
		{
			case 0:
			{
				for (String key : this.list.keySet())
				{
					key = parseID(key);
					if (checkBlock(key))
						list.put(key, null);
				}
				break;
			}
			case 1:
			{
				for (String key : this.list.keySet())
				{
					if (checkBlock(parseID(key)))
					{
						List<String> entries = new ArrayList<String>();
						for (String value : this.list.get(key))
							if (checkBlock(parseID(value)))
								entries.add(parseID(value));
						if (!entries.isEmpty())
							list.put(parseID(key), entries);
					}
				}
				break;
			}
			case 2:
			{
				for (String key : this.list.keySet())
						list.put(parseID(key), null);
				break;
			}
			case 3:
			{
				for (String key : this.listN.keySet())
					if (checkBlock(parseID(key)))
						listN.put(parseID(key), this.listN.get(key));
				break;
			}
			default:
				return;
		}

		clear();
		if (id == 3)
			this.listN.putAll(listN);
		else
			this.list.putAll(list);
	}
	
	private boolean checkBlock(String id)
	{
		Iterator<ResourceLocation> registry = Block.REGISTRY.getKeys().iterator();
		while(registry.hasNext())
		{
			if (registry.next().toString().equals(id))
				return true;
		}
		return false;
	}
	
	private String parseID(String id)
	{
		id = id.toLowerCase().replaceAll("[^a-z\\d\\:\\_]+", "");
		if (!id.contains(":"))
			id = "minecraft:" + id;
		return id;
	}
	
	/**Adds an entry to the list. Extra values need to be used depending on the id of this list.*/
	public boolean add(String id, String... values)
	{
		boolean truth = false;
		String[] entry = id.split("\\:");
		if (entry.length < 2)
			entry = new String[] {"", entry[0]};
		
		switch(this.id)
		{
			case 1:
			{
				if (values.length > 0)
				{
					List<String> list = new ArrayList<String>();
					for (String value : values)
						list.add(value);
					if (ConfigGrab.replace_list_partial_matches && !id.contains(":"))
					{
						for (ResourceLocation block : Block.REGISTRY.getKeys())
							if (block.getResourcePath().contains(entry[1].toLowerCase()))
								this.list.put(block.toString(), list);
					}
					else
						this.list.put(id, list);
					truth = true;
				}
				break;
			}
			case 3:
			{
				if (values.length > 0)
				{
					String a = values[0].replaceAll("[^\\d\\.]+", "");
					
					if (a.length() > 0)
					{
						float value = Float.parseFloat(a);
						if (ConfigGrab.wind_resistance_partial_matches && !id.contains(":"))
						{
							for (ResourceLocation block : Block.REGISTRY.getKeys())
								if (block.getResourcePath().contains(entry[1].toLowerCase()))
								{
									listN.put(block.toString(), value);
									truth = true;
								}
						}
						else
						{
							listN.put(id, value);
							truth = true;
						}
					}
				}
				break;
			}
			default:
			{
				if (ConfigGrab.grab_list_partial_matches)
				{
					for (ResourceLocation block : Block.REGISTRY.getKeys())
						if (block.getResourcePath().contains(entry[1].toLowerCase()))
						{
							list.put(block.toString(), null);
							truth = true;
						}
				}
				else
				{
					list.put(id, null);
					truth = true;
				}
			}
		}
		return truth;
	}
	
	/**Adds an entry with a float value associated with it. Unused if list id is not 3.*/
	public boolean add(String id, float value)
	{
		if (this.id == 3)
		{
			listN.put(id, value);
			return true;
		}
		
		return false;
	}
	
	/**Removes an entry from this list. Inputing no extra values will remove the id completely. Inputing extra values will remove only those values from the id.*/
	public boolean remove(String id, String... replacementIDs)
	{
		boolean truth = false;
		if (this.id == 3)
		{
			if (listN.containsKey(id))
			{
				listN.remove(id);
				truth = true;
			}
			else
				return truth;
		}
		else
		{
			List<String> list = this.list.get(id);
			if (replacementIDs.length == 0 || list == null)
			{
				this.list.remove(id);
				return true;
			}
			
			int s1 = replacementIDs.length;
			int s2 = list.size();
			
			for (int i1=0;i1<s1;i1++)
				for (int i2=0;i2<s2;i2++)
					if(list.get(i2) == replacementIDs[i1])
					{
						list.remove(i2);
						truth = true;
					}
			this.list.put(id, list);
			
		}
		return truth;
	}
	
	/**Parses a given string to add and remove items from this list. Format goes as follows:
	 *<br>- <b>"!"</b>  Remove this block from the list. Unused if id is 1
	 *<br>- <b>":"</b>  Modid prefix. If no modid is present and partialMatches is set to true, will grab all entires that contain this name, otherwise will default to "minecraft:".
	 *<br>- <b>"="</b>  Adds a replacement block to the block in front. Can be stacked any amount of times. Unused if id is 0 or 2.
	 *<br>- <b>"#"</b>  Metadata of the block <i><b>(NOT IMPLEMENTED YET)</i></b>.
	 *<br>- <b>","</b> and/or <b>" "</b>  Separates each entry in the string.*/
	public void parse(String str)
	{
		String[] list = str.split("[\\s\\,]+");
		for (String entry : list)
		{
			String[] entries = entry.split("\\=");
			switch(id)
			{
				case 1: case 3:
				{
					if (entries.length > 0 && entries[0].contains("!"))
					{
						remove(parseID(entries[0]));
						continue;
					}
					if (entries.length > 1 && entries[0].length() > 0 && entries[1].length() > 0)
					{
						if (id == 3)
							add(entries[0], entries[1]);
						else
						{
							int size = entries.length;
							String a = ""; String[] b = new String[size - 1];
							for (int i = 0; i < size; i++)
								if (i == 0)
										a = entries[i];
								else
									b[i - 1] = entries[i];
							add(a, b);
						}
					}
					break;
				}
				default:
				{
					if (entries.length > 0 && entries[0].length() > 0)
					{
						if (entries[0].contains("!"))
							remove(parseID(entries[0]));
						else
							add(entries[0]);
					}
					break;
				}
			}
		}
	}
	
	/**Returns a List with strings or a float value if the id is 3.*/
	public Object get(String id)
	{
		if (this.id == 3)
			return listN.get(id);
		else
			return list.get(id);
	}
	
	/**Returns true if the id is in this list.*/
	public boolean exists(String id)
	{
		if (this.id == 3)
			return listN.containsKey(id);
		else
			return list.containsKey(id);
	}
	
	/**Clears the list from all inputed values*/
	public void clear()
	{
		if (enableClear)
		{
			list.clear();
			listN.clear();
		}
	}
	
	public Map<String,List<String>> toMap()
	{
		return list;
	}
	
	public int size()
	{
		return id == 3 ? listN.size(): list.size();
	}
	
	public String getName()
	{
		return name;
	}
}
