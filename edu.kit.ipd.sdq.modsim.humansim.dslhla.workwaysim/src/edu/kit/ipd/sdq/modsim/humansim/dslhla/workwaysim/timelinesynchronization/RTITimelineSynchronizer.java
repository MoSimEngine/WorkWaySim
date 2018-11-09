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
	private int entityCounter;

	public RTITimelineSynchronizer(WorkwayModel model) {
		this.advanceTimeTokens = new LinkedList<SynchroniseToken>();
		this.rtiActivityTokens = new LinkedList<SynchroniseToken>();
		this.model = model;
		this.entityCounter = 0;
	}

	public boolean putToken(SynchroniseToken token) {

		if (token.getTokenSynchroType().equals(SynchronisedActionTypen.RTI_ACTION)) {
			rtiActivityTokens.add(token);
			((Human) token.getEntity()).addRegToken(token);
			return true;
		}

		for (SynchroniseToken synchroniseToken : this.advanceTimeTokens) {
			if (token.getEntity().getName() == synchroniseToken.getEntity().getName()) {
				Utils.log(token.getEntity(), "Denied: " + token.getReturnEvent().getName());
				return false;
			}
		}

		if (token.getTokenSynchroType().equals(SynchronisedActionTypen.ADVANCE_TIME)) {
			advanceTimeTokens.add(token);
			((Human) token.getEntity()).setTaToken(token);
			entityCounter++;
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
			for (; j < rtiActivityTokens.size(); j++) {
				actionTok = rtiActivityTokens.get(j);
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
			timeAdvanceTok.executeAction();
			ts += timeAdvanceTok.getTimeStep();
			scheduleReturnEvent(timeAdvanceTok);
			((Human)timeAdvanceTok.getEntity()).setTaToken(null);
		}

		advanceTimeTokens.clear();
		rtiActivityTokens.clear();
		
		entityCounter = 0;

	}

	@Override
	public void scheduleReturnEvent(SynchroniseToken token) {
		Utils.log(token.getEntity(), token.getReturnEvent().toString());
		token.getReturnEvent().schedule(token.getEntity(), token.getReturnEventTimestep());
	}

	private boolean checkForExecution() {

	
		if(entityCounter == model.getHumans().size()) {
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
			System.out.print(synchroniseToken.getEntity().getName() + ", ");
		}
		
		System.out.println();
		
		System.out.print("Actions");
		
		
		for (SynchroniseToken synchroniseToken : rtiActivityTokens) {
			System.out.print(synchroniseToken.getEntity().getName() + ", ");
		}
		

		System.out.println();
		
		
	}

	@Override
	public boolean revokeToken(SynchroniseToken token) {
		for (SynchroniseToken synchroniseToken : advanceTimeTokens) {
			if(synchroniseToken.equals(token)) {
				advanceTimeTokens.remove(synchroniseToken);
				((Human) token.getEntity()).setTaToken(null);
				entityCounter--;
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
	
	

}
