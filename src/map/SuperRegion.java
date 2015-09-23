package map;

import heuristics.SuperRegionExpansionValueHeuristic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import bot.HistoryTracker;

public class SuperRegion {

	private int id;
	private int armiesReward;
	private LinkedList<Region> subRegions;
	// private int expansionValue = 0;
	private boolean superHeuristic = false;
	private int attackValue = 0;
	private int takeOverValue = 0;
	private int preventTakeOverValue = 0;
	private int expansionValueCategory = 0;
	private int defenseValue = 0;
	public SuperRegionExpansionValueHeuristic myExpansionValueHeuristic = null;
	private SuperRegionExpansionValueHeuristic opponentExpansionValueHeuristic = null;

	public SuperRegion(int id, int armiesReward) {
		this.id = id;
		this.armiesReward = armiesReward;
		subRegions = new LinkedList<Region>();
	}
	
	
	
	public boolean isSuperHeuristic() {
		return superHeuristic;
	}



	public void setSuperHeuristic(boolean superHeuristic) {
		this.superHeuristic = superHeuristic;
	}



	public int getExpansionValue(){
		return (int) this.myExpansionValueHeuristic.expansionValue;
	}

	public SuperRegionExpansionValueHeuristic getMyExpansionValueHeuristic() {
		return myExpansionValueHeuristic;
	}

	public void setMyExpansionValueHeuristic() {
		myExpansionValueHeuristic = new SuperRegionExpansionValueHeuristic(this, HistoryTracker.myName);
	}
	
	public void insertMyExpansionValueHeuristic(SuperRegionExpansionValueHeuristic myValue){
		this.myExpansionValueHeuristic = myValue;
	}
	
	public void insertOpponentExpansionValueHeuristic(SuperRegionExpansionValueHeuristic opponentValue){
		this.opponentExpansionValueHeuristic = opponentValue;
	}

	public SuperRegionExpansionValueHeuristic getOpponentExpansionValueHeuristic() {
		return opponentExpansionValueHeuristic;
	}

	public void setOpponentExpansionValueHeuristic() {
		opponentExpansionValueHeuristic = new SuperRegionExpansionValueHeuristic(this, HistoryTracker.opponentName);
	}

	public void addSubRegion(Region subRegion) {
		if (!subRegions.contains(subRegion))
			subRegions.add(subRegion);
	}

	public int getAttackValue() {
		return attackValue;
	}

	public void setAttackValue(int attackValue) {
		this.attackValue = attackValue;
	}

	public int getTakeOverValue() {
		return takeOverValue;
	}

	public void setTakeOverValue(int takeOverValue) {
		this.takeOverValue = takeOverValue;
	}

	public int getPreventTakeOverValue() {
		return preventTakeOverValue;
	}

	public void setPreventTakeOverValue(int preventTakeOverValue) {
		this.preventTakeOverValue = preventTakeOverValue;
	}

	public int getDefenseValue() {
		return defenseValue;
	}

	public void setDefenseValue(int defenseValue) {
		this.defenseValue = defenseValue;
	}

	public int getExpansionValueCategory() {
		return expansionValueCategory;
	}

	public void setExpansionValueCategory(int expansionValueCategory) {
		this.expansionValueCategory = expansionValueCategory;
	}

	// public int getExpansionValue() {
	// return expansionValue;
	// }

	// public void setExpansionValue(int expansionValue) {
	// this.expansionValue = expansionValue;
	// }

	/**
	 * @return A string with the name of the player that fully owns this SuperRegion
	 */
	public String ownedByPlayer() {
		String playerName = subRegions.getFirst().getPlayerName();
		for (Region region : subRegions) {
			if (!playerName.equals(region.getPlayerName()))
				return null;
		}
		return playerName;
	}

	/**
	 * @return The id of this SuperRegion
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return The number of armies a Player is rewarded when he fully owns this SuperRegion
	 */
	public int getArmiesReward() {
		return armiesReward;
	}

	/**
	 * @return A list with the Regions that are part of this SuperRegion
	 */
	public LinkedList<Region> getSubRegions() {
		return subRegions;
	}

