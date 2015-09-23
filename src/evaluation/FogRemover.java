package evaluation;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.Region;
import map.SuperRegion;
import strategy.StartingRegionsChooser;
import bot.HistoryTracker;

public class FogRemover {

	public static List<SuperRegion> superRiskyGuesses = new ArrayList<SuperRegion>();

	public static void removeFog() {
		if (HistoryTracker.botState.getRoundNumber() == 1) {
			removeFogAfterPicks();
			return;
		}
		removeLastTurnSuperHeuristicGuesses();
		removeFogLostSpots();
		removeFogOpponentDeployment();
		removeUnknownFog();
		// removeFogHeuristic();
		removeFogSuperHeuristic();
	}
	
	private static void removeLastTurnSuperHeuristicGuesses(){
		for(SuperRegion superRegion : superRiskyGuesses){
			SuperRegion vmSuperRegion = HistoryTracker.botState.getVisibleMap().getSuperRegion(superRegion.getId());
			for(Region subRegion : vmSuperRegion.getSubRegions()){
				if(!subRegion.isVisible()){
					subRegion.setPlayerName("neutral");
					subRegion.setOwnershipHeuristic(false);
				}
			}
		}
		superRiskyGuesses = new ArrayList<SuperRegion>();
	}

	/**
	 * If round number > 1, a bonus has a value of >=2 and he is only missing one spot on which we have no intel then
	 * assume that he has the full bonus.
	 */
	@Deprecated
	private static void removeFogHeuristic() {
		for (SuperRegion superRegion : HistoryTracker.botState.getVisibleMap().getSuperRegions()) {
			if (superRegion.getArmiesReward() >= 2 && superRegion.canBeOwnedByOpponent()) {
				List<Region> missingRegions = new ArrayList<>();
				for (Region region : superRegion.getSubRegions()) {
					if (region.getPlayerName().equals("neutral")) {
						missingRegions.add(region);
					}
				}
				if (missingRegions.size() == 1 && missingRegions.get(0).getArmies() <= 2) {
					Region missingRegion = missingRegions.get(0);
					missingRegion.setPlayerName(HistoryTracker.botState.getOpponentPlayerName());
					missingRegion.setOwnershipHeuristic(true);
				} else if (missingRegions.size() == 2 && superRegion.getNeutralArmies() <= 4
						&& superRegion.getSubRegions().size() >= 4) {
					for (Region missingRegion : missingRegions) {
						missingRegion.setPlayerName(HistoryTracker.botState.getOpponentPlayerName());
						missingRegion.setOwnershipHeuristic(true);
					}
				}
			}
		}
	}

	private static void removeFogSuperHeuristic() {
		List<SuperRegion> possibleOpponentSuperRegions = new ArrayList<SuperRegion>();
		for (SuperRegion superRegion : HistoryTracker.botState.getVisibleMap().getSuperRegions()) {
			if (superRegion.getArmiesReward() > 0 && !superRegion.isOwnedByOpponent()
					&& superRegion.canBeOwnedByOpponent()) {
				possibleOpponentSuperRegions.add(superRegion);
			}
		}
		List<SuperRegion> sortedSuperRegions = SuperRegionExpansionValueCalculator.sortSuperRegions(
				HistoryTracker.botState.getVisibleMap(), HistoryTracker.botState.getOpponentPlayerName());
		List<SuperRegion> sortedPossibleSuperRegions = new ArrayList<SuperRegion>();
		for (SuperRegion superRegion : sortedSuperRegions) {
			if (possibleOpponentSuperRegions.contains(superRegion)) {
				sortedPossibleSuperRegions.add(superRegion);
			}
		}
		int stillMissingDeployment = HistoryTracker.getDeploymentHistory().getMissingDeployment(6);
		for (SuperRegion possibleSuperRegion : sortedPossibleSuperRegions) {
			int neededTakeoverDeployment = getNeededTakeoverDeployment(possibleSuperRegion);
			if (stillMissingDeployment > neededTakeoverDeployment) {
				superRiskyGuesses.add(possibleSuperRegion);
				stillMissingDeployment -= neededTakeoverDeployment;
				System.err.println("added super risky guess for SuperRegion"+possibleSuperRegion);
			}
		}

		for (SuperRegion superRegion : superRiskyGuesses) {
			for (Region region : superRegion.getSubRegions()) {
				region.setPlayerName(HistoryTracker.opponentName);
				region.setOwnershipHeuristic(true);
			}
		}
	}

