package net.mrbt0907.configex.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArrayUtils
{
	@SafeVarargs
	public static <A, B> Map<A, B> createMap(MapEntry<A, B>... entries)
	{
		Map<A, B> map = new LinkedHashMap<A, B>();
		
		for(MapEntry<A, B> entry : entries)
			map.put(entry.key, entry.value);
		
		return map;
	}
	
	public static Map<String, String> getMap(List<String> list)
	{
		Map<String, String> map = new LinkedHashMap<String, String>();
		list.forEach(str -> map.put(str.replaceAll("\\=.+", ""), str.replaceAll(".+\\=", "")));
		return map;
	}
	
	public static class MapEntry <A, B>
	{
		public final A key;
		public final B value;
		
		private MapEntry(A key, B value)
		{
			this.key = key;
			this.value = value;
		}
		
		public static <A, B> MapEntry<A, B> Entry(A key, B value)
		{
			return new MapEntry<A, B>(key, value);
		}
	}
}
