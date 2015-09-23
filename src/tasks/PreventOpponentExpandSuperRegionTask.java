package tasks;

import java.util.ArrayList;
import java.util.List;

import strategy.TakeRegionsTaskCalculator;
import evaluation.SuperRegionExpansionValueCalculator;
import bot.HistoryTracker;
import map.Map;
import map.Region;
import map.SuperRegion;
import move.AttackTransferMove;
import move.Moves;
import move.PlaceArmiesMove;

/**
 * This class is responsible for preventing the opponent from expanding in a SuperRegion in which we have no foothold
 * yet. Preventing the opponent from expanding can happen either by attacking a neutral territory there or by directly
 * attacking the opponent there.
 */
public class PreventOpponentExpandSuperRegionTask {

	public static SuperRegion getBestSuperRegionToPrevent(Map visibleMap, Map movesMap) {
		List<SuperRegion> possiblePreventableSuperRegions = new ArrayList<SuperRegion>();
		for (SuperRegion superRegion : visibleMap.getSuperRegions()) {
			if (isPreventingUseful(visibleMap, movesMap, superRegion)) {
				possiblePreventableSuperRegions.add(superRegion);
			}
		}
		List<SuperRegion> ourInterestingSuperRegions = getOurInterestingSuperregions();
		int ourMaxReward = 0;
		for (SuperRegion ourSuperRegion : ourInterestingSuperRegions) {
			if (ourSuperRegion.getArmiesReward() > ourMaxReward) {
				ourMaxReward = ourSuperRegion.getArmiesReward();
			}
		}
		int opponentMaxReward = 0;
		SuperRegion bestOpponentSuperRegion = null;
		for (SuperRegion opponentSuperRegion : possiblePreventableSuperRegions) {
			if (opponentSuperRegion.getArmiesReward() > opponentMaxReward) {
				bestOpponentSuperRegion = opponentSuperRegion;
				opponentMaxReward = opponentSuperRegion.getArmiesReward();
			} else if (opponentSuperRegion.getArmiesReward() == opponentMaxReward
					&& opponentSuperRegion.getNeutralArmies() < bestOpponentSuperRegion.getNeutralArmies()) {
				bestOpponentSuperRegion = opponentSuperRegion;
			}
		}

		for (SuperRegion superRegion : possiblePreventableSuperRegions) {
			if (superRegion != bestOpponentSuperRegion
					&& superRegion.getArmiesReward() == bestOpponentSuperRegion.getArmiesReward()
					&& superRegion.getNeutralArmies() == bestOpponentSuperRegion.getNeutralArmies()) {
				System.err.println("To many options for snipe");
				return null;
			}
		}

		if (opponentMaxReward > ourMaxReward) {
			return bestOpponentSuperRegion;
		} else {
			return null;
		}
	}

	public static Moves calculatePreventOpponentExpandSuperregionTaskk(SuperRegion superRegionToPrevent,
			int maxDeployment, Map visibleMap, Map mapSoFar) {
		Moves out = null;
		if (!isPreventingUseful(visibleMap, mapSoFar, superRegionToPrevent)) {
			return null;
		}
		// Step 1: Try to hit the opponent directly
		List<Region> possibleBreakRegions = new ArrayList<Region>();
		possibleBreakRegions.addAll(superRegionToPrevent.getVisibleOpponentSubRegions());
		out = BreakRegionsTask.calculateBreakRegionsTask(possibleBreakRegions, maxDeployment, 1,1);
		if (out != null) {
			System.err.println("snipe opponent");
			return out;
		}
		// Step 2: Try to hit a neutral region there
		List<Region> attackableNeutrals = new ArrayList<Region>();
		attackableNeutrals.addAll(superRegionToPrevent.getVisibleNeutralSubRegions());
		List<Region> sortedAttackableNeutrals = new ArrayList<Region>();
		while (!attackableNeutrals.isEmpty()) {
			Region bestNeutral = attackableNeutrals.get(0);
			for (Region neutral : attackableNeutrals) {
				if (neutral.getAttackRegionValue() > bestNeutral.getAttackRegionValue()) {
					bestNeutral = neutral;
				}
			}
			sortedAttackableNeutrals.add(bestNeutral);
			attackableNeutrals.remove(bestNeutral);
		}

		for (Region attackableNeutral : sortedAttackableNeutrals) {
			Moves attackRegionMoves = calculateAttackNeutralMoves(attackableNeutral, maxDeployment);
			if (attackRegionMoves != null) {
				System.err.println("snipe neutral");
				return attackRegionMoves;
			}
		}

		return null;
	}

	private static List<SuperRegion> getOurInterestingSuperregions() {
		List<SuperRegion> sortedAccessibleSuperRegions = SuperRegionExpansionValueCalculator
				.sortAccessibleSuperRegions(HistoryTracker.botState.getVisibleMap());
		List<SuperRegion> out = new ArrayList<SuperRegion>();
		for (SuperRegion superRegion : sortedAccessibleSuperRegions) {
			if ((superRegion.areAllRegionsVisible()) && (!superRegion.containsOpponentPresence())
					&& (superRegion.getArmiesReward() > 0) && !superRegion.isOwnedByMyself()
					&& superRegion.getNeutralArmies() <= 4) {
				out.add(superRegion);
			}
		}
		return out;
	}

	private static Moves calculateAttackNeutralMoves(Region neutralRegion, int maxDeployment) {
		Moves out = null;
		int neededAttackArmies = neutralRegion.getArmies() + 2;
		List<Region> ownedNeighbors = neutralRegion.getOwnedNeighbors();
		Region bestNeighbor = ownedNeighbors.get(0);
		for (Region ownedNeighbor : ownedNeighbors) {
			if (ownedNeighbor.getIdleArmies() > bestNeighbor.getIdleArmies()) {
				bestNeighbor = ownedNeighbor;
			}
		}
		int neededDeployment = Math.max(0, neededAttackArmies - bestNeighbor.getIdleArmies());
		if (neededDeployment <= maxDeployment) {
			out = new Moves();
			if (neededDeployment > 0) {
				PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.myName, bestNeighbor, neededDeployment);
				out.placeArmiesMoves.add(pam);
			}
			AttackTransferMove atm = new AttackTransferMove(HistoryTracker.myName, bestNeighbor, neutralRegion,
					neededAttackArmies);
			// TODO bad?
			 if(atm.getFromRegion().getOpponentNeighbors().size() == 0){
			atm.setMessage("snipe");
			 }
			out.attackTransferMoves.add(atm);
		}

		return out;
	}

	private static boolean isPreventingUseful(Map visibleMap, Map movesMap, SuperRegion superRegion) {
		if (superRegion.getOwnedSubRegionsAndNeighbors().size() == 0) {
			return false;
		}
		SuperRegion mmSuperRegion = movesMap.getSuperRegion(superRegion.getId());
		if (mmSuperRegion.getOwnedSubRegions().size() > 0) {
			return false;
		}
		if (superRegion.isOwnedByOpponent()) {
			return false;
		}

		if (superRegion.getOwnedNeighborRegions().size() == 0) {
			return false;
		}

		if (superRegion.getArmiesReward() < 2) {
			return false;
		}

		if (superRegion.getNeutralArmies() > 4) {
			return false;
		}

		for (Region neutralSubRegion : superRegion.getNeutralSubRegions()) {
			if (neutralSubRegion.getOpponentNeighbors().size() == 0) {
				return false;
			}
		}

		return true;
	}

}
