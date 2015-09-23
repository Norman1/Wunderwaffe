package bot;

/**
 * This is a simple bot that does random (but correct) moves.
 * This class implements the Bot interface and overrides its Move methods.
 * You can implement these methods yourself very easily now,
 * since you can retrieve all information about the match from variable â€œstateâ€�.
 * When the bot decided on the move to make, it returns an ArrayList of Moves. 
 * The bot is started by creating a Parser to which you add
 * a new instance of your bot, and then the parser is started.
 */

import java.util.List;

import map.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;
import strategy.MovesCalculator;
import strategy.StartingRegionsChooser;
import basicAlgorithms.DistanceCalculator;
import debug.Debug;
import evaluation.FogRemover;
import evaluation.LastVisibleMapUpdater;
import evaluation.OpponentDeploymentGuesser;
import evaluation.RegionValueCalculator;
import evaluation.SuperRegionExpansionValueCalculator;

public class BotStarter implements Bot {
	@Override
	public Region getStartingRegion(BotState state, Long timeOut) {
		HistoryTracker.myName = state.getMyPlayerName();
		HistoryTracker.opponentName = state.getOpponentPlayerName();
		return StartingRegionsChooser.getStartingRegion(state);
	}

	@Override
	public List<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {
		HistoryTracker.botState = state;
		if (state.getRoundNumber() != 1) {
			LastVisibleMapUpdater.storeOpponentDeployment();
		}
		Debug.printDebugOutputBeginTurn();
		HistoryTracker.readOpponentDeployment();
		FogRemover.removeFog();
		HistoryTracker.botState.workingMap = HistoryTracker.botState.getVisibleMap().getMapCopy();

		DistanceCalculator.calculateDistanceToBorder(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());

		DistanceCalculator.calculateDirectDistanceToOpponentRegions(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getVisibleMap());

		DistanceCalculator.calculateDistanceToOpponentSuperRegions(HistoryTracker.botState.getVisibleMap());
		DistanceCalculator.calculateDistanceToOwnSuperRegions(HistoryTracker.botState.getVisibleMap());
		SuperRegionExpansionValueCalculator.classifySuperRegions(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getVisibleMap());

		RegionValueCalculator.calculateRegionValues(HistoryTracker.botState.getVisibleMap(),
				HistoryTracker.botState.getWorkingMap());
		if (state.getRoundNumber() != 1) {
			// HistoryTracker.storeMissingOpponentDeployment();

		}
		OpponentDeploymentGuesser.guessOpponentDeployment();

		MovesCalculator.calculateMoves();
		Debug.printDebugOutput();
		LastVisibleMapUpdater.storeLastVisibleMap();
		// TODO debug
		Debug.printSuperRegionExpansionValues(HistoryTracker.botState.getVisibleMap());
		return MovesCalculator.getCalculatedMoves().placeArmiesMoves;
	}

	@Override
	public List<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) {
		return MovesCalculator.getCalculatedMoves().attackTransferMoves;
	}

	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
