package bot;

import java.util.ArrayList;
import java.util.List;

import evaluation.FogRemover;
import map.SuperRegion;

public class DeploymentHistory {

	private List<OpponentDeployment> opponentDeployments = new ArrayList<OpponentDeployment>();

	public int getOpponentDeployment() {
		if (HistoryTracker.botState.getRoundNumber() == 1) {
			return 0;
		}
		return opponentDeployments.get(opponentDeployments.size() - 1).visibleDeployment;
	}

	/**
	 * Gets the opponent deployment for the last couple of turns.
	 * 
	 * @param turnsBack
	 *            the turns back from which we want the deployment. 0 for only the last deployment.
	 * @return
	 */
	public int getOpponentDeployment(int turnsBack) {
		int deployment = 0;
		for (int i = opponentDeployments.size() - 1; i >= opponentDeployments.size() - 1 - turnsBack; i--) {
			if (i >= 0) {
				deployment += opponentDeployments.get(i).visibleDeployment;
			}
		}

		return deployment;
	}

	public int getMissingDeployment(int turnsBack) {
		int missingDeployment = 0;
		for (int i = opponentDeployments.size() - 1; i >= opponentDeployments.size() - 1 - turnsBack; i--) {
			if (i >= 0) {
				missingDeployment += opponentDeployments.get(i).missingDeployment;
			}
		}

		return missingDeployment;
	}

	public void update(int opponentDeployment) {
		int brokenSuperRegionIncome = 0;
		for (SuperRegion superRegion : HistoryTracker.botState.getLastVisibleMap().getSuperRegions()) {
			if (superRegion.isOwnedByOpponent()) {
				SuperRegion vmSuperRegion = HistoryTracker.botState.getVisibleMap().getSuperRegion(superRegion.getId());
				if (!vmSuperRegion.isOwnedByOpponent()) {
					brokenSuperRegionIncome += vmSuperRegion.getArmiesReward();
				}
			}
		}

		OpponentDeployment op = new OpponentDeployment();
		op.roundNr = HistoryTracker.botState.getRoundNumber() - 1;
		op.visibleDeployment = opponentDeployment;
		int guessedIncome = HistoryTracker.botState.getGuessedOpponentIncome(HistoryTracker.botState.getVisibleMap());
		guessedIncome += brokenSuperRegionIncome;
		// Subtract the income of SuperRegions that he has taken this turn
		for (SuperRegion superRegion : HistoryTracker.botState.getVisibleMap().getSuperRegions()) {
			if (superRegion.isOwnedByOpponent()) {
				SuperRegion lvmSuperRegion = HistoryTracker.botState.getLastVisibleMap().getSuperRegion(
						superRegion.getId());
				if (!lvmSuperRegion.canBeOwnedByOpponent()) {
					guessedIncome -= superRegion.getArmiesReward();
				}
			}
		}

		// Subtract the income from our super risky guesses
		for (SuperRegion superRegion : FogRemover.superRiskyGuesses) {
			guessedIncome = Math.max(opponentDeployment, guessedIncome - superRegion.getArmiesReward());
		}

		op.missingDeployment = guessedIncome - op.visibleDeployment;
		opponentDeployments.add(op);
		System.err.println("RoundNr: " + op.roundNr);
		System.err.println("VisibleDeployment: " + op.visibleDeployment);
		System.err.println("MissingDeployment: " + op.missingDeployment);
	}

	public class OpponentDeployment {
		public int roundNr = 0;
		public int visibleDeployment = 0;
		public int missingDeployment = 0;
	}

}
