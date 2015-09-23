package heuristics;

import java.util.ArrayList;
import java.util.List;

import evaluation.ExpansionMapUpdater;
import map.Region;
import map.SuperRegion;
import bot.HistoryTracker;

/**
 * The heuristic expansion value of a SuperRegion.
 *
 */
public class SuperRegionExpansionValueHeuristic {

	public double expansionValue = 0.0;
	private double incomeNeutralsRatio = 0.0;
	public int startingArmies = 5;

	// public static final int HIGH = 1000;
	// public static final int LOW = 10;

	public SuperRegionExpansionValueHeuristic(SuperRegion superRegion, String playerName) {
		setIncomeNeutralsRatio(superRegion);
		setExpansionValue2(superRegion, playerName);
	}

	// public void addExtraValueForFirstTurnBonus(SuperRegion superRegion) {
	// int neutrals = superRegion.getNeutralArmies();
	// double initialValue = incomeNeutralsRatio * HIGH;
	// double addition = 0.0;
	// if (neutrals <= 4) {
	// addition = initialValue * 0.8;
	// } else if (neutrals <= 6) {
	// addition = initialValue * 0.6;
	// } else {
	// addition = initialValue * 0.4;
	// }
	// if (superRegion.getArmiesReward() <= 1) {
	// addition *= 0.6;
	// } else if (superRegion.getArmiesReward() <= 2) {
	// addition *= 0.9;
	// }
	//
	// expansionValue += addition;
	// }

	private void setIncomeNeutralsRatio(SuperRegion superRegion) {
		double income = superRegion.getArmiesReward();
		double neutrals = superRegion.getNeutralArmies();
		this.incomeNeutralsRatio = income / neutrals;
	}

	public void setExpansionValue2(SuperRegion superRegion, String playerName) {
		this.expansionValue = 0.0;
		if (isExpansionWorthles(superRegion, playerName)) {
			return;
		}
		setIncomeNeutralsRatio(superRegion);
		double points = incomeNeutralsRatio * 200;

		if (HistoryTracker.botState.getRoundNumber() == 0) {
			if (playerName.equals(HistoryTracker.myName) && superRegion.getNeutralArmies() <= 6
					&& superRegion.areAllRegionsVisible()) {
			points +=	addExtraValueForFirstTurnBonus(superRegion);
			} else if (playerName.equals(HistoryTracker.opponentName) && superRegion.getNeutralArmies() <= 6
					&& superRegion.areAllRegionsVisibleToOpponent()) {
			points +=	addExtraValueForFirstTurnBonus(superRegion);
			}
		}

		if (superRegion.getNeutralArmies() > 8) {
			points -= superRegion.getNeutralArmies() * 4.5;
		} else if (superRegion.getNeutralArmies() > 6) {
			points -= superRegion.getNeutralArmies() * 3.5;
		} else if (superRegion.getNeutralArmies() > 4) {
			points -= superRegion.getNeutralArmies() * 2.5;
		}

		points -= 0.5 * superRegion.getSubRegions().size();

		int immediatelyCounteredRegions = 0;
		if (playerName.equals(HistoryTracker.myName)) {
			immediatelyCounteredRegions = superRegion.getOwnedRegionsBorderingOpponentNeighbors().size();
		} else {
			immediatelyCounteredRegions = superRegion.getOpponentRegionsBorderingOwnedNeighbors().size();
		}

		points -= 7 * immediatelyCounteredRegions;
		int allCounteredRegions = getCounteredRegions(superRegion, playerName);
		points -= 4 * allCounteredRegions;

		List<SuperRegion> neighborSuperRegions = superRegion.getNeighborSuperRegions();
		for (SuperRegion neighborSuperRegion : neighborSuperRegions) {
			if ((neighborSuperRegion.containsOpponentPresence() && playerName.equals(HistoryTracker.myName))
					|| (neighborSuperRegion.containsOwnPresence() && playerName.equals(HistoryTracker.opponentName))) {
				points -= 1;
			} else if (neighborSuperRegion.getOwnedSubRegions().size() > 0) {
				points += 0.5;
			} else {
				points -= 0.4;
			}
		}

		if (allCounteredRegions > 0) {
			points -= 7;
		}
		if (immediatelyCounteredRegions > 0) {
			double abs = Math.abs(points * 0.1);
			points -= abs;
		}

		// double value = this.incomeNeutralsRatio * HIGH;
		// double deductions = getDeductions(superRegion, playerName, value);
		// double additions = getAdditions(superRegion, playerName, value);
		// value += additions;
		// value -= deductions;
		this.expansionValue = points;

	}

