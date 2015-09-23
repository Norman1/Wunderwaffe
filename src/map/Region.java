package map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import move.AttackTransferMove;
import move.PlaceArmiesMove;
import bot.HistoryTracker;

public class Region {

	private int id;
	private LinkedList<Region> neighbors;
	private SuperRegion superRegion;
	private int armies;
	private String playerName;
	private List<PlaceArmiesMove> deployment = new ArrayList<>();
	private List<PlaceArmiesMove> nullDeployment = new ArrayList<PlaceArmiesMove>();
	private List<PlaceArmiesMove> conservativeDeployment = new ArrayList<PlaceArmiesMove>();
	private List<AttackTransferMove> outgoingMoves = new ArrayList<>();
	private List<AttackTransferMove> incomingMoves = new ArrayList<>();

	private int distanceToBorder;
	private int distanceToUnimportantSpot;
	private int distanceToImportantSpot;
	private int distanceToHighlyImportantSpot;
	private int directDistanceToOpponentBorder;
	private int distanceToOpponentBorder;
	private int distanceToImportantOpponentBorder;
	private int distanceToOpponentSuperRegion = -1;
	private int distanceToOwnSuperRegion = -1;
	private int expansionRegionValue;
	private int attackRegionValue;
	private int defenceRegionValue;
	private int flankingRegionValue;
	private boolean isRegionBlocked = false;
	private boolean isOwnershipHeuristic = false;

	public Region(int id, SuperRegion superRegion) {
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = "unknown";
		this.armies = 0;

		superRegion.addSubRegion(this);
	}

	public int getDistanceToOwnSuperRegion() {
		return distanceToOwnSuperRegion;
	}

	public void setDistanceToOwnSuperRegion(int distanceToOwnSuperRegion) {
		this.distanceToOwnSuperRegion = distanceToOwnSuperRegion;
	}

	public int getDistanceToOpponentSuperRegion() {
		return distanceToOpponentSuperRegion;
	}

	public void setDistanceToOpponentSuperRegion(int distanceToOpponentSuperRegion) {
		this.distanceToOpponentSuperRegion = distanceToOpponentSuperRegion;
	}

	public boolean isOwnershipHeuristic() {
		return isOwnershipHeuristic;
	}

	public void setOwnershipHeuristic(boolean isOwnershipHeuristic) {
		this.isOwnershipHeuristic = isOwnershipHeuristic;
	}

	public boolean isVisible() {
		boolean isVisible = false;
		if (this.getPlayerName().equals(HistoryTracker.myName)) {
			isVisible = true;
		}
		if (this.getOwnedNeighbors().size() > 0) {
			isVisible = true;
		}
		return isVisible;
	}

	/*
	 * We are only interested in our own incoming armies.
	 */
	public int getIncomingArmies() {
		int incomingArmies = 0;
		for (AttackTransferMove atm : this.getIncomingMoves()) {
			incomingArmies += atm.getArmies();
		}
		return incomingArmies;
	}

	/**
	 * For opponent regions.
	 * 
	 * @return
	 */
	public int getArmiesAfterDeploymentAndIncomingAttacks(int type) {
		int remainingArmies = this.getArmiesAfterDeployment(type);
		for (AttackTransferMove atm : this.getIncomingMoves()) {
			remainingArmies -= Math.round(atm.getArmies() * 0.6);
		}
		remainingArmies = Math.max(1, remainingArmies);
		return remainingArmies;
	}

	public int getArmiesAfterDeploymentAndIncomingMoves() {
		int out = this.getArmiesAfterDeployment(1);
		for (AttackTransferMove atm : this.getIncomingMoves()) {
			out += atm.getArmies();
		}
		return out;
	}

	public List<Region> getNeighborsWithinSameSuperRegion() {
		List<Region> out = new ArrayList<>();
		for (Region neighbor : this.getNeighbors()) {
			if (neighbor.getSuperRegion().equals(this.getSuperRegion())) {
				out.add(neighbor);
			}
		}
		return out;
	}

	public boolean isRegionBlocked() {
		return isRegionBlocked;
	}

	public void setRegionBlocked(boolean isRegionBlocked) {
		this.isRegionBlocked = isRegionBlocked;
	}

	public int getExpansionRegionValue() {
		return expansionRegionValue;
	}

	public void setExpansionRegionValue(int expansionRegionValue) {
		this.expansionRegionValue = expansionRegionValue;
	}

