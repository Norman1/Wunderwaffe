package tasks;

import java.util.List;

import map.Region;
import move.Moves;
import evaluation.RegionValueCalculator;

/**
 * DefendRegionsTask is responsible for calculating a defense plan so that the opponent can't take any of the specified
 * regions. This is for example relevant to defend a SuperRegion.
 * 
 */
public class DefendRegionsTask {
	/**
	 * Returns null if not possible to defend all regions and not the acceptNotAllDefense flag is set.
	 * 
	 * @param regionsToDefend
	 * @param maxDeployment
	 * @param acceptNotAllDefense
	 * @return
	 */
	public static Moves calculateDefendRegionsTask(List<Region> regionsToDefend, int maxDeployment,
			boolean acceptNotAllDefense, int lowerConservativeLevel, int upperConservativeLevel) {
		Moves out = new Moves();
		List<Region> sortedDefenceRegions = RegionValueCalculator.sortDefenseValue(regionsToDefend);
		for (Region region : sortedDefenceRegions) {
			int stillAvailableArmies = maxDeployment - out.getTotalDeployment();
			Moves defendRegionMoves = DefendRegionTask.calculateDefendRegionTask(region, stillAvailableArmies, true,
					lowerConservativeLevel,upperConservativeLevel);
			if (defendRegionMoves != null) {
				out.mergeMoves(defendRegionMoves);
			} else {
				if (!acceptNotAllDefense) {
					return null;
				}
			}
		}
		return out;
	}
}
