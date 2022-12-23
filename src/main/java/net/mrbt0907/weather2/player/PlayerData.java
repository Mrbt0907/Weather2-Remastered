package net.mrbt0907.weather2.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.mrbt0907.weather2.Weather2;
import CoroUtil.util.CoroUtilFile;

public class PlayerData {

	public static HashMap<UUID, NBTTagCompound> playerNBT = new HashMap<UUID, NBTTagCompound>();
	
	public static NBTTagCompound getPlayerNBT(UUID playerUUID) {
		if (!playerNBT.containsKey(playerUUID))
			tryLoadPlayerNBT(playerUUID);
		
		return playerNBT.get(playerUUID);
	}
	
	public static void tryLoadPlayerNBT(UUID playerUUID)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		
		try
		{
			String fileURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "weather2" + File.separator + "PlayerData" + File.separator + playerUUID.toString() + ".dat";
			
			if ((new File(fileURL)).exists())
			{
				nbt = CompressedStreamTools.readCompressed(new FileInputStream(fileURL));
			}
		} catch (Exception ex) {
			//Weather.dbg("no saved data found for " + username);
		}
		
		playerNBT.put(playerUUID, nbt);
	}
	
	public static void writeAllPlayerNBT(boolean resetData)
	{		
		String fileURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "weather2" + File.separator + "PlayerData";
		if (!new File(fileURL).exists()) new File(fileURL).mkdir();
		
		playerNBT.forEach((uuid, nbt) -> writePlayerNBT((uuid), nbt));
	    
	    if (resetData)
	    	playerNBT.clear();
	}
	
	public static void writePlayerNBT(UUID playerUUID, NBTTagCompound nbt)
	{
		String fileURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "weather2" + File.separator + "PlayerData" + File.separator + playerUUID.toString() + ".dat";
		
		try
		{
			FileOutputStream fos = new FileOutputStream(fileURL);
	    	CompressedStreamTools.writeCompressed(nbt, fos);
	    	fos.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Weather2.debug("Error writing Weather2 player data for " + playerUUID.toString());
		}
	}
	
}
