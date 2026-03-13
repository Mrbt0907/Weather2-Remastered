package net.CoroUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.item.Item;
import net.minecraft.util.math.vector.Vector3i;

public class OldUtil {

	
	public static String refl_loadedChunks_mcp = "loadedChunks";
	public static String refl_loadedChunks_obf = "field_73245_g";

	public static boolean checkforMCP = true;
	public static boolean runningMCP = true;
	
	public OldUtil() {
    }

	
	public static void check() {
		checkforMCP = false;
		try {
            runningMCP = getPrivateValue(Vector3i.class, null, "NULL_VECTOR") != null;
		} catch (Exception e) {
			runningMCP = false;
			System.out.println("CoroAI: 'tickables' field not found, mcp mode disabled");
		}
	}

    public static Object getPrivateValueSRGMCP(Class var0, Object var1, String srg, String mcp) {
    	if (checkforMCP) check();
    	try {
    		
    		if (!runningMCP) {
    			return getPrivateValue(var0, var1, srg);
    		} else {
    			return getPrivateValue(var0, var1, mcp);
    		}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object getPrivateValue(Class var0, Object var1, String var2) {
        try
        {
            Field var3 = var0.getDeclaredField(var2);
            var3.setAccessible(true);
            return var3.get(var1);
        }
        catch (Exception var4)
        {
            return null;
        }
    }
    
    static Field field_modifiers = null;

    public static void setPrivateValue(Class var0, Object var1, String var2, Object var3) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
        	if (field_modifiers == null) {
        		field_modifiers = Field.class.getDeclaredField("modifiers");
                field_modifiers.setAccessible(true);
        	}
        	
            Field var4 = var0.getDeclaredField(var2);
            int var5 = field_modifiers.getInt(var4);

            if ((var5 & 16) != 0)
            {
                field_modifiers.setInt(var4, var5 & -17);
            }

            var4.setAccessible(true);
            field_modifiers.setInt(var4, var4.getModifiers() & ~Modifier.FINAL);
            var4.set(var1, var3);
        }
        catch (IllegalAccessException var6)
        {
        }
    }

    public static boolean isServer() {
    	return false;
    }

}