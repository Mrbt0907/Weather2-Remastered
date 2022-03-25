package net.mrbt0907.weather2.util;

import java.util.HashMap;
import java.util.Map;

public class EXMap <T, A, B>
{
	private Map<T,A> listA;
	private Map<T,B> listB;
	
	public EXMap()
	{
		listA = new HashMap<T, A>();
		listB = new HashMap<T, B>();
	}
	
	public A getA(T key)
	{
		return listA.get(key);
	}
	
	public B getB(T key)
	{
		return listB.get(key);
	}
	
	public EXMap<T, A, B> put(T key, A valueA, B valueB)
	{
		listA.put(key, valueA);
		listB.put(key, valueB);
		return this;
	}
	
	public void clear()
	{
		listA.clear();
		listB.clear();
	}
	
	public boolean contains(T key)
	{
		return listA.containsKey(key);
	}
	
	public int size()
	{
		return listA.size();
	}
}