	private double getAdditions(SuperRegion superRegion, String playerName, double initialValue) {
		double additions = 0.0;

		// First turn bonus in picking stage
		if (HistoryTracker.botState.getRoundNumber() == 0) {
			if (playerName.equals(HistoryTracker.myName) && superRegion.getNeutralArmies() <= 6
					&& superRegion.areAllRegionsVisible()) {
				additions += initialValue * 0.8;
			} else if (playerName.equals(HistoryTracker.opponentName) && superRegion.getNeutralArmies() <= 6
					&& superRegion.areAllRegionsVisibleToOpponent()) {
				additions += initialValue * 0.8;
			}
		}

		if (superRegion.getNeutralArmies() <= 2) {
			additions += initialValue * 0.3;
		} else if (superRegion.getNeutralArmies() <= 4) {
			additions += initialValue * 0.25;
		} else if (superRegion.getNeutralArmies() <= 6) {
			additions += initialValue * -0.2;
		} else if (superRegion.getNeutralArmies() <= 8) {
			additions += initialValue * -0.3;
		} else {
			additions += initialValue * -0.4;
		}

		boolean otherPlayerNeighbors = false;
		for (SuperRegion neighborSuperRegion : superRegion.getNeighborSuperRegions()) {
			if ((neighborSuperRegion.containsOpponentPresence() && playerName.equals(HistoryTracker.myName))
					|| (neighborSuperRegion.containsOwnPresence() && playerName.equals(HistoryTracker.opponentName))) {
				otherPlayerNeighbors = true;
			}
		}
		if (!otherPlayerNeighbors) {
			additions += initialValue * 0.2;
		}

		additions += Math.max(0, 7 - superRegion.getSubRegions().size());

		return additions;
	}

	private double getDeductions(SuperRegion superRegion, String playerName, double initialValue) {
		boolean containsWasteland = false;
		for (Region subRegion : superRegion.getSubRegions()) {
			if (subRegion.getArmies() == 6
					&& (subRegion.getPlayerName().equals("unknown") || subRegion.getPlayerName().equals("neutral"))) {
				containsWasteland = true;
			}
		}
		int immediatelyCounteredRegions = 0;
		if (playerName.equals(HistoryTracker.myName)) {
			immediatelyCounteredRegions = superRegion.getOwnedRegionsBorderingOpponentNeighbors().size();
		} else {
			immediatelyCounteredRegions = superRegion.getOpponentRegionsBorderingOwnedNeighbors().size();
		}
		int allCounteredRegions = getCounteredRegions(superRegion, playerName);
		boolean opponentCanEasilyBreak = canOtherPlayerEasilyBreak(superRegion, playerName);

		double easyBreakDeduction = opponentCanEasilyBreak ? initialValue * 0.4 : 0;
		double counteredRegionsDeduction = 0;
		if (allCounteredRegions == 1) {
			counteredRegionsDeduction = initialValue * 0.15;
		} else if (allCounteredRegions == 2) {
			counteredRegionsDeduction = initialValue * 0.2;
		} else if (allCounteredRegions == 3) {
			counteredRegionsDeduction = initialValue * 0.25;
		}

		double immediateCounterDeduction = 0;
		if (immediatelyCounteredRegions == 1) {
			immediateCounterDeduction = initialValue * 0.2;
		} else if (immediatelyCounteredRegions == 2) {
			immediateCounterDeduction = initialValue * 0.25;
		} else if (immediatelyCounteredRegions == 3) {
			immediateCounterDeduction = initialValue * 0.3;
		}

		double wastelandDeduction = 0;
		if (HistoryTracker.botState.getRoundNumber() == 0 && containsWasteland) {
			wastelandDeduction = initialValue * 0.9;
		}

		double maxDeduction = 0.0;
		maxDeduction = Math.max(Math.max(wastelandDeduction, immediateCounterDeduction),
				Math.max(easyBreakDeduction, counteredRegionsDeduction));

		if (superRegion.getNeutralArmies() > 8) {
			maxDeduction += initialValue * 0.1;
			maxDeduction += superRegion.getNeutralArmies();
		}

		return maxDeduction;
	}

