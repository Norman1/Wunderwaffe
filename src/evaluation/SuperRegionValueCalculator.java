package evaluation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bot.HistoryTracker;
import map.Map;
import map.Region;
import map.SuperRegion;

/**
 * Calculates the SuperRegion values for break, defend, take over, prevent take over and expand
 *
 */
public class SuperRegionValueCalculator {

	public static String getPlanForSuperRegion(SuperRegion superRegion) {
		int highestValue = -1;
		String plan = "null";
		if (superRegion.getAttackValue() > highestValue) {
			highestValue = superRegion.getAttackValue();
			plan = "BREAK";
		}
		if (superRegion.getDefenseValue() > highestValue) {
			highestValue = superRegion.getDefenseValue();
			plan = "DEFEND";
		}
		if (superRegion.getTakeOverValue() > highestValue) {
			highestValue = superRegion.getTakeOverValue();
			plan = "TAKE_OVER";
		}
		if (superRegion.getPreventTakeOverValue() > highestValue) {
			highestValue = superRegion.getPreventTakeOverValue();
			plan = "PREVENT_TAKE_OVER";
		}
		return plan;
	}

	/**
	 *
	 * 
	 * @param mapToUse
	 *            visible map
	 * @return Only returns the SuperRegions with an adjusted factor of > 0.
	 */
	public static List<SuperRegion> getSortedSuperRegionsAdjustedFactor(Map mapToUse) {
		List<SuperRegion> out = new ArrayList<SuperRegion>();
		List<SuperRegion> copy = new ArrayList<SuperRegion>();
		for (SuperRegion superRegion : mapToUse.getSuperRegions()) {
			if (getAdjustedFactor(superRegion) > 0) {
				copy.add(superRegion);
			}
		}

		while (!copy.isEmpty()) {
			SuperRegion bestSuperRegion = copy.get(0);
			for (SuperRegion superRegion : copy) {
				if (getAdjustedFactor(superRegion) > getAdjustedFactor(bestSuperRegion)) {
					bestSuperRegion = superRegion;
				}
			}
			copy.remove(bestSuperRegion);
			out.add(bestSuperRegion);
		}
		return out;
	}

	private static int getAdjustedFactor(SuperRegion superRegion) {
		int adjustedFactor = Math.max(
				superRegion.getPreventTakeOverValue(),
				Math.max(superRegion.getTakeOverValue(),
						Math.max(superRegion.getAttackValue(), superRegion.getDefenseValue())));
		return adjustedFactor;
	}

	private static final int SUPERREGION_ATTACK_FACTOR = 15;
	private static final int SUPERREGIOIN_DEFENSE_FACTOR = 10;
	private static final int SUPERREGION_TAKE_OVER_FACTOR = 6;
	private static final int SUPERREGION_PREVENT_TAKE_OVER_FACTOR = 15;

	// private static final int SUPERREGION_ATTACK_FACTOR = 5;
	// private static final int SUPERREGIOIN_DEFENSE_FACTOR = 3;
	// private static final int SUPERREGION_TAKE_OVER_FACTOR = 3;
	// private static final int SUPERREGION_PREVENT_TAKE_OVER_FACTOR = 5;

