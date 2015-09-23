package map;

import heuristics.PlayerExpansionValueHeuristic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import bot.HistoryTracker;

public class Map {

	public LinkedList<Region> regions;
	public LinkedList<SuperRegion> superRegions;
	public PlayerExpansionValueHeuristic myExpansionValue = null;
	public PlayerExpansionValueHeuristic opponentExpansionValue = null;

	public Map() {
		this.regions = new LinkedList<Region>();
		this.superRegions = new LinkedList<SuperRegion>();
	}

	public Map(LinkedList<Region> regions, LinkedList<SuperRegion> superRegions) {
		this.regions = regions;
		this.superRegions = superRegions;
	}

	public PlayerExpansionValueHeuristic getMyExpansionValue() {
		if (myExpansionValue == null) {
			setMyExpansionValue();
		}
		return myExpansionValue;
	}

	public void setMyExpansionValue() {
		myExpansionValue = new PlayerExpansionValueHeuristic(this, HistoryTracker.myName);
	}

	public PlayerExpansionValueHeuristic getOpponentExpansionValue() {
		if (opponentExpansionValue == null) {
			setOpponentExpansionValue();
		}
		return opponentExpansionValue;
	}

	public void setOpponentExpansionValue() {
		opponentExpansionValue = new PlayerExpansionValueHeuristic(this, HistoryTracker.opponentName);
	}

	/**
	 * add a Region to the map
	 * 
	 * @param region
	 *            : Region to be added
	 */
	public void add(Region region) {
		for (Region r : regions)
			if (r.getId() == region.getId()) {
				System.err.println("Region cannot be added: id already exists.");
				return;
			}
		regions.add(region);
	}

	/**
	 * add a SuperRegion to the map
	 * 
	 * @param superRegion
	 *            : SuperRegion to be added
	 */
	public void add(SuperRegion superRegion) {
		for (SuperRegion s : superRegions)
			if (s.getId() == superRegion.getId()) {
				System.err.println("SuperRegion cannot be added: id already exists.");
				return;
			}
		superRegions.add(superRegion);
	}

	/**
	 * @return : a new Map object exactly the same as this one
	 */
	public Map getMapCopy() {
		Map newMap = new Map();
		for (SuperRegion sr : superRegions) {
			SuperRegion newSuperRegion = new SuperRegion(sr.getId(), sr.getArmiesReward());
			newSuperRegion.setExpansionValueCategory(sr.getExpansionValueCategory());
			// newSuperRegion.setExpansionValue(sr.getExpansionValue());
			newMap.add(newSuperRegion);
		}

		for (Region r : regions) {
			Region newRegion = new Region(r.getId(), newMap.getSuperRegion(r.getSuperRegion().getId()),
					r.getPlayerName(), r.getArmies());
			newRegion.setOwnershipHeuristic(r.isOwnershipHeuristic());
			newRegion.setExpansionRegionValue(r.getExpansionRegionValue());
			newRegion.setAttackRegionValue(r.getAttackRegionValue());
			newRegion.setDefenceRegionValue(r.getDefenceRegionValue());
			newRegion.setArmies(r.getArmies());
			newMap.add(newRegion);
		}

		for (Region r : regions) {
			Region newRegion = newMap.getRegion(r.getId());
			for (Region neighbor : r.getNeighbors()) {
				newRegion.addNeighbor(newMap.getRegion(neighbor.getId()));
			}
		}
		return newMap;
	}

