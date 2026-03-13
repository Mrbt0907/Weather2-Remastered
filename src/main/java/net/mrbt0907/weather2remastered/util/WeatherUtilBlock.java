package net.mrbt0907.weather2remastered.util;

import java.util.ArrayList;
import java.util.List;
<<<<<<< Updated upstream:src/main/java/net/mrbt0907/weather2remastered/util/WeatherUtilBlock.java

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.mrbt0907.weather2remastered.Weather2Remastered;
import net.mrbt0907.weather2remastered.api.WeatherAPI;
import net.mrbt0907.weather2remastered.api.weather.AbstractStormObject;
import net.mrbt0907.weather2remastered.block.BlockSandLayer;
import net.mrbt0907.weather2remastered.config.ConfigGrab;
import net.mrbt0907.weather2remastered.registry.BlockRegistry;
import net.mrbt0907.weather2remastered.util.Maths.Vec3;
import net.minecraft.world.gen.Heightmap;

public class WeatherUtilBlock
{
	public static int layerableHeightPropMax = 8;
	
	public static boolean safeReplaceCheck(BlockState state, World world, BlockPos pos)
	{
		try
		{
			return safeReplaceCheck(state, world, pos);
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public static void fillAgainstWallSmoothly(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable) {
		fillAgainstWallSmoothly(world, posSource, directionYaw, scanDistance, fillRadius, blockLayerable, 4);
	}

	public static boolean isAir(Block block) {
			return (block != null ? block.defaultBlockState().getMaterial() == Material.AIR : true);
	}

	public static void fillAgainstWallSmoothly(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable, int heightDiff)
	{
		/**
		 * for now, work in halves
		 * if "wall" is 4 height (aka 8 pixels high) or less, we can "go over it" aka continue onto next block past it
		 * 
		 * starting point needs to be air above solid
		 * 
		 * scan forward till not air or not placeable
		 * 
		 * get block height, if height < 4
		 * - place infront of wall
		 * if height >= 4
		 * - progress onto it and continue past it
		 * 
		 * 
		 * 
		 * - factor in height of current block we are on if its not air, aka half filled sand block vs next block
		 */
		
		//fix for starting on a layerable block
		BlockState stateTest = ChunkUtils.getBlockState(world, posSource.toBlockPos());
		if (stateTest.getBlock() == blockLayerable)
		{
			int heightTest = getHeightForAnyBlock(stateTest);
			if (heightTest < 8) {}
		}
		
		BlockPos posSourcei = posSource.toBlockPos();
		//int ySource = world.getHeight(posSourcei).getY();
		int y = posSourcei.getY();
		float tickStep = 0.75F;
		
		//float startScan = scanDistance;
		
		Vec3 posLastNonWall = posSource.copy();
		Vec3 posWall = null;
		
		BlockPos lastScannedPosXZ = null;//new BlockPos(posSourcei);
		
		//System.out.println("Start block (should be air): " + ChunkUtils.getBlockState(world, posSourcei));
		
		int previousBlockHeight = 0;
		
		//looking for a proper wall we cant fly over as sand
		for (float i = 0; i < scanDistance; i += tickStep) {
			double vecX = (-Maths.fastSin(Math.toRadians(directionYaw)) * (i));
    		double vecZ = (Maths.fastCos(Math.toRadians(directionYaw)) * (i));
    		
    		int x = MathHelper.floor(posSource.posX + vecX);
    		int z = MathHelper.floor(posSource.posZ + vecZ);
    		
    		BlockPos pos = new BlockPos(x, y, z);
    		BlockPos posXZ = new BlockPos(x, 0, z);
    		BlockState state = ChunkUtils.getBlockState(world, pos);
    		
    		if (lastScannedPosXZ == null || !posXZ.equals(lastScannedPosXZ)) {
    		
	    		lastScannedPosXZ = new BlockPos(posXZ);

				AxisAlignedBB aabbCompare = new AxisAlignedBB(pos);
				// Get the VoxelShape of the block's collision
			    VoxelShape shape = state.getCollisionShape(world, pos);
				List<AxisAlignedBB> listAABBCollision = new ArrayList<>();
				//state.addCollisionBoxToList(world, pos, aabbCompare, listAABBCollision, null, false);
				shape.toAabbs().stream().map(box -> box.move(pos)).filter(box -> box.intersects(aabbCompare)).forEach(listAABBCollision::add);
				//if solid ground we can place on
	    		if (state.getMaterial() != Material.AIR && state.getMaterial() != Material.PLANT && !safeReplaceCheck(state, world, pos) && !listAABBCollision.isEmpty())
	    		{
	    			BlockPos posUp = new BlockPos(x, y + 1, z);
	    			BlockState stateUp = ChunkUtils.getBlockState(world, posUp);
					//if above it is air
	    			if (stateUp.getMaterial() == Material.AIR) {
		    			int height = getHeightForAnyBlock(state);
		    			
		    			//if height of block minus block we are on/comparing against is short enough, we can continue onto it
		    			if (height - previousBlockHeight <= heightDiff) {
		    				//if block we are progressing to is a full block, reset height val
		    				if (height == 8) {
		    					previousBlockHeight = 0;
			    				y++;
		    				} else {
		    					previousBlockHeight = height;
		    				}
		    				
		    				posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
		    				
		    				//System.out.println(posLastNonWall);
		    				
		    				continue;
		    			//too high, count as a wall
		    			} else {
		    				posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
		    				break;
		    			}
		    		//hit a wall
	    			} else {
	    				posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
	    				break;
	    			}
	    			
	    			//startScan = i;
	    			//posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
	    			//break;
	    		} else {
	    			posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
	    		}
	    		
    		} else {
    			continue;
    		}
		}
		
		if (posWall != null) {
			int amountWeHave = 1;
			int amountToAddPerXZ = 1;
			
			BlockState state = ChunkUtils.getBlockState(world, posWall.toBlockPos());
			BlockState state1 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(1, 0, 0));
			BlockState state22 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(-1, 0, 0));
			BlockState state3 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(0, 0, 1));
			BlockState state4 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(0, 0, -1));

			//check all around place spot for cactus and cancel if true, to prevent cactus pop off when we place next to it
			if (state.getBlock() == Blocks.CACTUS || state1.getBlock() == Blocks.CACTUS ||
					state22.getBlock() == Blocks.CACTUS || state3.getBlock() == Blocks.CACTUS || state4.getBlock() == Blocks.CACTUS) {
				return;
			}
			
