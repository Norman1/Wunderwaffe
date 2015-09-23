package move;

import java.util.ArrayList;
import java.util.List;

/**
 * Moves is a data structure to store all calculated moves.
 * 
 */
public class Moves {

	public List<AttackTransferMove> attackTransferMoves = new ArrayList<>();
	public List<PlaceArmiesMove> placeArmiesMoves = new ArrayList<>();

	public void mergeMoves(Moves newMoves) {
		this.attackTransferMoves.addAll(newMoves.attackTransferMoves);
		this.placeArmiesMoves.addAll(newMoves.placeArmiesMoves);
	}

	public void setPlaceArmiesMoves(List<PlaceArmiesMove> placeArmiesMoves) {
		this.placeArmiesMoves = placeArmiesMoves;
	}

	public int getTotalDeployment() {
		int totalDeployment = 0;
		for (PlaceArmiesMove pam : placeArmiesMoves) {
			totalDeployment += pam.getArmies();
		}
		return totalDeployment;
	}

	/**
	 * Creates a copy of this object.
	 * 
	 * @return a copy where the attackTransferMoves and placeArmiesMoves point to the same objects.
	 */
	public Moves copy() {
		Moves copy = new Moves();
		copy.attackTransferMoves.addAll(this.attackTransferMoves);
		copy.placeArmiesMoves.addAll(this.placeArmiesMoves);
		return copy;
	}

}
