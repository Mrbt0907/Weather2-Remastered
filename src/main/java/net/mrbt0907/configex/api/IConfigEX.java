package net.mrbt0907.configex.api;

import net.minecraft.util.ResourceLocation;

public interface IConfigEX
{
	public ResourceLocation getConfigID();
	public void onConifgChanged();
	public void onValueChanged(String configID, Object oldValue, Object newValue);
}