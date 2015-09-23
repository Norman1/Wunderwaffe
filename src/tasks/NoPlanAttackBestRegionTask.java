package tasks;

import java.util.List;

import bot.HistoryTracker;

import evaluation.RegionValueCalculator;
import map.Region;
import move.AttackTransferMove;
import move.Moves;

/**
 * NoPlanAttackBestRegionTask is responsible for attacking the best opponent region with good attacks and without
 * following a specific plan.
 * 
 */
public class NoPlanAttackBestRegionTask {
	public static Moves calculateNoPlanAttackBestRegionTask(int maxDeployment) {
		Moves out = new Moves();
		// If true attacks are possible then go with them
		List<Region> sortedOpponentRegions = RegionValueCalculator.getSortedAttackValueRegions();
		List<Region> regionsToAttack = NoPlanBreakBestRegionTask.removeRegionsThatWeTook(sortedOpponentRegions);
		for (Region region : regionsToAttack) {
			if (region.isVisible()) {
				Moves attackRegionMoves = AttackRegionTask.calculatAttackRegionTask(region, maxDeployment);
				if (attackRegionMoves != null) {
					return attackRegionMoves;
				}
				// If we can't truly attack then attack with 1's
				int allowedSmallAttacks = region.getArmies() - 1;
				allowedSmallAttacks -= getAlreadyPresentSmallAttacks(region);
				for (Region ownedNeighbor : region.getOwnedNeighbors()) {
					if (allowedSmallAttacks > 0 && ownedNeighbor.getIdleArmies() > 0
							&& !isRegionAlreadySmallAttackedFromOurRegion(ownedNeighbor, region)) {

						boolean alreadyAttacksOpponent = false;
						for (AttackTransferMove atm : ownedNeighbor.getOutgoingMoves()) {
							if (atm.getArmies() > 1
									&& atm.getToRegion().getPlayerName().equals(HistoryTracker.opponentName)) {
								alreadyAttacksOpponent = true;
							}
						}
						if (!alreadyAttacksOpponent) {
							AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
									ownedNeighbor, region, 1);
							out.attackTransferMoves.add(atm);
							allowedSmallAttacks--;
						}
					}
				}
				if (out.attackTransferMoves.size() > 0) {
					return out;
				}
			}
		}
		// If absolutely no attack possible then return null
		return null;
	}

	private static int getAlreadyPresentSmallAttacks(Region opponentRegion) {
		int out = 0;
		for (AttackTransferMove atm : opponentRegion.getIncomingMoves()) {
			if (atm.getArmies() == 1) {
				out++;
			}
		}
		return out;
	}

	private static boolean isRegionAlreadySmallAttackedFromOurRegion(Region ourRegion, Region opponentRegion) {
		boolean alreadySmallAttacked = false;
		for (AttackTransferMove atm : ourRegion.getOutgoingMoves()) {
			if (atm.getArmies() == 1 && atm.getToRegion().equals(opponentRegion)) {
				alreadySmallAttacked = true;
			}
		}
		return alreadySmallAttacked;
	}
}
