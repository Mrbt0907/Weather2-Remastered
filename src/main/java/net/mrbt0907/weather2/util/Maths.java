package net.mrbt0907.weather2.util;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**A simple math library that can be used to simplify math related calculations*/
public class Maths
{
	private static Random random = new Random();
	
	/**Used to create 2D vectors for positioning calculations*/
	public static class Vec
	{
		/**Position X of this 2D vector*/
		public double posX = 0.0D;
		/**Position Z of this 2D vector*/
		public double posZ = 0.0D;
		
		/**Used to create 2D vectors for positioning calculations*/
		public Vec() {}
			/**Used to create 2D vectors for positioning calculations*/
		public Vec(CoroUtil.util.Vec3 pos)
		{
			this.posX = pos.xCoord;
			this.posZ = pos.zCoord;
		}
		/**Used to create 2D vectors for positioning calculations*/
		public Vec(BlockPos pos)
		{
			this.posX = pos.getX();
			this.posZ = pos.getZ();
		}
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
		
		public Vec copy()
		{
			return new Vec(posX, posZ);
		}
		
		public BlockPos toBlockPos(double posY)
		{
			return new BlockPos(posX, posY, posZ);
		}
		
		public CoroUtil.util.Vec3 toVec3Coro()
		{
			return new CoroUtil.util.Vec3(posX, 0.0D, posZ);
		}
		
		public net.minecraft.util.math.Vec3d toVec3MC()
		{
	    	return new net.minecraft.util.math.Vec3d(posX, 0.0D, posZ);
	    }
		
		public Vec addVector(double x, double z)
		{
			posX += x;
			posZ += z;
			return this;
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
		
		public double speed()
		{
			return Math.sqrt(posX * posX + posZ * posZ);
		}
	}
	
	/**Used to create 3D vectors for positioning calculations*/
	public static class Vec3
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
		public Vec3(Vec3d pos)
		{
			posX = pos.x;
			posY = pos.y;
			posZ = pos.z;
		}
		/**Used to create 3D vectors for positioning calculations*/
		public Vec3(CoroUtil.util.Vec3 pos)
		{
			posX = pos.xCoord;
			posY = pos.yCoord;
			posZ = pos.zCoord;
		}
		/**Used to create 3D vectors for positioning calculations*/
		public Vec3(BlockPos pos)
		{
			posX = pos.getX();
			posY = pos.getY();
			posZ = pos.getZ();
		}
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
		
		public BlockPos toBlockPos()
		{
			return new BlockPos(posX, posY, posZ);
		}
		
		public CoroUtil.util.Vec3 toVec3Coro()
		{
			return new CoroUtil.util.Vec3(posX, posY, posZ);
		}
		
		public net.minecraft.util.math.Vec3d toVec3MC()
		{
	    	return new net.minecraft.util.math.Vec3d(posX, posY, posZ);
	    }
		
		public Vec3 copy()
		{
			return new Vec3(posX, posY, posZ);
		}
		
		public Vec3 addVector(double x, double y, double z)
		{
			posX += x;
			posY += y;
			posZ += z;
			return this;
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
		
		public double speed()
		{
			return Math.sqrt(posX * posX + posY * posY + posZ * posZ);
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
	
	/**Calculates the distance between one set of positions and another set of positions*/
	public static double distance(double posXA, double posYA, double posZA, double posXB, double posYB, double posZB)
	{
		return Math.sqrt((posXA - posXB) * (posXA - posXB) + (posYA - posYB) * (posYA - posYB) + (posZA - posZB) * (posZA - posZB));
	}
	
	/**Calculates the distance between one set of positions and a 3d vector*/
	public static double distance(double posX, double posY, double posZ, Vec3 vector)
	{
		return Math.sqrt((posX - vector.posX) * (posX - vector.posX) + (posY - vector.posY) * (posY - vector.posY) + (posZ - vector.posZ) * (posZ - vector.posZ));
	}
	
	/**Calculates the distance between one set of positions and another set of positions*/
	public static double distance(double posXA, double posZA, double posXB, double posZB)
	{
		return Math.sqrt((posXA - posXB) * (posXA - posXB) + (posZA - posZB) * (posZA - posZB));
	}
	
	/**Calculates the distance between one set of positions and a 2d vector*/
	public static double distance(double posX, double posZ, Vec vector)
	{
		return Math.sqrt((posX - vector.posX) * (posX - vector.posX) + (posZ - vector.posZ) * (posZ - vector.posZ));
	}
	
	public static double speed(double motionX, double motionY, double motionZ)
	{
		return Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
	}
	
	/**Takes the provided integer and adjusts it closer to the target with the adjustment integer. Ensure that the adjustment number is positive!*/
	public static int adjust(int current, int target, int adjustment)
	{
		if (current > target)
		{
			current -= adjustment;
			if (current < target)
				return target;
		}
		else if (current < target)
		{
			current += adjustment;
			if (current > target)
				return target;
		}
		
		return current;
	}
	
	/**Takes the provided long and adjusts it closer to the target with the adjustment long. Ensure that the adjustment number is positive!*/
	public static long adjust(long current, long target, long adjustment)
	{
		if (current > target)
		{
			current -= adjustment;
			if (current < target)
				return target;
		}
		else if (current < target)
		{
			current += adjustment;
			if (current > target)
				return target;
		}
		
		return current;
	}
	
	/**Takes the provided short and adjusts it closer to the target with the adjustment short. Ensure that the adjustment number is positive!*/
	public static short adjust(short current, short target, short adjustment)
	{
		if (current > target)
		{
			current -= adjustment;
			if (current < target)
				return target;
		}
		else if (current < target)
		{
			current += adjustment;
			if (current > target)
				return target;
		}
		
		return current;
	}
	
	/**Takes the provided float and adjusts it closer to the target with the adjustment float. Ensure that the adjustment number is positive!*/
	public static float adjust(float current, float target, float adjustment)
	{
		if (current > target)
		{
			current -= adjustment;
			if (current < target)
				return target;
		}
		else if (current < target)
		{
			current += adjustment;
			if (current > target)
				return target;
		}
		
		return current;
	}
	
	/**Takes the provided double and adjusts it closer to the target with the adjustment double. Ensure that the adjustment number is positive!*/
	public static double adjust(double current, double target, double adjustment)
	{
		if (current > target)
		{
			current -= adjustment;
			if (current < target)
				return target;
		}
		else if (current < target)
		{
			current += adjustment;
			if (current > target)
				return target;
		}
		
		return current;
	}
}