package net.CoroUtil.block;

import net.CoroUtil.config.ConfigCoroUtil;
import net.CoroUtil.config.ConfigCoroUtil;
import net.CoroUtil.forge.CULog;
import net.CoroUtil.forge.CommonProxy;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityRepairingBlock extends TileEntity
{

    private BlockState orig_blockState;
    private float orig_hardness = 1;
    private float orig_explosionResistance = 1;

    
    private long timeToRepairAt = 0;

    public TileEntityRepairingBlock(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void onLoad() {
        super.onLoad();


        
    }

    public void restoreBlock() {

        level.setBlockAndUpdate(this.worldPosition, orig_blockState);


        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos posFix = worldPosition.offset(x, y, z);
                    BlockState state = level.getBlockState(posFix);
                    if (state.getBlock() instanceof LeavesBlock) {
                        try {

                            level.setBlock(posFix, state.setValue(LeavesBlock.PERSISTENT, true), 4);
                        } catch (Exception ex) {

                            if (ConfigCoroUtil.useLoggingDebug) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }



    }

    public void setBlockData(BlockState state) {

        this.orig_blockState = state;
    }

    

    public static TileEntityRepairingBlock replaceBlockAndBackup(World world, BlockPos pos) {
        return replaceBlockAndBackup(world, pos, ConfigCoroUtil.ticksToRepairBlock);
    }

    
    public static TileEntityRepairingBlock replaceBlockAndBackup(World world, BlockPos pos, int ticksToRepair) {
        BlockState oldState = world.getBlockState(pos);
        float oldHardness = oldState.getDestroySpeed(world, pos);
        float oldExplosionResistance = 1;
        try {
            oldExplosionResistance = oldState.getBlock().getExplosionResistance(oldState, world, pos, null);
        } catch (Exception ex) {

        }

        world.setBlockAndUpdate(pos, CommonProxy.blockRepairingBlock.get().defaultBlockState());
        TileEntity tEnt = world.getBlockEntity(pos);
        if (tEnt instanceof TileEntityRepairingBlock) {
            BlockState state = world.getBlockState(pos);

            TileEntityRepairingBlock repairing = ((TileEntityRepairingBlock) tEnt);
            repairing.setBlockData(oldState);
            repairing.setOrig_hardness(oldHardness);
            repairing.setOrig_explosionResistance(oldExplosionResistance);
            repairing.timeToRepairAt = world.getGameTime() + ticksToRepair;

            return (TileEntityRepairingBlock) tEnt;
        } else {
            CULog.dbg("failed to set repairing block for pos: " + pos);
            return null;
        }
    }


    public void setOrig_hardness(float orig_hardness) {
        this.orig_hardness = orig_hardness;
    }

    public void setOrig_explosionResistance(float orig_explosionResistance) {
        this.orig_explosionResistance = orig_explosionResistance;
    }
}