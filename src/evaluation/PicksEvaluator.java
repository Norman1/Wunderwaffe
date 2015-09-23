package evaluation;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.Region;
import bot.HistoryTracker;

public class PicksEvaluator {

	/**
	 * The maximum depth for searching the solution tree.
	 */
	private static int maxDepth = -1;

	private static int firstPlayer = 0;

	private static Map startingRegionMap = null;

	private static final int ACCEPTABLE_TREE_STEPS = 4000;

	/**
	 * Calculates which region to pick next.
	 * 
	 * @param startingRegionMap
	 * @return
	 */
	public static int getNextPick(Map startingRegionMap) {
		PicksEvaluator.startingRegionMap = startingRegionMap;
		List<Integer> stillPickableRegions = Map.getRegionIDs(HistoryTracker.botState.getPickableStartingRegions());
		setMaxDepth(stillPickableRegions.size());
		calculateWhoWentFirst();
		MinMaxNode minMaxNode = minMax(0, new ArrayList<Integer>(), new ArrayList<Integer>());
		System.err.println("--> minMaxNode.nodeDecision: " + minMaxNode.nodeDecision);
		System.err.println("--> minMaxNode.minMaxValue: " + minMaxNode.minMaxValue);
		System.err.println("--> maxDepth: " + maxDepth);
		System.err.println(calculateMoveOrder(0));
		return minMaxNode.nodeDecision;
	}

	private static MinMaxNode minMax(int currentDepth, List<Integer> opponentPickedRegions, List<Integer> pickedRegions) {
		MinMaxNode node = new PicksEvaluator().new MinMaxNode();
		node.opponentPickedRegions.addAll(opponentPickedRegions);
		node.pickedRegions.addAll(pickedRegions);
		List<Integer> moveOrder = calculateMoveOrder(pickedRegions.size() + opponentPickedRegions.size());

		// If we are at a natural leaf or don't want to search the tree any deeper then evaluate.
		if (currentDepth == maxDepth || moveOrder.size() == 0) {
			Map mapCopy = startingRegionMap.getMapCopy();
			fillMovesIntoMap(mapCopy, pickedRegions, opponentPickedRegions);

			node.minMaxValue = (int) (mapCopy.getMyExpansionValue().playerExpansionValue
					- mapCopy.getOpponentExpansionValue().playerExpansionValue);
			return node;
		}

		// Span the tree
		int currentPlayer = moveOrder.get(0);
		List<Integer> stillPickableRegions = getStillPickableRegions(pickedRegions, opponentPickedRegions);
		List<MinMaxNode> children = new ArrayList<PicksEvaluator.MinMaxNode>();
		for (int regionId : stillPickableRegions) {
			List<Integer> childPickedRegions = new ArrayList<Integer>();
			childPickedRegions.addAll(pickedRegions);
			List<Integer> childOpponentPickedRegions = new ArrayList<Integer>();
			childOpponentPickedRegions.addAll(opponentPickedRegions);
			if (currentPlayer == 1) {
				childPickedRegions.add(regionId);
			} else {
				childOpponentPickedRegions.add(regionId);
			}
			MinMaxNode child = minMax(currentDepth + 1, childOpponentPickedRegions, childPickedRegions);
			children.add(child);
		}

		// Minimize or Maximize
		MinMaxNode bestChild = children.get(0);
		for (MinMaxNode child : children) {
			if (currentPlayer == 1 && child.minMaxValue > bestChild.minMaxValue) {
				bestChild = child;
			} else if (currentPlayer == -1 && child.minMaxValue < bestChild.minMaxValue) {
				bestChild = child;
			}
		}
		node.minMaxValue = bestChild.minMaxValue;
		node.nodeDecision = getPickedRegion(node.pickedRegions, node.opponentPickedRegions, bestChild.pickedRegions,
				bestChild.opponentPickedRegions);
		return node;
	}

	private static int getPickedRegion(List<Integer> parentPickedRegions, List<Integer> opponentParentPickedRegions,
			List<Integer> childPickedRegions, List<Integer> opponentChildPickedRegions) {
		int pickedRegion = -1;
		List<Integer> parentRegions = new ArrayList<Integer>();
		parentRegions.addAll(parentPickedRegions);
		parentRegions.addAll(opponentParentPickedRegions);
		List<Integer> childRegions = new ArrayList<Integer>();
		childRegions.addAll(childPickedRegions);
		childRegions.addAll(opponentChildPickedRegions);

		for (int regionId : childRegions) {
			if (!parentRegions.contains(regionId)) {
				pickedRegion = regionId;
			}
		}

		return pickedRegion;
	}

