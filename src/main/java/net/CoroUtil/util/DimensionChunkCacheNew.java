package net.CoroUtil.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.CoroUtil.config.ConfigCoroUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.CoroUtil.OldUtil;
import net.mrbt0907.weather2.mixins.accessor.ChunkManagerAccessor;

public class DimensionChunkCacheNew implements IBlockReader {

    public static List<Integer> listBlacklistIDs = new ArrayList<Integer>();
    public static List<String> listBlacklistNamess = new ArrayList<String>();


    public static HashMap<Integer, DimensionChunkCacheNew> dimCacheLookup = new HashMap<Integer, DimensionChunkCacheNew>();

    public int chunkX;
    public int chunkZ;
    public int chunkXMax;
    public int chunkZMax;
    private Chunk[][] chunkArray;


    private boolean hasExtendedLevels;


    private World worldObj;

    public static int lastChunkCacheCount = 0;



    private static int getDimensionId(ServerWorld world) {
        String dimLocation = world.dimension().location().toString();
        switch (dimLocation) {
            case "minecraft:overworld":
                return 0;
            case "minecraft:the_nether":
                return -1;
            case "minecraft:the_end":
                return 1;
        }
        return dimLocation.hashCode();
    }

    public DimensionChunkCacheNew(World world, boolean useLoadedChunks)
    {
        int chunkCount = 0;

        try {
            int minX = 0;
            int minZ = 0;
            int maxX = 0;
            int maxZ = 0;

            List chunks = null;

            if (world instanceof ServerWorld) {
                ServerChunkProvider chunkProvider = (ServerChunkProvider)world.getChunkSource();

                try {
                    chunks = new ArrayList<>();
                    ((ChunkManagerAccessor)chunkProvider.chunkMap).invokeGetChunks().forEach(chunks::add);
                } catch (Exception ex) {
                    chunks = null;
                }
            }

            if (chunks == null) {
                try {
                    chunks = (ArrayList)OldUtil.getPrivateValueSRGMCP(ServerChunkProvider.class, world.getChunkSource(), OldUtil.refl_loadedChunks_obf, OldUtil.refl_loadedChunks_mcp);
                } catch (Exception ex2) {
                    System.out.println("SERIOUS REFLECTION FAIL IN DimensionChunkCache");
                }
            }

            if (chunks == null) {
                if (ConfigCoroUtil.usePlayerRadiusChunkLoadingForFallback) {
                    System.out.println("unable to get loaded chunks, reverting to potentially cpu/memory heavy player radius method, to deactivate set usePlayerRadiusChunkLoadingForFallback in CoroUtil.cfg to false");
                } else {
                    if (world instanceof ServerWorld) {
                        System.out.println("loadedChunks is null, DimensionChunkCache unable to cache chunk data for dimension: " + getDimensionId((ServerWorld)world) + " - " + world.dimension().location());
                    } else {
                        System.out.println("loadedChunks is null, DimensionChunkCache unable to cache chunk data");
                    }
                }
            }

            if (chunks != null && useLoadedChunks) {








                if (chunks != null) {
                    for (int i = 0; i < chunks.size(); i++) {
                        Chunk chunk = (Chunk) chunks.get(i);

                        if ((int)chunk.getPos().x < minX) minX = chunk.getPos().x;
                        if ((int)chunk.getPos().z < minZ) minZ = chunk.getPos().z;
                        if ((int)chunk.getPos().x > maxX) maxX = chunk.getPos().x;
                        if ((int)chunk.getPos().z > maxZ) maxZ = chunk.getPos().z;
                    }



                    this.worldObj = world;
                    this.chunkX = minX;
                    this.chunkZ = minZ;
                    int var8 = maxX;
                    int var9 = maxZ;

                    this.chunkArray = new Chunk[var8 - this.chunkX + 1][var9 - this.chunkZ + 1];
                    this.hasExtendedLevels = true;

                    for (int i = 0; i < chunks.size(); i++) {
                        Chunk chunk = (Chunk) chunks.get(i);
                        this.chunkArray[chunk.getPos().x - this.chunkX][chunk.getPos().z - this.chunkZ] = chunk;
                        chunkCount++;
                    }
                }

            } else if (ConfigCoroUtil.usePlayerRadiusChunkLoadingForFallback) {
                byte playerRadius = 8;

                for (int i = 0; i < world.players().size(); ++i)
                {
                    PlayerEntity var5 = (PlayerEntity)world.players().get(i);

                    if ((int)var5.getX() < minX) minX = (int)var5.getX();
                    if ((int)var5.getZ() < minZ) minZ = (int)var5.getZ();
                    if ((int)var5.getX() > maxX) maxX = (int)var5.getX();
                    if ((int)var5.getZ() > maxZ) maxZ = (int)var5.getZ();
                }

                minX -= (playerRadius * 16);
                minZ -= (playerRadius * 16);
                maxX += (playerRadius * 16);
                maxZ += (playerRadius * 16);

                this.worldObj = world;
                this.chunkX = minX >> 4;
                this.chunkZ = minZ >> 4;
                int var8 = maxX >> 4;
                int var9 = maxZ >> 4;
                this.chunkXMax = var8;
                this.chunkZMax = var9;
                this.chunkArray = new Chunk[var8 - this.chunkX + 1][var9 - this.chunkZ + 1];
                this.hasExtendedLevels = true;

                for (int i = 0; i < world.players().size(); ++i)
                {

                    PlayerEntity var5 = (PlayerEntity)world.players().get(i);

                    int pChunkX = MathHelper.floor(var5.getX() / 16.0D);
                    int pChunkZ = MathHelper.floor(var5.getZ() / 16.0D);

                    for (int xx = -playerRadius; xx <= playerRadius; ++xx)
                    {
                        for (int zz = -playerRadius; zz <= playerRadius; ++zz)
                        {
                            if (pChunkX + xx - this.chunkX >= 0 && pChunkZ + zz - this.chunkZ >= 0 && this.chunkArray[pChunkX + xx - this.chunkX][pChunkZ + zz - this.chunkZ] == null) {
                                Chunk var12 = world.getChunk(pChunkX + xx, pChunkZ + zz);

                                if (var12 != null)
                                {

                                    chunkCount++;
                                    this.chunkArray[pChunkX + xx - this.chunkX][pChunkZ + zz - this.chunkZ] = var12;
                                }
                            }
                        }
                    }
                }
            }

            lastChunkCacheCount = chunkCount;

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("DimensionChunkCache crash, tell Corosus");
            lastChunkCacheCount = 0;
        }

    }

    @Override
    public TileEntity getBlockEntity(BlockPos pos) {
        int i = (pos.getX() >> 4) - this.chunkX;
        int j = (pos.getZ() >> 4) - this.chunkZ;
        if (i < 0 || i >= chunkArray.length || j < 0 || j >= chunkArray[i].length) return null;
        if (chunkArray[i][j] == null) return null;
        return this.chunkArray[i][j].getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;

            System.out.println("PFQUEUE FIX ME IM BROKEN");
            if (i < 0 || i >= chunkArray.length || j < 0 || i >= chunkArray[i].length) return Blocks.AIR.defaultBlockState();

            if (i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length)
            {
                Chunk chunk = this.chunkArray[i][j];

                if (chunk != null)
                {
                    return chunk.getBlockState(pos);
                }
            }
        }

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;

            if (i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length)
            {
                Chunk chunk = this.chunkArray[i][j];

                if (chunk != null)
                {
                    return chunk.getFluidState(pos);
                }
            }
        }

        return Fluids.EMPTY.defaultFluidState();
    }

}