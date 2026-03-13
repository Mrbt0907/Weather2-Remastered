<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/TriMapEx.java
package net.mrbt0907.weather2remastered.util;
=======
package net.mrbt0907.weather2.util;
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/TriMapEx.java

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TriMapEx<T, A, B> {
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/TriMapEx.java
	  private Map<T, A> listA = new LinkedHashMap<>();
	  private Map<T, B> listB = new LinkedHashMap<>();
	  
	  public A getA(T key) {
	    return this.listA.get(key);
	  }
	  
	  public B getB(T key) {
	    return this.listB.get(key);
	  }
	  
	  public TriMapEx<T, A, B> put(T key, A valueA, B valueB) {
	    this.listA.put(key, valueA);
	    this.listB.put(key, valueB);
	    return this;
	  }
	  
	  public Set<T> keys()
	  {
		  return listA.keySet();
	  }
	  
	  public Collection<A> valuesA()
	  {
		  return listA.values();
	  }
	  
	  public Collection<B> valuesB()
	  {
		  return listB.values();
	  }
	  
	  public void clear() {
	    this.listA.clear();
	    this.listB.clear();
	  }
	  
	  public boolean contains(T key) {
	    return this.listA.containsKey(key);
	  }
	  
	  public int size() {
	    return this.listA.size();
	  }
	}
=======
  private Map<T, A> listA = new LinkedHashMap<>();
  private Map<T, B> listB = new LinkedHashMap<>();
  
  public A getA(T key) {
    return this.listA.get(key);
  }
  
  public B getB(T key) {
    return this.listB.get(key);
  }
  
  public TriMapEx<T, A, B> put(T key, A valueA, B valueB) {
    this.listA.put(key, valueA);
    this.listB.put(key, valueB);
    return this;
  }
  
  public Set<T> keys()
  {
	  return listA.keySet();
  }
  
  public Collection<A> valuesA()
  {
	  return listA.values();
  }
  
  public Collection<B> valuesB()
  {
	  return listB.values();
  }
  
  public void clear() {
    this.listA.clear();
    this.listB.clear();
  }
  
  public boolean contains(T key) {
    return this.listA.containsKey(key);
  }
  
  public int size() {
    return this.listA.size();
  }
}
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/TriMapEx.java
