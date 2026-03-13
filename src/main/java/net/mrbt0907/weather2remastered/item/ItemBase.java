package net.mrbt0907.weather2.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class ItemBase extends Item
{
    public ItemBase()
    {
        super(new Item.Properties().tab(ItemGroup.TAB_MISC));
    }

    public ItemBase(Item.Properties properties)
    {
        super(properties);
    }
}