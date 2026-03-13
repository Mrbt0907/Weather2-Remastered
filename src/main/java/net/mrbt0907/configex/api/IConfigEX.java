package net.mrbt0907.configex.api;

public interface IConfigEX
{

	public String getName();

	public String getDescription();

	public default String getSaveLocation()
	{
		return getName();
	}

	public void onConfigChanged(Phase phase, int variables);

	public void onValueChanged(String variable, Object oldValue, Object newValue);
	
	public static enum Phase
	{
		START, END;
	}
}
