package evaluation;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.Region;
import bot.HistoryTracker;

/**
 * This class is responsible for updating the ExpansionMap. The expansion map is
 * similar to the visible map with the exception that regions in which we
 * expanded are marked as owned. This allows us to not transfer in the direction
 * on neutrals that we are taking this turn and it allows us to transfer from
 * another spot to a region that we are taking this turn.
 * 
 */
public class ExpansionMapUpdater {

	public static void updateExpansionMap() {
		HistoryTracker.botState.setExpansionMap(HistoryTracker.botState.getVisibleMap().getMapCopy());
		Map visibleMap = HistoryTracker.botState.getVisibleMap();
		Map expansionMap = HistoryTracker.botState.getExpansionMap();

		List<Region> vmNeutralRegions = visibleMap.getNeutralRegions();
		List<Region> vmNeutralRegionsThatWeTake = new ArrayList<>();
		// find out which regions we are taking by expansion
		for (Region vmNeutralRegion : vmNeutralRegions) {
			if (vmNeutralRegion.isVisible()) {
				int attackingArmies = vmNeutralRegion.getIncomingArmies();
				if (Math.round(attackingArmies * 0.6) >= Math.round(vmNeutralRegion.getArmies())) {
					vmNeutralRegionsThatWeTake.add(vmNeutralRegion);
				}
			}
		}
		// update the expansionMap according to our expansion
		for (Region vmTakenRegion : vmNeutralRegionsThatWeTake) {
			Region emTakenRegion = expansionMap.getRegion(vmTakenRegion.getId());
			emTakenRegion.setPlayerName(HistoryTracker.myName);
			emTakenRegion.setArmies(vmTakenRegion.getIncomingArmies()
					- (int) Math.round(vmTakenRegion.getArmies() * 0.7));
		}
	}

}
