package net.mrbt0907.weather2.mixin.injection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

	// Fixes the renderSky method being invisible
	@Invoker("renderSky")
	public abstract void invokeRenderSky(BufferBuilder bufferBuilderIn, float posY, boolean reverseX);

	// Fixes the sky being too close for extended rendering
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
    	
    // Fixes the sky being stacked wrong for final buffers
    @Redirect(method = "generateSky()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderSky(Lnet/minecraft/client/renderer/BufferBuilder;FZ)V"), remap = true)
    private void redirectTopSkyPlaneY(RenderGlobal instance, BufferBuilder bufferBuilderIn, float posY, boolean reverseX) {
        // Original posY is 16.0F. We scale it up to match the 4096 width.
    	this.invokeRenderSky(bufferBuilderIn, 170.0F, reverseX);
    }
    @Redirect(method = "generateSky2()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderSky(Lnet/minecraft/client/renderer/BufferBuilder;FZ)V"), remap = true)
    private void redirectBottomSkyPlaneY(RenderGlobal instance, BufferBuilder bufferBuilderIn, float posY, boolean reverseX) {
        // Original posY is -16.0F. We push it down.
    	this.invokeRenderSky(bufferBuilderIn, -170.0F, reverseX);
    }
}
