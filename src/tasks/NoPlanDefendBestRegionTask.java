package tasks;

import java.util.List;

import map.Map;
import map.Region;
import move.Moves;
import evaluation.RegionValueCalculator;

/**
 * NoPlanDefendBestRegionTask is responsible for defending the highest priority region without following a specified
 * plan.
 * 
 */
public class NoPlanDefendBestRegionTask {

	public static Moves calculateNoPlanDefendBestRegionTask(int maxDeployment, Map visibleMap, Map workingMap) {
		List<Region> wmOpponentBorderingRegions = workingMap.getOpponentBorderingRegions();
		List<Region> vmOpponentBorderingRegions = visibleMap.copyRegions(wmOpponentBorderingRegions);
		List<Region> sortedVMOpponentBorderingRegions = RegionValueCalculator
				.sortDefenseValue(vmOpponentBorderingRegions);

		for (Region vmRegion : sortedVMOpponentBorderingRegions) {
			Moves defendRegionMoves = DefendRegionTask.calculateDefendRegionTask(vmRegion, maxDeployment, true,1,1);
			if (defendRegionMoves != null && defendRegionMoves.getTotalDeployment() > 0) {
				return defendRegionMoves;
			}
		}
		return null;
	}
}
