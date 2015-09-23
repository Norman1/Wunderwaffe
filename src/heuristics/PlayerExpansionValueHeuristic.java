package heuristics;

import map.Map;
import map.SuperRegion;
import bot.HistoryTracker;

/**
 * PlayerExpansionValueHeuristic represents the expansion value of a player looking at the whole board.
 *
 */
public class PlayerExpansionValueHeuristic {

	public double playerExpansionValue = 0.0;

	public PlayerExpansionValueHeuristic(Map map, String playerName) {
		for (SuperRegion superRegion : map.getSuperRegions()) {
			SuperRegionExpansionValueHeuristic superRegionHeuristic = null;
			if (playerName.equals(HistoryTracker.myName)) {
				if (superRegion.getMyExpansionValueHeuristic() == null) {
					superRegion.setMyExpansionValueHeuristic();
				}
				superRegionHeuristic = superRegion.getMyExpansionValueHeuristic();
			} else {
				if (superRegion.getOpponentExpansionValueHeuristic() == null) {
					superRegion.setOpponentExpansionValueHeuristic();
				}
				superRegionHeuristic = superRegion.getOpponentExpansionValueHeuristic();
			}
			playerExpansionValue += superRegionHeuristic.expansionValue;
		}
	}

	@Override
	public String toString() {
		String objectDescription = "PlayerExpansionValue: " + Double.toString(playerExpansionValue);
		return objectDescription;
	}
}
