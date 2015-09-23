package tasks;

import java.util.ArrayList;
import java.util.List;

import bot.HistoryTracker;

import map.Map;
import map.Region;
import move.Moves;
import evaluation.RegionValueCalculator;

/**
 * NoPlanBreakBestRegionTask is responsible for breaking the best opponent region without following a specified plan.
 * 
 */
public class NoPlanBreakBestRegionTask {

	public static Moves calculateNoPlanBreakBestRegionTask(int maxDeployment, List<Region> regionsToConsider,
			Map visibleMap, Map workingMap) {
		List<Region> wmOpponentRegions = workingMap.getVisibleOpponentRegions();
		List<Region> vmOpponentRegions = visibleMap.copyRegions(wmOpponentRegions);
		List<Region> sortedOpponentRegions = RegionValueCalculator.sortAttackValue(vmOpponentRegions);

		if (regionsToConsider != null) {
			List<Region> regionsToRemove = new ArrayList<>();
			for (Region region : sortedOpponentRegions) {
				if (!regionsToConsider.contains(region)) {
					regionsToRemove.add(region);
				}
			}
			sortedOpponentRegions.removeAll(regionsToRemove);
		}

		for (Region region : sortedOpponentRegions) {
			if (region.isVisible()) {
				Moves breakRegionMoves = BreakRegionTask.calculateBreakRegionTask(region, maxDeployment, 1,1);
				if (breakRegionMoves != null) {
					return breakRegionMoves;
				}
			}
		}
		return null;
	}

	public static List<Region> removeRegionsThatWeTook(List<Region> opponentRegions) {
		List<Region> out = new ArrayList<>();
		List<Region> newOpponentRegions = HistoryTracker.botState.getWorkingMap().getOpponentRegions();
		List<Integer> newOpponentRegionsIDs = Map.getRegionIDs(newOpponentRegions);

		for (Region opponentRegion : opponentRegions) {
			if (newOpponentRegionsIDs.contains(opponentRegion.getId())) {
				out.add(opponentRegion);
			}
		}
		return out;
	}

}
