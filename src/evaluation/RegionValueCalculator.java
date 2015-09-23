package evaluation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import map.Map;
import map.Region;
import map.SuperRegion;
import bot.HistoryTracker;

/**
 * This class is responsible for calculating region values for each region. With this information valid decisions can be
 * made where to expand, which regions to defend and which opponent regions to attack.
 */
public class RegionValueCalculator {

	public static final int LOWEST_MEDIUM_PRIORITY_VALUE = 1000;
	public static final int LOWEST_HIGH_PRIORITY_VALUE = 1000000;

	private static final int ATTACK_ADJUSTMENT_FACTOR = 4;
	// TODO: was 3
	private static final int DEFENSE_ADJUSTMENT_FACTOR = 4;

	/**
	 * Sorts the regions from high priority to low priority. If it's an opponent region then the AttackValue is used and
	 * if it's an owned region the defense value is used.
	 * 
	 * @param unsortedRegions
	 *            opponent bordering regions and opponent regions
	 * @return sorted regions
	 */
	public static List<Region> sortRegionsAttackDefense(List<Region> unsortedRegions) {
		List<Region> out = new ArrayList<Region>();
		List<Region> copy = new ArrayList<Region>();
		copy.addAll(unsortedRegions);
		while (!copy.isEmpty()) {
			Region mostImportantRegion = copy.get(0);
			int mostImportantRegionValue = Math.max(mostImportantRegion.getAttackRegionValue(),
					mostImportantRegion.getDefenceRegionValue());
			for (Region region : copy) {
				int regionValue = Math.max(region.getAttackRegionValue(), region.getDefenceRegionValue());
				if (regionValue > mostImportantRegionValue) {
					mostImportantRegion = region;
					mostImportantRegionValue = regionValue;
				}
			}
			copy.remove(mostImportantRegion);
			out.add(mostImportantRegion);
		}
		return out;
	}

	/**
	 * 
	 * @return sorted visible neutral regions with a flanking value of > 0
	 */
	public static List<Region> getSortedFlankingValueRegions() {
		List<Region> out = new ArrayList<Region>();
		List<Region> visibleNeutrals = HistoryTracker.botState.getVisibleMap().getVisibleNeutralRegions();
		List<Region> visibleFlankingRegions = new ArrayList<Region>();
		for (Region visibleNeutral : visibleNeutrals) {
			if (visibleNeutral.getFlankingRegionValue() > 0) {
				visibleFlankingRegions.add(visibleNeutral);
			}
		}
		while (!visibleFlankingRegions.isEmpty()) {
			Region bestRegion = visibleFlankingRegions.get(0);
			for (Region region : visibleFlankingRegions) {
				if (region.getFlankingRegionValue() > bestRegion.getFlankingRegionValue()) {
					bestRegion = region;
				}
			}
			out.add(bestRegion);
			visibleFlankingRegions.remove(bestRegion);
		}
		return out;
	}

