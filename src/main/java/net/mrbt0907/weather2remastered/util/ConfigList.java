<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
package net.mrbt0907.weather2remastered.util;
=======
package net.mrbt0907.weather2.util;
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
import net.mrbt0907.weather2remastered.Weather2Remastered;

=======
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java
public class ConfigList
{
	private Map<String, Object[]> map = new LinkedHashMap<String, Object[]>();
	private boolean replace = false;
	
	public ConfigList() {}
	
	public ConfigList(String... entries)
	{
		for (String entry : entries)
			parse(entry);
	}
	
	public ConfigList addAll(ConfigList list)
	{
		if (list != null)
			map.putAll(list.toMap());
		return this;
	}
	
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
	/**Adds an entry with specified values. Values are optional.*/
=======
	
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java
	public ConfigList add(String id, Object... values)
	{
		if (map.containsKey(id))
		{
			Object[] oldTable = map.get(id), table = null;
			int sizeA = oldTable.length, sizeB;
			
			if (replace)
				table = values;
			else
			{
				sizeB = sizeA + values.length;
				table = new Object[sizeB];
				
				for (int i = 0; i < sizeB; i++)
				{
					if (i < sizeA)
						table[i] = oldTable[i];
					else
						table[i] = values[i - sizeA];
				}
			}
			
			map.put(id, table);
		}
		else
			map.put(id, values);
		return this;
	}
	
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
	/**Removes an entry from this list.*/
=======
	
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java
	public ConfigList remove(String... ids)
	{
		for (String id : ids)
		{
			if (!id.contains(":"))
				id = "minecraft:" + id;
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
			Weather2Remastered.info("REMOVING: " + id);
=======
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java
			map.remove(id);
		}
		
		return this;
	}
	
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
	/**Returns a table of values from the specified id.*/
=======
	
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java
	public Object get(String id)
	{
		return get(id, 0);
	}
	
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
	/**Returns a table of values from the specified id.*/
=======
	
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java
	public Object get(String id, int index)
	{
		Object[] a = map.get(id);
		return a != null && a.length > index ? a[index] : null;
	}
	
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
	/**Returns a table of values from the specified id.*/
=======
	
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java
	public Object[] getValues(String id)
	{
		return map.get(id);
	}
	
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
	/**Returns true if the id is in this list.*/
=======
	
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java
	public boolean exists(String id)
	{
		return map.containsKey(id);
	}
	
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
	/**Clears the list from all inputed values*/
=======
	
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java
	public void clear()
	{
		map.clear();
	}
	
	public Map<String, Object[]> toMap()
	{
		return map;
	}
	
	public int size()
	{
		return map.size();
	}
	
	public boolean containsKey(String key)
	{
		return map.containsKey(key);
	}
	
	public boolean containsValue(Object[] value)
	{
		return map.containsValue(value);
	}
	
	public boolean isReplaceOnly()
	{
		return replace;
	}
	
	public ConfigList setReplaceOnly()
	{
		replace = true;
		return this;
	}
	
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/ConfigList.java
	/**Parses a given string to add and remove entries from this list. Format goes as follows:
	 *<br>- <b>"!"</b>  Remove this id from the list.
	 *<br>- <b>":"</b>  Modid prefix. If no modid is present and partialMatches is set to true, will grab all entires that contain this name, otherwise will default to "minecraft:".
	 *<br>- <b>"="</b>  Adds a replacement block to the block in front. Can be stacked any amount of times. Unused if id is 0 or 2.
	 *<br>- <b>"#"</b>  Metadata of the block.
	 *<br>- <b>","</b> and/or <b>" "</b>  Separates each entry in the string.*/
=======
	
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/ConfigList.java
	public void parse(String str)
	{
		String[] entries = str.split("[\\s\\,\\;\\\\]+");
		
		for (String entry : entries)
		{
			String id = "";
			List<Object> values = new ArrayList<Object>();
			
			String[] items = entry.split("\\=");
			int size = items.length;
			
			for (int i = 0; i < size; i++)
			{
				if (i == 0)
				{
					id = items[i];
					
					if (id.contains("!"))
					{
						remove(id.replaceAll("\\!", ""));
						id = "";
						break;
					}
				}
				else
				{
					values.add(items[i]);
					remove(id.replaceAll("\\!", "").replaceAll("\\#.*", ""));
				}
			}
			
			if (id != "")
				add(id, values.toArray());
		}
	}
}