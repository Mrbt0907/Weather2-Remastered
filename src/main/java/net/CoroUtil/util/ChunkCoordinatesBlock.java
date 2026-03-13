package net.CoroUtil.util;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class ChunkCoordinatesBlock extends BlockCoord {

	public Block block;
	public int meta;


	public ChunkCoordinatesBlock(int par1, int par2, int par3, Block parBlockID, int parMeta)
    {
        super(par1, par2, par3);
        block = parBlockID;
        meta = parMeta;
    }

}