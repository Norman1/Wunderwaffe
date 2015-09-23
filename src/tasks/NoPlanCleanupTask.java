package tasks;

import java.util.ArrayList;
import java.util.List;

import map.Region;
import move.AttackTransferMove;
import move.Moves;
import move.PlaceArmiesMove;
import bot.HistoryTracker;
import evaluation.RegionValueCalculator;

/**
 * NoPlanCleanupTask is responsible for calculating the remaining moves after the other tasks have been fulfilled. If
 * the other tasks don't use the full deployment then this task is responsible for deploying the remaining armies. Also
 * if there are idle armies after the other tasks then in this task those armies can get used to perform an expansion
 * step.
 * 
 */
public class NoPlanCleanupTask {

	public static Moves calculateNoPlanCleanupDeploymentTask(int armiesToDeploy, Moves movesSoFar) {
		Moves out = new Moves();
		if (armiesToDeploy > 0) {
			Region bestDeploymentRegion = getBestDeploymentRegion(movesSoFar);
			PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(), bestDeploymentRegion,
					armiesToDeploy);
			out.placeArmiesMoves.add(pam);
		}
		return out;
	}

	public static Moves calculateNoPlanCleanupExpansionTask(Moves movesSoFar) {
		Moves out = new Moves();
		for (Region fromRegion : HistoryTracker.botState.getVisibleMap().getNonOpponentBorderingBorderRegions()) {
			List<Region> possibleToRegions = new ArrayList<>();
			for (Region nonOwnedNeighbor : fromRegion.getNonOwnedNeighbors()) {
				if (nonOwnedNeighbor.getPlayerName().equals("neutral")
						&& isUnplannedExpansionStepSmart(fromRegion, nonOwnedNeighbor)) {
					possibleToRegions.add(nonOwnedNeighbor);
				}
			}
			if (possibleToRegions.size() > 0) {
				List<Region> sortedPossibleToRegions = RegionValueCalculator.sortExpansionValue(possibleToRegions);
				Region regionToAttack = sortedPossibleToRegions.get(0);
				AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(), fromRegion,
						regionToAttack, fromRegion.getIdleArmies());
				out.attackTransferMoves.add(atm);
			}
		}
		return out;
	}

	/**
	 * Returns whether it makes sense to perform an unplanned expansion step from our region to the neutral region. The
	 * parameters considered therefore are the distance to the opponent and whether we can take that region.
	 * 
	 * @param fromRegion
	 *            an owned region, not bordering the opponent
	 * @param toRegion
	 *            a neutral region
	 * @return
	 */
	private static boolean isUnplannedExpansionStepSmart(Region fromRegion, Region toRegion) {
		boolean isSmart = true;
		if (fromRegion.getExpansionMoves().size() > 0) {
		}

		if (fromRegion.getIdleArmies() <= 1) {
			isSmart = false;
		}
		
		if (toRegion.getArmies() > Math.round(fromRegion.getIdleArmies() * 0.6)) {
			isSmart = false;
		}

		boolean distanceCondition1 = fromRegion.getDistanceToOpponentBorder() <= 4;
		boolean distanceCondition2 = toRegion.getOpponentNeighbors().size() == 0;
		if (distanceCondition1 && distanceCondition2) {
			isSmart = false;
		}
		return isSmart;
	}

	private static Region getBestDeploymentRegion(Moves movesSoFar) {
		// If we are bordering the opponent then the highest defense region next
		// to the opponent is good
		List<Region> opponentBorderingRegions = HistoryTracker.botState.getVisibleMap().getOpponentBorderingRegions();
		if (opponentBorderingRegions.size() > 0) {
			List<Region> sortedRegions = RegionValueCalculator.sortDefenseValue(opponentBorderingRegions);
			Region bestRegion = sortedRegions.get(0);
			return bestRegion;
		}

		List<AttackTransferMove> expansionMoves = new ArrayList<>();
		for (AttackTransferMove atm : movesSoFar.attackTransferMoves) {
			if (atm.getToRegion().getPlayerName().equals("neutral")) {
				expansionMoves.add(atm);
			}
		}

		// If we aren't expanding at all then a random region is good (strange
		// case)
		// http://theaigames.com/competitions/warlight-ai-challenge-2/games/54d13c854b5ab20571901021
		if (expansionMoves.size() == 0) {
			Region bestRegion = HistoryTracker.botState.getVisibleMap().getOwnedRegions().get(0);
			return bestRegion;
		}

		// If we are expanding then look for the attack to the highest
		// ExpansionValueRegion and deploy to the moving region.
		int biggestExpansionValue = 0;
		AttackTransferMove mostImportantMove = expansionMoves.get(0);
		for (AttackTransferMove atm : expansionMoves) {
			if (atm.getToRegion().getExpansionRegionValue() > biggestExpansionValue) {
				biggestExpansionValue = atm.getToRegion().getExpansionRegionValue();
				mostImportantMove = atm;
			}
		}
		return mostImportantMove.getFromRegion();
	}

}
