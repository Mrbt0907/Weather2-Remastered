package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkManager.class)
public interface ChunkManagerAccessor {
    @Invoker("getChunks")
    Iterable<Chunk> invokeGetChunks();
}