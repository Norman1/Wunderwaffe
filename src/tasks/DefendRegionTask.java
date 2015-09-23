package tasks;

import java.util.ArrayList;
import java.util.List;

import map.Region;
import move.AttackTransferMove;
import move.Moves;
import move.PlaceArmiesMove;
import bot.HistoryTracker;

public class DefendRegionTask {

	/**
	 * Returns the needed moves to defend the region. If not possible then returns null. If no defense needed returns
	 * empty moves. First tries to fulfill the needed armies with background armies.
	 * 
	 * @param regionToDefend
	 * @param maxDeployment
	 * @return
	 */
	public static Moves calculateDefendRegionTask(Region regionToDefend, int maxDeployment,
			boolean useBackgroundArmies, int lowerConservativeLevel, int upperConservativeLevel) {
		Moves out = new Moves();

		int maxOpponentDeployment = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState
				.getVisibleMap());

		for (int i = 0; i < maxOpponentDeployment; i++) {
			Moves defendMoves = calculateDefendRegionMovesk(regionToDefend, maxDeployment, useBackgroundArmies, i,
					lowerConservativeLevel, upperConservativeLevel);
			if (defendMoves != null) {
				out = defendMoves;
			} else {
				return out;
			}
		}
		return null;

		// int upperOpponentDeployment =

		// int maxAttackingArmies = 0;
		// for (Region opponentNeighbor : regionToDefend.getOpponentNeighbors()) {
		// int opponentArmies = opponentNeighbor.getArmiesAfterDeployment(lowerConservativeLevel);
		// int idleArmies = opponentArmies - 1;
		// maxAttackingArmies += idleArmies;
		// }
		// int opponentKills = (int) Math.ceil(maxAttackingArmies * 0.6);
		// int ownArmies = regionToDefend.getArmiesAfterDeploymentAndIncomingMoves();
		// int missingArmies = Math.max(0, opponentKills - ownArmies + 1);
		//
		// // First try to pull in more armies
		// if (missingArmies > 0 && useBackgroundArmies) {
		// List<Region> neighborsWithIdleArmies = getNeighborsWithIdleArmies(regionToDefend);
		// for (Region neighbor : neighborsWithIdleArmies) {
		// int armiesToTransfer = Math.min(missingArmies, neighbor.getIdleArmies());
		// if (armiesToTransfer > 0) {
		// AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
		// neighbor, regionToDefend, armiesToTransfer);
		// out.attackTransferMoves.add(atm);
		// missingArmies -= armiesToTransfer;
		// }
		// }
		// }
		//
		// // Then try to deploy
		// if (missingArmies <= maxDeployment && missingArmies > 0) {
		// PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(), regionToDefend,
		// missingArmies);
		// out.placeArmiesMoves.add(pam);
		// } else if (missingArmies > maxDeployment) {
		// return null;
		// }
		// return out;
	}

	private static Moves calculateDefendRegionMovesk(Region regionToDefend, int maxDeployment,
			boolean useBackgroundArmies, int step, int lowerConservativeLevel, int upperConservativeLevel) {
		Moves out = new Moves();

		int maxAttackingArmies = 0;
		int currentDeployment = 0;
		for (Region opponentNeighbor : regionToDefend.getOpponentNeighbors()) {
			currentDeployment += opponentNeighbor.getTotalDeployment(lowerConservativeLevel);
			int opponentArmies = opponentNeighbor.getArmiesAfterDeployment(lowerConservativeLevel);
			int upperOpponentArmies = opponentNeighbor.getArmiesAfterDeployment(upperConservativeLevel);
			int deploymentDifference = upperOpponentArmies - opponentArmies;

			for (int i = 0; i < step; i++) {
				if (deploymentDifference > 0) {
					deploymentDifference--;
					opponentArmies++;
					currentDeployment++;
				}
			}

			int idleArmies = opponentArmies - 1;
			maxAttackingArmies += idleArmies;
		}

		// Adjust stuff so opponent can't deploy eyerything to every region
		int maxOpponentDeployment = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState
				.getVisibleMap());
		int deploymentDifference = maxOpponentDeployment - currentDeployment;
		maxAttackingArmies -= deploymentDifference;
		// if (deploymentDifference > 0) {
		// System.err.println("deploymentDifference = " + deploymentDifference + " !!!!!!!!!!!!!!!!!!!!!!!!!");
		// }

		int opponentKills = (int) Math.ceil(maxAttackingArmies * 0.6);
		int ownArmies = regionToDefend.getArmiesAfterDeploymentAndIncomingMoves();
		int missingArmies = Math.max(0, opponentKills - ownArmies + 1);

		// First try to pull in more armies
		if (missingArmies > 0 && useBackgroundArmies) {
			List<Region> neighborsWithIdleArmies = getNeighborsWithIdleArmies(regionToDefend);
			for (Region neighbor : neighborsWithIdleArmies) {
				int armiesToTransfer = Math.min(missingArmies, neighbor.getIdleArmies());
				if (armiesToTransfer > 0) {
					AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
							neighbor, regionToDefend, armiesToTransfer);
					out.attackTransferMoves.add(atm);
					missingArmies -= armiesToTransfer;
				}
			}
		}

		// Then try to deploy
		if (missingArmies <= maxDeployment && missingArmies > 0) {
			PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(), regionToDefend,
					missingArmies);
			out.placeArmiesMoves.add(pam);
		} else if (missingArmies > maxDeployment) {
			return null;
		}
		return out;
	}

	private static List<Region> getNeighborsWithIdleArmies(Region regionToDefend) {
		List<Region> unsortedNeighbors = new ArrayList<>();
		for (Region ownedNeighbor : regionToDefend.getOwnedNeighbors()) {
			if (ownedNeighbor.getOpponentNeighbors().size() == 0 && ownedNeighbor.getIdleArmies() > 0) {
				unsortedNeighbors.add(ownedNeighbor);
			}
		}
		// Sort according to the amount of idle armies
		List<Region> out = new ArrayList<>();
		while (!unsortedNeighbors.isEmpty()) {
			Region biggestIdleArmyRegion = unsortedNeighbors.get(0);
			for (Region region : unsortedNeighbors) {
				if (region.getIdleArmies() > biggestIdleArmyRegion.getIdleArmies()) {
					biggestIdleArmyRegion = region;
				}
			}
			out.add(biggestIdleArmyRegion);
			unsortedNeighbors.remove(biggestIdleArmyRegion);
		}
		return out;
	}

}
