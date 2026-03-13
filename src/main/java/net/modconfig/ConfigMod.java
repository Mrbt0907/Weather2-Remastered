package net.modconfig;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.CoroUtil.config.ConfigCoroUtil;
import net.CoroUtil.forge.CULog;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.CoroUtil.OldUtil;

@Mod("configmod")
public class ConfigMod {

    public static ConfigMod instance;




    public static List<ModConfigData> configs = new ArrayList<ModConfigData>();
    public static List<ModConfigData> liveEditConfigs = new ArrayList<ModConfigData>();
    public static HashMap<String, ModConfigData> configLookup = new HashMap<String, ModConfigData>();

    public static String eventChannelName = "modconfig";

    public static final SimpleChannel eventChannel = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(eventChannelName, "main"),
            () -> "1.0",
            s -> true,
            s -> true
    );
    public ConfigMod() {
        instance = this;


        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);


        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {


    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public void serverStart(FMLServerStartedEvent event) {



    }

    public static void populateData(String modid) {

        try {
            configLookup.get(modid).configData.clear();

            ModConfigData data = configLookup.get(modid);

            if (data != null) {


                processHashMap(modid, data.valsInteger);
                processHashMap(modid, data.valsDouble);
                processHashMap(modid, data.valsBoolean);
                processHashMap(modid, data.valsString);
            } else {
                System.out.println("error: cant find config data for gui");
            }


            configLookup.get(modid).configData.sort(new ConfigComparatorName());

        } catch (Exception ex) {
            if (ConfigCoroUtil.useLoggingDebug) {
                ex.printStackTrace();
            }
        }
    }

    public static void processHashMap(String modid, Map map) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            String name = (String)pairs.getKey();
            Object val = pairs.getValue();
            String comment = getComment(modid, name);
            ConfigEntryInfo info = new ConfigEntryInfo(configLookup.get(modid).configData.size(), name, val, comment);
            configLookup.get(modid).configData.add(info);
        }
    }

    public void initData() {

    }

    public void writeConfigFiles(Boolean resetData) {

    }

    public static String getSaveFolderPath() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server == null || server.isSingleplayer()) {
            return getClientSidePath() + File.separator;
        } else {
            return new File(".").getAbsolutePath() + File.separator;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static String getClientSidePath() {

        return FMLPaths.GAMEDIR.get().toFile().getPath();
    }

    public static void dbg(Object obj) {
        if (true) {
            System.out.println(obj);

        }
    }

    

    
    public static void addConfigFile(IConfigCategory configCat) {
        addConfigFile(null, configCat.getRegistryName(), configCat, true);
    }

    public static void addConfigFile(FMLCommonSetupEvent event, IConfigCategory configCat) {
        addConfigFile(event, configCat.getRegistryName(), configCat, true);
    }

    public static void addConfigFile(FMLCommonSetupEvent event, String modID, IConfigCategory configCat, boolean liveEdit) {



        if (configLookup.containsKey(configCat.getRegistryName())) {
            return;
        }

        ModConfigData configData = new ModConfigData(new File(getSaveFolderPath() + "config" + File.separator + configCat.getConfigFileName() + ".cfg"), modID, configCat.getClass(), configCat);

        configs.add(configData);
        if (liveEdit) liveEditConfigs.add(configData);
        configLookup.put(modID, configData);

        configData.initData();
        configData.writeConfigFile(false);
    }

    
    public static Object getField(String configID, String name) {
        try { return OldUtil.getPrivateValue(configLookup.get(configID).configClass, instance, name);
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }

    
    public static String getComment(String configID, String name) {
        try {
            Field field = configLookup.get(configID).configClass.getDeclaredField(name);
            ConfigComment anno_comment = field.getAnnotation(ConfigComment.class);
            return anno_comment == null ? null : anno_comment.value()[0];
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    
    public static boolean updateField(String configID, String name, Object obj) {
        if (configLookup.get(configID).setFieldBasedOnType(name, obj)) {

            configLookup.get(configID).writeConfigFile(true);
            return true;
        }
        return false;
    }

    public static void forceSaveAllFilesFromRuntimeSettings() {
        CULog.dbg("forceSaveAllFilesFromRuntimeSettings invoked");
        for (ModConfigData data : configLookup.values()) {
            data.writeConfigFile(true);
        }
    }

    public static void forceLoadRuntimeSettingsFromFile() {
        CULog.dbg("forceSaveAllFilesFromRuntimeSettings invoked");
        for (ModConfigData data : configLookup.values()) {

            data.writeConfigFile(false);
        }
    }

    
    public static void updateHashMaps() {
		
    }

    
}