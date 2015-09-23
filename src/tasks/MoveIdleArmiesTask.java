package tasks;

import java.util.ArrayList;
import java.util.List;

import map.Region;
import move.AttackTransferMove;
import move.Moves;
import bot.HistoryTracker;

/**
 * MoveIdleArmiesTask is responsible for calculating armies to join in to the
 * already calculated moves.
 * 
 */
public class MoveIdleArmiesTask {

	public static Moves calculateMoveIdleArmiesTask() {
		Moves out = new Moves();
		for (Region ownedRegion : HistoryTracker.botState.getVisibleMap().getOwnedRegions()) {
			List<AttackTransferMove> outgoingMoves = ownedRegion.getOutgoingMoves();
			AttackTransferMove mostImportantMove = null;
			int currentHighestValue = -1;
			for (AttackTransferMove atm : outgoingMoves) {
				if (atm.getToRegion().getPlayerName().equals(HistoryTracker.botState.getOpponentPlayerName())
						&& atm.getArmies() > 1 && !atm.getMessage().equals("tryoutAttack")) {
					int attackValue = atm.getToRegion().getAttackRegionValue();
					if (attackValue > currentHighestValue) {
						currentHighestValue = attackValue;
						mostImportantMove = atm;
					}
				}
			}
			int idleArmies = ownedRegion.getIdleArmies();
			if (mostImportantMove != null && idleArmies > 0) {
				AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(), ownedRegion,
						mostImportantMove.getToRegion(), ownedRegion.getIdleArmies());
				out.attackTransferMoves.add(atm);
			}
		}
		return out;
	}

	/**
	 * Calculates the movement of idle armies to join in expansion steps.
	 * 
	 * @return
	 */
	public static Moves calculateMoveIdleExpansionArmiesTask() {
		Moves out = new Moves();
		for (Region ourRegion : HistoryTracker.botState.getVisibleMap().getNonOpponentBorderingBorderRegions()) {
			if (ourRegion.getIdleArmies() > 0 && ourRegion.getExpansionMoves().size() > 0) {
				AttackTransferMove bestMove = getBestExpansionMoveToAddArmies(ourRegion.getExpansionMoves());
				if (isAddingArmiesBeneficial(bestMove)) {
					AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
							ourRegion, bestMove.getToRegion(), ourRegion.getIdleArmies());
					out.attackTransferMoves.add(atm);
				}
			}

		}
		return out;
	}

	private static boolean isAddingArmiesBeneficial(AttackTransferMove expansionMove) {
		boolean isBeneficial = true;
		boolean containsToRegionOpponentNeighbor = expansionMove.getToRegion().getOpponentNeighbors().size() > 0 ? true
				: false;
		boolean containsFromRegionOpponentBorderingNeighbor = false;
		for (Region ownedNeighbor : expansionMove.getFromRegion().getOwnedNeighbors()) {
			if (ownedNeighbor.getOpponentNeighbors().size() > 0) {
				containsFromRegionOpponentBorderingNeighbor = true;
			}
		}
		if (containsToRegionOpponentNeighbor == false && containsFromRegionOpponentBorderingNeighbor == true) {
			isBeneficial = false;
		}
		return isBeneficial;
	}

	/**
	 * 
	 * @param expansionMoves
	 *            only moves where the toRegion is neutral are allowed
	 * @return
	 */
	private static AttackTransferMove getBestExpansionMoveToAddArmies(List<AttackTransferMove> expansionMoves) {
		AttackTransferMove bestExpansionMove = null;
		List<AttackTransferMove> expansionMovesToOpponent = new ArrayList<>();
		for (AttackTransferMove atm : expansionMoves) {
			if (atm.getToRegion().getOpponentNeighbors().size() > 0) {
				expansionMovesToOpponent.add(atm);
			}
		}

		if (expansionMovesToOpponent.size() > 0) {
			bestExpansionMove = expansionMovesToOpponent.get(0);
			for (AttackTransferMove atm : expansionMovesToOpponent) {
				if (atm.getToRegion().getAttackRegionValue() > bestExpansionMove.getToRegion().getAttackRegionValue()) {
					bestExpansionMove = atm;
				}
			}

		} else if (expansionMoves.size() > 0) {
			bestExpansionMove = expansionMoves.get(0);
			for (AttackTransferMove atm : expansionMoves) {
				if (atm.getToRegion().getNonOwnedNeighbors().size() > bestExpansionMove.getToRegion()
						.getNonOwnedNeighbors().size()) {
					bestExpansionMove = atm;
				}
			}
		}
		return bestExpansionMove;
	}

}
