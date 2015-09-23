package strategy;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.Region;
import move.AttackTransferMove;
import move.Moves;
import bot.HistoryTracker;
import evaluation.MapUpdater;
import evaluation.RegionValueCalculator;

public class MovesScheduler2 {

	private static List<AttackTransferMove> earlyAttacks = new ArrayList<>();
	private static List<AttackTransferMove> supportMovesWhereOpponentMightBreak = new ArrayList<>();
	private static List<AttackTransferMove> supportMovesWhereOpponentMightGetAGoodAttack = new ArrayList<>();
	private static List<AttackTransferMove> crushingAttackMovesToSlipperyRegions = new ArrayList<>();
	private static List<AttackTransferMove> supportMovesWhereOpponentMightAttack = new ArrayList<>();
	private static List<AttackTransferMove> delayAttackMoves = new ArrayList<>();
	private static List<AttackTransferMove> safeAttackMovesWithGoodAttack = new ArrayList<>();
	private static List<AttackTransferMove> normalSupportMoves = new ArrayList<>();
	private static List<AttackTransferMove> bigExpansionMovesNonAttack = new ArrayList<>();
	private static List<AttackTransferMove> transferMoves = new ArrayList<>();
	private static List<AttackTransferMove> nonOpponentBorderingSmallExpansionMovesNonAttack = new ArrayList<>();
	private static List<AttackTransferMove> opponentBorderingSmallExpansionMovesNonAttack = new ArrayList<>();
	private static List<AttackTransferMove> bigExpansionMovesWithAttack = new ArrayList<>();
	private static List<AttackTransferMove> nonOpponentBorderingSmallExpansionMovesWithAttack = new ArrayList<>();
	private static List<AttackTransferMove> opponentBorderingSmallExpansionMovesWithAttack = new ArrayList<>();
	private static List<AttackTransferMove> safeAttackMovesWithPossibleBadAttack = new ArrayList<>();
	private static List<AttackTransferMove> riskyAttackMoves = new ArrayList<>();
	private static List<AttackTransferMove> transferingExpansionMoves = new ArrayList<>();

	private static List<AttackTransferMove> snipeMoves = new ArrayList<AttackTransferMove>();

	/**
	 * Schedules the AttackTransferMoves.
	 * 
	 * @param movesSoFar
	 * @return
	 */
	public static Moves scheduleMoves(Moves movesSoFar) {
		Moves out = new Moves();
		out.placeArmiesMoves = movesSoFar.placeArmiesMoves;
		out.attackTransferMoves = getSortedMoves(movesSoFar);
		return out;
	}

	private static void clearMoves() {
		earlyAttacks.clear();
		supportMovesWhereOpponentMightBreak.clear();
		supportMovesWhereOpponentMightGetAGoodAttack.clear();
		supportMovesWhereOpponentMightAttack.clear();
		crushingAttackMovesToSlipperyRegions.clear();
		delayAttackMoves.clear();
		safeAttackMovesWithGoodAttack.clear();
		normalSupportMoves.clear();
		bigExpansionMovesNonAttack.clear();
		transferMoves.clear();
		bigExpansionMovesWithAttack.clear();
		nonOpponentBorderingSmallExpansionMovesNonAttack.clear();
		opponentBorderingSmallExpansionMovesNonAttack.clear();
		nonOpponentBorderingSmallExpansionMovesWithAttack.clear();
		opponentBorderingSmallExpansionMovesWithAttack.clear();
		safeAttackMovesWithPossibleBadAttack.clear();
		riskyAttackMoves.clear();
		transferingExpansionMoves.clear();

		snipeMoves.clear();
	}

	/**
	 * Schedules the attacks with 1 in a way that we first attack regions bordering multiple of our regions (since the
	 * stack might move).
	 * 
	 * @param delayAttacks
	 * @return
	 */
	private static List<AttackTransferMove> scheduleDelayAttacks(List<AttackTransferMove> delayAttacks) {
		List<AttackTransferMove> out = new ArrayList<>();
		List<AttackTransferMove> delayAttacksToLonelyRegion = new ArrayList<>();
		List<AttackTransferMove> delayAttacksToNonLonelyRegion = new ArrayList<>();
		for (AttackTransferMove atm : delayAttacks) {
			if (atm.getToRegion().getOwnedNeighbors().size() == 1) {
				delayAttacksToLonelyRegion.add(atm);
			} else {
				delayAttacksToNonLonelyRegion.add(atm);
			}
		}
		out.addAll(delayAttacksToNonLonelyRegion);
		out.addAll(delayAttacksToLonelyRegion);
		return out;
	}

