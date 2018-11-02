package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position.PositionType;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class ArriveAtNextEvent extends AbstractSimEventDelegator<Human>{

	protected ArriveAtNextEvent(ISimulationModel model, String name) {
		super(model, name);
	
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		human.nextPosition();
		PositionType posType = human.getPosition().getPositionType();
		PositionType destType = human.getDestination().getPositionType();
		
		
		switch (posType) {
		case BUSSTOP:
			human.arriveAtBusStop();
			if(destType.equals(PositionType.BUSSTOP)) {
				RegisterAtBusStopEvent e = new RegisterAtBusStopEvent(getModel(), "Register at BusStop");
				if(HumanSimValues.FULL_SYNC) {
					m.getComponent().synchronisedAdvancedTime(0, e, human);
				} else {
					e.schedule(human, 0);
				}
				
				
				break;
			} else {
				TravelToNextEvent e = new TravelToNextEvent(getModel(), "Travel to next position");
				
				if(HumanSimValues.FULL_SYNC) {
					m.getComponent().synchronisedAdvancedTime(0, e, human);
				} else {
					e.schedule(human, 0);
				}
				
				break;
			}

		case HOME:
			Utils.log(human, human.getName() + " arrives at home. Afterwork Party!");
			human.arriveAtHome();
			HumanLivingHisLifeEvent livingEvent = new HumanLivingHisLifeEvent(human.getModel(), "Human is living his life");
			//
			
			if(HumanSimValues.FULL_SYNC) {
				m.getComponent().synchronisedAdvancedTime(0, livingEvent, human);
			} else {
				livingEvent.schedule(human, 0);
			}
			break;
			
		case WORK:
			human.arriveAtWork();
			Utils.log(human, human.getName() + " arrives at work.");
			HumanWorksEvent workingEvent = new HumanWorksEvent(this.getModel(), "Human Works");
			
			if(HumanSimValues.FULL_SYNC) {
				m.getComponent().synchronisedAdvancedTime(0, workingEvent, human);
			} else {
				workingEvent.schedule(human, 0);
			}
			
			//
			break;

		default:
			Utils.log(human, "Arrived at no known position");
			break;
		}
		
		
	}
	
}