	public int getFlankingRegionValue() {
		return flankingRegionValue;
	}

	public void setFlankingRegionValue(int flankingRegionValue) {
		this.flankingRegionValue = flankingRegionValue;
	}

	public int getAttackRegionValue() {
		return attackRegionValue;
	}

	public void setAttackRegionValue(int attackRegionValue) {
		this.attackRegionValue = attackRegionValue;
	}

	public int getDefenceRegionValue() {
		return defenceRegionValue;
	}

	public void setDefenceRegionValue(int defenceRegionValue) {
		this.defenceRegionValue = defenceRegionValue;
	}

	/**
	 * type 1 = normal deployment, type 2 = conservative deployment
	 * 
	 * @param type
	 * @return
	 */
	public List<PlaceArmiesMove> getDeployment(int type) {
		if (type == 0) {
			return nullDeployment;
		} else if (type == 1) {
			return deployment;
		} else {
			return conservativeDeployment;
		}
	}

	public int getTotalDeployment(int type) {
		int out = 0;
		for (PlaceArmiesMove pam : this.getDeployment(type)) {
			out += pam.getArmies();
		}
		return out;
	}

	public int getDistanceToBorder() {
		return distanceToBorder;
	}

	public void setDistanceToBorder(int distanceToBorder) {
		this.distanceToBorder = distanceToBorder;
	}

	public int getDistanceToUnimportantSpot() {
		return distanceToUnimportantSpot;
	}

	public void setDistanceToUnimportantSpot(int distanceToUnimportantSpot) {
		this.distanceToUnimportantSpot = distanceToUnimportantSpot;
	}

	public int getDistanceToImportantSpot() {
		return distanceToImportantSpot;
	}

	public void setDistanceToImportantSpot(int distanceToImportantSpot) {
		this.distanceToImportantSpot = distanceToImportantSpot;
	}

	public int getDistanceToHighlyImportantSpot() {
		return distanceToHighlyImportantSpot;
	}

	public void setDistanceToHighlyImportantSpot(int distanceToHighlyImportantSpot) {
		this.distanceToHighlyImportantSpot = distanceToHighlyImportantSpot;
	}

	public int getDirectDistanceToOpponentBorder() {
		return directDistanceToOpponentBorder;
	}

	public void setDirectDistanceToOpponentBorder(int directDistanceToOpponentBorder) {
		this.directDistanceToOpponentBorder = directDistanceToOpponentBorder;
	}

	public int getDistanceToOpponentBorder() {
		return distanceToOpponentBorder;
	}

	public void setDistanceToOpponentBorder(int distanceToOpponentBorder) {
		this.distanceToOpponentBorder = distanceToOpponentBorder;
	}

	public int getDistanceToImportantOpponentBorder() {
		return distanceToImportantOpponentBorder;
	}

	public void setDistanceToImportantOpponentBorder(int distanceToImportantOpponentBorder) {
		this.distanceToImportantOpponentBorder = distanceToImportantOpponentBorder;
	}

	public void addOutgoingMove(AttackTransferMove attackTransferMove) {
		this.outgoingMoves.add(attackTransferMove);
	}

	public void addIncomingMove(AttackTransferMove attackTransferMove) {
		this.incomingMoves.add(attackTransferMove);
	}

	public List<AttackTransferMove> getOutgoingMoves() {
		return outgoingMoves;
	}

	public void setOutgoingMoves(List<AttackTransferMove> outgoingMoves) {
		this.outgoingMoves = outgoingMoves;
	}

	public List<AttackTransferMove> getExpansionMoves() {
		List<AttackTransferMove> out = new ArrayList<>();
		for (AttackTransferMove atm : this.getOutgoingMoves()) {
			if (atm.getToRegion().getPlayerName().equals("neutral")) {
				out.add(atm);
			}
		}
		return out;
	}

	public List<AttackTransferMove> getIncomingMoves() {
		return incomingMoves;
	}

	public void setIncomingMoves(List<AttackTransferMove> incomingMoves) {
		this.incomingMoves = incomingMoves;
	}

	public int getArmiesAfterDeployment(int type) {
		int armies = this.getArmies();
		for (PlaceArmiesMove pam : this.getDeployment(type)) {
			armies += pam.getArmies();
		}
		return armies;
	}