	private static List<AttackTransferMove> scheduleCrushingAttackToSlipperyRegion(List<AttackTransferMove> attacks) {
		List<AttackTransferMove> out = new ArrayList<>();
		List<AttackTransferMove> copy = new ArrayList<>();
		copy.addAll(attacks);
		while (!copy.isEmpty()) {
			AttackTransferMove bestAttack = copy.get(0);
			for (AttackTransferMove atm : copy) {
				if (getSlipperyOpponentRegionNumber(atm.getToRegion()) > getSlipperyOpponentRegionNumber(bestAttack
						.getToRegion())) {
					bestAttack = atm;
				}
			}
			out.add(bestAttack);
			copy.remove(bestAttack);
		}
		return out;
	}

	private static List<AttackTransferMove> scheduleAttacksAttackingArmies(List<AttackTransferMove> attackTransferMoves) {
		List<AttackTransferMove> out = new ArrayList<>();
		List<AttackTransferMove> copy = new ArrayList<>();
		copy.addAll(attackTransferMoves);
		while (!copy.isEmpty()) {
			AttackTransferMove biggestAttack = copy.get(0);
			for (AttackTransferMove atm : copy) {
				if (atm.getArmies() > biggestAttack.getArmies()) {
					biggestAttack = atm;
				}
			}
			out.add(biggestAttack);
			copy.remove(biggestAttack);
		}
		return out;
	}

	private static List<AttackTransferMove> getSortedMoves(Moves movesSoFar) {
		List<AttackTransferMove> unhandledMoves = new ArrayList<AttackTransferMove>();
		unhandledMoves.addAll(movesSoFar.attackTransferMoves);
		Map movesMap = HistoryTracker.botState.getVisibleMap().getMapCopy();
		List<AttackTransferMove> sortedMoves = new ArrayList<AttackTransferMove>();
		while (!unhandledMoves.isEmpty()) {
			AttackTransferMove nextMove = getnextMove(unhandledMoves, movesMap);
			unhandledMoves.remove(nextMove);
			sortedMoves.add(nextMove);
			MapUpdater.updateMap(nextMove, movesMap, 2);
		}
		return sortedMoves;
	}