	public static List<Region> getSortedAttackValueRegions() {
		List<Region> out = new ArrayList<>();
		List<Region> opponentRegions = HistoryTracker.botState.getVisibleMap().getOpponentRegions();
		List<Region> copy = new ArrayList<>();
		copy.addAll(opponentRegions);
		while (!copy.isEmpty()) {
			int maxAttackValue = 0;
			Region maxAttackValueRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getAttackRegionValue() > maxAttackValue) {
					maxAttackValue = region.getAttackRegionValue();
					maxAttackValueRegion = region;
				}
			}
			copy.remove(maxAttackValueRegion);
			out.add(maxAttackValueRegion);
		}
		return out;
	}

	public static List<Region> getSortedAttackRegions(Region fromRegion) {
		List<Region> out = new ArrayList<>();
		List<Region> sortedOpponentRegions = getSortedAttackValueRegions();
		for (Region opponentRegion : sortedOpponentRegions) {
			if (fromRegion.getNeighbors().contains(opponentRegion)) {
				out.add(opponentRegion);
			}
		}
		return out;
	}

	/**
	 * Only returns the regions next to the opponent.
	 * 
	 * @return
	 */
	public static List<Region> getSortedDefenceValueRegions() {
		List<Region> out = new ArrayList<>();
		List<Region> opponentBorderingRegions = HistoryTracker.botState.getVisibleMap().getOpponentBorderingRegions();
		List<Region> copy = new ArrayList<>();
		copy.addAll(opponentBorderingRegions);
		while (!copy.isEmpty()) {
			int maxDefenceValue = 0;
			Region maxDefenceValueRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getDefenceRegionValue() > maxDefenceValue) {
					maxDefenceValue = region.getDefenceRegionValue();
					maxDefenceValueRegion = region;
				}
			}
			copy.remove(maxDefenceValueRegion);
			out.add(maxDefenceValueRegion);
		}
		return out;
	}

	public static List<Region> sortAttackValue(List<Region> inRegions) {
		List<Region> out = new ArrayList<>();
		List<Region> sortedAttackRegions = getSortedAttackValueRegions();
		for (Region region : sortedAttackRegions) {
			if (inRegions.contains(region)) {
				out.add(region);
			}
		}
		return out;
	}

	/**
	 * If an inRegion isn't next to an opponent it isn't returned
	 * 
	 * @param inRegions
	 * @return
	 */
	public static List<Region> sortDefenseValue(List<Region> inRegions) {
		List<Region> out = new ArrayList<>();
		List<Region> sortedDefenceRegions = getSortedDefenceValueRegions();
		for (Region region : sortedDefenceRegions) {
			if (inRegions.contains(region)) {
				out.add(region);
			}
		}
		return out;
	}

	public static List<Region> sortDefenseValueFullReturn(List<Region> inRegions) {
		List<Region> out = new ArrayList<>();
		List<Region> copy = new ArrayList<>();
		copy.addAll(inRegions);
		while (!copy.isEmpty()) {
			Region highestPrioRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getDefenceRegionValue() > highestPrioRegion.getDefenceRegionValue()) {
					highestPrioRegion = region;
				}
			}
			copy.remove(highestPrioRegion);
			out.add(highestPrioRegion);
		}
		return out;
	}

	public static List<Region> sortExpansionValue(List<Region> inRegions) {
		List<Region> out = new ArrayList<>();
		List<Region> sortedExpansionRegions = getSortedExpansionValueRegions();
		for (Region region : sortedExpansionRegions) {
			if (inRegions.contains(region)) {
				out.add(region);
			}
		}
		return out;
	}

	public static List<Region> getSortedExpansionValueRegions() {
		List<Region> out = new ArrayList<>();
		List<Region> neutralRegions = HistoryTracker.botState.getVisibleMap().getVisibleNeutralRegions();
		List<Region> copy = new ArrayList<>();
		copy.addAll(neutralRegions);
		while (!copy.isEmpty()) {
			int maxExpansionValue = 0;
			Region maxExpansionValueRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getExpansionRegionValue() > maxExpansionValue) {
					maxExpansionValue = region.getExpansionRegionValue();
					maxExpansionValueRegion = region;
				}
			}
			copy.remove(maxExpansionValueRegion);
			out.add(maxExpansionValueRegion);
		}
		return out;
	}

	/**
	 * Calculates the region values.
	 * 
	 * @param mapToWriteIn
	 *            the map in which the region values are to be inserted
	 * @param mapToUse
	 *            the map to use for calculating the values
	 */
	public static void calculateRegionValues(Map mapToWriteIn, Map mapToUse) {
		// TODO doesen't work for some reason
		// HistoryTracker.botState.getVisibleMap().setOpponentExpansionValue();
		// HistoryTracker.botState.getVisibleMap().setMyExpansionValue();
		for (SuperRegion superRegion : mapToUse.getSuperRegions()) {
			superRegion.setMyExpansionValueHeuristic();
			superRegion.setOpponentExpansionValueHeuristic();
		}

		for (Region region : mapToUse.getRegions()) {
			if (region.getPlayerName().equals("neutral")) {
				calculateExpansionRegionValue(region, mapToWriteIn);
				calculateFlankingValue(region, mapToWriteIn, mapToUse);
			} else if (isOwnedBorderRegion(region.getId())) {
				calculateDefenseRegionValue(region, mapToWriteIn);
			}
			if (isOpponentRegion(region.getId()) || isNeutralRegion(region.getId())) {
				calculateAttackRegionValue(region, mapToWriteIn);
			}
		}
	}

	private static boolean isNeutralRegion(int regionId) {
		Region region = HistoryTracker.botState.getVisibleMap().getRegion(regionId);
		if (region.getPlayerName().equals("neutral")) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isOpponentRegion(int regionId) {
		Region region = HistoryTracker.botState.getVisibleMap().getRegion(regionId);
		if (region.getPlayerName().equals(HistoryTracker.opponentName)) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isOwnedBorderRegion(int regionId) {
		Region region = HistoryTracker.botState.getVisibleMap().getRegion(regionId);
		if (region.getPlayerName().equals(HistoryTracker.myName) && region.getOpponentNeighbors().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Calculates the attack region values.
	 * 
	 * @param region
	 *            allowed are neutral and opponent regions
	 */
	public static void calculateAttackRegionValue(Region region, Map mapToWriteIn) {
		int currentValue = 0;
		// Add 100.000 * armies reward to the value if it's a spot in an
		// opponent SuperRegion
		if (region.getSuperRegion().isOwnedByOpponent()) {
			currentValue += LOWEST_HIGH_PRIORITY_VALUE * region.getSuperRegion().getArmiesReward();
		}
		// Add 1000 to the value for each bordering own SuperRegion
		currentValue += region.getAttackRegionValue() + LOWEST_MEDIUM_PRIORITY_VALUE
				* region.getAmountOfBordersToOwnSuperRegion();

		// Add 1000 to the value for each bordering opponent SuperRegion
		currentValue += LOWEST_MEDIUM_PRIORITY_VALUE * region.getAmountOfBordersToOpponentSuperRegion();

		// Add 1000 * armies reward for the opponent having all but one neutral
		// spot in the SuperRegion
		int neutralArmiesInSuperRegion = region.getSuperRegion().getNeutralArmies();
		int amountOfOwnedSubRegions = region.getSuperRegion().getOwnedSubRegions().size();
		if (amountOfOwnedSubRegions == 0 && neutralArmiesInSuperRegion <= 2 && neutralArmiesInSuperRegion > 0) {
			currentValue += region.getSuperRegion().getArmiesReward() * LOWEST_MEDIUM_PRIORITY_VALUE;
		}

		// Add up to 30 to the armies reward for being close to an opponent
		// SuperRegion
		if (region.getDistanceToOpponentSuperRegion() == 1) {
			currentValue += 30;
		} else if (region.getDistanceToOpponentSuperRegion() == 2) {
			currentValue += 20;
		} else if (region.getDistanceToOpponentSuperRegion() == 3) {
			currentValue += 10;
		}
		// Add up to 30 to the armies reward for being close to an own
		// SuperRegion
		if (region.getDistanceToOwnSuperRegion() == 1) {
			currentValue += 30;
		} else if (region.getDistanceToOwnSuperRegion() == 2) {
			currentValue += 20;
		} else if (region.getDistanceToOwnSuperRegion() == 3) {
			currentValue += 10;
		}

		// Add 10 - the amount of neutrals in the SuperRegion
		int neutralArmies = region.getSuperRegion().getNeutralArmies();
		currentValue += Math.max(0, 10 - neutralArmies);

		// Add 1 to the value for each opponent bordering region
		currentValue += 1 * region.getOpponentNeighbors().size();

		// Add stuff if the opponent seems to currently expand in that SuperRegion
		SuperRegion vmSuperRegion = region.getSuperRegion();
		if (HistoryTracker.botState.getRoundNumber() != 1 && vmSuperRegion.getOwnedSubRegions().size() == 0
				&& vmSuperRegion.getArmiesReward() > 0 && !vmSuperRegion.isOwnedByOpponent()) {
			boolean opponentIsExpanding = false;
			for (Region opponentSubRegion : vmSuperRegion.getVisibleOpponentSubRegions()) {
				Region lwmRegion = HistoryTracker.botState.getLastVisibleMap().getRegion(opponentSubRegion.getId());
				if (lwmRegion.isVisible() && lwmRegion.getPlayerName().equals("neutral")) {
					opponentIsExpanding = true;
				}
			}
			if (opponentIsExpanding) {
				if (neutralArmies <= 2) {
					currentValue += vmSuperRegion.getArmiesReward() * 30;
				} else if (neutralArmies <= 4) {
					currentValue += vmSuperRegion.getArmiesReward() * 10;
				} else {
					currentValue += vmSuperRegion.getArmiesReward() * 5;
				}
			}

			// Add stuff if it's the most important opponent SuperRegion
			boolean isMostImportantSuperRegion = true;
			double superRegionExpansionValue = vmSuperRegion.getOpponentExpansionValueHeuristic().expansionValue;
			for (SuperRegion superRegion : HistoryTracker.botState.getVisibleMap().getSuperRegions()) {
				if (superRegion.getOpponentExpansionValueHeuristic().expansionValue > superRegionExpansionValue) {
					isMostImportantSuperRegion = false;
				}
			}
			if (isMostImportantSuperRegion && vmSuperRegion.getArmiesReward() > 0 && !vmSuperRegion.isOwnedByOpponent()
					&& !vmSuperRegion.containsOwnPresence() && vmSuperRegion.getNeutralArmies() < 8) {
				currentValue += 1;
			}

		}

		currentValue *= ATTACK_ADJUSTMENT_FACTOR;

		Region regionToWriteIn = mapToWriteIn.getRegion(region.getId());
		regionToWriteIn.setAttackRegionValue(currentValue);

	}

	private static void calculateDefenseRegionValue(Region region, Map mapToWriteIn) {
		int currentValue = 0;
		// Add 100.000 * armies reward to the value if it's a spot in an
		// owned SuperRegion
		if (region.getSuperRegion().isOwnedByMyself()) {
			currentValue += LOWEST_HIGH_PRIORITY_VALUE * region.getSuperRegion().getArmiesReward();
		}

		// Add 100.000 * armies reward to the value if it's the only spot in an opponent SuperRegion
		boolean allOwnedByOpponent = true;
		for (Region subRegion : region.getSuperRegion().getSubRegions()) {
			if (subRegion != region && !subRegion.getPlayerName().equals(HistoryTracker.opponentName)) {
				allOwnedByOpponent = false;
			}
		}
		if (allOwnedByOpponent) {
			currentValue += LOWEST_HIGH_PRIORITY_VALUE * region.getSuperRegion().getArmiesReward();
		}

		// Add 1000 to the value for each bordering own SuperRegion
		currentValue += LOWEST_MEDIUM_PRIORITY_VALUE * region.getAmountOfBordersToOwnSuperRegion();

		// Add 1000 to the value for each bordering opponent SuperRegion
		currentValue += LOWEST_MEDIUM_PRIORITY_VALUE * region.getAmountOfBordersToOpponentSuperRegion();

		// Add up to 30 to the armies reward for being close to an opponent SuperRegion
		if (region.getDistanceToOpponentSuperRegion() == 1) {
			currentValue += 30;
		} else if (region.getDistanceToOpponentSuperRegion() == 2) {
			currentValue += 20;
		} else if (region.getDistanceToOpponentSuperRegion() == 3) {
			currentValue += 10;
		}
		// Add up to 30 to the armies reward for being close to an own
		// SuperRegion
		if (region.getDistanceToOwnSuperRegion() == 1) {
			currentValue += 30;
		} else if (region.getDistanceToOwnSuperRegion() == 2) {
			currentValue += 20;
		} else if (region.getDistanceToOwnSuperRegion() == 3) {
			currentValue += 10;
		}

		// Add 10 - the amount of neutrals in the SuperRegion
		int neutralArmies = region.getSuperRegion().getNeutralArmies();
		int valueToAdd = Math.max(0, 10 - neutralArmies);
		currentValue += valueToAdd;

		// Add stuff if it's the most important SuperRegion
		boolean isMostImportantSuperRegion = true;
		SuperRegion vmSuperRegion = region.getSuperRegion();
		// vmSuperRegion.setMyExpansionValueHeuristic();
		double superRegionExpansionValue = vmSuperRegion.getMyExpansionValueHeuristic().expansionValue;
		for (SuperRegion superRegion : HistoryTracker.botState.getVisibleMap().getSuperRegions()) {
			if (superRegion.getMyExpansionValueHeuristic().expansionValue > superRegionExpansionValue) {
				isMostImportantSuperRegion = false;
			}
		}
		if (isMostImportantSuperRegion && vmSuperRegion.getArmiesReward() > 0 && !vmSuperRegion.isOwnedByMyself()
				&& !vmSuperRegion.containsOpponentPresence() && vmSuperRegion.getNeutralArmies() < 8) {
			currentValue += 1;
		}

		currentValue *= DEFENSE_ADJUSTMENT_FACTOR;

		Region regionToWriteIn = mapToWriteIn.getRegion(region.getId());
		regionToWriteIn.setDefenceRegionValue(currentValue);
	}

	/**
	 * Calculates the expansion region value for a region.
	 * 
	 * @param region
	 *            the region which can be part of an arbitrary map
	 * @param mapToWriteIn
	 *            the map in which the calculated region value is to be inserted
	 */
	private static void calculateExpansionRegionValue(Region region, Map mapToWriteIn) {
		int currentValue = 0;
		// Add 1000 for each unknown neighbor within the same SuperRegion
		for (Region neighbor : region.getNeighborsWithinSameSuperRegion()) {
			if (neighbor.getPlayerName().equals("neutral") && neighbor.getOwnedNeighbors().size() == 0) {
				currentValue += 1000;
			}
		}

		// Add 100 for each neighbor within the same SuperRegion
		List<Region> neighborsWithinSuperRegion = region.getNeighborsWithinSameSuperRegion();
		for (Region neighbor : neighborsWithinSuperRegion) {
			if (neighbor.getPlayerName().equals("neutral")) {
				currentValue += 100;
			}
		}

		// Add 10 for each opponent neighbor
		currentValue += 10 * region.getOpponentNeighbors().size();

		// Add 1 for each neutral neighbor in another SuperRegion
		for (Region neighbor : region.getNeighbors()) {
			if (neighbor.getPlayerName().equals("neutral") && !neighborsWithinSuperRegion.contains(neighbor)) {
				currentValue += 1;
			}
		}

		Region regionToWriteIn = mapToWriteIn.getRegion(region.getId());
		regionToWriteIn.setExpansionRegionValue(currentValue);
	}

	/**
	 * Calculates the flanking value
	 * 
	 * @param region
	 *            the region neutral region (from the mapToUse) I guess
	 * @param mapToWriteIn
	 *            visible map
	 * @param mapToUse
	 *            map with already made move decisions
	 */
	private static void calculateFlankingValue(Region region, Map mapToWriteIn, Map mapToUse) {
		List<Region> neighbors = region.getNeighbors();
		List<Region> superRegionNeighborRegions = new ArrayList<Region>();
		for (Region neighbor : neighbors) {
			if (!neighbor.isVisible() && neighbor.getSuperRegion().isOwnedByOpponent()
					&& !isSuperRegionAlreadyFlanked(neighbor.getSuperRegion())) {
				superRegionNeighborRegions.add(neighbor);
			}
		}

		// TODO develop more complex algorithm also with already made decisions
		int flankingValue = 0;
		for (Region superRegionNeighborRegion : superRegionNeighborRegions) {
			flankingValue += superRegionNeighborRegion.getSuperRegion().getArmiesReward();
		}

		Region regionToWriteIn = mapToWriteIn.getRegion(region.getId());
		regionToWriteIn.setFlankingRegionValue(flankingValue);
	}

	private static boolean isSuperRegionAlreadyFlanked(SuperRegion opponentSuperRegion) {
		Set<Region> flankedRegions = new HashSet<Region>();
		for (Region ownedNeighbor : opponentSuperRegion.getOwnedNeighborRegions()) {
			for (Region opponentNeighbor : ownedNeighbor.getOpponentNeighbors()) {
				if (opponentNeighbor.getSuperRegion().equals(opponentSuperRegion)) {
					flankedRegions.add(opponentNeighbor);
				}
			}
		}
		return flankedRegions.size() >= 2 ? true : false;
	}

}