	public int getIdleArmies() {
		int out = getArmiesAfterDeployment(1);
		for (AttackTransferMove atm : this.getOutgoingMoves()) {
			out -= atm.getArmies();
		}
		out -= 1;
		if (isRegionBlocked) {
			out = 0;
		}
		return out;
	}

	public List<PlaceArmiesMove> getNullDeployment() {
		return nullDeployment;
	}

	public List<PlaceArmiesMove> getConservativeDeployment() {
		return conservativeDeployment;
	}

	public void setConservativeDeployment(List<PlaceArmiesMove> conservativeDeployment) {
		this.conservativeDeployment = conservativeDeployment;
	}

	public void addDeployment(PlaceArmiesMove placeArmiesMove) {
		this.deployment.add(placeArmiesMove);
	}

	public void addConservativeDeployment(PlaceArmiesMove placeArmiesMove) {
		this.conservativeDeployment.add(placeArmiesMove);
	}

	public Region(int id, SuperRegion superRegion, String playerName, int armies) {
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = playerName;
		this.armies = armies;

		superRegion.addSubRegion(this);
	}

	public void addNeighbor(Region neighbor) {
		if (!neighbors.contains(neighbor)) {
			neighbors.add(neighbor);
			neighbor.addNeighbor(this);
		}
	}

	/**
	 * @param region
	 *            a Region object
	 * @return True if this Region is a neighbor of given Region, false otherwise
	 */
	public boolean isNeighbor(Region region) {
		if (neighbors.contains(region))
			return true;
		return false;
	}

	/**
	 * @param playerName
	 *            A string with a player's name
	 * @return True if this region is owned by given playerName, false otherwise
	 */
	public boolean ownedByPlayer(String playerName) {
		if (playerName.equals(this.playerName))
			return true;
		return false;
	}

	/**
	 * @param armies
	 *            Sets the number of armies that are on this Region
	 */
	public void setArmies(int armies) {
		this.armies = armies;
	}

	/**
	 * @param playerName
	 *            Sets the Name of the player that this Region belongs to
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * @return The id of this Region
	 */
	public int getId() {
		return id;
	}

	public int getAmountOfBordersToOpponentSuperRegion() {
		int out = 0;
		for (Region neighbor : this.getNeighbors()) {
			if (!neighbor.getSuperRegion().equals(this.getSuperRegion())) {
				if (neighbor.getSuperRegion().isOwnedByOpponent()) {
					out++;
				}
			}
		}
		return out;
	}

	public int getAmountOfBordersToOwnSuperRegion() {
		int out = 0;
		for (Region neighbor : this.getNeighbors()) {
			if (!neighbor.getSuperRegion().equals(this.getSuperRegion())) {
				if (neighbor.getSuperRegion().isOwnedByMyself()) {
					out++;
				}
			}
		}
		return out;
	}

	public List<Region> getOwnedNeighbors() {
		List<Region> out = new ArrayList<>();
		for (Region neighbor : this.getNeighbors()) {
			if (neighbor.getPlayerName().equals(HistoryTracker.myName)) {
				out.add(neighbor);
			}
		}
		return out;
	}

	public int getSurroundingIdleArmies() {
		int idleArmies = 0;
		for (Region neighbor : this.getOwnedNeighbors()) {
			idleArmies += neighbor.getIdleArmies();
		}
		return idleArmies;
	}

	public List<Region> getNonOwnedNeighbors() {
		List<Region> out = new ArrayList<>();
		List<Region> ownedNeighbors = getOwnedNeighbors();
		for (Region neighbor : this.getNeighbors()) {
			if (!ownedNeighbors.contains(neighbor)) {
				out.add(neighbor);
			}
		}
		return out;
	}

	public List<Region> getOpponentNeighbors() {
		List<Region> out = new ArrayList<>();
		for (Region neighbor : this.getNeighbors()) {
			if (neighbor.getPlayerName().equals(HistoryTracker.opponentName)) {
				out.add(neighbor);
			}
		}
		return out;
	}

	/**
	 * @return A list of this Region's neighboring Regions
	 */
	public LinkedList<Region> getNeighbors() {
		return neighbors;
	}

	/**
	 * @return The SuperRegion this Region is part of
	 */
	public SuperRegion getSuperRegion() {
		return superRegion;
	}

	/**
	 * @return The number of armies on this region
	 */
	public int getArmies() {
		return armies;
	}

	/**
	 * @return A string with the name of the player that owns this region
	 */
	public String getPlayerName() {
		return playerName;
	}

}