	private static AttackTransferMove getnextMove(List<AttackTransferMove> unhandledMoves, Map movesMap) {
		clearMoves();
		fillMoveTypes(unhandledMoves, movesMap);

		List<AttackTransferMove> semiSortedMoves = new ArrayList<AttackTransferMove>();

		earlyAttacks = scheduleAttacksAttackingArmies(earlyAttacks);
		supportMovesWhereOpponentMightBreak = scheduleAttacksAttackingArmies(supportMovesWhereOpponentMightBreak);

		crushingAttackMovesToSlipperyRegions = scheduleAttacksAttackingArmies(crushingAttackMovesToSlipperyRegions);
		crushingAttackMovesToSlipperyRegions = scheduleCrushingAttackToSlipperyRegion(crushingAttackMovesToSlipperyRegions);

		supportMovesWhereOpponentMightGetAGoodAttack = scheduleAttacksAttackingArmies(supportMovesWhereOpponentMightGetAGoodAttack);
		supportMovesWhereOpponentMightAttack = scheduleAttacksAttackingArmies(supportMovesWhereOpponentMightAttack);
		delayAttackMoves = scheduleDelayAttacks(delayAttackMoves);
		safeAttackMovesWithGoodAttack = scheduleAttacksAttackingArmies(safeAttackMovesWithGoodAttack);
		normalSupportMoves = scheduleAttacksAttackingArmies(normalSupportMoves);

		bigExpansionMovesNonAttack = sortExpansionMovesOpponentDistance(bigExpansionMovesNonAttack, false);
		bigExpansionMovesNonAttack = scheduleAttacksAttackingArmies(bigExpansionMovesNonAttack);

		nonOpponentBorderingSmallExpansionMovesNonAttack = sortExpansionMovesOpponentDistance(
				nonOpponentBorderingSmallExpansionMovesNonAttack, true);
		nonOpponentBorderingSmallExpansionMovesNonAttack = scheduleAttacksAttackingArmies(nonOpponentBorderingSmallExpansionMovesNonAttack);

		opponentBorderingSmallExpansionMovesNonAttack = scheduleAttacksAttackingArmies(opponentBorderingSmallExpansionMovesNonAttack);

		bigExpansionMovesWithAttack = sortExpansionMovesOpponentDistance(bigExpansionMovesWithAttack, false);
		bigExpansionMovesWithAttack = scheduleAttacksAttackingArmies(bigExpansionMovesWithAttack);

		nonOpponentBorderingSmallExpansionMovesWithAttack = sortExpansionMovesOpponentDistance(
				nonOpponentBorderingSmallExpansionMovesWithAttack, true);
		nonOpponentBorderingSmallExpansionMovesWithAttack = scheduleAttacksAttackingArmies(nonOpponentBorderingSmallExpansionMovesWithAttack);

		opponentBorderingSmallExpansionMovesWithAttack = sortExpansionMovesOpponentDistance(
				opponentBorderingSmallExpansionMovesWithAttack, true);
		opponentBorderingSmallExpansionMovesWithAttack = scheduleAttacksAttackingArmies(opponentBorderingSmallExpansionMovesWithAttack);

		safeAttackMovesWithPossibleBadAttack = scheduleAttacksAttackingArmies(safeAttackMovesWithPossibleBadAttack);
		riskyAttackMoves = scheduleAttacksAttackingArmies(riskyAttackMoves);

		semiSortedMoves.addAll(earlyAttacks);
		semiSortedMoves.addAll(supportMovesWhereOpponentMightBreak);
		semiSortedMoves.addAll(crushingAttackMovesToSlipperyRegions);
		semiSortedMoves.addAll(supportMovesWhereOpponentMightGetAGoodAttack);
		semiSortedMoves.addAll(supportMovesWhereOpponentMightAttack);
		semiSortedMoves.addAll(delayAttackMoves);
		semiSortedMoves.addAll(safeAttackMovesWithGoodAttack);
		semiSortedMoves.addAll(normalSupportMoves);
		semiSortedMoves.addAll(bigExpansionMovesNonAttack);
		semiSortedMoves.addAll(transferMoves);
		semiSortedMoves.addAll(nonOpponentBorderingSmallExpansionMovesNonAttack);
		semiSortedMoves.addAll(opponentBorderingSmallExpansionMovesNonAttack);
		semiSortedMoves.addAll(bigExpansionMovesWithAttack);
		semiSortedMoves.addAll(nonOpponentBorderingSmallExpansionMovesWithAttack);
		semiSortedMoves.addAll(opponentBorderingSmallExpansionMovesWithAttack);
		semiSortedMoves.addAll(transferingExpansionMoves);
		semiSortedMoves.addAll(snipeMoves);
		semiSortedMoves.addAll(safeAttackMovesWithPossibleBadAttack);
		semiSortedMoves.addAll(riskyAttackMoves);
		

		AttackTransferMove nextMove = semiSortedMoves.get(0);

		if (movesMap.getRegion(nextMove.getToRegion().getId()).getOpponentNeighbors().size() > 0) {
			AttackTransferMove substituteMove = getSubstituteMove(nextMove, movesMap, unhandledMoves);
			if (substituteMove != null) {
				nextMove = substituteMove;
			}
		}

		return nextMove;
	}

	/**
	 * Tries to find an attack move to make the support move obsolete.
	 * 
	 * @param supportMove
	 * @return
	 */
	private static AttackTransferMove getSubstituteMove(AttackTransferMove supportMove, Map movesMap,
			List<AttackTransferMove> unhandledMoves) {
		Region regionToDefend = supportMove.getToRegion();
		Region mmRegionToDefend = movesMap.getRegion(regionToDefend.getId());
		if (mmRegionToDefend.getOpponentNeighbors().size() > 1) {
			return null;
		}
		Region mmOpponentRegion = mmRegionToDefend.getOpponentNeighbors().get(0);

		for (AttackTransferMove unhandledMove : unhandledMoves) {
			if (unhandledMove.getToRegion().getId() == mmOpponentRegion.getId()) {
				if (!canOpponentAttackRegion(unhandledMove.getFromRegion())) {
					if (unhandledMove.getArmies() * 0.6 > mmOpponentRegion.getArmies()
							+ HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap())
							+ 3) {
						System.err.println("found substitute move: " + unhandledMove);
						System.err.println(unhandledMove.getToRegion().getArmiesAfterDeploymentAndIncomingAttacks(2)
								+ " | " + mmOpponentRegion.getArmiesAfterDeploymentAndIncomingAttacks(2));
						return unhandledMove;
					}
				}
			}
		}

