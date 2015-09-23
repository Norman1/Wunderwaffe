package tasks;

import java.util.ArrayList;
import java.util.List;

import evaluation.RegionValueCalculator;

import bot.HistoryTracker;

import map.Region;
import move.AttackTransferMove;
import move.Moves;

/**
 * NoPlanTryoutAttackTask is responsible for attacking stacks of 1 with 2 armies and stacks of 2 with 3 armies since we
 * might get lucky without the opponent deploying there.
 * 
 */
public class NoPlanTryoutAttackTask {

	/**
	 * We only try to perform 1 attack to a high ranked region where an attack is possible. For multiple attacks this
	 * function has to be called multiple times.
	 * 
	 * @param attackLowImportantRegions
	 * @param attackMediumImportantRegions
	 * @param attackHighImportantRegions
	 * @return
	 */
	public static Moves calculateNoPlanTryoutAttackTask(boolean attackLowImportantRegions,
			boolean attackMediumImportantRegions, boolean attackHighImportantRegions) {
		List<Region> possibleAttackRegions = getPossibleRegionsToAttack(attackLowImportantRegions,
				attackMediumImportantRegions, attackHighImportantRegions);
		List<Region> sortedPossibleAttackRegions = RegionValueCalculator.sortAttackValue(possibleAttackRegions);

		for (Region regionToAttack : sortedPossibleAttackRegions) {

			int neededNewArmies = 0;
			List<Region> ownedNeighbors = regionToAttack.getOwnedNeighbors();
			List<Region> sortedOwnedNeighbors = RegionValueCalculator.sortDefenseValue(ownedNeighbors);
			Region bestNeighbor = null;

			for (int i = sortedOwnedNeighbors.size() - 1; i >= 0; i--) {
				boolean smallAttackPresent = isRegionAttackingOpponentRegionSmall(ownedNeighbors.get(i), regionToAttack);
				if (smallAttackPresent) {
					neededNewArmies = regionToAttack.getArmies();
				} else {
					neededNewArmies = regionToAttack.getArmies() + 1;
				}
				if (ownedNeighbors.get(i).getIdleArmies() >= neededNewArmies) {
					bestNeighbor = ownedNeighbors.get(i);
					break;
				}
			}
			if (bestNeighbor != null) {
				Moves out = new Moves();
				if (bestNeighbor.getIdleArmies() > 2) {
					AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
							bestNeighbor, regionToAttack, 3);
					atm.setMessage("tryoutAttack");
					out.attackTransferMoves.add(atm);
					return out;
				} else {
					AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
							bestNeighbor, regionToAttack, neededNewArmies);
					atm.setMessage("tryoutAttack");
					out.attackTransferMoves.add(atm);
					return out;
				}

			}
		}
		return null;
	}

	private static boolean isRegionAttackingOpponentRegionSmall(Region ourRegion, Region opponentRegion) {
		boolean attacksSmall = false;
		for (AttackTransferMove atm : opponentRegion.getIncomingMoves()) {
			if (atm.getArmies() == 1 && atm.getFromRegion().equals(ourRegion)) {
				attacksSmall = true;
			}
		}
		return attacksSmall;
	}

	private static List<Region> getPossibleRegionsToAttack(boolean attackLowImportantRegions,
			boolean attackMediumImportantRegions, boolean attackHighImportantRegions) {
		List<Region> possibleCandidates = new ArrayList<>();
		for (Region region : HistoryTracker.botState.getVisibleMap().getOpponentRegions()) {
			if (region.isVisible()) {
				if (region.getAttackRegionValue() >= RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE
						&& attackHighImportantRegions) {
					possibleCandidates.add(region);
				} else if (region.getAttackRegionValue() >= RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE
						&& attackMediumImportantRegions) {
					possibleCandidates.add(region);
				} else if (attackLowImportantRegions) {
					possibleCandidates.add(region);
				}
			}
		}
		List<Region> out = new ArrayList<>();
		for (Region region : possibleCandidates) {
			if (region.getArmies() <= 2) {
				boolean getsAlreadyAttacked = false;
				for (AttackTransferMove incomingMove : region.getIncomingMoves()) {
					if (incomingMove.getArmies() > 1) {
						getsAlreadyAttacked = true;
					}
				}
				if (!getsAlreadyAttacked) {
					out.add(region);
				}
			}
		}
		return out;
	}

}
