package tasks;

import map.Region;
import move.AttackTransferMove;
import move.Moves;
import bot.HistoryTracker;

/**
 * JoinInAttacksTask is responsible that regions with idle armies join in
 * attacks to opponent spots, happening from other owned regions.
 * 
 */
public class JoinInAttacksTask {

	public static Moves calculateJoinInAttacksTask() {
		Moves out = new Moves();
		for (Region ourRegion : HistoryTracker.botState.getVisibleMap().getOpponentBorderingRegions()) {
			Region bestSeriouslyAttackedNeighbor = getBestSeriouslyAttackedNeighbor(ourRegion);
			if (ourRegion.getIdleArmies() > 1 && bestSeriouslyAttackedNeighbor != null) {
				AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(), ourRegion,
						bestSeriouslyAttackedNeighbor, ourRegion.getIdleArmies());
				out.attackTransferMoves.add(atm);
			}
		}
		return out;
	}

	private static Region getBestSeriouslyAttackedNeighbor(Region ourRegion) {
		Region out = null;
		int bestNeighborAttackValue = -1;
		for (Region opponentNeighbor : ourRegion.getOpponentNeighbors()) {
			if (isRegionSeriouslyAttacked(opponentNeighbor)
					&& opponentNeighbor.getAttackRegionValue() > bestNeighborAttackValue) {
				bestNeighborAttackValue = opponentNeighbor.getAttackRegionValue();
				out = opponentNeighbor;
			}
		}
		return out;
	}

	private static boolean isRegionSeriouslyAttacked(Region opponentRegion) {
		boolean isSeriouslyAttacked = false;
		for (AttackTransferMove atm : opponentRegion.getIncomingMoves()) {
			if (atm.getArmies() > 1 && !atm.getMessage().equals("tryoutAttack")) {
				isSeriouslyAttacked = true;
			}
		}
		return isSeriouslyAttacked;
	}
}