	private static int getNeededTakeoverDeployment(SuperRegion superRegion) {
		int neutrals = superRegion.getNeutralArmies();
		int initialArmies = 1;
		double neededArmies = neutrals * 1.5;
		neededArmies -= initialArmies;
		return (int) Math.round(neededArmies);
	}

	private static void removeUnknownFog() {
		for (Region region : HistoryTracker.botState.getVisibleMap().getRegions()) {
			if (region.getPlayerName().equals("unknown")) {
				Region lwmRegion = HistoryTracker.botState.getLastVisibleMap().getRegion(region.getId());
				if (lwmRegion.getPlayerName().equals("neutral")) {
					region.setPlayerName("neutral");
					region.setArmies(lwmRegion.getArmies());
				}
			}
		}
	}

	private static void removeFogOpponentDeployment() {

		/*
		 * Copy the old ownership heuristics
		 */
		for (Region oldRegion : HistoryTracker.botState.lastVisibleMap.getRegions()) {
			Region vmRegion = HistoryTracker.botState.getVisibleMap().getRegion(oldRegion.getId());
			vmRegion.setOwnershipHeuristic(oldRegion.isOwnershipHeuristic());
		}

		/*
		 * Set all regions to ownership heuristic = false on which we have direct intel
		 */
		for (Region region : HistoryTracker.botState.getVisibleMap().getRegions()) {
			if (region.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())
					|| region.getOwnedNeighbors().size() > 0) {
				region.setOwnershipHeuristic(false);
			}
		}

