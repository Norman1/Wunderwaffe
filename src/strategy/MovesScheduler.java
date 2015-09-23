package strategy;

import java.util.ArrayList;
import java.util.List;

import evaluation.RegionValueCalculator;

import map.Region;
import move.AttackTransferMove;
import move.Moves;
import bot.HistoryTracker;

public class MovesScheduler {

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

	/**
	 * Schedules the AttackTransferMoves.
	 * 
	 * @param movesSoFar
	 * @return
	 */
	public static Moves scheduleMoves(Moves movesSoFar) {
		Moves out = new Moves();
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

		fillMoveTypes(movesSoFar);

		out.placeArmiesMoves = movesSoFar.placeArmiesMoves;

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
		
		nonOpponentBorderingSmallExpansionMovesNonAttack = sortExpansionMovesOpponentDistance(nonOpponentBorderingSmallExpansionMovesNonAttack, true);
		nonOpponentBorderingSmallExpansionMovesNonAttack = scheduleAttacksAttackingArmies(nonOpponentBorderingSmallExpansionMovesNonAttack);
		
		opponentBorderingSmallExpansionMovesNonAttack = scheduleAttacksAttackingArmies(opponentBorderingSmallExpansionMovesNonAttack);
		
		bigExpansionMovesWithAttack = sortExpansionMovesOpponentDistance(bigExpansionMovesWithAttack, false);
		bigExpansionMovesWithAttack = scheduleAttacksAttackingArmies(bigExpansionMovesWithAttack);
		
		nonOpponentBorderingSmallExpansionMovesWithAttack = sortExpansionMovesOpponentDistance(nonOpponentBorderingSmallExpansionMovesWithAttack, true);
		nonOpponentBorderingSmallExpansionMovesWithAttack = scheduleAttacksAttackingArmies(nonOpponentBorderingSmallExpansionMovesWithAttack);
		
		opponentBorderingSmallExpansionMovesWithAttack = sortExpansionMovesOpponentDistance(opponentBorderingSmallExpansionMovesWithAttack, true);
		opponentBorderingSmallExpansionMovesWithAttack = scheduleAttacksAttackingArmies(opponentBorderingSmallExpansionMovesWithAttack);
		
		safeAttackMovesWithPossibleBadAttack = scheduleAttacksAttackingArmies(safeAttackMovesWithPossibleBadAttack);
		riskyAttackMoves = scheduleAttacksAttackingArmies(riskyAttackMoves);

		out.attackTransferMoves.addAll(earlyAttacks);
		out.attackTransferMoves.addAll(supportMovesWhereOpponentMightBreak);
		out.attackTransferMoves.addAll(crushingAttackMovesToSlipperyRegions);
		out.attackTransferMoves.addAll(supportMovesWhereOpponentMightGetAGoodAttack);
		out.attackTransferMoves.addAll(supportMovesWhereOpponentMightAttack);
		out.attackTransferMoves.addAll(delayAttackMoves);
		out.attackTransferMoves.addAll(safeAttackMovesWithGoodAttack);
		out.attackTransferMoves.addAll(normalSupportMoves);
		out.attackTransferMoves.addAll(bigExpansionMovesNonAttack);
		out.attackTransferMoves.addAll(transferMoves);
		out.attackTransferMoves.addAll(nonOpponentBorderingSmallExpansionMovesNonAttack);
		out.attackTransferMoves.addAll(opponentBorderingSmallExpansionMovesNonAttack);
		out.attackTransferMoves.addAll(bigExpansionMovesWithAttack);
		out.attackTransferMoves.addAll(nonOpponentBorderingSmallExpansionMovesWithAttack);
		out.attackTransferMoves.addAll(opponentBorderingSmallExpansionMovesWithAttack);
		out.attackTransferMoves.addAll(transferingExpansionMoves);
		out.attackTransferMoves.addAll(safeAttackMovesWithPossibleBadAttack);
		out.attackTransferMoves.addAll(riskyAttackMoves);
		return out;
	}

