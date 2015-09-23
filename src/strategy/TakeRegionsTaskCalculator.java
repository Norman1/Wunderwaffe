package strategy;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.Region;
import map.SuperRegion;
import move.AttackTransferMove;
import move.Moves;
import move.PlaceArmiesMove;
import bot.HistoryTracker;
import evaluation.RegionValueCalculator;

public class TakeRegionsTaskCalculator {

	public static Moves calculateOneStepExpandSuperRegionTask(int maxDeployment, SuperRegion superRegion,
			boolean acceptStackOnly, Map workingMap, int conservativeLevel) {
		Moves out = new Moves();
		if (maxDeployment == -1) {
			maxDeployment = 1000;
		}

		List<Region> visibleNeutralSubRegions = superRegion.getVisibleNeutralSubRegions();
		List<Region> regionsToRemove = new ArrayList<Region>();
		for (Region region : visibleNeutralSubRegions) {
			if (workingMap.getRegion(region.getId()).getPlayerName().equals(HistoryTracker.myName)) {
				regionsToRemove.add(region);
			}
		}
		visibleNeutralSubRegions.removeAll(regionsToRemove);
		if (visibleNeutralSubRegions.size() == 0) {
			return null;
		}

		List<Region> sortedNeutralSubRegions = RegionValueCalculator.sortExpansionValue(visibleNeutralSubRegions);
		List<Region> regionToTake = new ArrayList<>();
		regionToTake.add(sortedNeutralSubRegions.get(0));
		Moves takeRegionMoves = calculateTakeRegionsTask(-1, regionToTake, conservativeLevel);
		if (takeRegionMoves.getTotalDeployment() > maxDeployment) {
			if (acceptStackOnly) {
				if (maxDeployment > 0) {
					Region regionToDeploy = takeRegionMoves.placeArmiesMoves.get(0).getRegion();
					PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(),
							regionToDeploy, maxDeployment);
					out.placeArmiesMoves.add(pam);
				}
				return out;
			} else {
				return null;
			}
		} else {
			out = takeRegionMoves;
			return out;
		}
	}

	/**
	 * Calculates the necessary moves to take the specified regions to take.
	 * 
	 * @param maxDeployment
	 *            the maximum allowed deployment. If no deployment constraint then put -1.
	 * @param regionsToTake
	 *            the regions that should be taken this turn.
	 * @return the necessary moves to take the regions or null if no solution was found.
	 */
	public static Moves calculateTakeRegionsTask(int maxDeployment, List<Region> regionsToTake, int conservativeLevel) {
		Moves out = new Moves();
		if (maxDeployment == -1) {
			maxDeployment = 1000;
		}
		int stillAvailableDeployment = maxDeployment;
		for (Region missingRegion : regionsToTake) {
			Region bestNeighborRegion = getBestNeighborRegion(missingRegion, out, regionsToTake);
			int missingRegionArmies = missingRegion.getArmiesAfterDeploymentAndIncomingAttacks(conservativeLevel);
			// int missingRegionArmies = missingRegion.getArmiesAfterDeployment(conservativeLevel);
			int neededAttackArmies = (int) Math.round(missingRegionArmies / 0.6);
			int missingArmies = getMissingArmies(bestNeighborRegion, missingRegion, out, conservativeLevel);
			if (missingArmies > stillAvailableDeployment) {
				return null;
			}
			if (missingArmies > 0) {
				PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(),
						bestNeighborRegion, missingArmies);
				out.placeArmiesMoves.add(pam);
				stillAvailableDeployment -= missingArmies;
			}
			AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
					bestNeighborRegion, missingRegion, neededAttackArmies);
			out.attackTransferMoves.add(atm);

		}
		return out;
	}

	private static int getMissingArmies(Region expandingRegion, Region toBeTakenRegion, Moves madeExpansionDecisions,
			int conservativeLevel) {
		int idleArmies = getOverflowIdleArmies(expandingRegion, madeExpansionDecisions);
		int toBeTakenRegionArmies = toBeTakenRegion.getArmiesAfterDeploymentAndIncomingAttacks(conservativeLevel);
		int neededArmies = (int) Math.round(toBeTakenRegionArmies / 0.6);
		if (idleArmies >= neededArmies) {
			return 0;
		} else {
			return neededArmies - idleArmies;
		}
	}

	private static Region getBestNeighborRegion(Region missingRegion, Moves madeExpansionDecisions,
			List<Region> regionsToTake) {
		List<Region> ownedNeighbors = missingRegion.getOwnedNeighbors();
		List<Region> presortedOwnedNeighbors = RegionValueCalculator.sortDefenseValueFullReturn(ownedNeighbors);

		int maximumIdleArmies = 0;
		// First calculate the maximum amount of armies of an owned neighbor.
		for (Region ownedNeighbbor : presortedOwnedNeighbors) {
			int idleArmies = getOverflowIdleArmies(ownedNeighbbor, madeExpansionDecisions);
			if (idleArmies > maximumIdleArmies) {
				maximumIdleArmies = idleArmies;
			}
		}
		// Second calculate the owned neighbor having the maximum amount of idle
		// armies while having a minimum amount of sill missing neighbors.
		int minimumMissingNeighbors = 1000;
		Region out = null;
		for (Region ownedNeighbor : presortedOwnedNeighbors) {
			int missingNeighborRegions = getStillMissingNeighborRegions(ownedNeighbor, madeExpansionDecisions,
					regionsToTake).size();
			if (getOverflowIdleArmies(ownedNeighbor, madeExpansionDecisions) == maximumIdleArmies
					&& missingNeighborRegions < minimumMissingNeighbors) {
				out = ownedNeighbor;
				minimumMissingNeighbors = missingNeighborRegions;
			}
		}
		if (out == null) {
			out = presortedOwnedNeighbors.get(0);
		}

		return out;
	}

	/**
	 * Calculates which regions are still missing after the made expansion decisions.
	 * 
	 * @param madeExpansionDecisions
	 * @param regionsToTake
	 * @return
	 */
	private static List<Region> getStillMissingRegions(Moves madeExpansionDecisions, List<Region> regionsToTake) {
		List<Region> out = new ArrayList<>();
		List<Region> regionsThatWeTook = new ArrayList<>();
		for (AttackTransferMove atm : madeExpansionDecisions.attackTransferMoves) {
			regionsThatWeTook.add(atm.getToRegion());
		}
		for (Region region : regionsToTake) {
			if (!regionsThatWeTook.contains(region)) {
				out.add(region);
			}
		}
		return out;
	}

	/**
	 * Calculates the regions next to our region which are still missing after the already made expansion decisions.
	 * 
	 * @param region
	 * @param madeExpansionDecisions
	 * @param regionsToTake
	 * @return
	 */
	private static List<Region> getStillMissingNeighborRegions(Region region, Moves madeExpansionDecisions,
			List<Region> regionsToTake) {
		List<Region> stillMissingRegions = getStillMissingRegions(madeExpansionDecisions, regionsToTake);
		List<Region> out = new ArrayList<>();
		for (Region neighbor : region.getNeighbors()) {
			if (stillMissingRegions.contains(neighbor)) {
				out.add(neighbor);
			}
		}

		return out;
	}

	/**
	 * Calculates the amount of idle armies on the region after the already made expansion decisions.
	 * 
	 * @param region
	 * @param expansionDecisions
	 * @return
	 */
	private static int getOverflowIdleArmies(Region region, Moves expansionDecisions) {
		int out = region.getIdleArmies();
		for (PlaceArmiesMove placeArmiesMove : expansionDecisions.placeArmiesMoves) {
			if (placeArmiesMove.getRegion().equals(region)) {
				out = out + placeArmiesMove.getArmies();
			}
		}
		for (AttackTransferMove expansionMove : expansionDecisions.attackTransferMoves) {
			if (expansionMove.getFromRegion().equals(region)) {
				out = out - expansionMove.getArmies();
			}
		}
		return out;
	}

}
