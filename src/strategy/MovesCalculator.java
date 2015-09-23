package strategy;

import java.util.ArrayList;
import java.util.List;

import debug.Debug;
import map.Region;
import map.SuperRegion;
import move.Moves;
import move.MovesCleaner;
import move.MovesCommitter;
import tasks.BreakRegionTask;
import tasks.BreakRegionsTask;
import tasks.DefendRegionTask;
import tasks.DefendSuperRegionTask;
import tasks.DelayTask;
import tasks.DeleteBadMovesTask;
import tasks.FlankSuperRegionTask;
import tasks.JoinInAttacksTask;
import tasks.MoveIdleArmiesTask;
import tasks.NoPlanAttackBestRegionTask;
import tasks.NoPlanCleanupTask;
import tasks.NoPlanTryoutAttackTask;
import tasks.PreventOpponentExpandSuperRegionTask;
import tasks.PreventSuperRegionTask;
import tasks.TakeSuperregionOverTask;
import basicAlgorithms.DistanceCalculator;
import bot.HistoryTracker;
import evaluation.ExpansionMapUpdater;
import evaluation.GameState;
import evaluation.RegionValueCalculator;
import evaluation.SuperRegionExpansionValueCalculator;
import evaluation.SuperRegionValueCalculator;
import evaluation.MapUpdater;

public class MovesCalculator {

	private static Moves calculatedMoves = new Moves();

	public static Moves getCalculatedMoves() {
		return calculatedMoves;
	}

	public static void calculateMoves() {
		calculatedMoves = new Moves();
		Moves movesSoFar = new Moves();

		new GameState().evaluateGameState();
		if (GameState.isGameCompletelyLost()) {
			System.err.println("Game completely lost!");
			new RunawayStrategy().calculateRunawayMoves(movesSoFar);
			calculatedMoves = movesSoFar;
			return;
		}

		System.err.println("Starting armies: " + HistoryTracker.botState.getStartingArmies());

		calculateXSuperRegionMoves(movesSoFar, 1, 1);
		Debug.println("Armies used after calculateXSuperRegionMoves type 1: " + movesSoFar.getTotalDeployment(), 1);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		calculateXSuperRegionMoves(movesSoFar, 1, 2);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		calculateXSuperRegionMoves(movesSoFar, 2, 2);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		Debug.println("Armies used after calculateXSuperRegionMoves type 2: " + movesSoFar.getTotalDeployment(), 1);
		calculateSnipeSuperRegionMoves(movesSoFar);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		Debug.println("Armies used after calculateSnipeSuperRegionMoves" + movesSoFar.getTotalDeployment(), 1);

		calculateXSuperRegionMoves(movesSoFar, 0, 1);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		// calculateXSuperRegionMoves(movesSoFar, 0, 1);
		// Debug.println("Armies used after calculateXSuperRegionMoves type 0: " + movesSoFar.getTotalDeployment(), 1);

		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());