	public static void calculatSuperRegionValues(Map mapToUse, Map mapToWriteIn) {
		List<SuperRegion> ownSuperRegionsUnderAttack = new ArrayList<SuperRegion>();
		List<SuperRegion> opponentSuperRegionsUnderAttack = new ArrayList<SuperRegion>();
		List<SuperRegion> superRegionsWeCanTakeOver = new ArrayList<SuperRegion>();
		List<SuperRegion> superRegionsOpponentCanTakeOver = new ArrayList<SuperRegion>();

		// Classify the SuperRegions
		for (SuperRegion superRegion : mapToUse.getSuperRegions()) {
			if (superRegion.getArmiesReward() > 0) {
				if (superRegion.isOwnedByMyself() && superRegion.getOpponentNeighbors().size() > 0) {
					ownSuperRegionsUnderAttack.add(superRegion);
				}
				if (superRegion.isOwnedByOpponent() && superRegion.getOwnedNeighborRegions().size() > 0) {
					opponentSuperRegionsUnderAttack.add(superRegion);
				}
				SuperRegion vmSuperRegion = HistoryTracker.botState.getVisibleMap().getSuperRegion(superRegion.getId());
				if (vmSuperRegion.canTakeOver()) {
					superRegionsWeCanTakeOver.add(superRegion);
				}
				if (vmSuperRegion.canOpponentTakeOver()) {
					superRegionsOpponentCanTakeOver.add(superRegion);
				}
			}
		}

		// Calculate the values
		for (SuperRegion superRegion : ownSuperRegionsUnderAttack) {
			calculateDefenseValue(superRegion, mapToWriteIn);
		}
		for (SuperRegion superRegion : opponentSuperRegionsUnderAttack) {
			calculateAttackValue(superRegion, mapToWriteIn);
		}
		for (SuperRegion superRegion : superRegionsWeCanTakeOver) {
			calculateTakeOverValue(superRegion, mapToWriteIn);
		}
		for (SuperRegion superRegion : superRegionsOpponentCanTakeOver) {
			calculatePreventValue(superRegion, mapToWriteIn);
		}

		// Set the factors to -1 where not possible or makes no sense (0 income)
		for (SuperRegion superRegion : mapToUse.getSuperRegions()) {
			SuperRegion superRegionToWriteIn = mapToWriteIn.getSuperRegion(superRegion.getId());
			if (!ownSuperRegionsUnderAttack.contains(superRegion)) {
				superRegionToWriteIn.setDefenseValue(-1);
			}
			if (!opponentSuperRegionsUnderAttack.contains(superRegion)) {
				superRegionToWriteIn.setAttackValue(-1);
			}
			if (!superRegionsWeCanTakeOver.contains(superRegion)) {
				superRegionToWriteIn.setTakeOverValue(-1);
			}
			if (!superRegionsOpponentCanTakeOver.contains(superRegion)) {
				superRegionToWriteIn.setPreventTakeOverValue(-1);
			}
		}

	}

	private static void calculateDefenseValue(SuperRegion superRegion, Map mapToWriteIn) {
		int armiesReward = superRegion.getArmiesReward();
		List<Region> opponentNeighborRegions = superRegion.getOpponentNeighbors();
		int opponentNeighbors = opponentNeighborRegions.size();
		List<Region> regionsUnderThreat = superRegion.getOwnedRegionsBorderingOpponentNeighbors();
		int amountOfRegionsUnderThreat = regionsUnderThreat.size();
		int opponentArmies = 0;
		for (Region opponentNeighbor : opponentNeighborRegions) {
			opponentArmies += opponentNeighbor.getArmies();
		}
		int ownArmies = 0;
		for (Region regionUnderThread : regionsUnderThreat) {
			ownArmies += regionUnderThread.getArmiesAfterDeploymentAndIncomingMoves();
		}
		int ownedNeighborRegions = superRegion.getOwnedNeighborRegions().size();
		int amountSubRegions = superRegion.getSubRegions().size();

		int defenseValue = 0;
		defenseValue += armiesReward * 10000;
		defenseValue += opponentArmies * -10;
		defenseValue += ownArmies * 10;
		defenseValue += ownedNeighborRegions * 1;
		defenseValue += opponentNeighbors * -100;
		defenseValue += amountOfRegionsUnderThreat * -1000;
		defenseValue += amountSubRegions * -1;
		defenseValue *= SUPERREGIOIN_DEFENSE_FACTOR;

		SuperRegion superRegionToWriteIn = mapToWriteIn.getSuperRegion(superRegion.getId());
		superRegionToWriteIn.setDefenseValue(defenseValue);

		// TODO hack so SuperRegions we are taking this turn don't already get a defense value of > 0
		if (!HistoryTracker.botState.getVisibleMap().getSuperRegion(superRegion.getId()).isOwnedByMyself()) {
			superRegionToWriteIn.setDefenseValue(-1);
		}

	}

