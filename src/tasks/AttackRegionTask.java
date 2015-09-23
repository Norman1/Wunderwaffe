package tasks;

import java.util.List;

import evaluation.RegionValueCalculator;

import bot.HistoryTracker;

import map.Map;
import map.Region;
import move.AttackTransferMove;
import move.Moves;
import move.PlaceArmiesMove;

/**
 * AttackRegionTask is responsible for attacking a single region. This isn't for calculating a minimal attack plan but
 * if a good attack plan is possible then a full force attack plan is calculated.
 * 
 */
public class AttackRegionTask {

	/**
	 * 
	 * @param opponentRegion
	 * @param maxDeployment
	 * @return
	 */
	public static Moves calculatAttackRegionTask(Region opponentRegion, int maxDeployment) {
		Moves out = new Moves();
		List<Region> ownedNeighbors = opponentRegion.getOwnedNeighbors();
		List<Region> presortedOwnedNeighbors = RegionValueCalculator.sortDefenseValue(ownedNeighbors);
		List<Region> sortedOwnedNeighbors = Map.getOrderedListOfRegionsByIdleArmies(presortedOwnedNeighbors);
		// Calculate the attacks
		for (int i = 0; i < sortedOwnedNeighbors.size(); i++) {
			Region attackingRegion = sortedOwnedNeighbors.get(i);
			if (i == 0 && maxDeployment > 0) {
				PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(), attackingRegion,
						maxDeployment);
				out.placeArmiesMoves.add(pam);
				if (attackingRegion.getIdleArmies() + maxDeployment > 1) {
					AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
							attackingRegion, opponentRegion, attackingRegion.getIdleArmies() + maxDeployment);
					out.attackTransferMoves.add(atm);
				}
			} else {
				if (attackingRegion.getIdleArmies() > 1) {
					AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
							attackingRegion, opponentRegion, attackingRegion.getIdleArmies());
					out.attackTransferMoves.add(atm);
				}
			}
		}
		// Check if we are killing more or equal armies than the opponent
		// double currentOpponentArmies = opponentRegion.getArmiesAfterDeployment();
		double currentOpponentArmies = opponentRegion.getArmiesAfterDeploymentAndIncomingAttacks(1);
		double opponentKills = 0;
		double ownKills = 0;
		for (AttackTransferMove atm : out.attackTransferMoves) {
			double ourKills = Math.min(currentOpponentArmies, atm.getArmies() * 0.6);
			double opponentKillsAttack = Math.min(atm.getArmies(), currentOpponentArmies * 0.7);
			ownKills += ourKills;
			opponentKills += opponentKillsAttack;
			currentOpponentArmies = Math.max(0, currentOpponentArmies - ourKills);
		}
		if (ownKills >= opponentKills && out.attackTransferMoves.size() > 0) {
			return out;
		} else {
			return null;
		}
	}
}