		// int movesWithoutExpansion = movesSoFar.attackTransferMoves.size();
		calculateExpansionMoves(movesSoFar, 100000, -51000);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		Debug.println("Armies used after calculateExpansionMoves: " + movesSoFar.getTotalDeployment(), 1);
		// int movesWithExpansion = movesSoFar.attackTransferMoves.size();

		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());
		calculateNoPlanBreakDefendMoves(movesSoFar, false, false, true, 1, 1);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		Debug.println("Armies used after calculateNoPlanBreakDefendMoves1: " + movesSoFar.getTotalDeployment(), 2);

		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());
		calculateNoPlanBreakDefendMoves(movesSoFar, false, true, false, 1, 1);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		Debug.println("Armies used after calculateNoPlanBreakDefendMoves: " + movesSoFar.getTotalDeployment(), 2);

		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());
		calculateNoPlanBreakDefendMoves(movesSoFar, false, false, true, 2, 2);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);

		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());
		calculateNoPlanBreakDefendMoves(movesSoFar, false, true, true, 1, 2);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);

		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());
		calculateFlankSuperRegionMoves(movesSoFar);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		Debug.println("Armies used after calculateFlankSuperRegionMoves: " + movesSoFar.getTotalDeployment(), 1);

		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());
		calculateNoPlanBreakDefendMoves(movesSoFar, true, false, false, 1, 1);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);

		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());
		calculateNoPlanBreakDefendMoves(movesSoFar, true, false, false, 1, 2);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);


		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getVisibleMap());
		calculateNoPlanAttackRegionsMoves(movesSoFar);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		Debug.println("Armies used after calculateNoPlanAttackRegionsMoves2: " + movesSoFar.getTotalDeployment(), 1);

		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getVisibleMap());
		calculateMoveIdleArmiesMoves(movesSoFar);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		calculateJoinInAttacksMoves(movesSoFar);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		Moves supportTransferMoves = TransferMovesChooser.calculateJoinStackMoves();
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		MovesCommitter.committMoves(supportTransferMoves);
		movesSoFar.mergeMoves(supportTransferMoves);

		// XX
		calculateNoPlanCleanupMoves(movesSoFar);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		calculateMoveIdleArmiesMoves(movesSoFar);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);
		calculateJoinInAttacksMoves(movesSoFar);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);

		calculateNoPlanTryoutAttackMoves(movesSoFar);
		DeleteBadMovesTask.calculateDeleteBadMovesTask(movesSoFar);

		// end xx
		Debug.println("Armies used after all moves done: " + movesSoFar.getTotalDeployment(), 1);
		MapUpdater.updateMap(HistoryTracker.botState.getWorkingMap());

		DistanceCalculator.calculateDistanceToBorder(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());

		ExpansionMapUpdater.updateExpansionMap();
		DistanceCalculator.calculateDistanceToUnimportantRegions(HistoryTracker.botState.getExpansionMap(),
				HistoryTracker.botState.getVisibleMap());
		DistanceCalculator.calculateDistanceToImportantExpansionRegions(HistoryTracker.botState.getExpansionMap(),
				HistoryTracker.botState.getVisibleMap());
		DistanceCalculator.calculateDistanceToOpponentBorderCare3(HistoryTracker.botState.getExpansionMap(),
				HistoryTracker.botState.getVisibleMap());

		for (SuperRegion emSuperRegion : HistoryTracker.botState.getExpansionMap().getSuperRegions()) {
			emSuperRegion.insertMyExpansionValueHeuristic(HistoryTracker.botState.getVisibleMap()
					.getSuperRegion(emSuperRegion.getId()).getMyExpansionValueHeuristic());
		}

		DistanceCalculator.calculateDistanceToHighlyImportantExpansionRegions(
				HistoryTracker.botState.getExpansionMap(), HistoryTracker.botState.getVisibleMap());
		DistanceCalculator.calculateDistanceToOpponentBorderCare4(HistoryTracker.botState.getExpansionMap(),
				HistoryTracker.botState.getVisibleMap());

		Moves transferMoves = TransferMovesChooser.calculateTransferMoves2();
		MovesCommitter.committMoves(transferMoves);
		movesSoFar.mergeMoves(transferMoves);

		calculateDelayMoves(movesSoFar);
		MovesCleaner.cleanupMoves(movesSoFar);
		// movesSoFar = MovesScheduler.scheduleMoves(movesSoFar);
		movesSoFar = MovesScheduler2.scheduleMoves(movesSoFar);
		calculatedMoves = movesSoFar;
	}

	private static void calculateJoinInAttacksMoves(Moves moves) {
		Moves joinInAttackMoves = JoinInAttacksTask.calculateJoinInAttacksTask();
		MovesCommitter.committMoves(joinInAttackMoves);
		moves.mergeMoves(joinInAttackMoves);
	}

	private static void calculateMoveIdleArmiesMoves(Moves moves) {
		Moves idleArmiesMoves = MoveIdleArmiesTask.calculateMoveIdleArmiesTask();
		MovesCommitter.committMoves(idleArmiesMoves);
		moves.mergeMoves(idleArmiesMoves);

		Moves idleExpansionArmiesMoves = MoveIdleArmiesTask.calculateMoveIdleExpansionArmiesTask();
		MovesCommitter.committMoves(idleExpansionArmiesMoves);
		moves.mergeMoves(idleExpansionArmiesMoves);
	}

	private static void calculateDelayMoves(Moves moves) {
		int maxMovesBeforeRiskyAttack = 7;
		int minMovesBeforeRiskyAttack = 1;
		Moves delayMoves = DelayTask.calculateDelayTask(moves, maxMovesBeforeRiskyAttack, minMovesBeforeRiskyAttack);
		MovesCommitter.committMoves(delayMoves);
		moves.mergeMoves(delayMoves);
	}

	private static void calculateNoPlanCleanupMoves(Moves moves) {
		// The cleanup deployment
		int armiesToDeploy = HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment();
		Moves cleanupDeploymentMoves = NoPlanCleanupTask.calculateNoPlanCleanupDeploymentTask(armiesToDeploy, moves);
		MovesCommitter.committMoves(cleanupDeploymentMoves);
		moves.mergeMoves(cleanupDeploymentMoves);

		calculateMoveIdleArmiesMoves(moves);

		// The cleanup expansion moves. We need here already the distance to the opponent border
		ExpansionMapUpdater.updateExpansionMap();
		DistanceCalculator.calculateDistanceToOpponentBorderCare3(HistoryTracker.botState.getExpansionMap(),
				HistoryTracker.botState.getVisibleMap());
		Moves cleanupExpansionMoves = NoPlanCleanupTask.calculateNoPlanCleanupExpansionTask(moves);
		MovesCommitter.committMoves(cleanupExpansionMoves);
		moves.mergeMoves(cleanupExpansionMoves);
	}

	private static void calculateNoPlanTryoutAttackMoves(Moves moves) {
		boolean foundMove = true;
		while (foundMove) {
			foundMove = false;
			Moves tryoutAttackMoves = NoPlanTryoutAttackTask.calculateNoPlanTryoutAttackTask(false, true, true);
			if (tryoutAttackMoves != null) {
				foundMove = true;
				MovesCommitter.committMoves(tryoutAttackMoves);
				moves.mergeMoves(tryoutAttackMoves);
			}
		}
	}

	private static void calculateNoPlanBreakDefendMoves(Moves moves, boolean lowImportance, boolean mediumImportance,
			boolean highImportance, int lowerConservative, int upperConservative) {
		List<Region> regionsToDefend = new ArrayList<Region>();
		for (Region region : HistoryTracker.botState.getVisibleMap().getOwnedRegions()) {
			int importance = region.getDefenceRegionValue();
			boolean lowImportant = importance < RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE;
			boolean mediumImportant = importance < RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE
					&& importance >= RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE;
			boolean highImportant = importance >= RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE;
			if ((lowImportance && lowImportant) || (mediumImportance && mediumImportant)
					|| (highImportance && highImportant)) {
				regionsToDefend.add(region);
			}

		}

		List<Region> possibleRegionsToAttack = new ArrayList<Region>();
		for (Region opponentRegion : HistoryTracker.botState.getVisibleMap().getVisibleOpponentRegions()) {
			Region wmOpponentRegion = HistoryTracker.botState.getWorkingMap().getRegion(opponentRegion.getId());
			if (wmOpponentRegion.getPlayerName().equals(HistoryTracker.botState.getOpponentPlayerName())) {
				possibleRegionsToAttack.add(opponentRegion);
			}
		}

		List<Region> regionsToAttack = new ArrayList<Region>();
		for (Region region : possibleRegionsToAttack) {
			int importance = region.getAttackRegionValue();
			boolean lowImportant = importance < RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE;
			boolean mediumImportant = importance < RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE
					&& importance >= RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE;
			boolean highImportant = importance >= RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE;
			if ((lowImportance && lowImportant) || (mediumImportance && mediumImportant)
					|| (highImportance && highImportant)) {
				regionsToAttack.add(region);
			}
		}

		List<Region> combinedRegions = new ArrayList<Region>();
		combinedRegions.addAll(regionsToDefend);
		combinedRegions.addAll(regionsToAttack);
		List<Region> sortedRegions = RegionValueCalculator.sortRegionsAttackDefense(combinedRegions);

		for (Region region : sortedRegions) {
			int maxDeployment = HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment();
			Moves defendBreakMoves = null;
			if (region.getPlayerName().equals(HistoryTracker.myName)) {
				defendBreakMoves = DefendRegionTask.calculateDefendRegionTask(region, maxDeployment, true,
						lowerConservative, upperConservative);
			} else {
				defendBreakMoves = BreakRegionTask.calculateBreakRegionTask(region, maxDeployment, lowerConservative,
						upperConservative);
			}
			if (defendBreakMoves != null) {
				MovesCommitter.committMoves(defendBreakMoves);
				moves.mergeMoves(defendBreakMoves);
			}
		}
	}

	private static void calculateFlankSuperRegionMoves(Moves moves) {
		int maxDeployment = HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment();
		Moves flankSuperRegionMoves = FlankSuperRegionTask.calculateFlankSuperRegionTask(maxDeployment);
		if (flankSuperRegionMoves != null) {
			System.err.println("FLANK_SUPERREGION_MOVES");
			MovesCommitter.committMoves(flankSuperRegionMoves);
			moves.mergeMoves(flankSuperRegionMoves);
		}
	}

	private static void calculateNoPlanAttackRegionsMoves(Moves moves) {
		boolean foundAnAttack = true;
		while (foundAnAttack) {
			int maxDeployment = HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment();
			Moves unplannedAttackMoves = NoPlanAttackBestRegionTask.calculateNoPlanAttackBestRegionTask(maxDeployment);
			if (unplannedAttackMoves == null) {
				foundAnAttack = false;
			} else {
				MovesCommitter.committMoves(unplannedAttackMoves);
				moves.mergeMoves(unplannedAttackMoves);
			}
		}
	}

	private static void calculateXSuperRegionMoves(Moves moves, int lowerBoundConservative, int upperBoundConservative) {
		boolean solutionFound = true;
		List<SuperRegion> alreadyHandledSuperRegions = new ArrayList<SuperRegion>();

		while (solutionFound) {
			solutionFound = false;
			SuperRegionValueCalculator.calculatSuperRegionValues(HistoryTracker.botState.getWorkingMap(),
					HistoryTracker.botState.getVisibleMap());

			RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
					HistoryTracker.botState.getWorkingMap());
			List<SuperRegion> superRegionsToX = SuperRegionValueCalculator
					.getSortedSuperRegionsAdjustedFactor(HistoryTracker.botState.getVisibleMap());
			superRegionsToX.removeAll(alreadyHandledSuperRegions);

			for (SuperRegion superRegion : superRegionsToX) {
				alreadyHandledSuperRegions.add(superRegion);
				int stillAvailableDeployment = HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment();
				String plan = SuperRegionValueCalculator.getPlanForSuperRegion(superRegion);
				if (plan.equals("BREAK")) {
					List<Region> visibleSubRegions = superRegion.getVisibleOpponentSubRegions();
					Moves breakSuperRegionMoves = BreakRegionsTask.calculateBreakRegionsTask(visibleSubRegions,
							stillAvailableDeployment, lowerBoundConservative, upperBoundConservative);
					if (breakSuperRegionMoves != null) {
						System.err.println("BREAK moves calculated for SuperRegion " + superRegion.getId());
						MovesCommitter.committMoves(breakSuperRegionMoves);
						moves.mergeMoves(breakSuperRegionMoves);
						solutionFound = true;
						break;
					}
				} else if (plan.equals("DEFEND")) {
					Moves defendSuperRegionMoves = DefendSuperRegionTask.calculateDefendSuperRegionTask(superRegion,
							stillAvailableDeployment, false, Math.max(1, lowerBoundConservative),
							Math.max(1, upperBoundConservative));
					if (defendSuperRegionMoves != null) {
						if (defendSuperRegionMoves.getTotalDeployment() > 0
								|| defendSuperRegionMoves.attackTransferMoves.size() > 0) {
							System.err.println("DEFEND moves calculated for SuperRegion " + superRegion.getId());
						}
						MovesCommitter.committMoves(defendSuperRegionMoves);
						moves.mergeMoves(defendSuperRegionMoves);
						solutionFound = true;
						break;
					}

				}

				else if (plan.equals("TAKE_OVER") /* && conservativeLevel == 1 */) {
					Moves takeOverMoves = TakeSuperregionOverTask.calculateTakeSuperRegionOverTask(
							stillAvailableDeployment, superRegion, lowerBoundConservative);
					if (takeOverMoves != null) {
						System.err.println("TAKE_OVER moves calculated for SuperRegion " + superRegion.getId());
						MovesCommitter.committMoves(takeOverMoves);
						moves.mergeMoves(takeOverMoves);
						solutionFound = true;
						break;
					}
				} else if (plan.equals("PREVENT_TAKE_OVER")) {
					Moves preventTakeOverMoves = PreventSuperRegionTask.calculatePreventSuperRegionTask(superRegion,
							stillAvailableDeployment, lowerBoundConservative);
					MovesCommitter.committMoves(preventTakeOverMoves);
					moves.mergeMoves(preventTakeOverMoves);
					solutionFound = true;
					if (preventTakeOverMoves.getTotalDeployment() > 0
							|| preventTakeOverMoves.attackTransferMoves.size() > 0) {
						System.err.println("PREVENT_TAKE_OVER moves calculated for SuperRegion " + superRegion.getId());
					}
					break;
				}
			}
		}
	}

	// private static void calculateXSuperRegionMoves(Moves moves, int conservativeLevel) {
	// boolean solutionFound = true;
	// List<SuperRegion> alreadyHandledSuperRegions = new ArrayList<SuperRegion>();
	//
	// while (solutionFound) {
	// solutionFound = false;
	// SuperRegionValueCalculator.calculatSuperRegionValues(HistoryTracker.botState.getWorkingMap(),
	// HistoryTracker.botState.getVisibleMap());
	//
	// RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
	// HistoryTracker.botState.getWorkingMap());
	// List<SuperRegion> superRegionsToX = SuperRegionValueCalculator
	// .getSortedSuperRegionsAdjustedFactor(HistoryTracker.botState.getVisibleMap());
	// superRegionsToX.removeAll(alreadyHandledSuperRegions);
	//
	// for (SuperRegion superRegion : superRegionsToX) {
	// alreadyHandledSuperRegions.add(superRegion);
	// int stillAvailableDeployment = HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment();
	// String plan = SuperRegionValueCalculator.getPlanForSuperRegion(superRegion);
	// if (plan.equals("BREAK")) {
	// List<Region> visibleSubRegions = superRegion.getVisibleOpponentSubRegions();
	// Moves breakSuperRegionMoves = BreakRegionsTask.calculateBreakRegionsTask(visibleSubRegions,
	// stillAvailableDeployment, conservativeLevel);
	// if (breakSuperRegionMoves != null) {
	// System.err.println("BREAK moves calculated for SuperRegion " + superRegion.getId());
	// MovesCommitter.committMoves(breakSuperRegionMoves);
	// moves.mergeMoves(breakSuperRegionMoves);
	// solutionFound = true;
	// break;
	// }
	// } else if (plan.equals("DEFEND")) {
	// Moves defendSuperRegionMoves = DefendSuperRegionTask.calculateDefendSuperRegionTask(superRegion,
	// stillAvailableDeployment, false, conservativeLevel);
	// if (defendSuperRegionMoves != null) {
	// if (defendSuperRegionMoves.getTotalDeployment() > 0
	// || defendSuperRegionMoves.attackTransferMoves.size() > 0) {
	// System.err.println("DEFEND moves calculated for SuperRegion " + superRegion.getId());
	// }
	// MovesCommitter.committMoves(defendSuperRegionMoves);
	// moves.mergeMoves(defendSuperRegionMoves);
	// solutionFound = true;
	// break;
	// }
	//
	// }
	//
	// else if (plan.equals("TAKE_OVER") /* && conservativeLevel == 1 */) {
	// Moves takeOverMoves = TakeSuperregionOverTask.calculateTakeSuperRegionOverTask(
	// stillAvailableDeployment, superRegion, conservativeLevel);
	// if (takeOverMoves != null) {
	// System.err.println("TAKE_OVER moves calculated for SuperRegion " + superRegion.getId());
	// MovesCommitter.committMoves(takeOverMoves);
	// moves.mergeMoves(takeOverMoves);
	// solutionFound = true;
	// break;
	// }
	// } else if (plan.equals("PREVENT_TAKE_OVER")) {
	// Moves preventTakeOverMoves = PreventSuperRegionTask.calculatePreventSuperRegionTask(superRegion,
	// stillAvailableDeployment, conservativeLevel);
	// MovesCommitter.committMoves(preventTakeOverMoves);
	// moves.mergeMoves(preventTakeOverMoves);
	// solutionFound = true;
	// if (preventTakeOverMoves.getTotalDeployment() > 0
	// || preventTakeOverMoves.attackTransferMoves.size() > 0) {
	// System.err.println("PREVENT_TAKE_OVER moves calculated for SuperRegion " + superRegion.getId());
	// }
	// break;
	// }
	// }
	// }
	// }

	private static void calculateSnipeSuperRegionMoves(Moves moves) {
		int maxDeployment = HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment();
		SuperRegion bestSnipableSuperRegion = PreventOpponentExpandSuperRegionTask.getBestSuperRegionToPrevent(
				HistoryTracker.botState.getVisibleMap(), HistoryTracker.botState.getWorkingMap());
		if (bestSnipableSuperRegion == null) {
			return;
		}

		Moves snipeSuperRegionMoves = PreventOpponentExpandSuperRegionTask
				.calculatePreventOpponentExpandSuperregionTaskk(bestSnipableSuperRegion, maxDeployment,
						HistoryTracker.botState.getVisibleMap(), HistoryTracker.botState.getWorkingMap());
		if (snipeSuperRegionMoves != null) {
			MovesCommitter.committMoves(snipeSuperRegionMoves);
			moves.mergeMoves(snipeSuperRegionMoves);
			System.err.println("Sniped " + bestSnipableSuperRegion.getId());
		}

	}

	private static void calculateExpansionMoves(Moves moves, int maxValue, int minValue) {
		int armiesForExpansion = Math.min(5, HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment());
		int armiesForTakeOver = HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment();
		if (HistoryTracker.botState.getVisibleMap().getOpponentBorderingRegions().size() == 0) {
			armiesForExpansion = HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment();
		}
		addValueToImmediateBonuses(armiesForTakeOver);

		List<SuperRegion> sortedAccessibleSuperRegions = SuperRegionExpansionValueCalculator
				.sortAccessibleSuperRegions(HistoryTracker.botState.getVisibleMap());
		List<SuperRegion> temp = new ArrayList<SuperRegion>();
		for (SuperRegion superRegion : sortedAccessibleSuperRegions) {
			if (superRegion.getExpansionValue() >= minValue && superRegion.getExpansionValue() <= maxValue) {
				temp.add(superRegion);
			}
		}
		sortedAccessibleSuperRegions = temp;

		/*
		 * Try to completely take over the SuperRegions from best to worst. If a SuperRegion can't be taken, then stop,
		 * don't try for worse SuperRegions
		 */
		List<SuperRegion> superRegionsThatCanBeTaken = getSuperRegionsThatCanBeTaken(armiesForTakeOver);
		List<SuperRegion> takenOverSuperRegions = new ArrayList<SuperRegion>();
		int armiesUsedForTakeOver = 0;
		for (SuperRegion superRegion : sortedAccessibleSuperRegions) {
			if (superRegionsThatCanBeTaken.contains(superRegion)) {
				Moves expansionMoves = TakeRegionsTaskCalculator.calculateTakeRegionsTask(armiesForTakeOver,
						superRegion.getNotOwnedRegions(), 1);
				MovesCommitter.committMoves(expansionMoves);
				moves.mergeMoves(expansionMoves);
				armiesForTakeOver -= expansionMoves.getTotalDeployment();
				superRegionsThatCanBeTaken = getSuperRegionsThatCanBeTaken(armiesForTakeOver);
				takenOverSuperRegions.add(superRegion);
				armiesUsedForTakeOver += expansionMoves.getTotalDeployment();
			} else {
				break;
			}
		}

		/*
		 * Only keep continuing expanding after already taken over SuperRegions in some special circumstances.
		 */
		boolean isExpandingAfterTakeOverSmart = true;
		if (HistoryTracker.botState.getVisibleMap().getOpponentBorderingRegions().size() > 0) {
			isExpandingAfterTakeOverSmart = false;
		}
		if (HistoryTracker.botState.getWorkingMap().getOpponentBorderingRegions().size() > 0) {
			isExpandingAfterTakeOverSmart = false;
		}

		boolean opponentBorderPresent = HistoryTracker.botState.getVisibleMap().getOpponentBorderingRegions().size() > 0 ? true
				: false;

		armiesForExpansion = Math.max(0, armiesForExpansion - armiesUsedForTakeOver);
		if (takenOverSuperRegions.size() == 0 || isExpandingAfterTakeOverSmart) {

			SuperRegion superRegionToExpand = null;
			for (SuperRegion superRegion : sortedAccessibleSuperRegions) {
				if (!takenOverSuperRegions.contains(superRegion)) {
					boolean condition1 = superRegion.getVisibleNeutralSubRegions().size() > 0;
					boolean condition2 = superRegion.getArmiesReward() > 0;
					boolean condition3 = !opponentBorderPresent || superRegion.getExpansionValueCategory() > 0;
					if (condition1 && condition2 && condition3) {
						superRegionToExpand = superRegion;
						break;
					}
				}
			}
			if (superRegionToExpand == null) {
				return;
			}

			boolean foundMoves = true;
			boolean firstStep = true;
			int debug = 0;
			while (foundMoves) {
				SuperRegionValueCalculator.calculatSuperRegionValues(HistoryTracker.botState.getWorkingMap(),
						HistoryTracker.botState.getVisibleMap());
				debug++;
				foundMoves = false;
				if (firstStep == false) {

					if (superRegionToExpand.getExpansionValueCategory() == 0) {
						return;
					}

					if (opponentBorderPresent) {
						armiesForExpansion = 0;
					}
					if (superRegionToExpand.getOpponentNeighbors().size() > 0) {
						return;
					}
					if (debug == 1) {
						return;
					}
				}
				Moves oneStepMoves = null;

				if (!opponentBorderPresent) {
					oneStepMoves = TakeRegionsTaskCalculator.calculateOneStepExpandSuperRegionTask(armiesForExpansion,
							superRegionToExpand, false, HistoryTracker.botState.getWorkingMap(), 1);
				} else {
					oneStepMoves = TakeRegionsTaskCalculator.calculateOneStepExpandSuperRegionTask(armiesForExpansion,
							superRegionToExpand, false, HistoryTracker.botState.getWorkingMap(), 1);
				}

				if (oneStepMoves != null) {
					firstStep = false;
					armiesForExpansion -= oneStepMoves.getTotalDeployment();
					MovesCommitter.committMoves(oneStepMoves);
					moves.mergeMoves(oneStepMoves);
					foundMoves = true;
				}
			}
		}
	}

	private static void addValueToImmediateBonuses(int maxDeployment) {
		List<SuperRegion> sortedAccessibleSuperRegions = SuperRegionExpansionValueCalculator
				.sortAccessibleSuperRegions(HistoryTracker.botState.getVisibleMap());
		for (SuperRegion superRegion : sortedAccessibleSuperRegions) {
			if ((superRegion.areAllRegionsVisible()) && (!superRegion.containsOpponentPresence())
					&& (superRegion.getArmiesReward() > 0) && !superRegion.isOwnedByMyself()) {
				List<Region> nonOwnedRegions = superRegion.getNotOwnedRegions();
				Moves expansionMoves = TakeRegionsTaskCalculator.calculateTakeRegionsTask(maxDeployment,
						nonOwnedRegions, 1);
				if (expansionMoves != null) {
					SuperRegionExpansionValueCalculator.addExtraValueForFirstTurnBonus(superRegion);
				}
			}
		}
	}

	private static List<SuperRegion> getSuperRegionsThatCanBeTaken(int maxDeployment) {
		List<SuperRegion> out = new ArrayList<>();
		List<SuperRegion> sortedAccessibleSuperRegions = SuperRegionExpansionValueCalculator
				.sortAccessibleSuperRegions(HistoryTracker.botState.getVisibleMap());
		for (SuperRegion superRegion : sortedAccessibleSuperRegions) {
			if ((superRegion.areAllRegionsVisible()) && (!superRegion.containsOpponentPresence())
					&& (superRegion.getArmiesReward() > 0)) {
				List<Region> nonOwnedRegions = superRegion.getNotOwnedRegions();
				Moves expansionMoves = TakeRegionsTaskCalculator.calculateTakeRegionsTask(maxDeployment,
						nonOwnedRegions, 1);
				if (expansionMoves != null) {
					out.add(superRegion);
				}
			}
		}
		return out;
	}

}
