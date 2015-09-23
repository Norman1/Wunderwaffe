package move;

import java.util.ArrayList;
import java.util.List;

import evaluation.MapUpdater;
import map.Region;
import bot.HistoryTracker;

/**
 * This class is responsible for cleaning up moves. This means that the deployment gets embellished and if there are
 * intern more than one move from Region A to Region B then those moves get joined.
 *
 */
public class MovesCleaner {

	public static void cleanupMoves(Moves moves) {
		deleteOldMovesFromMap();
		mergePlaceArmiesMoves(moves);
		mergeAttackTransferMoves(moves);
		MapUpdater.updateMap(HistoryTracker.botState.getWorkingMap()); // TODO hack
	}

	private static void mergeAttackTransferMoves(Moves moves) {
		List<AttackTransferMove> attackTransferMoves = moves.attackTransferMoves;
		List<AttackTransferMove> nullAttacks = new ArrayList<AttackTransferMove>();
		for (AttackTransferMove atm : attackTransferMoves) {
			if (atm.getArmies() == 0) {
				nullAttacks.add(atm);
			}
		}
		attackTransferMoves.removeAll(nullAttacks);

		List<AttackTransferMove> joinedAttacks = getJoinedAttacks(attackTransferMoves);
		moves.attackTransferMoves = joinedAttacks;
		MovesCommitter.committAttackTransferMoves(joinedAttacks);
	}

	private static List<AttackTransferMove> getJoinedAttacks(List<AttackTransferMove> in) {
		List<AttackTransferMove> out = new ArrayList<AttackTransferMove>();
		List<AttackTransferMove> illegalMoves = new ArrayList<AttackTransferMove>();
		// Step 1
		for (int i = 0; i < in.size(); i++) {
			AttackTransferMove movei = in.get(i);
			for (int j = i + 1; j < in.size(); j++) {
				AttackTransferMove movej = in.get(j);
				if (movei.getFromRegion().equals(movej.getFromRegion())
						&& movei.getToRegion().equals(movej.getToRegion())) {
					if (!illegalMoves.contains(movej)) {
						illegalMoves.add(movej);
					}
				}
			}
		}
		// Step two
		for (int i = 0; i < in.size(); i++) {
			AttackTransferMove movei = in.get(i);
			if (!illegalMoves.contains(movei)) {
				for (int j = 0; j < illegalMoves.size(); j++) {
					AttackTransferMove movej = illegalMoves.get(j);
					if (movei.getFromRegion().equals(movej.getFromRegion())
							&& movei.getToRegion().equals(movej.getToRegion())) {
						movei.setArmies(movei.getArmies() + movej.getArmies());
						if (!movej.getMessage().equals("")) {
							movei.setMessage(movej.getMessage());
						}
					}

				}
				out.add(movei);
			}
		}
		return out;
	}

	private static void mergePlaceArmiesMoves(Moves moves) {
		List<PlaceArmiesMove> placeArmiesMoves = moves.placeArmiesMoves;
		List<PlaceArmiesMove> mergedMoves = new ArrayList<PlaceArmiesMove>();
		for (Region ownedRegion : HistoryTracker.botState.getVisibleMap().getOwnedRegions()) {
			int armiesToPlace = 0;
			for (PlaceArmiesMove oldMove : placeArmiesMoves) {
				if (oldMove.getRegion() == ownedRegion) {
					armiesToPlace += oldMove.getArmies();
				}
			}
			if (armiesToPlace > 0) {
				PlaceArmiesMove newMove = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(), ownedRegion,
						armiesToPlace);
				mergedMoves.add(newMove);
			}
		}
		moves.placeArmiesMoves = mergedMoves;
		MovesCommitter.committPlaceArmiesMoves(mergedMoves);
	}

	/**
	 * Deletes all of our moves from the visible map.
	 */
	private static void deleteOldMovesFromMap() {
		for (Region region : HistoryTracker.botState.getVisibleMap().getRegions()) {
			region.getOutgoingMoves().clear();
			region.getIncomingMoves().clear();
			if (region.getPlayerName().equals(HistoryTracker.myName)) {
				region.getDeployment(1).clear();
			}
		}
	}

}
