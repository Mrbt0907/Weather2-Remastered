package net.mrbt0907.weather2.util;

import net.CoroUtil.config.ConfigCoroUtil;
import net.extendedrenderer.EventHandler;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigMisc;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MixinWorldReciever
{
    public static void renderRain(float partialTicks, CallbackInfo callback)
    {
        if (ConfigMisc.proxy_render_override)
        {
            if (ConfigCoroUtil.useEntityRenderHookForShaders)
                EventHandler.hookRenderShaders(partialTicks);
            if (!ConfigClient.enable_vanilla_rain)
                callback.cancel();
        }
    }
}