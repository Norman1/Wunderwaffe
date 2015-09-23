package tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import map.Region;
import map.SuperRegion;
import move.Moves;

/**
 * DefendSuperRegionTask is responsible for defending a SuperRegion under threat.
 * 
 */
public class DefendSuperRegionTask {

	public static Moves calculateDefendSuperRegionTask(SuperRegion superRegion, int maxDeployment,
			boolean acceptNotAllDefense, int lowerConservativeLevel, int upperConservativeLevel) {
		Moves out = new Moves();
		List<Region> threateningRegions = getThreateningRegions(superRegion);
		// First see if we can remove the threat by hitting the threatening
		// region
		if (threateningRegions.size() == 1) {
			Region threatRegion = threateningRegions.get(0);
			int regionsUnderThreat = getAmountOfRegionsUnderThreat(threatRegion, superRegion);
			if (regionsUnderThreat >= 2 && lowerConservativeLevel != 0) {
				Moves removeThreatMoves = OneHitBreakRegionTask.calculateBreakRegionTask(threatRegion, maxDeployment,
						lowerConservativeLevel);
				if (removeThreatMoves != null) {
					removeThreatMoves.attackTransferMoves.get(0).setMessage("earlyAttack");
					return removeThreatMoves;
				}
			}
		}
		// If this is not possible try the classic defense
		List<Region> regionsUnderThreat = superRegion.getOwnedRegionsBorderingOpponentNeighbors();
		out = DefendRegionsTask.calculateDefendRegionsTask(regionsUnderThreat, maxDeployment, acceptNotAllDefense,
				lowerConservativeLevel, upperConservativeLevel);
		return out;
	}

	private static int getAmountOfRegionsUnderThreat(Region threateningRegion, SuperRegion superRegion) {
		int out = 0;
		for (Region neighbor : threateningRegion.getOwnedNeighbors()) {
			if (neighbor.getSuperRegion().equals(superRegion)) {
				out++;
			}
		}
		return out;
	}

	private static List<Region> getThreateningRegions(SuperRegion ownedSuperRegion) {
		Set<Region> out = new HashSet<>();
		for (Region region : ownedSuperRegion.getSubRegions()) {
			out.addAll(region.getOpponentNeighbors());
		}
		List<Region> returnList = new ArrayList<>();
		returnList.addAll(out);
		return returnList;
	}

}
