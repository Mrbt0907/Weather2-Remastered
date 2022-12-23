package net.mrbt0907.weather2.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemSensor extends ItemBase
{
	private int type;
	public boolean enabled;
	
	public ItemSensor (int type)
	{
		this.type = type;
	}
	
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		
		enabled = !enabled;
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
	}
	
	public int getType()
	{
		return type;
	}
}