	public List<Region> getNeutralRegions() {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getRegions()) {
			if (region.getPlayerName().equals("neutral")) {
				out.add(region);
			}
		}
		return out;
	}

	public List<Region> getVisibleOpponentRegions() {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getOpponentRegions()) {
			if (region.isVisible()) {
				out.add(region);
			}
		}
		return out;
	}

	public List<Region> getOpponentRegions() {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getRegions()) {
			if (region.getPlayerName().equals(HistoryTracker.botState.getOpponentPlayerName())) {
				out.add(region);
			}
		}
		return out;
	}

	/**
	 * @return : the list of all Regions in this map
	 */
	public LinkedList<Region> getRegions() {
		return regions;
	}

	/**
	 * @return : the list of all SuperRegions in this map
	 */
	public LinkedList<SuperRegion> getSuperRegions() {
		return superRegions;
	}

	/**
	 * @param id
	 *            : a Region id number
	 * @return : the matching Region object
	 */
	public Region getRegion(int id) {
		for (Region region : regions)
			if (region.getId() == id)
				return region;
		return null;
	}

	/**
	 * @param id
	 *            : a SuperRegion id number
	 * @return : the matching SuperRegion object
	 */
	public SuperRegion getSuperRegion(int id) {
		for (SuperRegion superRegion : superRegions)
			if (superRegion.getId() == id)
				return superRegion;
		return null;
	}

	public String getMapString() {
		String mapString = "";
		for (Region region : regions) {
			mapString = mapString
					.concat(region.getId() + ";" + region.getPlayerName() + ";" + region.getArmies() + " ");
		}
		return mapString;
	}

	public List<Region> getOwnedRegions() {
		List<Region> ownedRegions = new ArrayList<>();
		for (Region region : this.getRegions()) {
			if (region.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())) {
				ownedRegions.add(region);
			}
		}
		return ownedRegions;
	}

	public List<Region> getVisibleNeutralRegions() {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getRegions()) {
			// TODO changed bug
			if (region.getPlayerName().equals("neutral") && region.getOwnedNeighbors().size() > 0) {
				out.add(region);
			}
		}
		return out;
	}

	public List<Region> getOpponentBorderingRegions() {
		List<Region> out = new ArrayList<>();
		List<Region> ownedRegions = getOwnedRegions();
		for (Region ownedRegion : ownedRegions) {
			if (ownedRegion.getOpponentNeighbors().size() > 0) {
				out.add(ownedRegion);
			}
		}
		return out;
	}

	public List<Region> getNonOwnedRegions() {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getRegions()) {
			if (!region.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())) {
				out.add(region);
			}
		}
		return out;
	}

	public List<SuperRegion> getOwnedSuperRegions() {
		List<SuperRegion> out = new ArrayList<>();
		for (SuperRegion superRegion : this.getSuperRegions()) {
			if (superRegion.isOwnedByMyself()) {
				out.add(superRegion);
			}
		}
		return out;
	}

	public List<Region> getBorderRegions() {
		List<Region> ownedRegions = getOwnedRegions();
		List<Region> borderRegions = new ArrayList<>();
		for (Region ownedRegion : ownedRegions) {
			boolean isBorderRegion = false;
			List<Region> neighbors = ownedRegion.getNeighbors();
			for (Region neighbor : neighbors) {
				if (!neighbor.getPlayerName().equals(HistoryTracker.botState.getMyPlayerName())) {
					isBorderRegion = true;
				}
			}
			if (isBorderRegion) {
				borderRegions.add(ownedRegion);
			}
		}
		return borderRegions;
	}

	public List<Region> sortRegionsDistanceToBorder(List<Region> inRegions) {
		List<Region> out = new ArrayList<>();
		List<Region> copy = new ArrayList<>();
		copy.addAll(inRegions);
		while (!copy.isEmpty()) {
			Region lowestDistanceRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getDistanceToBorder() < lowestDistanceRegion.getDistanceToBorder()) {
					lowestDistanceRegion = region;
				}
			}
			out.add(lowestDistanceRegion);
			copy.remove(lowestDistanceRegion);
		}
		return out;
	}

	public List<Region> getNonOpponentBorderingBorderRegions() {
		List<Region> out = new ArrayList<>();
		List<Region> borderRegions = this.getBorderRegions();
		for (Region borderRegion : borderRegions) {
			if (borderRegion.getOpponentNeighbors().size() == 0) {
				out.add(borderRegion);
			}
		}
		return out;
	}

	public static List<Integer> getRegionIDs(List<Region> regions) {
		List<Integer> out = new ArrayList<>();
		for (Region region : regions) {
			out.add(region.getId());
		}
		return out;
	}

	public List<Region> sortRegionsIdleArmies(List<Region> inRegions) {
		List<Region> out = new ArrayList<>();
		out.addAll(inRegions);
		boolean hasSomethingChanged = true;
		while (hasSomethingChanged) {
			hasSomethingChanged = false;
			for (int i = 0; i < inRegions.size() - 1; i++) {
				Region region1 = inRegions.get(i);
				Region region2 = inRegions.get(i + 1);
				if (region2.getIdleArmies() > region1.getIdleArmies()) {
					hasSomethingChanged = true;
					out.set(i, region2);
					out.set(i + 1, region1);
				}
			}
		}
		return out;
	}

	public static List<SuperRegion> sortSuperRegionsArmiesReward(List<SuperRegion> in) {
		List<SuperRegion> out = new ArrayList<>();
		List<SuperRegion> copy = new ArrayList<>();
		copy.addAll(in);
		while (!copy.isEmpty()) {
			SuperRegion highestRewardSuperRegion = copy.get(0);
			for (SuperRegion superRegion : copy) {
				if (superRegion.getArmiesReward() > highestRewardSuperRegion.getArmiesReward()) {
					highestRewardSuperRegion = superRegion;
				}
			}
			copy.remove(highestRewardSuperRegion);
			out.add(highestRewardSuperRegion);
		}
		return out;
	}

	public static List<Region> getOrderedListOfRegionsByIdleArmies(List<Region> in) {
		List<Region> out = new ArrayList<>();
		List<Region> copy = new ArrayList<>();
		copy.addAll(in);
		while (!copy.isEmpty()) {
			Region highestIdleArmiesRegion = copy.get(0);
			for (Region region : copy) {
				boolean defenseValueBigger = region.getDefenceRegionValue() > highestIdleArmiesRegion
						.getDefenceRegionValue();
				boolean defenseValueBiggerAndEqualIdles = region.getIdleArmies() == highestIdleArmiesRegion
						.getIdleArmies() && defenseValueBigger;
				if (region.getIdleArmies() > highestIdleArmiesRegion.getIdleArmies() || defenseValueBiggerAndEqualIdles) {
					highestIdleArmiesRegion = region;
				}
			}
			copy.remove(highestIdleArmiesRegion);
			out.add(highestIdleArmiesRegion);
		}
		return out;
	}

	/**
	 * Returns the according regions from this map to regions from another map.
	 * 
	 * @param otherMapRegions
	 * @return
	 */
	public List<Region> copyRegions(List<Region> otherMapRegions) {
		List<Region> thisMapRegions = new ArrayList<>();
		for (Region otherMapRegion : otherMapRegions) {
			thisMapRegions.add(this.getRegion(otherMapRegion.getId()));
		}
		return thisMapRegions;
	}

}
