package net.mrbt0907.weather2.util;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Random;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.particle.entity.EntityRotFX;

public class WeatherUtilParticle {
    public static ArrayDeque<Particle>[][] fxLayers;
    
    public static int effLeafID = 0;
    public static int effRainID = 1;
    public static int effWindID = 2;
    public static int effSnowID = 3;
    /*public static int effSandID = 4;
    public static int effWind2ID = 2;*/
    
    public static Random rand = new Random();
    //public static int rainDrops = 20;
    
    
    
    //weather2: not sure what will happen to this in 1.7, copied over for convenience
    public static int getParticleAge(Particle ent)
    {
        return ent.particleAge;
        //return (Integer) OldUtil.getPrivateValueBoth(Particle.class, ent, "field_70546_d", "particleAge");
    }

    //weather2: not sure what will happen to this in 1.7, copied over for convenience
    public static void setParticleAge(Particle ent, int val)
    {
        ent.particleAge = val;
        //OldUtil.setPrivateValueBoth(Particle.class, ent, "field_70546_d", "particleAge", val);
    }

    @SuppressWarnings("unchecked")
	@SideOnly(Side.CLIENT)
    public static void getFXLayers()
    {
        //fxLayers
        Field field = null;

        try
        {
            field = (ParticleManager.class).getDeclaredField("field_78876_b");//ObfuscationReflectionHelper.remapFieldNames("net.minecraft.client.particle.EffectRenderer", new String[] { "fxLayers" })[0]);
            field.setAccessible(true);
            fxLayers = (ArrayDeque<Particle>[][])field.get(FMLClientHandler.instance().getClient().effectRenderer);
        }
        catch (Exception ex)
        {
        	//System.out.println("temp message: obf reflection fail!");
        	//ex.printStackTrace();
            try
            {
                field = (ParticleManager.class).getDeclaredField("fxLayers");
                field.setAccessible(true);
                fxLayers = (ArrayDeque<Particle>[][])field.get(FMLClientHandler.instance().getClient().effectRenderer);
            }
            catch (Exception ex2)
            {
                ex2.printStackTrace();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static float getParticleWeight(Particle entity1)
    {
        if (entity1 instanceof EntityRotFX)
            return 5.0F + ((float)((EntityRotFX)entity1).getAge() / 200);
        else if (entity1 instanceof Particle)
            return 5.0F + ((float)entity1.particleAge / 200);

        return -1;
    }
   
}