	public List<Region> getNeutralSubRegions() {
		List<Region> out = new ArrayList<>();
		for (Region subRegion : this.getSubRegions()) {
			if (subRegion.getPlayerName().equals("neutral") || subRegion.getPlayerName().equals("unknown")) {
				out.add(subRegion);
			}
		}
		return out;
	}

	public int getNeutralArmies() {
		int out = 0;
		List<Region> neutralSubregions = this.getNeutralSubRegions();
		for (Region region : neutralSubregions) {
			out += region.getArmies();
		}
		return out;
	}

	public List<SuperRegion> getNeighborSuperRegions() {
		List<SuperRegion> out = new ArrayList<SuperRegion>();
		Set<SuperRegion> outSet = new HashSet<SuperRegion>();
		for (Region subRegion : this.getSubRegions()) {
			List<Region> neighbors = subRegion.getNeighbors();
			for (Region neighbor : neighbors) {
				if (neighbor.getSuperRegion() != this) {
					outSet.add(neighbor.getSuperRegion());
				}
			}
		}
		out.addAll(outSet);
		return out;
	}

	public List<Region> getOpponentSubRegions() {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getSubRegions()) {
			if (region.getPlayerName().equals(HistoryTracker.opponentName)) {
				out.add(region);
			}
		}
		return out;
	}

	public List<Region> getOwnedSubRegions() {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getSubRegions()) {
			if (region.getPlayerName().equals(HistoryTracker.myName)) {
				out.add(region);
			}
		}
		return out;
	}

	public List<Region> getOwnedSubRegionsAndNeighbors() {
		Set<Region> regionsToConsider = new HashSet<>();
		for (Region subRegion : this.getSubRegions()) {
			regionsToConsider.add(subRegion);
			regionsToConsider.addAll(subRegion.getNeighbors());
		}
		List<Region> out = new ArrayList<>();
		for (Region regionToConsider : regionsToConsider) {
			if (regionToConsider.getPlayerName().equals(HistoryTracker.myName)) {
				out.add(regionToConsider);
			}
		}
		return out;
	}

	public List<Region> getOwnedNeighborRegions() {
		Set<Region> regionsToConsider = new HashSet<>();
		for (Region subRegion : this.getSubRegions()) {
			List<Region> neighbors = subRegion.getOwnedNeighbors();
			for (Region neighbor : neighbors) {
				if (!neighbor.getSuperRegion().equals(this)) {
					regionsToConsider.add(neighbor);
				}
			}
		}
		List<Region> out = new ArrayList<>();
		out.addAll(regionsToConsider);
		return out;
	}

	public boolean containsOwnPresence() {
		return this.getOwnedSubRegions().size() > 0 ? true : false;
	}

	public boolean containsOpponentPresence() {
		boolean containsOpponentPresence = false;
		for (Region subRegion : this.getSubRegions()) {
			if (subRegion.getPlayerName().equals(HistoryTracker.opponentName)) {
				containsOpponentPresence = true;
			}
		}
		return containsOpponentPresence;
	}

	public boolean areAllRegionsVisibleToOpponent() {
		boolean areAllRegionsVisible = true;
		for (Region region : this.getSubRegions()) {
			if (!region.getPlayerName().equals(HistoryTracker.opponentName)) {
				if (region.getOpponentNeighbors().size() == 0) {
					areAllRegionsVisible = false;
				}
			}
		}
		return areAllRegionsVisible;
	}

	public boolean areAllRegionsVisible() {
		boolean areAllRegionsVisible = true;
		for (Region region : this.getSubRegions()) {
			if (!region.getPlayerName().equals(HistoryTracker.myName)) {
				if (region.getOwnedNeighbors().size() == 0) {
					areAllRegionsVisible = false;
				}
			}
		}
		return areAllRegionsVisible;
	}

	public List<Region> getVisibleOpponentSubRegions() {
		List<Region> out = new ArrayList<>();
		for (Region subRegion : this.getSubRegions()) {
			if (subRegion.getPlayerName().equals(HistoryTracker.opponentName)) {
				out.add(subRegion);
			}
		}
		return out;
	}

	public List<Region> getVisibleNeutralSubRegions() {
		List<Region> out = new ArrayList<>();
		for (Region subRegion : this.getSubRegions()) {
			if (subRegion.getPlayerName().equals("neutral") && subRegion.getOwnedNeighbors().size() > 0) {
				out.add(subRegion);
			}
		}
		return out;
	}

	public List<Region> getNotOwnedRegions() {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getSubRegions()) {
			if (!region.getPlayerName().equals(HistoryTracker.myName)) {
				out.add(region);
			}
		}
		return out;
	}

	public boolean canOpponentTakeOver() {
		boolean canOpponentTakeOver = true;
		if (this.isOwnedByOpponent()) {
			canOpponentTakeOver = false;
		}
		for (Region region : this.getSubRegions()) {
			if (region.getPlayerName().equals("neutral")
					|| (region.getPlayerName().equals(HistoryTracker.myName) && region.getOpponentNeighbors().size() == 0)) {
				canOpponentTakeOver = false;
			}
		}
		return canOpponentTakeOver;
	}

	public boolean canTakeOver() {
		boolean canTakeOver = true;
		if (this.isOwnedByMyself()) {
			canTakeOver = false;
		}
		for (Region region : this.getSubRegions()) {
			if (region.getPlayerName().equals("neutral")
					|| (region.getPlayerName().equals(HistoryTracker.opponentName) && region.getOwnedNeighbors().size() == 0)) {
				canTakeOver = false;
			}
		}
		return canTakeOver;
	}

	public boolean isOwnedByOpponent() {
		boolean isOwnedByOpponent = true;
		for (Region subRegion : this.getSubRegions()) {
			if (!subRegion.getPlayerName().equals(HistoryTracker.opponentName)) {
				isOwnedByOpponent = false;
			}
		}
		return isOwnedByOpponent;
	}

	public boolean canBeOwnedByOpponent() {
		boolean canBeOwnedByOpponent = true;
		for (Region subRegion : this.getSubRegions()) {
			if (subRegion.getPlayerName().equals(HistoryTracker.myName)
					|| (subRegion.getPlayerName().equals("neutral") && subRegion.getOwnedNeighbors().size() > 0)) {
				canBeOwnedByOpponent = false;
			}
		}
		return canBeOwnedByOpponent;
	}

	/**
	 * Calculates the opponent owned neighbor regions that aren't part of this SuperRegion.
	 * 
	 * @return
	 */
	public List<Region> getOpponentNeighbors() {
		List<Region> out = new ArrayList<>();
		for (Region subRegion : this.getSubRegions()) {
			for (Region opponentNeighbor : subRegion.getOpponentNeighbors()) {
				if (!opponentNeighbor.getSuperRegion().equals(this)) {
					out.add(opponentNeighbor);
				}
			}
		}
		return out;
	}

	public List<Region> getOwnedRegionsBorderingOpponentNeighbors() {
		List<Region> out = new ArrayList<>();
		for (Region ownedSubRegion : this.getOwnedSubRegions()) {
			if (ownedSubRegion.getOpponentNeighbors().size() > 0) {
				out.add(ownedSubRegion);
			}
		}
		return out;
	}

	public List<Region> getOpponentRegionsBorderingOwnedNeighbors() {
		List<Region> out = new ArrayList<>();
		for (Region opponentSubRegion : this.getOpponentSubRegions()) {
			if (opponentSubRegion.getOwnedNeighbors().size() > 0) {
				out.add(opponentSubRegion);
			}
		}
		return out;
	}

	public boolean isOwnedByMyself() {
		boolean isOwnedByMyself = true;
		for (Region subRegion : this.getSubRegions()) {
			if (!subRegion.getPlayerName().equals(HistoryTracker.myName)) {
				isOwnedByMyself = false;
			}
		}
		return isOwnedByMyself;
	}

	public boolean isPlayerPresent(String playerName) {
		boolean playerPresent = false;
		if (playerName == HistoryTracker.myName && this.containsOwnPresence()) {
			playerPresent = true;
		} else if (playerName == HistoryTracker.opponentName && this.containsOpponentPresence()) {
			playerPresent = true;
		}
		return playerPresent;
	}

	@Override
	public String toString(){
		return "ID = "+this.getId()+", ArmiesReward = "+this.armiesReward;
	}
	
}
