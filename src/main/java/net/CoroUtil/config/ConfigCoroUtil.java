package net.CoroUtil.config;

import java.io.File;
import java.util.Arrays;

import net.modconfig.ConfigComment;
import net.modconfig.IConfigCategory;
import net.CoroUtil.util.DimensionChunkCacheNew;

public class ConfigCoroUtil implements IConfigCategory {


    public static String chunkCacheDimensionBlacklist_IDs = "";
    public static String chunkCacheDimensionBlacklist_Names = "promised";
    public static boolean disableParticleRenderer = false;
    public static boolean disableMipmapFix = false;
    public static boolean forceShadersOff = false;

    @ConfigComment("Provides better context for shaders/particles to work nice with translucent blocks like glass and water")
    public static boolean useEntityRenderHookForShaders = true;

    @ConfigComment("WIP, more strict transparent cloud usage, better on fps")
    public static boolean optimizedCloudRendering = false;

    public static boolean debugShaders = false;

    public static boolean foliageShaders = false;
    public static boolean particleShaders = true;

    @ConfigComment("For seldom used but important things to print out in production")
    public static boolean useLoggingLog = true;

    @ConfigComment("For debugging things")
    public static boolean useLoggingDebug = false;

    @ConfigComment("For logging warnings/errors")
    public static boolean useLoggingError = true;


    public static boolean usePlayerRadiusChunkLoadingForFallback = true;


    @ConfigComment("How many game ticks until a repairing block fully restores to its original block")
    public static int ticksToRepairBlock = 20 * 60 * 5;

    @Override
    public String getName() {
        return "General";
    }

    @Override
    public String getRegistryName() {
        return "coroutil_general";
    }

    @Override
    public String getConfigFileName() {
        return "CoroUtil" + File.separator + "coroutil_general";
    }

    @Override
    public String getCategory() {
        return getName();
    }

    @Override
    public void hookUpdatedValues() {
        try {
            String[] ids = chunkCacheDimensionBlacklist_IDs.split(",");
            String[] names = chunkCacheDimensionBlacklist_Names.split(",");

            DimensionChunkCacheNew.listBlacklistIDs.clear();
            for (int i = 0; i < ids.length; i++) {
                DimensionChunkCacheNew.listBlacklistIDs.add(Integer.valueOf(ids[i]));
            }
            DimensionChunkCacheNew.listBlacklistNamess = Arrays.asList(names);
        } catch (Exception ex) {

        }
    }
}