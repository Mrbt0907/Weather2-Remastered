package net.CoroUtil.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class UtilMining {

    

    public static List<BlockState> listBlocksBlacklistedRepairing = new ArrayList<>();
    public static List<BlockState> listTileEntitiesWhitelistedBreakable = new ArrayList<>();
    public static class ClientData {
        public static List<BlockState> listBlocksBlacklistedRepairing = new ArrayList<>();
        public static List<BlockState> listTileEntitiesWhitelistedBreakable = new ArrayList<>();
    }

    
    public static boolean canConvertToRepairingBlockNew(World world, BlockPos pos, boolean isClient) {
        boolean isTileEntity = world.getBlockEntity(pos) != null;



        if (!isTileEntity) {
            return !isBlockBlacklistedFromRepairingBlockNonTileEntity(world, pos, isClient);
        }

        return false;
    }

    public static boolean isBlockBlacklistedFromRepairingBlockNonTileEntity(World world, BlockPos pos, boolean client) {

        BlockState state = world.getBlockState(pos);

        return CoroUtilBlockState.partialStateInListMatchesFullState(state, client ? ClientData.listBlocksBlacklistedRepairing : listBlocksBlacklistedRepairing);
    }



    @Deprecated
    public static boolean canConvertToRepairingBlock(World world, BlockState state) {

        if (state.getMaterial() == Material.GLASS) {
            return true;
        }



        
        if (!state.isCollisionShapeFullBlock(world, BlockPos.ZERO)) {
            return false;
        }
        return true;
    }

    @Deprecated
    public static boolean canMineBlock(World world, BlockCoord pos, Block block) {
        return canMineBlock(world, pos.toBlockPos(), block);
    }

    @Deprecated
    public static boolean canMineBlock(World world, BlockPos pos, Block block) {
        return canMineBlock(world, pos);
    }

    @Deprecated
    public static boolean canMineBlock(World world, BlockPos pos) {


        BlockState state = world.getBlockState(pos);


    
        if (state.getMaterial().isLiquid()) {
            return false;
        }

        return true;
    }

}