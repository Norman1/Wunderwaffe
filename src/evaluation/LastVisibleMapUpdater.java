package evaluation;

import map.Map;
import map.Region;
import move.MovesCommitter;
import move.PlaceArmiesMove;
import bot.HistoryTracker;

public class LastVisibleMapUpdater {

	public static void storeLastVisibleMap() {
		HistoryTracker.botState.lastVisibleMap = HistoryTracker.botState.getVisibleMap().getMapCopy();
	}

	public static void storeOpponentDeployment() {
		Map lastVisibleMap = HistoryTracker.botState.getLastVisibleMap();
		for (Region opponentRegion : lastVisibleMap.getOpponentRegions()) {
			int armiesDeployed = HistoryTracker.getOpponentDeployment(opponentRegion);
			if (armiesDeployed > 0) {
				PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getOpponentPlayerName(),
						opponentRegion, armiesDeployed);
				MovesCommitter.committPlaceArmiesMove(pam);
			}
		}
	}

}
