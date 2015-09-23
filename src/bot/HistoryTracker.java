package bot;

import java.util.ArrayList;
import java.util.List;

import map.Region;
import map.SuperRegion;
import move.Move;
import move.PlaceArmiesMove;

/**
 * HistoryTracker is responsible for storing the whole history of what has happened on the board so far and our thoughts
 * about it.
 * 
 */
public class HistoryTracker {

	public static BotState botState;
	// public static int opponentDeployment;
	public static String myName = "";
	public static String opponentName = "";
	private static DeploymentHistory deploymentHistory = new DeploymentHistory();

	public static int getOpponentDeployment(Region region) {
		int out = 0;
		List<Move> opponentMoves = botState.getOpponentMoves();
		List<PlaceArmiesMove> deploymentMoves = new ArrayList<>();
		for (Move opponentMove : opponentMoves) {
			if (opponentMove.getClass().equals(PlaceArmiesMove.class)) {
				deploymentMoves.add((PlaceArmiesMove) opponentMove);
			}
		}
		for (PlaceArmiesMove pam : deploymentMoves) {
			if (region.getId() == pam.getRegion().getId()) {
				out += pam.getArmies();
			}
		}
		return out;
	}

	public static void readOpponentDeployment() {
		int opponentDeployment = 0;
		List<Move> opponentMoves = botState.getOpponentMoves();
		List<PlaceArmiesMove> deploymentMoves = new ArrayList<>();
		for (Move opponentMove : opponentMoves) {
			if (opponentMove.getClass().equals(PlaceArmiesMove.class)) {
				deploymentMoves.add((PlaceArmiesMove) opponentMove);
			}
		}
		for (PlaceArmiesMove pam : deploymentMoves) {
			opponentDeployment += pam.getArmies();
		}
		if (botState.getRoundNumber() != 1) {
			deploymentHistory.update(opponentDeployment);
		}
	}

	public static int getOpponentDeployment() {
		return deploymentHistory.getOpponentDeployment();
	}
	
	public static DeploymentHistory getDeploymentHistory(){
		return deploymentHistory;
	}

}
