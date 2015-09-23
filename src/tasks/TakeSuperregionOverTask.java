package tasks;

import java.util.List;

import strategy.TakeRegionsTaskCalculator;

import map.Region;
import map.SuperRegion;
import move.Moves;

public class TakeSuperregionOverTask {

	/**
	 * Calculates the needed moves to push the opponent out of a SuperRegion
	 * that has no neutrals in it.
	 * 
	 * @param maxDeployment
	 * @param superRegion
	 * @return
	 */
	public static Moves calculateTakeSuperRegionOverTask(int maxDeployment, SuperRegion superRegion, int conservativeLevel) {
		List<Region> opponentSubRegions = superRegion.getVisibleOpponentSubRegions();
		return TakeRegionsTaskCalculator.calculateTakeRegionsTask(maxDeployment, opponentSubRegions,conservativeLevel);
	}
}
