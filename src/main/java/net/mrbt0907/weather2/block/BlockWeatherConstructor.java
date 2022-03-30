package net.mrbt0907.weather2.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import CoroUtil.util.CoroUtilMisc;

public class BlockWeatherConstructor extends BlockContainer
{
    public BlockWeatherConstructor()
    {
        super(Material.CLAY);
        setHardness(0.6F);
        setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileWeatherConstructor();
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    	
    	if (!world.isRemote && hand == EnumHand.MAIN_HAND)
    	{
	    	TileEntity tile = world.getTileEntity(pos);
	    	
	    	if (tile instanceof TileWeatherConstructor)
	    	{
	    		TileWeatherConstructor constructor = (TileWeatherConstructor) tile;
	    		constructor.cycleWeatherType(player.isSneaking());
	    		String msg = "Off";
	    		
	    		switch (constructor.weatherType)
	    		{
	    			case 1:
	    				msg = "Rainstorm";
	    				break;
	    			case 2:
	    				msg = "Thunderstorm";
	    				break;
	    			case 3:
	    				msg = "Supercell";
	    				break;
	    			case 4:
	    				msg = "Hailing Supercell";
	    				break;
	    			case 5:
	    				msg = "EF1 Tornado";
	    				break;
	    			case 6:
	    				msg = "Category 1 Hurricane";
	    				break;
	    		}
	    		
	    		CoroUtilMisc.sendCommandSenderMsg(player, "Weather Machine is now spawning a " + msg);
	    		return true;
	    	}
    	}
    	
    	return true;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        super.onBlockClicked(worldIn, pos, playerIn);
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
