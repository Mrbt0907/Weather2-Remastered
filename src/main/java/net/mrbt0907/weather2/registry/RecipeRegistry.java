package net.mrbt0907.weather2.registry;

import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ConfigMisc;

import java.util.ArrayList;
import java.util.List;

public class RecipeRegistry implements IConditionBuilder
{
    public static void postInit()
    {
        Weather2.info("oh hell naw i will have to port recipes to json");
    }

}