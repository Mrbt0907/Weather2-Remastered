package net.mrbt0907.weather2.client.foliage;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class FoliageReplacerCrop extends FoliageReplacerBase {

    public FoliageReplacerCrop(BlockState state) {
        super(state);
    }

    @Override
    public boolean validFoliageSpot(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.GRASS
                && world.getBlockState(pos.above()).getBlock() == state.getBlock();
    }

    @Override
    public void addForPos(World world, BlockPos pos) {
        FoliageEnhancerShader.addForPos(this, expectedHeight, pos);
    }
}