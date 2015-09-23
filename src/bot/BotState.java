package bot;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.Region;
import map.SuperRegion;
import move.AttackTransferMove;
import move.Move;
import move.PlaceArmiesMove;

public class BotState {

	private String myName = "";
	private String opponentName = "";

	private final Map fullMap = new Map();
	private Map visibleMap;
	public Map lastVisibleMap;
	/**
	 * This map is responsible for storing the current situation according to our already made move decisions during the
	 * current turn.
	 */
	public Map workingMap;

	private Map expansionMap;

	private List<Region> allStartingRegions = new ArrayList<>();
	private ArrayList<Region> pickableStartingRegions;
	private ArrayList<Region> wastelands;
	private ArrayList<Move> opponentMoves;

	private int startingArmies;
	private int maxRounds;
	private int roundNumber;
	private long totalTimebank;
	private long timePerMove;
	private int startingPicksAmount;

	public BotState() {
		opponentMoves = new ArrayList<Move>();
		roundNumber = 0;
	}

	public int getGuessedOpponentIncome(Map mapToUse) {
		int out = 5;
		for (SuperRegion superRegion : mapToUse.getSuperRegions()) {
			if (superRegion.isOwnedByOpponent()) {
				out += superRegion.getArmiesReward();
			}
		}
		return out;
	}

	public void updateSettings(String key, String value) {
		if (key.equals("your_bot"))
			myName = value;
		else if (key.equals("opponent_bot"))
			opponentName = value;
		else if (key.equals("max_rounds"))
			maxRounds = Integer.parseInt(value);
		else if (key.equals("timebank"))
			totalTimebank = Long.parseLong(value);
		else if (key.equals("time_per_move"))
			timePerMove = Long.parseLong(value);
		else if (key.equals("starting_armies")) {
			startingArmies = Integer.parseInt(value);
			roundNumber++; // next round
		} else if (key.equals("starting_pick_amount")) {
			startingPicksAmount = Integer.parseInt(value);
		}
	}

	public void setupAllStartingRegions(String[] mapInput) {
		for (int i = 2; i < mapInput.length; i++) {
			String startingRegionIDString = mapInput[i];
			int startingRegionID = Integer.parseInt(startingRegionIDString);
			Region pickableRegion = fullMap.getRegion(startingRegionID);
			allStartingRegions.add(pickableRegion);
		}

	}

	public void setupMap(String[] mapInput) {
		int i, regionId, superRegionId, wastelandId, reward;

		if (mapInput[1].equals("super_regions")) {
			for (i = 2; i < mapInput.length; i++) {
				try {
					superRegionId = Integer.parseInt(mapInput[i]);
					i++;
					reward = Integer.parseInt(mapInput[i]);
					fullMap.add(new SuperRegion(superRegionId, reward));
				} catch (Exception e) {
					System.err.println("Unable to parse SuperRegions");
				}
			}
		} else if (mapInput[1].equals("regions")) {
			for (i = 2; i < mapInput.length; i++) {
				try {
					regionId = Integer.parseInt(mapInput[i]);
					i++;
					superRegionId = Integer.parseInt(mapInput[i]);
					SuperRegion superRegion = fullMap.getSuperRegion(superRegionId);
					fullMap.add(new Region(regionId, superRegion));
				} catch (Exception e) {
					System.err.println("Unable to parse Regions " + e.getMessage());
				}
			}
		} else if (mapInput[1].equals("neighbors")) {
			for (i = 2; i < mapInput.length; i++) {
				try {
					Region region = fullMap.getRegion(Integer.parseInt(mapInput[i]));
					i++;
					String[] neighborIds = mapInput[i].split(",");
					for (int j = 0; j < neighborIds.length; j++) {
						Region neighbor = fullMap.getRegion(Integer.parseInt(neighborIds[j]));
						region.addNeighbor(neighbor);
					}
				} catch (Exception e) {
					System.err.println("Unable to parse Neighbors " + e.getMessage());
				}
			}
		} else if (mapInput[1].equals("wastelands")) {
			wastelands = new ArrayList<Region>();
			for (i = 2; i < mapInput.length; i++) {
				try {
					wastelandId = Integer.parseInt(mapInput[i]);
					wastelands.add(fullMap.getRegion(wastelandId));
				} catch (Exception e) {
					System.err.println("Unable to parse wastelands " + e.getMessage());
				}
			}
		}
	}

