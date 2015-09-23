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
 * OneHitBreakRegionTask is responsible for calculating an attack plan to break a single region with a single attack
 * without pulling other regions in.
 * 
 */
public class OneHitBreakRegionTask {

	public static Moves calculateBreakRegionTask(Region opponentRegion, int maxDeployment, int conservativeLevel) {
		Moves out = new Moves();
		int opponentArmies = opponentRegion.getArmiesAfterDeploymentAndIncomingAttacks(conservativeLevel);
		int neededAttackArmies = (int) Math.ceil(opponentArmies / 0.6);
		List<Region> ownedNeighbors = opponentRegion.getOwnedNeighbors();
		List<Region> presortedOwnedNeighbors = RegionValueCalculator.sortDefenseValue(ownedNeighbors);
		List<Region> sortedOwnedNeighbors = Map.getOrderedListOfRegionsByIdleArmies(presortedOwnedNeighbors);
		Region regionToUse = sortedOwnedNeighbors.get(0);
		int idleArmies = regionToUse.getIdleArmies();
		int neededDeployment = Math.max(0, neededAttackArmies - idleArmies);
		if (neededDeployment > maxDeployment) {
			return null;
		}
		if (neededDeployment > 0) {
			PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(), regionToUse,
					neededDeployment);
			out.placeArmiesMoves.add(pam);
		}
		AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(), regionToUse,
				opponentRegion, neededAttackArmies);
		out.attackTransferMoves.add(atm);
		return out;
	}

}
