package tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import map.Region;
import move.AttackTransferMove;
import move.Moves;
import move.MovesCommitter;
import move.PlaceArmiesMove;
import bot.HistoryTracker;

/**
 * DeleteBadMovesTask is responsible for deleting bad moves from previous steps. If we deploy in region B and transfer
 * to Region A then we should directly deploy at A. If we Attack from A O1 and O2 and also from B O1 and O2 then we
 * should go for lesser stronger attacks.
 *
 */
public class DeleteBadMovesTask {
	public static void calculateDeleteBadMovesTask(Moves movesSoFar) {
		deleteBadTransferMoves(movesSoFar);
		deleteBadAttacks(movesSoFar);
	}

	private static void deleteBadAttacks(Moves movesSoFar) {
		List<AttackTransferMove> interestingAttacks = new ArrayList<AttackTransferMove>();
		for (AttackTransferMove atm : movesSoFar.attackTransferMoves) {
			if (atm.getArmies() > 1 && atm.getToRegion().getPlayerName().equals(HistoryTracker.opponentName)) {
				boolean isInteresting = false;
				Region attackRegion = atm.getToRegion().getIncomingMoves().get(0).getFromRegion();
				for (AttackTransferMove incomingMove : atm.getToRegion().getIncomingMoves()) {
					if (incomingMove.getFromRegion() != attackRegion) {
						isInteresting = true;
					}
				}
				if (isInteresting) {
					interestingAttacks.add(atm);
				}
			}
		}
		Set<Region> regionsWithInterestingAttacks = new HashSet<Region>();
		for (AttackTransferMove atm : interestingAttacks) {
			regionsWithInterestingAttacks.add(atm.getFromRegion());
		}
		// TODO weitermachen
		
	}

	private static void deleteBadTransferMoves(Moves movesSoFar) {
		List<AttackTransferMove> interestingTransfers = new ArrayList<AttackTransferMove>();
		for (AttackTransferMove atm : movesSoFar.attackTransferMoves) {
			// TODO getArmies() > 1 ???
			if (atm.getArmies() > 1 && atm.getToRegion().getPlayerName().equals(HistoryTracker.myName)
					&& atm.getFromRegion().getTotalDeployment(1) > 0) {
				interestingTransfers.add(atm);
			}
		}
		if (interestingTransfers.size() > 0) {
			System.err.println("interestingTransfers.size(): " + interestingTransfers.size());
		}
		for (AttackTransferMove atm : interestingTransfers) {
			int deploymentToShift = Math.min(atm.getFromRegion().getTotalDeployment(1), atm.getArmies());
			atm.setArmies(atm.getArmies() - deploymentToShift);
			PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.myName, atm.getToRegion(), deploymentToShift);
			MovesCommitter.committPlaceArmiesMove(pam);
			movesSoFar.placeArmiesMoves.add(pam);
			for (PlaceArmiesMove oldDeployment : atm.getFromRegion().getDeployment(1)) {
				while (deploymentToShift > 0) {
					if (oldDeployment.getArmies() > 0) {
						deploymentToShift--;
						oldDeployment.setArmies(oldDeployment.getArmies() - 1);
					} else {
						break;
					}
				}
			}
		}

	}
}