	private boolean canOtherPlayerEasilyBreak(SuperRegion superRegion, String playerName) {
		int otherPlayerArmiesIdleArmies = 0;
		List<Region> otherPlayerRegions = new ArrayList<Region>();
		if (playerName.equals(HistoryTracker.myName)) {
			otherPlayerRegions = superRegion.getOpponentNeighbors();
		} else {
			otherPlayerRegions = superRegion.getOwnedNeighborRegions();
		}
		for (Region region : otherPlayerRegions) {
			otherPlayerArmiesIdleArmies += region.getArmies() - 1;
		}
		return otherPlayerArmiesIdleArmies >= 7 ? true : false;
	}

	private boolean isExpansionWorthles(SuperRegion superRegion, String playerName) {
		boolean worthless = false;
		if (superRegion.getArmiesReward() == 0) {
			worthless = true;
		}
		if ((playerName.equals(HistoryTracker.opponentName) && superRegion.containsOwnPresence())
				|| (playerName.equals(HistoryTracker.myName) && superRegion.containsOpponentPresence())) {
			worthless = true;
		}

		if ((playerName.equals(HistoryTracker.myName) && superRegion.isOwnedByMyself())
				|| (playerName.equals(HistoryTracker.opponentName) && superRegion.isOwnedByOpponent())) {
			worthless = true;
		}

		return worthless;
	}

	private int getCounteredRegions(SuperRegion superRegion, String playerName) {
		int out = 0;
		for (Region region : superRegion.getSubRegions()) {
			if (region.getOpponentNeighbors().size() > 0 && playerName.equals(HistoryTracker.myName)) {
				out++;
			} else if (region.getOwnedNeighbors().size() > 0 && playerName.equals(HistoryTracker.opponentName)) {
				out++;
			}
		}
		return out;
	}

	// public static int getSuperRegionValue(Map temporaryMap, SuperRegion superRegion) {
	// double incomeNeutralsRatio = getIncomeNeutralsRatio(superRegion);
	// int immediatelyCounteredRegions = superRegion.getOwnedRegionsBorderingOpponentNeighbors().size();
	// int allCounteredRegions = getCounteredRegions(superRegion);
	// int neutrals = superRegion.getNeutralArmies();
	// int allTerritories = superRegion.getSubRegions().size();
	//
	// double points = incomeNeutralsRatio * 200;
	// if (neutrals > 8) {
	// points -= neutrals * 4.5;
	// } else if (neutrals > 6) {
	// points -= neutrals * 3.5;
	// } else if (neutrals > 4) {
	// points -= neutrals * 2.5;
	// }
	// points -= 0.5 * allTerritories;
	// points -= 9 * immediatelyCounteredRegions;
	// points -= 5 * allCounteredRegions;
	//
	// List<SuperRegion> neighborSuperRegions = superRegion.getNeighborSuperRegions();
	// for (SuperRegion neighborSuperRegion : neighborSuperRegions) {
	// if (neighborSuperRegion.containsOpponentPresence()) {
	// points -= 1;
	// } else if (neighborSuperRegion.getOwnedSubRegions().size() > 0) {
	// points += 0.5;
	// } else {
	// points -= 0.4;
	// }
	// }
	//
	// if (allCounteredRegions > 0) {
	// points -= 12;
	// }
	// if (immediatelyCounteredRegions > 0) {
	// double abs = Math.abs(points * 0.2);
	// points -= abs;
	// }
	//
	// return (int) points;
	// }

	public double addExtraValueForFirstTurnBonus(SuperRegion superRegion) {
		int neutrals = superRegion.getNeutralArmies();
		if (neutrals <= 4) {
			expansionValue += superRegion.getArmiesReward() * 15;
//			expansionValue += 20;
			// superRegion.setExpansionValue(superRegion.getExpansionValue() + superRegion.getArmiesReward() * 15);
			return  superRegion.getArmiesReward() * 15;
		} else if (neutrals <= 6) {
			expansionValue += superRegion.getArmiesReward() * 7;
			// superRegion.setExpansionValue(superRegion.getExpansionValue() + superRegion.getArmiesReward() * 7);
			return superRegion.getArmiesReward() * 7;
		} else {
			expansionValue += superRegion.getArmiesReward() * 1;
			// superRegion.setExpansionValue(superRegion.getExpansionValue() + superRegion.getArmiesReward() * 5);
			return superRegion.getArmiesReward() * 1;
		}

	}

	// private static int getCounteredRegions(SuperRegion superRegion) {
	// int out = 0;
	// for (Region region : superRegion.getSubRegions()) {
	// if (region.getOpponentNeighbors().size() > 0) {
	// out++;
	// }
	// }
	// return out;
	// }

	@Override
	public String toString() {
		String objectDescription = "SuperRegionExpansionValue: " + Double.toString(expansionValue);
		return objectDescription;
	}

}
