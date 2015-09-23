package tasks;

import java.util.List;

import evaluation.RegionValueCalculator;

import map.Region;
import move.Moves;

/**
 * BreakRegionsTask is responsible for breaking a stuff like an opponent SuperRegion. Therefore the goal is not to break
 * all regions in the SuperRegion but to pick one and break this region.
 * 
 */
public class BreakRegionsTask {

	/**
	 * Returns null if none of the specified regions can get broken.
	 * 
	 * @param regionsToBreak
	 * @param maxDeployment
	 * @return
	 */
	public static Moves calculateBreakRegionsTask(List<Region> regionsToBreak, int maxDeployment,
			int lowerconservativeLevel, int upperConservativeLevel) {
		List<Region> sortedRegions = RegionValueCalculator.sortAttackValue(regionsToBreak);
		for (Region opponentRegion : sortedRegions) {
			Moves breakRegionMoves = BreakRegionTask.calculateBreakRegionTask(opponentRegion, maxDeployment,
					lowerconservativeLevel, upperConservativeLevel);
			if (breakRegionMoves != null) {
				return breakRegionMoves;
			}
		}
		return null;
	}

}