			BlockPos pos2 = new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ);
			BlockState state2 = ChunkUtils.getBlockState(world, pos2);
			if (state2.getMaterial() == Material.WATER || state2.getMaterial() == Material.LAVA) {
				return;
			}
			
			amountWeHave = trySpreadOnPos2(world, new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ), amountWeHave, amountToAddPerXZ, 10, blockLayerable);
		} else {
			//System.out.println("no wall found");
		}
	}
	
	public static void fillAgainstWall(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable) {
		//want to use this variable for how much the fill up spreads out to neighboring blocks
		//float thickness = 1F;
		float tickStep = 0.75F;
		//int fillPerTick = amountToTakeOrFill;
		//use snow for now, make sand block after
		
		//snow has 7 layers till its a full solid block (full solid on 8th layer)
		//0 is nothing, 1-7, 8 is full
		
		BlockPos posSourcei = posSource.toBlockPos();
		int ySource = WeatherUtilBlock.getPrecipitationHeightSafe(world, posSourcei).getY();
		int y = ySource;
		
		//override y so it scans from where ground at coord is
		y = (int) posSource.posY;
		//for sandstorm we might want to scan upwards in some scenarios.... but what
		
		@SuppressWarnings("unused")
		float startScan = scanDistance;
		
		Vec3 posLastNonWall = posSource.copy();
		Vec3 posWall = null;
		
		//scan outwards to find closest wall
		for (float i = 0; i < scanDistance; i += tickStep) {
			double vecX = (-Maths.fastSin(Math.toRadians(directionYaw)) * (i));
    		double vecZ = (Maths.fastCos(Math.toRadians(directionYaw)) * (i));
    		
    		int x = MathHelper.floor(posSource.posX + vecX);
    		int z = MathHelper.floor(posSource.posZ + vecZ);
    		
    		BlockPos pos = new BlockPos(x, y, z);
    		BlockState state = ChunkUtils.getBlockState(world, pos);
    		
    		if (state.getMaterial() != Material.AIR) {
    			startScan = i;
    			posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
    			break;
    		} else {
    			posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
    		}
		}
		
		//double distFromSourceToWall = posSource.distanceTo(posLastNonWall);
		
		//new code, check wall is high enough
		if (posWall != null) {
			BlockPos posCheck = new BlockPos(posWall.toBlockPos());
			int heightOfWall = 0;
			int heightNeeded = 2;
			while (heightOfWall++ < heightNeeded) {
				posCheck = posCheck.offset(0, 1, 0);
				BlockState stateCheck = ChunkUtils.getBlockState(world, posCheck);
				if (!stateCheck.isFaceSturdy(world, posCheck, Direction.UP)) {
					break;
				}
			}
			
			if (heightOfWall >= heightNeeded) {
				int amountWeHave = 4;
				int amountToAddPerXZ = 2;
				
				amountWeHave = trySpreadOnPos2(world, new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ), amountWeHave, amountToAddPerXZ, 10, blockLayerable);
			}
		}
		
		
	}

	public static void floodAreaWithLayerableBlock(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius/*, float takeRadius*/, Block blockLayerable, int amountToTakeOrFill) {
		floodAreaWithLayerableBlock(world, posSource, directionYaw, scanDistance, fillRadius, -1, blockLayerable/*, false*/, amountToTakeOrFill);
	}
	
	/**
	 * Fill direction up with a block, as if flowing particles filled the area up
	 * 
	 * Optional takeRadius param, if -1, dont use, otherwise it will 
	 * 
	 * - calculate endpoint and try to fill that up first, the propegate back
	 * - only propegate back if theres still more to fill, however, with new take radius, make sure not to just refil where we took from
	 * 
	 * Its possible that scanDistance is either not far enough or cant scan far enough
	 * - if low, fillRadius might fill into area that takeRadius processed
	 * -- possibly detect this state and prevent method from running and eating cpu
	 * 
	 * - I guess have method still scan ahead first:
	 * -- if scanDistance effective > fillRadius + takeRadius
	 * --- process
	 * -- if not
	 * --- abort, or maybe just do single block and not full radial mode, for more fine processing
	 * ---- do this only if posSource != posSource + scanDistance effective found pos
	 * 
	 */
	public static void floodAreaWithLayerableBlock(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, float takeRadius, Block blockLayerable, int amountToTakeOrRelocate/*, boolean transferSandEqually*/) {
		//want to use this variable for how much the fill up spreads out to neighboring blocks
		//float thickness = 1F;
		float tickStep = 0.75F;
		//int fillPerTick = amountToTakeOrFill;
		//use snow for now, make sand block after
		
		//snow has 7 layers till its a full solid block (full solid on 8th layer)
		//0 is nothing, 1-7, 8 is full
		
		BlockPos posSourcei = posSource.toBlockPos();
		int ySource = WeatherUtilBlock.getPrecipitationHeightSafe(world, posSourcei).getY();
		int y = ySource;
		
		//override y so it scans from where ground at coord is
		y = (int) posSource.posY;
		//for sandstorm we might want to scan upwards in some scenarios.... but what
		
		@SuppressWarnings("unused")
		float startScan = scanDistance;
		
		Vec3 posLastNonWall = posSource.copy();
		@SuppressWarnings("unused")
		Vec3 posWall = null;
		
		//scan outwards to find closest wall
		for (float i = 0; i < scanDistance; i += tickStep) {
			double vecX = (-Maths.fastSin(Math.toRadians(directionYaw)) * (i));
    		double vecZ = (Maths.fastCos(Math.toRadians(directionYaw)) * (i));
    		
    		int x = MathHelper.floor(posSource.posX + vecX);
    		int z = MathHelper.floor(posSource.posZ + vecZ);
    		
    		BlockPos pos = new BlockPos(x, y, z);
    		BlockState state = ChunkUtils.getBlockState(world, pos);
    		
    		if (state.getMaterial() != Material.AIR) {
    			startScan = i;
    			posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
    			break;
    		} else {
    			posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
    		}
		}
		
		double distFromSourceToWall = posSource.distanceSq(posLastNonWall);

		boolean doRadius = true;
		
		/**
		 * If in take mode, if distance between determined fill point and where we will take from is overlapping (close), dont use radial to prevent redundant take then fill
		 */
		if (takeRadius != -1) {
			if (distFromSourceToWall <= 2) {
				doRadius = false;
			}
		}
		
		//make dynamic depending on dist, see particle code for algo
		
		
		/**
		 * Scan in a pattern that sand would spread in IRL
		 * needs to scan in an arc, 360, cant assume we actually hit wall, but scanning will avoid filling up a wall of course
		 * - hit wall, spread dist 1 block, scan forward
		 * - scan left and right of decreasing angle
		 * - after full angle scan, repeat with larger block dist, and smaller angle jump amount to account for distance from center (use even particle spread algo for that)
		 * - 
		 * 
		 * still needs code to support dropping sand down on lower blocks
		 */
		
		float angleScanResolution = 1;
		float spreadDist = fillRadius;
		//int amountToFill = amountToTakeOrRelocate;
		int maxFallDist = 20;
		
		int amountToTakePerXZ = 2;
		int amountToAddPerXZ = 2;
		
		int amountWeHave = 0;
		if (takeRadius != -1) {
			amountWeHave = tryTakeFromPos(world, posSourcei, amountWeHave, amountToTakePerXZ, maxFallDist, blockLayerable);
		} else {
			amountWeHave = amountToTakeOrRelocate;
		}
		
		//prevents trying to add sand to same position twice due to how trig code rounds to nearest block coord
		List<BlockPos> listProcessedFilter = new ArrayList<BlockPos>();
		
		//TODO: radius for taking
		if (doRadius) {
			for (float i = 1; i < takeRadius/* && amountWeHave > 0*/; i += 0.75F) {
				
				//int amountToAddBasedOnDist = 2;
				
				//radial
				for (float angle = 0; angle <= 180/* && amountWeHave > 0*/; angle += angleScanResolution) {
					
					//left/right
					for (int mode = 0; mode <= 1/* && amountWeHave > 0*/; mode++) {
						
						float orientationMulti = 1F;
						if (mode == 1) {
							orientationMulti = -1F;
						}
						double vecX = (-Maths.fastSin(Math.toRadians(directionYaw - (angle * orientationMulti))) * (i));
			    		double vecZ = (Maths.fastCos(Math.toRadians(directionYaw - (angle * orientationMulti))) * (i));
			    		
			    		int x = MathHelper.floor(posSource.posX + vecX);
			    		int z = MathHelper.floor(posSource.posZ + vecZ);
			    		
			    		//fix for derp y
			    		y = (int)posSource.posY;
			    		
			    		BlockPos pos = new BlockPos(x, y, z);
			    		
			    		//IBlockState state = ChunkUtils.getBlockState(world, pos);
			    		if (!listProcessedFilter.contains(pos)) {
			    			listProcessedFilter.add(pos);
			    			//amountWeHave = trySpreadOnPos2(world, pos, amountWeHave, amountToAddBasedOnDist, maxFallDist, blockLayerable);
			    			amountWeHave = tryTakeFromPos(world, pos, amountWeHave, amountToTakePerXZ, maxFallDist, blockLayerable);
			    		}
					}
				}
			}
		}
		
		listProcessedFilter.clear();
		//TEMP OVERRIDE!!!! set pos to player
		//posLastNonWall = posSource;
		
		amountWeHave = trySpreadOnPos2(world, new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ), amountWeHave, amountToAddPerXZ, maxFallDist, blockLayerable);
		
		//TEMP!!!!
		//doRadius = false;
		
		//distance
		if (doRadius) {
			for (float i = 1; i < spreadDist && amountWeHave > 0; i += 0.75F) {
				
				//int amountToAddBasedOnDist = (int) (((float)snowMetaMax / spreadDist) * (float)i);
				
				/**
				 * for making it add less sand to each block the more distant it is from where the sand "landed"
				 * TODO: make this formula not suck for other spreadDist sizes, currently manually tweaked
				 */
				int amountToAddBasedOnDist = (int) (((float)layerableHeightPropMax+1F) - (i*1.5F));
				if (amountToAddBasedOnDist < 1) amountToAddBasedOnDist = 1;
				
				//temp
				amountToAddBasedOnDist = 2;
				
				//radial
				for (float angle = 0; angle <= 180 && amountWeHave > 0; angle += angleScanResolution) {
					
					//left/right
					for (int mode = 0; mode <= 1 && amountWeHave > 0; mode++) {
						
						float orientationMulti = 1F;
						if (mode == 1) {
							orientationMulti = -1F;
						}
						double vecX = (-Maths.fastSin(Math.toRadians(directionYaw - (angle * orientationMulti))) * (i));
			    		double vecZ = (Maths.fastCos(Math.toRadians(directionYaw - (angle * orientationMulti))) * (i));
			    		
			    		int x = MathHelper.floor(posLastNonWall.posX + vecX);
			    		int z = MathHelper.floor(posLastNonWall.posZ + vecZ);
			    		
			    		//fix for derp y
			    		y = (int)posLastNonWall.posY;
			    		
			    		BlockPos pos = new BlockPos(x, y, z);
			    		
			    		//scan a bit higher than positions for better result
			    		Vector3d sourceTest = posSource.addVector(0, 1D, 0).toVec3MC();
			    		Vector3d destTest = new Vector3d(x + 0.5F, y + 1.5F, z + 0.5F);
			    		
			    		//RayTraceResult destFound = world.rayTraceBlocks(sourceTest, destTest, false);
			    		BlockRayTraceResult destFound = world.clip(new RayTraceContext(sourceTest, destTest, BlockMode.COLLIDER, FluidMode.NONE, null));
			    		if (destFound == null) {
				    		//IBlockState state = ChunkUtils.getBlockState(world, pos);
				    		if (!listProcessedFilter.contains(pos)) {
				    			listProcessedFilter.add(pos);
				    			amountWeHave = trySpreadOnPos2(world, pos, amountWeHave, amountToAddBasedOnDist, maxFallDist, blockLayerable);
				    		}
			    		}
					}
				}
			}
		}
		
		Weather2Remastered.debug("leftover: " + amountWeHave);
	}
	
	public static int tryTakeFromPos(World world, BlockPos posTakeFrom, int amount, int amountAllowedToTakeForXZ, int maxDropAllowed, Block blockLayerable) {
		
		int amountTaken = 0;
		
		BlockState statePos = ChunkUtils.getBlockState(world, posTakeFrom);
		
		//
		if (!isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable) && statePos.getBlock() != Blocks.AIR) {
			return amount;
		}
		
		int dropDist = 0;
		BlockPos posScan = new BlockPos(posTakeFrom);
		while (statePos.getBlock() == Blocks.AIR && dropDist++ < maxDropAllowed) {
			posScan = posScan.offset(0, -1, 0);
			statePos = ChunkUtils.getBlockState(world, posScan);
			if (!isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable) && statePos.getBlock() != Blocks.AIR) {
				return amount;
			}
		}
		
		//statePos should now be blockLayerable type
		int amountLeftToTake = amountAllowedToTakeForXZ;
		while (amountTaken < amountAllowedToTakeForXZ) {
			int amountReturn = takeHeightFromLayerableBlock(world, posScan, blockLayerable/*statePos.getBlock()*/, amountLeftToTake);
			amountTaken += amountReturn;
			posScan = posScan.offset(0, -1, 0);
			statePos = ChunkUtils.getBlockState(world, posScan);
			//see if we can continue to take more
			if (!isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable)) {
				break;
			}
		}
		
		//return amount we could take + original fed in for additiveness
		return amount + amountTaken;
		
	}
	
	public static int trySpreadOnPos2(World world, BlockPos posSpreadTo, int amount, int amountAllowedToAdd, int maxDropAllowed, Block blockLayerable) {
		
		if (amount <= 0) return amount;
		
		/**
		 * - check pos for solid
		 * - if air, tick down till not air or drop limit
		 * - at first non air, find first block with up face solid or snow block
		 * - set air to everything between not air and up face solid or snow block (2 high tall grass removal)
		 * - 
		 * - run code that sets snow, deals with solid up face or existing snow, fully or partially layered
		 * 
		 * 
		 */
		
		//must have clear air above first spots
		//TODO: might need special case so we can fill up a partially layered snow block
		if (ChunkUtils.getBlockState(world, posSpreadTo.offset(0, 1, 0)).getMaterial() != Material.AIR) {
			return amount;
		}
		
		//IBlockState statePos = ChunkUtils.getBlockState(world, posSpreadTo);
		
		BlockPos posCheckNonAir = new BlockPos(posSpreadTo);
		BlockState stateCheckNonAir = ChunkUtils.getBlockState(world, posCheckNonAir);
		
		int depth = 0;
		
		//find first non air
		while (stateCheckNonAir.getMaterial() == Material.AIR) {
			posCheckNonAir = posCheckNonAir.offset(0, -1, 0);
			stateCheckNonAir = ChunkUtils.getBlockState(world, posCheckNonAir);
			depth++;
			//bail if drop too far, aka sand/snow fully particleizes
			if (depth > maxDropAllowed) {
				return amount;
			}
		}
		
		BlockPos currentPos = new BlockPos(posCheckNonAir);
		BlockState currentState = ChunkUtils.getBlockState(world, currentPos);

		int distForPlaceableBlocks = 0;

		while (distForPlaceableBlocks < 10) {
		    final BlockPos posCheckPlaceable = currentPos; // final per iteration for lambdas

		    AxisAlignedBB aabbCompare = new AxisAlignedBB(posCheckPlaceable);
		    List<AxisAlignedBB> listAABBCollision = new ArrayList<>();

		    VoxelShape shape = currentState.getCollisionShape(world, posCheckPlaceable);
		    shape.toAabbs().stream()
		         .map(box -> box.move(posCheckPlaceable))
		         .filter(box -> box.intersects(aabbCompare))
		         .forEach(listAABBCollision::add);

		    if (currentState.getBlock() != blockLayerable
		            && safeReplaceCheck(currentState, world, posCheckPlaceable)
		            && listAABBCollision.isEmpty()) {
		        // Move position down
		        currentPos = currentPos.offset(0, -1, 0);
		        currentState = ChunkUtils.getBlockState(world, currentPos);
		        distForPlaceableBlocks++;
		        continue;
		    } else if (currentState.isFaceSturdy(world, currentPos, Direction.UP)
		            || currentState.getBlock() == blockLayerable) {
		        break;
		    } else {
		        // Can't stack on this block type
		        return amount;
		    }
		}
		
		//for some reason theres 10+ blocks of half solid blocks, lets just abort
		if (distForPlaceableBlocks >= 10) {
			return amount;
		}
		
		//at this point the block we are about to work with is solid facing up, or snow
		if (!currentState.isFaceSturdy(world, currentPos, Direction.UP) && 
					currentState.getBlock() != blockLayerable) {
			Weather2Remastered.error("sandstorm: shouldnt be, failed a check somewhere!");
			return amount;
		}
		
		//lets clear out the blocks we found between air and solid or snow block
		for (int i = 0; i < distForPlaceableBlocks; i++) {
			ChunkUtils.setBlockState(world, posCheckNonAir.offset(0, -i, 0), Blocks.AIR.defaultBlockState());
		}
		
		BlockPos posPlaceLayerable = new BlockPos(currentPos);
		BlockState statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable);
		
		int amountToAdd = amountAllowedToAdd;
		
		//add in the amount of air blocks we found
		//distForPlaceableBlocks += depth;
		
		//just place while stuff to add and air above
		
		while (amountAllowedToAdd > 0 && ChunkUtils.getBlockState(world, posPlaceLayerable.offset(0, 1, 0)).getMaterial() == Material.AIR) {
			//if no more layers to add
			if (amountAllowedToAdd <= 0) {
				break;
			}
			//if its snow we can add snow to
			if (statePlaceLayerable.getBlock() == blockLayerable && getHeightForLayeredBlock(statePlaceLayerable) < layerableHeightPropMax) {
				int height = getHeightForLayeredBlock(statePlaceLayerable);
				//if (height < snowMetaMax) {
					height += amountAllowedToAdd;
					if (height > layerableHeightPropMax) {
						amountAllowedToAdd = height - layerableHeightPropMax;
						height = layerableHeightPropMax;
						
					} else {
						amountAllowedToAdd = 0;
					}
					try {
						ChunkUtils.setBlockState(world, posPlaceLayerable, setBlockWithLayerState(blockLayerable, height));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					//if we maxed it, up the val
					if (height == layerableHeightPropMax) {
						posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
						statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable);
					}
				//}
			//solid block------------------- or air because we moved up 1 due to the previous being fully filled snow
			} else if (statePlaceLayerable.isFaceSturdy(world, posPlaceLayerable, Direction.UP)) {
				posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
				statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable);
			//air
			} else if (statePlaceLayerable.getMaterial() == Material.AIR) {
				//copypasta, refactor/reduce once things work
				int height = amountAllowedToAdd;
				if (height > layerableHeightPropMax) {
					amountAllowedToAdd = height - layerableHeightPropMax;
					height = layerableHeightPropMax;
					
				} else {
					amountAllowedToAdd = 0;
				}
				try {
					ChunkUtils.setBlockState(world, posPlaceLayerable, setBlockWithLayerState(blockLayerable, height));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//if we maxed it, up the val
				if (height == layerableHeightPropMax) {
					posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
					statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable);
				}
			} else {
				Weather2Remastered.debug("wat! - " + statePlaceLayerable);
			}
		}
		
		if (amountAllowedToAdd < 0) {
			Weather2Remastered.debug("wat");
		}
		int amountAdded = amountToAdd - amountAllowedToAdd;
		amount -= amountAdded;
		return amount;
		
	}
	
	/**
	 * Checks if its the block layerable we want, or the vanilla 'full' height amount of it
	 * 
	 * @param state
	 * @param blockLayerable
	 * @return
	 */
	public static boolean isLayeredOrVanillaVersionOfBlock(BlockState state, Block blockLayerable) {
		Block block = state.getBlock();
		if (block == blockLayerable) {
			return true;
		}
		if (blockLayerable == BlockRegistry.sand_layer && block == Blocks.SAND) {
			return true;
		}
		if (blockLayerable == Blocks.SNOW && block == Blocks.SNOW_BLOCK) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static int getHeightForAnyBlock(BlockState state)
	{
		Block block = state.getBlock();
		if (block == Blocks.SNOW)
			return ((Integer)state.getValue(SnowBlock.LAYERS)).intValue();
		else if (block == BlockRegistry.sand_layer)
			return ((Integer)state.getValue(BlockSandLayer.LAYERS)).intValue();
		else if (block == Blocks.SAND || block == Blocks.SNOW)
			return 8;
		else if (block instanceof SlabBlock)
			return 4;
		else if (block == Blocks.AIR)
			return 0;
		else
			return 8;
	}
	
	public static int getHeightForLayeredBlock(BlockState state)
	{
		Weather2Remastered.warn("Checking height for layered block with missing sand_layer block!");
		if (state.getBlock() == Blocks.SNOW)
			return state.getValue(SnowBlock.LAYERS);
		//else if (state.getBlock() == BlockRegistry.sand_layer)
			//return ((Integer)state.getValue(BlockSandLayer.LAYERS)).intValue();
		else if (state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.SNOW)
			return 8;
		else
			return 0;
	}
	
	public static BlockState setBlockWithLayerState(Block block, int height)
	{
		if (block == Blocks.SNOW)
		{
			if (height == layerableHeightPropMax)
				return Blocks.SNOW.defaultBlockState();
			else
				return Blocks.SNOW.defaultBlockState().setValue(SnowBlock.LAYERS, height);
		}
		/*else if (block == BlockRegistry.sand_layer)
		{
			if (height == layerableHeightPropMax)
				return Blocks.SAND.getDefaultState();
			else
				return block.getDefaultState().withProperty(BlockSandLayer.LAYERS, height);
		}*/
		else
			//means missing implementation
			return null;
	}
	
	/**
	 * This method is bad, logic like this is difficult to do
	 * 
	 * @param state
	 * @param worldIn
	 * @param pos
	 * @param blockIn
	 * @return
	 */
	public static boolean divideToNeighborCheck(BlockState state, World worldIn, BlockPos pos, Block blockIn) {
		boolean foundSpotToSpread = false;
		
		int heightToUse = getHeightForLayeredBlock(state);

		if (heightToUse > 2) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
	            BlockPos posCheck = pos.relative(direction);
	            BlockState stateCheck = worldIn.getBlockState(posCheck);

	            int addAmount = 1;

	            if (stateCheck.getBlock() == state.getBlock()) {
	                int heightCheck = getHeightForLayeredBlock(stateCheck);
	                if (heightCheck + 2 <= heightToUse) {
	                    heightToUse -= addAmount;
	                    addHeightToLayerableBLock(worldIn, posCheck, stateCheck.getBlock(), heightCheck, addAmount);
	                    foundSpotToSpread = true;
	                }

	            // Optional: spreading into air blocks (commented-out part)
	            /*} else if (stateCheck.isAir(world, posCheck)) {
	                int returnVal = trySpreadOnPos2(world, posCheck, addAmount, addAmount, 10, stateCheck.getBlock());
	                if (returnVal == 0) {
	                    heightToUse -= addAmount;
	                    foundSpotToSpread = true;
	                }*/
	            }
	        }
	    }

	    if (foundSpotToSpread) {
	        worldIn.setBlock(pos, setBlockWithLayerState(state.getBlock(), heightToUse), 3);
	    }

	    return foundSpotToSpread;
	}
	
	/**
	 * Simple helper method, returns amount it couldnt add
	 * @param world
	 * @param pos
	 * @param block
	 * @param amount
	 * @return
	 */
	public static int addHeightToLayerableBLock(World world, BlockPos pos, Block block, int sourceAmount, int amount) {
		//IBlockState state = ChunkUtils.getBlockState(world, pos);
		int curAmount = sourceAmount;//getHeightForLayeredBlock(state);
		curAmount += amount;
		int leftOver = 0;
		if (curAmount > layerableHeightPropMax) {
			leftOver = curAmount - layerableHeightPropMax;
			curAmount = layerableHeightPropMax;
		} else {
			leftOver = 0;
		}
		try {
			ChunkUtils.setBlockState(world, pos, setBlockWithLayerState(block, curAmount));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return leftOver;
	}
	
	public static int takeHeightFromLayerableBlock(World world, BlockPos pos, Block block, int amount) {
		BlockState state = ChunkUtils.getBlockState(world, pos);
		int height = getHeightForLayeredBlock(state);
		int amountReceived = 0;
		int newHeight = 0;
		//if fully remove
		if (height <= amount) {
			newHeight = 0;
			amountReceived = height;
		//if partial remove
		} else {
			newHeight = height - amount;
			amountReceived = amount;
		}
		
		if (newHeight > 0) {
			try {
				ChunkUtils.setBlockState(world, pos, setBlockWithLayerState(block, newHeight));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else {
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		}
		
		return amountReceived;
	}
	
	public static BlockPos grabBlockPos(World world, int x, int y, int z)
	{
		BlockPos pos = new BlockPos(x, y, z);
		if (world.isLoaded(pos))
			return pos;
		return null;
	}
	
	
	public static boolean canReachBlock(World world, BlockPos pos)
	{
		return canReachBlock(world, pos, 1);
	}
	
	public static boolean canReachBlock(World world, BlockPos pos, int range)
	{
		if (!world.isLoaded(pos)) return false;
		if  (world.getChunkAt(pos).getHeight(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()) - 1 == pos.getY()) return true;
			
		for (int i = 1; i <= range; i++) {
			BlockPos north = pos.north(i);
			BlockPos east = pos.east(i);
			BlockPos south = pos.south(i);
			BlockPos west = pos.west(i);
			if (
				    world.getChunkAt(north).getHeight(Heightmap.Type.MOTION_BLOCKING, north.getX(), north.getZ()) <= pos.getY() ||
				    world.getChunkAt(east).getHeight(Heightmap.Type.MOTION_BLOCKING, east.getX(), east.getZ()) <= pos.getY() ||
				    world.getChunkAt(south).getHeight(Heightmap.Type.MOTION_BLOCKING, south.getX(), south.getZ()) <= pos.getY() ||
				    world.getChunkAt(west).getHeight(Heightmap.Type.MOTION_BLOCKING, west.getX(), west.getZ()) <= pos.getY()
				)
				    return true;
		}
		return false;
	}
	
	public static boolean canGrabBlock(AbstractStormObject storm, BlockPos pos, BlockState state)
	{
		if (!ConfigGrab.grab_blocks || pos == null) return false;
		World world = storm.manager.getWorld();
		if (world == null || !canReachBlock(world, pos)) return false;
		if (checkIllegalList(state))
		{
			return true;
		}
		return false;
	}
	
	public static boolean checkIllegalList(BlockState state)
	{
		Block block = state.getBlock();
		Material material = state.getMaterial();
		Weather2Remastered.warn("Checking if material is AIR is not recommended, also repairingblock is not yet working!!!");
		return !(state.getMaterial() != Material.AIR || material.isLiquid()); // || block instanceof BlockRepairingBlock);
	}
	
	public static boolean checkResistance(AbstractStormObject storm, String blockID)
	{
		ConfigList list = WeatherAPI.getWRList();		
		float resistance = list.exists(blockID) ? (float) list.get(blockID) / 9.657718F : -1.0F;
		return resistance > -1.0F && storm.windSpeed >= resistance;
	}
	
	/**
	 * Safe version of World.getPrecipitationHeight that wont invoke chunkgen/chunkload if its requesting height in unloaded chunk
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public static BlockPos getPrecipitationHeightSafe(World world, BlockPos pos) {
	    if (world.isLoaded(pos)) {
	        return new BlockPos(pos.getX(), world.getChunkAt(pos).getHeight(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getY()), pos.getZ());
	    } else {
	        return new BlockPos(pos.getX(), 0, pos.getZ());
	    }
	}
}
=======

import net.CoroUtil.block.BlockRepairingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerChunkProvider;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.block.BlockSandLayer;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.StormObject;


public class WeatherUtilBlock
{
    public static int layerableHeightPropMax = 8;

    public static boolean safeReplaceCheck(BlockState state, World world, BlockPos pos)
    {
        try
        {
            return state.getMaterial().isReplaceable();
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static boolean isReplacable(BlockState state, boolean includeReplaceableBlocks)
    {
        Material material = state.getMaterial();
        if (includeReplaceableBlocks)
            return material.isReplaceable();
        return material.isLiquid()
                || material == Material.AIR
                || material == Material.PLANT
                || material == Material.REPLACEABLE_PLANT
                || material == Material.WATER_PLANT
                || material == Material.REPLACEABLE_WATER_PLANT;
    }

    public static void fillAgainstWallSmoothly(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable) {
        WeatherUtilBlock.fillAgainstWallSmoothly(world, posSource, directionYaw, scanDistance, fillRadius, blockLayerable, 4);
    }

    public static void fillAgainstWallSmoothly(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable, int heightDiff)
    {
        BlockState stateTest = ChunkUtils.getBlockState(world, posSource.toBlockPos());
        if (stateTest.getBlock() == blockLayerable)
        {
            int heightTest = WeatherUtilBlock.getHeightForAnyBlock(stateTest);
            if (heightTest < 8) {}
        }

        BlockPos posSourcei = posSource.toBlockPos();
        int y = posSourcei.getY();
        float tickStep = 0.75F;

        Vec3 posLastNonWall = posSource.copy();
        Vec3 posWall = null;
        BlockPos lastScannedPosXZ = null;
        int previousBlockHeight = 0;

        for (float i = 0; i < scanDistance; i += tickStep) {
            double vecX = (-Maths.fastSin(Math.toRadians(directionYaw)) * (i));
            double vecZ = (Maths.fastCos(Math.toRadians(directionYaw)) * (i));
            int x = MathHelper.floor(posSource.posX + vecX);
            int z = MathHelper.floor(posSource.posZ + vecZ);
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos posXZ = new BlockPos(x, 0, z);
            BlockState state = ChunkUtils.getBlockState(world, pos);

            if (lastScannedPosXZ == null || !posXZ.equals(lastScannedPosXZ)) {
                lastScannedPosXZ = new BlockPos(posXZ);
                VoxelShape shape = state.getShape(world, pos);
                List<AxisAlignedBB> listAABBCollision = new ArrayList<>();
                if (!shape.isEmpty()) listAABBCollision.add(shape.bounds().move(pos));

                if (state.getMaterial() != Material.AIR && state.getMaterial() != Material.PLANT && !WeatherUtilBlock.safeReplaceCheck(state, world, pos) && !listAABBCollision.isEmpty()) {
                    BlockPos posUp = new BlockPos(x, y + 1, z);
                    BlockState stateUp = ChunkUtils.getBlockState(world, posUp);
                    if (stateUp.getMaterial() == Material.AIR) {
                        int height = WeatherUtilBlock.getHeightForAnyBlock(state);
                        if (height - previousBlockHeight <= heightDiff) {
                            if (height == 8) { previousBlockHeight = 0; y++; }
                            else { previousBlockHeight = height; }
                            posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                            continue;
                        } else {
                            posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                            break;
                        }
                    } else {
                        posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                        break;
                    }
                } else {
                    posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                }
            } else {
                continue;
            }
        }

        if (posWall != null) {
            BlockState state = ChunkUtils.getBlockState(world, posWall.toBlockPos());
            BlockState state1 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(1, 0, 0));
            BlockState state22 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(-1, 0, 0));
            BlockState state3 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(0, 0, 1));
            BlockState state4 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(0, 0, -1));
            if (state.getBlock() == Blocks.CACTUS || state1.getBlock() == Blocks.CACTUS ||
                    state22.getBlock() == Blocks.CACTUS || state3.getBlock() == Blocks.CACTUS || state4.getBlock() == Blocks.CACTUS) return;
            BlockPos pos2 = new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ);
            BlockState state2 = ChunkUtils.getBlockState(world, pos2);
            if (state2.getMaterial() == Material.WATER || state2.getMaterial() == Material.LAVA) return;
            WeatherUtilBlock.trySpreadOnPos2(world, new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ), 1, 1, 10, blockLayerable);
        }
    }

    public static void fillAgainstWall(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable) {
        float tickStep = 0.75F;
        int y = (int) posSource.posY;
        @SuppressWarnings("unused") float startScan = scanDistance;
        Vec3 posLastNonWall = posSource.copy();
        Vec3 posWall = null;

        for (float i = 0; i < scanDistance; i += tickStep) {
            double vecX = (-Maths.fastSin(Math.toRadians(directionYaw)) * (i));
            double vecZ = (Maths.fastCos(Math.toRadians(directionYaw)) * (i));
            int x = MathHelper.floor(posSource.posX + vecX);
            int z = MathHelper.floor(posSource.posZ + vecZ);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = ChunkUtils.getBlockState(world, pos);
            if (state.getMaterial() != Material.AIR) {
                startScan = i;
                posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                break;
            } else {
                posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
            }
        }

        if (posWall != null) {
            BlockPos posCheck = new BlockPos(posWall.toBlockPos());
            int heightOfWall = 0;
            int heightNeeded = 2;
            while (heightOfWall++ < heightNeeded) {
                posCheck = posCheck.offset(0, 1, 0);
                BlockState stateCheck = ChunkUtils.getBlockState(world, posCheck);
                if (!stateCheck.isFaceSturdy(world, posCheck, Direction.UP)) break;
            }
            if (heightOfWall >= heightNeeded)
                WeatherUtilBlock.trySpreadOnPos2(world, new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ), 4, 2, 10, blockLayerable);
        }
    }

    public static void floodAreaWithLayerableBlock(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable, int amountToTakeOrFill) {
        WeatherUtilBlock.floodAreaWithLayerableBlock(world, posSource, directionYaw, scanDistance, fillRadius, -1, blockLayerable, amountToTakeOrFill);
    }

    public static void floodAreaWithLayerableBlock(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, float takeRadius, Block blockLayerable, int amountToTakeOrRelocate) {
        float tickStep = 0.75F;
        BlockPos posSourcei = posSource.toBlockPos();
        int y = (int) posSource.posY;
        @SuppressWarnings("unused") float startScan = scanDistance;
        Vec3 posLastNonWall = posSource.copy();
        @SuppressWarnings("unused") Vec3 posWall = null;

        for (float i = 0; i < scanDistance; i += tickStep) {
            double vecX = (-Maths.fastSin(Math.toRadians(directionYaw)) * (i));
            double vecZ = (Maths.fastCos(Math.toRadians(directionYaw)) * (i));
            int x = MathHelper.floor(posSource.posX + vecX);
            int z = MathHelper.floor(posSource.posZ + vecZ);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = ChunkUtils.getBlockState(world, pos);
            if (state.getMaterial() != Material.AIR) {
                startScan = i;
                posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                break;
            } else {
                posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
            }
        }

        double distFromSourceToWall = posSource.distanceSq(posLastNonWall);
        boolean doRadius = !(takeRadius != -1 && distFromSourceToWall <= 2);

        float angleScanResolution = 1;
        float spreadDist = fillRadius;
        int maxFallDist = 20;
        int amountToTakePerXZ = 2;
        int amountToAddPerXZ = 2;

        int amountWeHave = takeRadius != -1
                ? WeatherUtilBlock.tryTakeFromPos(world, posSourcei, 0, amountToTakePerXZ, maxFallDist, blockLayerable)
                : amountToTakeOrRelocate;

        List<BlockPos> listProcessedFilter = new ArrayList<>();

        if (doRadius) {
            for (float i = 1; i < takeRadius; i += 0.75F) {
                for (float angle = 0; angle <= 180; angle += angleScanResolution) {
                    for (int mode = 0; mode <= 1; mode++) {
                        float om = mode == 1 ? -1F : 1F;
                        double vecX = (-Maths.fastSin(Math.toRadians(directionYaw - (angle * om))) * (i));
                        double vecZ = (Maths.fastCos(Math.toRadians(directionYaw - (angle * om))) * (i));
                        BlockPos pos = new BlockPos(MathHelper.floor(posSource.posX + vecX), (int)posSource.posY, MathHelper.floor(posSource.posZ + vecZ));
                        if (!listProcessedFilter.contains(pos)) {
                            listProcessedFilter.add(pos);
                            amountWeHave = WeatherUtilBlock.tryTakeFromPos(world, pos, amountWeHave, amountToTakePerXZ, maxFallDist, blockLayerable);
                        }
                    }
                }
            }
        }

        listProcessedFilter.clear();
        amountWeHave = WeatherUtilBlock.trySpreadOnPos2(world, new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ), amountWeHave, amountToAddPerXZ, maxFallDist, blockLayerable);

        if (doRadius) {
            for (float i = 1; i < spreadDist && amountWeHave > 0; i += 0.75F) {
                int amountToAddBasedOnDist = Math.max((int)(((float)WeatherUtilBlock.layerableHeightPropMax + 1F) - (i * 1.5F)), 1);
                amountToAddBasedOnDist = 2;
                for (float angle = 0; angle <= 180 && amountWeHave > 0; angle += angleScanResolution) {
                    for (int mode = 0; mode <= 1 && amountWeHave > 0; mode++) {
                        float om = mode == 1 ? -1F : 1F;
                        double vecX = (-Maths.fastSin(Math.toRadians(directionYaw - (angle * om))) * (i));
                        double vecZ = (Maths.fastCos(Math.toRadians(directionYaw - (angle * om))) * (i));
                        int x = MathHelper.floor(posLastNonWall.posX + vecX);
                        int z = MathHelper.floor(posLastNonWall.posZ + vecZ);
                        BlockPos pos = new BlockPos(x, (int)posLastNonWall.posY, z);
                        Vector3d sourceTest = posSource.addVector(0, 1D, 0).toVec3MC();
                        Vector3d destTest = new Vector3d(x + 0.5F, (int)posLastNonWall.posY + 1.5F, z + 0.5F);
                        BlockRayTraceResult destFound = world.clip(new RayTraceContext(sourceTest, destTest, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null));
                        if (destFound.getType() == BlockRayTraceResult.Type.MISS && !listProcessedFilter.contains(pos)) {
                            listProcessedFilter.add(pos);
                            amountWeHave = WeatherUtilBlock.trySpreadOnPos2(world, pos, amountWeHave, amountToAddBasedOnDist, maxFallDist, blockLayerable);
                        }
                    }
                }
            }
        }

        Weather2.debug("leftover: " + amountWeHave);
    }

    public static int tryTakeFromPos(World world, BlockPos posTakeFrom, int amount, int amountAllowedToTakeForXZ, int maxDropAllowed, Block blockLayerable) {
        int amountTaken = 0;
        BlockState statePos = ChunkUtils.getBlockState(world, posTakeFrom);
        if (!WeatherUtilBlock.isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable) && !statePos.isAir(world, posTakeFrom)) return amount;

        int dropDist = 0;
        BlockPos posScan = new BlockPos(posTakeFrom);
        while (statePos.isAir(world, posScan) && dropDist++ < maxDropAllowed) {
            posScan = posScan.offset(0, -1, 0);
            statePos = ChunkUtils.getBlockState(world, posScan);
            if (!WeatherUtilBlock.isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable) && !statePos.isAir(world, posScan)) return amount;
        }

        while (amountTaken < amountAllowedToTakeForXZ) {
            int amountReturn = WeatherUtilBlock.takeHeightFromLayerableBlock(world, posScan, blockLayerable, amountAllowedToTakeForXZ);
            amountTaken += amountReturn;
            posScan = posScan.offset(0, -1, 0);
            statePos = ChunkUtils.getBlockState(world, posScan);
            if (!WeatherUtilBlock.isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable)) break;
        }
        return amount + amountTaken;
    }

    public static int trySpreadOnPos2(World world, BlockPos posSpreadTo, int amount, int amountAllowedToAdd, int maxDropAllowed, Block blockLayerable) {
        if (amount <= 0) return amount;
        if (ChunkUtils.getBlockState(world, posSpreadTo.offset(0, 1, 0)).getMaterial() != Material.AIR) return amount;

        BlockPos posCheckNonAir = new BlockPos(posSpreadTo);
        BlockState stateCheckNonAir = ChunkUtils.getBlockState(world, posCheckNonAir);
        int depth = 0;

        while (stateCheckNonAir.getMaterial() == Material.AIR) {
            posCheckNonAir = posCheckNonAir.offset(0, -1, 0);
            stateCheckNonAir = ChunkUtils.getBlockState(world, posCheckNonAir);
            if (++depth > maxDropAllowed) return amount;
        }

        BlockPos posCheckPlaceable = new BlockPos(posCheckNonAir);
        BlockState stateCheckPlaceable = ChunkUtils.getBlockState(world, posCheckPlaceable);
        int distForPlaceableBlocks = 0;

        while (distForPlaceableBlocks < 10) {
            VoxelShape shape = stateCheckPlaceable.getShape(world, posCheckPlaceable);
            List<AxisAlignedBB> listAABBCollision = new ArrayList<>();
            if (!shape.isEmpty()) listAABBCollision.add(shape.bounds().move(posCheckPlaceable));

            if (stateCheckPlaceable.getBlock() != blockLayerable && WeatherUtilBlock.safeReplaceCheck(stateCheckPlaceable, world, posCheckPlaceable) && listAABBCollision.isEmpty()) {
                posCheckPlaceable = posCheckPlaceable.offset(0, -1, 0);
                stateCheckPlaceable = ChunkUtils.getBlockState(world, posCheckPlaceable);
                distForPlaceableBlocks++;
            } else if (stateCheckPlaceable.isFaceSturdy(world, posCheckPlaceable, Direction.UP) || stateCheckPlaceable.getBlock() == blockLayerable) {
                break;
            } else {
                return amount;
            }
        }

        if (distForPlaceableBlocks >= 10) return amount;
        if (!stateCheckPlaceable.isFaceSturdy(world, posCheckPlaceable, Direction.UP) && stateCheckPlaceable.getBlock() != blockLayerable) {
            Weather2.error("sandstorm: shouldnt be, failed a check somewhere!");
            return amount;
        }

        for (int i = 0; i < distForPlaceableBlocks; i++)
            ChunkUtils.setBlockState(world, posCheckNonAir.offset(0, -i, 0), Blocks.AIR.defaultBlockState());

        BlockPos posPlaceLayerable = new BlockPos(posCheckPlaceable);
        BlockState statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable);
        int amountToAdd = amountAllowedToAdd;

        while (amountAllowedToAdd > 0 && ChunkUtils.getBlockState(world, posPlaceLayerable.offset(0, 1, 0)).getMaterial() == Material.AIR) {
            if (statePlaceLayerable.getBlock() == blockLayerable && WeatherUtilBlock.getHeightForLayeredBlock(statePlaceLayerable) < WeatherUtilBlock.layerableHeightPropMax) {
                int height = WeatherUtilBlock.getHeightForLayeredBlock(statePlaceLayerable) + amountAllowedToAdd;
                if (height > WeatherUtilBlock.layerableHeightPropMax) { amountAllowedToAdd = height - WeatherUtilBlock.layerableHeightPropMax; height = WeatherUtilBlock.layerableHeightPropMax; }
                else { amountAllowedToAdd = 0; }
                try { ChunkUtils.setBlockState(world, posPlaceLayerable, WeatherUtilBlock.setBlockWithLayerState(blockLayerable, height)); } catch (Exception e) { e.printStackTrace(); }
                if (height == WeatherUtilBlock.layerableHeightPropMax) { posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0); statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable); }
            } else if (statePlaceLayerable.isFaceSturdy(world, posPlaceLayerable, Direction.UP)) {
                posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
                statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable);
            } else if (statePlaceLayerable.getMaterial() == Material.AIR) {
                int height = amountAllowedToAdd;
                if (height > WeatherUtilBlock.layerableHeightPropMax) { amountAllowedToAdd = height - WeatherUtilBlock.layerableHeightPropMax; height = WeatherUtilBlock.layerableHeightPropMax; }
                else { amountAllowedToAdd = 0; }
                try { ChunkUtils.setBlockState(world, posPlaceLayerable, WeatherUtilBlock.setBlockWithLayerState(blockLayerable, height)); } catch (Exception e) { e.printStackTrace(); }
                if (height == WeatherUtilBlock.layerableHeightPropMax) { posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0); statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable); }
            } else {
                Weather2.debug("wat! - " + statePlaceLayerable);
                break;
            }
        }

        if (amountAllowedToAdd < 0) Weather2.debug("wat");
        amount -= amountToAdd - amountAllowedToAdd;
        return amount;
    }

    public static boolean isLayeredOrVanillaVersionOfBlock(BlockState state, Block blockLayerable) {
        Block block = state.getBlock();
        if (block == blockLayerable) return true;
        if (blockLayerable == BlockRegistry.sand_layer.get() && block == Blocks.SAND) return true;
        if (blockLayerable == Blocks.SNOW && block == Blocks.SNOW_BLOCK) return true;
        return false;
    }

    public static int getHeightForAnyBlock(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.SNOW) return state.getValue(SnowBlock.LAYERS).intValue();
        else if (block == BlockRegistry.sand_layer.get()) return state.getValue(BlockSandLayer.LAYERS).intValue();
        else if (block == Blocks.SAND || block == Blocks.SNOW_BLOCK) return 8;
        else if (block instanceof SlabBlock) return 4;
        else if (block == Blocks.AIR) return 0;
        else return 8;
    }

    public static int getHeightForLayeredBlock(BlockState state) {
        if (state.getBlock() == Blocks.SNOW) return state.getValue(SnowBlock.LAYERS).intValue();
        else if (state.getBlock() == BlockRegistry.sand_layer.get()) return state.getValue(BlockSandLayer.LAYERS).intValue();
        else if (state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.SNOW_BLOCK) return 8;
        else return 0;
    }

    public static BlockState setBlockWithLayerState(Block block, int height) {
        if (block == Blocks.SNOW) {
            if (height == WeatherUtilBlock.layerableHeightPropMax) return Blocks.SNOW_BLOCK.defaultBlockState();
            else return block.defaultBlockState().setValue(SnowBlock.LAYERS, height);
        } else if (block == BlockRegistry.sand_layer.get()) {
            if (height == WeatherUtilBlock.layerableHeightPropMax) return Blocks.SAND.defaultBlockState();
            else return block.defaultBlockState().setValue(BlockSandLayer.LAYERS, height);
        } else return null;
    }

    public static boolean divideToNeighborCheck(BlockState state, World worldIn, BlockPos pos, Block blockIn) {
        boolean foundSpotToSpread = false;
        int heightToUse = WeatherUtilBlock.getHeightForLayeredBlock(state);
        if (heightToUse > 2) {
            for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
                if (heightToUse > 2) {
                    BlockPos posCheck = pos.relative(enumfacing);
                    BlockState stateCheck = worldIn.getBlockState(posCheck);
                    if (stateCheck.getBlock() == state.getBlock()) {
                        int heightCheck = WeatherUtilBlock.getHeightForLayeredBlock(stateCheck);
                        if (heightCheck + 2 <= heightToUse) {
                            heightToUse -= 1;
                            WeatherUtilBlock.addHeightToLayerableBLock(worldIn, posCheck, stateCheck.getBlock(), heightCheck, 1);
                            foundSpotToSpread = true;
                        }
                    }
                }
            }
        }
        if (foundSpotToSpread)
            worldIn.setBlockAndUpdate(pos, WeatherUtilBlock.setBlockWithLayerState(state.getBlock(), heightToUse));
        return foundSpotToSpread;
    }

    public static int addHeightToLayerableBLock(World world, BlockPos pos, Block block, int sourceAmount, int amount) {
        int curAmount = sourceAmount + amount;
        int leftOver = 0;
        if (curAmount > WeatherUtilBlock.layerableHeightPropMax) { leftOver = curAmount - WeatherUtilBlock.layerableHeightPropMax; curAmount = WeatherUtilBlock.layerableHeightPropMax; }
        try { ChunkUtils.setBlockState(world, pos, WeatherUtilBlock.setBlockWithLayerState(block, curAmount)); } catch (Exception e) { e.printStackTrace(); }
        return leftOver;
    }

    public static int takeHeightFromLayerableBlock(World world, BlockPos pos, Block block, int amount) {
        BlockState state = ChunkUtils.getBlockState(world, pos);
        int height = WeatherUtilBlock.getHeightForLayeredBlock(state);
        int newHeight, amountReceived;
        if (height <= amount) { newHeight = 0; amountReceived = height; }
        else { newHeight = height - amount; amountReceived = amount; }
        if (newHeight > 0) {
            try { ChunkUtils.setBlockState(world, pos, WeatherUtilBlock.setBlockWithLayerState(block, newHeight)); } catch (Exception e) { e.printStackTrace(); }
        } else {
            world.removeBlock(pos, false);
        }
        return amountReceived;
    }

    public static BlockPos grabBlockPos(World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return world.isLoaded(pos) ? pos : null;
    }

    public static boolean canReachBlock(World world, BlockPos pos)
    {
        return WeatherUtilBlock.canReachBlock(world, pos, 1);
    }

    public static boolean canReachBlock(World world, BlockPos pos, int range)
    {
        if (!world.isLoaded(pos)) return false;
        if (WeatherUtilBlock.getHeightSafe(world, pos).getY() - 1 == pos.getY()) return true;

        for (int i = 1; i <= range; i++)
            if (WeatherUtilBlock.getHeightSafe(world, pos.north(i)).getY() <= pos.getY()
                    || WeatherUtilBlock.getHeightSafe(world, pos.east(i)).getY()  <= pos.getY()
                    || WeatherUtilBlock.getHeightSafe(world, pos.south(i)).getY() <= pos.getY()
                    || WeatherUtilBlock.getHeightSafe(world, pos.west(i)).getY()  <= pos.getY())
                return true;

        return false;
    }

    public static boolean canGrabBlock(StormObject storm, BlockPos pos, BlockState state)
    {
        if (!ConfigGrab.grab_blocks || pos == null) return false;
        World world = storm.manager.getWorld();
        if (world == null || !WeatherUtilBlock.canReachBlock(world, pos)) return false;
        return WeatherUtilBlock.checkIllegalList(state);
    }

    public static boolean checkIllegalList(BlockState state)
    {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return !(state.isAir() || material.isLiquid() || block instanceof BlockRepairingBlock);
    }

    public static boolean checkResistance(StormObject storm, String blockID)
    {
        ConfigList list = WeatherAPI.getWRList();
        float resistance = list.exists(blockID) ? (float) list.get(blockID) / 9.657718F : -1.0F;
        return resistance > -1.0F && storm.windSpeed >= resistance;
    }

    public static BlockPos getHeightSafe(World world, BlockPos pos)
    {
        Chunk chunk = WeatherUtilBlock.getChunk(world, pos.getX(), pos.getZ());
        if (chunk == null)
            return pos.below(pos.getY());

        int surfaceY = chunk.getHeight(Heightmap.Type.WORLD_SURFACE, pos.getX() & 15, pos.getZ() & 15);
        BlockPos new_pos = new BlockPos(pos.getX(), Math.min(pos.getY(), surfaceY), pos.getZ());

        while (new_pos.getY() < 255)
        {
            BlockState state = chunk.getBlockState(new_pos);
            if (WeatherUtilBlock.isReplacable(state, false))
                break;
            new_pos = new_pos.above();
        }
        return new_pos;
    }

    public static BlockPos getPrecipitationHeightSafe(World world, BlockPos pos)
    {
        if (world.isLoaded(pos))
            return world.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, pos);
        else
            return new BlockPos(pos.getX(), 0, pos.getZ());
    }

    public static Chunk getChunk(World world, int x, int z)
    {
        if (world.isClientSide)
            return world.getChunk(x >> 4, z >> 4);
        else
            return ((ServerChunkProvider) world.getChunkSource()).getChunk(x >> 4, z >> 4, false);
    }
}
>>>>>>> Stashed changes:src/main/java/net/mrbt0907/weather2/util/WeatherUtilBlock.java
