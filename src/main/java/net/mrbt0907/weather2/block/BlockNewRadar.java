package net.mrbt0907.weather2.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

public class BlockNewRadar extends BlockMachine
{
	private int tier = 0;
	
    public BlockNewRadar()
    {
        super(Material.CLAY);
        setHardness(0.6F);
        setResistance(10.0F);
    }
    
    public BlockNewRadar(int tier)
    {
    	this();
    	this.tier = tier;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileRadar(tier);
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
    
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
    
	public int getTier()
	{
		return tier;
	}
    
    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
}