		int opponentDeployment = HistoryTracker.getOpponentDeployment();
		List<SuperRegion> sortedPossibleSuperRegions = sortSuperRegionsNeutralCount();
		int stillMissingIncome = opponentDeployment - 5;
		List<SuperRegion> guessedSuperRegions = new ArrayList<>();
		for (SuperRegion superRegion : sortedPossibleSuperRegions) {
			if (stillMissingIncome > 0) {
				guessedSuperRegions.add(superRegion);
				stillMissingIncome -= superRegion.getArmiesReward();
			}
		}
		// Switch to the visible map
		List<SuperRegion> visibleMapSuperRegions = new ArrayList<>();
		for (SuperRegion lvmSuperRegion : guessedSuperRegions) {
			SuperRegion vmSuperRegion = HistoryTracker.botState.getVisibleMap().getSuperRegion(lvmSuperRegion.getId());
			visibleMapSuperRegions.add(vmSuperRegion);
		}
		// Calculate the non possible SuperRegions
		List<SuperRegion> brokenSuperRegions = new ArrayList<>();
		List<SuperRegion> wrongGuesses = new ArrayList<>();
		for (SuperRegion vmGuessedSuperRegion : visibleMapSuperRegions) {
			if (vmGuessedSuperRegion.getOwnedSubRegions().size() > 0) {
				brokenSuperRegions.add(vmGuessedSuperRegion);
			} else if (!vmGuessedSuperRegion.canBeOwnedByOpponent()) {
				wrongGuesses.add(vmGuessedSuperRegion);
			}
		}
		visibleMapSuperRegions.removeAll(brokenSuperRegions);
		visibleMapSuperRegions.removeAll(wrongGuesses);
		/*
		 * Add the regions of the guessed SuperRegions to the opponent spot. If they are not already for sure owned then
		 * flag them as unsure
		 */
		for (SuperRegion guessedSuperRegion : visibleMapSuperRegions) {
			for (Region guessedRegion : guessedSuperRegion.getSubRegions()) {
				if (!guessedRegion.getPlayerName().equals(HistoryTracker.botState.getOpponentPlayerName())) {
					guessedRegion.setPlayerName(HistoryTracker.botState.getOpponentPlayerName());
					guessedRegion.setOwnershipHeuristic(true);
				}
			}
		}
		/*
		 * Do the same thing for the broken SuperRegions
		 */
		for (SuperRegion brokenSuperRegion : brokenSuperRegions) {
			for (Region guessedRegion : brokenSuperRegion.getSubRegions()) {
				if (guessedRegion.getOwnedNeighbors().size() == 0
						&& !guessedRegion.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())) {
					guessedRegion.setPlayerName(HistoryTracker.botState.getOpponentPlayerName());
					guessedRegion.setOwnershipHeuristic(true);
				}
			}
		}

		/*
		 * Remove all heuristic regions form SuperRegions that the opponent can't have (and we didn't break)
		 */
		for (SuperRegion superRegion : HistoryTracker.botState.getVisibleMap().getSuperRegions()) {
			if (!superRegion.canBeOwnedByOpponent() && !brokenSuperRegions.contains(superRegion)) {
				for (Region region : superRegion.getSubRegions()) {
					if (region.getPlayerName().equals(HistoryTracker.botState.getOpponentPlayerName())
							&& region.isOwnershipHeuristic() && region.getOwnedNeighbors().size() == 0) {
						region.setOwnershipHeuristic(false);
						region.setPlayerName("neutral");
					}
				}
			}
		}
		/*
		 * Remove all uncertainties on regions that we have direct intel on
		 */
		for (Region region : HistoryTracker.botState.getVisibleMap().getRegions()) {
			if (region.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())
					|| region.getOwnedNeighbors().size() > 0) {
				region.setOwnershipHeuristic(false);
			}
		}
	}

	private static void removeFogLostSpots() {
		Map mapThisTurn = HistoryTracker.botState.getVisibleMap();
		Map mapLastTurn = HistoryTracker.botState.getLastVisibleMap();
		for (Region region : mapThisTurn.getRegions()) {
			if (region.getPlayerName().equals("unknown")) {
				Region regionLastTurn = mapLastTurn.getRegion(region.getId());
				if (regionLastTurn.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())
						|| regionLastTurn.getPlayerName().equals(HistoryTracker.botState.getOpponentPlayerName())) {
					region.setPlayerName(HistoryTracker.botState.getOpponentPlayerName());
					region.setArmies(1);
				}
			}
		}
	}

	private static void removeFogAfterPicks() {
		List<Integer> opponentPickedRegions = StartingRegionsChooser.opponentPickedStartingRegions;

		for (int regionID : opponentPickedRegions) {
			Region opponentPickedRegion = HistoryTracker.botState.getVisibleMap().getRegion(regionID);
			if (opponentPickedRegion.getPlayerName().equals("unknown")) {
				opponentPickedRegion.setPlayerName(HistoryTracker.botState.getOpponentPlayerName());
				opponentPickedRegion.setArmies(2);
			}
		}

		for (Region region : HistoryTracker.botState.getVisibleMap().getRegions()) {
			if (region.getPlayerName().equals("unknown")) {
				region.setPlayerName("neutral");
				List<Region> wastelands = HistoryTracker.botState.getWastelands();
				List<Integer> wastelandIDs = new ArrayList<>();
				for (Region wasteland : wastelands) {
					wastelandIDs.add(wasteland.getId());
				}
				if (wastelandIDs.contains(region.getId())) {
					region.setArmies(6);
				} else {
					region.setArmies(2);
				}
			}
		}
	}

	private static List<SuperRegion> sortSuperRegionsNeutralCount() {
		List<SuperRegion> possibleSuperRegions = new ArrayList<>();
		for (SuperRegion superRegion : HistoryTracker.botState.getLastVisibleMap().getSuperRegions()) {
			if (superRegion.canBeOwnedByOpponent()) {
				possibleSuperRegions.add(superRegion);
			}
		}
		boolean hasSomethingChanged = true;
		while (hasSomethingChanged) {
			hasSomethingChanged = false;
			for (int i = 0; i < possibleSuperRegions.size() - 1; i++) {
				SuperRegion superRegion1 = possibleSuperRegions.get(i);
				SuperRegion superRegion2 = possibleSuperRegions.get(i + 1);
				if (getGuessedNeutralsSoFar(superRegion2) < getGuessedNeutralsSoFar(superRegion1)) {
					hasSomethingChanged = true;
					possibleSuperRegions.set(i, superRegion2);
					possibleSuperRegions.set(i + 1, superRegion1);
				}
			}
		}
		// Remove all SuperRegions that give 0 income
		List<SuperRegion> noIncomeSuperRegions = new ArrayList<>();
		for (SuperRegion superRegion : possibleSuperRegions) {
			if (superRegion.getArmiesReward() == 0) {
				noIncomeSuperRegions.add(superRegion);
			}
		}
		possibleSuperRegions.removeAll(noIncomeSuperRegions);
		return possibleSuperRegions;
	}

	private static int getGuessedNeutralsSoFar(SuperRegion superRegion) {
		int out = 0;
		for (Region region : superRegion.getSubRegions()) {
			if (region.getPlayerName().equals("neutral")) {
				out += region.getArmies();
			}
		}
		return out;
	}
}