	/**
	 * Schedules the attacks with 1 in a way that we first attack regions
	 * bordering multiple of our regions (since the stack might move).
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

	private static void fillMoveTypes(Moves movesSoFar) {
		for (AttackTransferMove atm : movesSoFar.attackTransferMoves) {
			// Opponent attack moves
			if (atm.getToRegion().getPlayerName().equals(HistoryTracker.botState.getOpponentPlayerName())) {
				if (atm.getMessage().equals("earlyAttack")) {
					earlyAttacks.add(atm);
				} else if (atm.getArmies() == 1) {
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
			else if (atm.getToRegion().getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())) {
				if (atm.getToRegion().getOpponentNeighbors().size() > 0 && canOpponentBreakRegion(atm.getToRegion())) {
					supportMovesWhereOpponentMightBreak.add(atm);
				} else if (atm.getToRegion().getOpponentNeighbors().size() > 0
						&& canOpponentGetAGoodAttack(atm.getToRegion())) {
					supportMovesWhereOpponentMightGetAGoodAttack.add(atm);
				} else if (atm.getToRegion().getOpponentNeighbors().size() > 0
						&& canOpponentAttackRegion(atm.getToRegion())) {
					supportMovesWhereOpponentMightAttack.add(atm);
				} else if (atm.getToRegion().getOpponentNeighbors().size() > 0
						&& !canOpponentAttackRegion(atm.getToRegion())) {
					normalSupportMoves.add(atm);
				} else if (atm.getToRegion().getOpponentNeighbors().size() == 0) {
					transferMoves.add(atm);
				}
			}
			// Expansion moves
			else if (atm.getToRegion().getPlayerName().equals("neutral")) {
				if (Math.round(atm.getArmies() * 0.6) < atm.getToRegion().getArmies()) {
					transferingExpansionMoves.add(atm);
				} else if (atm.getArmies() > 3 && !canOpponentAttackRegion(atm.getFromRegion())) {
					bigExpansionMovesNonAttack.add(atm);
				} else if (atm.getArmies() <= 3 && atm.getToRegion().getOpponentNeighbors().size() == 0
						&& !canOpponentAttackRegion(atm.getFromRegion())) {
					nonOpponentBorderingSmallExpansionMovesNonAttack.add(atm);
				} else if (atm.getArmies() <= 3 && atm.getToRegion().getOpponentNeighbors().size() > 0
						&& !canOpponentAttackRegion(atm.getFromRegion())) {
					opponentBorderingSmallExpansionMovesNonAttack.add(atm);
				} else if (atm.getArmies() > 3 && canOpponentAttackRegion(atm.getFromRegion())) {
					bigExpansionMovesWithAttack.add(atm);
				} else if (atm.getArmies() <= 3 && atm.getToRegion().getOpponentNeighbors().size() == 0
						&& canOpponentAttackRegion(atm.getFromRegion())) {
					nonOpponentBorderingSmallExpansionMovesWithAttack.add(atm);
				} else if (atm.getArmies() <= 3 && atm.getToRegion().getOpponentNeighbors().size() > 0
						&& canOpponentAttackRegion(atm.getFromRegion())) {
					opponentBorderingSmallExpansionMovesWithAttack.add(atm);
				}
			}
		}
	}

	private static boolean isAlwaysGoodAttackMove(AttackTransferMove atm) {
		int opponentIncome = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
		int opponentArmies = atm.getToRegion().getArmies() + opponentIncome;
		// Heuristic since the opponent might have more income than expected
//		opponentArmies += 3;
		return Math.round(atm.getArmies()) * 0.6 >= Math.round(opponentArmies * 0.7);
	}

	/**
	 * Calculates the highest defense region value of a bordering region that
	 * the opponent might break from his slippery region. If there is no such
	 * region then returns -1.
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
	 * Sorts the expansion moves according to the distance of the toRegion to the direct opponent border (without blocking
	 * neutrals).
	 * @param unsortedMoves the unsorted moves
	 * @param reverse if true then the move with the biggest to region distance is returned first, else returned last
	 * @return sorted moves
	 */
	private static List<AttackTransferMove> sortExpansionMovesOpponentDistance(List<AttackTransferMove> unsortedMoves, boolean reverse){
		List<AttackTransferMove> out = new ArrayList<AttackTransferMove>();
		List<AttackTransferMove> temp = new ArrayList<AttackTransferMove>();
		temp.addAll(unsortedMoves);
		
		while(!temp.isEmpty()){
			AttackTransferMove extremestDistanceMove = temp.get(0);
			for(AttackTransferMove atm : temp){
				boolean reverseCondition = atm.getToRegion().getDirectDistanceToOpponentBorder() > extremestDistanceMove.getToRegion().
						getDirectDistanceToOpponentBorder();
				boolean nonReverseCondition = atm.getToRegion().getDirectDistanceToOpponentBorder() < extremestDistanceMove.getToRegion().
						getDirectDistanceToOpponentBorder();	
				if((reverseCondition && reverse) ||(nonReverseCondition && !reverse)){
					extremestDistanceMove = atm;
				}
			}
			temp.remove(extremestDistanceMove);
			out.add(extremestDistanceMove);
		}
		
		return out;
	}
	

}


