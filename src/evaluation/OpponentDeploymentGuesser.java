package evaluation;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.Region;
import move.MovesCommitter;
import move.PlaceArmiesMove;
import bot.HistoryTracker;

public class OpponentDeploymentGuesser {

	public static void guessOpponentDeployment() {
		if (HistoryTracker.botState.getRoundNumber() == 1) {
			for (Region vmRegion : HistoryTracker.botState.getVisibleMap().getOpponentRegions()) {
				int armies = 5;
				PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getOpponentPlayerName(), vmRegion,
						armies);
				MovesCommitter.committPlaceArmiesMove(pam);
			}
			return;
		}

		// Round number != 1
		for (Region vmRegion : HistoryTracker.botState.getVisibleMap().getOpponentRegions()) {
			Region lvmRegion = HistoryTracker.botState.getLastVisibleMap().getRegion(vmRegion.getId());
			int guessedOpponentDeployment = 0;
			if (lvmRegion.isVisible()
					&& lvmRegion.getPlayerName().equals(HistoryTracker.botState.getOpponentPlayerName())) {
				int opponentIncome = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState
						.getVisibleMap());
				guessedOpponentDeployment = Math.min(lvmRegion.getTotalDeployment(1), opponentIncome);
				if (hasDeploymentReasonDisapeared(lvmRegion, vmRegion)) {
					// guessedOpponentDeployment = Math.min(3, guessedOpponentDeployment);
					int boundDeployment = getBoundOpponentDeployment(vmRegion);
					int maxDeployment = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState
							.getVisibleMap()) - boundDeployment;
					// guessedOpponentDeployment = maxDeployment;
					guessedOpponentDeployment = Math.min(5, maxDeployment);
				}
			} else {
				int boundDeployment = getBoundOpponentDeployment(vmRegion);
				int maxDeployment = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState
						.getVisibleMap()) - boundDeployment;
				// guessedOpponentDeployment = maxDeployment;
				guessedOpponentDeployment = Math.max(1, Math.min(5, maxDeployment));
				// guessedOpponentDeployment = 5;
			}
			PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getOpponentPlayerName(), vmRegion,
					guessedOpponentDeployment);
			MovesCommitter.committPlaceArmiesMove(pam);

			PlaceArmiesMove conservativePam = new PlaceArmiesMove(HistoryTracker.botState.getOpponentPlayerName(),
					vmRegion, HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap()));
			MovesCommitter.committPlaceArmiesMove(conservativePam, 2);

		}
	}

	/**
	 * Calculate the opponent deployment that is bound to other regions.
	 * 
	 * @param opponentRegion
	 * @return
	 */
	private static int getBoundOpponentDeployment(Region opponentRegion) {
		List<Region> sortedOpponentRegions = RegionValueCalculator.getSortedAttackValueRegions();
		List<Region> moreImportantRegions = new ArrayList<Region>();
		for (Region region : sortedOpponentRegions) {
			if (region.getId() != opponentRegion.getId()) {
				moreImportantRegions.add(region);
			} else {
				break;
			}
		}
		int boundDeployment = 0;
		int stillAvailableIncome = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState
				.getVisibleMap());
		for (Region region : moreImportantRegions) {
			int armiesNeededThere = getOpponentNeededDeployment(region);
			if (armiesNeededThere <= stillAvailableIncome) {
				boundDeployment += armiesNeededThere;
				stillAvailableIncome -= armiesNeededThere;
			}
		}

		return boundDeployment;
	}

	private static int getOpponentNeededDeployment(Region opponentRegion) {
		int neededDeployment = 0;
		if (opponentRegion.getAttackRegionValue() < RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE) {
			return 0;
		}
		int ourAttackingArmies = 0;
		for (Region ownedNeighbor : opponentRegion.getOwnedNeighbors()) {
			ourAttackingArmies += ownedNeighbor.getArmies() - 1;
		}

		if (opponentRegion.getAttackRegionValue() > RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE) {
			ourAttackingArmies += 5;
		}

		neededDeployment = Math.max(0, (int) Math.round(ourAttackingArmies * 0.6));
		return neededDeployment;
	}

	private static boolean hasDeploymentReasonDisapeared(Region lvmRegion, Region vmRegion) {
		if (lvmRegion.getAttackRegionValue() >= RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE
				&& vmRegion.getAttackRegionValue() < RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE) {
			return true;
		} else if (lvmRegion.getAttackRegionValue() >= RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE
				&& vmRegion.getAttackRegionValue() < RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE) {
			return true;
		}
		return false;
	}

}