	private static void calculateAttackValue(SuperRegion superRegion, Map mapToWriteIn) {
		int armiesReward = superRegion.getArmiesReward();
		List<Region> ownedNeighbors = superRegion.getOwnedNeighborRegions();
		int amountOwnedNeighbors = ownedNeighbors.size();
		List<Region> regionsUnderAttack = superRegion.getVisibleOpponentSubRegions();
		int amountRegionsUnderAttack = regionsUnderAttack.size();
		int opponentArmies = 0;
		for (Region opponentSubRegion : regionsUnderAttack) {
			opponentArmies += opponentSubRegion.getArmies();
		}
		int ownArmies = 0;
		for (Region ownedNeighbor : ownedNeighbors) {
			ownArmies += ownedNeighbor.getIdleArmies();
		}
		int opponentNeighbors = superRegion.getOpponentNeighbors().size();

		int attackValue = 0;
		attackValue += armiesReward * 10000;
		attackValue += opponentArmies * -1;
		attackValue += ownArmies * 3;
		attackValue += amountOwnedNeighbors * 100;
		attackValue += opponentNeighbors * 1;
		attackValue += amountRegionsUnderAttack * 1000;
		attackValue *= SUPERREGION_ATTACK_FACTOR;

		SuperRegion superRegionToWriteIn = mapToWriteIn.getSuperRegion(superRegion.getId());
		superRegionToWriteIn.setAttackValue(attackValue);

	}

	private static void calculateTakeOverValue(SuperRegion superRegion, Map mapToWriteIn) {
		int armiesReward = superRegion.getArmiesReward();
		List<Region> opponentSubRegions = superRegion.getOpponentSubRegions();
		int amountOpponentSubRegions = opponentSubRegions.size();
		int opponentArmies = 0;
		for (Region opponentSubRegion : opponentSubRegions) {
			opponentArmies += opponentSubRegion.getArmies();
		}
		Set<Region> possibleAttackRegions = new HashSet<Region>();
		for (Region opponentSubRegion : opponentSubRegions) {
			possibleAttackRegions.addAll(opponentSubRegion.getOwnedNeighbors());
		}
		int ownArmies = 0;
		for (Region possibleAttackRegion : possibleAttackRegions) {
			ownArmies += possibleAttackRegion.getIdleArmies();
		}
		int amountSubRegions = superRegion.getSubRegions().size();
		int opponentNeighbors = superRegion.getOpponentNeighbors().size();

		int takeOverValue = 0;
		takeOverValue += armiesReward * 10000;
		takeOverValue += opponentArmies * -10;
		takeOverValue += ownArmies * 10;
		takeOverValue += opponentNeighbors * -1;
		takeOverValue += amountOpponentSubRegions * -1000;
		takeOverValue += amountSubRegions * -1;
		takeOverValue *= SUPERREGION_TAKE_OVER_FACTOR;

		SuperRegion superRegionToWriteIn = mapToWriteIn.getSuperRegion(superRegion.getId());
		superRegionToWriteIn.setTakeOverValue(takeOverValue);

	}

	private static void calculatePreventValue(SuperRegion superRegion, Map mapToWriteIn) {
		int armiesReward = superRegion.getArmiesReward();
		List<Region> ownedSubRegions = superRegion.getOwnedSubRegions();
		int amountOwnedSubRegions = ownedSubRegions.size();
		int ownArmies = 0;
		for (Region ownedSubRegion : ownedSubRegions) {
			ownArmies += ownedSubRegion.getArmies();
		}
		Set<Region> attackingRegions = new HashSet<Region>();
		for (Region ownedSubRegion : ownedSubRegions) {
			attackingRegions.addAll(ownedSubRegion.getOpponentNeighbors());
		}
		int opponentArmies = 0;
		for (Region region : attackingRegions) {
			opponentArmies += region.getArmies();
		}
		int amountSubRegions = superRegion.getSubRegions().size();

		int preventValue = 0;
		preventValue += armiesReward * 10000;
		preventValue += opponentArmies * -10;
		preventValue += ownArmies * 10;
		preventValue += amountOwnedSubRegions * 1000;
		preventValue += amountSubRegions * -1;
		preventValue *= SUPERREGION_PREVENT_TAKE_OVER_FACTOR;

		SuperRegion superRegionToWriteIn = mapToWriteIn.getSuperRegion(superRegion.getId());
		superRegionToWriteIn.setPreventTakeOverValue(preventValue);

	}

}
