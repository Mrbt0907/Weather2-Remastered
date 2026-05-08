package net.mrbt0907.weather2.mixin.injection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Inject(method = "renderSky(Lnet/minecraft/client/renderer/BufferBuilder;FZ)V", at = @At("HEAD"), cancellable = true, remap = true)
    private void renderSky(BufferBuilder bufferBuilderIn, float posY, boolean reverseX, CallbackInfo ci) {
        ci.cancel();
        int i = 64;
        int j = 6;
        bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);
        int max = 4096;
        for (int k = -max; k <= max; k += 64) {
            for (int l = -max; l <= max; l += 64) {
                float f = (float) k;
                float f1 = (float) (k + 64);

                if (reverseX) {
                    f1 = (float) k;
                    f = (float) (k + 64);
                }

                bufferBuilderIn.pos((double) f, (double) posY, (double) l).endVertex();
                bufferBuilderIn.pos((double) f1, (double) posY, (double) l).endVertex();
                bufferBuilderIn.pos((double) f1, (double) posY, (double) (l + 64)).endVertex();
                bufferBuilderIn.pos((double) f, (double) posY, (double) (l + 64)).endVertex();
            }
        }
    }
}
