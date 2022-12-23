package net.mrbt0907.weather2.config;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Used for anything that needs to be used on both client and server side, to avoid config mismatch between dedicated server and clients
 */
public class ClientConfigData {

    public boolean overcastMode = false;
    public boolean Storm_Tornado_grabPlayer = true;
    public boolean Storm_Tornado_grabMobs = true;
    public boolean Storm_Tornado_grabAnimals = true;
    public boolean Storm_Tornado_grabItems = false;
    public boolean Storm_Tornado_grabVillagers = true;
    public boolean Aesthetic_Only_Mode = false;

    /**
     * For client side
     *
     * @param nbt
     */
    public void readNBT(NBTTagCompound nbt)
    {
        overcastMode = nbt.getBoolean("overcastMode");
        Storm_Tornado_grabPlayer = nbt.getBoolean("Storm_Tornado_grabPlayer");
        Storm_Tornado_grabMobs = nbt.getBoolean("Storm_Tornado_grabMobs");
        Storm_Tornado_grabAnimals = nbt.getBoolean("Storm_Tornado_grabAnimals");
        Storm_Tornado_grabVillagers = nbt.getBoolean("Storm_Tornado_grabVillagers");
        Storm_Tornado_grabItems = nbt.getBoolean("Storm_Tornado_grabItems");
        Aesthetic_Only_Mode = nbt.getBoolean("Aesthetic_Only_Mode");
    }

    /**
     * For server side
     *
     * @param data
     */
    public static void writeNBT(NBTTagCompound data)
    {
        data.setBoolean("overcastMode", ConfigMisc.overcast_mode);
        data.setBoolean("Storm_Tornado_grabPlayer", ConfigGrab.grab_players);
        data.setBoolean("Storm_Tornado_grabMobs", ConfigGrab.grab_mobs);
        data.setBoolean("Storm_Tornado_grabAnimals", ConfigGrab.grab_animals);
        data.setBoolean("Storm_Tornado_grabVillagers", ConfigGrab.grab_villagers);
        data.setBoolean("Storm_Tornado_grabItems", ConfigGrab.grab_items);
        data.setBoolean("Aesthetic_Only_Mode", ConfigMisc.aesthetic_mode);
    }

}
