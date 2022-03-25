package net.mrbt0907.weather2.weather.storm;

import net.minecraftforge.common.util.EnumHelper;
import net.mrbt0907.weather2.util.StringUtils;

public class WeatherEnum
{
	public static enum Type
	{
		CLOUD(0), RAIN(1), THUNDER(2), SUPERCELL(3), TORNADO(4, true),
		TROPICAL_DISTURBANCE(2), TROPICAL_DEPRESSION(3), TROPICAL_STORM(4, true), HURRICANE(5, true),
		SANDSTORM(4, true),
		BLIZZARD(4, true);
		
		private int stage = -1;
		private boolean isDangerous;
		
		Type(int stage)
		{
			this(stage, false);
		}
		
		Type(int stage, boolean isDangerous)
		{
			this.stage = stage;
			this.isDangerous = isDangerous;
		}
		
		public static Type add(Type weather_type, int stage, boolean isDangerous)
		{
			EnumHelper.addEnum(Type.class, weather_type.name(), new Class[] {Integer.class, Boolean.class}, stage, isDangerous);
			return weather_type;
		}
		
		public static Type get(int index)
		{
			return values()[index];
		}
		
		public static int size()
		{
			return values().length;
		}
		
		public int getStage()
		{
			return stage;
		}
		
		public boolean isDangerous()
		{
			return isDangerous;
		}
		
		@Override
		public String toString()
		{
			return StringUtils.toUpperCaseAlt(super.toString().replaceAll("\\_", " ").toLowerCase());
		}
	}
}