	/**
	 * Calculates which regions are still unpicked.
	 * 
	 * @param pickedRegions
	 * @param opponentPickedRegions
	 * @return
	 */
	private static List<Integer> getStillPickableRegions(List<Integer> pickedRegions,
			List<Integer> opponentPickedRegions) {
		List<Integer> allPickableRegions = Map.getRegionIDs(HistoryTracker.botState.getPickableStartingRegions());
		List<Integer> stillPickableRegions = new ArrayList<Integer>();
		for (int regionId : allPickableRegions) {
			if (!pickedRegions.contains(regionId) && !opponentPickedRegions.contains(regionId)) {
				stillPickableRegions.add(regionId);
			}
		}

		return stillPickableRegions;
	}

	/**
	 * Updates the map according to the made moves.
	 * 
	 * @param mapToFill
	 * @param ourMoves
	 * @param opponentMoves
	 */
	private static void fillMovesIntoMap(Map mapToFill, List<Integer> ourMoves, List<Integer> opponentMoves) {
		for (int i = 0; i < ourMoves.size(); i++) {
			mapToFill.getRegion(ourMoves.get(i)).setPlayerName(HistoryTracker.myName);
		}
		for (int i = 0; i < opponentMoves.size(); i++) {
			mapToFill.getRegion(opponentMoves.get(i)).setPlayerName(HistoryTracker.opponentName);
		}
	}

	/**
	 * Calculates the move order after in this step already some regions were picked for tryout.
	 * 
	 * @param pickedRegionAmount
	 * @return
	 */
	private static List<Integer> calculateMoveOrder(int pickedRegionAmount) {
		List<Integer> moveOrder = new ArrayList<Integer>();
		// first calculate the whole move order
		int startingPicksAmount = HistoryTracker.botState.getStartingPicksAmount();
		int reverse = 1;
		for (int i = 1; i <= startingPicksAmount; i++) {
			moveOrder.add(1 * reverse * firstPlayer);
			reverse *= -1;
			moveOrder.add(1 * reverse * firstPlayer);
		}
		// Remove the moves that were already done this turn
		for (int i = 0; i < pickedRegionAmount; i++) {
			moveOrder.remove(0);
		}
		// Remove the moves that were already done previous turns
		for (int i = 0; i < HistoryTracker.botState.getAllStartingRegions().size()
				- HistoryTracker.botState.getPickableStartingRegions().size(); i++) {
			moveOrder.remove(0);
		}
		return moveOrder;
	}

	/**
	 * Calculates which player had his first pick in the game.
	 */
	private static void calculateWhoWentFirst() {
		List<Region> allPickableRegions = HistoryTracker.botState.getAllStartingRegions();
		if (HistoryTracker.botState.getPickableStartingRegions().size() == allPickableRegions.size()) {
			firstPlayer = 1;
		} else if (HistoryTracker.botState.getPickableStartingRegions().size() == allPickableRegions.size() - 1) {
			firstPlayer = -1;
		}
	}

	/**
	 * Sets the maxDepth value.
	 * 
	 * @param regionsToDistribute
	 */
	private static void setMaxDepth(int regionsToDistribute) {
		int treeSteps = 1;
		do {
			maxDepth++;
			treeSteps = 1;
			for (int i = regionsToDistribute; i > regionsToDistribute - maxDepth; i--) {
				treeSteps *= i;
			}
		} while (maxDepth <= 10 && treeSteps <= ACCEPTABLE_TREE_STEPS);
		maxDepth--;
	}

	private class MinMaxNode {
		/**
		 * The minMax value.
		 */
		int minMaxValue = 0;
		/**
		 * The region picked in this node according to the minMax value.
		 */
		int nodeDecision = 0;
		/**
		 * The picked regions along this branch including the nodeDecision if it's our turn.
		 */
		List<Integer> pickedRegions = new ArrayList<Integer>();

		/**
		 * The opponent picked regions along this branch including the nodeDecision if it's the opponent turn.
		 */
		List<Integer> opponentPickedRegions = new ArrayList<Integer>();

	}
}
