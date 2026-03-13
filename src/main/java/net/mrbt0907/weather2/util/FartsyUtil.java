package net.mrbt0907.weather2.util;

public class FartsyUtil {

	public static float sqrtf(float x) {
	    if (x < 0f) return Float.NaN;
	    if (x == 0f || x == Float.POSITIVE_INFINITY) return x;

	    int i = Float.floatToIntBits(x);
	    i = (1 << 29) + (i >> 1) - (1 << 22);
	    float y = Float.intBitsToFloat(i);

	    y = 0.5f * (y + x / y);
	    y = 0.5f * (y + x / y);

	    return y;
	}
}
