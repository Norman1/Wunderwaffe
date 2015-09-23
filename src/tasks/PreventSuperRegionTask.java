package tasks;

import java.util.List;

import map.Region;
import map.SuperRegion;
import move.Moves;
import bot.HistoryTracker;

/**
 * This class is responsible for preventing the opponent from taking over a SuperRegion. This can be achieved in
 * different ways. We can deploy to a region there or we can hit the SuperRegion from outside.
 * 
 */
public class PreventSuperRegionTask {

	public static Moves calculatePreventSuperRegionTask(SuperRegion superRegionToPrevent, int maxDeployment,
			int conservativeLevel) {
		List<Region> subRegionsToPrevent = superRegionToPrevent.getOwnedSubRegions();
		Moves opponentAttacks = PreventRegionsTask.calculateGuessedOpponentTakeOverMoves(subRegionsToPrevent, true,
				conservativeLevel);
		if (opponentAttacks == null) {
			return new Moves();
		}
		Moves preventRegionMovesByDeploying = PreventRegionsTask.calculatePreventRegionsTask(subRegionsToPrevent,
				maxDeployment, conservativeLevel);
		List<Region> visibleOpponentSubRegions = superRegionToPrevent.getVisibleOpponentSubRegions();
		Moves breakBestRegionMoves = NoPlanBreakBestRegionTask.calculateNoPlanBreakBestRegionTask(maxDeployment,
				visibleOpponentSubRegions, HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());
		if (breakBestRegionMoves == null) {
			return preventRegionMovesByDeploying;
		} else {
			if (breakBestRegionMoves.getTotalDeployment() <= preventRegionMovesByDeploying.getTotalDeployment()) {
				return breakBestRegionMoves;
			} else {
				return preventRegionMovesByDeploying;
			}
		}
	}

}
