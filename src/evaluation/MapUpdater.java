package evaluation;

import map.Map;
import map.Region;
import move.AttackTransferMove;
import bot.HistoryTracker;

public class MapUpdater {

	// TODO
	public static void updateMap(Map mapToUpdate) {
		HistoryTracker.botState.workingMap = HistoryTracker.botState.getVisibleMap().getMapCopy();
		for (Region wmRegion : HistoryTracker.botState.getWorkingMap().getRegions()) {
			Region vmRegion = HistoryTracker.botState.getVisibleMap().getRegion(wmRegion.getId());
			if (!vmRegion.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName()) && vmRegion.isVisible()) {
				int toBeKilledArmies = vmRegion.getArmiesAfterDeployment(1);
				int attackingArmies = vmRegion.getIncomingArmies();
				if (Math.round(attackingArmies * 0.6) >= toBeKilledArmies) {
					wmRegion.setPlayerName(HistoryTracker.botState.getMyPlayerName());
					wmRegion.setArmies(vmRegion.getIncomingArmies()
							- (int) Math.round(vmRegion.getArmiesAfterDeployment(1) * 0.7));
				} else {
					// TODO
					wmRegion.setArmies(vmRegion.getArmiesAfterDeployment(1)
							- (int) Math.round(0.6 * vmRegion.getIncomingArmies()));
				}
			}

		}
	}

	/**
	 * Updates the working map according to the move input
	 * 
	 * @param attackTransferMove
	 */
	public static void updateMap(AttackTransferMove attackTransferMove, Map mapToUpdate, int conservativeLevel) {
		int toRegionID = attackTransferMove.getToRegion().getId();
		Region wmRegion = HistoryTracker.botState.getWorkingMap().getRegion(toRegionID);
		Region vmRegion = HistoryTracker.botState.getVisibleMap().getRegion(toRegionID);
		int toBeKilledArmies = vmRegion.getArmiesAfterDeployment(1);
		int attackingArmies = vmRegion.getIncomingArmies();
		if (Math.round(attackingArmies * 0.6) >= toBeKilledArmies) {
			wmRegion.setPlayerName(HistoryTracker.botState.getMyPlayerName());
		}
	}

}
