package net.CoroUtil.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;

public class CoroUtilFile {
    public static String lastWorldFolder = "";


    public static String getWorldFolderName() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server != null) {
            Iterable<ServerWorld> worlds = server.getAllLevels();
            for (ServerWorld world : worlds) {
                if (world.dimension().location().toString().equals("minecraft:overworld")) {

                    File worldDir = world.getServer().getServerDirectory();
                    lastWorldFolder = worldDir.getName();
                    return lastWorldFolder + File.separator;
                }
            }
        }

        return lastWorldFolder + File.separator;
    }


    public static String getMinecraftSaveFolderPath() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.isSingleplayer()) {
            return getClientSidePath() + File.separator + "config" + File.separator;
        } else {
            return new File(".").getAbsolutePath() + File.separator + "config" + File.separator;
        }
    }

    public static String getWorldSaveFolderPath() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.isSingleplayer()) {
            return getClientSidePath() + File.separator + "saves" + File.separator;
        } else {
            return new File(".").getAbsolutePath() + File.separator;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static String getClientSidePath() {
        return Minecraft.getInstance().gameDirectory.getPath();
    }


    @OnlyIn(Dist.CLIENT)
    public static String getContentsFromResourceLocation(ResourceLocation resourceLocation) {
        try {
            IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            IResource iresource = resourceManager.getResource(resourceLocation);
            String contents = IOUtils.toString(iresource.getInputStream(), StandardCharsets.UTF_8);
            return contents;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
}