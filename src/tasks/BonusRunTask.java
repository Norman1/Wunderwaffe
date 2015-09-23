package tasks;

import basicAlgorithms.DistanceCalculator;
import map.Map;
import map.SuperRegion;
import move.Moves;

/**
 * BonusRunTaks is responsible for moving in the direction of an opponent bonus.
 * 
 */
public class BonusRunTask {

	public static Moves calculateBonusRunMoves(SuperRegion opponentSuperRegion, int maxDeployment, Map visibleMap,
			Map workingMap) {
		Moves out = new Moves();
		return out;
	}
	
	
	private static boolean areWeAlreadyMovingInDirection(SuperRegion opponentSuperRegion, Map visibleMap, Map workingMap){
		boolean alreadyMovingInDirection = false;
		return alreadyMovingInDirection;
	}

}
