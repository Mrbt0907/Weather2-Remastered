package net.mrbt0907.weather2.util;

import java.util.Random;

/**A simple math library that can be used to simplify math related calculations*/
public class Maths
{
	private static Random random = new Random();
	
	/**Used to create 2D vectors for positioning calculations*/
	public class Vec
	{
		/**Position X of this 2D vector*/
		public double posX = 0.0D;
		/**Position Z of this 2D vector*/
		public double posZ = 0.0D;
		
		/**Used to create 2D vectors for positioning calculations*/
		public Vec() {}
		/**Used to create 2D vectors for positioning calculations*/
		public Vec(int posX, int posZ)
		{
			this.posX = posX;
			this.posZ = posZ;
		}
		/**Used to create 2D vectors for positioning calculations*/
		public Vec(float posX, float posZ)
		{
			this.posX = posX;
			this.posZ = posZ;
		}
		/**Used to create 2D vectors for positioning calculations*/
		public Vec(double posX, double posZ)
		{
			this.posX = posX;
			this.posZ = posZ;
		}
		
		/**Calculates the distance between this 2D vector and another set of positions*/
		public double distance(double posX, double posZ)
		{
			return Math.sqrt((this.posX - posX) * (this.posX - posX) + (this.posZ - posZ) * (this.posZ - posZ));
		}
		
		/**Calculates the distance between this 2D vector and another 2D vector*/
		public double distance(Vec vector)
		{
			return distance(vector.posX, vector.posZ);
		}
		
		/**Calculates the distance between this 2D vector and another 3D vector. posY of the 3D vector is not used in the formula*/
		public double distance(Vec3 vector)
		{
			return distance(vector.posX, vector.posZ);
		}
	}
	
	/**Used to create 3D vectors for positioning calculations*/
	public class Vec3
	{
		/**Position X of this 3D vector*/
		public double posX = 0.0D;
		/**Position Y of this 3D vector*/
		public double posY = 0.0D;
		/**Position Z of this 3D vector*/
		public double posZ = 0.0D;

		/**Used to create 3D vectors for positioning calculations*/
		public Vec3() {}
		/**Used to create 3D vectors for positioning calculations*/
		public Vec3(int posX, int posY, int posZ)
		{
			this.posX = posX;
			this.posY = posY;
			this.posZ = posZ;
		}
		/**Used to create 3D vectors for positioning calculations*/
		public Vec3(float posX, float posY, float posZ)
		{
			this.posX = posX;
			this.posY = posY;
			this.posZ = posZ;
		}
		/**Used to create 3D vectors for positioning calculations*/
		public Vec3(double posX, double posY, double posZ)
		{
			this.posX = posX;
			this.posY = posY;
			this.posZ = posZ;
		}
		
		/**Calculates the distance between this 3D vector and another set of positions*/
		public double distance(double posX, double posY, double posZ)
		{
			return Math.sqrt((this.posX - posX) * (this.posX - posX) + (this.posY - posY) * (this.posY - posY) + (this.posZ - posZ) * (this.posZ - posZ));
		}
		
		/**Calculates the distance between this 3D vector and another 2D vector. The posY value for the 2D vector is equal to posY of this vector*/
		public double distance(Vec vector)
		{
			return distance(vector.posX, posY, vector.posZ);
		}
		
		/**Calculates the distance between this 3D vector and another 3D vector*/
		public double distance(Vec3 vector)
		{
			return distance(vector.posX, vector.posY, vector.posZ);
		}
	}
	
	/**Returns true 50% of the time this is ran.*/
	public static boolean chance()
	{
		return (random(1) == 1) ? true : false;
	}
	
	/**Returns true if a 0 through 100 random value is equal or greater than the given chance.
	1 = 1% Chance, 25 = 25% Chance, 100 = 100% Chance*/
	public static boolean chance(int chance)
	{
		return (random(0,100) <= chance) ? true : false;
	}
	
	/**Returns true if a 0.0F through 1.0F random value is equal or greater than the given chance.
	0.01F = 1% Chance, 0.25F = 25% Chance, 1.0F = 100% Chance*/
	public static boolean chance(float chance)
	{
		return (random(0.0F,1.0F) <= chance) ? true : false;
	}
	
	/**Returns true if a 0.0D through 1.0D random value is equal or greater than the given chance.
	0.01D = 1% Chance, 0.25D = 25% Chance, 1.0D = 100% Chance*/
	public static boolean chance(double chance)
	{
		return (random(0.0D,1.0D) <= chance) ? true : false;
	}
	
	/**Returns a random integer value from 0 through integerA*/
	public static int random(int integerA)
	{
		return random(0, integerA);
	}
	
	/**Returns a random integer value from integerA through integerB*/
	public static int random(int integerA, int integerB)
	{
		if (integerA >= integerB)
			return integerA;
		else
			return random.nextInt(integerB - integerA) + integerA;
	}
	
	/**Returns a random float value from 0.0F through floatA*/
	public static float random(float floatA)
	{
		return random(0.0F, floatA);
	}
	
	/**Returns a random float value from floatA through floatB*/
	public static float random(float floatA, float floatB)
	{
		if (floatA >= floatB)
			return floatA;
		else
			return (random.nextFloat() * (floatB - floatA)) + floatA;
	}
	
	/**Returns a random double value from 0.0D through doubleA*/
	public static double random(double doubleA)
	{
		return random(0, doubleA);
	}
	
	/**Returns a random double value from doubleA through doubleB*/
	public static double random(double doubleA, double doubleB)
	{
		if (doubleA >= doubleB)
			return doubleA;
		else
			return (random.nextDouble() * (doubleB - doubleA)) + doubleA;
	}
}