package net.mrbt0907.weather2.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemSensor extends ItemBase
{
	private int type;
	
	public ItemSensor (int type)
	{
		this.type = type;
	}
	
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		ItemStack stack = playerIn.getHeldItem(handIn);
		
		if (!worldIn.isRemote && stack.getItem().equals(this))
		{
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null)
				nbt = new NBTTagCompound();
			nbt.setBoolean("enabled", !nbt.getBoolean("enabled"));
			stack.setTagCompound(nbt);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}
	
	public int getType()
	{
		return type;
	}
}
