package tasks;

import java.util.ArrayList;
import java.util.List;

import map.Region;
import move.Moves;
import strategy.TakeRegionsTaskCalculator;
import evaluation.RegionValueCalculator;

/**
 * This class is responsible for flanking a SuperRegion owned by the opponent.
 *
 */
public class FlankSuperRegionTask {

	/**
	 * Calculates the best flanking moves for opponent SuperRegions.
	 * 
	 * @param maxDeployment
	 *            the max deployment constraint
	 * @return the best flanking moves and null if no such moves were found
	 */
	public static Moves calculateFlankSuperRegionTask(int maxDeployment) {
		Moves out = null;
		List<Region> sortedFlankingRegions = RegionValueCalculator.getSortedFlankingValueRegions();
		for (Region flankableRegion : sortedFlankingRegions) {
			if(flankableRegion.getFlankingRegionValue() <= 2){
				break;
			}
			List<Region> regionToTakeAsList = new ArrayList<Region>();
			regionToTakeAsList.add(flankableRegion);
			out = TakeRegionsTaskCalculator.calculateTakeRegionsTask(maxDeployment, regionToTakeAsList, 0);
			if (out != null) {
				break;
			}
		}

		return out;

	}

}
