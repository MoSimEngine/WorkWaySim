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
	private WorkwayModel model;
	private boolean breakExecution = false;

	public RTITimelineSynchronizer(WorkwayModel model) {
		this.advanceTimeTokens = new LinkedList<SynchroniseToken>();
		this.rtiActivityTokens = new LinkedList<SynchroniseToken>();
		this.model = model;
	}

	public boolean putToken(SynchroniseToken token) {
		
		if((((Human) token.getEntity()).getTaToken() != null) && token.getTokenSynchroType().equals(SynchronisedActionTypen.ADVANCE_TIME)) {
			Utils.log(token.getEntity(), "Denied: " + token.getReturnEvent().getName() +":" + token.getEntity().getName() + " due to already existing TA Token with returnEvent " + (((Human) token.getEntity()).getTaToken().getReturnEvent().getName()) + ":"  + ((Human) token.getEntity()).getTaToken().getEntity().getName());
			return false;
		}
		
		if (token.getTokenSynchroType().equals(SynchronisedActionTypen.RTI_ACTION)) {
			rtiActivityTokens.add(token);
			((Human) token.getEntity()).addRegToken(token);
			return true;
		}

		for (SynchroniseToken synchroniseToken : this.advanceTimeTokens) {
			if (token.getEntity().getName() == synchroniseToken.getEntity().getName()) {
				Utils.log(token.getEntity(), "Denied: " + token.getReturnEvent().getName() + " due to " + synchroniseToken.getReturnEvent().getName());
				return false;
			}
		}

		if (token.getTokenSynchroType().equals(SynchronisedActionTypen.ADVANCE_TIME)) {
			advanceTimeTokens.add(token);
			((Human) token.getEntity()).setTaToken(token);
		}

		if (checkForExecution()) {
			sortTokens();
			executeTimeorderedEvents();
		}

		return true;
	}

	@Override
	public void sortTokens() {

		Collections.sort(advanceTimeTokens);
		Collections.sort(rtiActivityTokens);

	}

	@Override
	public void executeTimeorderedEvents() {
		
	
	
		int j = 0;
		double ts = 0.0;
		SynchroniseToken actionTok;
		SynchroniseToken timeAdvanceTok = null;
		for (int i = 0; i < advanceTimeTokens.size(); i++) {
			timeAdvanceTok = advanceTimeTokens.get(i);
			Utils.log(timeAdvanceTok.getEntity(), "At TA Pos: " + i);
			printTokenOccupationNames();
			for (; j < rtiActivityTokens.size(); j++) {
				actionTok = rtiActivityTokens.get(j);
				Utils.log(actionTok.getEntity(), "At Action Pos: " + j);
				if (timeAdvanceTok.compareTo(rtiActivityTokens.get(j)) > 0) {
					actionTok.executeAction();
					((Human)actionTok.getEntity()).removeRegToken(actionTok);
				} else {
					break;
				}
			}
			
		
			if(!(-0.000000001 < ts && ts < 0.00000001)) {
				timeAdvanceTok.reduceTimestep(ts);
			}
	
			//TODO time step independent in two executions ==> H0 Advance by Far (e.g. 27000s) H1 advance by 10
			//next execution H1 advance by 28800 --> large difference between RTI and FedTime --> cannot schedule in past.
			//Large difference between RTI and FedTime.
			
//			Utils.log("TS: " + timeAdvanceTok.getTimeStep() );
			timeAdvanceTok.executeAction();
			
			if(breakExecution) {
				printTokenOccupationNames();
				return;
			}
			ts += timeAdvanceTok.getTimeStep();
			scheduleReturnEvent(timeAdvanceTok);
			((Human)timeAdvanceTok.getEntity()).setTaToken(null);
			printTokenOccupationNames();
			
		}

		if(!breakExecution) {
			advanceTimeTokens.clear();
			rtiActivityTokens.clear();
		} 
		 		Utils.log("-----End Execution----");
	}

	@Override
	public void scheduleReturnEvent(SynchroniseToken token) {
//		Utils.log(token.getEntity(), "Scheduling:" + token.getReturnEvent().getName() + " for " + token.getReturnEventTimepoint() + ":" + token.getReturnEventTimestep());
//		Utils.log("CT " + token.getEntity().getModel().getSimulationControl().getCurrentSimulationTime());
//		Utils.log("RetTS: " + token.getReturnEventTimestep());
//		Utils.log("RetTP " + token.getReturnEventTimepoint() );
		double step = token.getReturnEventTimepoint() - token.getReturnEventTimestep() - token.getEntity().getModel().getSimulationControl().getCurrentSimulationTime();
//		Utils.log(token.getEntity(),"ResStep " + step + "ResTime: " + (this.model.getSimulationControl().getCurrentSimulationTime() + token.getReturnEventTimestep() + step) + ":" + token.getReturnEventTimepoint());
		token.getReturnEvent().schedule(token.getEntity(), token.getReturnEventTimestep() + step);
		
	}

	private boolean checkForExecution() {

	
		if(advanceTimeTokens.size() == model.getHumans().size()) {
			return true;
		}
		
		return false;
	}

	@Override
	public void handleEntityLeft(AbstractSimEntityDelegator entity) {
		for (SynchroniseToken synchroniseToken : advanceTimeTokens) {
			if(synchroniseToken.getEntity().equals(entity)) {
				advanceTimeTokens.remove(synchroniseToken);
			}
		}
		
		for (SynchroniseToken synchroniseToken : rtiActivityTokens) {
			if(synchroniseToken.getEntity().equals(entity)) {
				rtiActivityTokens.remove(synchroniseToken);
			}
		}
		
		if (checkForExecution()) {
			sortTokens();
			executeTimeorderedEvents();
		}
	}
	
	public void printTokenOccupationNames() {
		System.out.print("TimeAdvances: ");
		
		for (SynchroniseToken synchroniseToken : advanceTimeTokens) {
			System.out.print(synchroniseToken.getEntity().getName() + ":" + synchroniseToken.getReturnEvent().getName() + " -> " + synchroniseToken.getReturnEventTimepoint() + ", ");
		}
		
		System.out.println();
		
		System.out.print("Actions");
		
		
		for (SynchroniseToken synchroniseToken : rtiActivityTokens) {
			System.out.print(synchroniseToken.getEntity().getName() + " -> " + synchroniseToken.getReturnEventTimepoint() + ", ");
		}
		

		System.out.println();
		
		
	}

	@Override
	public boolean revokeToken(SynchroniseToken token) {
		
		for (SynchroniseToken synchroniseToken : advanceTimeTokens) {
			if(synchroniseToken.equals(token)) {
				advanceTimeTokens.remove(synchroniseToken);
				((Human) token.getEntity()).setTaToken(null);
				return true;
			}
		}
		
		for (SynchroniseToken synchroniseToken : rtiActivityTokens) {
			if(synchroniseToken.equals(token)) {
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

	
	

}
