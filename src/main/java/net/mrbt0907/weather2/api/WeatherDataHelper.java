package net.mrbt0907.weather2.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.server.event.ServerTickHandler;
import net.mrbt0907.weather2.weather.WeatherSystem;

public class WeatherDataHelper {

    /**
     * Check if precipitation occurring at position.
     * Use is somewhat expensive on cpu, consider caching result for frequent use
     *
     * @param world
     * @param position
     * @return
     */
	public static boolean isPrecipitatingAt(World world, BlockPos position) {
	    WeatherSystem weatherManager;
	    if (world.isRemote) {
	        weatherManager = getWeatherManagerForClient();
        } else {
	        weatherManager = ServerTickHandler.getWeatherSystemForDim(world.provider.getDimension());
        }
        if (weatherManager != null) {
	        return weatherManager.isPrecipitatingAt(position);
        }
	    return false;
    }

    @SideOnly(Side.CLIENT)
    public static WeatherSystem getWeatherManagerForClient() {
	    return ClientTickHandler.weatherManager;
    }

}
