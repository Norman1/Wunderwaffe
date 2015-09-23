package tasks;

import java.util.List;

import map.Region;
import move.AttackTransferMove;
import move.Moves;
import bot.HistoryTracker;

/**
 * MoveIdleArmiesTask is responsible for helping us getting last order. This happens by moving around armies in a semi
 * reasonable way or by small attacks with 1.
 * 
 */
public class DelayTask {

	/**
	 * 
	 * @param maxMovesBeforeRiskyAttack
	 *            the maximum amount of moves that should happen before the first risky attack.
	 * @return
	 */
	public static Moves calculateDelayTask(Moves movesSoFar, int maxMovesBeforeRiskyAttack,
			int minMovesBeforeRiskyAttack) {
		Moves out = new Moves();
		if (!isRiskyAttackPresent(movesSoFar)) {
			return out;
		}

		int amountOfSafeMoves = calculateAmountOfSafeMoves(movesSoFar);

		// Step 1: Try to move armies next to neutral regions.
		int maximumNewDelays = Math.max(0, maxMovesBeforeRiskyAttack - amountOfSafeMoves);
		for (Region region : HistoryTracker.botState.getVisibleMap().getNonOpponentBorderingBorderRegions()) {
			List<Region> ownedNeighbors = region.getOwnedNeighbors();
			List<Region> sortedDistanceNeighbors = HistoryTracker.botState.getVisibleMap().sortRegionsDistanceToBorder(
					ownedNeighbors);
			int maxPossibleDelays = Math.min(sortedDistanceNeighbors.size(), region.getIdleArmies());
			int delaysToAdd = Math.min(maximumNewDelays, maxPossibleDelays);
			for (int i = 0; i < delaysToAdd; i++) {
				Region regionToTransferTo = sortedDistanceNeighbors.get(i);
				AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(), region,
						regionToTransferTo, 1);
				out.attackTransferMoves.add(atm);
				maximumNewDelays--;
			}
		}
		// TODO verschoben
		// Only add step 2 delays when really needed
		// if (!isRiskyAttackPresent(movesSoFar)) {
		// return out;
		// }

		// Step 2: If the minMovesBeforeRiskyAttack constraint isn't fulfilled
		// then also add delay moves next to the opponent
		int stillNeededDelays = Math.max(0,
				minMovesBeforeRiskyAttack - (amountOfSafeMoves + out.attackTransferMoves.size()));
		for (Region region : HistoryTracker.botState.getVisibleMap().getOpponentBorderingRegions()) {
			List<Region> ownedNeighbors = region.getOwnedNeighbors();
			List<Region> sortedDistanceNeighbors = HistoryTracker.botState.getVisibleMap().sortRegionsDistanceToBorder(
					ownedNeighbors);
			int maxPossibleDelays = Math.min(sortedDistanceNeighbors.size(), region.getIdleArmies());
			int delaysToAdd = Math.min(stillNeededDelays, maxPossibleDelays);
			for (int i = 0; i < delaysToAdd; i++) {
				Region regionToTransferTo = sortedDistanceNeighbors.get(i);
				AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(), region,
						regionToTransferTo, 1);
				out.attackTransferMoves.add(atm);
				stillNeededDelays--;
			}
		}
		return out;
	}

	private static boolean isRiskyAttackPresent(Moves movesSoFar) {
		boolean containsRiskyAttack = false;
		for (AttackTransferMove atm : movesSoFar.attackTransferMoves) {
			if (atm.getToRegion().getPlayerName().equals(HistoryTracker.botState.getOpponentPlayerName())) {
				int attackingArmies = atm.getArmies();
				int maxOpponentArmies = atm.getToRegion().getArmies()
						+ HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
				if (attackingArmies > 1 && attackingArmies * 0.6 <= maxOpponentArmies * 0.7) {
					containsRiskyAttack = true;
				}
			}
		}
		return containsRiskyAttack;
	}

	private static int calculateAmountOfSafeMoves(Moves movesSoFar) {
		int out = 0;
		for (AttackTransferMove atm : movesSoFar.attackTransferMoves) {
			if (atm.getArmies() == 1 || !atm.getToRegion().equals(HistoryTracker.botState.getOpponentPlayerName())) {
				out++;
			} else {
				int maxOpponentArmies = atm.getToRegion().getArmies()
						+ HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
				int attackingArmies = atm.getArmies();
				if (attackingArmies * 0.6 > maxOpponentArmies * 0.7) {
					out++;
				}
			}
		}
		return out;
	}

}
