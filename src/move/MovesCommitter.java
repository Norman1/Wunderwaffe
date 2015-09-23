package move;

import java.util.List;

import bot.HistoryTracker;
import evaluation.MapUpdater;

/**
 * This class is responsible for committing moves.
 * 
 */
public class MovesCommitter {

	public static void committMoves(Moves moves) {
		committPlaceArmiesMoves(moves.placeArmiesMoves);
		committAttackTransferMoves(moves.attackTransferMoves);
	}

	public static void committPlaceArmiesMoves(List<PlaceArmiesMove> placeArmiesMoves) {
		for (PlaceArmiesMove placeArmiesMove : placeArmiesMoves) {
			committPlaceArmiesMove(placeArmiesMove);
		}
	}

	public static void committAttackTransferMoves(List<AttackTransferMove> attackTransferMoves) {
		for (AttackTransferMove attackTransferMove : attackTransferMoves) {
			committAttackTransferMove(attackTransferMove);
		}
	}

	public static void committPlaceArmiesMove(PlaceArmiesMove placeArmiesMove) {
		placeArmiesMove.getRegion().addDeployment(placeArmiesMove);
	}

	public static void committPlaceArmiesMove(PlaceArmiesMove placeArmiesMove, int type) {
		if (type == 1) {
			placeArmiesMove.getRegion().addDeployment(placeArmiesMove);
		} else if (type == 2) {
			placeArmiesMove.getRegion().addConservativeDeployment(placeArmiesMove);
		}
	}

	public static void committAttackTransferMove(AttackTransferMove attackTransferMove) {
		attackTransferMove.getFromRegion().addOutgoingMove(attackTransferMove);
		attackTransferMove.getToRegion().addIncomingMove(attackTransferMove);
		MapUpdater.updateMap(attackTransferMove, HistoryTracker.botState.getWorkingMap(), 1);
	}

}
