package net.mrbt0907.weather2.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

public class BlockMachine extends BlockContainer
{

	public BlockMachine(Material material)
	{
		this(material, 0.6F, 10.0F);
	}
	
	public BlockMachine(Material material, float hardness, float resistance)
	{
		super(material);
        setHardness(hardness);
        setResistance(resistance);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileMachine();
	}
	
	@Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
}
