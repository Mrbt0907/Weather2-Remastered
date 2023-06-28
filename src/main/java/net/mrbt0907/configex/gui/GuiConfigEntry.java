package net.mrbt0907.configex.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.manager.FieldInstance;

@SideOnly(Side.CLIENT)
public class GuiConfigEntry
{
	private static final Minecraft MC = Minecraft.getMinecraft();
	public final String categoryName;
	public final String name;
	public final String registryName;
	public final String comment;
	public final GuiBetterTextField textField;
	public final String value;
	public final String defaultValue;
	public final int type;
	public final double min;
	public final double max;
	public final boolean hasPermission;
	public final boolean showMin;
	public final boolean showMax;
	
	public GuiConfigEntry(FieldInstance field, boolean serverValue)
	{
		categoryName = ConfigManager.formatRegistryName(field.config.getName());
		name = field.displayName;
		registryName = field.registryName;
		comment = field.comment;
		value = String.valueOf(serverValue ? field.getServerValue() : field.hasPermission() ? field.getRealClientValue() : field.getClientValue());
		defaultValue = String.valueOf(field.defaultValue);
		textField = new GuiBetterTextField(MC.fontRenderer, 0, 0, 130, 16, value, defaultValue);
		textField.isEnabled = field.hasPermission();
		hasPermission = field.hasPermission();
		textField.setVisible((!(!hasPermission && field.hide) || hasPermission) && (field.enforce && (GuiConfigEditor.serverMode || ConfigManager.isSinglePlayer()) || !field.enforce));
		type = field.type;
		min = field.min;
		max = field.max;
		showMin = field.showMin;
		showMax = field.showMax;
	}
	
	@SideOnly(Side.CLIENT)
	public void initButton()
	{
		textField.setText(value);
	}
}
