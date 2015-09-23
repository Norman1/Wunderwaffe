package evaluation;

import map.Region;
import bot.HistoryTracker;

/**
 * GameState is responsible for evaluating whether we are winning or losing.
 *
 */
public class GameState {

	private static boolean gameCompletelyLost = false;

	public void evaluateGameState() {
		// Don't switch from a lost game to an open game
		if (gameCompletelyLost) {
			return;
		}

		int ownArmies = 0;
		int opponentArmies = 0;
		for (Region region : HistoryTracker.botState.getVisibleMap().getOwnedRegions()) {
			ownArmies += region.getArmies();
		}
		for (Region region : HistoryTracker.botState.getVisibleMap().getOpponentRegions()) {
			opponentArmies += region.getArmies();
		}

		int ownIncome = HistoryTracker.botState.getStartingArmies();
		int opponentIncome = HistoryTracker.getOpponentDeployment();
		// int opponentIncome =
		// HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());

		if (ownIncome == 5 && opponentIncome >= 12 && opponentArmies * 3 > ownArmies) {
			GameState.gameCompletelyLost = true;
		}
	}

	public static boolean isGameCompletelyLost() {
		return gameCompletelyLost;
	}

	public static void setGameCompletelyLost(boolean gameCompletelyLost) {
		GameState.gameCompletelyLost = gameCompletelyLost;
	}

}
