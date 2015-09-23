package strategy;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.Region;
import bot.BotState;
import bot.HistoryTracker;
import debug.Debug;
import evaluation.PicksEvaluator;

public class StartingRegionsChooser {

	private static List<Integer> pickedStartingRegions = new ArrayList<>();
	public static List<Integer> opponentPickedStartingRegions = new ArrayList<Integer>();
	private static Map startingRegionMap = null;

	public static Region getStartingRegion(BotState state) {
		HistoryTracker.botState = state;
		if (startingRegionMap == null) {
			init(state);
		}
		addAlreadyPickedOpponentRegions(state);
		
		int minMax2Pick = PicksEvaluator.getNextPick(startingRegionMap);
		Region nextBestRegion = startingRegionMap.getRegion(minMax2Pick);

		startingRegionMap.getRegion(nextBestRegion.getId()).setPlayerName(state.getMyPlayerName());
		pickedStartingRegions.add(nextBestRegion.getId());
		updateOpponentPickedStartingRegions(state);
		Debug.printSuperRegionExpansionValues(startingRegionMap);
		System.err.println("StartingArmies: " + HistoryTracker.botState.getStartingArmies());
		System.err.println();
		return nextBestRegion;
	}

	/**
	 * Updates the opponent picked starting regions for the fog remover component afterwards.
	 * 
	 * @param state
	 */
	private static void updateOpponentPickedStartingRegions(BotState state) {
		List<Integer> opponentRegionIDs = Map.getRegionIDs(startingRegionMap.getOpponentRegions());
		List<Integer> ourRegionIDs = pickedStartingRegions;
		List<Integer> allStartingRegionIDs = Map.getRegionIDs(state.getAllStartingRegions());
		int picksAmount = state.getStartingPicksAmount();

		// Normal case. Add the known opponent spots so far
		opponentPickedStartingRegions.clear();
		opponentPickedStartingRegions.addAll(opponentRegionIDs);

		// Special case that all SuperRegions will get taken and it's the last deployment step. All possible picks that
		// we don't have belong to the opponent.
		if (ourRegionIDs.size() == picksAmount && picksAmount * 2 == allStartingRegionIDs.size()) {
			opponentPickedStartingRegions.clear();
			for (int regionID : allStartingRegionIDs) {
				if (!ourRegionIDs.contains(regionID)) {
					opponentPickedStartingRegions.add(regionID);
				}
			}
		}
	}

	private static void addAlreadyPickedOpponentRegions(BotState state) {
		List<Region> allStartingRegions = state.getAllStartingRegions();
		List<Region> newPickableStartingRegions = state.getPickableStartingRegions();

		List<Integer> allStartingRegionsIDs = Map.getRegionIDs(allStartingRegions);
		List<Integer> newPickableStartingRegionsIDs = Map.getRegionIDs(newPickableStartingRegions);

		List<Integer> alreadyPickedRegionIDs = new ArrayList<>();
		for (int initialPickableRegion : allStartingRegionsIDs) {
			if (!newPickableStartingRegionsIDs.contains(initialPickableRegion)) {
				alreadyPickedRegionIDs.add(initialPickableRegion);
			}
		}

		for (int pickedRegionID : alreadyPickedRegionIDs) {
			Region theRegion = startingRegionMap.getRegion(pickedRegionID);
			if (theRegion.getPlayerName().equals("neutral") || theRegion.getPlayerName().equals("unknown")) {
				theRegion.setPlayerName(state.getOpponentPlayerName());
			}
		}
	}

	private static void init(BotState state) {
		startingRegionMap = state.getFullMap().getMapCopy();
		List<Region> wastelands = state.getWastelands();
		List<Integer> wastelandIDs = Map.getRegionIDs(wastelands);
		for (Region region : startingRegionMap.getRegions()) {
			if (wastelandIDs.contains(region.getId())) {
				region.setArmies(6);
			} else {
				region.setArmies(2);
			}
		}
	}

}
