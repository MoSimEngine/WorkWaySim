package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization;

import java.util.Collections;
import java.util.LinkedList;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.SynchroniseToken.SynchronisedActionTypen;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;

public class RTITimelineSynchronizer implements TimelineSynchronizer {
	private LinkedList<SynchroniseToken> advanceTimeTokens;
	private LinkedList<SynchroniseToken> rtiActivityTokens;
	private LinkedList<SynchroniseToken> rescheduleList;
	private WorkwayModel model;
	private boolean breakExecution = false;
	private boolean rescheduleExecution = false;
	private boolean replacedToken = false;

	public RTITimelineSynchronizer(WorkwayModel model) {
		this.advanceTimeTokens = new LinkedList<SynchroniseToken>();
		this.rtiActivityTokens = new LinkedList<SynchroniseToken>();
		this.rescheduleList = new LinkedList<SynchroniseToken>();
		this.model = model;
	}

	public boolean putToken(SynchroniseToken token, boolean forcedOverride) {

		Human h = (Human) token.getEntity();
//		
//		if(!forcedOverride && (h.getTaToken() != null) && token.getTokenSynchroType().equals(SynchronisedActionTypen.ADVANCE_TIME)) {
//			
//			printTokenOccupationNames();
//		
//			return false;
//		}
//		


		if (token.getTokenSynchroType().equals(SynchronisedActionTypen.RTI_ACTION)) {
			rtiActivityTokens.add(token);
			((Human) token.getEntity()).addRegToken(token);
			printTokenOccupationNames();
			return true;
		}

		if (token.getTokenSynchroType().equals(SynchronisedActionTypen.ADVANCE_TIME)) {
			if (h.getTaToken() != null) {
				if (forcedOverride) {
					for (int i = 0; i < advanceTimeTokens.size(); i++) {
						if (token.getEntity().getName().equals(advanceTimeTokens.get(i).getEntity().getName())) {
//							Utils.log(token.getEntity(), "In replacement");
							advanceTimeTokens.set(i, token);
							h.setTaToken(token);
							break;
						}
					}
				} else {
					Utils.log(token.getEntity(), "Denied: " + token.getReturnEvent().getName() + ":"
							+ token.getEntity().getName() + "->" + token.getReturnEventTimepoint()
							+ " due to already existing TA Token with returnEvent "
							+ h.getTaToken().getReturnEvent().getName() + "-> "
							+ h.getTaToken().getReturnEventTimepoint() + ":" + h.getTaToken().getEntity().getName());
					return false;
				}
			} else {
//				Utils.log(h, "Putting Elsewise");
				advanceTimeTokens.add(token);
				h.setTaToken(token);
			}

		}

		if (((WorkwayModel) h.getModel()).getComponent().isAdvancingTime()) {
			replacedToken = true;
			return false;
		}

		return checkAndExecute();
	}

	@Override
	public void sortTokens() {

		Collections.sort(advanceTimeTokens);
		Collections.sort(rtiActivityTokens);

	}

