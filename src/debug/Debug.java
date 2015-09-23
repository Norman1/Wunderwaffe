package debug;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.Region;
import map.SuperRegion;
import strategy.TransferMovesChooser;
import bot.HistoryTracker;

public class Debug {

	public static List<Integer> idsToPrint = new ArrayList<Integer>();

	/**
	 * Prints the message to the console. Via the id debug outputs can be dynamically turned off.
	 * 
	 * @param message
	 * @param id
	 */
	public static void println(String message, int id) {
		if (idsToPrint.contains(id)) {
			System.err.println(message);
		}
	}

	public static void printSuperRegionExpansionValues(Map map) {
//		for (SuperRegion superRegion : map.getSuperRegions()) {
//			superRegion.setMyExpansionValueHeuristic();
//			superRegion.setOpponentExpansionValueHeuristic();
//			System.err.println(superRegion.getId()+": "+superRegion.getMyExpansionValueHeuristic());
//		}
	}

	private static void chooseMessagesToPrint() {
		 idsToPrint.add(-1);
		 idsToPrint.add(1);
		idsToPrint.add(2);
	}

	public static void printDebugOutputBeginTurn() {
		System.err.println("========================= " + HistoryTracker.botState.getRoundNumber()
				+ " ==========================");
		chooseMessagesToPrint();
	}

	public static void printDebugOutput() {
		printOpponentSuperRegions();
		// printDistances();
		// System.err.println();
		// printSuperRegionValues();
		// System.err.println();

		// System.err.println("StartingPicksAmount: " + HistoryTracker.botState.getStartingPicksAmount());
		// System.err.println("Known opponent spots: ");
		// for (Region region : HistoryTracker.botState.getVisibleMap().getOpponentRegions()) {
		// System.err.print(region.getId() + ", ");
		// }
		// System.err.println();
	}

	private static void printDistances() {
		System.err.println("Region distances:");
		for (Region region : HistoryTracker.botState.getVisibleMap().getOwnedRegions()) {
			String message = region.getId() + " --> " + region.getDirectDistanceToOpponentBorder() + " | "
					+ region.getDistanceToUnimportantSpot() + " | " + region.getDistanceToImportantSpot() + " | "
					+ region.getDistanceToHighlyImportantSpot() + " | " + region.getDistanceToOpponentBorder() + " | "
					+ region.getDistanceToImportantOpponentBorder() + " || "
					+ TransferMovesChooser.getAdjustedDistance(region);
			System.err.println(message);
		}
	}


	private static void printAllRegions() {
		System.err.println("Regions:");
		for (Region region : HistoryTracker.botState.getVisibleMap().getRegions()) {
			int id = region.getId();
			String player = region.getPlayerName();
			int armies = region.getArmies();
			boolean ownershipHeuristic = region.isOwnershipHeuristic();
			int deployment = region.getTotalDeployment(1);
			String message = "Region " + id + " (" + player + " | " + armies + " | " + ownershipHeuristic + " | "
					+ deployment + ")";
			System.err.println(message);
		}
	}

	private static void printOpponentSuperRegions() {
		String message = "Opponent owns SuperRegions: ";
		for (SuperRegion superRegion : HistoryTracker.botState.getVisibleMap().getSuperRegions()) {
			if (superRegion.isOwnedByOpponent()) {
				message += superRegion.getId() + ", ";
			}
		}
		Debug.println(message, 2);
	}

}
