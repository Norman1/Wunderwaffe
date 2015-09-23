package strategy;

import java.util.ArrayList;
import java.util.List;

import map.Region;
import move.AttackTransferMove;
import move.Moves;
import move.MovesCommitter;
import move.PlaceArmiesMove;
import bot.HistoryTracker;

/**
 * This class is responsible for calculating the runaway moves when the game is completely lost.
 *
 */
public class RunawayStrategy {

	public void calculateRunawayMoves(Moves moves) {
		Region bestRunawayRegion = getBestDeploymentRegion();
		PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.myName, bestRunawayRegion,
				HistoryTracker.botState.getStartingArmies());
		MovesCommitter.committPlaceArmiesMove(pam);
		moves.placeArmiesMoves.add(pam);

		Region bestRegionToAttack = getBestRegionToAttack(bestRunawayRegion);
		AttackTransferMove atm = new AttackTransferMove(HistoryTracker.myName, bestRunawayRegion, bestRegionToAttack,
				bestRunawayRegion.getIdleArmies());
		MovesCommitter.committAttackTransferMove(atm);
		moves.attackTransferMoves.add(atm);

	}

	private Region getBestDeploymentRegion() {
		List<Region> ownedRegions = HistoryTracker.botState.getVisibleMap().getOwnedRegions();
		Region bestRegion = ownedRegions.get(0);
		for (Region region : ownedRegions) {
			if (region.getArmies() > bestRegion.getArmies()) {
				bestRegion = region;
			}
		}
		return bestRegion;
	}

	private Region getBestRegionToAttack(Region ourRegion) {
		List<Region> nonCanAttackRegions = getNoCanAttackRegions(ourRegion);
		List<Region> stayAwayRegions = getRegionsToStayAwayFrom(ourRegion);
		Region goodRegionToAttack = getGoodAttackRegion(ourRegion, nonCanAttackRegions, stayAwayRegions);
		if (goodRegionToAttack != null) {
			return goodRegionToAttack;
		}


		for (Region neighbor : ourRegion.getNeighbors()) {
			if (neighbor.getPlayerName().equals("neutral")) {
				return neighbor;
			}
		}
		
		if (ourRegion.getOpponentNeighbors().size() == 0) {
			return ourRegion;
		}
		
		Region bestOpponentNeighbor = ourRegion.getOpponentNeighbors().get(0);
		for (Region opponentNeighbor : ourRegion.getOpponentNeighbors()) {
			if (opponentNeighbor.getArmies() < bestOpponentNeighbor.getArmies()) {
				bestOpponentNeighbor = opponentNeighbor;
			}
		}
		return bestOpponentNeighbor;
	}

	private Region getGoodAttackRegion(Region ourRegion, List<Region> nonCanAttackRegions, List<Region> stayAwayRegions) {
		List<Region> possibleAttackRegions = new ArrayList<Region>();
		for (Region neighbor : ourRegion.getNeighbors()) {
			if (!nonCanAttackRegions.contains(neighbor) && !stayAwayRegions.contains(neighbor)) {
				possibleAttackRegions.add(neighbor);
			}
		}
		if (possibleAttackRegions.size() == 0) {
			return null;
		}
		HistoryTracker.botState.getVisibleMap().setOpponentExpansionValue();
		Region bestNeighbor = possibleAttackRegions.get(0);
		for (Region neighbor : possibleAttackRegions) {
			if (!neighbor.getSuperRegion().isOwnedByOpponent()) {
				bestNeighbor = neighbor;
			}
		}

		return bestNeighbor;
	}

	private List<Region> getRegionsToStayAwayFrom(Region ourRegion) {
		List<Region> stayAwayRegions = new ArrayList<Region>();
		for (Region ownedNeighbor : ourRegion.getOwnedNeighbors()) {
			if (ownedNeighbor.getOpponentNeighbors().size() > 0) {
				stayAwayRegions.add(ownedNeighbor);
				for (Region neighborNeighbor : ownedNeighbor.getNeighbors()) {
					stayAwayRegions.add(neighborNeighbor);
				}
			}
		}
		Region biggestOpponentStackNeighbor = null;
		int biggestOpponentStack = 0;
		for (Region opponentNeighbor : ourRegion.getOpponentNeighbors()) {
			if (opponentNeighbor.getArmies() > biggestOpponentStack) {
				biggestOpponentStack = opponentNeighbor.getArmies();
				biggestOpponentStackNeighbor = opponentNeighbor;
			}
		}
		if (biggestOpponentStackNeighbor != null) {
			stayAwayRegions.add(biggestOpponentStackNeighbor);
		}
		return stayAwayRegions;
	}

	private List<Region> getNoCanAttackRegions(Region ourRegion) {
		List<Region> out = new ArrayList<Region>();
		for (Region neighbor : ourRegion.getNonOwnedNeighbors()) {
			int ourAttackingArmies = ourRegion.getIdleArmies() + HistoryTracker.botState.getStartingArmies();
			if (neighbor.getArmies() > ourAttackingArmies * 0.6) {
				out.add(neighbor);
			}
		}
		return out;
	}

}
