package basicAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import evaluation.RegionValueCalculator;

import map.Map;
import map.Region;
import map.SuperRegion;
import bot.HistoryTracker;

public class DistanceCalculator {

	public static List<Region> getShortestPathToRegions(Map mapToUse, Region fromRegion, List<Region> toRegions,
			List<Region> blockedRegions) {
		List<Region> out = new ArrayList<>();
		java.util.Map<Region, Integer> annotatedRegions = calculateDistances(mapToUse, toRegions, blockedRegions);
		out.add(fromRegion);
		Region currentRegion = fromRegion;
		int currentDistance = annotatedRegions.get(fromRegion);
		while (currentDistance != 0) {
			Region closestNeighbor = getClosestNeighborToTargetRegions(currentRegion, annotatedRegions, blockedRegions);
			out.add(closestNeighbor);
			currentRegion = closestNeighbor;
			currentDistance = annotatedRegions.get(closestNeighbor);
		}
		return out;
	}

	public static void calculateDistanceToOwnSuperRegions(Map mapToUse) {
		List<Region> ownSuperRegionRegions = new ArrayList<>();
		for (SuperRegion superRegion : mapToUse.getSuperRegions()) {
			if (superRegion.isOwnedByMyself()) {
				ownSuperRegionRegions.addAll(superRegion.getSubRegions());
			}
		}
		if (ownSuperRegionRegions.size() > 0) {
			java.util.Map<Region, Integer> annotadedRegions = calculateDistances(mapToUse, ownSuperRegionRegions, null);
			for (Region region : annotadedRegions.keySet()) {
				int regionDistance = annotadedRegions.get(region);
				region.setDistanceToOwnSuperRegion(regionDistance);
			}
		}
	}

	public static void calculateDistanceToOpponentSuperRegions(Map mapToUse) {
		List<Region> opponentSuperRegionRegions = new ArrayList<>();
		for (SuperRegion superRegion : mapToUse.getSuperRegions()) {
			if (superRegion.isOwnedByOpponent()) {
				opponentSuperRegionRegions.addAll(superRegion.getSubRegions());
			}
		}
		if (opponentSuperRegionRegions.size() > 0) {
			java.util.Map<Region, Integer> annotadedRegions = calculateDistances(mapToUse, opponentSuperRegionRegions,
					null);
			for (Region region : annotadedRegions.keySet()) {
				int regionDistance = annotadedRegions.get(region);
				region.setDistanceToOpponentSuperRegion(regionDistance);
			}
		}
	}

	// TODO
	public static java.util.Map<Region, Integer> getClosestRegionToOpponentSuperRegion(Map mapToUse,
			SuperRegion opponentSuperRegion) {
		List<Region> subRegions = opponentSuperRegion.getSubRegions();
		java.util.Map<Region, Integer> annotadedRegions = calculateDistances(mapToUse, subRegions, null);
		int minDistance = 1000;
		Region minDistanceRegion = null;
		for (Region region : annotadedRegions.keySet()) {
			int regionDistance = annotadedRegions.get(region);
			if (region.getPlayerName().equals(HistoryTracker.myName) && regionDistance < minDistance) {
				minDistance = annotadedRegions.get(region);
				minDistanceRegion = region;
			}
		}
		java.util.Map<Region, Integer> returnRegion = new HashMap<>();
		returnRegion.put(minDistanceRegion, minDistance);
		return returnRegion;
	}

	/**
	 * Care0Spots
	 * 
	 * @param mapToUse
	 */
	public static void calculateDistanceToUnimportantRegions(Map mapToUse, Map mapToWriteIn) {
		List<Region> unimportantRegions = new ArrayList<>();
		for (Region neutralRegion : mapToUse.getNeutralRegions()) {
			if (neutralRegion.getSuperRegion().getExpansionValueCategory() == 0) {
				unimportantRegions.add(neutralRegion);
			}
		}
		List<Region> blockedRegions = mapToUse.getOpponentRegions();
		java.util.Map<Region, Integer> annotadedRegions = calculateDistances(mapToUse, unimportantRegions,
				blockedRegions);
		for (Region region : annotadedRegions.keySet()) {
			int regionDistance = annotadedRegions.get(region);
			Region regionToWriteIn = mapToWriteIn.getRegion(region.getId());
			regionToWriteIn.setDistanceToUnimportantSpot(regionDistance);
		}
	}

	/**
	 * Care1Spots
	 * 
	 * @param mapToUse
	 */
	public static void calculateDistanceToImportantExpansionRegions(Map mapToUse, Map mapToWriteIn) {
		List<Region> importantRegions = new ArrayList<>();
		for (Region neutralRegion : mapToUse.getNeutralRegions()) {
			if (neutralRegion.getSuperRegion().getExpansionValueCategory() == 1) {
				importantRegions.add(neutralRegion);
			}
		}
		List<Region> blockedRegions = mapToUse.getOpponentRegions();
		java.util.Map<Region, Integer> annotadedRegions = calculateDistances(mapToUse, importantRegions, blockedRegions);
		for (Region region : annotadedRegions.keySet()) {
			int regionDistance = annotadedRegions.get(region);
			Region regionToWriteIn = mapToWriteIn.getRegion(region.getId());
			regionToWriteIn.setDistanceToImportantSpot(regionDistance);
		}
	}

