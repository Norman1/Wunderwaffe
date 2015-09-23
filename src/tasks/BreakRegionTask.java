package tasks;

import java.util.List;

import map.Map;
import map.Region;
import move.AttackTransferMove;
import move.Moves;
import move.PlaceArmiesMove;
import bot.HistoryTracker;
import evaluation.RegionValueCalculator;

/**
 * BreakRegionTask is responsible for breaking a single region with a good attack plan.
 * 
 */
public class BreakRegionTask {

	/**
	 * Calculates a good plan to break the region with minimal deployment. Returns null if no solution was found.
	 * 
	 * @param opponentRegion
	 * @param maxDeployment
	 * @return
	 */
	public static Moves calculateBreakRegionTask(Region opponentRegion, int maxDeployment, int lowerConservativeLevel,
			int upperConservativeLevel) {
		Moves out = new Moves();

		int lowestBoundDeployment = opponentRegion.getArmiesAfterDeployment(lowerConservativeLevel);
		int uppestBoundDeployment = opponentRegion.getArmiesAfterDeployment(upperConservativeLevel);

		for (int deployment = lowestBoundDeployment; deployment <= uppestBoundDeployment; deployment++) {
			Moves solution = calculateBreakRegionMoves(opponentRegion, maxDeployment, deployment);
			if (solution == null) {
				return out;
			} else {
				out = solution;
			}

		}
		return out;

		// int opponentArmies = opponentRegion.getArmiesAfterDeploymentAndIncomingAttacks(lowerConservativeLevel);
		//
		// int neededAttackArmies = (int) Math.ceil(opponentArmies / 0.6);
		// List<Region> ownedNeighbors = opponentRegion.getOwnedNeighbors();
		// List<Region> presortedOwnedNeighbors = RegionValueCalculator.sortDefenseValue(ownedNeighbors);
		// List<Region> sortedOwnedNeighbors = Map.getOrderedListOfRegionsByIdleArmies(presortedOwnedNeighbors);
		//
		// // First deploy and then pull in more territories if necessary.
		// int attackedWithSoFar = 0;
		// for (int i = 0; i < sortedOwnedNeighbors.size(); i++) {
		// if (i == 0) {
		// int neededDeployment = Math.max(0, neededAttackArmies - sortedOwnedNeighbors.get(0).getIdleArmies());
		// int totalDeployment = Math.min(neededDeployment, maxDeployment);
		// if (totalDeployment > 0) {
		// PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(),
		// sortedOwnedNeighbors.get(0), totalDeployment);
		// out.placeArmiesMoves.add(pam);
		// }
		// int attackingArmies = Math.min(neededAttackArmies, sortedOwnedNeighbors.get(0).getIdleArmies()
		// + totalDeployment);
		// out.attackTransferMoves.add(new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
		// sortedOwnedNeighbors.get(0), opponentRegion, attackingArmies));
		// attackedWithSoFar += attackingArmies;
		// } else {
		// // i != 0
		// int stillNeededArmies = neededAttackArmies - attackedWithSoFar;
		// if (stillNeededArmies > 0 && sortedOwnedNeighbors.get(i).getIdleArmies() > 1) {
		// int newAttackingArmies = Math.min(stillNeededArmies, sortedOwnedNeighbors.get(i).getIdleArmies());
		// out.attackTransferMoves.add(new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
		// sortedOwnedNeighbors.get(i), opponentRegion, newAttackingArmies));
		// attackedWithSoFar += newAttackingArmies;
		// }
		// }
		// }
		// if (attackedWithSoFar >= neededAttackArmies) {
		// return out;
		// } else {
		// return null;
		// }
	}

	private static Moves calculateBreakRegionMoves(Region opponentRegion, int maxDeployment, int opponentDeployment) {
		Moves out = new Moves();
		int opponentArmies = opponentRegion.getArmies();
		opponentArmies += opponentDeployment;

		int neededAttackArmies = (int) Math.ceil(opponentArmies / 0.6);
		List<Region> ownedNeighbors = opponentRegion.getOwnedNeighbors();
		List<Region> presortedOwnedNeighbors = RegionValueCalculator.sortDefenseValue(ownedNeighbors);
		List<Region> sortedOwnedNeighbors = Map.getOrderedListOfRegionsByIdleArmies(presortedOwnedNeighbors);

		// First deploy and then pull in more territories if necessary.
		int attackedWithSoFar = 0;
		for (int i = 0; i < sortedOwnedNeighbors.size(); i++) {
			if (i == 0) {
				int neededDeployment = Math.max(0, neededAttackArmies - sortedOwnedNeighbors.get(0).getIdleArmies());
				int totalDeployment = Math.min(neededDeployment, maxDeployment);
				if (totalDeployment > 0) {
					PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(),
							sortedOwnedNeighbors.get(0), totalDeployment);
					out.placeArmiesMoves.add(pam);
				}
				int attackingArmies = Math.min(neededAttackArmies, sortedOwnedNeighbors.get(0).getIdleArmies()
						+ totalDeployment);
				out.attackTransferMoves.add(new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
						sortedOwnedNeighbors.get(0), opponentRegion, attackingArmies));
				attackedWithSoFar += attackingArmies;
			} else {
				// i != 0
				int stillNeededArmies = neededAttackArmies - attackedWithSoFar;
				if (stillNeededArmies > 0 && sortedOwnedNeighbors.get(i).getIdleArmies() > 1) {
					int newAttackingArmies = Math.min(stillNeededArmies, sortedOwnedNeighbors.get(i).getIdleArmies());
					out.attackTransferMoves.add(new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
							sortedOwnedNeighbors.get(i), opponentRegion, newAttackingArmies));
					attackedWithSoFar += newAttackingArmies;
				}
			}
		}
		if (attackedWithSoFar >= neededAttackArmies) {
			return out;
		} else {
			return null;
		}
	}

}