		return null;
	}

	private static void fillMoveTypes(List<AttackTransferMove> unhandledMoves, Map movesMap) {

		for (AttackTransferMove atm : unhandledMoves) {
			Region mmToRegion = movesMap.getRegion(atm.getToRegion().getId());
			Region mmFromRegion = movesMap.getRegion(atm.getFromRegion().getId());

			if (atm.getMessage().equals("earlyAttack")) {
				earlyAttacks.add(atm);
				// Opponent attack moves
			} else if (mmToRegion.getPlayerName().equals(HistoryTracker.opponentName)) {
				if (atm.getArmies() == 1) {
					delayAttackMoves.add(atm);
				} else if (!canOpponentAttackRegion(atm.getFromRegion())
						&& getSlipperyOpponentRegionNumber(atm.getToRegion()) > -1 && isProbablyCrushingMove(atm)) {
					crushingAttackMovesToSlipperyRegions.add(atm);
				} else if (!canOpponentAttackRegion(atm.getFromRegion()) && isAlwaysGoodAttackMove(atm)) {
					safeAttackMovesWithGoodAttack.add(atm);
				} else if (!canOpponentAttackRegion(atm.getFromRegion()) && !isAlwaysGoodAttackMove(atm)) {
					safeAttackMovesWithPossibleBadAttack.add(atm);
				} else if (canOpponentAttackRegion(atm.getFromRegion())) {
					riskyAttackMoves.add(atm);
				}
			}
			// Transfer moves
			else if (mmToRegion.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())) {
				if (mmToRegion.getOpponentNeighbors().size() > 0 && canOpponentBreakRegion(mmToRegion)) {
					supportMovesWhereOpponentMightBreak.add(atm);
				} else if (mmToRegion.getOpponentNeighbors().size() > 0 && canOpponentGetAGoodAttack(mmToRegion)) {
					supportMovesWhereOpponentMightGetAGoodAttack.add(atm);
				} else if (mmToRegion.getOpponentNeighbors().size() > 0 && canOpponentAttackRegion(mmToRegion)) {
					supportMovesWhereOpponentMightAttack.add(atm);
				} else if (mmToRegion.getOpponentNeighbors().size() > 0 && !canOpponentAttackRegion(mmToRegion)) {
					normalSupportMoves.add(atm);
				} else if (mmToRegion.getOpponentNeighbors().size() == 0) {
					transferMoves.add(atm);
				}
			}

			// Expansion moves
			else if (mmToRegion.getPlayerName().equals("neutral")) {
				if (atm.getMessage().equals("snipe")) {
					snipeMoves.add(atm);
				} else if (Math.round(atm.getArmies() * 0.6) < mmToRegion.getArmies()) {
					transferingExpansionMoves.add(atm);
				} else if (atm.getArmies() > 3 && !canOpponentAttackRegion(mmFromRegion)) {
					bigExpansionMovesNonAttack.add(atm);
				} else if (atm.getArmies() <= 3 && mmToRegion.getOpponentNeighbors().size() == 0
						&& !canOpponentAttackRegion(mmFromRegion)) {
					nonOpponentBorderingSmallExpansionMovesNonAttack.add(atm);
				} else if (atm.getArmies() <= 3 && mmToRegion.getOpponentNeighbors().size() > 0
						&& !canOpponentAttackRegion(mmFromRegion)) {
					opponentBorderingSmallExpansionMovesNonAttack.add(atm);
				} else if (atm.getArmies() > 3 && canOpponentAttackRegion(mmFromRegion)) {
					bigExpansionMovesWithAttack.add(atm);
				} else if (atm.getArmies() <= 3 && mmToRegion.getOpponentNeighbors().size() == 0
						&& canOpponentAttackRegion(mmFromRegion)) {
					nonOpponentBorderingSmallExpansionMovesWithAttack.add(atm);
				} else if (atm.getArmies() <= 3 && mmToRegion.getOpponentNeighbors().size() > 0
						&& canOpponentAttackRegion(mmFromRegion)) {
					opponentBorderingSmallExpansionMovesWithAttack.add(atm);
				}
			}
		}
	}

	private static boolean isAlwaysGoodAttackMove(AttackTransferMove atm) {
		int opponentIncome = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
		int opponentArmies = atm.getToRegion().getArmies() + opponentIncome;
		// Heuristic since the opponent might have more income than expected
		opponentArmies += 3;
		return Math.round(atm.getArmies()) * 0.6 >= Math.round(opponentArmies * 0.7);
	}

	/**
	 * Calculates the highest defense region value of a bordering region that the opponent might break from his slippery
	 * region. If there is no such region then returns -1.
	 * 
	 * @param opponentRegion
	 * @return
	 */
	private static int getSlipperyOpponentRegionNumber(Region slipperyOpponentRegion) {
		List<Region> regionsOpponentMightBreak = new ArrayList<>();
		int opponentIncome = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
		int opponentAttackingArmies = opponentIncome + slipperyOpponentRegion.getArmies() - 1;
		int neededArmiesForDefense = (int) Math.round(opponentAttackingArmies * 0.6);
		for (Region ownedNeighbor : slipperyOpponentRegion.getOwnedNeighbors()) {
			if (ownedNeighbor.getArmiesAfterDeploymentAndIncomingMoves() < neededArmiesForDefense) {
				regionsOpponentMightBreak.add(ownedNeighbor);
			}
		}
		List<Region> sortedOwnedNeighbors = RegionValueCalculator.sortDefenseValue(regionsOpponentMightBreak);
		if (sortedOwnedNeighbors.size() > 0) {
			return sortedOwnedNeighbors.get(0).getDefenceRegionValue();
		} else {
			return -1;
		}

	}

	private static boolean isProbablyCrushingMove(AttackTransferMove attack) {
		int guessedOpponentArmies = attack.getToRegion().getArmiesAfterDeployment(1);
		int maximumOpponentArmies = attack.getToRegion().getArmies()
				+ HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
		int adjustedOpponentArmies = Math.max(guessedOpponentArmies, maximumOpponentArmies - 2);
		boolean isCrushingMove = Math.round(attack.getArmies()) * 0.6 >= adjustedOpponentArmies;
		return isCrushingMove;
	}

	private static boolean canOpponentBreakRegion(Region ourRegion) {
		if (ourRegion.getOpponentNeighbors().size() == 0) {
			return false;
		}
		int opponentIncome = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
		int ourArmies = ourRegion.getArmiesAfterDeploymentAndIncomingMoves();
		int opponentAttackingArmies = opponentIncome;
		for (Region opponentNeighbor : ourRegion.getOpponentNeighbors()) {
			opponentAttackingArmies += opponentNeighbor.getArmies() - 1;
		}
		return Math.round(opponentAttackingArmies) * 0.6 >= ourArmies;
	}

	private static boolean canOpponentGetAGoodAttack(Region ourRegion) {
		if (ourRegion.getOpponentNeighbors().size() == 0) {
			return false;
		}
		int opponentIncome = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
		int ourArmies = ourRegion.getArmiesAfterDeploymentAndIncomingMoves();
		int opponentAttackingArmies = opponentIncome;
		for (Region opponentNeighbor : ourRegion.getOpponentNeighbors()) {
			opponentAttackingArmies += opponentNeighbor.getArmies() - 1;
		}
		return Math.round(opponentAttackingArmies) * 0.6 >= Math.round(ourArmies * 0.7);
	}

	private static boolean canOpponentAttackRegion(Region ourRegion) {
		if (ourRegion.getOpponentNeighbors().size() == 0) {
			return false;
		}
		int opponentIncome = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
		int ourArmies = ourRegion.getArmies();
		int opponentAttackingArmies = opponentIncome;
		for (Region opponentNeighbor : ourRegion.getOpponentNeighbors()) {
			opponentAttackingArmies += opponentNeighbor.getArmies() - 1;
		}
		return Math.round(opponentAttackingArmies) * 0.6 >= Math.round(ourArmies * 0.7);
	}

	/**
	 * Sorts the expansion moves according to the distance of the toRegion to the direct opponent border (without
	 * blocking neutrals).
	 * 
	 * @param unsortedMoves
	 *            the unsorted moves
	 * @param reverse
	 *            if true then the move with the biggest to region distance is returned first, else returned last
	 * @return sorted moves
	 */
	private static List<AttackTransferMove> sortExpansionMovesOpponentDistance(List<AttackTransferMove> unsortedMoves,
			boolean reverse) {
		List<AttackTransferMove> out = new ArrayList<AttackTransferMove>();
		List<AttackTransferMove> temp = new ArrayList<AttackTransferMove>();
		temp.addAll(unsortedMoves);

		while (!temp.isEmpty()) {
			AttackTransferMove extremestDistanceMove = temp.get(0);
			for (AttackTransferMove atm : temp) {
				boolean reverseCondition = atm.getToRegion().getDirectDistanceToOpponentBorder() > extremestDistanceMove
						.getToRegion().getDirectDistanceToOpponentBorder();
				boolean nonReverseCondition = atm.getToRegion().getDirectDistanceToOpponentBorder() < extremestDistanceMove
						.getToRegion().getDirectDistanceToOpponentBorder();
				if ((reverseCondition && reverse) || (nonReverseCondition && !reverse)) {
					extremestDistanceMove = atm;
				}
			}
			temp.remove(extremestDistanceMove);
			out.add(extremestDistanceMove);
		}

		return out;
	}

}
