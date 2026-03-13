package net.CoroUtil.forge;

import net.CoroUtil.config.*;
import net.modconfig.ConfigMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CoroUtil.modID)
public class CoroUtil {

    public static CoroUtil instance;
    public static final String modID = "coroutil";
    public static final String version = "1.16.5-1.2.37";

    public static ConfigCoroUtil configCoroUtil = new ConfigCoroUtil();

    public CoroUtil() {
        instance = this;




        CommonProxy.register(FMLJavaModLoadingContext.get().getModEventBus());




        ConfigMod.addConfigFile(configCoroUtil);
    }

    public static void dbg(String obj) {
        CULog.dbg(obj);
    }
}