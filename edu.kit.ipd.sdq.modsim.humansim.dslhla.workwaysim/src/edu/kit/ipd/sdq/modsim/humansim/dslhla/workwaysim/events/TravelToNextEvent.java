package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Token;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position.PositionType;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class TravelToNextEvent extends AbstractSimEventDelegator<Token>{

	public TravelToNextEvent(ISimulationModel model, String name) {
		super(model, name);
	}

	@Override
	public void eventRoutine(Token token) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		PositionType posType = token.getPosition().getPositionType();
		PositionType destType = token.getDestination().getPositionType();
		
		double travelTime;
		String eventName = "";
		
		switch (posType) {
		case QUEUE:
			
			switch (destType) {
			case QUEUE:
				travelTime = token.getEnqueuingDelayInSeconds();
				eventName = "Enqueing Again";
				token.enqueueAgain();
				break;
			default:
				throw new IllegalStateException("No way from busStop to destination");
			}
			
			break;	
		default:
			throw new IllegalStateException("No valid position  to travel to");
		}
		
		ArriveAtNextEvent e = new ArriveAtNextEvent(getModel(), eventName);
		TimeAdvanceToken advTimeToken = new TimeAdvanceToken(e, token, travelTime);
		m.getTimelineSynchronizer().putToken(advTimeToken, false);
	}
}
