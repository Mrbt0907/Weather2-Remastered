package net.mrbt0907.weather2.config;

/**
 * Used for anything that needs to be used on both client and server side, to avoid config mismatch between dedicated server and clients
 */
public class ClientConfigData
{
	/*//Storm
	public boolean overcastMode = false;
	public boolean stormGrabPlayers = true;
	public boolean stormGrabMobs = true;
	public boolean stormGrabAnimals = true;
	public boolean stormGrabItems = false;
	public boolean stormGrabVillagers = true;
	public int stormCloudHeight0 = 200;
	public int stormCloudHeight1 = 350;
	public int stormCloudHeight2 = 500;
	
	//Wind
	public boolean enableWind = true;
	public boolean enableWindAffectsEntities = true;
	public double windPlayerWeightMult = 1.0D;
	public double windSwimmingWeightMult = 1.0D;
	public double windChangeMult = 1.0D;
	
	//Misc
	public boolean aestheticMode = false;
	public boolean debug_mode = ConfigMisc.debug_mode;
	public boolean debug_mode_radar = false;
	public double radar_range = 1024.0D;
	public double doppler_radar_range = 2048.0D;
	public double pulse_doppler_radar_range = 4096.0D;
	*/
	/**
	 * For client side
	 *
	 * @param nbt
	 */
	/*public void readNBT(NBTTagCompound nbt)
	{
		overcastMode = nbt.getBoolean("overcastMode");
		stormGrabPlayers = nbt.getBoolean("stormGrabPlayers");
		stormGrabMobs = nbt.getBoolean("stormGrabMobs");
		stormGrabAnimals = nbt.getBoolean("stormGrabAnimals");
		stormGrabVillagers = nbt.getBoolean("stormGrabVillagers");
		stormGrabItems = nbt.getBoolean("stormGrabItems");
		stormCloudHeight0 = nbt.getInteger("stormCloudHeight0");
		stormCloudHeight1 = nbt.getInteger("stormCloudHeight1");
		stormCloudHeight2 = nbt.getInteger("stormCloudHeight2");

		enableWind = nbt.getBoolean("enableWind");
		enableWindAffectsEntities = nbt.getBoolean("enableWindAffectsEntities");
		windPlayerWeightMult = nbt.getDouble("windPlayerWeightMult");
		windSwimmingWeightMult = nbt.getDouble("windSwimmingWeightMult");
		windChangeMult = nbt.getDouble("windChangeMult");
		
		aestheticMode = nbt.getBoolean("aestheticMode");
		debug_mode = nbt.getBoolean("debug_mode");
		debug_mode_radar = nbt.getBoolean("debug_mode_radar");
		radar_range = nbt.getDouble("radar_range");
		doppler_radar_range = nbt.getDouble("doppler_radar_range");
		pulse_doppler_radar_range = nbt.getDouble("pulse_doppler_radar_range");
	}*/

	/**
	 * For server side
	 *
	 * @param nbt
	 */
	/*public static void writeNBT(NBTTagCompound nbt)
	{
		nbt.setBoolean("overcastMode", ConfigMisc.overcast_mode);
		nbt.setBoolean("stormGrabPlayers", ConfigGrab.grab_players);
		nbt.setBoolean("stormGrabMobs", ConfigGrab.grab_mobs);
		nbt.setBoolean("stormGrabAnimals", ConfigGrab.grab_animals);
		nbt.setBoolean("stormGrabVillagers", ConfigGrab.grab_villagers);
		nbt.setBoolean("stormGrabItems", ConfigGrab.grab_items);

		nbt.setBoolean("enableWind", ConfigWind.enable);
		nbt.setBoolean("enableWindAffectsEntities", ConfigWind.enableWindAffectsEntities);
		nbt.setDouble("windPlayerWeightMult", ConfigWind.windPlayerWeightMult);
		nbt.setDouble("windSwimmingWeightMult", ConfigWind.windSwimmingWeightMult);
		nbt.setDouble("windChangeMult", ConfigWind.windChangeMult);
		nbt.setInteger("stormCloudHeight0", ConfigStorm.cloud_layer_0_height);
		nbt.setInteger("stormCloudHeight1", ConfigStorm.cloud_layer_1_height);
		nbt.setInteger("stormCloudHeight2", ConfigStorm.cloud_layer_2_height);
		
		nbt.setBoolean("aestheticMode", ConfigMisc.aesthetic_mode);
		nbt.setBoolean("debug_mode", ConfigMisc.debug_mode);
		nbt.setBoolean("debug_mode_radar", ConfigMisc.debug_mode_radar);
		nbt.setDouble("radar_range", ConfigMisc.radar_range);
		nbt.setDouble("doppler_radar_range", ConfigMisc.doppler_radar_range);
		nbt.setDouble("pulse_doppler_radar_range", ConfigMisc.pulse_doppler_radar_range);
	}*/
}