	public boolean checkAndExecute() {
		if (checkForExecution()) {
			executeTimeorderedEvents();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void executeTimeorderedEvents() {

		breakExecution = false;
		sortTokens();
//		printTokenOccupationNames();

		int counter = 0;
		int j = 0;
		double ts = 0.0;
		SynchroniseToken actionTok;
		SynchroniseToken timeAdvanceTok = null;

		timeAdvanceTok = advanceTimeTokens.pop();
		((Human) timeAdvanceTok.getEntity()).setTaToken(null);
		for (; j < rtiActivityTokens.size(); j++) {
			actionTok = rtiActivityTokens.get(j);
			if (timeAdvanceTok.getReturnEventTimepoint() > actionTok.getReturnEventTimepoint()) {
				actionTok.executeAction();
				((Human) actionTok.getEntity()).removeRegToken(actionTok);
				counter++;
			} else {
				break;
			}
		}

		for (int i = 0; i < counter; i++) {
			if (rtiActivityTokens.size() != 0) {
				rtiActivityTokens.pop();
			}
		}

		// TODO time step independent in two executions ==> H0 Advance by Far (e.g.
		// 27000s) H1 advance by 10
		// next execution H1 advance by 28800 --> large difference between RTI and
		// FedTime --> cannot schedule in past.
		// Large difference between RTI and FedTime.

//			Utils.log("TS: " + timeAdvanceTok.getTimeStep() );

//			Utils.log(timeAdvanceTok.getEntity(), "Executing TimeAdvance Token");
		timeAdvanceTok.executeAction();

		if (replacedToken) {
			sortTokens();
			SynchroniseToken t = advanceTimeTokens.peek();
			if (timeAdvanceTok.getReturnEventTimepoint() > t.getReturnEventTimepoint()) {
				putToken(timeAdvanceTok, false);

				TimeAdvanceSynchronisationEvent e = new TimeAdvanceSynchronisationEvent(model, "TimeAdvanceEvent",
						t.getReturnEvent(), t.getReturnEventTimestep());
				Utils.log(timeAdvanceTok.getEntity(), "Scheduling TA synch");
				e.schedule(t.getEntity(), 0);
				replacedToken = false;
				return;
			}
		}

		replacedToken = false;

		scheduleReturnEvent(timeAdvanceTok);

//			Utils.log("!!!!!!!!End Execution!!!!!!!!!!!");
	}

	@Override
	public void scheduleReturnEvent(SynchroniseToken token) {
//		Utils.log(token.getEntity(), "Scheduling:" + token.getReturnEvent().getName() + " for " + token.getReturnEventTimepoint() + ":" + token.getReturnEventTimestep());
//		Utils.log("CT " + token.getEntity().getModel().getSimulationControl().getCurrentSimulationTime());
//		Utils.log("RetTS: " + token.getReturnEventTimestep());
//		Utils.log("RetTP " + token.getReturnEventTimepoint() );
		double step = token.getReturnEventTimepoint() - token.getReturnEventTimestep()
				- token.getEntity().getModel().getSimulationControl().getCurrentSimulationTime();
//		Utils.log(token.getEntity(),"ResStep " + step + "ResTime: " + (this.model.getSimulationControl().getCurrentSimulationTime() + token.getReturnEventTimestep() + step) + ":" + token.getReturnEventTimepoint());
//		Utils.log(token.getEntity(), " Event scheduled: " + token.getReturnEvent().getName());
		token.getReturnEvent().schedule(token.getEntity(), token.getReturnEventTimestep() + step);

	}

	public boolean checkForExecution() {

		if (advanceTimeTokens.size() == model.getHumans().size()) {
			return true;
		}

		return false;
	}

	@Override
	public void handleEntityLeft(AbstractSimEntityDelegator entity) {
		for (SynchroniseToken synchroniseToken : advanceTimeTokens) {
			if (synchroniseToken.getEntity().equals(entity)) {
				advanceTimeTokens.remove(synchroniseToken);
			}
		}

		for (SynchroniseToken synchroniseToken : rtiActivityTokens) {
			if (synchroniseToken.getEntity().equals(entity)) {
				rtiActivityTokens.remove(synchroniseToken);
			}
		}

		if (checkForExecution()) {
			sortTokens();
			executeTimeorderedEvents();
		}
	}

	public void printTokenOccupationNames(int i, int j) {

		String sign = "";

		System.out.println("------TokenList" + " FedTime:" + model.getComponent().getCurrentFedTime() + " SimTime:"
				+ model.getSimulationControl().getCurrentSimulationTime() + "--------");
		System.out.print("| TimeAdvances: ");

		for (int m = 0; m < advanceTimeTokens.size(); m++) {

			if ((m == i) && (i != -1)) {
				sign = "(X)";
			}
			System.out.print(advanceTimeTokens.get(m).getEntity().getName() + ":"
					+ advanceTimeTokens.get(m).getReturnEvent().getName() + " -> "
					+ advanceTimeTokens.get(m).getReturnEventTimepoint() + sign + ", ");

			sign = "";
		}

		System.out.println(" |");

		System.out.print("| Actions: ");

		for (int n = 0; n < rtiActivityTokens.size(); n++) {

			if ((n == j) && (j != -1)) {
				sign = "(X)";
			}
			System.out.print(rtiActivityTokens.get(n).getEntity().getName() + ":" + "Register Activity" + " -> "
					+ rtiActivityTokens.get(n).getResultingTimepoint() + sign + ", ");

			sign = "";
		}

		System.out.println(" |");

		System.out.print("| ExchangeList: ");

		for (int o = 0; o < rescheduleList.size(); o++) {

			System.out.print(
					rescheduleList.get(o).getEntity().getName() + ":" + rescheduleList.get(o).getReturnEvent().getName()
							+ " -> " + rescheduleList.get(o).getResultingTimepoint() + sign + ", ");

			sign = "";
		}

		System.out.println(" |");
		System.out.println("-----------------------TokenList--------------------");

	}

	public void printTokenOccupationNames() {
		printTokenOccupationNames(-1, -1);
	}

	@Override
	public boolean revokeToken(SynchroniseToken token) {

		for (SynchroniseToken synchroniseToken : advanceTimeTokens) {
			if (synchroniseToken.equals(token)) {
				advanceTimeTokens.remove(synchroniseToken);
				((Human) token.getEntity()).setTaToken(null);
				return true;
			}
		}

		for (SynchroniseToken synchroniseToken : rtiActivityTokens) {
			if (synchroniseToken.equals(token)) {
				advanceTimeTokens.remove(synchroniseToken);
				((Human) token.getEntity()).removeRegToken(token);
				return true;
			}
		}

		return false;
	}

	@Override
	public void breakExecution(AbstractSimEntityDelegator entity) {
		this.breakExecution = true;
		Utils.log(entity, "Break Received");
	}

	public void rescheduleToken(SynchroniseToken token, boolean retainOld, boolean forced) {

		// TODO Mark exchanged element as "not to delete" --> first element executes,
		// second elements receives but exchanges first --> dont delete first

		SynchroniseToken oldToken;
//		printTokenOccupationNames();
		if (token.getTokenSynchroType().equals(SynchronisedActionTypen.ADVANCE_TIME)) {
			for (int i = 0; i < advanceTimeTokens.size(); i++) {
				oldToken = advanceTimeTokens.get(i);
				if (oldToken.getEntity().getName().equals(token.getEntity().getName())) {
					if ((token.getReturnEventTimepoint() < oldToken.getReturnEventTimepoint()) || forced) {
						token.setBlockedFromRemove(true);
						advanceTimeTokens.add(token);

//						if(maxDistance < i) {
//							maxDistance = i;
//						}

						Utils.log(token.getEntity(), "Replaced on " + i);
						((Human) token.getEntity()).setTaToken(token);
						if (retainOld) {
							rescheduleList.add(oldToken);
						}

					} else {
						rescheduleList.add(token);

					}

					breakExecution = true;
					break;
				}
			}
		}

//		printTokenOccupationNames();
	}

}
