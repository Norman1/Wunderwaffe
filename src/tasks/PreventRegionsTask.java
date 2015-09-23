package tasks;

import java.util.ArrayList;
import java.util.List;

import map.Region;
import move.AttackTransferMove;
import move.Moves;
import move.PlaceArmiesMove;
import bot.HistoryTracker;

/**
 * This class is responsible for preventing the opponent from taking all of some regions. This is needed to prevent him
 * from completely taking over a SuperRegion.
 * 
 */
public class PreventRegionsTask {

	public static Moves calculatePreventRegionsTask(List<Region> regionsToPrevent, int maxDeployment,
			int conservativeLevel) {
		Moves out = new Moves();
		Moves opponentAttacks = calculateGuessedOpponentTakeOverMoves(regionsToPrevent, true, conservativeLevel);
		if (opponentAttacks == null) {
			return new Moves();
		}
		// Just try to prevent the region with the highest defense region value
		double highestDefenceRegionValue = 0;
		Region highestDefenceValueRegion = null;
		for (Region region : regionsToPrevent) {
			if (region.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())
					&& region.getDefenceRegionValue() >= highestDefenceRegionValue) {
				highestDefenceValueRegion = region;
				highestDefenceRegionValue = region.getDefenceRegionValue();
			}
		}
		int currentArmies = highestDefenceValueRegion.getArmiesAfterDeploymentAndIncomingMoves();
		int attackingArmies = calculateOpponentAttackingArmies(highestDefenceValueRegion, opponentAttacks);
		int minimumNeededArmies = (int) Math.ceil((attackingArmies * 0.6));
		int maximumNeededArmies = minimumNeededArmies;
		int maximumMissingArmies = Math.max(0, maximumNeededArmies - currentArmies);
		int minimumMissingArmies = Math.max(0, minimumNeededArmies - currentArmies);

		if (maximumMissingArmies <= maxDeployment && maximumMissingArmies > 0) {
			PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(),
					highestDefenceValueRegion, maximumMissingArmies);
			out.placeArmiesMoves.add(pam);
		} else if (minimumMissingArmies <= maxDeployment && maxDeployment > 0) {
			PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(),
					highestDefenceValueRegion, maxDeployment);
			out.placeArmiesMoves.add(pam);
		}
		// If no solution then empty moves instead of null
		return out;
	}

	private static int calculateOpponentAttackingArmies(Region region, Moves opponentAttacks) {
		int attackingArmies = 0;
		for (AttackTransferMove atm : opponentAttacks.attackTransferMoves) {
			if (atm.getToRegion().equals(region)) {
				attackingArmies += atm.getArmies();
			}
		}
		return attackingArmies;
	}

	public static Moves calculateGuessedOpponentTakeOverMoves(List<Region> regions, boolean doesOpponentDeploy,
			int conservativeLevel) {
		int opponentIncome = 5;
		if (conservativeLevel == 2) {
			opponentIncome = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
		}
		List<Region> ownedRegions = new ArrayList<>();
		for (Region region : regions) {
			if (region.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())) {
				ownedRegions.add(region);
			}
		}
		Moves opponentAttacks = calculateMinimumOpponentMoves(ownedRegions, conservativeLevel);
		if (opponentAttacks.getTotalDeployment() > opponentIncome) {
			return null;
		}
		if (doesOpponentDeploy) {
			int remainingOpponentIncome = opponentIncome - opponentAttacks.getTotalDeployment();
			while (remainingOpponentIncome > 0) {
				for (AttackTransferMove atm : opponentAttacks.attackTransferMoves) {
					atm.setArmies(atm.getArmies() + 1);
					remainingOpponentIncome--;
					if (remainingOpponentIncome == 0) {
						break;
					}
				}
			}
		}

		return opponentAttacks;
	}

	/**
	 * Calculates the minimum opponent moves that he needs to make if we don't deploy.
	 * 
	 * @param state
	 * @param ownedRegions
	 * @return
	 */
	private static Moves calculateMinimumOpponentMoves(List<Region> ownedRegions, int conservativeLevel) {
		Moves out = new Moves();
		for (Region ownedRegion : ownedRegions) {
			Region attackingOpponentRegion = getOpponentNeighborMaxIdlearmies(ownedRegion, out);
			int stilIdleArmies = calculateStillOpponentIdleArmies(attackingOpponentRegion, out);
			int attackingOpponentArmies = (int) Math.ceil(ownedRegion
					.getArmiesAfterDeploymentAndIncomingAttacks(conservativeLevel) / 0.6);
			// int attackingOpponentArmies = (int) Math.ceil(ownedRegion.getArmies() / 0.6);
			int opponentDeployment = Math.max(0, attackingOpponentArmies - stilIdleArmies);
			if (opponentDeployment > 0) {
				PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getOpponentPlayerName(),
						attackingOpponentRegion, opponentDeployment);
				out.placeArmiesMoves.add(pam);
			}
			AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getOpponentPlayerName(),
					attackingOpponentRegion, ownedRegion, attackingOpponentArmies);
			out.attackTransferMoves.add(atm);
		}

		// Now let's assume that the opponent doesen't leave armies idle
		boolean hasSomethingChanged = true;
		while (hasSomethingChanged) {
			hasSomethingChanged = false;
			for (AttackTransferMove attackTransferMove : out.attackTransferMoves) {
				int stillIdleArmies = calculateStillOpponentIdleArmies(attackTransferMove.getFromRegion(), out);
				if (stillIdleArmies > 0) {
					hasSomethingChanged = true;
					attackTransferMove.setArmies(attackTransferMove.getArmies() + 1);
				}
			}
		}
		return out;
	}

	private static Region getOpponentNeighborMaxIdlearmies(Region ownedRegion, Moves alreadyMadeAttacks) {
		List<Region> opponentNeighbors = ownedRegion.getOpponentNeighbors();
		Region maxIdleArmiesRegion = opponentNeighbors.get(0);
		int maxIdleArmies = calculateStillOpponentIdleArmies(maxIdleArmiesRegion, alreadyMadeAttacks);
		for (Region region : opponentNeighbors) {
			int idleArmies = calculateStillOpponentIdleArmies(region, alreadyMadeAttacks);
			if (idleArmies > maxIdleArmies) {
				maxIdleArmies = idleArmies;
				maxIdleArmiesRegion = region;
			}
		}
		return maxIdleArmiesRegion;
	}

	private static int calculateStillOpponentIdleArmies(Region region, Moves alreadyMadeMoves) {
		int idleArmies = region.getArmies() - 1;
		for (PlaceArmiesMove pam : alreadyMadeMoves.placeArmiesMoves) {
			if (pam.getRegion().equals(region)) {
				idleArmies += pam.getArmies();
			}
		}

		for (AttackTransferMove atm : alreadyMadeMoves.attackTransferMoves) {
			if (atm.getFromRegion().equals(region)) {
				idleArmies -= atm.getArmies();
			}
		}
		return idleArmies;
	}

}
