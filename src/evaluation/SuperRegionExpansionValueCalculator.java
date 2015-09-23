package evaluation;

import heuristics.PlayerExpansionValueHeuristic;

import java.util.ArrayList;
import java.util.List;

import bot.HistoryTracker;
import map.Map;
import map.SuperRegion;

/**
 * This class is responsible for finding out which SuperRegions to expand into. This happens by giving all SuperRegions
 * values. Furthermore this class is used during picking stage.
 * 
 */
public class SuperRegionExpansionValueCalculator {

	
	public static List<SuperRegion> sortSuperRegions(Map mapToUse, String playerName){
		List<SuperRegion> allSuperRegions = new ArrayList<SuperRegion>();
		allSuperRegions.addAll(mapToUse.getSuperRegions());
		List<SuperRegion> sortedSuperRegions = new ArrayList<SuperRegion>();
		mapToUse.setOpponentExpansionValue();
		while(!allSuperRegions.isEmpty()){
			SuperRegion bestSuperRegion = allSuperRegions.get(0);
			double bestValue = 0;
			if(playerName.equals(HistoryTracker.myName)){
				bestValue = bestSuperRegion.getMyExpansionValueHeuristic().expansionValue;
			}else{
				bestValue = bestSuperRegion.getOpponentExpansionValueHeuristic().expansionValue;
			}
			for(SuperRegion superRegion : allSuperRegions){
				double value = 0;
				if(playerName.equals(HistoryTracker.myName)){
					value = superRegion.getMyExpansionValueHeuristic().expansionValue;
				}else{
					value = superRegion.getOpponentExpansionValueHeuristic().expansionValue;
				}
				if(value > bestValue){
					bestSuperRegion = superRegion;
					bestValue = value;
				}
			}
			allSuperRegions.remove(bestSuperRegion);
			sortedSuperRegions.add(bestSuperRegion);
		}
		return sortedSuperRegions;
	}
	
	public static List<SuperRegion> sortAccessibleSuperRegions(Map mapToUse) {
		List<SuperRegion> copy = new ArrayList<>();
		copy.addAll(mapToUse.getSuperRegions());
		List<SuperRegion> out = new ArrayList<>();
		while (!copy.isEmpty()) {
			SuperRegion highestPrioSuperRegion = copy.get(0);
			for (SuperRegion superRegion : copy) {
				if (superRegion.getExpansionValue() > highestPrioSuperRegion.getExpansionValue()) {
					highestPrioSuperRegion = superRegion;
				}
			}
			copy.remove(highestPrioSuperRegion);
			out.add(highestPrioSuperRegion);
		}
		// Remove the non accessible SuperRegions
		List<SuperRegion> nonAccessibleSuperRegions = new ArrayList<>();
		for (SuperRegion superRegion : mapToUse.getSuperRegions()) {
			if (superRegion.getOwnedSubRegionsAndNeighbors().size() == 0) {
				nonAccessibleSuperRegions.add(superRegion);
			}
		}
		out.removeAll(nonAccessibleSuperRegions);
		return out;
	}

	public static void addExtraValueForFirstTurnBonus(SuperRegion superRegion) {
		superRegion.getMyExpansionValueHeuristic().addExtraValueForFirstTurnBonus(superRegion);

	}

	/**
	 * Classifies the SuperRegion according to the intel from the temporaryMap. However the results of the
	 * classification aren't written to the temporary map but to the visible map.
	 * 
	 * @param temporaryMap
	 */
	public static void classifySuperRegions(Map temporaryMap, Map mapToWriteIn) {
		for (SuperRegion superRegion : temporaryMap.getSuperRegions()) {
			superRegion.setMyExpansionValueHeuristic();
			superRegion.setOpponentExpansionValueHeuristic();

			// Categorize the expansion values. Possible values are 0 = rubbish
			// and 1 = good
			boolean toMuchNeutrals = false;
			if (superRegion.getNeutralArmies() > 14) {
				toMuchNeutrals = true;
			} else if (superRegion.getNeutralArmies() >= 10 && superRegion.getArmiesReward() <= 3) {
				toMuchNeutrals = true;
			} else if (superRegion.getNeutralArmies() >= 8 && superRegion.getArmiesReward() <= 2) {
				toMuchNeutrals = true;
			} else if (superRegion.getNeutralArmies() >= 6 && superRegion.getArmiesReward() <= 1) {
				toMuchNeutrals = true;
			}

			if (superRegion.isOwnedByMyself() || superRegion.getArmiesReward() == 0
					|| superRegion.containsOpponentPresence() || toMuchNeutrals) {
				mapToWriteIn.getSuperRegion(superRegion.getId()).setExpansionValueCategory(0);
			} else {
				mapToWriteIn.getSuperRegion(superRegion.getId()).setExpansionValueCategory(1);
			}

		}
	}
}