	public void setPickableStartingRegions(String[] mapInput) {
		pickableStartingRegions = new ArrayList<Region>();
		for (int i = 2; i < mapInput.length; i++) {
			int regionId;
			try {
				regionId = Integer.parseInt(mapInput[i]);
				Region pickableRegion = fullMap.getRegion(regionId);
				pickableStartingRegions.add(pickableRegion);
			} catch (Exception e) {
				System.err.println("Unable to parse pickable regions " + e.getMessage());
			}
		}
	}

	public void updateMap(String[] mapInput) {
		visibleMap = fullMap.getMapCopy();
		for (int i = 1; i < mapInput.length; i++) {
			try {
				Region region = visibleMap.getRegion(Integer.parseInt(mapInput[i]));
				String playerName = mapInput[i + 1];
				int armies = Integer.parseInt(mapInput[i + 2]);

				region.setPlayerName(playerName);
				region.setArmies(armies);
				i += 2;
			} catch (Exception e) {
				System.err.println("Unable to parse Map Update " + e.getMessage());
			}
		}
	}

	public void readOpponentMoves(String[] moveInput) {
		opponentMoves.clear();
		for (int i = 1; i < moveInput.length; i++) {
			try {
				Move move;
				if (moveInput[i + 1].equals("place_armies")) {
					Region region = visibleMap.getRegion(Integer.parseInt(moveInput[i + 2]));
					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i + 3]);
					move = new PlaceArmiesMove(playerName, region, armies);
					i += 3;
				} else if (moveInput[i + 1].equals("attack/transfer")) {
					Region fromRegion = visibleMap.getRegion(Integer.parseInt(moveInput[i + 2]));
					if (fromRegion == null) // might happen if the region isn't
											// visible
						fromRegion = fullMap.getRegion(Integer.parseInt(moveInput[i + 2]));

					Region toRegion = visibleMap.getRegion(Integer.parseInt(moveInput[i + 3]));
					if (toRegion == null) // might happen if the region isn't
											// visible
						toRegion = fullMap.getRegion(Integer.parseInt(moveInput[i + 3]));

					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i + 4]);
					move = new AttackTransferMove(playerName, fromRegion, toRegion, armies);
					i += 4;
				} else { // never happens
					continue;
				}
				opponentMoves.add(move);
			} catch (Exception e) {
				System.err.println("Unable to parse Opponent moves " + e.getMessage());
			}
		}
	}

	public int getStartingPicksAmount() {
		return startingPicksAmount;
	}

	public void setStartingPicksAmount(int startingPicksAmount) {
		this.startingPicksAmount = startingPicksAmount;
	}

	public List<Region> getAllStartingRegions() {
		return allStartingRegions;
	}

	public void setAllStartingRegions(List<Region> allStartingRegions) {
		this.allStartingRegions = allStartingRegions;
	}

	public String getMyPlayerName() {
		return myName;
	}

	public String getOpponentPlayerName() {
		return opponentName;
	}

	public int getStartingArmies() {
		return startingArmies;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public Map getWorkingMap() {
		return workingMap;
	}

	public void setWorkingMap(Map workingMap) {
		this.workingMap = workingMap;
	}

	public Map getExpansionMap() {
		return expansionMap;
	}

	public void setExpansionMap(Map expansionMap) {
		this.expansionMap = expansionMap;
	}

	public Map getLastVisibleMap() {
		return lastVisibleMap;
	}

	public Map getVisibleMap() {
		return visibleMap;
	}

	public Map getFullMap() {
		return fullMap;
	}

	public ArrayList<Move> getOpponentMoves() {
		return opponentMoves;
	}

	public List<Region> getPickableStartingRegions() {
		return pickableStartingRegions;
	}

	public List<Region> getWastelands() {
		return this.wastelands;
	}

}