	/**
	 * Care2Spots
	 * 
	 * @param mapToUse
	 */
	public static void calculateDistanceToHighlyImportantExpansionRegions(Map mapToUse, Map mapToWriteIn) {
		List<Region> highlyImportantRegions = new ArrayList<>();
		for (Region neutralRegion : mapToUse.getNeutralRegions()) {
			if (neutralRegion.getSuperRegion().getExpansionValueCategory() == 1
					&& neutralRegion.getSuperRegion().getExpansionValue() >= 100) {
				highlyImportantRegions.add(neutralRegion);
			}
		}
		List<Region> blockedRegions = mapToUse.getOpponentRegions();
		java.util.Map<Region, Integer> annotadedRegions = calculateDistances(mapToUse, highlyImportantRegions,
				blockedRegions);
		for (Region region : annotadedRegions.keySet()) {
			int regionDistance = annotadedRegions.get(region);
			Region regionToWriteIn = mapToWriteIn.getRegion(region.getId());
			regionToWriteIn.setDistanceToHighlyImportantSpot(regionDistance);
		}
	}

	/**
	 * Care3Spots
	 * 
	 * @param mapToUse
	 */
	public static void calculateDistanceToOpponentBorderCare3(Map mapToUse, Map mapToWriteIn) {
		List<Region> opponentRegions = mapToUse.getOpponentRegions();
		List<Region> blockedRegions = mapToUse.getNeutralRegions();
		java.util.Map<Region, Integer> annotadedRegions = calculateDistances(mapToUse, opponentRegions, blockedRegions);
		for (Region region : annotadedRegions.keySet()) {
			int regionDistance = annotadedRegions.get(region);
			Region regionToWriteIn = mapToWriteIn.getRegion(region.getId());
			regionToWriteIn.setDistanceToOpponentBorder(regionDistance);
		}
	}

	/**
	 * Care4Spots
	 * 
	 * @param mapToUse
	 */
	public static void calculateDistanceToOpponentBorderCare4(Map mapToUse, Map mapToWriteIn) {
		List<Region> importantOpponentRegions = new ArrayList<>();
		for (Region opponentRegion : mapToUse.getOpponentRegions()) {
			if (opponentRegion.getAttackRegionValue() >= RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE) {
				importantOpponentRegions.add(opponentRegion);
			}
		}
		List<Region> blockedRegions = mapToUse.getNeutralRegions();
		java.util.Map<Region, Integer> annotadedRegions = calculateDistances(mapToUse, importantOpponentRegions,
				blockedRegions);
		for (Region region : annotadedRegions.keySet()) {
			int regionDistance = annotadedRegions.get(region);
			Region regionToWriteIn = mapToWriteIn.getRegion(region.getId());
			regionToWriteIn.setDistanceToImportantOpponentBorder(regionDistance);
		}
	}

	public static void calculateDirectDistanceToOpponentRegions(Map mapToUse, Map mapToWriteIn) {
		List<Region> opponentRegions = mapToUse.getOpponentRegions();
		java.util.Map<Region, Integer> annotadedRegions = calculateDistances(mapToUse, opponentRegions, null);
		for (Region region : annotadedRegions.keySet()) {
			int regionDistance = annotadedRegions.get(region);
			Region regionToWriteIn = mapToWriteIn.getRegion(region.getId());
			regionToWriteIn.setDirectDistanceToOpponentBorder(regionDistance);
		}
	}

	public static void calculateDistanceToBorder(Map mapToWriteIn, Map mapToUse) {
		List<Region> nonOwnedRegions = new ArrayList<>();
		for (Region vmRegion : mapToUse.getRegions()) {
			Region wmRegion = mapToUse.getRegion(vmRegion.getId());
			if (!wmRegion.getPlayerName().equals(HistoryTracker.myName)) {
				nonOwnedRegions.add(vmRegion);
			}
		}

		java.util.Map<Region, Integer> annotadedRegions = calculateDistances(mapToWriteIn, nonOwnedRegions, null);
		for (Region region : annotadedRegions.keySet()) {
			int regionDistance = annotadedRegions.get(region);
			region.setDistanceToBorder(regionDistance);
		}
	}

	/**
	 * 
	 * @param state
	 * @param toRegions
	 * @param blockedRegions
	 *            blocked regions. Insert null here if not needed.
	 * @return
	 */
	public static java.util.Map<Region, Integer> calculateDistances(Map mapToUse, List<Region> toRegions,
			List<Region> blockedRegions) {
		java.util.Map<Region, Integer> out = new HashMap<>();
		// initialize
		for (Region region : mapToUse.getRegions()) {
			if (toRegions.contains(region)) {
				out.put(region, 0);
			} else {
				out.put(region, 100);
			}
		}
		// Now do the real stuff
		boolean hasSomethingChanged = true;
		while (hasSomethingChanged) {
			hasSomethingChanged = false;
			for (Region region : mapToUse.getRegions()) {
				Region closestNeighbor = getClosestNeighborToTargetRegions(region, out, blockedRegions);
				if (out.get(closestNeighbor) < out.get(region) && out.get(region) != out.get(closestNeighbor) + 1) {
					out.put(region, out.get(closestNeighbor) + 1);
					hasSomethingChanged = true;
				}
			}
		}
		return out;
	}

	private static Region getClosestNeighborToTargetRegions(Region inRegion,
			java.util.Map<Region, Integer> annotatedRegions, List<Region> blockedRegions) {
		List<Region> nonBlockedNeighbors = new ArrayList<>();
		for (Region neighbor : inRegion.getNeighbors()) {
			if (blockedRegions == null || !blockedRegions.contains(neighbor)) {
				nonBlockedNeighbors.add(neighbor);
			}
		}
		Region closestNeighbor = inRegion;
		for (Region neighbor : nonBlockedNeighbors) {
			int neighborDistance = annotatedRegions.get(neighbor);
			if (neighborDistance < annotatedRegions.get(closestNeighbor)) {
				closestNeighbor = neighbor;
			}
		}
		return closestNeighbor;
	}

}
