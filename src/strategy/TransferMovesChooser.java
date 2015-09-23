package strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import evaluation.RegionValueCalculator;

import map.Region;
import move.AttackTransferMove;
import move.Moves;
import bot.HistoryTracker;

public class TransferMovesChooser {

	public static Moves calculateJoinStackMoves() {
		// Calculate the border regions and the regions bordering border regions
		Moves out = new Moves();
		Set<Region> possibleFromRegions = new HashSet<>();
		possibleFromRegions.addAll(HistoryTracker.botState.getVisibleMap().getBorderRegions());
		Set<Region> temp = new HashSet<>();
		for (Region region : possibleFromRegions) {
			temp.addAll(region.getOwnedNeighbors());
		}
		possibleFromRegions.addAll(temp);

		// Calculate which regions use for transferring
		List<Region> goodTransferRegions = new ArrayList<>();
		for (Region possibleFromRegion : possibleFromRegions) {
			if (possibleFromRegion.getOpponentNeighbors().size() == 0
					|| possibleFromRegion.getDefenceRegionValue() < RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE) {
				goodTransferRegions.add(possibleFromRegion);
			}
		}
		// Calculate where to transfer to
		for (Region region : goodTransferRegions) {
			if (region.getOwnedNeighbors().size() > 0 && region.getIdleArmies() > 0) {
				int regionValue = region.getDefenceRegionValue();
				Region bestNeighbor = null;
				int bestNeighborValue = -1;
				for (Region neighbor : region.getOwnedNeighbors()) {
					if (neighbor.getOpponentNeighbors().size() > 0
							&& neighbor.getDefenceRegionValue() > bestNeighborValue) {
						bestNeighbor = neighbor;
						bestNeighborValue = neighbor.getDefenceRegionValue();
					}
				}
				if (bestNeighbor != null && bestNeighborValue > regionValue) {
					AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(), region,
							bestNeighbor, region.getIdleArmies());
					out.attackTransferMoves.add(atm);
				}
			}
		}
		return out;
	}

	public static Moves calculateTransferMoves2() {
		Moves out = new Moves();
		for (Region region : HistoryTracker.botState.getVisibleMap().getOwnedRegions()) {
			if (region.getIdleArmies() > 0 && region.getOpponentNeighbors().size() == 0) {
				// List<Region> ownedNeighbors = region.getOwnedNeighbors();
				List<Region> ownedNeighbors = getOwnedNeighborsAfterExpansion(region);
				if (ownedNeighbors.size() > 0) {
					Region bestNeighbor = region;
					for (Region neighbor : ownedNeighbors) {
						bestNeighbor = getCloserRegion(bestNeighbor, neighbor);
					}
					if (bestNeighbor != region) {
						AttackTransferMove atm = new AttackTransferMove(HistoryTracker.myName, region, bestNeighbor,
								region.getIdleArmies());
						out.attackTransferMoves.add(atm);
					}
				}
			}
		}
		return out;
	}

	private static List<Region> getOwnedNeighborsAfterExpansion(Region ourRegion) {
		List<Region> out = new ArrayList<>();
		Region emOurRegion = HistoryTracker.botState.getExpansionMap().getRegion(ourRegion.getId());
		List<Region> emOwnedNeighbors = emOurRegion.getOwnedNeighbors();
		for (Region emOwnedNeighbor : emOwnedNeighbors) {
			out.add(HistoryTracker.botState.getVisibleMap().getRegion(emOwnedNeighbor.getId()));
		}
		return out;
	}

	private static Region getCloserRegion(Region region1, Region region2) {

		if (getAdjustedDistance(region1) < getAdjustedDistance(region2)) {
			return region1;
		} else if (getAdjustedDistance(region2) < getAdjustedDistance(region1)) {
			return region2;
		}

		if (region1.getDistanceToImportantOpponentBorder() < region2.getDistanceToImportantOpponentBorder()) {
			return region1;
		} else if (region2.getDistanceToImportantOpponentBorder() < region1.getDistanceToImportantOpponentBorder()) {
			return region2;
		}

		if (region1.getDistanceToOpponentBorder() < region2.getDistanceToOpponentBorder()) {
			return region1;
		} else if (region2.getDistanceToOpponentBorder() < region1.getDistanceToOpponentBorder()) {
			return region2;
		}

		if (region1.getDistanceToHighlyImportantSpot() < region2.getDistanceToHighlyImportantSpot()) {
			return region1;
		} else if (region2.getDistanceToHighlyImportantSpot() < region1.getDistanceToHighlyImportantSpot()) {
			return region2;
		}

		if (region1.getDistanceToImportantSpot() < region2.getDistanceToImportantSpot()) {
			return region1;
		} else if (region2.getDistanceToImportantSpot() < region1.getDistanceToImportantSpot()) {
			return region2;
		}

		if (region1.getArmiesAfterDeploymentAndIncomingMoves() > region2.getArmiesAfterDeploymentAndIncomingMoves()) {
			return region1;
		} else if (region2.getArmiesAfterDeploymentAndIncomingMoves() > region1
				.getArmiesAfterDeploymentAndIncomingMoves()) {
			return region2;
		}
		// Prefer region2 by default since the initial region is region1 so we move more
		return region2;

//		if (region1.getId() > region2.getId()) {
//			return region1;
//		} else {
//			return region2;
//		}
	}

	public static int getAdjustedDistance(Region region) {
		int distanceToUnimportantSpot = region.getDistanceToUnimportantSpot() + 5;
		int distanceToImportantExpansionSpot = region.getDistanceToImportantSpot() + 1;
		int distanceToHighlyImportantExpansionSpot = region.getDistanceToHighlyImportantSpot();
		int distanceToOpponentSpot = region.getDistanceToOpponentBorder();
		int distanceToImportantOpponentSpot = region.getDistanceToImportantOpponentBorder();

		int minDistance = Math.min(
				Math.min(Math.min(distanceToUnimportantSpot, distanceToImportantExpansionSpot),
						Math.min(distanceToHighlyImportantExpansionSpot, distanceToOpponentSpot)),
				distanceToImportantOpponentSpot);
		return minDistance;
	}


}
