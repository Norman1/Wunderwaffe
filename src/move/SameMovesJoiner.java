package move;

import java.util.ArrayList;
import java.util.List;

public class SameMovesJoiner {
	//
	// public static void joinSameMoves(Moves moves) {
	// List<AttackTransferMove> ourAttacks = moves.attackTransferMoves;
	// List<AttackTransferMove> joinedAttacks = joinSameMoves(ourAttacks);
	// moves.attackTransferMoves = joinedAttacks;
	// }
	//
	// private static List<AttackTransferMove>
	// joinSameMoves(List<AttackTransferMove> in) {
	// List<AttackTransferMove> out = new ArrayList<AttackTransferMove>();
	// List<AttackTransferMove> illegalMoves = new
	// ArrayList<AttackTransferMove>();
	// // Step 1
	// for (int i = 0; i < in.size(); i++) {
	// AttackTransferMove movei = in.get(i);
	// for (int j = i + 1; j < in.size(); j++) {
	// AttackTransferMove movej = in.get(j);
	// if (movei.getFromRegion().equals(movej.getFromRegion())
	// && movei.getToRegion().equals(movej.getToRegion())) {
	// if (!illegalMoves.contains(movej)) {
	// illegalMoves.add(movej);
	// }
	// }
	// }
	// }
	// // Step two
	// for (int i = 0; i < in.size(); i++) {
	// AttackTransferMove movei = in.get(i);
	// if (!illegalMoves.contains(movei)) {
	// for (int j = 0; j < illegalMoves.size(); j++) {
	// AttackTransferMove movej = illegalMoves.get(j);
	// if (movei.getFromRegion().equals(movej.getFromRegion())
	// && movei.getToRegion().equals(movej.getToRegion())) {
	// movei.setArmies(movei.getArmies() + movej.getArmies());
	// if (!movej.getMessage().equals("")) {
	// movei.setMessage(movej.getMessage());
	// }
	// }
	//
	// }
	// out.add(movei);
	// }
	// }
	// return out;
	// }

}
